package com.draagon.meta.generator.hybrid;

import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.loader.uri.URIHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for ScriptContext functionality
 */
public class ScriptContextTest {

    private ScriptContext context;
    private SimpleLoader loader;
    
    @Before
    public void setUp() {
        loader = new SimpleLoader("test-loader")
                .setSourceURIs(Arrays.asList(URIHelper.toURI(
                    "model:resource:com/draagon/meta/generator/direct/javacode/simple/test-interface-metadata.xml"
                )))
                .init();
        context = new ScriptContext(loader);
    }
    
    @Test
    public void testContextCreation() {
        assertNotNull("Context should be created", context);
        assertNotNull("Should have loader", context.getLoader());
    }
    
    @Test
    public void testScriptVariables() {
        // Test script variable management
        context.setScriptVariable("name", "TestValue");
        assertTrue("Should have script variable", context.hasScriptVariable("name"));
        assertEquals("Should get correct value", "TestValue", context.getScriptVariable("name"));
        
        // Test with default value
        assertEquals("Should return default", "default", 
            context.getScriptVariable("nonexistent", "default"));
    }
    
    @Test
    public void testBindings() {
        context.setBinding("key1", "value1");
        context.setBinding("key2", 42);
        
        Object value1 = context.getBinding("key1");
        Object value2 = context.getBinding("key2");
        
        assertEquals("Should get string binding", "value1", value1);
        assertEquals("Should get integer binding", 42, value2);
        
        Map<String, Object> allBindings = context.getAllBindings();
        assertNotNull("Should get all bindings", allBindings);
        assertTrue("Should contain key1", allBindings.containsKey("key1"));
        assertTrue("Should contain key2", allBindings.containsKey("key2"));
    }
    
    @Test
    public void testUtilityMethods() {
        // Test utility methods that ScriptContext should provide
        assertNotNull("Should have toCamelCase", context.toCamelCase("test-name", true));
        assertEquals("Should convert to camelCase", "TestName", context.toCamelCase("test-name", true));
        assertEquals("Should convert to camelCase", "testName", context.toCamelCase("test-name", false));
    }
    
    @Test
    public void testInheritedProperties() {
        // Test that ScriptContext inherits GenerationContext functionality
        context.setProperty("test.prop", "test.value");
        assertEquals("Should inherit property methods", "test.value", context.getProperty("test.prop", null));
    }
}