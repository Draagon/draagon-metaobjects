package com.metaobjects.identity;

import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.registry.MetaDataTypeProvider;

/**
 * Identity Types MetaData provider.
 * Registers distinct identity types (PrimaryIdentity, SecondaryIdentity) for object identification.
 * Depends on core-types for metadata.base inheritance.
 */
public class IdentityTypesMetaDataProvider implements MetaDataTypeProvider {

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // Register distinct identity types
        PrimaryIdentity.registerTypes(registry);
        SecondaryIdentity.registerTypes(registry);
    }

    @Override
    public String getProviderId() {
        return "identity-types";
    }

    @Override
    public String[] getDependencies() {
        return new String[]{"core-types"}; // Need metadata.base for inheritance
    }

    @Override
    public String getDescription() {
        return "Identity Types (primary, secondary) for object identification";
    }
}