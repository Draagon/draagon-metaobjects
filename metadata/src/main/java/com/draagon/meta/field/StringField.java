/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.registry.MetaDataTypeHandler;

/**
 * A String Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
@MetaDataTypeHandler(type = "field", subType = "string", description = "String field type")
public class StringField extends PrimitiveField<String> {

    public final static String SUBTYPE_STRING   = "string";

    public StringField( String name ) {
        super( SUBTYPE_STRING, name, DataTypes.STRING );
    }

    /**
     * Manually Create a StringField
     * @param name Name of the field
     * @param defaultValue Default value for the field
     * @return New StringField
     */
    public static StringField create( String name, String defaultValue ) {
        StringField f = new StringField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(StringAttribute.create( ATTR_DEFAULT_VALUE, defaultValue ));
        }
        return f;
    }
}
