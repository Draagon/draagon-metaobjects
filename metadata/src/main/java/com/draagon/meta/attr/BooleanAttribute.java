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
        registry.registerValidationConstraint(
            "booleanattribute.value.validation",
            "BooleanAttribute values must be valid boolean strings",
            (metadata) -> metadata instanceof BooleanAttribute,
            (metadata, value) -> {
                if (metadata instanceof BooleanAttribute) {
                    BooleanAttribute boolAttr = (BooleanAttribute) metadata;
                    String valueStr = boolAttr.getValueAsString();
                    return valueStr == null ||
                           "true".equalsIgnoreCase(valueStr) ||
                           "false".equalsIgnoreCase(valueStr);
                }
                return true;
            }
        );
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
