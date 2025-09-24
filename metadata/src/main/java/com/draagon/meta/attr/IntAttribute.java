/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.attr;

import com.draagon.meta.DataTypes;
import com.draagon.meta.registry.MetaDataType;
// Constraint registration now handled by consolidated MetaDataRegistry
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Integer Attribute with self-registration and constraint setup.
 */
@MetaDataType(type = "attr", subType = "int", description = "Integer attribute type")
public class IntAttribute extends MetaAttribute<Integer> {

    private static final Logger log = LoggerFactory.getLogger(IntAttribute.class);

    public final static String SUBTYPE_INT = "int";

    /**
     * Constructs the Integer MetaAttribute
     */
    public IntAttribute(String name ) {
        super( SUBTYPE_INT, name, DataTypes.INT);
    }

    // Unified registry self-registration
    static {
        try {
            com.draagon.meta.registry.MetaDataRegistry.registerType(IntAttribute.class, def -> def
                .type(TYPE_ATTR).subType(SUBTYPE_INT)
                .description("Integer attribute for numeric metadata values")
                
                // Integer attributes can be placed under any MetaData
                // No specific child requirements
            );
            
            log.debug("Registered IntAttribute type with unified registry");
            
            // Register IntAttribute-specific constraints using consolidated registry
            setupIntAttributeConstraints(com.draagon.meta.registry.MetaDataRegistry.getInstance());
            
        } catch (Exception e) {
            log.error("Failed to register IntAttribute type with unified registry", e);
        }
    }
    
    /**
     * Setup IntAttribute-specific constraints using consolidated registry
     *
     * @param registry The MetaDataRegistry to use for constraint registration
     */
    private static void setupIntAttributeConstraints(com.draagon.meta.registry.MetaDataRegistry registry) {
        try {
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

            log.debug("Registered IntAttribute-specific constraints using consolidated registry");

        } catch (Exception e) {
            log.error("Failed to register IntAttribute constraints", e);
        }
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
