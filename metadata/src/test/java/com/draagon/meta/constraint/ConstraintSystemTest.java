package com.draagon.meta.constraint;

import com.draagon.meta.*;
import com.draagon.meta.field.StringField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.object.pojo.PojoMetaObject;
import com.draagon.meta.registry.SharedTestRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Comprehensive tests for the constraint system that replaced ValidationChain.
 *
 * <p>Uses SharedTestRegistry to eliminate test interference and follow
 * the READ-OPTIMIZED architecture where registry is loaded once per
 * application lifetime.</p>
 *
 * <p>These tests verify that constraints are enforced in real-time during metadata construction,
 * ensuring data integrity without requiring explicit validation calls.</p>
 */
public class ConstraintSystemTest {

    private static final Logger log = LoggerFactory.getLogger(ConstraintSystemTest.class);
    private MetaDataLoader loader;

    @Before
    public void setUp() {
        // Use shared registry - no repeated service discovery
        SharedTestRegistry.getInstance();
        log.debug("ConstraintSystemTest setup with shared registry: {}", SharedTestRegistry.getStatus());

        // Create a simple loader with a minimal URI for constraint testing
        SimpleLoader simpleLoader = new SimpleLoader("constraint-test");
        simpleLoader.setSourceURIs(Arrays.asList(URI.create("model:resource:com/draagon/meta/loader/simple/fruitbasket-metadata.json")));
        simpleLoader.init();
        loader = simpleLoader;
    }

    @After
    public void tearDown() {
        // NOTE: Don't destroy loader or reset SharedTestRegistry - preserve for other tests
        // The READ-OPTIMIZED architecture means loaders are permanent for application lifetime
    }

    /**
     * Test that constraint system enforces naming pattern constraints.
     * Names should follow the pattern: ^[a-zA-Z][a-zA-Z0-9_]*$
     */
    @Test
    public void testNamingPatternConstraintEnforcement() {
        PojoMetaObject metaObject = new PojoMetaObject("testObject");
        
        try {
            // Valid names should work
            StringField validField1 = new StringField("validName");
            metaObject.addMetaField(validField1);
            
            StringField validField2 = new StringField("valid_name_123");
            metaObject.addMetaField(validField2);
            
            // This should succeed
            loader.addChild(metaObject);
            
        } catch (Exception e) {
            fail("Valid field names should not cause constraint violations: " + e.getMessage());
        }
    }

    /**
     * Test that constraint system rejects invalid naming patterns.
     */
    @Test
    public void testNamingPatternConstraintViolation() {
        PojoMetaObject metaObject = new PojoMetaObject("testObject");
        
        try {
            // Invalid name starting with number should be rejected
            StringField invalidField = new StringField("123invalid");
            metaObject.addMetaField(invalidField);
            
            // This should fail when adding to loader due to constraint violation
            loader.addChild(metaObject);
            fail("Expected constraint violation for invalid field name pattern");
            
        } catch (ConstraintViolationException e) {
            // Expected - constraint system should reject this
            assertTrue("Should mention pattern constraint", 
                      e.getMessage().contains("pattern"));
            assertTrue("Should mention the invalid value", 
                      e.getMessage().contains("123invalid"));
        } catch (Exception e) {
            // Also acceptable - could be wrapped in MetaDataException
            assertTrue("Should mention constraint violation", 
                      e.getMessage().contains("Constraint violation"));
        }
    }

    /**
     * Test that constraint system enforces required attributes.
     */
    @Test
    public void testRequiredAttributeConstraint() {
        try {
            // Field should require a name
            StringField field = new StringField(null); // Invalid - no name
            
            PojoMetaObject metaObject = new PojoMetaObject("testObject");
            metaObject.addMetaField(field);
            loader.addChild(metaObject);
            
            fail("Expected constraint violation for missing field name");
            
        } catch (Exception e) {
            // Expected - constraint system should enforce required attributes
            assertTrue("Should indicate constraint or validation error", 
                      e.getMessage().contains("name") || e.getMessage().contains("null"));
        }
    }

