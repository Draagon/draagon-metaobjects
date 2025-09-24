/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.DoubleAttribute;
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
 * A Double Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@MetaDataType(type = "field", subType = "double", description = "Double field with numeric and precision validation")
@SuppressWarnings("serial")
public class DoubleField extends PrimitiveField<Double>
{
    private static final Logger log = LoggerFactory.getLogger(DoubleField.class);

    public final static String SUBTYPE_DOUBLE   = "double";
    public final static String ATTR_MIN_VALUE = "minValue";
    public final static String ATTR_MAX_VALUE = "maxValue";
    public final static String ATTR_PRECISION = "precision";
    public final static String ATTR_SCALE = "scale";

    public DoubleField( String name ) {
        super( SUBTYPE_DOUBLE, name, DataTypes.DOUBLE );
    }

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

            MetaDataRegistry.registerType(DoubleField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_DOUBLE)
                .description("Double field with numeric and precision validation")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // DOUBLE-SPECIFIC ATTRIBUTES (using new API)
                .acceptsNamedAttributes(DoubleAttribute.SUBTYPE_DOUBLE, ATTR_MIN_VALUE)
                .acceptsNamedAttributes(DoubleAttribute.SUBTYPE_DOUBLE, ATTR_MAX_VALUE)
                .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, ATTR_PRECISION)
                .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, ATTR_SCALE)

            );

            log.debug("Registered DoubleField type with unified registry");

            // Register DoubleField-specific validation constraints only
            setupDoubleFieldValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register DoubleField type with unified registry", e);
        }
    }
    
    /**
     * Setup DoubleField-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupDoubleFieldValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALUE VALIDATION CONSTRAINT: Range validation for double fields
            ValidationConstraint rangeValidation = new ValidationConstraint(
                "doublefield.range.validation",
                "DoubleField minValue must be less than or equal to maxValue",
                (metadata) -> metadata instanceof DoubleField &&
                              (metadata.hasMetaAttr(ATTR_MIN_VALUE) || metadata.hasMetaAttr(ATTR_MAX_VALUE)),
                (metadata, value) -> {
                    if (!metadata.hasMetaAttr(ATTR_MIN_VALUE) || !metadata.hasMetaAttr(ATTR_MAX_VALUE)) {
                        return true; // Only one bound specified - always valid
                    }

                    try {
                        double minValue = Double.parseDouble(metadata.getMetaAttr(ATTR_MIN_VALUE).getValueAsString());
                        double maxValue = Double.parseDouble(metadata.getMetaAttr(ATTR_MAX_VALUE).getValueAsString());
                        return minValue <= maxValue;
                    } catch (NumberFormatException e) {
                        return false; // Invalid number format
                    }
                }
            );
            constraintRegistry.addConstraint(rangeValidation);

            // VALUE VALIDATION CONSTRAINT: Precision and scale validation
            ValidationConstraint precisionScaleValidation = new ValidationConstraint(
                "doublefield.precision.scale.validation",
                "DoubleField precision must be greater than scale when both are specified",
                (metadata) -> metadata instanceof DoubleField &&
                              metadata.hasMetaAttr(ATTR_PRECISION) && metadata.hasMetaAttr(ATTR_SCALE),
                (metadata, value) -> {
                    try {
                        int precision = Integer.parseInt(metadata.getMetaAttr(ATTR_PRECISION).getValueAsString());
                        int scale = Integer.parseInt(metadata.getMetaAttr(ATTR_SCALE).getValueAsString());
                        return precision > scale && precision > 0 && scale >= 0;
                    } catch (NumberFormatException e) {
                        return false; // Invalid number format
                    }
                }
            );
            constraintRegistry.addConstraint(precisionScaleValidation);

            log.debug("Registered DoubleField-specific constraints");

        } catch (Exception e) {
            log.error("Failed to register DoubleField constraints", e);
        }
    }

    /**
     * Manually Create a DoubleField
     * @param name Name of the field
     * @return New DoubleField
     */
    public static DoubleField create( String name ) {
        DoubleField f = new DoubleField( name );
        return f;
    }
}
