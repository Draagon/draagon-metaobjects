package com.draagon.meta.object;

import com.draagon.meta.object.mapped.MappedMetaObject;
import com.draagon.meta.object.pojo.PojoMetaObject;
import com.draagon.meta.object.proxy.ProxyMetaObject;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataTypeProvider;

/**
 * Object Types MetaData provider with priority 5.
 * Registers base MetaObject and all concrete object types after core base types are available.
 */
public class ObjectTypesMetaDataProvider implements MetaDataTypeProvider {

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // FIRST: Register the base object type that all others inherit from
        MetaObject.registerTypes(registry);

        // THEN: Register concrete object types that inherit from object.base
        PojoMetaObject.registerTypes(registry);
        ProxyMetaObject.registerTypes(registry);
        MappedMetaObject.registerTypes(registry);
    }

    @Override
    public String getProviderId() {
        return "object-types";
    }

    @Override
    public String[] getDependencies() {
        // Depends on core base types to ensure metadata.base is available for object.base inheritance
        return new String[]{"core-base-types"};
    }

    @Override
    public String getDescription() {
        return "Object Types MetaData Provider - Registers MetaObject and concrete object implementations";
    }
}