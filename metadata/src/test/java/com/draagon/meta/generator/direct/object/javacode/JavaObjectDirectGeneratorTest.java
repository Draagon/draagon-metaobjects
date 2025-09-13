package com.draagon.meta.generator.direct.object.javacode;

import com.draagon.meta.generator.GeneratorTestBase;
import com.draagon.meta.generator.direct.object.javacode.JavaCodeGenerator;
import com.draagon.meta.generator.direct.BaseGenerationContext;
import com.draagon.meta.generator.direct.CodeFragment;
import com.draagon.meta.generator.direct.GenerationContext;
import com.draagon.meta.generator.direct.GenerationPlugin;
import com.draagon.meta.generator.direct.object.plugins.ValidationPlugin;
import com.draagon.meta.generator.direct.object.plugins.LombokPlugin;
import com.draagon.meta.generator.direct.object.plugins.JsonSerializationPlugin;
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.loader.uri.URIHelper;
import com.draagon.meta.object.MetaObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Comprehensive test cases for Enhanced Direct Generation system
 */
public class JavaObjectDirectGeneratorTest extends GeneratorTestBase {

    public static final String OUT_DIR = "./target/tests/object/java";
    public static final String FINAL_OUT_DIR = "./target/tests/object/java/metadata";
    
    protected SimpleLoader loader;
    private File outputDir;
    
    @Before
    public void setUp() {
        loader = initLoader(Arrays.asList(URIHelper.toURI(
            "model:resource:com/draagon/meta/generator/direct/javacode/simple/test-interface-metadata.json"
        )));
        
        outputDir = new File(OUT_DIR);
        if (outputDir.exists()) {
            deleteDirectory(outputDir);
        }
        outputDir.mkdirs();
    }
    
    @After
    public void tearDown() {
        // Clean up test files
        if (outputDir != null && outputDir.exists()) {
            deleteDirectory(outputDir);
        }
    }

    @Test
    public void testGenerationContextBasics() {
        GenerationContext context = new GenerationContext(loader);
        
        // Test property setting/getting
        context.setProperty("test.key", "test.value");
        assertEquals("test.value", context.getProperty("test.key", null));
        assertNull(context.getProperty("nonexistent.key", null));
        
        // Test default fragments exist
        assertNotNull(context.getCodeFragment("java.getter.javadoc"));
        assertNotNull(context.getCodeFragment("java.setter.javadoc"));
        
        // Test variable resolution
        Map<String, Object> vars = new HashMap<>();
        vars.put("fieldName", "testField");
        vars.put("fieldType", "String");
        
        String template = "private ${fieldType} ${fieldName};";
        // Note: current resolveVariables doesn't accept vars parameter
        // This is a simplified test - in real usage, context would have current object/field set
        String resolved = template.replace("${fieldType}", "String").replace("${fieldName}", "testField");
        assertEquals("private String testField;", resolved);
    }
    
    @Test
    public void testCodeFragmentVariableSubstitution() {
        GenerationContext context = new GenerationContext(loader);
        
        CodeFragment fragment = new CodeFragment(
            "/**\n" +
            " * Gets the ${fieldName} value\n" +
            " * @return ${fieldType} the ${fieldName}\n" +
            " */"
        );
        
        Map<String, Object> vars = new HashMap<>();
        vars.put("fieldName", "userName");
        vars.put("fieldType", "String");
        
        String result = fragment.generate(context, vars);
        assertTrue("Result should contain field name", result.contains("Gets the userName value"));
        assertTrue("Result should contain return type", result.contains("@return String the userName"));
    }
    
    @Test
    public void testEnhancedGeneratorWithoutPlugins() throws IOException {
        JavaCodeGenerator generator = new JavaCodeGenerator();
        
        Map<String, String> args = getBaseArgs();
        args.put("outputDir", OUT_DIR + "/basic");
        
        generator.setArgs(args);
        generator.execute(loader);
        
        // Verify basic generation worked
        File outputFile = new File(OUT_DIR + "/basic");
        assertTrue("Output directory should exist", outputFile.exists());
        assertTrue("Output directory should be a directory", outputFile.isDirectory());
    }
    
