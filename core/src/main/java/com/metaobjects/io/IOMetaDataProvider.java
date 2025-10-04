package com.metaobjects.io;

import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.registry.MetaDataTypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IO MetaData provider for core module types.
 * Registers types that are specific to the core module.
 * Note: DataObject and ValueObject types are now registered in the dynamic module.
 */
public class IOMetaDataProvider implements MetaDataTypeProvider {

    private static final Logger log = LoggerFactory.getLogger(IOMetaDataProvider.class);

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        log.debug("Registering core module types through provider");

        // Core module currently has no specific object types to register
        // DataObject and ValueObject types are now handled by the dynamic module

        log.debug("Completed core module types registration via provider");
    }

    @Override
    public String getProviderId() {
        return "core-types";
    }

    @Override
    public String[] getDependencies() {
        // Depends on object-types for object.base inheritance
        return new String[]{"object-types"};
    }

    @Override
    public String getDescription() {
        return "IO MetaData Provider - Registers core module specific types";
    }
}