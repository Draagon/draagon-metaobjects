package com.draagon.meta.util;

import com.draagon.meta.*;
import com.draagon.meta.util.ErrorFormatter;
import com.draagon.meta.util.MetaDataPath;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Integration tests demonstrating the enhanced error reporting system in action.
 * Shows how all components work together to provide rich, contextual error information.
 */
public class ErrorReportingIntegrationTest {

    @Test
    public void testCompleteErrorReportingWorkflow() {
        // Setup: Create a realistic metadata hierarchy
        TestMetaData user = new TestMetaData("object", "domain", "User");
        TestMetaData emailField = new TestMetaData("field", "string", "email");
        TestMetaData emailValidator = new TestMetaData("validator", "format", "emailFormat");
        
        // Build hierarchy: User -> email -> emailFormat
        user.addTestChild(emailField);
        emailField.setTestParent(user);
        emailField.addTestChild(emailValidator);
        emailValidator.setTestParent(emailField);
        
        // Simulate a validation error
        String invalidEmail = "not-an-email";
        
        try {
            // This simulates what would happen in real validation
            throw new MetaDataException(
                ErrorFormatter.formatValidationError(
                    emailValidator, 
                    "email format", 
                    invalidEmail, 
                    "valid email address (user@domain.com)"
                ),
                emailValidator,
                "fieldValidation",
                null,
                Map.of(
                    "validationPhase", "field-level",
                    "inputSource", "userForm",
                    "attemptNumber", 2
                )
            );
        } catch (MetaDataException e) {
            // Verify the rich error information
            assertTrue(e.hasEnhancedContext());
            
            // Check path information
            assertTrue(e.getMetaDataPath().isPresent());
            MetaDataPath path = e.getMetaDataPath().get();
            assertEquals("object:User(domain) → field:email(string) → validator:emailFormat(format)", 
                         path.toHierarchicalString());
            
            // Check operation
            assertEquals("fieldValidation", e.getOperation().orElse(null));
            
            // Check context
            Map<String, Object> context = e.getContext();
            assertEquals("field-level", context.get("validationPhase"));
            assertEquals("userForm", context.get("inputSource"));
            assertEquals(2, context.get("attemptNumber"));
            
            // Check automatic context
            assertEquals("TestMetaData", context.get("sourceClass"));
            assertEquals("validator", context.get("sourceType"));
            assertEquals("format", context.get("sourceSubType"));
            assertEquals("emailFormat", context.get("sourceName"));
            
            // Check message contains all important information
            String message = e.getMessage();
            assertTrue(message.contains("Validation failed for emailFormat"));
            assertTrue(message.contains("Value: not-an-email"));
            assertTrue(message.contains("Expected: valid email address"));
            assertTrue(message.contains("Path: object:User(domain) → field:email(string) → validator:emailFormat(format)"));
            assertTrue(message.contains("Operation: fieldValidation"));
            assertTrue(message.contains("validationPhase: field-level"));
        }
    }

    @Test
    public void testEnhancedFieldNotFoundError() {
        TestMetaData user = new TestMetaData("object", "domain", "User");
        user.addTestChild(new TestMetaData("field", "string", "name"));
        user.addTestChild(new TestMetaData("field", "string", "email"));
        user.addTestChild(new TestMetaData("field", "int", "age"));
        
        try {
            // Simulate trying to find a non-existent field
            throw MetaDataNotFoundException.forField("invalidField", user);
        } catch (MetaDataNotFoundException e) {
            String message = e.getMessage();
            
            // Check that error provides helpful information
            assertTrue(message.contains("Field 'invalidField' not found in User"));
            assertTrue(message.contains("Available: age, email, name")); // Should be sorted
            assertTrue(message.contains("Path: object:User(domain)"));
            
            // Check context
            Map<String, Object> context = e.getContext();
            assertEquals("field", context.get("itemType"));
            assertEquals("invalidField", context.get("itemName"));
            assertEquals("lookup", e.getOperation().orElse(null));
        }
    }

    @Test
    public void testEnhancedValidationErrorWithMultipleFactories() {
        TestMetaData ageField = new TestMetaData("field", "int", "age");
        
        // Test different validation error types
        InvalidValueException requiredError = InvalidValueException.forRequired(ageField);
        assertTrue(requiredError.getMessage().contains("Value: <null/empty>"));
        assertTrue(requiredError.getMessage().contains("Expected: non-null value"));
        
        InvalidValueException rangeError = InvalidValueException.forRange(ageField, 150, 0, 120);
        assertTrue(rangeError.getMessage().contains("Value: 150"));
        assertTrue(rangeError.getMessage().contains("value between 0 and 120"));
        
        InvalidValueException formatError = InvalidValueException.forFormat(ageField, "abc", "integer number");
        assertTrue(formatError.getMessage().contains("Value: abc"));
        assertTrue(formatError.getMessage().contains("Expected: integer number"));
        
        InvalidValueException typeError = InvalidValueException.forTypeMismatch(ageField, "Integer", "String");
        assertTrue(typeError.getMessage().contains("Expected: Integer"));
        assertTrue(typeError.getMessage().contains("Actual: String"));
    }

