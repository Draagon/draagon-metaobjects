package com.draagon.meta.core;

import com.draagon.meta.MetaData;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataTypeProvider;

/**
 * Core Type MetaData provider that registers the fundamental base types.
 *
 * <p>This provider registers the most fundamental base type (metadata.base) that
 * all other metadata types inherit from. It must be loaded before any other providers
 * that define types inheriting from metadata.base.</p>
 *
 * <h3>Base Types Registered:</h3>
 * <ul>
 * <li><strong>metadata.base:</strong> Root metadata type that all others inherit from</li>
 * </ul>
 *
 * <h3>Dependencies:</h3>
 * <p>No dependencies - This provider registers the foundational base type that
 * all other types depend on.</p>
 *
 * @since 6.0.0
 */
public class CoreTypeMetaDataProvider implements MetaDataTypeProvider {

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // Register the fundamental metadata.base type that all others inherit from
        MetaData.registerTypes(registry);

        System.out.println("Info: Core base types registered via provider");
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