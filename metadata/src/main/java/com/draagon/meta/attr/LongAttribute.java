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
 * A Long Attribute with provider-based registration.
 */
@SuppressWarnings("serial")
public class LongAttribute extends MetaAttribute<Long> {

    public final static String SUBTYPE_LONG = "long";

    /**
     * Register this type with the MetaDataRegistry (called by provider)
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(LongAttribute.class, def -> def
            .type(TYPE_ATTR).subType(SUBTYPE_LONG)
            .description("Long attribute for large integer numeric metadata")
            .inheritsFrom(TYPE_ATTR, SUBTYPE_BASE)
        );
    }

    /**
     * Constructs the Integer MetaAttribute
     */
    public LongAttribute(String name ) {
        super( SUBTYPE_LONG, name, DataTypes.LONG);
    }

    /**
     * Manually create an Integer MetaAttribute with a value
     */
    public static LongAttribute create(String name, Long value ) {
        LongAttribute a = new LongAttribute( name );
        a.setValue( value );
        return a;
    }
}
