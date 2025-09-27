/*
 * Copyright 2002 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.attr;

import com.metaobjects.DataTypes;
import com.metaobjects.constraint.RegexConstraint;
import com.metaobjects.registry.MetaDataRegistry;

import static com.metaobjects.attr.MetaAttribute.TYPE_ATTR;
import static com.metaobjects.attr.MetaAttribute.SUBTYPE_BASE;

/**
 * An Integer Attribute with provider-based registration.
 */
public class IntAttribute extends MetaAttribute<Integer> {

    public final static String SUBTYPE_INT = "int";

    /**
     * Constructs the Integer MetaAttribute
     */
    public IntAttribute(String name ) {
        super( SUBTYPE_INT, name, DataTypes.INT);
    }

    /**
     * Register this type with the MetaDataRegistry (called by provider)
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(IntAttribute.class, def -> def
            .type(TYPE_ATTR).subType(SUBTYPE_INT)
            .description("Integer attribute for numeric metadata values")
            .inheritsFrom(TYPE_ATTR, SUBTYPE_BASE)
        );

        // Register IntAttribute-specific constraints
        setupIntAttributeConstraints(registry);
    }
    
    /**
     * Setup IntAttribute-specific constraints using consolidated registry
     *
     * @param registry The MetaDataRegistry to use for constraint registration
     */
    private static void setupIntAttributeConstraints(com.metaobjects.registry.MetaDataRegistry registry) {
        // VALIDATION CONSTRAINT: Integer attribute values
        registry.addConstraint(new RegexConstraint(
            "intattribute.value.validation",
            "IntAttribute values must be valid integers",
            "attr",                     // Target type
            "int",                      // Integer subtype
            "*",                        // Any name
            "^-?\\d+$",                 // Integer pattern (optional negative sign, digits)
            true                        // Allow null/empty
        ));
    }

    /**
     * Manually create an Integer MetaAttribute with a value
     */
    public static IntAttribute create(String name, Integer value ) {
        IntAttribute a = new IntAttribute( name );
        a.setValue( value );
        return a;
    }
}
