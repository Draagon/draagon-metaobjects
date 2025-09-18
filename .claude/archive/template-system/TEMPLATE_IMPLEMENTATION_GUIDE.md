# MetaObjects Mustache Template System Implementation Guide

## Overview

This guide provides step-by-step instructions for implementing the Mustache-based cross-language template system for MetaObjects. Follow this guide to migrate from the current YAML/JSON template approach to the new Mustache + Helper Functions architecture.

## Prerequisites

- Java 21+ development environment
- Maven 3.8+ build system
- Understanding of MetaObjects v6.0.0 service architecture
- Familiarity with existing codegen module structure

## Phase 1: Java Foundation Implementation

### Step 1: Add Mustache Dependencies

**Update `codegen/pom.xml`:**
```xml
<dependencies>
    <!-- Existing dependencies -->
    
    <!-- Mustache Template Engine -->
    <dependency>
        <groupId>com.github.spullara.mustache.java</groupId>
        <artifactId>compiler</artifactId>
        <version>0.9.10</version>
    </dependency>
    
    <!-- YAML parsing for template metadata -->
    <dependency>
        <groupId>com.fasterxml.jackson.dataformat</groupId>
        <artifactId>jackson-dataformat-yaml</artifactId>
        <version>2.15.2</version>
    </dependency>
    
    <!-- Template validation -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
        <version>3.12.0</version>
    </dependency>
</dependencies>
```

### Step 2: Create Template Data Structures

**Create `codegen/src/main/java/com/draagon/meta/generator/mustache/TemplateDefinition.java`:**
```java
package com.draagon.meta.generator.mustache;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateDefinition {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("version")
    private String version = "1.0.0";
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("targetLanguage")
    private String targetLanguage;
    
    @JsonProperty("outputFileExtension")
    private String outputFileExtension;
    
    @JsonProperty("packagePath")
    private boolean packagePath = true;
    
    @JsonProperty("requirements")
    private TemplateRequirements requirements;
    
    @JsonProperty("template")
    private String template;
    
    @JsonProperty("partials")
    private Map<String, String> partials;
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
    
    public String getOutputFileExtension() { return outputFileExtension; }
    public void setOutputFileExtension(String outputFileExtension) { this.outputFileExtension = outputFileExtension; }
    
    public boolean isPackagePath() { return packagePath; }
    public void setPackagePath(boolean packagePath) { this.packagePath = packagePath; }
    
    public TemplateRequirements getRequirements() { return requirements; }
    public void setRequirements(TemplateRequirements requirements) { this.requirements = requirements; }
    
    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }
    
    public Map<String, String> getPartials() { return partials; }
    public void setPartials(Map<String, String> partials) { this.partials = partials; }
    
    public static class TemplateRequirements {
        @JsonProperty("attributes")
        private List<String> attributes;
        
        @JsonProperty("helpers")
        private List<String> helpers;
        
        public List<String> getAttributes() { return attributes; }
        public void setAttributes(List<String> attributes) { this.attributes = attributes; }
        
        public List<String> getHelpers() { return helpers; }
        public void setHelpers(List<String> helpers) { this.helpers = helpers; }
    }
}
```

### Step 3: Create Helper Function System

