package com.draagon.meta.registry;

import com.draagon.meta.validator.MetaValidator;
import com.draagon.meta.validator.RequiredValidator;
import com.draagon.meta.validator.LengthValidator;
import com.draagon.meta.validator.RegexValidator;
import com.draagon.meta.validator.NumericValidator;
import com.draagon.meta.validator.ArrayValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Provider for all validator types in the MetaObjects framework.
 *
 * <p>This provider registers the complete validator type hierarchy:</p>
 * <ul>
 *   <li><strong>validator.base:</strong> Base validator type with common validator attributes and children</li>
 *   <li><strong>validator.required:</strong> Required field validation</li>
 *   <li><strong>validator.length:</strong> String length validation with min/max bounds</li>
 *   <li><strong>validator.regex:</strong> Regular expression pattern validation</li>
 *   <li><strong>validator.numeric:</strong> Numeric range and type validation</li>
 *   <li><strong>validator.array:</strong> Array and collection validation</li>
 * </ul>
 *
 * <p>All concrete validator types inherit from validator.base, which inherits from metadata.base,
 * providing a clean inheritance hierarchy with shared attributes and child acceptance.</p>
 *
 * <h3>Validator Type Hierarchy:</h3>
 * <pre>
 * metadata.base (MetaDataLoader)
 *     └── validator.base (MetaValidator) - common validator attributes, accepts attributes
 *         ├── validator.required (RequiredValidator) - required field validation
 *         ├── validator.length (LengthValidator) - string length validation + min/max attributes
 *         ├── validator.regex (RegexValidator) - pattern validation + pattern attributes
 *         ├── validator.numeric (NumericValidator) - numeric validation + range attributes
 *         └── validator.array (ArrayValidator) - array validation + size attributes
 * </pre>
 *
 * @since 6.3.0
 */
public class ValidatorTypeProvider implements MetaDataTypeProvider {

    private static final Logger log = LoggerFactory.getLogger(ValidatorTypeProvider.class);

    @Override
    public void registerTypes(MetaDataRegistry registry) throws Exception {
        log.info("Registering validator types...");

        // Force class loading to trigger static block registrations
        // These classes have static blocks that register themselves
        Class.forName(MetaValidator.class.getName());
        Class.forName(RequiredValidator.class.getName());
        Class.forName(LengthValidator.class.getName());
        Class.forName(RegexValidator.class.getName());
        Class.forName(NumericValidator.class.getName());
        Class.forName(ArrayValidator.class.getName());

        log.info("Successfully registered {} validator types", getValidatorTypeCount());
    }

    @Override
    public String getProviderName() {
        return "validator-types";
    }

    @Override
    public Set<String> getDependencies() {
        // Validator types depend on core types being loaded first
        return Set.of("core-types");
    }

    @Override
    public int getPriority() {
        // High priority - fundamental types
        return 650;
    }

    @Override
    public boolean supportsOSGi() {
        return true;
    }

    @Override
    public String getDescription() {
        return "All MetaValidator types (validator.base + 5 concrete validator types)";
    }

    /**
     * Get the total number of validator types registered by this provider
     */
    private int getValidatorTypeCount() {
        return 6; // validator.base + validator.required + validator.length + validator.regex + validator.numeric + validator.array
    }
}