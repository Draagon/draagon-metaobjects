/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.attr.LongAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Long Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class LongField extends PrimitiveField<Long> {

    private static final Logger log = LoggerFactory.getLogger(LongField.class);

    public final static String SUBTYPE_LONG = "long";
    public final static String ATTR_MIN_VALUE = "minValue";
    public final static String ATTR_MAX_VALUE = "maxValue";

    public LongField( String name ) {
        super( SUBTYPE_LONG, name, DataTypes.LONG );
    }

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.registerType(LongField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_LONG)
                .description("Long field with numeric validation")
                
                // LONG-SPECIFIC ATTRIBUTES
                .optionalAttribute(ATTR_MIN_VALUE, "long")
                .optionalAttribute(ATTR_MAX_VALUE, "long")
                
                // COMMON FIELD ATTRIBUTES
                .optionalAttribute("isAbstract", "string")
                .optionalAttribute("validation", "string")
                .optionalAttribute("required", "string")
                .optionalAttribute("defaultValue", "string")
                .optionalAttribute("defaultView", "string")
                
                // TEST-SPECIFIC ATTRIBUTES (for codegen tests)
                .optionalAttribute("isId", "boolean")
                .optionalAttribute("dbColumn", "string")
                .optionalAttribute("isSearchable", "boolean")
                .optionalAttribute("isOptional", "boolean")
                
                // ACCEPTS VALIDATORS
                .optionalChild("validator", "*")
                
                // ACCEPTS VIEWS  
                .optionalChild("view", "*")
                
                // ACCEPTS COMMON ATTRIBUTES
                .optionalChild("attr", "string")
                .optionalChild("attr", "int")
                .optionalChild("attr", "boolean")
            );
            
            log.debug("Registered LongField type with unified registry");
            
            // Register LongField-specific constraints
            setupLongFieldConstraints();
            
        } catch (Exception e) {
            log.error("Failed to register LongField type with unified registry", e);
        }
    }
    
    /**
     * Setup LongField-specific constraints in the constraint registry
     */
    private static void setupLongFieldConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();
            
            // PLACEMENT CONSTRAINT: LongField CAN have minValue attribute
            PlacementConstraint minValuePlacement = new PlacementConstraint(
                "longfield.minvalue.placement",
                "LongField can optionally have minValue attribute",
                (metadata) -> metadata instanceof LongField,
                (child) -> (child instanceof LongAttribute || child instanceof StringAttribute) && 
                          child.getName().equals(ATTR_MIN_VALUE)
            );
            constraintRegistry.addConstraint(minValuePlacement);
            
            // PLACEMENT CONSTRAINT: LongField CAN have maxValue attribute
            PlacementConstraint maxValuePlacement = new PlacementConstraint(
                "longfield.maxvalue.placement",
                "LongField can optionally have maxValue attribute",
                (metadata) -> metadata instanceof LongField,
                (child) -> (child instanceof LongAttribute || child instanceof StringAttribute) && 
                          child.getName().equals(ATTR_MAX_VALUE)
            );
            constraintRegistry.addConstraint(maxValuePlacement);
            
            log.debug("Registered LongField-specific constraints");
            
        } catch (Exception e) {
            log.error("Failed to register LongField constraints", e);
        }
    }

    /**
     * Manually Create a LongField
     * @param name Name of the field
     * @param defaultValue Default value for the field
     * @return New LongField
     */
    public static LongField create( String name, Integer defaultValue ) {
        LongField f = new LongField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(StringAttribute.create( ATTR_DEFAULT_VALUE, defaultValue.toString() ));
        }
        return f;
    }
}