**Create `codegen/src/main/java/com/draagon/meta/generator/mustache/HelperRegistry.java`:**
```java
package com.draagon.meta.generator.mustache;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.field.MetaField;
import org.apache.commons.lang3.StringUtils;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;

public class HelperRegistry {
    
    private final Map<String, Function<Object, Object>> helpers = new HashMap<>();
    
    public HelperRegistry() {
        registerDefaultHelpers();
    }
    
    private void registerDefaultHelpers() {
        // String manipulation helpers
        register("capitalize", this::capitalize);
        register("camelCase", this::camelCase);
        register("pascalCase", this::pascalCase);
        register("upperCase", this::upperCase);
        register("lowerCase", this::lowerCase);
        
        // Java type helpers
        register("javaType", this::getJavaType);
        register("isPrimitive", this::isPrimitive);
        register("isCollection", this::isCollection);
        
        // Database helpers
        register("dbColumnName", this::getDbColumnName);
        register("dbTableName", this::getDbTableName);
        register("isIdField", this::isIdField);
        register("isNullable", this::isNullable);
        
        // MetaObjects helpers
        register("hasAttribute", this::hasAttribute);
        register("getAttributeValue", this::getAttributeValue);
        register("getFieldsByType", this::getFieldsByType);
    }
    
    public void register(String name, Function<Object, Object> helper) {
        helpers.put(name, helper);
    }
    
    public Function<Object, Object> get(String name) {
        return helpers.get(name);
    }
    
    public boolean contains(String name) {
        return helpers.containsKey(name);
    }
    
    // Helper function implementations
    private Object capitalize(Object input) {
        return input != null ? StringUtils.capitalize(input.toString()) : null;
    }
    
    private Object camelCase(Object input) {
        if (input == null) return null;
        String str = input.toString();
        return StringUtils.uncapitalize(pascalCase(str).toString());
    }
    
    private Object pascalCase(Object input) {
        if (input == null) return null;
        String str = input.toString();
        return Arrays.stream(str.split("[_\\s]+"))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining());
    }
    
    private Object upperCase(Object input) {
        return input != null ? input.toString().toUpperCase() : null;
    }
    
    private Object lowerCase(Object input) {
        return input != null ? input.toString().toLowerCase() : null;
    }
    
    private Object getJavaType(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            String dataType = field.getDataType();
            
            switch (dataType) {
                case "string": return "String";
                case "int": return "Integer";
                case "long": return "Long";
                case "boolean": return "Boolean";
                case "date": return "java.time.LocalDate";
                case "datetime": return "java.time.LocalDateTime";
                case "decimal": return "java.math.BigDecimal";
                default: return "Object";
            }
        }
        return "Object";
    }
    
    private Object isPrimitive(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            String dataType = field.getDataType();
            return Arrays.asList("int", "long", "boolean", "double", "float").contains(dataType);
        }
        return false;
    }
    
    private Object isCollection(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            return field.hasAttr("collection") && Boolean.parseBoolean(field.getAttrValueAsString("collection"));
        }
        return false;
    }
    
    private Object getDbColumnName(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            return field.hasAttr("dbColumn") ? field.getAttrValueAsString("dbColumn") : field.getName();
        }
        return null;
    }
    
    private Object getDbTableName(Object input) {
        if (input instanceof MetaObject) {
            MetaObject metaObject = (MetaObject) input;
            return metaObject.hasAttr("dbTable") ? metaObject.getAttrValueAsString("dbTable") : metaObject.getName();
        }
        return null;
    }
    
    private Object isIdField(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            return field.hasAttr("isId") && Boolean.parseBoolean(field.getAttrValueAsString("isId"));
        }
        return false;
    }
    
    private Object isNullable(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            return !field.hasAttr("required") || !Boolean.parseBoolean(field.getAttrValueAsString("required"));
        }
        return true;
    }
    
    private Object hasAttribute(Object input) {
        // Implementation for checking if MetaObject/MetaField has specific attribute
        return false; // Placeholder
    }
    
    private Object getAttributeValue(Object input) {
        // Implementation for getting attribute value
        return null; // Placeholder
    }
    
    private Object getFieldsByType(Object input) {
        // Implementation for filtering fields by type
        return null; // Placeholder
    }
}
```

### Step 4: Create Mustache Template Engine

