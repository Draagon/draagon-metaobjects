package com.metaobjects.object.managed;

import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.registry.MetaDataTypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OM (Object Manager) MetaData provider that registers managed object types.
 *
 * <p>This provider registers the ManagedMetaObject type which provides
 * state awareness and object manager integration capabilities.</p>
 *
 * <h3>Types Registered:</h3>
 * <ul>
 * <li><strong>object.managed:</strong> ManagedMetaObject with state awareness</li>
 * </ul>
 *
 * <h3>Priority:</h3>
 * <p>Priority 35 - Runs after core object types (30) to ensure object.base is available
 * before managed objects are registered.</p>
 *
 * @since 6.0.0
 */
public class OMMetaDataProvider implements MetaDataTypeProvider {

    private static final Logger log = LoggerFactory.getLogger(OMMetaDataProvider.class);

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // Register managed object type
        ManagedMetaObject.registerTypes(registry);

        log.info("OM managed types registered via provider");
    }

    @Override
    public String getProviderId() {
        return "om-managed-types";
    }

    @Override
    public String[] getDependencies() {
        // Depends on object-types for object.base inheritance
        return new String[]{"object-types"};
    }

    @Override
    public String getDescription() {
        return "OM MetaData Provider - Registers managed object types with state awareness";
    }
}