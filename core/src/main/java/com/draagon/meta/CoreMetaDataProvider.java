package com.draagon.meta;

import com.draagon.meta.object.data.DataMetaObject;
import com.draagon.meta.object.value.ValueMetaObject;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataTypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core MetaData provider for core module types.
 * Registers types that are specific to the core module.
 */
public class CoreMetaDataProvider implements MetaDataTypeProvider {

    private static final Logger log = LoggerFactory.getLogger(CoreMetaDataProvider.class);

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        log.info("Registering core module types through provider");

        // Register core-specific object types
        DataMetaObject.registerTypes(registry);
        ValueMetaObject.registerTypes(registry);

        log.info("Completed core module types registration via provider");
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
        return "Core MetaData Provider - Registers core module specific types";
    }
}