**Create `codegen/src/main/java/com/draagon/meta/generator/mustache/MustacheTemplateEngine.java`:**
```java
package com.draagon.meta.generator.mustache;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.field.MetaField;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

public class MustacheTemplateEngine {
    
    private final MustacheFactory mustacheFactory;
    private final HelperRegistry helperRegistry;
    
    public MustacheTemplateEngine() {
        this.mustacheFactory = new DefaultMustacheFactory();
        this.helperRegistry = new HelperRegistry();
    }
    
    public String generateCode(TemplateDefinition template, MetaObject metaObject) {
        try {
            // Create template context
            Map<String, Object> context = createTemplateContext(template, metaObject);
            
            // Compile mustache template
            Mustache mustache = mustacheFactory.compile(
                new StringReader(template.getTemplate()), 
                template.getName()
            );
            
            // Generate code
            StringWriter writer = new StringWriter();
            mustache.execute(writer, context);
            
            return writer.toString();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate code for template: " + template.getName(), e);
        }
    }
    
    private Map<String, Object> createTemplateContext(TemplateDefinition template, MetaObject metaObject) {
        Map<String, Object> context = new HashMap<>();
        
        // Basic object information
        context.put("className", metaObject.getName());
        context.put("packageName", getPackageName(metaObject));
        context.put("imports", getRequiredImports(template, metaObject));
        
        // Database-specific context
        context.put("dbTableName", helperRegistry.get("dbTableName").apply(metaObject));
        
        // Fields with helper function processing
        List<Map<String, Object>> fields = new ArrayList<>();
        for (MetaField field : metaObject.getFields()) {
            Map<String, Object> fieldContext = createFieldContext(field);
            fields.add(fieldContext);
        }
        context.put("fields", fields);
        
        // Add helper functions as lambda expressions
        addHelperFunctions(context);
        
        return context;
    }
    
    private Map<String, Object> createFieldContext(MetaField field) {
        Map<String, Object> fieldContext = new HashMap<>();
        
        fieldContext.put("name", field.getName());
        fieldContext.put("javaType", helperRegistry.get("javaType").apply(field));
        fieldContext.put("dbColumnName", helperRegistry.get("dbColumnName").apply(field));
        fieldContext.put("isIdField", helperRegistry.get("isIdField").apply(field));
        fieldContext.put("isNullable", helperRegistry.get("isNullable").apply(field));
        fieldContext.put("isPrimitive", helperRegistry.get("isPrimitive").apply(field));
        fieldContext.put("capitalizedName", helperRegistry.get("capitalize").apply(field.getName()));
        
        return fieldContext;
    }
    
    private void addHelperFunctions(Map<String, Object> context) {
        // Add commonly used helper functions as lambdas for template use
        context.put("capitalize", (Mustache.Lambda) (frag, out) -> {
            Object result = helperRegistry.get("capitalize").apply(frag.execute());
            out.write(result != null ? result.toString() : "");
        });
        
        context.put("camelCase", (Mustache.Lambda) (frag, out) -> {
            Object result = helperRegistry.get("camelCase").apply(frag.execute());
            out.write(result != null ? result.toString() : "");
        });
        
        // Add more helper lambdas as needed
    }
    
    private String getPackageName(MetaObject metaObject) {
        if (metaObject.hasAttr("package")) {
            return metaObject.getAttrValueAsString("package");
        }
        return "com.example.generated";
    }
    
    private List<String> getRequiredImports(TemplateDefinition template, MetaObject metaObject) {
        Set<String> imports = new HashSet<>();
        
        // Add JPA imports if this is a JPA template
        if (template.getName().toLowerCase().contains("jpa")) {
            imports.add("javax.persistence.*");
        }
        
        // Add imports based on field types
        for (MetaField field : metaObject.getFields()) {
            String javaType = (String) helperRegistry.get("javaType").apply(field);
            if (javaType.contains(".")) {
                imports.add(javaType);
            }
        }
        
        return new ArrayList<>(imports);
    }
}
```

### Step 5: Create Template Parser

**Create `codegen/src/main/java/com/draagon/meta/generator/mustache/TemplateParser.java`:**
```java
package com.draagon.meta.generator.mustache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TemplateParser {
    
    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;
    
    public TemplateParser() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.jsonMapper = new ObjectMapper();
    }
    
    public TemplateDefinition parseTemplate(String content, boolean isYaml) throws IOException {
        ObjectMapper mapper = isYaml ? yamlMapper : jsonMapper;
        return mapper.readValue(content, TemplateDefinition.class);
    }
    
    public TemplateDefinition parseTemplateFromFile(String filePath) throws IOException {
        boolean isYaml = filePath.endsWith(".yaml") || filePath.endsWith(".yml");
        
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new IOException("Template file not found: " + filePath);
            }
            
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return parseTemplate(content, isYaml);
        }
    }
    
    public void validateTemplate(TemplateDefinition template) {
        if (template.getName() == null || template.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Template name is required");
        }
        
        if (template.getTemplate() == null || template.getTemplate().trim().isEmpty()) {
            throw new IllegalArgumentException("Template content is required");
        }
        
        if (template.getTargetLanguage() == null || template.getTargetLanguage().trim().isEmpty()) {
            throw new IllegalArgumentException("Target language is required");
        }
        
        // Validate required helpers are available
        if (template.getRequirements() != null && template.getRequirements().getHelpers() != null) {
            HelperRegistry helperRegistry = new HelperRegistry();
            for (String helperName : template.getRequirements().getHelpers()) {
                if (!helperRegistry.contains(helperName)) {
                    throw new IllegalArgumentException("Required helper not available: " + helperName);
                }
            }
        }
    }
}
```

### Step 6: Create Maven Plugin Integration