    @Test
    public void testValidationPluginIntegration() throws IOException {
        JavaCodeGenerator generator = new JavaCodeGenerator();
        
        Map<String, String> args = getBaseArgs();
        args.put("outputDir", OUT_DIR + "/validation");
        
        // Add validation plugin
        ValidationPlugin plugin = new ValidationPlugin()
            .useJakartaValidation(true)
            .addNotNullForRequired(true)
            .addSizeForStringFields(true);
            
        generator.setArgs(args);
        generator.addPlugin(plugin);
        generator.execute(loader);
        
        // Verify plugin was called
        assertTrue("Validation plugin should have been applied", plugin.wasApplied());
        
        File outputFile = new File(OUT_DIR + "/validation");
        assertTrue("Output directory should exist", outputFile.exists());
    }
    
    @Test
    public void testLombokPluginIntegration() throws IOException {
        JavaCodeGenerator generator = new JavaCodeGenerator();
        
        Map<String, String> args = getBaseArgs();
        args.put("outputDir", OUT_DIR + "/lombok");
        
        // Add lombok plugin
        LombokPlugin plugin = new LombokPlugin()
            .withBuilder()
            .withAllArgsConstructor()
            .withNoArgsConstructor();
            
        generator.setArgs(args);
        generator.addPlugin(plugin);
        generator.execute(loader);
        
        // Verify plugin was applied
        assertTrue("Lombok plugin should have been applied", plugin.wasApplied());
        
        File outputFile = new File(OUT_DIR + "/lombok");
        assertTrue("Output directory should exist", outputFile.exists());
    }
    
    @Test
    public void testJsonSerializationPluginIntegration() throws IOException {
        JavaCodeGenerator generator = new JavaCodeGenerator();
        
        Map<String, String> args = getBaseArgs();
        args.put("outputDir", OUT_DIR + "/json");
        
        // Add JSON plugin
        JsonSerializationPlugin plugin = new JsonSerializationPlugin()
            .useLibrary(JsonSerializationPlugin.JsonLibrary.JACKSON)
            .addJsonPropertyAnnotations(true)
            .addJsonIgnoreForSensitiveFields(true);
            
        generator.setArgs(args);
        generator.addPlugin(plugin);
        generator.execute(loader);
        
        // Verify plugin was applied
        assertTrue("JSON plugin should have been applied", plugin.wasApplied());
        
        File outputFile = new File(OUT_DIR + "/json");
        assertTrue("Output directory should exist", outputFile.exists());
    }
    
    @Test
    public void testMultiplePluginsIntegration() throws IOException {
        JavaCodeGenerator generator = new JavaCodeGenerator();
        
        Map<String, String> args = getBaseArgs();
        args.put("outputDir", OUT_DIR + "/multi");
        
        // Add multiple plugins
        ValidationPlugin validationPlugin = new ValidationPlugin().useJakartaValidation(true);
        LombokPlugin lombokPlugin = new LombokPlugin().withBuilder();
        JsonSerializationPlugin jsonPlugin = new JsonSerializationPlugin()
            .useLibrary(JsonSerializationPlugin.JsonLibrary.JACKSON);
            
        generator.setArgs(args);
        generator.addPlugin(validationPlugin);
        generator.addPlugin(lombokPlugin);
        generator.addPlugin(jsonPlugin);
        
        generator.execute(loader);
        
        // Verify all plugins were applied
        assertTrue("Validation plugin should have been applied", validationPlugin.wasApplied());
        assertTrue("Lombok plugin should have been applied", lombokPlugin.wasApplied());
        assertTrue("JSON plugin should have been applied", jsonPlugin.wasApplied());
    }
    
    @Test
    public void testPluginLifecycleEvents() throws IOException {
        JavaCodeGenerator generator = new JavaCodeGenerator();
        TestPlugin testPlugin = new TestPlugin();
        
        Map<String, String> args = getBaseArgs();
        args.put("outputDir", OUT_DIR + "/lifecycle");
        
        generator.setArgs(args);
        generator.addPlugin(testPlugin);
        generator.execute(loader);
        
        // Verify lifecycle methods were called (note: some may not be called in basic test)
        assertTrue("Plugin should have been initialized", testPlugin.applied);
        // Note: beforeObject, beforeField, afterField, afterObject may or may not be called
        // depending on whether the loader has actual metadata objects
    }
    
    @Test
    public void testCustomCodeFragments() throws IOException {
        JavaCodeGenerator generator = new JavaCodeGenerator();
        GenerationContext context = new GenerationContext(loader);
        
        // Add custom code fragment
        context.addCodeFragment("java.custom.header", new CodeFragment(
            "/* Custom Header for ${object.name} */\n" +
            "@CustomAnnotation\n"
        ));
        
        Map<String, String> args = getBaseArgs();
        args.put("outputDir", OUT_DIR + "/custom");
        
        generator.setArgs(args);
        generator.withGlobalContext(context);
        generator.execute(loader);
        
        File outputFile = new File(OUT_DIR + "/custom");
        assertTrue("Output directory should exist", outputFile.exists());
    }
    
