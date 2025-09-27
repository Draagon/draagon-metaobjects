/*
 * Copyright 2004 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.field;

import com.metaobjects.*;
import com.metaobjects.attr.IntAttribute;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.constraint.PlacementConstraint;
import com.metaobjects.registry.MetaDataRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.metaobjects.field.MetaField.TYPE_FIELD;
import static com.metaobjects.field.MetaField.SUBTYPE_BASE;
import static com.metaobjects.attr.MetaAttribute.TYPE_ATTR;

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

    
    /**
     * Register TimestampField type with the registry (called by provider)
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(TimestampField.class, def -> def
            .type(TYPE_FIELD).subType(SUBTYPE_TIMESTAMP)
            .description("Timestamp field with date/time and precision validation")
            .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)
            .optionalAttribute(ATTR_PRECISION, IntAttribute.SUBTYPE_INT)
            .optionalAttribute(ATTR_DATE_FORMAT, StringAttribute.SUBTYPE_STRING)
            .optionalAttribute(ATTR_MIN_DATE, StringAttribute.SUBTYPE_STRING)
            .optionalAttribute(ATTR_MAX_DATE, StringAttribute.SUBTYPE_STRING)
        );

        // Register TimestampField-specific constraints
        setupTimestampFieldConstraints(registry);
    }

    /**
     * Setup TimestampField-specific constraints using consolidated registry
     */
    private static void setupTimestampFieldConstraints(MetaDataRegistry registry) {
        // PLACEMENT CONSTRAINT: TimestampField CAN have precision attribute
        registry.addConstraint(new PlacementConstraint(
            "timestampfield.precision.placement",
            "TimestampField can optionally have precision attribute",
            TYPE_FIELD, SUBTYPE_TIMESTAMP,                    // Parent: field.timestamp
            TYPE_ATTR, IntAttribute.SUBTYPE_INT, "precision", // Child: attr.int[precision]
            true                                              // Allowed
        ));

        // PLACEMENT CONSTRAINT: TimestampField CAN have dateFormat attribute
        registry.addConstraint(new PlacementConstraint(
            "timestampfield.dateformat.placement",
            "TimestampField can optionally have dateFormat attribute",
            TYPE_FIELD, SUBTYPE_TIMESTAMP,                       // Parent: field.timestamp
            TYPE_ATTR, StringAttribute.SUBTYPE_STRING, "dateFormat", // Child: attr.string[dateFormat]
            true                                                 // Allowed
        ));

        // PLACEMENT CONSTRAINT: TimestampField CAN have minDate attribute
        registry.addConstraint(new PlacementConstraint(
            "timestampfield.mindate.placement",
            "TimestampField can optionally have minDate attribute",
            TYPE_FIELD, SUBTYPE_TIMESTAMP,                   // Parent: field.timestamp
            TYPE_ATTR, StringAttribute.SUBTYPE_STRING, "minDate", // Child: attr.string[minDate]
            true                                             // Allowed
        ));

        // PLACEMENT CONSTRAINT: TimestampField CAN have maxDate attribute
        registry.addConstraint(new PlacementConstraint(
            "timestampfield.maxdate.placement",
            "TimestampField can optionally have maxDate attribute",
            TYPE_FIELD, SUBTYPE_TIMESTAMP,                   // Parent: field.timestamp
            TYPE_ATTR, StringAttribute.SUBTYPE_STRING, "maxDate", // Child: attr.string[maxDate]
            true                                             // Allowed
        ));
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
        field.addMetaAttr(com.metaobjects.attr.IntAttribute.create(ATTR_PRECISION, precision));
        return field;
    }
}