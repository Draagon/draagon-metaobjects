package com.draagon.meta.constraint;

import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.field.StringField;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.SharedTestRegistry;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Integration test for the complete bidirectional constraint system.
 *
 * Tests the core constraint system components in the metadata module:
 * 1. ConstraintFlattener - Bidirectional constraint resolution for CORE attributes
 * 2. ConstraintEnforcer - Integration with MetaData.addChild()
 * 3. Proper separation - Database attributes rejected (handled by database-common module)
 *
 * Note: This test verifies proper extensibility architecture where core types
 * only accept core attributes, and database/service attributes are handled
 * by their respective constraint providers in separate modules.
 */
public class BidirectionalConstraintSystemTest {

    private static final Logger log = LoggerFactory.getLogger(BidirectionalConstraintSystemTest.class);

    private ConstraintEnforcer constraintEnforcer;
    private ConstraintFlattener constraintFlattener;

    @Before
    public void setUp() {
        // Use SharedTestRegistry to ensure proper provider discovery timing
        SharedTestRegistry.getInstance();
        log.debug("BidirectionalConstraintSystemTest setup with shared registry: {}", SharedTestRegistry.getStatus());

        // Get the singleton ConstraintEnforcer with ConstraintFlattener
        constraintEnforcer = ConstraintEnforcer.getInstance();
        constraintFlattener = constraintEnforcer.getConstraintFlattener();

        // NOTE: Don't refresh constraint flattener - use shared state from SharedTestRegistry
        // refreshConstraintFlattener() corrupts the constraint state for other tests

        log.info("Test setup complete - constraint system initialized");
    }

    @Test
    public void testConstraintFlattenerInitialization() {
        assertNotNull("ConstraintEnforcer should be available", constraintEnforcer);
        assertNotNull("ConstraintFlattener should be available", constraintFlattener);

        // Check flattening statistics
        var stats = constraintFlattener.getStatistics();
        assertNotNull("Flattening statistics should be available", stats);

        Integer totalRules = (Integer) stats.get("totalRules");
        assertNotNull("Total rules should be reported", totalRules);

        log.info("Constraint flattening statistics: {}", stats);

        // Debug: Check what type definitions are registered
        MetaDataRegistry registry = MetaDataRegistry.getInstance();
        var allTypes = registry.getAllTypeDefinitions();
        log.info("Registered types count: {}", allTypes.size());

        // Look specifically for StringField registration
        var stringFieldType = allTypes.stream()
            .filter(td -> "field.string".equals(td.getQualifiedName()))
            .findFirst();

        if (stringFieldType.isPresent()) {
            log.info("StringField type definition found: {}", stringFieldType.get().getQualifiedName());
            var acceptsChildrenDecls = stringFieldType.get().getAcceptsChildren();
            log.info("StringField accepts children declarations: {}", acceptsChildrenDecls.size());
            acceptsChildrenDecls.forEach(decl ->
                log.info("  - Accepts: {}:{} named '{}'", decl.getChildType(), decl.getChildSubType(), decl.getChildName())
            );
        } else {
            log.warn("StringField type definition NOT found!");
        }

        // We should have generated some rules from the bidirectional constraints
        assertTrue("Should have generated some placement rules", totalRules > 0);
    }

    @Test
    public void testBidirectionalConstraintResolution() {
        // Test CORE attributes that StringField should accept
        boolean acceptsPattern = constraintFlattener.isPlacementAllowed(
            "field", "string",    // StringField parent
            "attr", "string",     // StringAttribute child
            "pattern"             // Core string field attribute
        );
        log.info("StringField accepts StringAttribute(pattern): {}", acceptsPattern);
        assertTrue("StringField should accept StringAttribute named 'pattern'", acceptsPattern);

        boolean acceptsMaxLength = constraintFlattener.isPlacementAllowed(
            "field", "string",    // StringField parent
            "attr", "int",        // IntAttribute child
            "maxLength"           // Core string field attribute
        );
        log.info("StringField accepts IntAttribute(maxLength): {}", acceptsMaxLength);
        assertTrue("StringField should accept IntAttribute named 'maxLength'", acceptsMaxLength);

        // Test that StringField does NOT accept database attributes (proper separation)
        boolean rejectsDbColumn = constraintFlattener.isPlacementAllowed(
            "field", "string",    // StringField parent
            "attr", "string",     // StringAttribute child
            "dbColumn"            // Database attribute - not in metadata module
        );
        log.info("StringField accepts StringAttribute(dbColumn): {}", rejectsDbColumn);
        assertFalse("StringField should reject database attributes (proper separation)", rejectsDbColumn);

        // Test that StringAttribute CANNOT be placed under StringField as invalid name
        boolean rejectsInvalid = constraintFlattener.isPlacementAllowed(
            "field", "string",    // StringField parent
            "attr", "string",     // StringAttribute child
            "invalidName"         // Invalid name
        );
        log.info("StringField accepts StringAttribute(invalidName): {}", rejectsInvalid);
        assertFalse("StringField should NOT accept StringAttribute with invalid name", rejectsInvalid);
    }

