package com.draagon.meta.generator.mustache;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

/**
 * Test class for TemplateParser.
 * Validates template parsing, validation, and requirement checking.
 */
public class TemplateParserTest {
    
    private TemplateParser parser;
    
    @Before
    public void setUp() {
        parser = new TemplateParser();
    }
    
    @Test
    public void testParseYamlTemplate() throws IOException {
        String yamlContent = 
            "name: \"Test Template\"\n" +
            "version: \"1.0.0\"\n" +
            "description: \"A test template\"\n" +
            "targetLanguage: \"java\"\n" +
            "outputFileExtension: \"java\"\n" +
            "packagePath: true\n" +
            "requirements:\n" +
            "  attributes: [\"dbTable\"]\n" +
            "  helpers: [\"capitalize\", \"javaType\"]\n" +
            "template: |\n" +
            "  package {{packageName}};\n" +
            "  public class {{className}} {\n" +
            "  }\n";
        
        TemplateDefinition template = parser.parseTemplate(yamlContent, true);
        
        assertNotNull("Template should be parsed", template);
        assertEquals("Test Template", template.getName());
        assertEquals("1.0.0", template.getVersion());
        assertEquals("A test template", template.getDescription());
        assertEquals("java", template.getTargetLanguage());
        assertEquals("java", template.getOutputFileExtension());
        assertTrue("Package path should be true", template.isPackagePath());
        
        assertNotNull("Requirements should be parsed", template.getRequirements());
        List<String> attributes = template.getRequirements().getAttributes();
        assertNotNull("Attributes should be parsed", attributes);
        assertEquals(1, attributes.size());
        assertEquals("dbTable", attributes.get(0));
        
        List<String> helpers = template.getRequirements().getHelpers();
        assertNotNull("Helpers should be parsed", helpers);
        assertEquals(2, helpers.size());
        assertTrue("Should contain capitalize helper", helpers.contains("capitalize"));
        assertTrue("Should contain javaType helper", helpers.contains("javaType"));
        
        assertTrue("Template content should contain class declaration", 
            template.getTemplate().contains("public class {{className}}"));
    }
    
    @Test
    public void testParseJsonTemplate() throws IOException {
        String jsonContent = 
            "{\n" +
            "  \"name\": \"Test Template\",\n" +
            "  \"version\": \"1.0.0\",\n" +
            "  \"targetLanguage\": \"java\",\n" +
            "  \"outputFileExtension\": \"java\",\n" +
            "  \"template\": \"public class {{className}} {}\"\n" +
            "}";
        
        TemplateDefinition template = parser.parseTemplate(jsonContent, false);
        
        assertNotNull("Template should be parsed", template);
        assertEquals("Test Template", template.getName());
        assertEquals("java", template.getTargetLanguage());
        assertEquals("public class {{className}} {}", template.getTemplate());
    }
    
    @Test
    public void testParseTemplateFromFile() throws IOException {
        TemplateDefinition template = parser.parseTemplateFromFile("templates/basic-entity.mustache.yaml");
        
        assertNotNull("Template should be loaded from file", template);
        assertEquals("Basic Entity Template", template.getName());
        assertEquals("java", template.getTargetLanguage());
        assertEquals("java", template.getOutputFileExtension());
        assertTrue("Template should contain class definition", 
            template.getTemplate().contains("public class {{className}}"));
    }
    
    @Test(expected = IOException.class)
    public void testParseNonExistentFile() throws IOException {
        parser.parseTemplateFromFile("templates/non-existent.yaml");
    }
    
    @Test
    public void testValidateValidTemplate() {
        TemplateDefinition template = createValidTemplate();
        
        // Should not throw exception
        parser.validateTemplate(template);
    }
    
    @Test
    public void testValidateTemplateWithMissingName() {
        TemplateDefinition template = createValidTemplate();
        template.setName(null);
        
        try {
            parser.validateTemplate(template);
            fail("Should throw exception for missing name");
        } catch (IllegalArgumentException e) {
            assertTrue("Should indicate name is required", 
                e.getMessage().contains("Template name is required"));
        }
    }
    
    @Test
    public void testValidateTemplateWithEmptyName() {
        TemplateDefinition template = createValidTemplate();
        template.setName("   ");
        
        try {
            parser.validateTemplate(template);
            fail("Should throw exception for empty name");
        } catch (IllegalArgumentException e) {
            assertTrue("Should indicate name is required", 
                e.getMessage().contains("Template name is required"));
        }
    }
    
    @Test
    public void testValidateTemplateWithMissingContent() {
        TemplateDefinition template = createValidTemplate();
        template.setTemplate(null);
        
        try {
            parser.validateTemplate(template);
            fail("Should throw exception for missing template content");
        } catch (IllegalArgumentException e) {
            assertTrue("Should indicate template content is required", 
                e.getMessage().contains("Template content is required"));
        }
    }
    
