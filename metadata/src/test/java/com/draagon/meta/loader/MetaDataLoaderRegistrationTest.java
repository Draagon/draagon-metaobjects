package com.draagon.meta.loader;

import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.SharedTestRegistry;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Test to force MetaDataLoader registration and debug base type issues
 */
public class MetaDataLoaderRegistrationTest {

    private static final Logger log = LoggerFactory.getLogger(MetaDataLoaderRegistrationTest.class);

    @Before
    public void setUp() {
        // Use SharedTestRegistry to ensure proper provider discovery timing
        SharedTestRegistry.getInstance();
        log.debug("MetaDataLoaderRegistrationTest setup with shared registry: {}", SharedTestRegistry.getStatus());
    }

    @Test
    public void testMetaDataLoaderRegistration() {
        log.info("Testing MetaDataLoader registration");

        // Force class loading of MetaDataLoader (this should trigger static block)
        log.info("About to reference MetaDataLoader class...");
        Class<?> clazz = MetaDataLoader.class;
        log.info("MetaDataLoader class loaded: " + clazz.getName());

        // Check if metadata.base is registered
        MetaDataRegistry registry = MetaDataRegistry.getInstance();
        var allTypes = registry.getAllTypeDefinitions();

        log.info("All registered types count: " + allTypes.size());
        allTypes.forEach(td -> log.info("Type: " + td.getQualifiedName()));

        boolean hasMetadataBase = allTypes.stream()
            .anyMatch(td -> "metadata.base".equals(td.getQualifiedName()));

        boolean hasLoaderManual = allTypes.stream()
            .anyMatch(td -> "loader.manual".equals(td.getQualifiedName()));

        log.info("Has metadata.base: " + hasMetadataBase);
        log.info("Has loader.manual: " + hasLoaderManual);

        if (!hasMetadataBase) {
            log.error("metadata.base not registered! This is the root type that all other types inherit from.");
        }

        assertTrue("MetaDataLoader should be registered as metadata.base", hasMetadataBase);
        assertTrue("MetaDataLoader should be registered as loader.manual", hasLoaderManual);
    }
}