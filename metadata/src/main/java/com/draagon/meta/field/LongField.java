/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.LongAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Long Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@MetaDataType(type = "field", subType = "long", description = "Long field with numeric validation")
public class LongField extends PrimitiveField<Long> {

    private static final Logger log = LoggerFactory.getLogger(LongField.class);

    public final static String SUBTYPE_LONG = "long";
    public final static String ATTR_MIN_VALUE = "minValue";
    public final static String ATTR_MAX_VALUE = "maxValue";

    public LongField( String name ) {
        super( SUBTYPE_LONG, name, DataTypes.LONG );
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

            MetaDataRegistry.registerType(LongField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_LONG)
                .description("Long field with numeric validation")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // LONG-SPECIFIC ATTRIBUTES (using new API) - CORE ONLY
                .acceptsNamedAttributes(LongAttribute.SUBTYPE_LONG, ATTR_MIN_VALUE)
                .acceptsNamedAttributes(LongAttribute.SUBTYPE_LONG, ATTR_MAX_VALUE)

                // NOTE: Database attributes are declared by DatabaseConstraintProvider
                // This maintains separation of concerns and extensibility
            );

            log.debug("Registered LongField type with unified registry");

            // Register LongField-specific validation constraints only
            setupLongFieldValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register LongField type with unified registry", e);
        }
    }
    
    /**
     * Setup LongField-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupLongFieldValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALUE VALIDATION CONSTRAINT: Range validation for long fields
            ValidationConstraint rangeValidation = new ValidationConstraint(
                "longfield.range.validation",
                "LongField minValue must be less than or equal to maxValue",
                (metadata) -> metadata instanceof LongField &&
                              (metadata.hasMetaAttr(ATTR_MIN_VALUE) || metadata.hasMetaAttr(ATTR_MAX_VALUE)),
                (metadata, value) -> {
                    if (!metadata.hasMetaAttr(ATTR_MIN_VALUE) || !metadata.hasMetaAttr(ATTR_MAX_VALUE)) {
                        return true; // Only one bound specified - always valid
                    }

                    try {
                        long minValue = Long.parseLong(metadata.getMetaAttr(ATTR_MIN_VALUE).getValueAsString());
                        long maxValue = Long.parseLong(metadata.getMetaAttr(ATTR_MAX_VALUE).getValueAsString());
                        return minValue <= maxValue;
                    } catch (NumberFormatException e) {
                        return false; // Invalid number format
                    }
                }
            );
            constraintRegistry.addConstraint(rangeValidation);
            
            log.debug("Registered LongField-specific constraints");
            
        } catch (Exception e) {
            log.error("Failed to register LongField constraints", e);
        }
    }

    /**
     * Manually Create a LongField
     * @param name Name of the field
     * @param defaultValue Default value for the field
     * @return New LongField
     */
    public static LongField create( String name, Long defaultValue ) {
        LongField f = new LongField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(StringAttribute.create( ATTR_DEFAULT_VALUE, defaultValue.toString() ));
        }
        return f;
    }
}
