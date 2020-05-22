/*
 * Copyright 2016 Doug Mealing LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.DataTypes;
import com.draagon.meta.attr.StringArrayAttribute;
import com.draagon.meta.io.string.StringSerializationHandler;
import com.draagon.meta.util.DataConverter;

import java.util.List;

/**
 * A String Array Field.
 *
 * @version 1.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class StringArrayField extends ArrayField<List<String>> implements StringSerializationHandler {

    public final static String SUBTYPE_STRING_ARRAY = "stringArray";

    public StringArrayField( String name ) {
        super( SUBTYPE_STRING_ARRAY, name, DataTypes.STRING_ARRAY );
    }

    /**
     * Manually Create a StringArrayField
     * @param name Name of the field
     * @param defaultValue Default value for the field
     * @return New StringArrayField
     */
    public static StringArrayField create( String name, String defaultValue ) {
        StringArrayField f = new StringArrayField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(StringArrayAttribute.create( ATTR_DEFAULT_VALUE, defaultValue ));
        }
        return f;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // String SerializationHandler

    public String getValueAsString(Object o) {
        return DataConverter.toString(getObjectAttribute(o));
    }

    public void setValueAsString(Object o, String val) {
        setObjectAttribute(o, DataConverter.toStringArray( val ));
    }
}