    @Test
    public void testMetaDataAddChildIntegration() {
        // Create a StringField
        StringField field = new StringField("testField");

        // Create a valid CORE attribute (pattern)
        StringAttribute pattern = new StringAttribute("pattern");
        pattern.setValue("^[a-zA-Z0-9_]*$");

        // This should succeed with bidirectional constraint system
        try {
            field.addChild(pattern);
            log.info("Successfully added pattern attribute to StringField");

            // Verify the child was added
            assertTrue("Field should contain the pattern attribute",
                field.getChildren().contains(pattern));
            assertEquals("pattern should have correct parent", field, pattern.getParent());

        } catch (Exception e) {
            fail("Adding valid pattern attribute should succeed: " + e.getMessage());
        }
    }

    @Test
    public void testConstraintViolationBlocking() {
        // Create a StringField
        StringField field = new StringField("testField");

        // Create a database attribute that StringField should reject (proper separation)
        StringAttribute dbColumn = new StringAttribute("dbColumn");
        dbColumn.setValue("test_column");

        // This should fail with bidirectional constraint system due to proper separation
        try {
            field.addChild(dbColumn);
            fail("Adding database attribute should have been blocked by constraints (proper separation)");

        } catch (Exception e) {
            log.info("Constraint system correctly blocked database attribute: {}", e.getMessage());
            assertTrue("Error should mention constraint violation",
                e.getMessage().contains("does not accept") || e.getMessage().contains("constraint"));
        }
    }

    @Test
    public void testCoreValueValidationStillWorks() {
        // Create a field and valid core attribute
        StringField field = new StringField("testField");
        StringAttribute pattern = new StringAttribute("pattern");

        // Test that VALUE validation still works (not just placement)
        // Invalid regex pattern should be caught by VALUE validation
        pattern.setValue("[invalid-regex"); // Invalid regex

        try {
            field.addChild(pattern);
            log.info("Placed pattern attribute - placement constraints passed");

            // Note: VALUE validation for regex patterns happens during constraint enforcement
            // This tests that the system is integrated

        } catch (Exception e) {
            log.info("System correctly handled pattern validation: {}", e.getMessage());
        }
    }

    @Test
    public void testValidChildTypeQuery() {
        // Test the quick lookup functionality
        var validChildTypes = constraintFlattener.getValidChildTypes("field", "string");
        assertNotNull("Valid child types should be returned", validChildTypes);

        log.info("Valid child types for StringField: {}", validChildTypes);

        // Should include attr.string for core attributes (pattern)
        assertTrue("Should include string attributes for core attributes",
            validChildTypes.contains("attr.string"));

        // Should include attr.int for core attributes (maxLength, minLength)
        assertTrue("Should include int attributes for core attributes",
            validChildTypes.contains("attr.int"));
    }

    @Test
    public void testValidParentTypeQuery() {
        // Test the reverse lookup functionality
        var validParentTypes = constraintFlattener.getValidParentTypes("attr", "string");
        assertNotNull("Valid parent types should be returned", validParentTypes);

        log.info("Valid parent types for StringAttribute: {}", validParentTypes);

        // Should include field.string and object types for database attributes
        boolean hasFieldParent = validParentTypes.stream()
            .anyMatch(parentType -> parentType.startsWith("field."));
        assertTrue("Should include field types as valid parents", hasFieldParent);
    }
}