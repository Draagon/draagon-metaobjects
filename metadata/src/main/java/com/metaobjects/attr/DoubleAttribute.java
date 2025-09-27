/*
 * Copyright 2002 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.attr;

import com.metaobjects.DataTypes;
import com.metaobjects.registry.MetaDataRegistry;

import static com.metaobjects.attr.MetaAttribute.TYPE_ATTR;
import static com.metaobjects.attr.MetaAttribute.SUBTYPE_BASE;

/**
 * A Double Attribute with provider-based registration.
 */
@SuppressWarnings("serial")
public class DoubleAttribute extends MetaAttribute<Double> {

    public final static String SUBTYPE_DOUBLE = "double";

    /**
     * Register this type with the MetaDataRegistry (called by provider)
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(DoubleAttribute.class, def -> def
            .type(TYPE_ATTR).subType(SUBTYPE_DOUBLE)
            .description("Double attribute for floating-point numeric metadata")
            .inheritsFrom(TYPE_ATTR, SUBTYPE_BASE)
        );
    }

    /**
     * Constructs the Double MetaAttribute
     */
    public DoubleAttribute(String name) {
        super(SUBTYPE_DOUBLE, name, DataTypes.DOUBLE);
    }

    /**
     * Manually create a Double MetaAttribute with a value
     */
    public static DoubleAttribute create(String name, Double value) {
        DoubleAttribute a = new DoubleAttribute(name);
        a.setValue(value);
        return a;
    }
}