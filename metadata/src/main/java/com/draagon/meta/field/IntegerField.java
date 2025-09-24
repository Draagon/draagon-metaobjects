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
 * An Integer Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@MetaDataType(type = "field", subType = "int", description = "Integer field with range validation")
public class IntegerField extends PrimitiveField<Integer> {

    private static final Logger log = LoggerFactory.getLogger(IntegerField.class);

    public final static String SUBTYPE_INT = "int";
    public final static String ATTR_MIN_VALUE = "minValue";
    public final static String ATTR_MAX_VALUE = "maxValue";

    public IntegerField( String name ) {
        super( SUBTYPE_INT, name, DataTypes.INT );
    }

    /**
     * Register field.int type and integer-specific constraints using Phase 2 standardized pattern.
     *
     * @param registry MetaDataRegistry to register with
     */
    public static void registerTypes(MetaDataRegistry registry) {
        try {
            registry.registerType(IntegerField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_INT)
                .description("Integer field with range validation")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // INTEGER-SPECIFIC ATTRIBUTES (using new API) - CORE ONLY
                .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, ATTR_MIN_VALUE)
                .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, ATTR_MAX_VALUE)

                // NOTE: Database attributes are declared by DatabaseConstraintProvider
                // This maintains separation of concerns and extensibility
            );

            log.debug("Registered IntegerField type using Phase 2 pattern");

            // Register IntegerField-specific validation constraints only
            setupIntegerFieldValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register IntegerField type using Phase 2 pattern", e);
            throw new RuntimeException("IntegerField type registration failed", e);
        }
    }
    
    /**
     * Setup IntegerField-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupIntegerFieldValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALUE VALIDATION CONSTRAINT: Range validation for integer fields
            ValidationConstraint rangeValidation = new ValidationConstraint(
                "integerfield.range.validation",
                "IntegerField minValue must be less than or equal to maxValue",
                (metadata) -> metadata instanceof IntegerField &&
                              (metadata.hasMetaAttr(ATTR_MIN_VALUE) || metadata.hasMetaAttr(ATTR_MAX_VALUE)),
                (metadata, value) -> {
                    if (!metadata.hasMetaAttr(ATTR_MIN_VALUE) || !metadata.hasMetaAttr(ATTR_MAX_VALUE)) {
                        return true; // Only one bound specified - always valid
                    }

                    try {
                        int minValue = Integer.parseInt(metadata.getMetaAttr(ATTR_MIN_VALUE).getValueAsString());
                        int maxValue = Integer.parseInt(metadata.getMetaAttr(ATTR_MAX_VALUE).getValueAsString());
                        return minValue <= maxValue;
                    } catch (NumberFormatException e) {
                        return false; // Invalid number format
                    }
                }
            );
            constraintRegistry.addConstraint(rangeValidation);

            // Additional validation constraints can be added here
            // (validation of actual values against min/max ranges)
            
            log.debug("Registered IntegerField-specific constraints");
            
        } catch (Exception e) {
            log.error("Failed to register IntegerField constraints", e);
        }
    }

    public static IntegerField create( String name, Integer defaultValue ) {
        IntegerField f = new IntegerField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(StringAttribute.create( ATTR_DEFAULT_VALUE, defaultValue.toString() ));
        }
        return f;
    }
}
