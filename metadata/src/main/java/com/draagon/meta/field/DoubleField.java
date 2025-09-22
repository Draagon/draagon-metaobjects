/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.DoubleAttribute;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.draagon.meta.field.MetaField.TYPE_FIELD;
import static com.draagon.meta.field.MetaField.SUBTYPE_BASE;

/**
 * A Double Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@MetaDataType(type = "field", subType = "double", description = "Double field with numeric and precision validation")
@SuppressWarnings("serial")
public class DoubleField extends PrimitiveField<Double>
{
    private static final Logger log = LoggerFactory.getLogger(DoubleField.class);

    public final static String SUBTYPE_DOUBLE   = "double";
    public final static String ATTR_MIN_VALUE = "minValue";
    public final static String ATTR_MAX_VALUE = "maxValue";
    public final static String ATTR_PRECISION = "precision";
    public final static String ATTR_SCALE = "scale";

    public DoubleField( String name ) {
        super( SUBTYPE_DOUBLE, name, DataTypes.DOUBLE );
    }

    // Unified registry self-registration
    static {
        try {
            // Explicitly trigger MetaField static initialization first
            try {
                Class.forName(MetaField.class.getName());
                // Add a small delay to ensure MetaField registration completes
                Thread.sleep(1);
            } catch (ClassNotFoundException | InterruptedException e) {
                log.warn("Could not force MetaField class loading", e);
            }

            MetaDataRegistry.registerType(DoubleField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_DOUBLE)
                .description("Double field with numeric and precision validation")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // DOUBLE-SPECIFIC ATTRIBUTES ONLY
                .optionalAttribute(ATTR_MIN_VALUE, "double")
                .optionalAttribute(ATTR_MAX_VALUE, "double")
                .optionalAttribute(ATTR_PRECISION, "int")
                .optionalAttribute(ATTR_SCALE, "int")

            );

            log.debug("Registered DoubleField type with unified registry");

            // Register DoubleField-specific constraints
            setupDoubleFieldConstraints();

        } catch (Exception e) {
            log.error("Failed to register DoubleField type with unified registry", e);
        }
    }
    
    /**
     * Setup DoubleField-specific constraints in the constraint registry
     */
    private static void setupDoubleFieldConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();
            
            // PLACEMENT CONSTRAINT: DoubleField CAN have minValue attribute
            PlacementConstraint minValuePlacement = new PlacementConstraint(
                "doublefield.minvalue.placement",
                "DoubleField can optionally have minValue attribute",
                (metadata) -> metadata instanceof DoubleField,
                (child) -> (child instanceof DoubleAttribute || child instanceof StringAttribute) && 
                          child.getName().equals(ATTR_MIN_VALUE)
            );
            constraintRegistry.addConstraint(minValuePlacement);
            
            // PLACEMENT CONSTRAINT: DoubleField CAN have maxValue attribute
            PlacementConstraint maxValuePlacement = new PlacementConstraint(
                "doublefield.maxvalue.placement",
                "DoubleField can optionally have maxValue attribute",
                (metadata) -> metadata instanceof DoubleField,
                (child) -> (child instanceof DoubleAttribute || child instanceof StringAttribute) && 
                          child.getName().equals(ATTR_MAX_VALUE)
            );
            constraintRegistry.addConstraint(maxValuePlacement);
            
            // PLACEMENT CONSTRAINT: DoubleField CAN have precision attribute
            PlacementConstraint precisionPlacement = new PlacementConstraint(
                "doublefield.precision.placement",
                "DoubleField can optionally have precision attribute",
                (metadata) -> metadata instanceof DoubleField,
                (child) -> child instanceof IntAttribute && 
                          child.getName().equals(ATTR_PRECISION)
            );
            constraintRegistry.addConstraint(precisionPlacement);
            
            // PLACEMENT CONSTRAINT: DoubleField CAN have scale attribute
            PlacementConstraint scalePlacement = new PlacementConstraint(
                "doublefield.scale.placement",
                "DoubleField can optionally have scale attribute",
                (metadata) -> metadata instanceof DoubleField,
                (child) -> child instanceof IntAttribute && 
                          child.getName().equals(ATTR_SCALE)
            );
            constraintRegistry.addConstraint(scalePlacement);
            
            log.debug("Registered DoubleField-specific constraints");
            
        } catch (Exception e) {
            log.error("Failed to register DoubleField constraints", e);
        }
    }

    /**
     * Manually Create a DoubleField
     * @param name Name of the field
     * @return New DoubleField
     */
    public static DoubleField create( String name ) {
        DoubleField f = new DoubleField( name );
        return f;
    }
}
