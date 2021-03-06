/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;

/**
 * A Float Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class FloatField extends PrimitiveField<Float> {

    public final static String SUBTYPE_FLOAT    = "float";

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
