/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.IntAttribute;
// Constraint registration now handled by consolidated MetaDataRegistry
import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Timestamp Field with unified registry registration and child requirements.
 * Extends DateField to provide timestamp-specific functionality.
 *
 * @version 6.0
 * @author Doug Mealing
 */
public class TimestampField extends PrimitiveField<java.util.Date> {

    private static final Logger log = LoggerFactory.getLogger(TimestampField.class);

    public final static String SUBTYPE_TIMESTAMP = "timestamp";
    public final static String ATTR_PRECISION = "precision";
    public final static String ATTR_DATE_FORMAT = "dateFormat";
    public final static String ATTR_MIN_DATE = "minDate";
    public final static String ATTR_MAX_DATE = "maxDate";

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

            MetaDataRegistry.getInstance().registerType(TimestampField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_TIMESTAMP)
                .description("Timestamp field with date/time and precision validation")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // TIMESTAMP-SPECIFIC ATTRIBUTES ONLY
                .optionalAttribute(ATTR_PRECISION, "int")
                .optionalAttribute(ATTR_DATE_FORMAT, "string")
                .optionalAttribute(ATTR_MIN_DATE, "string")
                .optionalAttribute(ATTR_MAX_DATE, "string")

            );

            log.debug("Registered TimestampField type with unified registry");

            // Register TimestampField-specific constraints using consolidated registry
            setupTimestampFieldConstraints(MetaDataRegistry.getInstance());

        } catch (Exception e) {
            log.error("Failed to register TimestampField type with unified registry", e);
        }
    }
    
    /**
     * Setup TimestampField-specific constraints using consolidated registry
     *
     * @param registry The MetaDataRegistry to use for constraint registration
     */
    private static void setupTimestampFieldConstraints(MetaDataRegistry registry) {
        try {
            // PLACEMENT CONSTRAINT: TimestampField CAN have precision attribute
            registry.addConstraint(new PlacementConstraint(
                "timestampfield.precision.placement",
                "TimestampField can optionally have precision attribute",
                "field.timestamp",        // Parent pattern
                "attr.int[precision]",    // Child pattern
                true                      // Allowed
            ));

            log.debug("Registered TimestampField-specific constraints using consolidated registry");

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