package com.metaobjects.util;

import com.metaobjects.MetaData;
import com.metaobjects.util.ErrorFormatter;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for ErrorFormatter utility class.
 */
public class ErrorFormatterTest {

    @Test
    public void testFormatValidationError() {
        MetaData field = createTestMetaData("field", "string", "email");
        
        String result = ErrorFormatter.formatValidationError(field, "format", "invalid-email", "valid email format");
        
        assertNotNull(result);
        assertTrue(result.contains("Validation failed for email"));
        assertTrue(result.contains("Value: invalid-email"));
        assertTrue(result.contains("Expected: valid email format"));
        assertTrue(result.contains("Validation: format"));
        assertTrue(result.contains("Path: field:email(string)"));
    }

    @Test
    public void testFormatValidationErrorWithNullValue() {
        MetaData field = createTestMetaData("field", "string", "name");
        
        String result = ErrorFormatter.formatValidationError(field, "required", null, "non-null value");
        
        assertTrue(result.contains("Value: <null>"));
        assertTrue(result.contains("Validation: required"));
    }

    @Test
    public void testFormatValidationErrorWithLongValue() {
        MetaData field = createTestMetaData("field", "string", "description");
        String longValue = "a".repeat(150); // Very long string
        
        String result = ErrorFormatter.formatValidationError(field, "length", longValue, "max 100 characters");
        
        assertTrue(result.contains("Value: " + "a".repeat(97) + "..."));
    }

    @Test
    public void testFormatTypeErrorWithClasses() {
        MetaData field = createTestMetaData("field", "int", "age");
        
        String result = ErrorFormatter.formatTypeError(field, Integer.class, String.class);
        
        assertTrue(result.contains("Type mismatch at age"));
        assertTrue(result.contains("Expected: Integer"));
        assertTrue(result.contains("Actual: String"));
        assertTrue(result.contains("Path: field:age(int)"));
    }

    @Test
    public void testFormatTypeErrorWithStringTypes() {
        MetaData field = createTestMetaData("field", "date", "birthDate");
        
        String result = ErrorFormatter.formatTypeError(field, "java.util.Date", "java.lang.String");
        
        assertTrue(result.contains("Type mismatch at birthDate"));
        assertTrue(result.contains("Expected: java.util.Date"));
        assertTrue(result.contains("Actual: java.lang.String"));
    }

    @Test
    public void testFormatNotFoundError() {
        MetaData parent = createTestMetaDataWithChildren("object", "domain", "User");
        
        String result = ErrorFormatter.formatNotFoundError("field", "nonexistent", parent);
        
        assertTrue(result.contains("Field 'nonexistent' not found in User"));
        assertTrue(result.contains("Available: email, name")); // From our test children
        assertTrue(result.contains("Path: object:User(domain)"));
    }

    @Test
    public void testFormatNotFoundErrorWithNoAvailableItems() {
        MetaData parent = createTestMetaData("object", "domain", "EmptyObject");
        
        String result = ErrorFormatter.formatNotFoundError("field", "anyField", parent);
        
        assertTrue(result.contains("Available: <none>"));
    }

    @Test
    public void testFormatConfigurationError() {
        MetaData config = createTestMetaData("config", "database", "connectionConfig");
        
        String result = ErrorFormatter.formatConfigurationError(
            config, "timeout", -1, "timeout must be positive");
        
        assertTrue(result.contains("Configuration error in connectionConfig"));
        assertTrue(result.contains("Property: timeout"));
        assertTrue(result.contains("Value: -1"));
        assertTrue(result.contains("Issue: timeout must be positive"));
    }

    @Test
    public void testFormatLoadingErrorWithSource() {
        MetaData loader = createTestMetaData("loader", "file", "userLoader");
        
        String result = ErrorFormatter.formatLoadingError(loader, "parseMetadata", "Invalid XML format");
        
        assertTrue(result.contains("Loading failed during parseMetadata for userLoader"));
        assertTrue(result.contains("Details: Invalid XML format"));
        assertTrue(result.contains("Path: loader:userLoader(file)"));
    }

