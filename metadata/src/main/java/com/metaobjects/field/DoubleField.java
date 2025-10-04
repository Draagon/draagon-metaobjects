/*
 * Copyright 2004 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.field;

import com.metaobjects.*;
import com.metaobjects.attr.DoubleAttribute;
import com.metaobjects.attr.IntAttribute;
import com.metaobjects.attr.StringAttribute;
// Constraint registration now handled by consolidated MetaDataRegistry
import com.metaobjects.constraint.PlacementConstraint;
import com.metaobjects.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.metaobjects.field.MetaField.TYPE_FIELD;
import static com.metaobjects.field.MetaField.SUBTYPE_BASE;
import static com.metaobjects.attr.MetaAttribute.TYPE_ATTR;

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
            registry.registerType(DoubleField.class, def -> {
                def.type(TYPE_FIELD).subType(SUBTYPE_DOUBLE)
                   .description("Double field with range validation")

                   // INHERIT FROM BASE FIELD
                   .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE);

                // DOUBLE-SPECIFIC ATTRIBUTES WITH FLUENT CONSTRAINTS
                def.optionalAttributeWithConstraints(ATTR_MIN_VALUE)
                   .ofType(DoubleAttribute.SUBTYPE_DOUBLE)
                   .asSingle();

                def.optionalAttributeWithConstraints(ATTR_MAX_VALUE)
                   .ofType(DoubleAttribute.SUBTYPE_DOUBLE)
                   .asSingle();
            });

            if (log != null) {
                log.debug("Registered DoubleField type with unified registry (auto-generated constraints)");
            }

        } catch (Exception e) {
            if (log != null) {
                log.error("Failed to register DoubleField type with unified registry", e);
            }
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
