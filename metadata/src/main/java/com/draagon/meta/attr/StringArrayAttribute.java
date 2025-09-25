/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.attr;

import com.draagon.meta.DataTypes;
import com.draagon.meta.constraint.CustomConstraint;
import com.draagon.meta.registry.MetaDataRegistry;

import static com.draagon.meta.attr.MetaAttribute.TYPE_ATTR;
import static com.draagon.meta.attr.MetaAttribute.SUBTYPE_BASE;

import java.util.List;

/**
 * A String Array Attribute with provider-based registration.
 */
@SuppressWarnings("serial")
public class StringArrayAttribute extends MetaAttribute<List<String>>
{
    public final static String SUBTYPE_STRING_ARRAY = "stringarray";

    /**
     * Constructs the String Array MetaAttribute
     */
    public StringArrayAttribute(String name ) {
        super( SUBTYPE_STRING_ARRAY, name, DataTypes.STRING_ARRAY);
    }

    /**
     * Register this type with the MetaDataRegistry (called by provider)
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(StringArrayAttribute.class, def -> def
            .type(TYPE_ATTR).subType(SUBTYPE_STRING_ARRAY)
            .description("String array attribute for multiple text values")
            .inheritsFrom(TYPE_ATTR, SUBTYPE_BASE)
        );

        // Register StringArrayAttribute-specific constraints
        setupStringArrayAttributeConstraints(registry);
    }
    
    /**
     * Setup StringArrayAttribute-specific constraints using consolidated registry
     *
     * @param registry The MetaDataRegistry to use for constraint registration
     */
    private static void setupStringArrayAttributeConstraints(MetaDataRegistry registry) {
        // CUSTOM CONSTRAINT: String array attribute format (requires parsing)
        registry.addConstraint(new CustomConstraint(
            "stringarrayattribute.format.validation",
            "StringArrayAttribute values must be properly formatted",
            (metadata) -> metadata instanceof StringArrayAttribute,
            (metadata, value) -> {
                if (metadata instanceof StringArrayAttribute) {
                    StringArrayAttribute arrayAttr = (StringArrayAttribute) metadata;
                    try {
                        // Test if the value can be parsed as a string array
                        arrayAttr.getValue();
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }
                return true;
            },
            "Validates array parsing using getValue()"
        ));
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