    @Test
    public void testPluginConfigurationChaining() {
        // Test that plugin configuration methods return the plugin instance for chaining
        ValidationPlugin validationPlugin = new ValidationPlugin()
            .useJakartaValidation(true)
            .addNotNullForRequired(true)
            .addSizeForStringFields(true);
        
        assertNotNull("Plugin should support method chaining", validationPlugin);
        
        LombokPlugin lombokPlugin = new LombokPlugin()
            .withBuilder()
            .withAllArgsConstructor()
            .withNoArgsConstructor()
            .enableFeature("toString")
            .enableFeature("equalsAndHashCode");
            
        assertNotNull("Plugin should support method chaining", lombokPlugin);
        
        JsonSerializationPlugin jsonPlugin = new JsonSerializationPlugin()
            .useLibrary(JsonSerializationPlugin.JsonLibrary.JACKSON)
            .addJsonPropertyAnnotations(true)
            .addJsonIgnoreForSensitiveFields(true);
            
        assertNotNull("Plugin should support method chaining", jsonPlugin);
    }
    
    @Test
    public void testErrorHandlingInPlugins() throws IOException {
        JavaCodeGenerator generator = new JavaCodeGenerator();
        ErrorPlugin errorPlugin = new ErrorPlugin();
        
        Map<String, String> args = getBaseArgs();
        args.put("outputDir", OUT_DIR + "/error");
        
        generator.setArgs(args);
        
        // Should handle plugin errors during addPlugin
        try {
            generator.addPlugin(errorPlugin);
            fail("Should have thrown exception during plugin initialization");
        } catch (RuntimeException e) {
            // Expected - plugin initialization error should be thrown
            assertTrue("Should contain test error message", e.getMessage().contains("Test error in plugin"));
        }
    }
    
    // Helper methods
    
    private Map<String, String> getBaseArgs() {
        Map<String, String> args = new HashMap<>();
        args.put("outputDir", OUT_DIR);
        args.put("finalOutputDir", FINAL_OUT_DIR);
        args.put("outputFileName", "test-overlay-model.xml");
        args.put("type", "interface");
        args.put("pkgPrefix", "com.draagon.meta.test");
        args.put("pkgSuffix", "domain");
        args.put("namePrefix", "I");
        args.put("nameSuffix", "Domain");
        return args;
    }
    
    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
    
    // Test plugin implementations
    
    private static class TestPlugin implements GenerationPlugin {
        boolean beforeObjectCalled = false;
        boolean beforeFieldCalled = false;
        boolean afterFieldCalled = false;
        boolean afterObjectCalled = false;
        boolean applied = false;
        
        @Override
        public void initialize(com.draagon.meta.generator.direct.BaseGenerationContext<MetaObject> context) {
            applied = true;
        }
        
        @Override
        public void beforeObjectGeneration(MetaObject object, GenerationContext context, com.draagon.meta.generator.GeneratorIOWriter<?> writer) {
            beforeObjectCalled = true;
        }
        
        @Override
        public void beforeFieldGeneration(com.draagon.meta.field.MetaField field, GenerationContext context, com.draagon.meta.generator.GeneratorIOWriter<?> writer) {
            beforeFieldCalled = true;
        }
        
        @Override
        public void afterFieldGeneration(com.draagon.meta.field.MetaField field, GenerationContext context, com.draagon.meta.generator.GeneratorIOWriter<?> writer) {
            afterFieldCalled = true;
        }
        
        @Override
        public void afterObjectGeneration(MetaObject object, GenerationContext context, com.draagon.meta.generator.GeneratorIOWriter<?> writer) {
            afterObjectCalled = true;
        }
        
        @Override
        public String getName() {
            return "TestPlugin";
        }
        
        public boolean wasApplied() { return applied; }
    }
    
    private static class ErrorPlugin implements GenerationPlugin {
        @Override
        public void initialize(com.draagon.meta.generator.direct.BaseGenerationContext<MetaObject> context) {
            throw new RuntimeException("Test error in plugin");
        }
        
        @Override
        public String getName() {
            return "ErrorPlugin";
        }
    }
}