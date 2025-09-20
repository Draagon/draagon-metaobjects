/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.attr;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaDataTypeId;
import com.draagon.meta.registry.MetaDataTypeHandler;
import com.draagon.meta.registry.MetaDataTypeRegistry;
import com.draagon.meta.registry.ServiceRegistryFactory;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A String Attribute with self-registration and constraint setup.
 */
@SuppressWarnings("serial")
@MetaDataTypeHandler(type = "attr", subType = "string", description = "String attribute type")
public class StringAttribute extends MetaAttribute<String> {

    private static final Logger log = LoggerFactory.getLogger(StringAttribute.class);

    public final static String SUBTYPE_STRING = "string";

    /**
     * Constructs the String MetaAttribute
     */
    public StringAttribute(String name ) {
        super( SUBTYPE_STRING, name, DataTypes.STRING);
    }

    // Self-registration for attributes
    static {
        try {
            MetaDataTypeRegistry registry = new MetaDataTypeRegistry();
            
            // Register this type handler  
            registry.registerHandler(
                new MetaDataTypeId(TYPE_ATTR, SUBTYPE_STRING), 
                StringAttribute.class
            );
            
            // Setup constraints for string attributes
            setupStringAttributeConstraints();
            
            log.debug("Self-registered StringAttribute type handler: attr.string");
            
        } catch (Exception e) {
            log.error("Failed to register StringAttribute type handler", e);
        }
    }

    /**
     * Setup constraints using extensible patterns
     */
    private static void setupStringAttributeConstraints() {
        ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();
        
        // Placement constraint - StringAttribute can be placed under any MetaData
        // This shows proper separation of concerns - not trying to "allow" or "disallow"
        PlacementConstraint attributePlacement = new PlacementConstraint(
            "stringattr.placement",
            "StringAttribute can be placed under any MetaData type",
            (parent) -> parent instanceof MetaData, // Any MetaData can have string attributes
            (child) -> child instanceof StringAttribute
        );
        constraintRegistry.addConstraint(attributePlacement);
    }

    /**
     * Manually create a String MetaAttribute with a value
     */
    public static StringAttribute create(String name, String value ) {
        StringAttribute a = new StringAttribute( name );
        a.setValue( value );
        return a;
    }
}
