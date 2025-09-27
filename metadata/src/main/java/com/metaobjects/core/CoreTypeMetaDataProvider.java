package com.metaobjects.core;

import com.metaobjects.MetaData;
import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.registry.MetaDataTypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core Type MetaData provider that registers the fundamental base types.
 *
 * <p>This provider registers the most fundamental base type (metadata.base) that
 * all other metadata types inherit from. It must be loaded before any other providers
 * that define types inheriting from metadata.base.</p>
 *
 * <strong>Base Types Registered:</strong>:
 * <ul>
 * <li><strong>metadata.base:</strong> Root metadata type that all others inherit from</li>
 * </ul>
 *
 * <strong>Dependencies:</strong>:
 * <p>No dependencies - This provider registers the foundational base type that
 * all other types depend on.</p>
 *
 * @since 6.0.0
 */
public class CoreTypeMetaDataProvider implements MetaDataTypeProvider {

    private static final Logger log = LoggerFactory.getLogger(CoreTypeMetaDataProvider.class);

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // Register the fundamental metadata.base type that all others inherit from
        MetaData.registerTypes(registry);

        log.info("Core base types registered via provider");
    }

    @Override
    public String getProviderId() {
        return "core-base-types";
    }

    @Override
    public String[] getDependencies() {
        // No dependencies - this registers the root metadata.base type
        return new String[0];
    }

    @Override
    public String getDescription() {
        return "Core Type MetaData Provider - Registers fundamental metadata.base type";
    }
}