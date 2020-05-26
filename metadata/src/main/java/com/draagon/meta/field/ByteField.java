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
 * A Byte Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class ByteField extends PrimitiveField<Byte> {

    public final static String SUBTYPE_BYTE = "byte";

    public ByteField(String name) {
        super(SUBTYPE_BYTE, name, DataTypes.BYTE);
    }

    /**
     * Manually Create a ByteField
     * @param name Name of the field
     * @param defaultValue Default value for the field
     * @return New ByteField
     */
    public static ByteField create( String name, Integer defaultValue ) {
        ByteField f = new ByteField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(IntAttribute.create( ATTR_DEFAULT_VALUE, defaultValue ));
        }
        return f;
    }
}
