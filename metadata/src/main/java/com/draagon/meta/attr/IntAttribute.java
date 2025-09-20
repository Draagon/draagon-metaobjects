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
 * An Integer Attribute with self-registration and constraint setup.
 */
@SuppressWarnings("serial")
@MetaDataTypeHandler(type = "attr", subType = "int", description = "Integer attribute type")
public class IntAttribute extends MetaAttribute<Integer> {

    private static final Logger log = LoggerFactory.getLogger(IntAttribute.class);

    public final static String SUBTYPE_INT = "int";

    /**
     * Constructs the Integer MetaAttribute
     */
    public IntAttribute(String name ) {
        super( SUBTYPE_INT, name, DataTypes.INT);
    }

    // Self-registration for int attributes
    static {
        try {
            MetaDataTypeRegistry registry = new MetaDataTypeRegistry();
            
            // Register this type handler
            registry.registerHandler(
                new MetaDataTypeId(TYPE_ATTR, SUBTYPE_INT),
                IntAttribute.class
            );
            
            // Setup constraints for int attributes
            setupIntAttributeConstraints();
            
            log.debug("Self-registered IntAttribute type handler: attr.int");
            
        } catch (Exception e) {
            log.error("Failed to register IntAttribute type handler", e);
        }
    }

    /**
     * Setup constraints using extensible patterns
     */
    private static void setupIntAttributeConstraints() {
        ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();
        
        // Placement constraint - IntAttribute can be placed under any MetaData
        PlacementConstraint attributePlacement = new PlacementConstraint(
            "intattr.placement",
            "IntAttribute can be placed under any MetaData type",
            (parent) -> parent instanceof MetaData, // Any MetaData can have int attributes
            (child) -> child instanceof IntAttribute
        );
        constraintRegistry.addConstraint(attributePlacement);
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
