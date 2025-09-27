/*
 * Copyright 2004 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.field;

import com.metaobjects.*;
import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.attr.IntAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.metaobjects.field.MetaField.TYPE_FIELD;
import static com.metaobjects.field.MetaField.SUBTYPE_BASE;

/**
 * A Float Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class FloatField extends PrimitiveField<Float>
{
    private static final Logger log = LoggerFactory.getLogger(FloatField.class);

    public final static String SUBTYPE_FLOAT = "float";
    public final static String ATTR_MIN_VALUE = "minValue";
    public final static String ATTR_MAX_VALUE = "maxValue";
    public final static String ATTR_PRECISION = "precision";

    public FloatField( String name ) {
        super( SUBTYPE_FLOAT, name, DataTypes.FLOAT );
    }

    /**
     * Register FloatField type using the standardized registerTypes() pattern.
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(FloatField.class, def -> def
            .type(TYPE_FIELD).subType(SUBTYPE_FLOAT)
            .description("Float field with numeric and precision validation")
            .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)
            .optionalAttribute(ATTR_MIN_VALUE, SUBTYPE_FLOAT)
            .optionalAttribute(ATTR_MAX_VALUE, SUBTYPE_FLOAT)
            .optionalAttribute(ATTR_PRECISION, IntAttribute.SUBTYPE_INT)
        );
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
