/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.attr;

import com.draagon.meta.DataTypes;
import com.draagon.meta.registry.MetaDataRegistry;

import static com.draagon.meta.attr.MetaAttribute.TYPE_ATTR;
import static com.draagon.meta.attr.MetaAttribute.SUBTYPE_BASE;

/**
 * A String Attribute with provider-based registration.
 */
public class StringAttribute extends MetaAttribute<String> {

    public final static String SUBTYPE_STRING = "string";

    /**
     * Constructs the String MetaAttribute
     */
    public StringAttribute(String name ) {
        super( SUBTYPE_STRING, name, DataTypes.STRING);
    }

    /**
     * Register this type with the MetaDataRegistry (called by provider)
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(StringAttribute.class, def -> def
            .type(TYPE_ATTR).subType(SUBTYPE_STRING)
            .description("String attribute value")
            .inheritsFrom(TYPE_ATTR, SUBTYPE_BASE)
        );
    }

    /**
     * Manually create a String MetaAttribute with a value
     */
    public static StringAttribute create(String name, String value ) {
        StringAttribute a = new StringAttribute( name );
        a.setValue( value );
        return a;
    }
}
