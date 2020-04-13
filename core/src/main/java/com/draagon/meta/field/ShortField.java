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
 * A Short Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
public class ShortField extends MetaField<Short> {

    public final static String SUBTYPE_SHORT    = "short";

    public ShortField( String name ) {
        super( SUBTYPE_SHORT, name, DataTypes.SHORT );
    }

    /**
     * Manually Create a ByteField
     * @param name Name of the field
     * @param defaultValue Default value for the field
     * @return New ByteField
     */
    public static ShortField create( String name, Integer defaultValue ) {
        ShortField f = new ShortField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(IntAttribute.create( ATTR_DEFAULT_VALUE, defaultValue ));
        }
        return f;
    }
}