**Create `codegen/src/main/java/com/draagon/meta/generator/mustache/MustacheTemplateGenerator.java`:**
```java
package com.draagon.meta.generator.mustache;

import com.draagon.meta.generator.direct.MultiFileDirectGeneratorBase;
import com.draagon.meta.generator.io.GeneratorIOException;
import com.draagon.meta.generator.io.GeneratorIOManager;
import com.draagon.meta.generator.param.GeneratorParam;
import com.draagon.meta.object.MetaObject;
import java.io.IOException;
import java.util.List;

public class MustacheTemplateGenerator extends MultiFileDirectGeneratorBase {
    
    private MustacheTemplateEngine templateEngine;
    private TemplateParser templateParser;
    private String templatePath;
    
    @Override
    public void init(GeneratorParam param) throws GeneratorIOException {
        super.init(param);
        
        this.templateEngine = new MustacheTemplateEngine();
        this.templateParser = new TemplateParser();
        this.templatePath = param.getStringValue("templatePath", "templates/");
        
        if (!templatePath.endsWith("/")) {
            templatePath += "/";
        }
    }
    
    @Override
    protected void generateFiles(MetaObject metaObject, GeneratorIOManager manager) throws GeneratorIOException {
        try {
            // Load templates from classpath or file system
            List<TemplateDefinition> templates = loadTemplatesForObject(metaObject);
            
            for (TemplateDefinition template : templates) {
                generateFileFromTemplate(metaObject, template, manager);
            }
            
        } catch (Exception e) {
            throw new GeneratorIOException("Failed to generate files for object: " + metaObject.getName(), e);
        }
    }
    
    private void generateFileFromTemplate(MetaObject metaObject, TemplateDefinition template, GeneratorIOManager manager) throws GeneratorIOException {
        try {
            // Validate template requirements
            templateParser.validateTemplate(template);
            
            // Generate code using Mustache engine
            String generatedCode = templateEngine.generateCode(template, metaObject);
            
            // Determine output file name and path
            String fileName = metaObject.getName() + "." + template.getOutputFileExtension();
            String packagePath = template.isPackagePath() ? getPackagePath(metaObject) : "";
            
            // Write generated file
            manager.writeFile(packagePath, fileName, generatedCode);
            
        } catch (Exception e) {
            throw new GeneratorIOException("Failed to generate file from template: " + template.getName(), e);
        }
    }
    
    private List<TemplateDefinition> loadTemplatesForObject(MetaObject metaObject) throws IOException {
        // Implementation to load applicable templates based on MetaObject attributes
        // This could scan the template directory, or use configured template lists
        // For now, return a simple implementation
        
        String templateFile = templatePath + "entity.mustache.yaml";
        TemplateDefinition template = templateParser.parseTemplateFromFile(templateFile);
        
        return List.of(template);
    }
    
    private String getPackagePath(MetaObject metaObject) {
        String packageName = metaObject.hasAttr("package") ? 
            metaObject.getAttrValueAsString("package") : "com.example.generated";
        return packageName.replace(".", "/");
    }
}
```

## Phase 2: Testing and Validation

### Step 7: Create Comprehensive Tests

**Create `codegen/src/test/java/com/draagon/meta/generator/mustache/MustacheTemplateEngineTest.java`:**
```java
package com.draagon.meta.generator.mustache;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.simple.SimpleLoader;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MustacheTemplateEngineTest {
    
    private MustacheTemplateEngine engine;
    private MetaObject testMetaObject;
    
    @Before
    public void setUp() throws Exception {
        engine = new MustacheTemplateEngine();
        
        // Load test metadata
        SimpleLoader loader = new SimpleLoader();
        loader.loadFromResource("mustache-test-metadata.json");
        testMetaObject = loader.getMetaObject("com.example.model::User");
    }
    
    @Test
    public void testBasicTemplateGeneration() {
        TemplateDefinition template = createBasicEntityTemplate();
        
        String result = engine.generateCode(template, testMetaObject);
        
        assertNotNull(result);
        assertTrue(result.contains("public class User"));
        assertTrue(result.contains("private String name"));
        assertTrue(result.contains("private Integer age"));
    }
    
    @Test
    public void testJpaEntityTemplate() {
        TemplateDefinition template = createJpaEntityTemplate();
        
        String result = engine.generateCode(template, testMetaObject);
        
        assertNotNull(result);
        assertTrue(result.contains("@Entity"));
        assertTrue(result.contains("@Table(name = \"users\")"));
        assertTrue(result.contains("@Column(name = \"user_name\")"));
        assertTrue(result.contains("@Id"));
    }
    
    private TemplateDefinition createBasicEntityTemplate() {
        TemplateDefinition template = new TemplateDefinition();
        template.setName("Basic Entity");
        template.setTargetLanguage("java");
        template.setOutputFileExtension("java");
        template.setTemplate(
            "package {{packageName}};\n\n" +
            "public class {{className}} {\n" +
            "{{#fields}}" +
            "    private {{javaType}} {{name}};\n" +
            "{{/fields}}" +
            "}"
        );
        return template;
    }
    
    private TemplateDefinition createJpaEntityTemplate() {
        TemplateDefinition template = new TemplateDefinition();
        template.setName("JPA Entity");
        template.setTargetLanguage("java");
        template.setOutputFileExtension("java");
        template.setTemplate(
            "package {{packageName}};\n\n" +
            "import javax.persistence.*;\n\n" +
            "@Entity\n" +
            "@Table(name = \"{{dbTableName}}\")\n" +
            "public class {{className}} {\n" +
            "{{#fields}}" +
            "{{#isIdField}}    @Id\n{{/isIdField}}" +
            "    @Column(name = \"{{dbColumnName}}\")\n" +
            "    private {{javaType}} {{name}};\n" +
            "{{/fields}}" +
            "}"
        );
        return template;
    }
}
```

