package com.draagon.meta.attr;

import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataTypeProvider;

/**
 * Attribute Types MetaData provider that registers all concrete attribute type implementations.
 *
 * <p>This provider registers all the concrete attribute types that extend attr.base.
 * It calls the registerTypes() methods on each concrete attribute class to ensure proper registration.</p>
 *
 * <h3>Attribute Types Registered:</h3>
 * <ul>
 * <li><strong>attr.string:</strong> String attributes</li>
 * <li><strong>attr.int:</strong> Integer attributes</li>
 * <li><strong>attr.long:</strong> Long attributes</li>
 * <li><strong>attr.double:</strong> Double attributes</li>
 * <li><strong>attr.boolean:</strong> Boolean attributes</li>
 * <li><strong>attr.stringarray:</strong> String array attributes</li>
 * <li><strong>attr.class:</strong> Class attributes</li>
 * <li><strong>attr.properties:</strong> Properties attributes</li>
 * </ul>
 *
 * <h3>Priority:</h3>
 * <p>Priority 15 - Runs after field types (10) but before validators (20).
 * This ensures attr.base is available before concrete attribute types are registered.</p>
 *
 * @since 6.0.0
 */
public class AttributeTypesMetaDataProvider implements MetaDataTypeProvider {

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // Register concrete attribute types - no more static initializers

        StringAttribute.registerTypes(registry);
        IntAttribute.registerTypes(registry);
        LongAttribute.registerTypes(registry);
        DoubleAttribute.registerTypes(registry);
        BooleanAttribute.registerTypes(registry);
        StringArrayAttribute.registerTypes(registry);
        ClassAttribute.registerTypes(registry);
        PropertiesAttribute.registerTypes(registry);

        System.out.println("Info: Attribute types registered via provider");
    }

    @Override
    public int getPriority() {
        // Priority 15: After field types (10), before validators (20)
        return 15;
    }

    @Override
    public String getDescription() {
        return "Attribute Types MetaData Provider - Registers all concrete attribute type implementations";
    }
}