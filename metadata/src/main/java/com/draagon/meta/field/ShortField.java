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

/**
 * A Short Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@MetaDataType(type = "field", subType = "short", description = "Short field type with numeric validation")
public class ShortField extends PrimitiveField<Short>
{
    private static final Logger log = LoggerFactory.getLogger(ShortField.class);

    public final static String SUBTYPE_SHORT = "short";
    public final static String ATTR_MIN_VALUE = "minValue";
    public final static String ATTR_MAX_VALUE = "maxValue";

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

            MetaDataRegistry.registerType(ShortField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_SHORT)
                .description("Short field with numeric validation")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // SHORT-SPECIFIC ATTRIBUTES (using new API)
                .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, ATTR_MIN_VALUE)
                .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, ATTR_MAX_VALUE)

            );

            log.debug("Registered ShortField type with unified registry");

            // Register ShortField-specific validation constraints only
            setupShortFieldValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register ShortField type with unified registry", e);
        }
    }

    /**
     * Setup ShortField-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupShortFieldValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALUE VALIDATION CONSTRAINT: Range validation for short fields
            ValidationConstraint rangeValidation = new ValidationConstraint(
                "shortfield.range.validation",
                "ShortField minValue must be less than or equal to maxValue",
                (metadata) -> metadata instanceof ShortField &&
                              (metadata.hasMetaAttr(ATTR_MIN_VALUE) || metadata.hasMetaAttr(ATTR_MAX_VALUE)),
                (metadata, value) -> {
                    if (!metadata.hasMetaAttr(ATTR_MIN_VALUE) || !metadata.hasMetaAttr(ATTR_MAX_VALUE)) {
                        return true; // Only one bound specified - always valid
                    }

                    try {
                        short minValue = Short.parseShort(metadata.getMetaAttr(ATTR_MIN_VALUE).getValueAsString());
                        short maxValue = Short.parseShort(metadata.getMetaAttr(ATTR_MAX_VALUE).getValueAsString());
                        return minValue <= maxValue;
                    } catch (NumberFormatException e) {
                        return false; // Invalid number format
                    }
                }
            );
            constraintRegistry.addConstraint(rangeValidation);

            log.debug("Registered ShortField-specific constraints");

        } catch (Exception e) {
            log.error("Failed to register ShortField constraints", e);
        }
    }

    public ShortField( String name ) {
        super( SUBTYPE_SHORT, name, DataTypes.SHORT );
    }

    /**
     * Manually Create a ByteField
     * @param name Name of the field
     * @param defaultValue Default value for the field
     * @return New ByteField
     */
    public static ShortField create( String name, Integer defaultValue ) {
        ShortField f = new ShortField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(StringAttribute.create( ATTR_DEFAULT_VALUE, defaultValue.toString() ));
        }
        return f;
    }
}