### Step 8: Create Test Metadata

**Create `codegen/src/test/resources/mustache-test-metadata.json`:**
```json
{
  "metadata": [
    {
      "type": "object",
      "name": "User",
      "package": "com.example.model",
      "attributes": {
        "dbTable": "users"
      },
      "fields": [
        {
          "name": "id",
          "dataType": "long",
          "attributes": {
            "isId": "true",
            "dbColumn": "user_id"
          }
        },
        {
          "name": "name",
          "dataType": "string",
          "attributes": {
            "required": "true",
            "dbColumn": "user_name"
          }
        },
        {
          "name": "age",
          "dataType": "int",
          "attributes": {
            "dbColumn": "user_age"
          }
        },
        {
          "name": "email",
          "dataType": "string",
          "attributes": {
            "required": "true",
            "dbColumn": "email_address"
          }
        }
      ]
    }
  ]
}
```

## Phase 3: Template Creation and Migration

### Step 9: Create Template Examples

**Create example templates in `codegen/src/test/resources/templates/`:**

**`entity.mustache.yaml`:**
```yaml
name: "Basic Entity Template"
version: "1.0.0"
description: "Generate basic POJO entities"
targetLanguage: "java"
outputFileExtension: "java"
packagePath: true
requirements:
  helpers: ["capitalize", "javaType"]
template: |
  package {{packageName}};
  
  {{#imports}}
  import {{.}};
  {{/imports}}
  
  /**
   * {{description}}
   * Generated entity class for {{className}}
   */
  public class {{className}} {
      
      {{#fields}}
      private {{javaType}} {{name}};
      {{/fields}}
      
      // Default constructor
      public {{className}}() {}
      
      {{#fields}}
      // Getter for {{name}}
      public {{javaType}} {{#capitalize}}get{{name}}{{/capitalize}}() {
          return this.{{name}};
      }
      
      // Setter for {{name}}
      public void {{#capitalize}}set{{name}}{{/capitalize}}({{javaType}} {{name}}) {
          this.{{name}} = {{name}};
      }
      
      {{/fields}}
  }
```

### Step 10: Integration Testing

Run comprehensive tests to ensure the new Mustache system works correctly:

```bash
# Build and test the codegen module
cd codegen
mvn clean test

# Run specific Mustache tests
mvn test -Dtest=MustacheTemplateEngineTest

# Test with actual MetaObjects metadata
mvn test -Dtest=MustacheTemplateGeneratorIntegrationTest
```

## Phase 4: Production Deployment

### Step 11: Maven Plugin Configuration

**Update `maven-plugin/src/main/java/com/draagon/meta/web/maven/MetaObjectsGeneratorMojo.java` to support Mustache templates:**

```java
@Parameter(property = "templateEngine", defaultValue = "mustache")
private String templateEngine;

@Parameter(property = "templatePath", defaultValue = "templates/")
private String templatePath;

// Add logic to use MustacheTemplateGenerator when templateEngine="mustache"
```

### Step 12: Documentation and Examples

Create comprehensive documentation and working examples for developers to adopt the new template system.

## Next Steps: Cross-Language Implementation

Once the Java foundation is solid, proceed to implement the C# and TypeScript versions following the same architectural patterns but using language-specific Mustache implementations (Stubble for C#, Mustache.js for TypeScript).

This implementation provides a robust foundation for the Mustache-based template system while maintaining full compatibility with existing MetaObjects architecture and build processes.