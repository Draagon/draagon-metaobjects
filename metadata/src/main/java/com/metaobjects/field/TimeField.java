/*
 * Copyright 2004 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.field;

import com.metaobjects.*;
import com.metaobjects.attr.StringAttribute;
// Constraint registration now handled by consolidated MetaDataRegistry
import com.metaobjects.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * A Time Field for time-only values (hours, minutes, seconds) without date information.
 *
 * <p>Designed for AI-optimized cross-language compatibility, this field maps to:</p>
 * <ul>
 *   <li><strong>Java:</strong> {@code LocalTime}</li>
 *   <li><strong>C#:</strong> {@code TimeOnly} (.NET 6+)</li>
 *   <li><strong>TypeScript:</strong> {@code string} (ISO time format)</li>
 *   <li><strong>SQL:</strong> {@code TIME} type</li>
 * </ul>
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li>Business hours (e.g., "09:00", "17:30")</li>
 *   <li>Appointment scheduling</li>
 *   <li>Daily recurring times</li>
 *   <li>Time-based constraints</li>
 * </ul>
 *
 * <p><strong>Time Attributes:</strong></p>
 * <ul>
 *   <li><strong>format:</strong> Time format pattern (e.g., "HH:mm:ss", "hh:mm a")</li>
 *   <li><strong>minTime:</strong> Minimum allowed time (e.g., "08:00:00")</li>
 *   <li><strong>maxTime:</strong> Maximum allowed time (e.g., "18:00:00")</li>
 * </ul>
 *
 * @version 6.2.6
 * @author AI-Optimized Implementation
 */
@SuppressWarnings("serial")
public class TimeField extends PrimitiveField<LocalTime> {

    private static final Logger log = LoggerFactory.getLogger(TimeField.class);

    // TYPE CONSTANTS
    public static final String SUBTYPE_TIME = "time";

    // TIME-SPECIFIC ATTRIBUTES
    public static final String ATTR_FORMAT = "format";        // Time format pattern (e.g., "HH:mm:ss")
    public static final String ATTR_MIN_TIME = "minTime";     // Minimum time constraint (e.g., "08:00:00")
    public static final String ATTR_MAX_TIME = "maxTime";     // Maximum time constraint (e.g., "18:00:00")

    /**
     * Default constructor for TimeField.
     * @param name The name of the time field
     */
    public TimeField(String name) {
        super(SUBTYPE_TIME, name, DataTypes.CUSTOM);
    }

    /**
     * Register TimeField type and constraints with the registry.
     * Called by FieldTypesMetaDataProvider during system initialization.
     */
    public static void registerTypes(MetaDataRegistry registry) {
        try {
            registry.registerType(TimeField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_TIME)
                .description("Time field for time-only values (hours, minutes, seconds)")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // TIME-SPECIFIC ATTRIBUTES ONLY
                .optionalAttribute(ATTR_FORMAT, StringAttribute.SUBTYPE_STRING)     // Time format pattern
                .optionalAttribute(ATTR_MIN_TIME, StringAttribute.SUBTYPE_STRING)   // Minimum time constraint
                .optionalAttribute(ATTR_MAX_TIME, StringAttribute.SUBTYPE_STRING)   // Maximum time constraint
            );

            if (log != null) {
                log.debug("Registered TimeField type with unified registry (auto-generated constraints)");
            }

        } catch (Exception e) {
            if (log != null) {
                log.error("Failed to register TimeField type with unified registry", e);
            }
        }
    }

    /**
     * Get the time format pattern for this field.
     * @return format pattern or default "HH:mm:ss"
     */
    public String getFormat() {
        if (hasMetaAttr(ATTR_FORMAT)) {
            return getMetaAttr(ATTR_FORMAT).getValueAsString();
        }
        return "HH:mm:ss"; // Default format with seconds
    }

    /**
     * Get the minimum allowed time for this field.
     * @return minimum time as string or null if not set
     */
    public String getMinTime() {
        if (hasMetaAttr(ATTR_MIN_TIME)) {
            return getMetaAttr(ATTR_MIN_TIME).getValueAsString();
        }
        return null;
    }

    /**
     * Get the maximum allowed time for this field.
     * @return maximum time as string or null if not set
     */
    public String getMaxTime() {
        if (hasMetaAttr(ATTR_MAX_TIME)) {
            return getMetaAttr(ATTR_MAX_TIME).getValueAsString();
        }
        return null;
    }

    /**
     * Validate a time value against this field's constraints.
     * @param timeValue the time value to validate (as string)
     * @return true if valid, false otherwise
     */
    public boolean isValidTime(String timeValue) {
        if (timeValue == null || timeValue.trim().isEmpty()) {
            return false;
        }

        try {
            // Parse the time using the field's format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(getFormat());
            LocalTime time = LocalTime.parse(timeValue, formatter);

            // Check minimum time constraint
            String minTime = getMinTime();
            if (minTime != null && !minTime.isEmpty()) {
                LocalTime minLocalTime = LocalTime.parse(minTime, formatter);
                if (time.isBefore(minLocalTime)) {
                    return false;
                }
            }

            // Check maximum time constraint
            String maxTime = getMaxTime();
            if (maxTime != null && !maxTime.isEmpty()) {
                LocalTime maxLocalTime = LocalTime.parse(maxTime, formatter);
                if (time.isAfter(maxLocalTime)) {
                    return false;
                }
            }

            return true;

        } catch (DateTimeParseException e) {
            if (log != null) {
                log.warn("Invalid time format for TimeField {}: {} (expected format: {})",
                        getName(), timeValue, getFormat());
            }
            return false;
        }
    }

    /**
     * Manually create a TimeField with default value.
     * @param name Name of the field
     * @param defaultValue Default time value
     * @return configured TimeField
     */
    public static TimeField create(String name, String defaultValue) {
        TimeField field = new TimeField(name);
        if (defaultValue != null && !defaultValue.trim().isEmpty()) {
            field.addMetaAttr(StringAttribute.create(ATTR_DEFAULT_VALUE, defaultValue));
        }
        return field;
    }

    /**
     * Manually create a TimeField with format and constraints.
     * @param name Name of the field
     * @param format Time format pattern
     * @param minTime Minimum time constraint (optional)
     * @param maxTime Maximum time constraint (optional)
     * @return configured TimeField
     */
    public static TimeField create(String name, String format, String minTime, String maxTime) {
        TimeField field = new TimeField(name);

        if (format != null && !format.trim().isEmpty()) {
            field.addMetaAttr(StringAttribute.create(ATTR_FORMAT, format));
        }

        if (minTime != null && !minTime.trim().isEmpty()) {
            field.addMetaAttr(StringAttribute.create(ATTR_MIN_TIME, minTime));
        }

        if (maxTime != null && !maxTime.trim().isEmpty()) {
            field.addMetaAttr(StringAttribute.create(ATTR_MAX_TIME, maxTime));
        }

        return field;
    }
}