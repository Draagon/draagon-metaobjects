package com.draagon.meta.generator.mustache;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.generator.GeneratorTestBase;
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.loader.uri.URIHelper;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;

/**
 * Test class for MustacheTemplateEngine.
 * Validates the core functionality of the Mustache-based code generation system.
 */
public class MustacheTemplateEngineTest extends GeneratorTestBase {
    
    private MustacheTemplateEngine engine;
    private MetaObject testMetaObject;
    private SimpleLoader loader;
    
    @Before
    public void setUp() throws Exception {
        engine = new MustacheTemplateEngine();
        
        // Load test metadata using the proper pattern
        loader = initLoader(Arrays.asList(
            URIHelper.toURI("model:resource:mustache-test-metadata.json")
        ));
        
        // Get the User MetaObject
        testMetaObject = loader.getChild("com.example.model::User", MetaObject.class);
        
        assertNotNull("Test MetaObject should be loaded", testMetaObject);
    }
    
    @Test
    public void testBasicTemplateGeneration() {
        TemplateDefinition template = createBasicEntityTemplate();
        
        String result = engine.generateCode(template, testMetaObject);
        
        assertNotNull("Generated code should not be null", result);
        assertTrue("Should contain class declaration", result.contains("public class User"));
        assertTrue("Should contain String field", result.contains("private String username"));
        assertTrue("Should contain Integer field", result.contains("private Integer age"));
        assertTrue("Should contain getter", result.contains("getUsername()"));
        assertTrue("Should contain setter", result.contains("setUsername("));
    }
    
    @Test
    public void testJpaEntityTemplate() {
        TemplateDefinition template = createJpaEntityTemplate();
        
        String result = engine.generateCode(template, testMetaObject);
        
        assertNotNull("Generated code should not be null", result);
        assertTrue("Should contain @Entity annotation", result.contains("@Entity"));
        assertTrue("Should contain @Table annotation", result.contains("@Table(name = \"users\")"));
        assertTrue("Should contain @Column annotation", result.contains("@Column(name = \"username\""));
        assertTrue("Should contain @Id annotation", result.contains("@Id"));
        assertTrue("Should implement Serializable", result.contains("implements Serializable"));
        assertTrue("Should have newInstance() method", result.contains("newInstance()"));
    }
    
    @Test
    public void testValueObjectExtensionTemplate() {
        TemplateDefinition template = createValueObjectExtensionTemplate();
        
        String result = engine.generateCode(template, testMetaObject);
        
        assertNotNull("Generated code should not be null", result);
        assertTrue("Should extend ValueObject", result.contains("extends ValueObject"));
        assertTrue("Should have META_OBJECT_NAME constant", result.contains("META_OBJECT_NAME"));
        assertTrue("Should have dynamic getters", result.contains("getAttrValue(\"username\")"));
        assertTrue("Should have dynamic setters", result.contains("setAttrValue(\"username\""));
        assertTrue("Should have fluent methods", result.contains("username(String username)"));
        assertTrue("Should have has methods", result.contains("hasUsername()"));
        assertTrue("Should have copy() method", result.contains("copy()"));
    }
    
    @Test
    public void testHelperFunctions() {
        HelperRegistry helperRegistry = engine.getHelperRegistry();
        
        // Test string helpers
        assertEquals("Username", helperRegistry.get("capitalize").apply("username"));
        assertEquals("userName", helperRegistry.get("camelCase").apply("user_name"));
        assertEquals("UserName", helperRegistry.get("pascalCase").apply("user_name"));
        
        // Test Java type helpers with MetaField
        MetaField stringField = (MetaField) testMetaObject.getChild("username", MetaField.class);
        assertNotNull("Username field should exist", stringField);
        assertEquals("String", helperRegistry.get("javaType").apply(stringField));
        
        MetaField intField = (MetaField) testMetaObject.getChild("age", MetaField.class);
        assertNotNull("Age field should exist", intField);
        assertEquals("Integer", helperRegistry.get("javaType").apply(intField));
        
        // Test database helpers
        assertEquals("username", helperRegistry.get("dbColumnName").apply(stringField));
        assertEquals("users", helperRegistry.get("dbTableName").apply(testMetaObject));
        
        // Test ID field detection
        MetaField idField = (MetaField) testMetaObject.getChild("id", MetaField.class);
        assertNotNull("ID field should exist", idField);
        assertTrue((Boolean) helperRegistry.get("isIdField").apply(idField));
        assertFalse((Boolean) helperRegistry.get("isIdField").apply(stringField));
    }
    
    @Test
    public void testTemplateContextCreation() {
        TemplateDefinition template = createBasicEntityTemplate();
        
        // This test verifies that the template context is created correctly
        // by checking that code generation succeeds and produces expected output
        String result = engine.generateCode(template, testMetaObject);
        
        // Verify package name is included
        assertTrue("Should include package declaration", result.contains("package com.example.model"));
        
        // Verify class name is correct
        assertTrue("Should have correct class name", result.contains("class User"));
        
        // Verify all fields are included
        assertTrue("Should include id field", result.contains("private Long id"));
        assertTrue("Should include username field", result.contains("private String username"));
        assertTrue("Should include email field", result.contains("private String email"));
        assertTrue("Should include age field", result.contains("private Integer age"));
        assertTrue("Should include createdAt field", result.contains("private java.time.LocalDate createdAt"));
    }
    
