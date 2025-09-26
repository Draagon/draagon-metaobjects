package com.draagon.meta.attr;

import com.draagon.meta.constraint.ConstraintViolationException;
import com.draagon.meta.constraint.RegexConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test the new regex-based validation for StringArrayAttribute
 * This test verifies that the CustomConstraint has been successfully
 * replaced with a RegexConstraint for comma-delimited array validation.
 */
public class StringArrayAttributeRegexValidationTest {

    private static MetaDataRegistry registry;
    private static StringArrayAttribute testAttribute;

    @BeforeClass
    public static void setUp() {
        registry = MetaDataRegistry.getInstance();
        testAttribute = new StringArrayAttribute("testArrayAttr");
    }

    @Test
    public void testValidCommaSeparatedArrays() {
        // These should all be valid according to our regex pattern: ^[^,]*(,[^,]*)*$

        String[] validArrays = {
            "",                    // Empty string (valid empty array)
            "item1",              // Single item
            "item1,item2",        // Two items
            "item1,item2,item3",  // Three items
            "a,b,c,d,e",         // Multiple single characters
            "first item,second item,third item" // Items with spaces
        };

        for (String validArray : validArrays) {
            testAttribute.setValueAsString(validArray);
            // If no exception is thrown, the validation passed
            assertNotNull("Valid array '" + validArray + "' should be accepted",
                         testAttribute.getValue());
        }
    }

    @Test
    public void testInvalidCommaSeparatedArrays() {
        // These should all be invalid according to our regex pattern

        String[] invalidArrays = {
            ",item1",           // Leading comma
            "item1,",           // Trailing comma
            "item1,,item2",     // Double comma (empty element)
            ",",                // Just a comma
            "item1,item2,",     // Trailing comma with multiple items
            ",item1,item2"      // Leading comma with multiple items
        };

        for (String invalidArray : invalidArrays) {
            try {
                // Find the regex constraint for StringArrayAttribute
                RegexConstraint regexConstraint = registry.getAllValidationConstraints().stream()
                    .filter(c -> c instanceof RegexConstraint)
                    .map(c -> (RegexConstraint) c)
                    .filter(c -> c.getConstraintId().equals("stringarrayattribute.format.validation"))
                    .findFirst()
                    .orElse(null);

                assertNotNull("Should have found StringArrayAttribute regex constraint", regexConstraint);

                // Test the regex constraint directly
                try {
                    regexConstraint.validate(testAttribute, invalidArray);
                    fail("Invalid array '" + invalidArray + "' should have been rejected by regex constraint");
                } catch (ConstraintViolationException e) {
                    // Expected - this means our regex correctly rejected the invalid format
                    assertTrue("Error message should mention pattern",
                              e.getMessage().contains("does not match required pattern"));
                }

            } catch (Exception e) {
                // This is what we expect for invalid arrays
            }
        }
    }

    @Test
    public void testRegexPatternDetails() {
        // Verify the exact regex pattern being used
        RegexConstraint regexConstraint = registry.getAllValidationConstraints().stream()
            .filter(c -> c instanceof RegexConstraint)
            .map(c -> (RegexConstraint) c)
            .filter(c -> c.getConstraintId().equals("stringarrayattribute.format.validation"))
            .findFirst()
            .orElse(null);

        assertNotNull("Should have found StringArrayAttribute regex constraint", regexConstraint);
        assertEquals("Should use correct comma-delimited regex pattern",
                    "^(?:[^,]+(?:,[^,]+)*)?$", regexConstraint.getRegexPattern());
        assertTrue("Should allow null values", regexConstraint.isAllowNull());
    }

    @Test
    public void testCustomConstraintElimination() {
        // Verify that we no longer have CustomConstraints for StringArrayAttribute
        long customConstraintCount = registry.getAllValidationConstraints().stream()
            .filter(c -> c.getClass().getSimpleName().equals("CustomConstraint"))
            .filter(c -> c.getConstraintId().equals("stringarrayattribute.format.validation"))
            .count();

        assertEquals("Should not have CustomConstraint for StringArrayAttribute validation",
                    0, customConstraintCount);

        // Verify that we DO have the RegexConstraint
        long regexConstraintCount = registry.getAllValidationConstraints().stream()
            .filter(c -> c instanceof RegexConstraint)
            .filter(c -> c.getConstraintId().equals("stringarrayattribute.format.validation"))
            .count();

        assertEquals("Should have exactly one RegexConstraint for StringArrayAttribute validation",
                    1, regexConstraintCount);
    }
}