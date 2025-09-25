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
    private static void setupIntAttributeConstraints(com.draagon.meta.registry.MetaDataRegistry registry) {
        // VALIDATION CONSTRAINT: Integer attribute values
        registry.registerValidationConstraint(
            "intattribute.value.validation",
            "IntAttribute values must be valid integers",
            (metadata) -> metadata instanceof IntAttribute,
            (metadata, value) -> {
                if (metadata instanceof IntAttribute) {
                    IntAttribute intAttr = (IntAttribute) metadata;
                    String valueStr = intAttr.getValueAsString();
                    if (valueStr == null || valueStr.isEmpty()) {
                        return true;
                    }
                    try {
                        Integer.parseInt(valueStr);
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                return true;
            }
        );
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
