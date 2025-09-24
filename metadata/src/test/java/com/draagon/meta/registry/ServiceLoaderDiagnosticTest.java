package com.draagon.meta.registry;

import com.draagon.meta.registry.SharedTestRegistry;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import static org.junit.Assert.*;

/**
 * Test to diagnose ServiceLoader discovery issues.
 */
public class ServiceLoaderDiagnosticTest {

    private static final Logger log = LoggerFactory.getLogger(ServiceLoaderDiagnosticTest.class);

    @Before
    public void setUp() {
        // Use SharedTestRegistry to ensure proper provider discovery timing
        SharedTestRegistry.getInstance();
        log.debug("ServiceLoaderDiagnosticTest setup with shared registry: {}", SharedTestRegistry.getStatus());
    }

    @Test
    public void testDirectServiceLoaderDiscovery() {
        log.info("=== Testing Direct ServiceLoader Discovery ===");

        // Test direct ServiceLoader discovery
        ServiceLoader<MetaDataTypeProvider> serviceLoader = ServiceLoader.load(MetaDataTypeProvider.class);
        List<MetaDataTypeProvider> providers = new ArrayList<>();

        for (MetaDataTypeProvider provider : serviceLoader) {
            providers.add(provider);
            log.info("Discovered provider: {} ({})", provider.getProviderName(), provider.getClass().getSimpleName());
        }

        log.info("Total providers discovered via ServiceLoader: {}", providers.size());

        // Check specifically for ObjectTypeProvider
        boolean hasObjectTypeProvider = providers.stream()
                .anyMatch(p -> "object-types".equals(p.getProviderName()));

        log.info("ObjectTypeProvider found: {}", hasObjectTypeProvider);

        // List all provider names
        List<String> providerNames = providers.stream()
                .map(MetaDataTypeProvider::getProviderName)
                .toList();
        log.info("All provider names: {}", providerNames);

        // Verify we have the expected providers
        assertTrue("Should have at least 8 providers", providers.size() >= 8);
        assertTrue("Should have ObjectTypeProvider", hasObjectTypeProvider);
    }

    @Test
    public void testMetaDataProviderDiscovery() {
        log.info("=== Testing MetaDataProviderDiscovery ===");

        // Test the discovery system directly
        MetaDataProviderDiscovery.StandardProviderManager manager = new MetaDataProviderDiscovery.StandardProviderManager();
        List<MetaDataTypeProvider> providers = manager.discoverProviders();

        log.info("Total providers discovered via MetaDataProviderDiscovery: {}", providers.size());

        for (MetaDataTypeProvider provider : providers) {
            log.info("Provider: {} ({})", provider.getProviderName(), provider.getClass().getSimpleName());
        }

        // Check specifically for ObjectTypeProvider
        boolean hasObjectTypeProvider = providers.stream()
                .anyMatch(p -> "object-types".equals(p.getProviderName()));

        log.info("ObjectTypeProvider found via discovery: {}", hasObjectTypeProvider);

        assertTrue("Should have ObjectTypeProvider via discovery", hasObjectTypeProvider);
    }

    @Test
    public void testRegistryStateAfterOtherTests() {
        log.info("=== Testing Registry State After Other Tests ===");

        MetaDataRegistry registry = MetaDataRegistry.getInstance();

        // Check if object types are registered
        boolean hasObjectBase = registry.getTypeDefinition("object", "base") != null;
        boolean hasObjectMap = registry.getTypeDefinition("object", "map") != null;
        boolean hasObjectPojo = registry.getTypeDefinition("object", "pojo") != null;
        boolean hasObjectProxy = registry.getTypeDefinition("object", "proxy") != null;

        log.info("Registry object types: base={}, map={}, pojo={}, proxy={}",
                hasObjectBase, hasObjectMap, hasObjectPojo, hasObjectProxy);

        // Check total types
        log.info("Total types in registry: {}", registry.getRegisteredTypeNames().size());

        // Check if loader.simple accepts object types
        boolean acceptsObjectMap = registry.acceptsChild("loader", "simple", "object", "map", null);
        log.info("loader.simple accepts object.map: {}", acceptsObjectMap);

        // This test doesn't fail - just provides diagnostics
        assertTrue("Registry should have types", registry.getRegisteredTypeNames().size() > 0);
    }
}