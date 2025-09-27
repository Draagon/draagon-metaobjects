package com.metaobjects.constraint;

import com.metaobjects.attr.BooleanAttribute;
import com.metaobjects.attr.IntAttribute;
import com.metaobjects.attr.MetaAttribute;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.field.MetaField;
import com.metaobjects.field.StringField;
import com.metaobjects.object.MetaObject;
import com.metaobjects.registry.ChildRequirement;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example showing how to create unified constraint rules that work for both
 * PlacementConstraint (for validation) and ChildRequirement (for schema generation)
 * using the enhanced constant-based APIs.
 */
public class UnifiedConstraintExampleTest {

    @Test
    public void testUnifiedAttributeRuleForStringField() {
        // Example: StringField can have maxLength attribute

        // NEW WAY: PlacementConstraint using constants (for validation)
        PlacementConstraint placementConstraint = PlacementConstraint.allowAttribute(
            "stringfield.maxlength.placement",
            "StringField can optionally have maxLength attribute",
            MetaField.TYPE_FIELD, StringField.SUBTYPE_STRING,    // Parent: field.string
            IntAttribute.SUBTYPE_INT, StringField.ATTR_MAX_LENGTH // Child: attr.int[maxLength]
        );

        // Corresponding ChildRequirement (for schema generation and type definitions)
        ChildRequirement childRequirement = ChildRequirement.optional(
            StringField.ATTR_MAX_LENGTH,        // Attribute name
            MetaAttribute.TYPE_ATTR,            // Child type
            IntAttribute.SUBTYPE_INT            // Child subtype
        );

        // Verify they reference the same concepts
        assertEquals("maxLength", StringField.ATTR_MAX_LENGTH);
        assertEquals("attr", MetaAttribute.TYPE_ATTR);
        assertEquals("int", IntAttribute.SUBTYPE_INT);
        assertEquals("field", MetaField.TYPE_FIELD);
        assertEquals("string", StringField.SUBTYPE_STRING);

        // Both use the same constants - no string literal duplication!
        assertTrue("PlacementConstraint allows the attribute", placementConstraint.isAllowed());
        assertFalse("ChildRequirement is optional", childRequirement.isRequired());
        assertTrue("ChildRequirement matches the type",
            childRequirement.matches(MetaAttribute.TYPE_ATTR, IntAttribute.SUBTYPE_INT, StringField.ATTR_MAX_LENGTH));
    }

    @Test
    public void testUnifiedRuleForRequiredAttribute() {
        // Example: Any field can have a required attribute

        // PlacementConstraint: Allow boolean 'required' attribute on any field
        PlacementConstraint placementConstraint = PlacementConstraint.allowAttributeOnAnyField(
            "field.required.placement",
            "Fields can optionally have required attribute",
            BooleanAttribute.SUBTYPE_BOOLEAN, MetaField.ATTR_REQUIRED
        );

        // ChildRequirement: Define required as optional boolean attribute
        ChildRequirement childRequirement = ChildRequirement.optional(
            MetaField.ATTR_REQUIRED,            // Attribute name
            MetaAttribute.TYPE_ATTR,            // Child type
            BooleanAttribute.SUBTYPE_BOOLEAN    // Child subtype
        );

        // Verify consistency using constants
        assertEquals("required", MetaField.ATTR_REQUIRED);
        assertEquals("boolean", BooleanAttribute.SUBTYPE_BOOLEAN);
        assertEquals("field.*", placementConstraint.getParentPattern());
        assertEquals("attr.boolean[required]", placementConstraint.getChildPattern());
    }

    @Test
    public void testUnifiedRuleForObjectContainingFields() {
        // Example: Objects can contain fields

        // PlacementConstraint: Allow any field type under any object type
        PlacementConstraint placementConstraint = PlacementConstraint.allowChildType(
            "object.fields.placement",
            "Objects can contain fields",
            MetaObject.TYPE_OBJECT, "*",        // Parent: object.*
            MetaField.TYPE_FIELD, "*"           // Child: field.*
        );

        // ChildRequirement: Objects can have field children (wildcard matching)
        ChildRequirement childRequirement = ChildRequirement.optional(
            "*",                                // Any field name
            MetaField.TYPE_FIELD,               // Child type
            "*"                                 // Any field subtype
        );

        // Verify they use the same type constants
        assertEquals("object", MetaObject.TYPE_OBJECT);
        assertEquals("field", MetaField.TYPE_FIELD);
        assertEquals("object.*", placementConstraint.getParentPattern());
        assertEquals("field.*", placementConstraint.getChildPattern());
        assertTrue("ChildRequirement uses wildcards", childRequirement.isWildcard());
    }

    @Test
    public void testDemonstrateProblemWithOldApproach() {
        // OLD WAY: Error-prone string literals (what we're replacing)
        PlacementConstraint oldConstraint = new PlacementConstraint(
            "old.stringfield.maxlength",
            "Old way with string literals",
            "field.string",           // ❌ Could have typos
            "attr.int[maxLength]",    // ❌ Could have typos
            true
        );

        // NEW WAY: Type-safe with constants
        PlacementConstraint newConstraint = PlacementConstraint.allowAttribute(
            "new.stringfield.maxlength",
            "New way with constants",
            MetaField.TYPE_FIELD, StringField.SUBTYPE_STRING,     // ✅ Compile-time checked
            IntAttribute.SUBTYPE_INT, StringField.ATTR_MAX_LENGTH // ✅ Compile-time checked
        );

        // They should produce identical patterns
        assertEquals("Both approaches produce same parent pattern",
            oldConstraint.getParentPattern(), newConstraint.getParentPattern());
        assertEquals("Both approaches produce same child pattern",
            oldConstraint.getChildPattern(), newConstraint.getChildPattern());

        // But the new way is type-safe and uses constants!
    }

    @Test
    public void testAdvancedConstraintCombinations() {
        // Example: Complex rule with multiple constraints working together

        // 1. StringField CAN have pattern attribute
        PlacementConstraint allowPattern = PlacementConstraint.allowAttribute(
            "stringfield.pattern.allow",
            "StringField can have pattern attribute",
            MetaField.TYPE_FIELD, StringField.SUBTYPE_STRING,
            StringAttribute.SUBTYPE_STRING, StringField.ATTR_PATTERN
        );

        // 2. IntegerField CANNOT have pattern attribute (forbidden)
        PlacementConstraint forbidPattern = PlacementConstraint.forbidAttribute(
            "integerfield.pattern.forbid",
            "IntegerField cannot have pattern attribute",
            MetaField.TYPE_FIELD, "int",  // IntegerField.SUBTYPE_INT
            StringAttribute.SUBTYPE_STRING, StringField.ATTR_PATTERN
        );

        // 3. ChildRequirement for StringField pattern attribute
        ChildRequirement patternRequirement = ChildRequirement.optional(
            StringField.ATTR_PATTERN,
            MetaAttribute.TYPE_ATTR,
            StringAttribute.SUBTYPE_STRING
        );

        // Verify all use the same constants
        assertEquals("pattern", StringField.ATTR_PATTERN);
        assertEquals("string", StringAttribute.SUBTYPE_STRING);
        assertTrue("StringField allows pattern", allowPattern.isAllowed());
        assertTrue("IntegerField forbids pattern", forbidPattern.isForbidden());
        assertTrue("Pattern requirement matches correctly",
            patternRequirement.matches(MetaAttribute.TYPE_ATTR, StringAttribute.SUBTYPE_STRING, StringField.ATTR_PATTERN));
    }
}