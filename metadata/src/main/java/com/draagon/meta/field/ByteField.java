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
 * A Byte Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@MetaDataType(type = "field", subType = "byte", description = "Byte field with numeric validation")
@SuppressWarnings("serial")
public class ByteField extends PrimitiveField<Byte>
{
    private static final Logger log = LoggerFactory.getLogger(ByteField.class);

    public final static String SUBTYPE_BYTE = "byte";
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

            MetaDataRegistry.registerType(ByteField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_BYTE)
                .description("Byte field with numeric validation")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // BYTE-SPECIFIC ATTRIBUTES (using new API)
                .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, ATTR_MIN_VALUE)
                .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, ATTR_MAX_VALUE)

            );

            log.debug("Registered ByteField type with unified registry");

            // Register ByteField-specific validation constraints only
            setupByteFieldValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register ByteField type with unified registry", e);
        }
    }

    /**
     * Setup ByteField-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupByteFieldValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALUE VALIDATION CONSTRAINT: Range validation for byte fields
            ValidationConstraint rangeValidation = new ValidationConstraint(
                "bytefield.range.validation",
                "ByteField minValue must be less than or equal to maxValue",
                (metadata) -> metadata instanceof ByteField &&
                              (metadata.hasMetaAttr(ATTR_MIN_VALUE) || metadata.hasMetaAttr(ATTR_MAX_VALUE)),
                (metadata, value) -> {
                    if (!metadata.hasMetaAttr(ATTR_MIN_VALUE) || !metadata.hasMetaAttr(ATTR_MAX_VALUE)) {
                        return true; // Only one bound specified - always valid
                    }

                    try {
                        byte minValue = Byte.parseByte(metadata.getMetaAttr(ATTR_MIN_VALUE).getValueAsString());
                        byte maxValue = Byte.parseByte(metadata.getMetaAttr(ATTR_MAX_VALUE).getValueAsString());
                        return minValue <= maxValue;
                    } catch (NumberFormatException e) {
                        return false; // Invalid number format
                    }
                }
            );
            constraintRegistry.addConstraint(rangeValidation);

            log.debug("Registered ByteField-specific constraints");

        } catch (Exception e) {
            log.error("Failed to register ByteField constraints", e);
        }
    }

    public ByteField(String name) {
        super(SUBTYPE_BYTE, name, DataTypes.BYTE);
    }

    /**
     * Manually Create a ByteField
     * @param name Name of the field
     * @param defaultValue Default value for the field
     * @return New ByteField
     */
    public static ByteField create( String name, Byte defaultValue ) {
        ByteField f = new ByteField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(StringAttribute.create( ATTR_DEFAULT_VALUE, defaultValue.toString() ));
        }
        return f;
    }
}
