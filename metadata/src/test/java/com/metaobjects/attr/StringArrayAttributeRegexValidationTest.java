package com.metaobjects.attr;

import com.metaobjects.loader.parser.BaseMetaDataParser;
import com.metaobjects.registry.SharedRegistryTestBase;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test the universal @isArray support for StringAttribute.
 * This test verifies that StringAttribute with @isArray=true can handle
 * comma-delimited array values correctly, replacing the old StringArrayAttribute.
 *
 * Uses SharedRegistryTestBase to avoid registry conflicts on different platforms.
 */
public class StringArrayAttributeRegexValidationTest extends SharedRegistryTestBase {

    private StringAttribute testAttribute;

    @Before
    public void setUp() {
        // Create StringAttribute with @isArray=true behavior for testing
        // Since we're testing the universal @isArray functionality,
        // we'll use a simple override approach to simulate array mode
        testAttribute = new StringAttribute("testArrayAttr") {
            @Override
            public boolean isArrayType() {
                return true; // Override to simulate @isArray=true
            }
        };
    }

    @Test
    public void testValidCommaSeparatedArrays() {
        // Test that StringAttribute in array mode correctly parses valid comma-separated values

        String[] validArrays = {
            "",                    // Empty string (should result in empty list)
            "item1",              // Single item
            "item1,item2",        // Two items
            "item1,item2,item3",  // Three items
            "a,b,c,d,e",         // Multiple single characters
            "first item,second item,third item" // Items with spaces
        };

        for (String validArray : validArrays) {
            testAttribute.setValueAsString(validArray);

            // Verify the value was set correctly
            Object value = testAttribute.getValue();
            assertNotNull("Valid array '" + validArray + "' should be accepted", value);

            // Verify it's a List (since we're in array mode)
            assertTrue("Array mode should produce a List, got: " + value.getClass().getSimpleName(),
                      value instanceof List);

            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) value;

            // Verify the contents match expectations
            if (validArray.isEmpty()) {
                assertTrue("Empty string should result in empty list", list.isEmpty());
            } else if (!validArray.contains(",")) {
                assertEquals("Single item should result in single-element list", 1, list.size());
                assertEquals("Single item should match input", validArray.trim(), list.get(0));
            } else {
                String[] expectedItems = validArray.split(",");
                assertEquals("List size should match number of comma-separated items",
                           expectedItems.length, list.size());
                for (int i = 0; i < expectedItems.length; i++) {
                    assertEquals("Item " + i + " should match",
                               expectedItems[i].trim(), list.get(i));
                }
            }
        }
    }

    @Test
    public void testArrayParsingBehavior() {
        // Test edge cases in array parsing - some cases may result in unexpected but predictable behavior

        // Test cases that might have leading/trailing/double commas
        String[] edgeCases = {
            ",item1",           // Leading comma - should skip empty element
            "item1,",           // Trailing comma - should skip empty element
            "item1,,item2",     // Double comma - should skip empty element
            ",",                // Just a comma - should result in empty list
            "item1,item2,",     // Trailing comma with multiple items
            ",item1,item2"      // Leading comma with multiple items
        };

        for (String edgeCase : edgeCases) {
            testAttribute.setValueAsString(edgeCase);

            Object value = testAttribute.getValue();
            assertNotNull("Edge case '" + edgeCase + "' should still produce a value", value);

            assertTrue("Array mode should produce a List", value instanceof List);

            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) value;

            // Verify that empty elements are filtered out (as per StringAttribute implementation)
            for (String item : list) {
                assertFalse("Should not contain empty strings after trimming",
                           item == null || item.trim().isEmpty());
            }

            // Specific behavior validation
            if (",".equals(edgeCase)) {
                assertTrue("Just a comma should result in empty list", list.isEmpty());
            }
        }
    }

    @Test
    public void testUniversalArraySupport() {
        // Verify that the universal @isArray support is working correctly

        // Test that isArrayType() returns true
        assertTrue("StringAttribute with @isArray=true should return true for isArrayType()",
                  testAttribute.isArrayType());

        // Test null handling in array mode
        testAttribute.setValueAsString(null);
        assertNull("Null value should remain null in array mode", testAttribute.getValue());

        // Test that the attribute processes values as arrays
        testAttribute.setValueAsString("test");
        Object value = testAttribute.getValue();
        assertTrue("Array mode should produce a List", value instanceof List);

        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) value;
        assertEquals("Single value should result in single-element list", 1, list.size());
        assertEquals("Single value should match input", "test", list.get(0));
    }

    @Test
    public void testSingleValueModeCompatibility() {
        // Test that StringAttribute still works in single value mode (without @isArray)

        // Create a regular StringAttribute without @isArray
        StringAttribute singleValueAttr = new StringAttribute("singleValue");

        // Verify it's NOT in array mode
        assertFalse("StringAttribute without @isArray should return false for isArrayType()",
                   singleValueAttr.isArrayType());

        // Test single value assignment
        singleValueAttr.setValueAsString("single value");

        Object value = singleValueAttr.getValue();
        assertNotNull("Single value should be set", value);

        // In single value mode, it should store the string directly, not as a List
        assertTrue("Single value mode should store String, got: " + value.getClass().getSimpleName(),
                  value instanceof String);

        assertEquals("Single value should match input", "single value", value);

        // Test that comma values are treated as literal strings in single mode
        singleValueAttr.setValueAsString("value1,value2,value3");
        assertEquals("Comma values should be literal in single mode",
                    "value1,value2,value3", singleValueAttr.getValue());
    }
}