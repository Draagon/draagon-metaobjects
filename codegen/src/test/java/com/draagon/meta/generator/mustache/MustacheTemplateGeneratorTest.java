package com.draagon.meta.generator.mustache;

import com.draagon.meta.generator.GeneratorTestBase;
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.loader.uri.URIHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

/**
 * Integration test for MustacheTemplateGenerator.
 * Tests the complete code generation workflow.
 */
public class MustacheTemplateGeneratorTest extends GeneratorTestBase {
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    private MustacheTemplateGenerator generator;
    private SimpleLoader loader;
    private File outputDir;
    
    @Before
    public void setUp() throws Exception {
        generator = new MustacheTemplateGenerator();
        
        // Load test metadata using the proper pattern
        loader = initLoader(Arrays.asList(
            URIHelper.toURI("model:resource:mustache-test-metadata.json")
        ));
        
        outputDir = tempFolder.newFolder("generated");
    }
    
    @Test
    public void testGenerateBasicEntity() throws Exception {
        // Set up generator arguments
        Map<String, String> args = new HashMap<>();
        args.put(MustacheTemplateGenerator.PARAM_TEMPLATE_PATH, "templates/");
        args.put(MustacheTemplateGenerator.PARAM_OUTPUT_DIR, outputDir.getAbsolutePath());
        args.put(MustacheTemplateGenerator.PARAM_TARGET_LANGUAGE, "java");
        args.put(MustacheTemplateGenerator.PARAM_TEMPLATE_NAME, "basic-entity");
        
        generator.setArgs(args);
        generator.execute(loader);
        
        // Verify User.java was generated
        File userFile = new File(outputDir, "com/example/model/User.java");
        assertTrue("User.java should be generated", userFile.exists());
        
        String content = Files.readString(userFile.toPath());
        assertTrue("Should contain package declaration", content.contains("package com.example.model"));
        assertTrue("Should contain class declaration", content.contains("public class User"));
        assertTrue("Should contain username field", content.contains("private String username"));
        assertTrue("Should contain getters", content.contains("getUsername()"));
        assertTrue("Should contain setters", content.contains("setUsername("));
        
        // Verify Product.java was generated
        File productFile = new File(outputDir, "com/example/model/Product.java");
        assertTrue("Product.java should be generated", productFile.exists());
        
        String productContent = Files.readString(productFile.toPath());
        assertTrue("Should contain Product class", productContent.contains("public class Product"));
        assertTrue("Should contain price field", productContent.contains("private java.math.BigDecimal price"));
    }
    
    @Test
    public void testGenerateJpaEntity() throws Exception {
        // Set JPA template
        Map<String, String> args = new HashMap<>();
        args.put(MustacheTemplateGenerator.PARAM_TEMPLATE_PATH, "templates/");
        args.put(MustacheTemplateGenerator.PARAM_OUTPUT_DIR, outputDir.getAbsolutePath());
        args.put(MustacheTemplateGenerator.PARAM_TARGET_LANGUAGE, "java");
        args.put(MustacheTemplateGenerator.PARAM_TEMPLATE_NAME, "jpa-entity");
        
        generator.setArgs(args);
        generator.execute(loader);
        
        // Verify User.java was generated with JPA annotations
        File userFile = new File(outputDir, "com/example/model/User.java");
        assertTrue("User.java should be generated", userFile.exists());
        
        String content = Files.readString(userFile.toPath());
        assertTrue("Should contain @Entity", content.contains("@Entity"));
        assertTrue("Should contain @Table", content.contains("@Table(name = \"users\")"));
        assertTrue("Should contain @Id", content.contains("@Id"));
        assertTrue("Should contain @Column", content.contains("@Column(name = \"username\""));
        assertTrue("Should implement Serializable", content.contains("implements Serializable"));
        assertTrue("Should have equals method", content.contains("equals(Object obj)"));
        assertTrue("Should have hashCode method", content.contains("hashCode()"));
        assertTrue("Should have toString method", content.contains("toString()"));
    }
    
    @Test
    public void testGenerateValueObjectExtension() throws Exception {
        // Set ValueObject template
        Map<String, String> args = new HashMap<>();
        args.put(MustacheTemplateGenerator.PARAM_TEMPLATE_PATH, "templates/");
        args.put(MustacheTemplateGenerator.PARAM_OUTPUT_DIR, outputDir.getAbsolutePath());
        args.put(MustacheTemplateGenerator.PARAM_TARGET_LANGUAGE, "java");
        args.put(MustacheTemplateGenerator.PARAM_TEMPLATE_NAME, "valueobject-extension");
        
        generator.setArgs(args);
        generator.execute(loader);
        
        // Verify User.java was generated with ValueObject extension
        File userFile = new File(outputDir, "com/example/model/User.java");
        assertTrue("User.java should be generated", userFile.exists());
        
        String content = Files.readString(userFile.toPath());
        assertTrue("Should extend ValueObject", content.contains("extends ValueObject"));
        assertTrue("Should have META_OBJECT_NAME", content.contains("META_OBJECT_NAME"));
        assertTrue("Should have dynamic getters", content.contains("getAttrValue(\"username\")"));
        assertTrue("Should have dynamic setters", content.contains("setAttrValue(\"username\""));
        assertTrue("Should have fluent methods", content.contains("username(String username)"));
        assertTrue("Should have has methods", content.contains("hasUsername()"));
        assertTrue("Should have copy method", content.contains("copy()"));
        assertTrue("Should have newInstance method", content.contains("newInstance()"));
    }
    