    @Test
    public void testFormatLoadingErrorWithoutSource() {
        String result = ErrorFormatter.formatLoadingError(null, "initialization", "Missing configuration file");
        
        assertTrue(result.contains("Loading failed during initialization"));
        assertTrue(result.contains("Details: Missing configuration file"));
        assertFalse(result.contains("Path:"));
    }

    @Test
    public void testFormatGenericError() {
        MetaData source = createTestMetaData("field", "string", "email");
        Map<String, Object> context = new HashMap<>();
        context.put("attempt", 3);
        context.put("lastError", "Connection timeout");
        
        String result = ErrorFormatter.formatGenericError(
            source, "validation", "Processing failed", context);
        
        assertTrue(result.contains("Processing failed (during validation)"));
        assertTrue(result.contains("Target: email"));
        assertTrue(result.contains("attempt: 3"));
        assertTrue(result.contains("lastError: Connection timeout"));
    }

    @Test
    public void testFormatGenericErrorWithoutSource() {
        Map<String, Object> context = Collections.singletonMap("reason", "system error");
        
        String result = ErrorFormatter.formatGenericError(null, null, "Operation failed", context);
        
        assertTrue(result.contains("Operation failed"));
        assertTrue(result.contains("reason: system error"));
        assertFalse(result.contains("Target:"));
        assertFalse(result.contains("during"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatValidationErrorWithNullSource() {
        ErrorFormatter.formatValidationError(null, "required", "value", "expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatValidationErrorWithNullValidation() {
        MetaData field = createTestMetaData("field", "string", "name");
        ErrorFormatter.formatValidationError(field, null, "value", "expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatTypeErrorWithNullSource() {
        ErrorFormatter.formatTypeError(null, String.class, Integer.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatNotFoundErrorWithNullItemType() {
        MetaData parent = createTestMetaData("object", "domain", "User");
        ErrorFormatter.formatNotFoundError(null, "field", parent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatGenericErrorWithNullMessage() {
        MetaData source = createTestMetaData("field", "string", "name");
        ErrorFormatter.formatGenericError(source, "operation", null, Collections.emptyMap());
    }

    @Test
    public void testValueFormattingEdgeCases() {
        MetaData field = createTestMetaData("field", "string", "test");
        
        // Test empty string
        String result1 = ErrorFormatter.formatValidationError(field, "required", "", "non-empty");
        assertTrue(result1.contains("Value: <empty>"));
        
        // Test very long string truncation
        String longString = "x".repeat(200);
        String result2 = ErrorFormatter.formatValidationError(field, "length", longString, "shorter");
        assertTrue(result2.contains("Value: " + "x".repeat(97) + "..."));
        
        // Test null value
        String result3 = ErrorFormatter.formatValidationError(field, "required", null, "non-null");
        assertTrue(result3.contains("Value: <null>"));
    }

    // Helper methods for creating test MetaData objects
    private MetaData createTestMetaData(String type, String subType, String name) {
        return new TestMetaData(type, subType, name);
    }

    private MetaData createTestMetaDataWithChildren(String type, String subType, String name) {
        TestMetaData parent = new TestMetaData(type, subType, name);
        parent.addTestChild(new TestMetaData("field", "string", "email"));
        parent.addTestChild(new TestMetaData("field", "string", "name"));
        return parent;
    }

    // Simple test implementation of MetaData for testing purposes
    private static class TestMetaData extends MetaData {
        private final java.util.List<MetaData> children = new java.util.ArrayList<>();

        public TestMetaData(String type, String subType, String name) {
            super(type, subType, name);
        }

        @Override
        public MetaData getParent() {
            return null; // Simplified for testing
        }

        @Override
        public java.util.List<MetaData> getChildren() {
            return children;
        }

        public void addTestChild(MetaData child) {
            children.add(child);
        }
    }
}