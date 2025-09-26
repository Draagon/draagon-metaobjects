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
// Constraint registration now handled by consolidated MetaDataRegistry
import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.draagon.meta.field.MetaField.TYPE_FIELD;
import static com.draagon.meta.field.MetaField.SUBTYPE_BASE;
import static com.draagon.meta.attr.MetaAttribute.TYPE_ATTR;

/**
 * A Double Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
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

    /**
     * Register DoubleField type and constraints with the registry
     *
     * @param registry The MetaDataRegistry to register with
     */
    public static void registerTypes(MetaDataRegistry registry) {
        try {
            // Register the type definition
            registry.registerType(DoubleField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_DOUBLE)
                .description("Double field with numeric and precision validation")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // DOUBLE-SPECIFIC ATTRIBUTES ONLY
                .optionalAttribute(ATTR_MIN_VALUE, "double")
                .optionalAttribute(ATTR_MAX_VALUE, "double")
                .optionalAttribute(ATTR_PRECISION, "int")
                .optionalAttribute(ATTR_SCALE, "int")
            );

            log.debug("Registered DoubleField type with unified registry (auto-generated constraints)");

        } catch (Exception e) {
            log.error("Failed to register DoubleField type with unified registry", e);
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
