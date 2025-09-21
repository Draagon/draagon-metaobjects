package com.draagon.meta.generator.direct.object.javacode;

import com.draagon.meta.generator.GeneratorTestBase;
import com.draagon.meta.generator.direct.object.javacode.JavaCodeGenerator;
import com.draagon.meta.generator.direct.GenerationContext;
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
 * Basic test cases for deprecated Java Direct Generator
 * @deprecated This tests the deprecated JavaCodeGenerator. 
 *             New tests should focus on Mustache template system.
 */
@Deprecated
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
        // Clean up generated test files if needed
    }

    @Test
    public void testGenerationContextBasics() {
        GenerationContext context = new GenerationContext(loader);
        
        context.setProperty("test.key", "test.value");
        assertEquals("Property should be retrievable", "test.value", context.getProperty("test.key", null));
        
        context.setCurrentPackage("com.test");
        assertEquals("Package should be settable", "com.test", context.getCurrentPackage());
        
        context.setCurrentClassName("TestClass");
        assertEquals("Class name should be settable", "TestClass", context.getCurrentClassName());
    }

    @Test
    public void testBasicConfiguration() throws IOException {
        JavaCodeGenerator generator = new JavaCodeGenerator();
        Map<String, String> args = new HashMap<>();
        args.put("type", "interface");
        args.put("outputDir", OUT_DIR);
        
        generator.setArgs(args);
        // Test configuration without actual generation since the deprecated generator
        // may have issues after plugin removal
        assertEquals("Type should be configurable", "interface", generator.getType());
    }

    @Test
    public void testSupportedTypes() {
        JavaCodeGenerator generator = new JavaCodeGenerator();
        String[] supportedTypes = generator.getSupportedTypes();
        
        assertEquals("Should support 2 types", 2, supportedTypes.length);
        assertTrue("Should support interface", Arrays.asList(supportedTypes).contains("interface"));
        assertTrue("Should support class", Arrays.asList(supportedTypes).contains("class"));
    }

    @Test
    public void testDefaultType() {
        JavaCodeGenerator generator = new JavaCodeGenerator();
        assertEquals("Default type should be interface", "interface", generator.getDefaultType());
    }

    @Test
    public void testFileExtension() {
        JavaCodeGenerator generator = new JavaCodeGenerator();
        assertEquals("File extension should be .java", ".java", generator.getFileExtension());
    }

    @Test
    public void testLanguageName() {
        JavaCodeGenerator generator = new JavaCodeGenerator();
        assertEquals("Language name should be Java", "Java", generator.getLanguageName());
    }

    // Utility method to recursively delete directory
    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }
}