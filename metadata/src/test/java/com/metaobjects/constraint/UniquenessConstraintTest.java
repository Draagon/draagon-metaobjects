package com.metaobjects.constraint;

import com.metaobjects.field.MetaField;
import com.metaobjects.field.StringField;
import com.metaobjects.object.MetaObject;
import com.metaobjects.object.pojo.PojoMetaObject;
import com.metaobjects.registry.MetaDataRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for UniquenessConstraint
 * Verifies that the UniquenessConstraint correctly replaces CustomConstraint
 * for field name uniqueness validation.
 */
public class UniquenessConstraintTest {

    private static MetaDataRegistry registry;

    @BeforeClass
    public static void setUp() {
        registry = MetaDataRegistry.getInstance();
    }

    @Test
    public void testForFieldNamesFactoryMethod() {
        UniquenessConstraint constraint = UniquenessConstraint.forFieldNames(
            "test.field.uniqueness",
            "Test field name uniqueness",
            "object", "*"
        );

        assertNotNull("Constraint should be created", constraint);
        assertEquals("Should have correct constraint ID", "test.field.uniqueness", constraint.getConstraintId());
        assertEquals("Should have correct description", "Test field name uniqueness", constraint.getDescription());
        assertEquals("Should have correct type", "uniqueness", constraint.getType());
        assertEquals("Should have correct value description", "field names", constraint.getValueDescription());
        assertEquals("Should target object type", "object", constraint.getTargetType());
        assertEquals("Should target any subtype", "*", constraint.getTargetSubType());
    }

    @Test
    public void testForKeyNamesFactoryMethod() {
        UniquenessConstraint constraint = UniquenessConstraint.forKeyNames(
            "test.key.uniqueness",
            "Test key name uniqueness",
            "object", "*"
        );

        assertNotNull("Constraint should be created", constraint);
        assertEquals("Should have correct value description", "key names", constraint.getValueDescription());
    }

    @Test
    public void testForAttributeNamesFactoryMethod() {
        UniquenessConstraint constraint = UniquenessConstraint.forAttributeNames(
            "test.attr.uniqueness",
            "Test attribute name uniqueness",
            "object", "*"
        );

        assertNotNull("Constraint should be created", constraint);
        assertEquals("Should have correct value description", "attribute names", constraint.getValueDescription());
    }

    @Test
    public void testAppliesToMetaObject() {
        UniquenessConstraint constraint = UniquenessConstraint.forFieldNames(
            "test.field.uniqueness",
            "Test field name uniqueness",
            "object", "*"
        );

        PojoMetaObject metaObject = new PojoMetaObject("TestObject");
        assertTrue("Should apply to MetaObject", constraint.appliesTo(metaObject));

        StringField field = new StringField("testField");
        assertFalse("Should not apply to MetaField", constraint.appliesTo(field));
    }

    @Test
    public void testValidationWithUniqueFieldNames() throws Exception {
        UniquenessConstraint constraint = UniquenessConstraint.forFieldNames(
            "test.field.uniqueness",
            "Test field name uniqueness",
            "object", "*"
        );

        PojoMetaObject metaObject = new PojoMetaObject("TestObject");

        // Add fields with unique names
        StringField field1 = new StringField("firstName");
        StringField field2 = new StringField("lastName");
        StringField field3 = new StringField("email");

        metaObject.addChild(field1);
        metaObject.addChild(field2);
        metaObject.addChild(field3);

        // Should not throw exception with unique field names
        constraint.validate(metaObject, null);
    }

    @Test
    public void testValidationWithDuplicateFieldNames() {
        // Test the constraint directly with a custom object that has duplicates
        // We create the object in a way that bypasses MetaData's built-in uniqueness checking
        UniquenessConstraint constraint = UniquenessConstraint.forFieldNames(
            "test.field.uniqueness",
            "Test field name uniqueness",
            "object", "*"
        );

        // Create a test object that will simulate having duplicate field names
        PojoMetaObject metaObject = new PojoMetaObject("TestObject");

        // Add unique fields first
        StringField field1 = new StringField("firstName");
        StringField field2 = new StringField("lastName");
        metaObject.addChild(field1);
        metaObject.addChild(field2);

        // Now test the constraint by creating a custom constraint that simulates duplicates
        UniquenessConstraint testConstraint = new UniquenessConstraint(
            "test.field.uniqueness",
            "Test field name uniqueness",
            "object", "*", "*",
            (metaData) -> List.of("name", "email", "name"), // Simulated duplicate
            "field names"
        );

        try {
            testConstraint.validate(metaObject, null);
            fail("Should have thrown ConstraintViolationException for duplicate field names");
        } catch (ConstraintViolationException e) {
            assertTrue("Error message should mention duplicate field names",
                      e.getMessage().contains("Duplicate field names"));
            assertTrue("Error message should mention the object name",
                      e.getMessage().contains("TestObject"));
            assertTrue("Error message should mention the duplicate field name",
                      e.getMessage().contains("name"));
            assertEquals("Should have correct constraint ID", "test.field.uniqueness", e.getConstraintType());
        }
    }

