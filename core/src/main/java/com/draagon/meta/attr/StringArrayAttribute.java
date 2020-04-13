/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.attr;

import com.draagon.meta.DataTypes;

import java.util.List;

/**
 * An attribute of a MetaClass, MetaField, or MetaView
 */
@SuppressWarnings("serial")
public class StringArrayAttribute extends MetaAttribute<List<String>> {

    public final static String SUBTYPE_STRING_ARRAY = "stringArray";

    /**
     * Constructs the String Array MetaAttribute
     */
    public StringArrayAttribute(String name ) {
        super( SUBTYPE_STRING_ARRAY, name, DataTypes.STRING_ARRAY);
    }


    /**
     * Manually create a StringArray MetaAttribute with a value
     */
    public static StringArrayAttribute create(String name, String value ) {
        StringArrayAttribute a = new StringArrayAttribute( name );
        a.setValueAsString( value );
        return a;
    }
}
