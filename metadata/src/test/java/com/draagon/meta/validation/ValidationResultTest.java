package com.draagon.meta.validation;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.ValidationResult;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for ValidationResult class
 */
public class ValidationResultTest {
    
    @Test
    public void testSuccessResult() {
        ValidationResult result = ValidationResult.success();
        
        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
        assertFalse(result.hasChildErrors());
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getChildErrors().isEmpty());
        assertTrue(result.getAllErrors().isEmpty());
    }
    
    @Test
    public void testSingleError() {
        ValidationResult result = ValidationResult.withError("Test error");
        
        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertFalse(result.hasChildErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals("Test error", result.getErrors().get(0));
        assertEquals(1, result.getAllErrors().size());
    }
    
    @Test
    public void testMultipleErrors() {
        List<String> errors = Arrays.asList("Error 1", "Error 2", "Error 3");
        ValidationResult result = ValidationResult.withErrors(errors);
        
        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertFalse(result.hasChildErrors());
        assertEquals(3, result.getErrors().size());
        assertEquals(errors, result.getErrors());
        assertEquals(3, result.getAllErrors().size());
    }
    
    @Test
    public void testBuilderPattern() {
        ValidationResult result = ValidationResult.builder()
            .addError("Main error")
            .addChildError("child1", "Child error 1")
            .addChildError("child1", "Child error 2")
            .addChildError("child2", "Child error 3")
            .build();
        
        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertTrue(result.hasChildErrors());
        
        assertEquals(1, result.getErrors().size());
        assertEquals("Main error", result.getErrors().get(0));
        
        assertEquals(2, result.getChildErrors().size());
        assertEquals(2, result.getChildErrors().get("child1").size());
        assertEquals(1, result.getChildErrors().get("child2").size());
        
        List<String> allErrors = result.getAllErrors();
        assertEquals(4, allErrors.size());
        assertTrue(allErrors.contains("Main error"));
        assertTrue(allErrors.contains("child1: Child error 1"));
        assertTrue(allErrors.contains("child1: Child error 2"));
        assertTrue(allErrors.contains("child2: Child error 3"));
    }
    
    @Test
    public void testCombineResults() {
        ValidationResult result1 = ValidationResult.builder()
            .addError("Error 1")
            .addChildError("child1", "Child error 1")
            .build();
        
        ValidationResult result2 = ValidationResult.builder()
            .addError("Error 2")
            .addChildError("child1", "Child error 2")
            .addChildError("child2", "Child error 3")
            .build();
        
        ValidationResult combined = result1.combine(result2);
        
        assertFalse(combined.isValid());
        assertEquals(2, combined.getErrors().size());
        assertTrue(combined.getErrors().contains("Error 1"));
        assertTrue(combined.getErrors().contains("Error 2"));
        
        assertEquals(2, combined.getChildErrors().size());
        assertEquals(2, combined.getChildErrors().get("child1").size());
        assertEquals(1, combined.getChildErrors().get("child2").size());
    }
    
    @Test
    public void testCombineWithNull() {
        ValidationResult result = ValidationResult.withError("Test error");
        ValidationResult combined = result.combine(null);
        
        assertSame(result, combined);
    }
    
    @Test
    public void testThrowIfInvalidSuccess() {
        ValidationResult result = ValidationResult.success();
        
        // Should not throw
        try {
            result.throwIfInvalid();
        } catch (Exception e) {
            fail("Should not throw exception for valid result");
        }
    }
    
    @Test
    public void testThrowIfInvalidWithErrors() {
        ValidationResult result = ValidationResult.builder()
            .addError("Main error")
            .addChildError("child", "Child error")
            .build();
        
        try {
            result.throwIfInvalid();
            fail("Should throw exception for invalid result");
        } catch (MetaDataException e) {
            String message = e.getMessage();
            assertTrue(message.contains("Validation failed"));
            assertTrue(message.contains("Main error"));
            assertTrue(message.contains("child: Child error"));
        }
    }
    
    @Test
    public void testBuilderFiltersNullAndEmpty() {
        ValidationResult result = ValidationResult.builder()
            .addError(null)
            .addError("")
            .addError("   ")
            .addError("Valid error")
            .addChildError(null, "error")
            .addChildError("child", null)
            .addChildError("child", "")
            .addChildError("child", "Valid child error")
            .build();
        
        assertEquals(1, result.getErrors().size());
        assertEquals("Valid error", result.getErrors().get(0));
        
        assertEquals(1, result.getChildErrors().size());
        assertEquals(1, result.getChildErrors().get("child").size());
        assertEquals("Valid child error", result.getChildErrors().get("child").get(0));
    }
    
    @Test
    public void testChildResultHandling() {
        ValidationResult childResult = ValidationResult.builder()
            .addError("Child main error")
            .addChildError("grandchild", "Grandchild error")
            .build();
        
        ValidationResult result = ValidationResult.builder()
            .addError("Main error")
            .addChildResult("child", childResult)
            .build();
        
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals("Main error", result.getErrors().get(0));
        
        assertEquals(2, result.getChildErrors().size());
        assertEquals(1, result.getChildErrors().get("child").size());
        assertEquals("Child main error", result.getChildErrors().get("child").get(0));
        assertEquals(1, result.getChildErrors().get("child.grandchild").size());
        assertEquals("Grandchild error", result.getChildErrors().get("child.grandchild").get(0));
    }
    
    @Test
    public void testToString() {
        ValidationResult validResult = ValidationResult.success();
        assertEquals("ValidationResult: VALID", validResult.toString());
        
        ValidationResult invalidResult = ValidationResult.builder()
            .addError("Main error")
            .addChildError("child", "Child error")
            .build();
        
        String str = invalidResult.toString();
        assertTrue(str.contains("ValidationResult: INVALID"));
        assertTrue(str.contains("Errors:"));
        assertTrue(str.contains("Main error"));
        assertTrue(str.contains("Child Errors:"));
        assertTrue(str.contains("child:"));
        assertTrue(str.contains("Child error"));
    }
    
    @Test
    public void testImmutability() {
        ValidationResult.Builder builder = ValidationResult.builder()
            .addError("Error 1");
        
        ValidationResult result1 = builder.build();
        
        builder.addError("Error 2");
        ValidationResult result2 = builder.build();
        
        // First result should not be affected by subsequent builder changes
        assertEquals(1, result1.getErrors().size());
        assertEquals(2, result2.getErrors().size());
        
        // Attempting to modify returned collections should not affect the result
        List<String> errors = result1.getErrors();
        int originalSize = errors.size();
        
        try {
            errors.add("New error");
            fail("Should not be able to modify returned error list");
        } catch (UnsupportedOperationException e) {
            // Expected - list should be immutable
            assertEquals(originalSize, result1.getErrors().size());
        }
    }
}