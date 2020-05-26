/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.IntAttribute;

/**
 * An Integer Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class IntegerField extends PrimitiveField<Integer> {

    public final static String SUBTYPE_INT = "int";

    public IntegerField( String name ) {
        super( SUBTYPE_INT, name, DataTypes.INT );
    }

    public static IntegerField create( String name, Integer defaultValue ) {
        IntegerField f = new IntegerField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(IntAttribute.create( ATTR_DEFAULT_VALUE, defaultValue ));
        }
        return f;
    }
}
