package com.metaobjects;

import com.metaobjects.util.MetaDataPath;
import org.junit.Test;

import java.util.Map;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Tests for enhanced MetaDataException with structured context.
 */
public class MetaDataExceptionTest {

    @Test
    public void testBasicConstructorBackwardCompatibility() {
        MetaDataException exception = new MetaDataException("Simple error message");
        
        assertEquals("Simple error message", exception.getMessage());
        assertFalse(exception.getMetaDataPath().isPresent());
        assertFalse(exception.getOperation().isPresent());
        assertTrue(exception.getContext().isEmpty());
        assertFalse(exception.hasEnhancedContext());
    }

    @Test
    public void testConstructorWithCauseBackwardCompatibility() {
        RuntimeException cause = new RuntimeException("Original cause");
        MetaDataException exception = new MetaDataException("Error with cause", cause);
        
        assertEquals("Error with cause", exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertFalse(exception.hasEnhancedContext());
    }

    @Test
    public void testEnhancedConstructorWithSource() {
        MetaData source = createTestMetaData("field", "string", "email");
        MetaDataException exception = new MetaDataException("Validation failed", source, "validation");
        
        assertTrue(exception.hasEnhancedContext());
        assertTrue(exception.getMetaDataPath().isPresent());
        assertEquals("validation", exception.getOperation().orElse(null));
        
        // Check message contains enhanced details
        String message = exception.getMessage();
        assertTrue(message.contains("Validation failed"));
        assertTrue(message.contains("--- Error Details ---"));
        assertTrue(message.contains("Path: field:email(string)"));
        assertTrue(message.contains("Operation: validation"));
        assertTrue(message.contains("Thread:"));
        assertTrue(message.contains("Timestamp:"));
    }

    @Test
    public void testFullConstructorWithContext() {
        MetaData source = createTestMetaData("field", "int", "age");
        Map<String, Object> context = new HashMap<>();
        context.put("expectedRange", "0-120");
        context.put("actualValue", 150);
        context.put("validationRule", "range");
        
        RuntimeException cause = new RuntimeException("Validation error");
        MetaDataException exception = new MetaDataException(
            "Age validation failed", source, "fieldValidation", cause, context);
        
        assertTrue(exception.hasEnhancedContext());
        assertEquals(cause, exception.getCause());
        
        // Check that automatic context was added
        Map<String, Object> allContext = exception.getContext();
        assertEquals("TestMetaData", allContext.get("sourceClass"));
        assertEquals("field", allContext.get("sourceType"));
        assertEquals("int", allContext.get("sourceSubType"));
        assertEquals("age", allContext.get("sourceName"));
        
        // Check that user context was preserved
        assertEquals("0-120", allContext.get("expectedRange"));
        assertEquals(150, allContext.get("actualValue"));
        assertEquals("range", allContext.get("validationRule"));
        
        // Check specific context access
        assertEquals("0-120", exception.getContextValue("expectedRange").orElse(null));
        assertFalse(exception.getContextValue("nonexistent").isPresent());
    }

    @Test
    public void testTimestampAndThread() {
        long beforeCreation = System.currentTimeMillis();
        String currentThreadName = Thread.currentThread().getName();
        
        MetaData source = createTestMetaData("object", "domain", "User");
        MetaDataException exception = new MetaDataException("Test error", source, "test");
        
        long afterCreation = System.currentTimeMillis();
        
        assertTrue(exception.getTimestamp() >= beforeCreation);
        assertTrue(exception.getTimestamp() <= afterCreation);
        assertEquals(currentThreadName, exception.getThreadName());
    }

    @Test
    public void testMetaDataPathIntegration() {
        MetaData root = createTestMetaData("object", "domain", "User");
        MetaData field = createTestMetaData("field", "string", "email");
        MetaData validator = createTestMetaData("validator", "required", "emailRequired");
        
        // Create hierarchy
        addChild(root, field);
        addChild(field, validator);
        
        MetaDataException exception = new MetaDataException("Validation failed", validator, "validate");
        
        assertTrue(exception.getMetaDataPath().isPresent());
        MetaDataPath path = exception.getMetaDataPath().get();
        assertEquals("object:User(domain) → field:email(string) → validator:emailRequired(required)", 
                     path.toHierarchicalString());
    }

    @Test
    public void testEnhancedMessageOnlyWithContext() {
        MetaData source = createTestMetaData("field", "string", "name");
        
        // Test with only source (no operation)
        MetaDataException exception1 = new MetaDataException("Error", source, null);
        assertTrue(exception1.getMessage().contains("--- Error Details ---"));
        
        // Test with only operation (no source)
        MetaDataException exception2 = new MetaDataException("Error", null, "operation");
        assertTrue(exception2.getMessage().contains("--- Error Details ---"));
        
        // Test with only context (no source or operation)
        Map<String, Object> context = Map.of("key", "value");
        MetaDataException exception3 = new MetaDataException("Error", null, null, null, context);
        assertTrue(exception3.getMessage().contains("--- Error Details ---"));
        
        // Test with no enhanced context at all
        MetaDataException exception4 = new MetaDataException("Error", null, null, null, Map.of());
        assertFalse(exception4.getMessage().contains("--- Error Details ---"));
    }

    @Test
    public void testContextWithNullValues() {
        MetaData source = createTestMetaData("field", "string", "test");
        Map<String, Object> context = new HashMap<>();
        context.put("nullValue", null);
        context.put("validValue", "test");
        
        MetaDataException exception = new MetaDataException("Test", source, "test", null, context);
        
        Map<String, Object> resultContext = exception.getContext();
        assertTrue(resultContext.containsKey("nullValue"));
        assertNull(resultContext.get("nullValue"));
        assertEquals("test", resultContext.get("validValue"));
        
        assertFalse(exception.getContextValue("nullValue").isPresent());
        assertEquals("test", exception.getContextValue("validValue").orElse(null));
    }

    @Test
    public void testContextImmutability() {
        MetaData source = createTestMetaData("field", "string", "test");
        Map<String, Object> originalContext = new HashMap<>();
        originalContext.put("key", "value");
        
        MetaDataException exception = new MetaDataException("Test", source, "test", null, originalContext);
        
        // Modify original context after exception creation
        originalContext.put("newKey", "newValue");
        
        // Exception context should not be affected
        Map<String, Object> exceptionContext = exception.getContext();
        assertFalse(exceptionContext.containsKey("newKey"));
        
        // Try to modify returned context (should fail)
        try {
            exceptionContext.put("shouldFail", "test");
            fail("Context should be immutable");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testToStringContainsEnhancedInfo() {
        MetaData source = createTestMetaData("field", "string", "email");
        Map<String, Object> context = Map.of("rule", "format");
        
        MetaDataException exception = new MetaDataException(
            "Format validation failed", source, "validation", null, context);
        
        String toString = exception.toString();
        assertTrue(toString.contains("MetaDataException"));
        assertTrue(toString.contains("Format validation failed"));
        // The enhanced message details should be included in toString via getMessage()
        assertTrue(toString.contains("Path: field:email(string)"));
    }

    // Helper methods for creating test MetaData objects
    private MetaData createTestMetaData(String type, String subType, String name) {
        return new TestMetaData(type, subType, name);
    }

    private void addChild(MetaData parent, MetaData child) {
        ((TestMetaData) parent).addTestChild(child);
        ((TestMetaData) child).setTestParent(parent);
    }

    // Simple test implementation of MetaData for testing purposes
    private static class TestMetaData extends MetaData {
        private MetaData parent;

        public TestMetaData(String type, String subType, String name) {
            super(type, subType, name);
        }

        @Override
        public MetaData getParent() {
            return parent;
        }

        public void setTestParent(MetaData parent) {
            this.parent = parent;
        }

        public void addTestChild(MetaData child) {
            // For testing purposes, we don't need to maintain the full children collection
        }
    }
}