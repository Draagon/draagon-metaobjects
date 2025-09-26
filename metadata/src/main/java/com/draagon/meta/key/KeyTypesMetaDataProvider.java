package com.draagon.meta.key;

import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataTypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Key Types MetaData provider that registers all concrete key type implementations.
 *
 * <p>This provider registers all the concrete key types that extend key.base.
 * It calls the registerTypes() methods on each concrete key class to ensure proper registration.</p>
 *
 * <h3>Key Types Registered:</h3>
 * <ul>
 * <li><strong>key.primary:</strong> Primary key definitions</li>
 * <li><strong>key.foreign:</strong> Foreign key relationships</li>
 * <li><strong>key.secondary:</strong> Secondary keys and indexes</li>
 * </ul>
 *
 * <h3>Priority:</h3>
 * <p>Priority 25 - Runs after validator types (20) but before object types (30).
 * This ensures key.base is available before concrete key types are registered.</p>
 *
 * @since 6.0.0
 */
public class KeyTypesMetaDataProvider implements MetaDataTypeProvider {

    private static final Logger log = LoggerFactory.getLogger(KeyTypesMetaDataProvider.class);

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // FIRST: Register the base key type that all others inherit from
        MetaKey.registerTypes(registry);

        // THEN: Register concrete key types that inherit from key.base
        PrimaryKey.registerTypes(registry);
        ForeignKey.registerTypes(registry);
        SecondaryKey.registerTypes(registry);

        log.info("Key types registered via provider");
    }

    @Override
    public String getProviderId() {
        return "key-types";
    }

    @Override
    public String[] getDependencies() {
        // Depends on core base types to ensure metadata.base is available for key.base inheritance
        return new String[]{"core-base-types"};
    }

    @Override
    public String getDescription() {
        return "Key Types MetaData Provider - Registers all concrete key type implementations";
    }
}