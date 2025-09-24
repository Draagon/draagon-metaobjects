package com.draagon.meta.constraint;

import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.TypeDefinition;
import com.draagon.meta.registry.AcceptsChildrenDeclaration;
import com.draagon.meta.registry.SharedTestRegistry;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Debug test to investigate loader.simple constraint issues
 */
public class LoaderConstraintDebugTest {

    private static final Logger log = LoggerFactory.getLogger(LoaderConstraintDebugTest.class);

    @Before
    public void setUp() {
        // Use SharedTestRegistry to ensure proper provider discovery timing
        SharedTestRegistry.getInstance();
        log.debug("LoaderConstraintDebugTest setup with shared registry: {}", SharedTestRegistry.getStatus());
    }

    @Test
    public void debugLoaderSimpleConstraints() {
        log.info("=== LOADER.SIMPLE CONSTRAINT DEBUG TEST ===");

        MetaDataRegistry registry = MetaDataRegistry.getInstance();

        // Check if object types are registered
        log.info("=== CHECKING OBJECT TYPE REGISTRATIONS ===");
        log.info("object.base registered: {}", registry.getTypeDefinition("object", "base") != null);
        log.info("object.map registered: {}", registry.getTypeDefinition("object", "map") != null);
        log.info("object.pojo registered: {}", registry.getTypeDefinition("object", "pojo") != null);
        log.info("object.proxy registered: {}", registry.getTypeDefinition("object", "proxy") != null);

        // Check loader.simple type definition
        log.info("=== CHECKING LOADER.SIMPLE TYPE DEFINITION ===");
        TypeDefinition loaderSimple = registry.getTypeDefinition("loader", "simple");
        if (loaderSimple != null) {
            log.info("loader.simple found with {} acceptsChildren declarations", loaderSimple.getAcceptsChildren().size());

            for (AcceptsChildrenDeclaration decl : loaderSimple.getAcceptsChildren()) {
                log.info("  Accepts: {}:{}:{}", decl.getChildType(), decl.getChildSubType(), decl.getChildName());
            }

            // Test specific object types
            boolean acceptsObjectBase = loaderSimple.acceptsChild("object", "base", "testObject");
            boolean acceptsObjectMap = loaderSimple.acceptsChild("object", "map", "testObject");
            boolean acceptsObjectPojo = loaderSimple.acceptsChild("object", "pojo", "testObject");
            boolean acceptsObjectProxy = loaderSimple.acceptsChild("object", "proxy", "testObject");

            log.info("loader.simple.acceptsChild results:");
            log.info("  object.base: {}", acceptsObjectBase);
            log.info("  object.map: {}", acceptsObjectMap);
            log.info("  object.pojo: {}", acceptsObjectPojo);
            log.info("  object.proxy: {}", acceptsObjectProxy);
        } else {
            log.error("loader.simple NOT FOUND!");
        }

        // Test constraint flattener
        log.info("=== TESTING CONSTRAINT FLATTENER ===");
        ConstraintEnforcer enforcer = ConstraintEnforcer.getInstance();
        ConstraintFlattener flattener = enforcer.getConstraintFlattener();

        boolean allowsObjectBase = flattener.isPlacementAllowed("loader", "simple", "object", "base", "testObject");
        boolean allowsObjectMap = flattener.isPlacementAllowed("loader", "simple", "object", "map", "testObject");
        boolean allowsObjectPojo = flattener.isPlacementAllowed("loader", "simple", "object", "pojo", "testObject");
        boolean allowsObjectProxy = flattener.isPlacementAllowed("loader", "simple", "object", "proxy", "testObject");

        log.info("ConstraintFlattener.isPlacementAllowed results:");
        log.info("  loader.simple -> object.base: {}", allowsObjectBase);
        log.info("  loader.simple -> object.map: {}", allowsObjectMap);
        log.info("  loader.simple -> object.pojo: {}", allowsObjectPojo);
        log.info("  loader.simple -> object.proxy: {}", allowsObjectProxy);

        // Test with actual package-qualified names from failing tests
        log.info("=== TESTING WITH PACKAGE-QUALIFIED NAMES ===");
        boolean allowsPackagedProxy = flattener.isPlacementAllowed("loader", "simple", "object", "proxy", "simple::fruitbasket::Basket");
        boolean allowsPackagedMap = flattener.isPlacementAllowed("loader", "simple", "object", "map", "test::TestObject");
        boolean allowsGarage = flattener.isPlacementAllowed("loader", "simple", "object", "pojo", "acme::garage::Garage");

        log.info("Package-qualified name results:");
        log.info("  loader.simple -> object.proxy ['simple::fruitbasket::Basket']: {}", allowsPackagedProxy);
        log.info("  loader.simple -> object.map ['test::TestObject']: {}", allowsPackagedMap);
        log.info("  loader.simple -> object.pojo ['acme::garage::Garage']: {}", allowsGarage);
    }
}