/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.draagon.meta.field.MetaField.TYPE_FIELD;
import static com.draagon.meta.field.MetaField.SUBTYPE_BASE;

/**
 * A Float Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@MetaDataType(type = "field", subType = "float", description = "Float field with numeric and precision validation")
public class FloatField extends PrimitiveField<Float>
{
    private static final Logger log = LoggerFactory.getLogger(FloatField.class);

    public final static String SUBTYPE_FLOAT = "float";
    public final static String ATTR_MIN_VALUE = "minValue";
    public final static String ATTR_MAX_VALUE = "maxValue";
    public final static String ATTR_PRECISION = "precision";

    // Unified registry self-registration
    static {
        try {
            // Explicitly trigger MetaField static initialization first
            try {
                Class.forName(MetaField.class.getName());
                // Add a small delay to ensure MetaField registration completes
                Thread.sleep(1);
            } catch (ClassNotFoundException | InterruptedException e) {
                log.warn("Could not force MetaField class loading", e);
            }

            MetaDataRegistry.registerType(FloatField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_FLOAT)
                .description("Float field with numeric and precision validation")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // FLOAT-SPECIFIC ATTRIBUTES (using new API)
                .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_MIN_VALUE)
                .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_MAX_VALUE)
                .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, ATTR_PRECISION)

            );

            log.debug("Registered FloatField type with unified registry");

            // Register FloatField-specific validation constraints only
            setupFloatFieldValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register FloatField type with unified registry", e);
        }
    }

    /**
     * Setup FloatField-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupFloatFieldValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALUE VALIDATION CONSTRAINT: Range validation for float fields
            ValidationConstraint rangeValidation = new ValidationConstraint(
                "floatfield.range.validation",
                "FloatField minValue must be less than or equal to maxValue",
                (metadata) -> metadata instanceof FloatField &&
                              (metadata.hasMetaAttr(ATTR_MIN_VALUE) || metadata.hasMetaAttr(ATTR_MAX_VALUE)),
                (metadata, value) -> {
                    if (!metadata.hasMetaAttr(ATTR_MIN_VALUE) || !metadata.hasMetaAttr(ATTR_MAX_VALUE)) {
                        return true; // Only one bound specified - always valid
                    }

                    try {
                        float minValue = Float.parseFloat(metadata.getMetaAttr(ATTR_MIN_VALUE).getValueAsString());
                        float maxValue = Float.parseFloat(metadata.getMetaAttr(ATTR_MAX_VALUE).getValueAsString());
                        return minValue <= maxValue;
                    } catch (NumberFormatException e) {
                        return false; // Invalid number format
                    }
                }
            );
            constraintRegistry.addConstraint(rangeValidation);

            // VALUE VALIDATION CONSTRAINT: Precision validation
            ValidationConstraint precisionValidation = new ValidationConstraint(
                "floatfield.precision.validation",
                "FloatField precision must be a positive integer",
                (metadata) -> metadata instanceof FloatField && metadata.hasMetaAttr(ATTR_PRECISION),
                (metadata, value) -> {
                    try {
                        int precision = Integer.parseInt(metadata.getMetaAttr(ATTR_PRECISION).getValueAsString());
                        return precision > 0;
                    } catch (NumberFormatException e) {
                        return false; // Invalid number format
                    }
                }
            );
            constraintRegistry.addConstraint(precisionValidation);

            log.debug("Registered FloatField-specific constraints");

        } catch (Exception e) {
            log.error("Failed to register FloatField constraints", e);
        }
    }

    public FloatField( String name ) {
        super( SUBTYPE_FLOAT, name, DataTypes.FLOAT );
    }

    /**
     * Manually Create a FloatField
     * @param name Name of the field
     * @return New FloatField
     */
    public static FloatField create( String name ) {
        FloatField f = new FloatField( name );
        return f;
    }
}
