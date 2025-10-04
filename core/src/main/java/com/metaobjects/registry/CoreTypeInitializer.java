package com.metaobjects.registry;

import com.metaobjects.loader.file.FileMetaDataLoader;
import com.metaobjects.attr.PropertiesAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core type initializer that ensures all core types are loaded and registered.
 * This class explicitly loads core classes to trigger their static registration blocks.
 * Note: DataObject and ValueObject types are now handled by the dynamic module.
 */
public class CoreTypeInitializer {

    private static final Logger log = LoggerFactory.getLogger(CoreTypeInitializer.class);
    private static boolean initialized = false;

    /**
     * Initialize all core types by loading their classes to trigger static blocks.
     */
    public static synchronized void initializeCoreTypes() {
        if (initialized) {
            return;
        }

        try {
            log.debug("Loading core MetaData types...");

            // Load FileMetaDataLoader to trigger its static block
            Class.forName(FileMetaDataLoader.class.getName());
            log.debug("Loaded FileMetaDataLoader type");

            // Load PropertiesAttribute to trigger its static block
            Class.forName(PropertiesAttribute.class.getName());
            log.debug("Loaded PropertiesAttribute type");

            // Note: DataObject and ValueObject initialization is now handled by the dynamic module

            initialized = true;
            log.debug("Successfully initialized core MetaData types");

        } catch (ClassNotFoundException e) {
            log.error("Failed to load core MetaData types", e);
            throw new RuntimeException("Failed to initialize core MetaData types", e);
        }
    }

    // Static block to auto-initialize when this class is loaded
    static {
        initializeCoreTypes();
    }
}