    @Test
    public void testTemplateWithPartials() {
        // Test that templates can include partials (if implemented)
        TemplateDefinition template = createBasicEntityTemplate();
        
        String result = engine.generateCode(template, testMetaObject);
        
        // For now, just verify basic functionality works
        assertNotNull("Generated code should not be null", result);
        assertTrue("Should contain class declaration", result.contains("public class User"));
    }
    
    @Test
    public void testErrorHandling() {
        // Test with invalid template
        TemplateDefinition invalidTemplate = new TemplateDefinition();
        invalidTemplate.setName("Invalid Template");
        invalidTemplate.setTemplate("{{#unclosed"); // Invalid Mustache syntax
        
        try {
            engine.generateCode(invalidTemplate, testMetaObject);
            fail("Should throw exception for invalid template");
        } catch (RuntimeException e) {
            assertTrue("Should indicate template generation failure", 
                e.getMessage().contains("Failed to generate code"));
        }
    }
    
    @Test
    public void testCacheClearing() {
        TemplateDefinition template = createBasicEntityTemplate();
        
        // Generate code twice to test caching
        String result1 = engine.generateCode(template, testMetaObject);
        String result2 = engine.generateCode(template, testMetaObject);
        
        assertEquals("Cached results should be identical", result1, result2);
        
        // Clear cache and generate again
        engine.clearCache();
        String result3 = engine.generateCode(template, testMetaObject);
        
        assertEquals("Results after cache clear should be identical", result1, result3);
    }
    
    // Helper methods to create test templates
    
    private TemplateDefinition createBasicEntityTemplate() {
        TemplateDefinition template = new TemplateDefinition();
        template.setName("Basic Entity");
        template.setTargetLanguage("java");
        template.setOutputFileExtension("java");
        template.setTemplate(
            "package {{packageName}};\n\n" +
            "{{#imports}}\n" +
            "import {{.}};\n" +
            "{{/imports}}\n\n" +
            "/**\n" +
            " * Generated entity class for {{className}}\n" +
            " */\n" +
            "public class {{className}} {\n" +
            "{{#fields}}" +
            "    private {{javaType}} {{name}};\n" +
            "{{/fields}}" +
            "\n" +
            "    // Default constructor\n" +
            "    public {{className}}() {}\n" +
            "\n" +
            "{{#fields}}" +
            "    public {{javaType}} {{getterName}}() {\n" +
            "        return this.{{name}};\n" +
            "    }\n" +
            "\n" +
            "    public void {{setterName}}({{javaType}} {{name}}) {\n" +
            "        this.{{name}} = {{name}};\n" +
            "    }\n" +
            "\n" +
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
            "import javax.persistence.*;\n" +
            "import java.io.Serializable;\n" +
            "{{#imports}}\n" +
            "import {{.}};\n" +
            "{{/imports}}\n\n" +
            "@Entity\n" +
            "@Table(name = \"{{dbTableName}}\")\n" +
            "public class {{className}} implements Serializable {\n" +
            "\n" +
            "    private static final long serialVersionUID = 1L;\n" +
            "\n" +
            "{{#fields}}" +
            "{{#isIdField}}    @Id\n" +
            "    @GeneratedValue(strategy = GenerationType.IDENTITY)\n{{/isIdField}}" +
            "    @Column(name = \"{{dbColumnName}}\")\n" +
            "    private {{javaType}} {{name}};\n" +
            "\n" +
            "{{/fields}}" +
            "    public static {{className}} newInstance() {\n" +
            "        return new {{className}}();\n" +
            "    }\n" +
            "}"
        );
        return template;
    }
    
    private TemplateDefinition createValueObjectExtensionTemplate() {
        TemplateDefinition template = new TemplateDefinition();
        template.setName("ValueObject Extension");
        template.setTargetLanguage("java");
        template.setOutputFileExtension("java");
        template.setTemplate(
            "package {{packageName}};\n\n" +
            "import com.draagon.meta.object.value.ValueObject;\n" +
            "import com.draagon.meta.object.MetaObject;\n\n" +
            "public class {{className}} extends ValueObject {\n" +
            "\n" +
            "    private static final String META_OBJECT_NAME = \"{{fullName}}\";\n" +
            "\n" +
            "{{#fields}}" +
            "    public {{javaType}} {{getterName}}() {\n" +
            "        return ({{javaType}}) getAttrValue(\"{{name}}\");\n" +
            "    }\n" +
            "\n" +
            "    public void {{setterName}}({{javaType}} {{name}}) {\n" +
            "        setAttrValue(\"{{name}}\", {{name}});\n" +
            "    }\n" +
            "\n" +
            "    public boolean {{hasMethodName}}() {\n" +
            "        return hasAttr(\"{{name}}\");\n" +
            "    }\n" +
            "\n" +
            "    public {{className}} {{name}}({{javaType}} {{name}}) {\n" +
            "        {{setterName}}({{name}});\n" +
            "        return this;\n" +
            "    }\n" +
            "\n" +
            "{{/fields}}" +
            "    public {{className}} copy() {\n" +
            "        {{className}} copy = new {{className}}(getMetaObject());\n" +
            "{{#fields}}" +
            "        if ({{hasMethodName}}()) {\n" +
            "            copy.{{setterName}}({{getterName}}());\n" +
            "        }\n" +
            "{{/fields}}" +
            "        return copy;\n" +
            "    }\n" +
            "}"
        );
        return template;
    }
}