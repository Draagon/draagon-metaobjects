package com.draagon.meta.validator;

import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataTypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(ValidatorTypesMetaDataProvider.class);

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // FIRST: Register the base validator type that all others inherit from
        MetaValidator.registerTypes(registry);

        // THEN: Register concrete validator types that inherit from validator.base
        RequiredValidator.registerTypes(registry);
        LengthValidator.registerTypes(registry);
        RegexValidator.registerTypes(registry);
        NumericValidator.registerTypes(registry);
        ArrayValidator.registerTypes(registry);

        log.info("Validator types registered via provider");
    }

    @Override
    public String getProviderId() {
        return "validator-types";
    }

    @Override
    public String[] getDependencies() {
        // No dependencies - validator.base inherits from metadata.base which is auto-registered
        return new String[0];
    }

    @Override
    public String getDescription() {
        return "Validator Types MetaData Provider - Registers all concrete validator type implementations";
    }
}