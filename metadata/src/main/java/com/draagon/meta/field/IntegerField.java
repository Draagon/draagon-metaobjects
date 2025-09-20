/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.registry.MetaDataTypeHandler;
import com.draagon.meta.registry.MetaDataTypeRegistry;
import com.draagon.meta.registry.ServiceRegistryFactory;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.PlacementConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Integer Field with self-registration and constraint setup.
 *
 * @version 2.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
@MetaDataTypeHandler(type = "field", subType = "int", description = "Integer field type")
public class IntegerField extends PrimitiveField<Integer> {

    private static final Logger log = LoggerFactory.getLogger(IntegerField.class);

    public final static String SUBTYPE_INT = "int";
    
    // Attribute name constants for constraints
    public final static String MIN_VALUE_ATTR_NAME = "minValue";
    public final static String MAX_VALUE_ATTR_NAME = "maxValue";

    public IntegerField( String name ) {
        super( SUBTYPE_INT, name, DataTypes.INT );
    }

    // Self-registration with constraint setup
    static {
        try {
            MetaDataTypeRegistry registry = new MetaDataTypeRegistry();
            
            // Register this type handler
            registry.registerHandler(
                new MetaDataTypeId(TYPE_FIELD, SUBTYPE_INT), 
                IntegerField.class
            );
            
            // Set up constraints for this type
            setupIntegerFieldConstraints();
            
            log.debug("Self-registered IntegerField type handler: field.int");
            
        } catch (Exception e) {
            log.error("Failed to register IntegerField type handler", e);
        }
    }

    /**
     * Setup constraints using existing attribute classes following extensible patterns
     */
    private static void setupIntegerFieldConstraints() {
        ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();
        
        // CONSTRAINT 1: IntegerField CAN have minValue attribute (placement constraint)
        PlacementConstraint minValuePlacement = new PlacementConstraint(
            "integerfield.minvalue.placement",
            "IntegerField can optionally have minValue attribute",
            (metadata) -> metadata instanceof IntegerField,
            (child) -> child instanceof IntAttribute && 
                      child.getName().equals(MIN_VALUE_ATTR_NAME)
        );
        constraintRegistry.addConstraint(minValuePlacement);
        
        // CONSTRAINT 2: IntegerField CAN have maxValue attribute (placement constraint)
        PlacementConstraint maxValuePlacement = new PlacementConstraint(
            "integerfield.maxvalue.placement", 
            "IntegerField can optionally have maxValue attribute",
            (metadata) -> metadata instanceof IntegerField,
            (child) -> child instanceof IntAttribute && 
                      child.getName().equals(MAX_VALUE_ATTR_NAME)
        );
        constraintRegistry.addConstraint(maxValuePlacement);
    }

    public static IntegerField create( String name, Integer defaultValue ) {
        IntegerField f = new IntegerField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(IntAttribute.create( ATTR_DEFAULT_VALUE, defaultValue ));
        }
        return f;
    }
}
