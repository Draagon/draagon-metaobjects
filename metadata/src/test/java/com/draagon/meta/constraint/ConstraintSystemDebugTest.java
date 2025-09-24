package com.draagon.meta.constraint;

import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.field.StringField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.TypeDefinition;
import com.draagon.meta.registry.SharedTestRegistry;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Debug test to understand why bidirectional constraints aren't working.
 */
public class ConstraintSystemDebugTest {

    private static final Logger log = LoggerFactory.getLogger(ConstraintSystemDebugTest.class);

    @Before
    public void setUp() {
        // Use SharedTestRegistry to ensure proper provider discovery timing
        SharedTestRegistry.getInstance();
        log.debug("ConstraintSystemDebugTest setup with shared registry: {}", SharedTestRegistry.getStatus());
    }

    @Test
    public void debugTypeRegistrations() {
        // Force class loading to trigger static blocks
        try {
            Class.forName(MetaDataLoader.class.getName());
            Class.forName(StringField.class.getName());
            Class.forName(StringAttribute.class.getName());
        } catch (ClassNotFoundException e) {
            log.error("Failed to load classes", e);
        }

        // Check what types are registered
        MetaDataRegistry registry = MetaDataRegistry.getInstance();
        var registeredTypes = registry.getRegisteredTypes();

        log.info("Registered types: {}", registeredTypes);

        // Check specific registrations
        TypeDefinition metadataBase = registry.getTypeDefinition("metadata", "base");
        log.info("metadata.base definition: {}", metadataBase);

        TypeDefinition fieldString = registry.getTypeDefinition("field", "string");
        log.info("field.string definition: {}", fieldString);

        TypeDefinition attrString = registry.getTypeDefinition("attr", "string");
        log.info("attr.string definition: {}", attrString);

        if (fieldString != null) {
            log.info("field.string acceptsChildren: {}", fieldString.getAcceptsChildren());
        }

        if (attrString != null) {
            log.info("attr.string acceptsParents: {}", attrString.getAcceptsParents());
        }
    }

    @Test
    public void debugConstraintFlattening() {
        // Get constraint flattener
        ConstraintEnforcer enforcer = ConstraintEnforcer.getInstance();
        ConstraintFlattener flattener = enforcer.getConstraintFlattener();

        // Get flattening statistics
        var stats = flattener.getStatistics();
        log.info("Constraint flattening statistics: {}", stats);

        // Check specific placement query
        boolean allowed = flattener.isPlacementAllowed(
            "field", "string",
            "attr", "string",
            "dbColumn"
        );
        log.info("field.string -> attr.string[dbColumn] allowed: {}", allowed);

        // Get all rules to see what was generated
        var allRules = flattener.getAllRules();
        log.info("All generated rules ({}): ", allRules.size());
        allRules.forEach((key, rule) -> log.info("  {}: {}", key, rule));
    }

    @Test
    public void debugStringAttributeParentAcceptance() {
        // Check if StringAttribute has the expected parent acceptance declarations
        MetaDataRegistry registry = MetaDataRegistry.getInstance();
        TypeDefinition attrString = registry.getTypeDefinition("attr", "string");

        if (attrString != null) {
            log.info("StringAttribute acceptsParents declarations:");
            attrString.getAcceptsParents().forEach(decl ->
                log.info("  {}", decl));

            // Check if it accepts field.string as parent when named dbColumn
            boolean acceptsFieldStringAsDbColumn = attrString.getAcceptsParents().stream()
                .anyMatch(decl ->
                    "field".equals(decl.getParentType()) &&
                    "*".equals(decl.getParentSubType()) &&
                    "dbColumn".equals(decl.getExpectedChildName()));

            log.info("StringAttribute accepts field.* as parent when named dbColumn: {}",
                acceptsFieldStringAsDbColumn);
        } else {
            log.warn("attr.string TypeDefinition not found!");
        }
    }
}