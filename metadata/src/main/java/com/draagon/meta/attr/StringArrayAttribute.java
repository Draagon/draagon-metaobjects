/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.attr;

import com.draagon.meta.DataTypes;
import com.draagon.meta.constraint.RegexConstraint;
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
        // REGEX CONSTRAINT: String array attribute format (comma-delimited validation)
        registry.addConstraint(new RegexConstraint(
            "stringarrayattribute.format.validation",
            "StringArrayAttribute values must be comma-delimited without leading/trailing/double commas",
            TYPE_ATTR, SUBTYPE_STRING_ARRAY, "*",
            "^(?:[^,]+(?:,[^,]+)*)?$", // Comma-delimited pattern: item1,item2,item3 (no leading/trailing/double commas)
            true // Allow null values
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
