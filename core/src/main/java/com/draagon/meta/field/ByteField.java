/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;

/**
 * A Byte Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class ByteField extends MetaField<Byte> {
    //private static Log log = LogFactory.getLog( ByteField.class );

    public ByteField( String name ) {
        super( SUBTYPE_BYTE, name );
    }

    /**
     * Returns the type of value
     */
    public int getType() {
        return BYTE;
    }

    /**
     * Gets the type of value object class returned
     */
    public Class<?> getValueClass() {
        return Byte.class;
    }
}
