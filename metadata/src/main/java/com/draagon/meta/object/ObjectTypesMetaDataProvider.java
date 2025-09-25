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
        // Register base object type first
        MetaObject.registerTypes(registry);

        // Register concrete object types
        PojoMetaObject.registerTypes(registry);
        ProxyMetaObject.registerTypes(registry);
        MappedMetaObject.registerTypes(registry);
    }

    @Override
    public int getPriority() {
        // Priority 5: After core metadata (0), before fields (10)
        return 5;
    }

    @Override
    public String getDescription() {
        return "Object Types MetaData Provider - Registers MetaObject and concrete object implementations";
    }
}