    @Test
    public void testErrorReportingWithDifferentFormats() {
        TestMetaData config = new TestMetaData("config", "database", "dbConfig");
        
        // Test different error formats
        String validationError = ErrorFormatter.formatValidationError(
            config, "connection", "invalid_url", "valid database URL");
        assertTrue(validationError.contains("Validation failed for dbConfig"));
        
        String configError = ErrorFormatter.formatConfigurationError(
            config, "maxConnections", "abc", "must be a positive integer");
        assertTrue(configError.contains("Configuration error in dbConfig"));
        
        String loadingError = ErrorFormatter.formatLoadingError(
            config, "initialization", "Database driver not found");
        assertTrue(loadingError.contains("Loading failed during initialization for dbConfig"));
        
        String genericError = ErrorFormatter.formatGenericError(
            config, "startup", "Service unavailable", Map.of("retryCount", 3));
        assertTrue(genericError.contains("Service unavailable (during startup)"));
        assertTrue(genericError.contains("retryCount: 3"));
    }

    @Test
    public void testBackwardCompatibilityMaintained() {
        // Test that old-style exception creation still works
        MetaDataException oldStyle1 = new MetaDataException("Simple error");
        assertFalse(oldStyle1.hasEnhancedContext());
        assertEquals("Simple error", oldStyle1.getMessage());
        
        RuntimeException cause = new RuntimeException("Original cause");
        MetaDataException oldStyle2 = new MetaDataException("Error with cause", cause);
        assertEquals(cause, oldStyle2.getCause());
        assertFalse(oldStyle2.hasEnhancedContext());
        
        MetaDataNotFoundException oldNotFound = new MetaDataNotFoundException("Not found: field", "field");
        assertEquals("field", oldNotFound.getName());
        assertFalse(oldNotFound.hasEnhancedContext());
        
        MetaDataNotFoundException newFieldNotFound = new MetaDataNotFoundException("Field not found", "email");
        assertEquals("email", newFieldNotFound.getName());
        
        InvalidValueException oldInvalid = new InvalidValueException("Invalid value");
        assertEquals("Invalid value", oldInvalid.getMessage());
    }

    @Test
    public void testErrorChaining() {
        TestMetaData loader = new TestMetaData("loader", "file", "configLoader");
        TestMetaData config = new TestMetaData("config", "xml", "appConfig");
        
        // Simulate nested error scenario
        try {
            try {
                // Inner error
                throw new MetaDataException("Parse error", config, "parsing",
                    new RuntimeException("XML malformed"),
                    Map.of("lineNumber", 45, "columnNumber", 12));
            } catch (MetaDataException inner) {
                // Outer error that wraps the inner one
                throw new MetaDataException("Loading failed", loader, "load", inner,
                    Map.of("filePath", "/config/app.xml", "loadAttempt", 1));
            }
        } catch (MetaDataException outer) {
            // Check that both levels of error information are preserved
            assertTrue(outer.getMessage().contains("Loading failed"));
            assertTrue(outer.getMessage().contains("Path: loader:configLoader(file)"));
            
            MetaDataException inner = (MetaDataException) outer.getCause();
            assertNotNull(inner);
            assertTrue(inner.getMessage().contains("Parse error"));
            assertTrue(inner.getMessage().contains("Path: config:appConfig(xml)"));
            
            // Check context at both levels
            assertEquals("/config/app.xml", outer.getContextValue("filePath").orElse(null));
            assertEquals(45, inner.getContextValue("lineNumber").orElse(null));
        }
    }

    // Helper test MetaData implementation
    private static class TestMetaData extends MetaData {
        private MetaData parent;
        private final java.util.List<MetaData> children = new java.util.ArrayList<>();

        public TestMetaData(String type, String subType, String name) {
            super(type, subType, name);
        }

        @Override
        public MetaData getParent() {
            return parent;
        }

        @Override
        public java.util.List<MetaData> getChildren() {
            return children;
        }

        public void setTestParent(MetaData parent) {
            this.parent = parent;
        }

        public void addTestChild(MetaData child) {
            children.add(child);
        }
    }
}