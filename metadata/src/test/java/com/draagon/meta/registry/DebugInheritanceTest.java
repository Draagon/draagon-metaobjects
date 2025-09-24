package com.draagon.meta.registry;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.field.StringField;
import com.draagon.meta.MetaDataTypeId;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class DebugInheritanceTest {

    private static final Logger log = LoggerFactory.getLogger(DebugInheritanceTest.class);

    @Before
    public void setUp() {
        // Use SharedTestRegistry to ensure proper provider discovery timing
        SharedTestRegistry.getInstance();
        log.debug("DebugInheritanceTest setup with shared registry: {}", SharedTestRegistry.getStatus());
    }

    @Test
    public void debugFieldInheritance() {
        // Force class loading to ensure registration
        try {
            Class.forName("com.draagon.meta.field.MetaField");
            Class.forName("com.draagon.meta.field.StringField");
        } catch (ClassNotFoundException e) {
            fail("Required classes not found: " + e.getMessage());
        }

        MetaDataRegistry registry = MetaDataRegistry.getInstance();

        // Resolve any deferred inheritance after all static blocks have completed
        int resolvedCount = registry.resolveDeferredInheritance();
        log.info("Resolved {} deferred inheritance relationships", resolvedCount);

        // Check what's registered
        log.info("=== REGISTERED TYPES ===");
        for (MetaDataTypeId typeId : registry.getRegisteredTypes()) {
            log.info("Type: {}", typeId.toQualifiedName());
        }

        // Get base field definition
        MetaDataTypeId baseFieldId = new MetaDataTypeId("field", "base");
        TypeDefinition baseFieldDef = registry.getTypeDefinition(baseFieldId);
        assertNotNull("MetaField base should be registered", baseFieldDef);

        log.info("=== BASE FIELD (field.base) ===");
        log.info("Description: {}", baseFieldDef.getDescription());
        log.info("Direct child requirements: {}", baseFieldDef.getDirectChildRequirements().size());

        for (ChildRequirement req : baseFieldDef.getDirectChildRequirements()) {
            log.info("  - {} ({}:{})", req.getName(), req.getExpectedType(), req.getExpectedSubType());
        }

        log.info("Total child requirements: {}", baseFieldDef.getChildRequirements().size());
        for (ChildRequirement req : baseFieldDef.getChildRequirements()) {
            log.info("  - {} ({}:{})", req.getName(), req.getExpectedType(), req.getExpectedSubType());
        }

        // Get StringField definition
        MetaDataTypeId stringFieldId = new MetaDataTypeId("field", "string");
        TypeDefinition stringFieldDef = registry.getTypeDefinition(stringFieldId);
        assertNotNull("StringField should be registered", stringFieldDef);

        log.info("=== STRING FIELD (field.string) ===");
        log.info("Description: {}", stringFieldDef.getDescription());
        log.info("Has parent: {}", stringFieldDef.hasParent());
        if (stringFieldDef.hasParent()) {
            log.info("Parent: {}", stringFieldDef.getParentQualifiedName());
        }

        log.info("Direct child requirements: {}", stringFieldDef.getDirectChildRequirements().size());
        for (ChildRequirement req : stringFieldDef.getDirectChildRequirements()) {
            log.info("  - {} ({}:{})", req.getName(), req.getExpectedType(), req.getExpectedSubType());
        }

        log.info("Inherited child requirements: {}", stringFieldDef.getInheritedChildRequirements().size());
        for (ChildRequirement req : stringFieldDef.getInheritedChildRequirements().values()) {
            log.info("  - {} ({}:{})", req.getName(), req.getExpectedType(), req.getExpectedSubType());
        }

        log.info("Total child requirements: {}", stringFieldDef.getChildRequirements().size());
        for (ChildRequirement req : stringFieldDef.getChildRequirements()) {
            log.info("  - {} ({}:{})", req.getName(), req.getExpectedType(), req.getExpectedSubType());
        }

        // Test specific child acceptance
        log.info("=== CHILD ACCEPTANCE TESTS ===");

        boolean acceptsValidator = stringFieldDef.acceptsChild("validator", "required", "myValidator");
        log.info("StringField accepts validator.required: {}", acceptsValidator);

        boolean acceptsView = stringFieldDef.acceptsChild("view", "form", "myView");
        log.info("StringField accepts view.form: {}", acceptsView);

        // Check what the base field accepts
        boolean baseAcceptsValidator = baseFieldDef.acceptsChild("validator", "required", "myValidator");
        log.info("Base field accepts validator.required: {}", baseAcceptsValidator);

        // Check if there are wildcard requirements
        log.info("=== WILDCARD REQUIREMENTS CHECK ===");

        for (ChildRequirement req : baseFieldDef.getChildRequirements()) {
            if ("*".equals(req.getName())) {
                log.info("Base field has wildcard: {} ({}:{})", req.getName(), req.getExpectedType(), req.getExpectedSubType());
                boolean matches = req.matches("validator", "required", "myValidator");
                log.info("  - Matches validator.required: {}", matches);
            }
        }

        for (ChildRequirement req : stringFieldDef.getInheritedChildRequirements().values()) {
            if ("*".equals(req.getName())) {
                log.info("String field inherited wildcard: {} ({}:{})", req.getName(), req.getExpectedType(), req.getExpectedSubType());
                boolean matches = req.matches("validator", "required", "myValidator");
                log.info("  - Matches validator.required: {}", matches);
            }
        }
    }
}