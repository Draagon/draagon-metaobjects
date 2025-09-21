/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;

/**
 * A Timestamp Field with unified registry registration and child requirements.
 * Extends DateField to provide timestamp-specific functionality.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class TimestampField extends PrimitiveField<java.util.Date> {

    private static final Logger log = LoggerFactory.getLogger(TimestampField.class);

    public final static String TYPE_FIELD = "field";
    public final static String SUBTYPE_TIMESTAMP = "timestamp";
    public final static String ATTR_PRECISION = "precision";
    public final static String ATTR_DATE_FORMAT = "dateFormat";
    public final static String ATTR_MIN_DATE = "minDate";
    public final static String ATTR_MAX_DATE = "maxDate";

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.registerType(TimestampField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_TIMESTAMP)
                .description("Timestamp field with date/time and precision validation")
                
                // TIMESTAMP-SPECIFIC ATTRIBUTES
                .optionalAttribute(ATTR_PRECISION, "int")
                .optionalAttribute(ATTR_DATE_FORMAT, "string")
                .optionalAttribute(ATTR_MIN_DATE, "string")
                .optionalAttribute(ATTR_MAX_DATE, "string")
                
                // COMMON FIELD ATTRIBUTES
                .optionalAttribute("isAbstract", "string")
                .optionalAttribute("validation", "string")
                .optionalAttribute("required", "string")
                .optionalAttribute("defaultValue", "string")
                .optionalAttribute("defaultView", "string")
                
                // ACCEPTS VALIDATORS
                .optionalChild("validator", "*")
                
                // ACCEPTS COMMON ATTRIBUTES
                .optionalChild("attr", "string")
                .optionalChild("attr", "int")
                .optionalChild("attr", "boolean")
            );
            
            log.debug("Registered TimestampField type with unified registry");
            
            // Register TimestampField-specific constraints
            setupTimestampFieldConstraints();
            
        } catch (Exception e) {
            log.error("Failed to register TimestampField type with unified registry", e);
        }
    }
    
    /**
     * Setup TimestampField-specific constraints in the constraint registry
     */
    private static void setupTimestampFieldConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();
            
            // PLACEMENT CONSTRAINT: TimestampField CAN have precision attribute
            PlacementConstraint timestampPrecisionPlacement = new PlacementConstraint(
                "timestampfield.precision.placement",
                "TimestampField can optionally have precision attribute",
                (metadata) -> metadata instanceof TimestampField,
                (child) -> child instanceof IntAttribute && 
                          child.getName().equals(ATTR_PRECISION)
            );
            constraintRegistry.addConstraint(timestampPrecisionPlacement);
            
            log.debug("Registered TimestampField-specific constraints");
            
        } catch (Exception e) {
            log.error("Failed to register TimestampField constraints", e);
        }
    }

    public TimestampField(String name) {
        super(SUBTYPE_TIMESTAMP, name, DataTypes.DATE);  // Use DataTypes.DATE since timestamps are date-based
    }

    /**
     * Manually Create a TimestampField
     * @param name Name of the field
     * @return New TimestampField
     */
    public static TimestampField create(String name) {
        return new TimestampField(name);
    }

    /**
     * Create a TimestampField with a default precision
     * @param name Name of the field
     * @param precision Timestamp precision (e.g., 3 for milliseconds)
     * @return New TimestampField
     */
    public static TimestampField create(String name, int precision) {
        TimestampField field = new TimestampField(name);
        field.addMetaAttr(com.draagon.meta.attr.IntAttribute.create(ATTR_PRECISION, precision));
        return field;
    }
}