/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.draagon.meta.validator;

import com.draagon.meta.*;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.validator.GenericValidator;

import static com.draagon.meta.validator.MetaValidator.SUBTYPE_BASE;
import static com.draagon.meta.validator.MetaValidator.TYPE_VALIDATOR;

/**
 * Regular expression validator, that ensures a field value matches the specific expression mask
 * with unified registry registration.
 *
 * @version 6.2
 */
@MetaDataType(type = "validator", subType = "regex", description = "Regular expression validator for pattern matching")
public class RegexValidator extends MetaValidator {

    private static final Logger log = LoggerFactory.getLogger(RegexValidator.class);

    public final static String SUBTYPE_REGEX = "regex";

    /** Mask attribute */
    public final static String ATTR_MASK = "mask";

    // Unified registry self-registration
    static {
        try {
            // Explicitly trigger MetaValidator static initialization first
            try {
                Class.forName(MetaValidator.class.getName());
                // Add a small delay to ensure MetaValidator registration completes
                Thread.sleep(1);
            } catch (ClassNotFoundException | InterruptedException e) {
                log.warn("Could not force MetaValidator class loading", e);
            }

            MetaDataRegistry.registerType(RegexValidator.class, def -> def
                .type(TYPE_VALIDATOR).subType(SUBTYPE_REGEX)
                .description("Regular expression validator for pattern matching")

                // INHERIT FROM BASE VALIDATOR
                .inheritsFrom(TYPE_VALIDATOR, SUBTYPE_BASE)

                // RegexValidator inherits all parent acceptance from validator.base:
                // - Can be placed under any field type (especially string fields)
                // - Can be placed under any object type
                // - Can be placed under other validators
                // - Can be placed under loaders as abstract

                // REGEX-SPECIFIC CHILD ACCEPTANCE DECLARATIONS
                // RegexValidator requires a mask attribute for the regex pattern
                .acceptsNamedChildren("attr", "string", ATTR_MASK)  // Required mask attribute

                // NO ADDITIONAL CHILD REQUIREMENTS (only uses inherited validator capabilities + mask)
            );

            log.debug("Registered RegexValidator type with unified registry");

            // Register RegexValidator-specific validation constraints only
            setupRegexValidatorValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register RegexValidator type with unified registry", e);
        }
    }

    /**
     * Setup RegexValidator-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupRegexValidatorValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALIDATION CONSTRAINT: Regex validator must have valid mask
            ValidationConstraint regexMaskValidation = new ValidationConstraint(
                "regexvalidator.mask.validation",
                "RegexValidator must have a valid regex mask attribute",
                (metadata) -> metadata instanceof RegexValidator,
                (metadata, value) -> {
                    if (metadata instanceof RegexValidator) {
                        RegexValidator validator = (RegexValidator) metadata;
                        try {
                            // Check if mask attribute exists and is valid regex
                            String mask = validator.getMetaAttr(ATTR_MASK).getValueAsString();
                            if (mask == null || mask.trim().isEmpty()) {
                                return false;
                            }
                            // Try to compile the regex to validate it
                            java.util.regex.Pattern.compile(mask);
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    }
                    return true;
                }
            );
            constraintRegistry.addConstraint(regexMaskValidation);

            log.debug("Registered RegexValidator-specific constraints");

        } catch (Exception e) {
            log.error("Failed to register RegexValidator constraints", e);
        }
    }

    public RegexValidator(String name) {
        super(SUBTYPE_REGEX, name);
    }

    /**
     * Validates the value of the field in the specified object
     */
    public void validate(Object object, Object value)
    //throws MetaException
    {
        String mask = getMetaAttr(ATTR_MASK).getValueAsString();
        String msg = getMessage("Invalid value format");

        String val = (value == null) ? null : value.toString();

        if (!GenericValidator.isBlankOrNull(val)
                && !GenericValidator.matchRegexp(val, mask)) {
            throw new InvalidValueException(msg);
        }
    }
}
