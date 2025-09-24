/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.draagon.meta.validator;

import java.util.ArrayList;
import java.util.Collection;

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
 * Numeric Validator that ensures a value is a number with unified registry registration.
 *
 * @version 6.2
 */
@MetaDataType(type = "validator", subType = "numeric", description = "Numeric validator ensuring values are numbers")
@SuppressWarnings("serial")
public class NumericValidator extends MetaValidator {

    private static final Logger log = LoggerFactory.getLogger(NumericValidator.class);
    public final static String SUBTYPE_NUMERIC = "numeric";

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

            MetaDataRegistry.registerType(NumericValidator.class, def -> def
                .type(TYPE_VALIDATOR).subType(SUBTYPE_NUMERIC)
                .description("Numeric validator ensuring values are numbers")

                // INHERIT FROM BASE VALIDATOR
                .inheritsFrom(TYPE_VALIDATOR, SUBTYPE_BASE)

                // NumericValidator inherits all parent acceptance from validator.base:
                // - Can be placed under any field type (especially numeric fields)
                // - Can be placed under any object type
                // - Can be placed under other validators
                // - Can be placed under loaders as abstract

                // NO NUMERIC-SPECIFIC ATTRIBUTES (only uses inherited base attributes)
                // NO ADDITIONAL CHILD REQUIREMENTS (only uses inherited validator capabilities)
            );

            log.debug("Registered NumericValidator type with unified registry");

            // Register NumericValidator-specific validation constraints only
            setupNumericValidatorValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register NumericValidator type with unified registry", e);
        }
    }

    /**
     * Setup NumericValidator-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupNumericValidatorValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALIDATION CONSTRAINT: Numeric validator behavior consistency
            ValidationConstraint numericValidatorValidation = new ValidationConstraint(
                "numericvalidator.behavior.validation",
                "NumericValidator must be properly configured for numeric validation",
                (metadata) -> metadata instanceof NumericValidator,
                (metadata, value) -> {
                    if (metadata instanceof NumericValidator) {
                        NumericValidator validator = (NumericValidator) metadata;
                        // Validate that the numeric validator has a proper name and parent context
                        return validator.getName() != null &&
                               !validator.getName().trim().isEmpty() &&
                               validator.getParent() != null;
                    }
                    return true;
                }
            );
            constraintRegistry.addConstraint(numericValidatorValidation);

            log.debug("Registered NumericValidator-specific constraints");

        } catch (Exception e) {
            log.error("Failed to register NumericValidator constraints", e);
        }
    }

    public NumericValidator(String name) {
        super(SUBTYPE_NUMERIC, name);
    }

    /**
     * Validates the value of the field in the specified object
     */
    public void validate(Object object, Object value) {

        String val = (value == null) ? null : value.toString();

        if (!GenericValidator.isBlankOrNull(val)) {

            for (int i = 0; i < val.length(); i++) {
                if (val.charAt(i) < '0' || val.charAt(i) > '9')
                    throw new InvalidValueException(getMessage("The value is not a valid number"));
            }
        }
    }
}
