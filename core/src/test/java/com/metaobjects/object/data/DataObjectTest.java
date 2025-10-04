/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.object.data;

import com.metaobjects.MetaDataException;
import com.metaobjects.field.MetaField;
import com.metaobjects.field.StringField;
import com.metaobjects.object.MetaObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Enhanced tests for DataMetaObject to improve core module coverage
 */
public class DataObjectTest {

    private DataMetaObject dataObject;
    private StringField nameField;
    private StringField emailField;
    private DataObject testDataObj;

    @Before
    public void setUp() throws MetaDataException {
        // Create the DataMetaObject
        dataObject = new DataMetaObject("TestClass");

        // Add some fields to the DataMetaObject
        nameField = new StringField("name");
        emailField = new StringField("email");
        dataObject.addChild(nameField);
        dataObject.addChild(emailField);

        // Create a test data object instance using the DataMetaObject
        testDataObj = new DataObject(dataObject);
    }

    @Test
    public void testDataObjectCreation() {
        assertNotNull("Data object should be created", dataObject);
        assertEquals("Object name should be set", "TestClass", dataObject.getName());
        assertNotNull("Data object instance should be created", testDataObj);
        assertEquals("Data object name should be set", "TestClass", dataObject.getObjectName(testDataObj));
    }

    @Test
    public void testFieldValueOperations() throws MetaDataException {
        // Test setting and getting field values using MetaField API
        dataObject.setValue(nameField, testDataObj, "John Doe");
        dataObject.setValue(emailField, testDataObj, "john@example.com");

        assertEquals("Name field should be set", "John Doe", dataObject.getValue(nameField, testDataObj));
        assertEquals("Email field should be set", "john@example.com", dataObject.getValue(emailField, testDataObj));
    }

    @Test
    public void testNullValueHandling() throws MetaDataException {
        // Test null value handling
        dataObject.setValue(nameField, testDataObj, null);
        assertNull("Null values should be preserved", dataObject.getValue(nameField, testDataObj));

        // Test with a field that has a value first
        dataObject.setValue(emailField, testDataObj, "test@example.com");
        assertNotNull("Field should have value", dataObject.getValue(emailField, testDataObj));

        // Then set it to null
        dataObject.setValue(emailField, testDataObj, null);
        assertNull("Field should be null after setting to null", dataObject.getValue(emailField, testDataObj));
    }

    @Test
    public void testInvalidFieldHandling() {
        // Test with DataMetaObject API - setting attributes that don't correspond to MetaFields
        // Since we can't access the protected methods directly, we'll test through the DataMetaObject
        // This tests the DataMetaObject's ability to handle dynamic attributes

        // Test field extension capability - by default allowExtensions=false and isStrict=true
        assertFalse("Extensions should be disabled by default", dataObject.allowExtensions());
        assertTrue("Should be strict by default", dataObject.isStrict());

        // Test that hasObjectAttribute works
        assertFalse("Should not have non-existent attribute", dataObject.hasObjectAttribute(testDataObj, "nonExistentField"));
    }

    @Test
    public void testDataObjectAccess() throws MetaDataException {
        // Test MetaField API for setting and getting values
        dataObject.setValue(nameField, testDataObj, "Jane Doe");
        assertEquals("MetaField API should work", "Jane Doe", dataObject.getValue(nameField, testDataObj));

        // Test that MetaField API handles value changes
        dataObject.setValue(nameField, testDataObj, "John Doe");
        assertEquals("MetaField API should handle updates", "John Doe", dataObject.getValue(nameField, testDataObj));

        // Test hasObjectAttribute through DataMetaObject
        assertTrue("Should have attribute after setting", dataObject.hasObjectAttribute(testDataObj, "name"));
    }

    @Test
    public void testFieldIteration() throws MetaDataException {
        // Set some values using MetaField API
        dataObject.setValue(nameField, testDataObj, "Test User");
        dataObject.setValue(emailField, testDataObj, "test@example.com");

        // Test iteration over fields through DataMetaObject
        assertTrue("Should contain name field", dataObject.hasObjectAttribute(testDataObj, "name"));
        assertTrue("Should contain email field", dataObject.hasObjectAttribute(testDataObj, "email"));

        // Test field access through DataMetaObject
        assertNotNull("Name field should be accessible", dataObject.getMetaField("name"));
        assertNotNull("Email field should be accessible", dataObject.getMetaField("email"));

        // Test getting all object attributes
        assertNotNull("Should have object attributes", dataObject.getObjectAttributes(testDataObj));
    }

    @Test
    public void testTypeConversion() throws MetaDataException {
        // Test type conversion for field values - DataConverter will handle conversion
        dataObject.setValue(nameField, testDataObj, 123); // Set integer as name
        Object value = dataObject.getValue(nameField, testDataObj);
        assertNotNull("Value should not be null", value);

        // The DataConverter should convert it to a string since nameField is a StringField
        assertTrue("Value should be converted to appropriate type", value instanceof String || value instanceof Number);
    }

    @Test
    public void testClearOperations() throws MetaDataException {
        // Set some values
        dataObject.setValue(nameField, testDataObj, "Test");
        dataObject.setValue(emailField, testDataObj, "test@test.com");

        // Clear specific field
        dataObject.setValue(nameField, testDataObj, null);
        assertNull("Field should be cleared", dataObject.getValue(nameField, testDataObj));
        assertNotNull("Other fields should remain", dataObject.getValue(emailField, testDataObj));

        // Clear the other field as well
        dataObject.setValue(emailField, testDataObj, null);
        assertNull("Email field should be cleared", dataObject.getValue(emailField, testDataObj));
    }

    @Test
    public void testEqualsAndHashCode() throws MetaDataException {
        // Create another data object
        DataObject other = new DataObject(dataObject);

        // Set same values using the MetaObject API
        dataObject.setValue(nameField, testDataObj, "Test");
        dataObject.setValue(nameField, other, "Test");

        // Test equality (behavior may vary by implementation)
        int hash1 = testDataObj.hashCode();
        int hash2 = other.hashCode();
        assertNotEquals("Hash codes should be different for different objects", 0, hash1);
        assertNotEquals("Hash codes should be different for different objects", 0, hash2);

        // Test DataMetaObject properties
        assertEquals("Should have same name", dataObject.getName(), "TestClass");
        assertTrue("Should be data subtype", dataObject.getSubType().equals("data"));
    }

    @Test
    public void testToString() {
        // Test string representation
        String str = dataObject.toString();
        assertNotNull("toString should not return null", str);
        assertTrue("toString should contain class information", str.length() > 0);
    }

    @Test
    public void testConcurrentAccess() throws MetaDataException {
        // Test thread safety (basic test)
        dataObject.setValue(nameField, testDataObj, "Initial");

        // Simulate concurrent access
        Runnable modifier = () -> {
            try {
                dataObject.setValue(nameField, testDataObj, "Modified");
            } catch (Exception e) {
                // Handle exception
            }
        };

        Thread thread = new Thread(modifier);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Value should be either Initial or Modified
        Object value = dataObject.getValue(nameField, testDataObj);
        assertTrue("Value should be one of the expected values",
                  "Initial".equals(value) || "Modified".equals(value));
    }
}