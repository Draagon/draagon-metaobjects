package com.draagon.meta.constraint;

import com.draagon.meta.attr.StringArrayAttribute;
import com.draagon.meta.field.StringField;
import com.draagon.meta.registry.SharedRegistryTestBase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Test the consolidated constraint system using shared MetaDataRegistry.
 *
 * This test verifies that:
 * 1. Constraints are properly registered via consolidated MetaDataRegistry
 * 2. MetaData classes use the unified constraint registration approach
 * 3. StringArrayAttribute and other classes register their constraints properly
 * 4. The consolidated registry provides both type and constraint functionality
 */
public class UnifiedConstraintSystemTest extends SharedRegistryTestBase {

    private static final Logger log = LoggerFactory.getLogger(UnifiedConstraintSystemTest.class);

    @Test
    public void testBaseClassConstraintsRegistered() {
        // Verify that base class constraints are registered in the consolidated registry
        log.info("Testing consolidated constraint registry pattern");

        // Verify specific base class constraints are registered
        var allConstraints = getSharedRegistry().getAllConstraints();

        // Check for MetaField constraints
        boolean foundFieldNaming = allConstraints.stream()
            .anyMatch(c -> "field.naming.pattern".equals(c.getConstraintId()));
        boolean foundFieldRequired = allConstraints.stream()
            .anyMatch(c -> "field.required.placement".equals(c.getConstraintId()));

        // Check for MetaAttribute constraints
        boolean foundAttrPlacement = allConstraints.stream()
            .anyMatch(c -> "attribute.universal.placement".equals(c.getConstraintId()));
        boolean foundAttrNaming = allConstraints.stream()
            .anyMatch(c -> "attribute.naming.pattern".equals(c.getConstraintId()));

        // Check for MetaObject constraints
        boolean foundObjNaming = allConstraints.stream()
            .anyMatch(c -> "object.naming.pattern".equals(c.getConstraintId()));
        boolean foundFieldsPlacement = allConstraints.stream()
            .anyMatch(c -> "object.fields.placement".equals(c.getConstraintId()));

        assertTrue("Should find field naming constraint", foundFieldNaming);
        assertTrue("Should find field required placement constraint", foundFieldRequired);
        assertTrue("Should find attribute placement constraint", foundAttrPlacement);
        assertTrue("Should find attribute naming constraint", foundAttrNaming);
        assertTrue("Should find object naming constraint", foundObjNaming);
        assertTrue("Should find object fields placement constraint", foundFieldsPlacement);
    }
    
    @Test
    public void testConstraintsLoaded() {
        // Verify that constraints are loaded in the consolidated registry
        var stats = getSharedRegistry().getStats();

        assertTrue("Should have constraints loaded", stats.constraintStats().size() > 0);
        assertTrue("Should have types loaded", stats.totalTypes() > 0);

        log.info("Consolidated registry stats: {}", stats);

        // Check for specific constraint types
        var allConstraints = getSharedRegistry().getAllConstraints();
        var placementConstraints = allConstraints.stream()
            .filter(c -> c.getValidationDescription().contains("can optionally have"))
            .count();
        var validationConstraints = allConstraints.stream()
            .filter(c -> c.getValidationDescription().contains("must") ||
                        c.getValidationDescription().contains("pattern") ||
                        c.getValidationDescription().contains("validation"))
            .count();

        assertTrue("Should have placement constraints", placementConstraints > 0);
        assertTrue("Should have validation constraints", validationConstraints > 0);

        log.info("Loaded {} placement constraints and {} validation constraints",
                 placementConstraints, validationConstraints);
    }
    
    @Test
    public void testStringFieldConstraints() {
        // Test that StringField constraints are properly loaded in consolidated registry
        var allConstraints = getSharedRegistry().getAllConstraints();

        // Look for StringField-specific constraints
        boolean foundMaxLengthPlacement = allConstraints.stream()
            .anyMatch(c -> c.getConstraintId().contains("stringfield.maxlength"));
        boolean foundPatternPlacement = allConstraints.stream()
            .anyMatch(c -> c.getConstraintId().contains("stringfield.pattern"));

        assertTrue("Should find StringField maxLength placement constraint", foundMaxLengthPlacement);
        assertTrue("Should find StringField pattern placement constraint", foundPatternPlacement);

        boolean foundFieldNamingValidation = allConstraints.stream()
            .anyMatch(c -> c.getConstraintId().contains("field.naming.pattern"));

        assertTrue("Should find field naming validation constraint", foundFieldNamingValidation);
    }
    
    @Test
    public void testStringArrayAttributeLoadable() {
        // Test that StringArrayAttribute works with consolidated registry

        try {
            // Test that StringArrayAttribute can be created
            StringArrayAttribute attr = StringArrayAttribute.create("testArray", "value1,value2");
            assertNotNull("StringArrayAttribute should be creatable", attr);
            assertEquals("Should have correct name", "testArray", attr.getName());

            // Test that it has proper subtype registered
            assertEquals("Should have stringarray subtype", "stringarray", attr.getSubType());

            log.info("StringArrayAttribute created successfully: {} with values: {}",
                     attr.getName(), attr.getValue());

        } catch (Exception e) {
            fail("StringArrayAttribute should work with consolidated registry: " + e.getMessage());
        }
    }

    @Test
    public void testConsolidatedRegistryFunctionality() {
        // Test that the consolidated registry provides both type and constraint functionality
        var stats = getSharedRegistry().getStats();

        assertTrue("Should have types registered", stats.totalTypes() > 0);
        assertTrue("Should have constraints registered", stats.constraintStats().size() > 0);

        log.info("Consolidated registry contains {} types and {} constraints",
                 stats.totalTypes(), stats.constraintStats().size());

        // Test that we can access both types and constraints from same registry
        var registeredTypes = getSharedRegistry().getRegisteredTypes();
        var allConstraints = getSharedRegistry().getAllConstraints();

        assertFalse("Should have registered types", registeredTypes.isEmpty());
        assertFalse("Should have registered constraints", allConstraints.isEmpty());
    }

    @Test
    public void testConstraintValidation() {
        // Test that constraints can validate metadata
        var allConstraints = getSharedRegistry().getAllConstraints();

        // Find a validation constraint to test
        var validationConstraint = allConstraints.stream()
            .filter(c -> c.getConstraintId().contains("field.naming.pattern"))
            .findFirst();

        assertTrue("Should find field naming pattern constraint", validationConstraint.isPresent());

        var constraint = validationConstraint.get();
        log.info("Found validation constraint: {} - {}",
                 constraint.getConstraintId(), constraint.getValidationDescription());

        // Test constraint validation functionality exists
        assertNotNull("Constraint should have validation description", constraint.getValidationDescription());
        assertNotNull("Constraint should have constraint ID", constraint.getConstraintId());
    }
}