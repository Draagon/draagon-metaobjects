/*
 * Copyright 2002 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.attr;

import com.metaobjects.DataTypes;
import com.metaobjects.constraint.EnumConstraint;
import com.metaobjects.registry.MetaDataRegistry;
import java.util.Set;

import static com.metaobjects.attr.MetaAttribute.TYPE_ATTR;
import static com.metaobjects.attr.MetaAttribute.SUBTYPE_BASE;

/**
 * A Boolean Attribute with provider-based registration.
 */
@SuppressWarnings("serial")
public class BooleanAttribute extends MetaAttribute<Boolean>
{
    public final static String SUBTYPE_BOOLEAN = "boolean";

    /**
     * Register this type with the MetaDataRegistry (called by provider)
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(BooleanAttribute.class, def -> def
            .type(TYPE_ATTR).subType(SUBTYPE_BOOLEAN)
            .description("Boolean attribute for true/false metadata values")
            .inheritsFrom(TYPE_ATTR, SUBTYPE_BASE)
        );

        // Register BooleanAttribute-specific constraints
        registry.addConstraint(new EnumConstraint(
            "booleanattribute.value.validation",
            "BooleanAttribute values must be valid boolean strings",
            "attr",                     // Target type
            "boolean",                  // Target subtype
            "*",                        // Any name
            Set.of("true", "false"),    // Allowed values
            true,                       // Case insensitive
            true                        // Allow null
        ));
    }

    /**
     * Constructs the Boolean MetaAttribute
     */
    public BooleanAttribute(String name ) {
        super( SUBTYPE_BOOLEAN, name, DataTypes.BOOLEAN);
    }

    /**
     * Manually create a Boolean MetaAttribute with a value
     */
    public static BooleanAttribute create(String name, Boolean value ) {
        BooleanAttribute a = new BooleanAttribute( name );
        a.setValue( value );
        return a;
    }
}
