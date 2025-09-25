package com.draagon.meta.field;

import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataTypeProvider;

/**
 * Field Types MetaData provider that registers all concrete field type implementations.
 *
 * <p>This provider registers all the concrete field types that extend field.base.
 * It calls the registerTypes() methods on each concrete field class to ensure proper registration.</p>
 *
 * <h3>Field Types Registered:</h3>
 * <ul>
 * <li><strong>field.string:</strong> String fields with pattern and length validation</li>
 * <li><strong>field.int:</strong> Integer fields with range validation</li>
 * <li><strong>field.long:</strong> Long fields with range validation</li>
 * <li><strong>field.double:</strong> Double fields with precision validation</li>
 * <li><strong>field.float:</strong> Float fields with precision validation</li>
 * <li><strong>field.boolean:</strong> Boolean fields</li>
 * <li><strong>field.date:</strong> Date fields with format validation</li>
 * <li><strong>field.timestamp:</strong> Timestamp fields</li>
 * <li><strong>field.byte:</strong> Byte fields</li>
 * <li><strong>field.short:</strong> Short fields</li>
 * </ul>
 *
 * <h3>Priority:</h3>
 * <p>Priority 10 - Runs after core base types (0) but before extensions (50+).
 * This ensures field.base is available before concrete field types are registered.</p>
 *
 * @since 6.0.0
 */
public class FieldTypesMetaDataProvider implements MetaDataTypeProvider {

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // Register concrete field types - no more static initializers

        StringField.registerTypes(registry);
        IntegerField.registerTypes(registry);
        LongField.registerTypes(registry);
        DoubleField.registerTypes(registry);
        FloatField.registerTypes(registry);
        BooleanField.registerTypes(registry);
        DateField.registerTypes(registry);
        TimestampField.registerTypes(registry);
        ByteField.registerTypes(registry);
        ShortField.registerTypes(registry);

        System.out.println("Info: Field types registered via provider");
    }

    @Override
    public int getPriority() {
        // Priority 10: After base types (0), before extensions (50+)
        return 10;
    }

    @Override
    public String getDescription() {
        return "Field Types MetaData Provider - Registers all concrete field type implementations";
    }
}