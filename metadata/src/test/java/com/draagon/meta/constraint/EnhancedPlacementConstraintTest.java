package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.field.StringField;
import com.draagon.meta.object.MetaObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test the enhanced PlacementConstraint with separate type/subtype/name fields and static factory methods
 */
public class EnhancedPlacementConstraintTest {

    @Test
    public void testNewConstructorWithSeparateFields() {
        // Test the new constructor that takes separate type/subtype/name parameters
        PlacementConstraint constraint = new PlacementConstraint(
            "test.constraint",
            "Test constraint with separate fields",
            MetaField.TYPE_FIELD, StringField.SUBTYPE_STRING,
            MetaAttribute.TYPE_ATTR, IntAttribute.SUBTYPE_INT, StringField.ATTR_MAX_LENGTH,
            true
        );

        assertEquals("test.constraint", constraint.getConstraintId());
        assertEquals("Test constraint with separate fields", constraint.getDescription());
        assertEquals("field.string", constraint.getParentPattern());
        assertEquals("attr.int[maxLength]", constraint.getChildPattern());
        assertTrue(constraint.isAllowed());
    }

    @Test
    public void testAllowAttributeFactoryMethod() {
        // Test the allowAttribute static factory method
        PlacementConstraint constraint = PlacementConstraint.allowAttribute(
            "stringfield.maxlength",
            "StringField can have maxLength attribute",
            MetaField.TYPE_FIELD, StringField.SUBTYPE_STRING,
            IntAttribute.SUBTYPE_INT, StringField.ATTR_MAX_LENGTH
        );

        assertEquals("stringfield.maxlength", constraint.getConstraintId());
        assertEquals("field.string", constraint.getParentPattern());
        assertEquals("attr.int[maxLength]", constraint.getChildPattern());
        assertTrue(constraint.isAllowed());
    }

    @Test
    public void testAllowAttributeOnAnyFieldFactoryMethod() {
        // Test the allowAttributeOnAnyField static factory method
        PlacementConstraint constraint = PlacementConstraint.allowAttributeOnAnyField(
            "field.required",
            "Any field can have required attribute",
            "boolean", MetaField.ATTR_REQUIRED
        );

        assertEquals("field.required", constraint.getConstraintId());
        assertEquals("field.*", constraint.getParentPattern());
        assertEquals("attr.boolean[required]", constraint.getChildPattern());
        assertTrue(constraint.isAllowed());
    }

    @Test
    public void testAllowChildTypeFactoryMethod() {
        // Test the allowChildType static factory method
        PlacementConstraint constraint = PlacementConstraint.allowChildType(
            "metadata.fields",
            "Metadata can contain fields",
            "metadata", "base",
            MetaField.TYPE_FIELD, "*"
        );

        assertEquals("metadata.fields", constraint.getConstraintId());
        assertEquals("metadata.base", constraint.getParentPattern());
        assertEquals("field.*", constraint.getChildPattern());
        assertTrue(constraint.isAllowed());
    }

    @Test
    public void testForbidAttributeFactoryMethod() {
        // Test the forbidAttribute static factory method
        PlacementConstraint constraint = PlacementConstraint.forbidAttribute(
            "object.maxlength.forbidden",
            "Objects cannot have maxLength attribute",
            MetaObject.TYPE_OBJECT, "*",
            IntAttribute.SUBTYPE_INT, StringField.ATTR_MAX_LENGTH
        );

        assertEquals("object.maxlength.forbidden", constraint.getConstraintId());
        assertEquals("object.*", constraint.getParentPattern());
        assertEquals("attr.int[maxLength]", constraint.getChildPattern());
        assertFalse(constraint.isAllowed());
        assertTrue(constraint.isForbidden());
    }

    @Test
    public void testPatternBuildingWithNullName() {
        // Test pattern building when name is null (no name constraint)
        PlacementConstraint constraint = new PlacementConstraint(
            "test.noname",
            "Test without name constraint",
            MetaField.TYPE_FIELD, "*",
            MetaAttribute.TYPE_ATTR, StringAttribute.SUBTYPE_STRING, null,
            true
        );

        assertEquals("field.*", constraint.getParentPattern());
        assertEquals("attr.string", constraint.getChildPattern()); // No [name] part
    }

    @Test
    public void testPatternBuildingWithWildcardName() {
        // Test pattern building when name is "*" (wildcard name)
        PlacementConstraint constraint = new PlacementConstraint(
            "test.wildcard",
            "Test with wildcard name",
            MetaField.TYPE_FIELD, "*",
            MetaAttribute.TYPE_ATTR, StringAttribute.SUBTYPE_STRING, "*",
            true
        );

        assertEquals("field.*", constraint.getParentPattern());
        assertEquals("attr.string", constraint.getChildPattern()); // No [name] part for wildcard
    }

    @Test
    public void testBackwardCompatibilityWithLegacyConstructor() {
        // Ensure the legacy string-based constructor still works
        PlacementConstraint legacyConstraint = new PlacementConstraint(
            "legacy.test",
            "Legacy constraint",
            "field.string",
            "attr.int[maxLength]",
            true
        );

        PlacementConstraint newConstraint = PlacementConstraint.allowAttribute(
            "new.test",
            "New constraint",
            MetaField.TYPE_FIELD, StringField.SUBTYPE_STRING,
            IntAttribute.SUBTYPE_INT, StringField.ATTR_MAX_LENGTH
        );

        // Both should produce the same patterns
        assertEquals(legacyConstraint.getParentPattern(), newConstraint.getParentPattern());
        assertEquals(legacyConstraint.getChildPattern(), newConstraint.getChildPattern());
    }
}