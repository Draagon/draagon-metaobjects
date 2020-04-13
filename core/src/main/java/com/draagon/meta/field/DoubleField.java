/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.field;

import com.draagon.meta.*;

/**
 * A Double Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
public class DoubleField extends MetaField<Double>
{
    public final static String SUBTYPE_DOUBLE   = "double";

    public DoubleField( String name ) {
        super( SUBTYPE_DOUBLE, name, DataTypes.DOUBLE );
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
