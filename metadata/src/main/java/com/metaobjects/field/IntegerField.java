/*
 * Copyright 2004 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.field;

import com.metaobjects.*;
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
 * An Integer Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class IntegerField extends PrimitiveField<Integer> {

    private static final Logger log = LoggerFactory.getLogger(IntegerField.class);

    public final static String SUBTYPE_INT = "int";
    public final static String ATTR_MIN_VALUE = "minValue";
    public final static String ATTR_MAX_VALUE = "maxValue";

    public IntegerField( String name ) {
        super( SUBTYPE_INT, name, DataTypes.INT );
    }

    /**
     * Register IntegerField type and constraints with the registry
     *
     * @param registry The MetaDataRegistry to register with
     */
    public static void registerTypes(MetaDataRegistry registry) {
        try {
            // Register the type definition
            registry.registerType(IntegerField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_INT)
                .description("Integer field with range validation")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // INTEGER-SPECIFIC ATTRIBUTES ONLY
                .optionalAttribute(ATTR_MIN_VALUE, IntAttribute.SUBTYPE_INT)
                .optionalAttribute(ATTR_MAX_VALUE, IntAttribute.SUBTYPE_INT)
            );

            log.debug("Registered IntegerField type with unified registry (auto-generated constraints)");

        } catch (Exception e) {
            log.error("Failed to register IntegerField type with unified registry", e);
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
