package com.draagon.meta.core;

import com.draagon.meta.object.service.ObjectCreationService;
import com.draagon.meta.io.service.IOService;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataTypeProvider;

/**
 * Core MetaData provider that registers type extensions for core framework services.
 *
 * <p>This provider delegates to service classes that contain the actual extension logic
 * and constants for core framework operations. It extends existing MetaData types with
 * attributes needed for object creation (ValueObject, DataObject) and I/O operations.</p>
 *
 * <h3>Services Provided:</h3>
 * <ul>
 * <li><strong>ObjectCreationService:</strong> ValueObject and DataObject creation attributes</li>
 * <li><strong>IOService:</strong> File I/O, serialization, and data exchange attributes</li>
 * </ul>
 *
 * <h3>Priority:</h3>
 * <p>Priority 100 - Runs after basic type registration but before database services.
 * This ensures core object and I/O attributes are available for other service extensions.</p>
 *
 * @since 6.0.0
 */
public class CoreMetaDataProvider implements MetaDataTypeProvider {

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // Delegate to service classes that contain the extension logic and constants

        // Register object creation type extensions (ValueObject, DataObject)
        ObjectCreationService.registerTypeExtensions(registry);

        // Register I/O type extensions (file I/O, serialization)
        IOService.registerTypeExtensions(registry);
    }

    @Override
    public int getPriority() {
        // Priority 100: After basic types, before database services (150+)
        return 100;
    }

    @Override
    public String getDescription() {
        return "Core MetaData Provider - Object creation, ValueObject, DataObject, and I/O extensions";
    }
}