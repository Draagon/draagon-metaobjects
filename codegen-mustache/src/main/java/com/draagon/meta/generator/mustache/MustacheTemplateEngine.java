package com.draagon.meta.generator.mustache;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.field.MetaField;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mustache-based template engine for generating code from MetaObjects.
 * Uses JMustache library for template processing and integrates with
 * the HelperRegistry for Java-specific helper functions.
 * 
 * Based on the cross-language template system architecture documented in
 * .claude/archive/template-system/TEMPLATE_IMPLEMENTATION_GUIDE.md
 */
public class MustacheTemplateEngine {
    
    private final MustacheFactory mustacheFactory;
    private final HelperRegistry helperRegistry;
    private final Map<String, Mustache> templateCache;
    
    public MustacheTemplateEngine() {
        this.mustacheFactory = new DefaultMustacheFactory();
        this.helperRegistry = new HelperRegistry();
        this.templateCache = new ConcurrentHashMap<>();
    }
    
    /**
     * Generate code using the specified template and MetaObject.
     * 
     * @param template the template definition containing the Mustache template
     * @param metaObject the MetaObject to generate code for
     * @return the generated code as a string
     * @throws RuntimeException if code generation fails
     */
    public String generateCode(TemplateDefinition template, MetaObject metaObject) {
        return generateCode(template, metaObject, null);
    }
    
    /**
     * Generate code from a template and MetaObject with package prefix support.
     * 
     * @param template the template definition containing the Mustache template
     * @param metaObject the MetaObject to generate code for
     * @param packagePrefix optional package prefix to prepend to the package name
     * @return the generated code as a string
     * @throws RuntimeException if code generation fails
     */
    public String generateCode(TemplateDefinition template, MetaObject metaObject, String packagePrefix) {
        try {
            // Create template context
            Map<String, Object> context = createTemplateContext(template, metaObject, packagePrefix);
            
            // Compile mustache template (with caching)
            Mustache mustache = getCompiledTemplate(template);
            
            // Generate code
            StringWriter writer = new StringWriter();
            mustache.execute(writer, context);
            
            return writer.toString();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate code for template: " + template.getName(), e);
        }
    }
    
    /**
     * Get or compile a Mustache template, using caching for performance.
     */
    private Mustache getCompiledTemplate(TemplateDefinition template) {
        return templateCache.computeIfAbsent(template.getName(), 
            name -> mustacheFactory.compile(
                new StringReader(template.getTemplate()), 
                name
            )
        );
    }
    
    /**
     * Create the template context with all necessary data and helper functions.
     */
    private Map<String, Object> createTemplateContext(TemplateDefinition template, MetaObject metaObject) {
        return createTemplateContext(template, metaObject, null);
    }
    
    /**
     * Create the template context with all necessary data and helper functions, with package prefix support.
     */
    private Map<String, Object> createTemplateContext(TemplateDefinition template, MetaObject metaObject, String packagePrefix) {
        Map<String, Object> context = new HashMap<>();
        
        // Basic object information
        String fullName = metaObject.getName();
        String className = fullName.contains("::") ? 
            fullName.substring(fullName.lastIndexOf("::") + 2) : fullName;
        context.put("className", className);
        context.put("packageName", getPackageName(metaObject, packagePrefix));
        context.put("fullName", metaObject.getPackage() + "::" + metaObject.getName());
        context.put("imports", getRequiredImports(template, metaObject));
        
        // Database-specific context
        context.put("dbTableName", helperRegistry.get("dbTableName").apply(metaObject));
        
        // Fields with helper function processing
        List<Map<String, Object>> fields = new ArrayList<>();
        List<MetaField> metaFields = metaObject.getChildren(MetaField.class);
        for (int i = 0; i < metaFields.size(); i++) {
            MetaField field = metaFields.get(i);
            Map<String, Object> fieldContext = createFieldContext(field);
            fieldContext.put("isLast", i == metaFields.size() - 1);
            fields.add(fieldContext);
        }
        context.put("fields", fields);
        
        // Required fields (non-ID fields that are required)
        List<Map<String, Object>> requiredFields = fields.stream()
            .filter(field -> !(Boolean) field.get("isIdField") && !(Boolean) field.get("isNullable"))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        context.put("requiredFields", requiredFields);
        
        // Add helper functions as lambda expressions
        addHelperFunctions(context);
        
        // Add template-specific flags
        addTemplateFlags(context, template);
        
        return context;
    }
    
