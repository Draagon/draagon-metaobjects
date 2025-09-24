package com.draagon.meta.constraint;

import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.SharedTestRegistry;
import com.draagon.meta.registry.TypeDefinition;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectConstraintTest {

    private static final Logger log = LoggerFactory.getLogger(DirectConstraintTest.class);

    @Before
    public void setUp() {
        // Use SharedTestRegistry to ensure proper provider discovery timing
        SharedTestRegistry.getInstance();
        log.debug("DirectConstraintTest setup with shared registry: {}", SharedTestRegistry.getStatus());
    }

    @Test
    public void testLoaderSimpleFieldLongConstraint() {
        log.info("=== DIRECT CONSTRAINT TEST: loader.simple -> field.long ===");

        // Force class loading
        try {
            Class.forName("com.draagon.meta.loader.MetaDataLoader");
            Class.forName("com.draagon.meta.loader.simple.SimpleLoader");
            Class.forName("com.draagon.meta.field.LongField");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        MetaDataRegistry registry = MetaDataRegistry.getInstance();

        // Resolve any deferred inheritance
        int resolvedCount = registry.resolveDeferredInheritance();
        log.info("Resolved {} deferred inheritance relationships", resolvedCount);

        // Get type definitions
        TypeDefinition loaderSimple = registry.getTypeDefinition("loader", "simple");
        TypeDefinition fieldLong = registry.getTypeDefinition("field", "long");

        if (loaderSimple == null) {
            log.error("loader.simple NOT FOUND!");
            return;
        }

        if (fieldLong == null) {
            log.error("field.long NOT FOUND!");
            return;
        }

        log.info("=== LOADER.SIMPLE TYPE DEFINITION ===");
        log.info("Type: {}", loaderSimple.getQualifiedName());
        log.info("Parent: {}", loaderSimple.getParentQualifiedName());
        log.info("Direct accepts children: {}", loaderSimple.getDirectAcceptsChildren().size());
        log.info("Inherited accepts children: {}", loaderSimple.getInheritedAcceptsChildren().size());
        log.info("ALL accepts children: {}", loaderSimple.getAcceptsChildren().size());

        loaderSimple.getAcceptsChildren().forEach(child ->
            log.info("  Accepts child: {}:{} named '{}'",
                child.getChildType(), child.getChildSubType(), child.getChildName())
        );

        log.info("=== FIELD.LONG TYPE DEFINITION ===");
        log.info("Type: {}", fieldLong.getQualifiedName());
        log.info("Parent: {}", fieldLong.getParentQualifiedName());
        log.info("Direct accepts parents: {}", fieldLong.getDirectAcceptsParents().size());
        log.info("Inherited accepts parents: {}", fieldLong.getInheritedAcceptsParents().size());
        log.info("ALL accepts parents: {}", fieldLong.getAcceptsParents().size());

        fieldLong.getAcceptsParents().forEach(parent ->
            log.info("  Accepts parent: {}:{} when named '{}'",
                parent.getParentType(), parent.getParentSubType(), parent.getExpectedChildName())
        );

        // Test the actual constraint methods
        log.info("=== CONSTRAINT EVALUATION ===");
        boolean loaderAcceptsField = loaderSimple.acceptsChild("field", "long", "testField");
        log.info("loader.simple.acceptsChild('field', 'long', 'testField') = {}", loaderAcceptsField);

        boolean fieldAcceptsLoader = fieldLong.acceptsParent("loader", "simple", "testField");
        log.info("field.long.acceptsParent('loader', 'simple', 'testField') = {}", fieldAcceptsLoader);

        // Test constraint flattener if initialized
        try {
            ConstraintEnforcer enforcer = ConstraintEnforcer.getInstance();
            ConstraintFlattener flattener = enforcer.getConstraintFlattener();

            boolean canPlace = flattener.isPlacementAllowed("loader", "simple", "field", "long", "testField");
            log.info("ConstraintFlattener.isPlacementAllowed('loader', 'simple', 'field', 'long', 'testField') = {}", canPlace);

            log.info("Valid child types for loader.simple: {}", flattener.getValidChildTypes("loader", "simple"));
        } catch (Exception e) {
            log.error("Error testing constraint flattener", e);
        }
    }
}