package com.draagon.meta.loader.simple;

import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.SharedTestRegistry;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Test to force SimpleLoader registration and debug issues
 */
public class SimpleLoaderRegistrationTest {

    private static final Logger log = LoggerFactory.getLogger(SimpleLoaderRegistrationTest.class);

    @Before
    public void setUp() {
        // Use SharedTestRegistry to ensure proper provider discovery timing
        SharedTestRegistry.getInstance();
        log.debug("SimpleLoaderRegistrationTest setup with shared registry: {}", SharedTestRegistry.getStatus());
    }

    @Test
    public void testSimpleLoaderRegistration() {
        log.info("Testing SimpleLoader registration");

        // Force class loading of SimpleLoader (this should trigger static block)
        log.info("About to reference SimpleLoader class...");
        Class<?> clazz = SimpleLoader.class;
        log.info("SimpleLoader class loaded: " + clazz.getName());

        // Try to force static block execution by creating an instance
        log.info("Attempting to create SimpleLoader instance to force static block execution...");
        try {
            SimpleLoader testLoader = new SimpleLoader("test");
            log.info("SimpleLoader instance created successfully: " + testLoader.getName());
        } catch (Exception e) {
            log.warn("Failed to create SimpleLoader instance: " + e.getMessage(), e);
        }

        // Check if loader.simple is registered
        MetaDataRegistry registry = MetaDataRegistry.getInstance();
        var allTypes = registry.getAllTypeDefinitions();

        log.info("All registered types count: " + allTypes.size());
        allTypes.forEach(td -> log.info("Type: " + td.getQualifiedName()));

        boolean hasLoaderSimple = allTypes.stream()
            .anyMatch(td -> td.getQualifiedName().contains("loader.simple"));

        log.info("Has loader.simple: " + hasLoaderSimple);

        if (!hasLoaderSimple) {
            log.error("SimpleLoader not registered! Checking for loader types...");
            allTypes.stream()
                .filter(td -> td.getQualifiedName().contains("loader"))
                .forEach(td -> log.info("Found loader type: " + td.getQualifiedName()));
        }

        assertTrue("SimpleLoader should be registered as loader.simple", hasLoaderSimple);
    }
}