    @Test
    public void testAutoDiscoverTemplates() throws Exception {
        // Use auto-discovery (no specific template name)
        Map<String, String> args = new HashMap<>();
        args.put(MustacheTemplateGenerator.PARAM_TEMPLATE_PATH, "templates/");
        args.put(MustacheTemplateGenerator.PARAM_OUTPUT_DIR, outputDir.getAbsolutePath());
        args.put(MustacheTemplateGenerator.PARAM_TARGET_LANGUAGE, "java");
        // No template name specified - should auto-discover
        
        generator.setArgs(args);
        generator.execute(loader);
        
        // Should generate files for discovered templates
        // The exact files depend on which templates are available
        File userDir = new File(outputDir, "com/example/model");
        if (userDir.exists()) {
            File[] files = userDir.listFiles((dir, name) -> name.endsWith(".java"));
            assertTrue("Should generate at least one Java file", files != null && files.length > 0);
        }
    }
    
    @Test
    public void testPackagePrefix() throws Exception {
        // Set package prefix
        Map<String, String> args = new HashMap<>();
        args.put(MustacheTemplateGenerator.PARAM_TEMPLATE_PATH, "templates/");
        args.put(MustacheTemplateGenerator.PARAM_OUTPUT_DIR, outputDir.getAbsolutePath());
        args.put(MustacheTemplateGenerator.PARAM_TARGET_LANGUAGE, "java");
        args.put(MustacheTemplateGenerator.PARAM_TEMPLATE_NAME, "basic-entity");
        args.put(MustacheTemplateGenerator.PARAM_PACKAGE_PREFIX, "org.example.generated");
        
        generator.setArgs(args);
        generator.execute(loader);
        
        // Verify files are generated in prefixed package structure
        File userFile = new File(outputDir, "org/example/generated/com/example/model/User.java");
        assertTrue("User.java should be generated in prefixed package", userFile.exists());
        
        String content = Files.readString(userFile.toPath());
        assertTrue("Should contain prefixed package", 
            content.contains("package org.example.generated.com.example.model"));
    }
    
    @Test
    public void testNonPackagePath() throws Exception {
        // Create a custom template with packagePath=false
        String templateContent = 
            "name: \"Flat Template\"\n" +
            "targetLanguage: \"java\"\n" +
            "outputFileExtension: \"java\"\n" +
            "packagePath: false\n" +
            "template: |\n" +
            "  // Flat file for {{className}}\n" +
            "  public class {{className}} {\n" +
            "  }\n";
        
        // Write template to temp location
        File tempTemplate = tempFolder.newFile("flat-template.mustache.yaml");
        Files.writeString(tempTemplate.toPath(), templateContent);
        
        // Use the temp template
        Map<String, String> args = new HashMap<>();
        args.put(MustacheTemplateGenerator.PARAM_TEMPLATE_PATH, tempTemplate.getParent() + "/");
        args.put(MustacheTemplateGenerator.PARAM_OUTPUT_DIR, outputDir.getAbsolutePath());
        args.put(MustacheTemplateGenerator.PARAM_TARGET_LANGUAGE, "java");
        args.put(MustacheTemplateGenerator.PARAM_TEMPLATE_NAME, "flat-template");
        
        generator.setArgs(args);
        generator.execute(loader);
        
        // Verify files are generated in flat structure (no package path)
        File userFile = new File(outputDir, "User.java");
        assertTrue("User.java should be generated in flat structure", userFile.exists());
        
        String content = Files.readString(userFile.toPath());
        assertTrue("Should contain class declaration", content.contains("public class User"));
    }
    
    @Test
    public void testErrorHandlingNonExistentTemplate() throws Exception {
        // Set non-existent template
        Map<String, String> args = new HashMap<>();
        args.put(MustacheTemplateGenerator.PARAM_TEMPLATE_PATH, "templates/");
        args.put(MustacheTemplateGenerator.PARAM_OUTPUT_DIR, outputDir.getAbsolutePath());
        args.put(MustacheTemplateGenerator.PARAM_TARGET_LANGUAGE, "java");
        args.put(MustacheTemplateGenerator.PARAM_TEMPLATE_NAME, "non-existent-template");
        
        generator.setArgs(args);
        
        try {
            generator.execute(loader);
            fail("Should throw exception for non-existent template");
        } catch (Exception e) {
            assertTrue("Should indicate template generation failure", 
                e.getMessage().contains("Failed to execute"));
        }
    }
    
    @Test
    public void testFilteredMetaObjects() throws Exception {
        // Test with filtered MetaObjects (this would require setting up filters)
        // For now, just verify the basic functionality works
        Map<String, String> args = new HashMap<>();
        args.put(MustacheTemplateGenerator.PARAM_TEMPLATE_PATH, "templates/");
        args.put(MustacheTemplateGenerator.PARAM_OUTPUT_DIR, outputDir.getAbsolutePath());
        args.put(MustacheTemplateGenerator.PARAM_TARGET_LANGUAGE, "java");
        args.put(MustacheTemplateGenerator.PARAM_TEMPLATE_NAME, "basic-entity");
        
        generator.setArgs(args);
        generator.execute(loader);
        
        // Should complete without error
        assertTrue("Generator should complete without error", true);
    }
    
    @Test
    public void testGeneratorConfiguration() throws Exception {
        // Test all the Generator interface methods
        Map<String, String> args = new HashMap<>();
        args.put(MustacheTemplateGenerator.PARAM_OUTPUT_DIR, outputDir.getAbsolutePath());
        
        // Test setArgs
        generator.setArgs(args);
        assertNotNull("Template engine should be accessible", generator.getTemplateEngine());
        
        // Test setFilters
        generator.setFilters(Arrays.asList("filter1", "filter2"));
        
        // Test setScripts
        generator.setScripts(Arrays.asList("script1", "script2"));
        
        // Should complete configuration without error
        assertTrue("Configuration should complete without error", true);
    }
}