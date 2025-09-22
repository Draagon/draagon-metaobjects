package com.draagon.meta.constraint;

import com.draagon.meta.field.StringField;
import com.draagon.meta.object.pojo.PojoMetaObject;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.loader.MetaDataLoader;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.*;

import java.util.ArrayList;

/**
 * Basic test for RelationshipConstraint system to verify core functionality.
 * This test focuses on demonstrating that the RelationshipConstraint system
 * compiles and executes without errors.
 */
public class RelationshipConstraintBasicTest {

    private static final Logger log = LoggerFactory.getLogger(RelationshipConstraintBasicTest.class);

    private ConstraintRegistry constraintRegistry;
    private RelationshipConstraintEnforcer enforcer;
    private SimpleLoader loader;

    @Before
    public void setUp() {
        log.info("Setting up basic relationship constraint test");

        // Initialize constraint registry with relationship constraints
        constraintRegistry = ConstraintRegistry.getInstance();

        // Register advanced relationship constraints
        AdvancedRelationshipConstraintProvider provider = new AdvancedRelationshipConstraintProvider();
        provider.registerConstraints(constraintRegistry);

        // Create relationship constraint enforcer
        enforcer = new RelationshipConstraintEnforcer(constraintRegistry);

        // Create test metadata loader without source data (for testing purposes)
        loader = new SimpleLoader("basic-relationship-test");

        // Set empty source URIs and initialize the loader
        loader.setSourceURIs(new ArrayList<>());

        try {
            loader.init();
        } catch (Exception e) {
            // Initialization with empty sources should work, but if it fails we'll log it
            log.debug("Loader initialization completed (empty sources): {}", e.getMessage());
        }

        log.info("Basic relationship constraint test setup complete");
    }

    @Test
    public void testConstraintSystemInitialization() {
        log.info("Testing constraint system initialization");

        // Verify that the constraint system is properly initialized
        assertTrue("RelationshipConstraintEnforcer should be initialized", enforcer != null);
        assertTrue("Should have relationship constraints available",
                   enforcer.isRelationshipValidationEnabled());
        assertTrue("Should have constraint count > 0",
                   enforcer.getRelationshipConstraintCount() > 0);

        log.info("✅ Constraint system initialization test passed");
        log.info("Available relationship constraints: {}", enforcer.getRelationshipConstraintCount());
    }

    @Test
    public void testBasicValidationExecution() {
        log.info("Testing basic validation execution");

        // Create simple metadata structure
        PojoMetaObject user = new PojoMetaObject("User");
        StringField nameField = new StringField("name");
        user.addChild(nameField);
        loader.addChild(user);

        // Execute relationship validation - should not throw exceptions
        List<ConstraintViolation> violations = enforcer.validateAllRelationships(loader);

        assertNotNull("Violations list should not be null", violations);
        log.info("Validation executed successfully with {} violations", violations.size());

        // Log any violations for analysis
        for (ConstraintViolation violation : violations) {
            log.debug("Violation: {} - {}", violation.getConstraintId(), violation.getViolationMessage());
        }

        log.info("✅ Basic validation execution test passed");
    }

    @Test
    public void testValidationStatistics() {
        log.info("Testing validation statistics");

        // Create some test metadata
        PojoMetaObject user = new PojoMetaObject("User");
        StringField nameField = new StringField("name");
        user.addChild(nameField);
        loader.addChild(user);

        // Get validation statistics
        RelationshipConstraintEnforcer.RelationshipValidationStats stats =
            enforcer.getValidationStats(loader);

        assertNotNull("Validation stats should not be null", stats);
        assertTrue("Should have relationship constraints", stats.getTotalConstraints() > 0);
        assertTrue("Should have metadata to validate", stats.getTotalMetaData() > 0);

        log.info("✅ Validation statistics: {}", stats);
    }

    @Test
    public void testConstraintProviderRegistration() {
        log.info("Testing constraint provider registration");

        // Verify that the AdvancedRelationshipConstraintProvider was registered successfully
        assertTrue("Should have relationship constraints after provider registration",
                   enforcer.getRelationshipConstraintCount() > 0);

        // Test provider properties
        AdvancedRelationshipConstraintProvider provider = new AdvancedRelationshipConstraintProvider();
        assertEquals("Provider should have high priority", 800, provider.getPriority());
        assertNotNull("Provider should have description", provider.getDescription());
        assertTrue("Description should mention relationship constraints",
                   provider.getDescription().toLowerCase().contains("relationship"));

        log.info("✅ Constraint provider registration test passed");
        log.info("Provider description: {}", provider.getDescription());
    }
}