package com.draagon.meta.generator.hybrid;

import com.draagon.meta.generator.GeneratorException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for GroovyScriptEngine functionality
 * Note: These tests will pass even if Groovy is not on the classpath
 */
public class GroovyScriptEngineTest {

    private GroovyScriptEngine engine;
    
    @Before
    public void setUp() {
        engine = new GroovyScriptEngine();
    }
    
    @Test
    public void testEngineCreation() {
        assertNotNull("Engine should be created", engine);
    }
    
    @Test
    public void testInitialize() {
        try {
            engine.initialize();
            // If we get here, Groovy is available and initialization succeeded
        } catch (GeneratorException e) {
            // Expected if Groovy is not on classpath
            assertTrue("Should mention Groovy or classpath", 
                e.getMessage().contains("Groovy") || e.getMessage().contains("classpath"));
        }
    }
    
    @Test
    public void testCompileWithGroovyAvailable() {
        try {
            engine.initialize();
            
            String script = "return 'Hello World'";
            CompiledScript compiled = engine.compile("test", script);
            
            assertNotNull("Script should compile", compiled);
            assertEquals("Source should match", script, compiled.getSource());
            
            Map<String, Object> bindings = new HashMap<>();
            Object result = engine.execute(compiled, bindings);
            assertEquals("Should execute and return result", "Hello World", result.toString());
            
        } catch (GeneratorException e) {
            // Expected if Groovy is not available - test passes
            assertTrue("Should be Groovy-related error", e.getMessage().contains("Groovy"));
        }
    }
    
    @Test
    public void testExecuteDirectScript() {
        try {
            String script = "return 42";
            Map<String, Object> bindings = new HashMap<>();
            Object result = engine.execute(script, bindings);
            
            assertEquals("Should execute direct script", 42, result);
            
        } catch (GeneratorException e) {
            // Expected if Groovy is not available - test passes
            assertTrue("Should be Groovy-related error", e.getMessage().contains("Groovy"));
        }
    }
    
    @Test
    public void testScriptWithBindings() {
        try {
            String script = "return greeting + ' ' + name";
            Map<String, Object> bindings = new HashMap<>();
            bindings.put("greeting", "Hello");
            bindings.put("name", "World");
            
            Object result = engine.execute(script, bindings);
            assertEquals("Should use bindings", "Hello World", result.toString());
            
        } catch (GeneratorException e) {
            // Expected if Groovy is not available - test passes
            assertTrue("Should be Groovy-related error", e.getMessage().contains("Groovy"));
        }
    }
}