    /**
     * Test that constraint system enforces data type requirements for fields.
     */
    @Test
    public void testDataTypeConstraintEnforcement() {
        PojoMetaObject metaObject = new PojoMetaObject("testObject");
        StringField field = new StringField("testField");
        
        // Fields should have data types enforced by constraints
        assertNotNull("StringField should have default data type", field.getDataType());
        assertEquals("StringField should have STRING data type", DataTypes.STRING, field.getDataType());
        
        try {
            metaObject.addMetaField(field);
            loader.addChild(metaObject);
            // Should succeed with proper data type
        } catch (Exception e) {
            fail("Valid field with proper data type should not cause constraint violations: " + e.getMessage());
        }
    }

    /**
     * Test that constraint system allows valid metadata construction.
     */
    @Test
    public void testValidMetadataConstruction() {
        try {
            // Create a valid metadata structure
            PojoMetaObject metaObject = new PojoMetaObject("User");
            
            StringField nameField = new StringField("name");
            StringField emailField = new StringField("email"); 
            
            metaObject.addMetaField(nameField);
            metaObject.addMetaField(emailField);
            
            // Add to loader - should succeed with valid structure
            loader.addChild(metaObject);
            
            // Verify structure is intact
            assertEquals("User", metaObject.getName());
            assertEquals(2, metaObject.getMetaFields().size());
            assertNotNull(metaObject.getMetaField("name"));
            assertNotNull(metaObject.getMetaField("email"));
            
        } catch (Exception e) {
            fail("Valid metadata structure should not cause constraint violations: " + e.getMessage());
        }
    }

    /**
     * Test that constraint system enforces field uniqueness within objects.
     */
    @Test
    public void testFieldUniquenessConstraint() {
        PojoMetaObject metaObject = new PojoMetaObject("testObject");
        
        try {
            StringField field1 = new StringField("duplicateName");
            StringField field2 = new StringField("duplicateName");
            
            metaObject.addMetaField(field1);
            metaObject.addMetaField(field2); // Should fail - duplicate name
            
            fail("Expected constraint violation for duplicate field names");
            
        } catch (Exception e) {
            // Expected - constraint system should enforce field uniqueness
            assertTrue("Should mention duplicate or existing child", 
                      e.getMessage().toLowerCase().contains("duplicate") ||
                      e.getMessage().toLowerCase().contains("already exists") ||
                      e.getMessage().toLowerCase().contains("child"));
        }
    }

    /**
     * Test that the constraint system provides meaningful error messages.
     */
    @Test
    public void testConstraintErrorMessages() {
        try {
            PojoMetaObject metaObject = new PojoMetaObject("testObject");
            StringField invalidField = new StringField("123invalid"); // Starts with number
            
            metaObject.addMetaField(invalidField);
            loader.addChild(metaObject);
            
            fail("Expected constraint violation for field name starting with number");
            
        } catch (Exception e) {
            String message = e.getMessage();
            
            // Verify error message contains useful information
            assertTrue("Error message should mention the constraint type", 
                      message.contains("pattern") || message.contains("constraint"));
            assertTrue("Error message should mention the invalid value", 
                      message.contains("123invalid"));
            assertTrue("Error message should be descriptive", 
                      message.length() > 20);
        }
    }

    /**
     * Test that constraint system works without requiring explicit validation calls.
     * This verifies that constraints are enforced during construction, not validation.
     */
    @Test
    public void testConstraintEnforcementDuringConstruction() {
        // The constraint system should enforce rules during addChild(), not during validate()
        PojoMetaObject metaObject = new PojoMetaObject("testObject");
        
        try {
            // Invalid field name should be rejected immediately when added to object
            StringField invalidField = new StringField("9invalidName");
            metaObject.addMetaField(invalidField); // Should work at object level
            
            // But should fail when adding to loader (where constraints are enforced)
            loader.addChild(metaObject);
            fail("Expected constraint violation during construction");
            
        } catch (Exception e) {
            // Expected - constraints enforced during construction, not later validation
            assertTrue("Should be constraint-related error", 
                      e.getMessage().toLowerCase().contains("constraint") ||
                      e.getMessage().toLowerCase().contains("pattern"));
        }
    }
}