    @Test
    public void testValidateTemplateWithMissingTargetLanguage() {
        TemplateDefinition template = createValidTemplate();
        template.setTargetLanguage(null);
        
        try {
            parser.validateTemplate(template);
            fail("Should throw exception for missing target language");
        } catch (IllegalArgumentException e) {
            assertTrue("Should indicate target language is required", 
                e.getMessage().contains("Target language is required"));
        }
    }
    
    @Test
    public void testValidateTemplateWithUnsupportedLanguage() {
        TemplateDefinition template = createValidTemplate();
        template.setTargetLanguage("python");
        
        try {
            parser.validateTemplate(template);
            fail("Should throw exception for unsupported language");
        } catch (IllegalArgumentException e) {
            assertTrue("Should indicate unsupported language", 
                e.getMessage().contains("Unsupported target language"));
        }
    }
    
    @Test
    public void testValidateTemplateWithMissingOutputExtension() {
        TemplateDefinition template = createValidTemplate();
        template.setOutputFileExtension(null);
        
        try {
            parser.validateTemplate(template);
            fail("Should throw exception for missing output extension");
        } catch (IllegalArgumentException e) {
            assertTrue("Should indicate output extension is required", 
                e.getMessage().contains("Output file extension is required"));
        }
    }
    
    @Test
    public void testValidateTemplateWithInvalidHelper() {
        TemplateDefinition template = createValidTemplate();
        TemplateDefinition.TemplateRequirements requirements = new TemplateDefinition.TemplateRequirements();
        requirements.setHelpers(List.of("nonExistentHelper"));
        template.setRequirements(requirements);
        
        try {
            parser.validateTemplate(template);
            fail("Should throw exception for invalid helper");
        } catch (IllegalArgumentException e) {
            assertTrue("Should indicate invalid helper", 
                e.getMessage().contains("Required helper not available"));
        }
    }
    
    @Test
    public void testSupportsLanguage() {
        TemplateDefinition template = createValidTemplate();
        template.setTargetLanguage("java");
        
        assertTrue("Should support java", parser.supportsLanguage(template, "java"));
        assertTrue("Should support Java (case insensitive)", parser.supportsLanguage(template, "Java"));
        assertFalse("Should not support csharp", parser.supportsLanguage(template, "csharp"));
    }
    
    @Test
    public void testRequiresAttribute() {
        TemplateDefinition template = createValidTemplate();
        TemplateDefinition.TemplateRequirements requirements = new TemplateDefinition.TemplateRequirements();
        requirements.setAttributes(List.of("dbTable", "package"));
        template.setRequirements(requirements);
        
        assertTrue("Should require dbTable", parser.requiresAttribute(template, "dbTable"));
        assertTrue("Should require package", parser.requiresAttribute(template, "package"));
        assertFalse("Should not require nonExistent", parser.requiresAttribute(template, "nonExistent"));
    }
    
    @Test
    public void testTemplateWithoutRequirements() {
        TemplateDefinition template = createValidTemplate();
        template.setRequirements(null);
        
        // Should not throw exception
        parser.validateTemplate(template);
        
        assertFalse("Should not require any attribute", parser.requiresAttribute(template, "anything"));
    }
    
    @Test
    public void testParseJpaEntityTemplate() throws IOException {
        TemplateDefinition template = parser.parseTemplateFromFile("templates/jpa-entity.mustache.yaml");
        
        assertNotNull("JPA template should be loaded", template);
        assertEquals("JPA Entity Template", template.getName());
        assertEquals("java", template.getTargetLanguage());
        
        // Validate the template
        parser.validateTemplate(template);
        
        // Check requirements
        assertTrue("Should require dbTable attribute", parser.requiresAttribute(template, "dbTable"));
        
        // Check template content
        String templateContent = template.getTemplate();
        assertTrue("Should contain @Entity annotation", templateContent.contains("@Entity"));
        assertTrue("Should contain @Table annotation", templateContent.contains("@Table"));
        assertTrue("Should contain @Column annotation", templateContent.contains("@Column"));
    }
    
    @Test
    public void testParseValueObjectTemplate() throws IOException {
        TemplateDefinition template = parser.parseTemplateFromFile("templates/valueobject-extension.mustache.yaml");
        
        assertNotNull("ValueObject template should be loaded", template);
        assertEquals("ValueObject Extension Template", template.getName());
        assertEquals("java", template.getTargetLanguage());
        
        // Validate the template
        parser.validateTemplate(template);
        
        // Check template content
        String templateContent = template.getTemplate();
        assertTrue("Should extend ValueObject", templateContent.contains("extends ValueObject"));
        assertTrue("Should have META_OBJECT_NAME", templateContent.contains("META_OBJECT_NAME"));
        assertTrue("Should have dynamic getters", templateContent.contains("getAttrValue"));
    }
    
    private TemplateDefinition createValidTemplate() {
        TemplateDefinition template = new TemplateDefinition();
        template.setName("Valid Template");
        template.setTargetLanguage("java");
        template.setOutputFileExtension("java");
        template.setTemplate("public class {{className}} {}");
        return template;
    }
}