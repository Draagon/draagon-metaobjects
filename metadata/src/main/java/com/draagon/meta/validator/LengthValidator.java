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
import com.draagon.meta.field.MetaField;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.apache.commons.validator.GenericValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.draagon.meta.validator.MetaValidator.SUBTYPE_BASE;
import static com.draagon.meta.validator.MetaValidator.TYPE_VALIDATOR;

// TODO:  Make this work for numeric fields and even Date fields, or create new validator types
/**
 * A length validator with unified registry registration that ensures the string representation of a field value is of the min or max length.
 *
 * @version 6.0
 */
@MetaDataType(type = "validator", subType = "length", description = "Length validator for string field validation")
public class LengthValidator extends MetaValidator
{
    private static final Logger log = LoggerFactory.getLogger(LengthValidator.class);

    public final static String SUBTYPE_LENGTH = "length";

    /**
     * Minimum length attribute
     */
    public final static String ATTR_MIN = "min";
    /**
     * Maximum length attribute
     */
    public final static String ATTR_MAX = "max";

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

            MetaDataRegistry.registerType(LengthValidator.class, def -> def
                .type(TYPE_VALIDATOR).subType(SUBTYPE_LENGTH)
                .description("Length validator ensures field value is within min/max length")

                // INHERIT FROM BASE VALIDATOR
                .inheritsFrom(TYPE_VALIDATOR, SUBTYPE_BASE)

                // LengthValidator inherits all parent acceptance from validator.base:
                // - Can be placed under any field type (especially string fields)
                // - Can be placed under any object type
                // - Can be placed under other validators
                // - Can be placed under loaders as abstract

                // LENGTH-SPECIFIC CHILD ACCEPTANCE DECLARATIONS
                // LengthValidator can have min and max attributes for length bounds
                .acceptsNamedChildren("attr", "int", ATTR_MIN)    // Min length attribute
                .acceptsNamedChildren("attr", "int", ATTR_MAX)    // Max length attribute
                .acceptsNamedChildren("attr", "string", ATTR_MIN) // Min length as string (legacy support)
                .acceptsNamedChildren("attr", "string", ATTR_MAX) // Max length as string (legacy support)

                // NO ADDITIONAL CHILD REQUIREMENTS (only uses inherited validator capabilities + min/max)
            );

            log.debug("Registered LengthValidator type with unified registry");

            // Register LengthValidator-specific validation constraints only
            setupLengthValidatorValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register LengthValidator type with unified registry", e);
        }
    }

    /**
     * Setup LengthValidator-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupLengthValidatorValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALIDATION CONSTRAINT: Length validator bounds validation
            ValidationConstraint lengthBoundsValidation = new ValidationConstraint(
                "lengthvalidator.bounds.validation",
                "LengthValidator min and max attributes must be valid integers with min <= max",
                (metadata) -> metadata instanceof LengthValidator,
                (metadata, value) -> {
                    if (metadata instanceof LengthValidator) {
                        LengthValidator validator = (LengthValidator) metadata;
                        try {
                            int min = 0;
                            int max = Integer.MAX_VALUE;

                            // Check min attribute if present
                            if (validator.hasMetaAttr(ATTR_MIN)) {
                                String minStr = validator.getMetaAttr(ATTR_MIN).getValueAsString();
                                min = Integer.parseInt(minStr);
                                if (min < 0) return false; // Min cannot be negative
                            }

                            // Check max attribute if present
                            if (validator.hasMetaAttr(ATTR_MAX)) {
                                String maxStr = validator.getMetaAttr(ATTR_MAX).getValueAsString();
                                max = Integer.parseInt(maxStr);
                                if (max < 0) return false; // Max cannot be negative
                            }

                            // Min must be <= max
                            return min <= max;

                        } catch (NumberFormatException e) {
                            return false; // Invalid number format
                        } catch (Exception e) {
                            return false; // Any other error
                        }
                    }
                    return true;
                }
            );
            constraintRegistry.addConstraint(lengthBoundsValidation);

            log.debug("Registered LengthValidator-specific constraints");

        } catch (Exception e) {
            log.error("Failed to register LengthValidator constraints", e);
        }
    }

    public LengthValidator(String name) {
        super(SUBTYPE_LENGTH, name);
    }

    /**
     * Validates the value of the field in the specified object
     */
    public void validate(Object object, Object value) {

        int min = hasMetaAttr(ATTR_MIN)
                ? Integer.parseInt(getMetaAttr(ATTR_MIN).getValueAsString())
                : 0;

        int max = hasMetaAttr(ATTR_MAX)
                ? Integer.parseInt(getMetaAttr(ATTR_MAX).getValueAsString())
                : getDefaultMax( getMetaField( object ));

        String msg = getMessage("A valid length between " + min + " and " + max + " must be entered");
        String val = (value == null) ? null : value.toString();

        if (!GenericValidator.isBlankOrNull(val)
                && (val.length() < min || val.length() > max)) {
            throw new InvalidValueException(msg);
        }
    }

    /** Get the default max string size based on the MetaField DataType */
    protected int getDefaultMax( MetaField f ) {
        switch( f.getDataType() )
        {
            case BOOLEAN: return 1;
            case BYTE: return 4;
            case SHORT: return 6;
            case INT: return 10;
            case LONG: return 15;
            case FLOAT: return 12;
            case DOUBLE: return 16;
            case STRING: return 100;
            case DATE: return 15;
            default:  return 1000000;
        }
    }
}
