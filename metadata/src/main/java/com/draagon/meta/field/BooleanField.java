/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.BooleanAttribute;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
/**
 * A Boolean Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class BooleanField extends PrimitiveField<Boolean> {

    public final static String SUBTYPE_BOOLEAN = "boolean";

    public BooleanField(String name ) {
        super( SUBTYPE_BOOLEAN, name, DataTypes.BOOLEAN );
    }

    /**
     * Manually Create a Boolean Filed
     * @param name Name of the field
     * @param defaultValue Default value for the field
     * @return New BooleanField
     */
    public static BooleanField create( String name, Boolean defaultValue ) {
        BooleanField f = new BooleanField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(BooleanAttribute.create( ATTR_DEFAULT_VALUE, defaultValue ));
        }
        return f;
    }
}