    /**
     * Create context map for a single field with all helper function results.
     */
    private Map<String, Object> createFieldContext(MetaField field) {
        Map<String, Object> fieldContext = new HashMap<>();
        
        String fieldName = field.getName();
        fieldContext.put("name", fieldName);
        
        // Pre-compute method names
        String capitalizedName = helperRegistry.get("capitalize").apply(fieldName).toString();
        fieldContext.put("getterName", "get" + capitalizedName);
        fieldContext.put("setterName", "set" + capitalizedName);
        fieldContext.put("hasMethodName", "has" + capitalizedName);
        fieldContext.put("javaType", helperRegistry.get("javaType").apply(field));
        fieldContext.put("dbColumnName", helperRegistry.get("dbColumnName").apply(field));
        fieldContext.put("isIdField", helperRegistry.get("isIdField").apply(field));
        fieldContext.put("isNullable", helperRegistry.get("isNullable").apply(field));
        fieldContext.put("isPrimitive", helperRegistry.get("isPrimitive").apply(field));
        fieldContext.put("isSearchable", helperRegistry.get("isSearchable").apply(field));
        fieldContext.put("capitalizedName", helperRegistry.get("capitalize").apply(field.getName()));
        fieldContext.put("description", field.hasMetaAttr("description") ? field.getMetaAttr("description").getValueAsString() : "Property " + field.getName());
        
        // Additional field properties
        fieldContext.put("hasDefaultValue", field.hasMetaAttr("defaultValue"));
        if (field.hasMetaAttr("defaultValue")) {
            fieldContext.put("defaultValue", field.getMetaAttr("defaultValue").getValueAsString());
        }
        
        return fieldContext;
    }
    
    /**
     * Add helper functions as lambda expressions for template use.
     */
    private void addHelperFunctions(Map<String, Object> context) {
        // Add commonly used helper functions as functions that can be called in templates
        // Note: Mustache lambdas are complex, so we pre-compute values in the context instead
        // The actual lambda functionality is handled by pre-processing field contexts
    }
    
    /**
     * Add template-specific flags based on template name and attributes.
     */
    private void addTemplateFlags(Map<String, Object> context, TemplateDefinition template) {
        String templateName = template.getName().toLowerCase();
        
        // Template type flags
        context.put("hasJpa", templateName.contains("jpa"));
        context.put("hasValidation", templateName.contains("validation"));
        context.put("hasAuditing", templateName.contains("audit"));
        context.put("hasSpring", templateName.contains("spring"));
    }
    
    /**
     * Get the package name for the MetaObject.
     */
    private String getPackageName(MetaObject metaObject) {
        return getPackageName(metaObject, null);
    }
    
    private String getPackageName(MetaObject metaObject, String packagePrefix) {
        // First try to get from package property
        String packageName = metaObject.getPackage();
        if (packageName == null || packageName.isEmpty()) {
            // Fallback to package attribute
            if (metaObject.hasMetaAttr("package")) {
                packageName = metaObject.getMetaAttr("package").getValueAsString();
            } else {
                packageName = "com.example.generated";
            }
        }
        
        // Apply package prefix if provided
        if (packagePrefix != null && !packagePrefix.trim().isEmpty()) {
            packageName = packagePrefix + "." + packageName;
        }
        
        return packageName;
    }
    
    /**
     * Get required imports based on template type and field types.
     */
    private List<String> getRequiredImports(TemplateDefinition template, MetaObject metaObject) {
        Set<String> imports = new HashSet<>();
        
        // Add JPA imports if this is a JPA template
        if (template.getName().toLowerCase().contains("jpa")) {
            imports.add("javax.persistence.*");
            imports.add("java.io.Serializable");
        }
        
        // Add Spring imports if this is a Spring template
        if (template.getName().toLowerCase().contains("spring")) {
            imports.add("org.springframework.data.jpa.repository.JpaRepository");
            imports.add("org.springframework.data.jpa.repository.Query");
            imports.add("org.springframework.data.repository.query.Param");
            imports.add("org.springframework.stereotype.Repository");
        }
        
        // Add imports based on field types
        for (MetaField field : metaObject.getChildren(MetaField.class)) {
            Object javaImport = helperRegistry.get("javaImport").apply(field);
            if (javaImport != null) {
                imports.add(javaImport.toString());
            }
        }
        
        // Add common utility imports
        imports.add("java.util.Objects");
        
        return new ArrayList<>(imports);
    }
    
    /**
     * Get the helper registry for testing purposes.
     */
    public HelperRegistry getHelperRegistry() {
        return helperRegistry;
    }
    
    /**
     * Clear the template cache (useful for testing).
     */
    public void clearCache() {
        templateCache.clear();
    }
}