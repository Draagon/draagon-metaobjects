package com.draagon.meta.registry;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * Diagnostic test to understand provider loading issues during full test suite.
 */
public class ProviderLoadingDiagnosticTest {

    private static final Logger log = LoggerFactory.getLogger(ProviderLoadingDiagnosticTest.class);

    @Before
    public void setUp() {
        // Use SharedTestRegistry to ensure proper provider discovery timing
        SharedTestRegistry.getInstance();
        log.debug("ProviderLoadingDiagnosticTest setup with shared registry: {}", SharedTestRegistry.getStatus());
    }

    @Test
    public void testProviderLoadingDiagnostic() {
        MetaDataRegistry registry = MetaDataRegistry.getInstance();

        // Check how many providers and types are loaded
        Set<String> registeredTypes = registry.getRegisteredTypeNames();
        log.info("Registry has {} types registered", registeredTypes.size());

        // Check specifically for object types
        boolean hasObjectBase = registry.getTypeDefinition("object", "base") != null;
        boolean hasObjectMap = registry.getTypeDefinition("object", "map") != null;
        boolean hasObjectPojo = registry.getTypeDefinition("object", "pojo") != null;
        boolean hasObjectProxy = registry.getTypeDefinition("object", "proxy") != null;

        log.info("Object types available: base={}, map={}, pojo={}, proxy={}",
                hasObjectBase, hasObjectMap, hasObjectPojo, hasObjectProxy);

        // Check loader.simple configuration
        boolean hasLoaderSimple = registry.getTypeDefinition("loader", "simple") != null;
        log.info("Loader simple available: {}", hasLoaderSimple);

        if (hasLoaderSimple) {
            // Check what children loader.simple accepts
            String supportedChildren = registry.getSupportedChildrenDescription("loader", "simple");
            log.info("Loader.simple supported children: {}", supportedChildren);

            // Test specific acceptance
            boolean acceptsObjectMap = registry.acceptsChild("loader", "simple", "object", "map", null);
            boolean acceptsObjectProxy = registry.acceptsChild("loader", "simple", "object", "proxy", null);
            boolean acceptsObjectPojo = registry.acceptsChild("loader", "simple", "object", "pojo", null);

            log.info("Loader.simple accepts: object.map={}, object.proxy={}, object.pojo={}",
                    acceptsObjectMap, acceptsObjectProxy, acceptsObjectPojo);
        }

        // List all registered types
        log.info("All registered types: {}", registeredTypes);

        // This test doesn't fail - just provides diagnostic information
        assertTrue("Registry should have types", registeredTypes.size() > 0);
    }
}