    @Test
    public void testValidationWithEmptyFieldList() throws Exception {
        UniquenessConstraint constraint = UniquenessConstraint.forFieldNames(
            "test.field.uniqueness",
            "Test field name uniqueness",
            "object", "*"
        );

        PojoMetaObject metaObject = new PojoMetaObject("EmptyObject");

        // No fields added - should be valid
        constraint.validate(metaObject, null);
    }

    @Test
    public void testValidationWithMultipleDuplicates() {
        // Test with multiple duplicates using simulated data
        PojoMetaObject metaObject = new PojoMetaObject("TestObject");

        // Create a constraint that simulates multiple duplicates
        UniquenessConstraint testConstraint = new UniquenessConstraint(
            "test.field.uniqueness",
            "Test field name uniqueness",
            "object", "*", "*",
            (metaData) -> List.of("name", "email", "name", "email"), // Multiple duplicates
            "field names"
        );

        try {
            testConstraint.validate(metaObject, null);
            fail("Should have thrown ConstraintViolationException for multiple duplicate field names");
        } catch (ConstraintViolationException e) {
            assertTrue("Error message should mention both duplicate names",
                      e.getMessage().contains("email") && e.getMessage().contains("name"));
        }
    }

    @Test
    public void testCustomValueExtractor() {
        // Test with custom value extractor function
        UniquenessConstraint customConstraint = new UniquenessConstraint(
            "custom.test.uniqueness",
            "Test custom uniqueness",
            "object", "*", "*",
            (metaData) -> {
                // Custom extractor that returns a test collection
                return List.of("value1", "value2", "value1"); // Has duplicate
            },
            "test values"
        );

        PojoMetaObject metaObject = new PojoMetaObject("TestObject");

        try {
            customConstraint.validate(metaObject, null);
            fail("Should have thrown ConstraintViolationException for duplicate test values");
        } catch (ConstraintViolationException e) {
            assertTrue("Error message should mention test values",
                      e.getMessage().contains("test values"));
            assertTrue("Error message should mention value1",
                      e.getMessage().contains("value1"));
        }
    }

    @Test
    public void testConstraintRegisteredInMetaDataRegistry() {
        // Verify that the constraint is actually registered and replaces the CustomConstraint
        long uniquenessConstraintCount = registry.getAllValidationConstraints().stream()
            .filter(c -> c instanceof UniquenessConstraint)
            .filter(c -> c.getConstraintId().equals("object.field.uniqueness"))
            .count();

        assertEquals("Should have exactly one UniquenessConstraint for object field uniqueness",
                    1, uniquenessConstraintCount);

        // Verify that we no longer have the CustomConstraint for field uniqueness
        long customConstraintCount = registry.getAllValidationConstraints().stream()
            .filter(c -> c.getClass().getSimpleName().equals("CustomConstraint"))
            .filter(c -> c.getConstraintId().equals("object.field.uniqueness"))
            .count();

        assertEquals("Should not have CustomConstraint for object field uniqueness",
                    0, customConstraintCount);
    }

    @Test
    public void testRealMetaObjectFieldUniquenessValidation() throws Exception {
        // Test with a real MetaObject to ensure the constraint works with the actual registry
        PojoMetaObject testObject = new PojoMetaObject("RealTestObject");

        // Add unique fields with names that don't conflict with reserved attributes
        StringField field1 = new StringField("userId");
        StringField field2 = new StringField("userName");
        StringField field3 = new StringField("userEmail");

        testObject.addChild(field1);
        testObject.addChild(field2);
        testObject.addChild(field3);

        // Verify our UniquenessConstraint is properly registered
        long uniquenessConstraintCount = registry.getAllValidationConstraints().stream()
            .filter(c -> c instanceof UniquenessConstraint)
            .filter(c -> c.getConstraintId().equals("object.field.uniqueness"))
            .count();

        assertEquals("Should have UniquenessConstraint registered for object field uniqueness",
                    1, uniquenessConstraintCount);

        // Test that the constraint works properly with the real field collection
        UniquenessConstraint realConstraint = (UniquenessConstraint) registry.getAllValidationConstraints().stream()
            .filter(c -> c instanceof UniquenessConstraint)
            .filter(c -> c.getConstraintId().equals("object.field.uniqueness"))
            .findFirst()
            .orElse(null);

        assertNotNull("Should find the real UniquenessConstraint", realConstraint);

        // This should pass validation (unique field names)
        realConstraint.validate(testObject, null);
    }
}