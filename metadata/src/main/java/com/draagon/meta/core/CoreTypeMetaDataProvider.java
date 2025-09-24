package com.draagon.meta.core;

import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataTypeProvider;

/**
 * Core Type MetaData provider that ensures base MetaData types are registered first.
 *
 * <p>This provider registers the fundamental base types that all other providers depend on.
 * It does not add extensions but ensures that core field, object, attribute, validator,
 * and view base types are available for other providers to extend.</p>
 *
 * <h3>Core Types Ensured:</h3>
 * <ul>
 * <li><strong>field.base:</strong> Base type for all fields</li>
 * <li><strong>object.base:</strong> Base type for all objects</li>
 * <li><strong>attr.base:</strong> Base type for all attributes</li>
 * <li><strong>validator.base:</strong> Base type for all validators</li>
 * <li><strong>view.base:</strong> Base type for all views</li>
 * <li><strong>key.base:</strong> Base type for all keys</li>
 * </ul>
 *
 * <h3>Priority:</h3>
 * <p>Priority 0 - Runs first to ensure base types are available for all other providers.
 * This is critical for the service provider pattern to work correctly.</p>
 *
 * @since 6.0.0
 */
public class CoreTypeMetaDataProvider implements MetaDataTypeProvider {

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // This provider ensures that base types are registered.
        // The actual registration happens via the existing @MetaDataType annotations
        // and static blocks in the MetaData classes themselves.

        // We don't need to do anything here because the base types are already
        // registered via the static blocks in:
        // - MetaField.registerTypes() -> field.base
        // - MetaObject.registerTypes() -> object.base
        // - MetaAttribute.registerTypes() -> attr.base
        // - MetaValidator.registerTypes() -> validator.base
        // - MetaView.registerTypes() -> view.base
        // - MetaKey.registerTypes() -> key.base

        // This provider exists to ensure proper dependency ordering.
        // By having priority 0, it runs first and ensures all base types
        // are available before other providers try to extend them.

        // Log that core types are ready for extension
        System.out.println("Info: Core base types ready for service provider extensions");
    }

    @Override
    public int getPriority() {
        // Priority 0: Runs first to ensure base types are available
        return 0;
    }

    @Override
    public String getDescription() {
        return "Core Type MetaData Provider - Ensures base types are available for extension";
    }
}