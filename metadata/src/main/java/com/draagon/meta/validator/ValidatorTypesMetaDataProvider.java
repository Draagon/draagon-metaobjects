package com.draagon.meta.validator;

import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataTypeProvider;

/**
 * Validator Types MetaData provider that registers all concrete validator type implementations.
 *
 * <p>This provider registers all the concrete validator types that extend validator.base.
 * It calls the registerTypes() methods on each concrete validator class to ensure proper registration.</p>
 *
 * <h3>Validator Types Registered:</h3>
 * <ul>
 * <li><strong>validator.required:</strong> Required field validators</li>
 * <li><strong>validator.length:</strong> Length validation for strings/arrays</li>
 * <li><strong>validator.regex:</strong> Regular expression pattern validators</li>
 * <li><strong>validator.numeric:</strong> Numeric range validators</li>
 * <li><strong>validator.array:</strong> Array validation</li>
 * </ul>
 *
 * <h3>Priority:</h3>
 * <p>Priority 20 - Runs after attribute types (15) but before key types (25).
 * This ensures validator.base is available before concrete validator types are registered.</p>
 *
 * @since 6.0.0
 */
public class ValidatorTypesMetaDataProvider implements MetaDataTypeProvider {

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // Register concrete validator types - no more static initializers

        RequiredValidator.registerTypes(registry);
        LengthValidator.registerTypes(registry);
        RegexValidator.registerTypes(registry);
        NumericValidator.registerTypes(registry);
        ArrayValidator.registerTypes(registry);

        System.out.println("Info: Validator types registered via provider");
    }

    @Override
    public int getPriority() {
        // Priority 20: After attribute types (15), before key types (25)
        return 20;
    }

    @Override
    public String getDescription() {
        return "Validator Types MetaData Provider - Registers all concrete validator type implementations";
    }
}