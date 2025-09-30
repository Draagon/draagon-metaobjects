package com.metaobjects.generator.mustache;

import com.metaobjects.object.MetaObject;
import com.metaobjects.field.MetaField;
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
        return generateCode(template, metaObject, null, null);
    }
    
    /**
     * Generate code from a template and MetaObject with package prefix support.
     * 
     * @param template the template definition containing the Mustache template
     * @param metaObject the MetaObject to generate code for
     * @param packagePrefix optional package prefix to prepend to the package name
     * @param packagePostfix optional package postfix to append to the package name
     * @return the generated code as a string
     * @throws RuntimeException if code generation fails
     */
    public String generateCode(TemplateDefinition template, MetaObject metaObject, String packagePrefix, String packagePostfix) {
        try {
            // Create template context
            Map<String, Object> context = createTemplateContext(template, metaObject, packagePrefix, packagePostfix);
            
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
        return createTemplateContext(template, metaObject, packagePrefix, null);
    }

    /**
     * Create template context with package prefix and postfix support.
     */
    private Map<String, Object> createTemplateContext(TemplateDefinition template, MetaObject metaObject, String packagePrefix, String packagePostfix) {
        Map<String, Object> context = new HashMap<>();
        
        // Basic object information
        String fullName = metaObject.getName();
        String className = fullName.contains("::") ?
            fullName.substring(fullName.lastIndexOf("::") + 2) : fullName;
        String qualifiedName = (metaObject.getPackage() != null && !metaObject.getPackage().isEmpty()) ?
            metaObject.getPackage() + "::" + metaObject.getName() : metaObject.getName();

        String packageName = getPackageName(metaObject, packagePrefix, packagePostfix);

        context.put("className", className);
        context.put("packageName", packageName);
        context.put("package", packageName); // Template uses {{package}}
        context.put("fullName", qualifiedName);
        context.put("metaObjectName", qualifiedName); // For managed-object template constants
        context.put("imports", getRequiredImports(template, metaObject));
        
        // Database-specific context
        context.put("dbTableName", helperRegistry.get("dbTableName").apply(metaObject));
        
        // Fields with helper function processing
        List<Map<String, Object>> fields = new ArrayList<>();
        List<MetaField> metaFields = metaObject.getChildren(MetaField.class);
        for (int i = 0; i < metaFields.size(); i++) {
            MetaField field = metaFields.get(i);
            Map<String, Object> fieldContext = createFieldContext(field);
            boolean isLast = i == metaFields.size() - 1;
            fieldContext.put("isLast", isLast);
            fieldContext.put("hasNext", !isLast);
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
        fieldContext.put("fieldName", fieldName); // Alias for template consistency

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
        fieldContext.put("capitalizedFieldName", capitalizedName); // Alias for template consistency
        fieldContext.put("description", field.hasMetaAttr("description") ? field.getMetaAttr("description").getValueAsString() : "Property " + field.getName());

        // Add template-specific variables for managed-object template
        fieldContext.put("constantFieldName", helperRegistry.get("constantFieldName").apply(field));
        fieldContext.put("isBoolean", helperRegistry.get("isBoolean").apply(field));
        
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
        return getPackageName(metaObject, packagePrefix, null);
    }

    /**
     * Get the package name for a MetaObject with prefix and postfix support.
     */
    private String getPackageName(MetaObject metaObject, String packagePrefix, String packagePostfix) {
        // Get package from metadata first
        String metadataPackage = metaObject.getPackage();
        if (metadataPackage == null || metadataPackage.isEmpty()) {
            // Fallback to package attribute
            if (metaObject.hasMetaAttr("package")) {
                metadataPackage = metaObject.getMetaAttr("package").getValueAsString();
            } else {
                metadataPackage = "generated";
            }
        }

        // Keep metadata package name as specified (no underscore-to-dot conversion)
        String packageName;

        // If packagePrefix is provided, combine it with metadata package
        if (packagePrefix != null && !packagePrefix.trim().isEmpty()) {
            packageName = packagePrefix.trim() + "." + metadataPackage;
        } else {
            packageName = metadataPackage;
        }

        // Append package postfix if provided
        if (packagePostfix != null && !packagePostfix.trim().isEmpty()) {
            if (!packageName.endsWith(".")) {
                packageName += ".";
            }
            packageName += packagePostfix.trim();
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