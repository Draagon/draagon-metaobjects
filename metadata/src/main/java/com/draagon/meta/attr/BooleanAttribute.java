/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.attr;

import com.draagon.meta.DataTypes;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Boolean Attribute with unified registry registration.
 *
 * @version 6.0
 */
@SuppressWarnings("serial")
public class BooleanAttribute extends MetaAttribute<Boolean>
{
    private static final Logger log = LoggerFactory.getLogger(BooleanAttribute.class);

    public final static String SUBTYPE_BOOLEAN = "boolean";

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.registerType(BooleanAttribute.class, def -> def
                .type(TYPE_ATTR).subType(SUBTYPE_BOOLEAN)
                .description("Boolean attribute for true/false metadata values")
                
                // Boolean attributes can be placed under any MetaData
                // No specific child requirements
            );
            
            log.debug("Registered BooleanAttribute type with unified registry");
            
            // Register BooleanAttribute-specific constraints
            setupBooleanAttributeConstraints();
            
        } catch (Exception e) {
            log.error("Failed to register BooleanAttribute type with unified registry", e);
        }
    }
    
    /**
     * Setup BooleanAttribute-specific constraints in the constraint registry
     */
    private static void setupBooleanAttributeConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();
            
            // VALIDATION CONSTRAINT: Boolean attribute values
            ValidationConstraint booleanAttributeValidation = new ValidationConstraint(
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
            constraintRegistry.addConstraint(booleanAttributeValidation);
            
            log.debug("Registered BooleanAttribute-specific constraints");
            
        } catch (Exception e) {
            log.error("Failed to register BooleanAttribute constraints", e);
        }
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
