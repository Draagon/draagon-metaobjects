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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
     * Universal @isArray support - handles both single booleans and boolean arrays
     */
    @Override
    public void setValueAsString(String value) {
        if (isArrayType()) {
            // Array mode: Parse comma-delimited format for booleans
            if (value == null) {
                setValueAsObject(null);
                return;
            }

            // Empty string should result in empty list, not null
            if (value.trim().isEmpty()) {
                setValueAsObject(new ArrayList<>());
                return;
            }

            // Parse comma-delimited format: "true,false,true" or single value "true"
            if (value.contains(",")) {
                // Comma-delimited: true,false,true
                String[] items = value.split(",");
                List<Boolean> list = new ArrayList<>();
                for (String item : items) {
                    String trimmed = item.trim();
                    if (!trimmed.isEmpty()) {
                        list.add(Boolean.parseBoolean(trimmed));
                    }
                }
                setValueAsObject(list);
            } else {
                // Single value
                Boolean boolValue = Boolean.parseBoolean(value.trim());
                setValueAsObject(Arrays.asList(boolValue));
            }
        } else {
            // Single value mode: Standard boolean handling
            if (value == null) {
                setValueAsObject(null);
            } else {
                setValueAsObject(Boolean.parseBoolean(value.trim()));
            }
        }
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
