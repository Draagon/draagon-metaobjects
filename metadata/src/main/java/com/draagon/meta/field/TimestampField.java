/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

import static com.draagon.meta.field.MetaField.TYPE_FIELD;
import static com.draagon.meta.field.MetaField.SUBTYPE_BASE;

/**
 * A Timestamp Field with unified registry registration and child requirements.
 * Extends DateField to provide timestamp-specific functionality.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@MetaDataType(type = "field", subType = "timestamp", description = "Timestamp field with date/time and precision validation")
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

            MetaDataRegistry.registerType(TimestampField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_TIMESTAMP)
                .description("Timestamp field with date/time and precision validation")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // TIMESTAMP-SPECIFIC ATTRIBUTES (using new API)
                .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, ATTR_PRECISION)
                .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_DATE_FORMAT)
                .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_MIN_DATE)
                .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_MAX_DATE)

            );

            log.debug("Registered TimestampField type with unified registry");

            // Register TimestampField-specific validation constraints only
            setupTimestampFieldValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register TimestampField type with unified registry", e);
        }
    }
    
    /**
     * Setup TimestampField-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupTimestampFieldValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALUE VALIDATION CONSTRAINT: Date format validation for timestamps
            ValidationConstraint formatValidation = new ValidationConstraint(
                "timestampfield.format.validation",
                "TimestampField dateFormat attribute must be a valid SimpleDateFormat pattern",
                (metadata) -> metadata instanceof TimestampField && metadata.hasMetaAttr(ATTR_DATE_FORMAT),
                (metadata, value) -> {
                    try {
                        String format = metadata.getMetaAttr(ATTR_DATE_FORMAT).getValueAsString();
                        if (format != null && !format.isEmpty()) {
                            new SimpleDateFormat(format); // Validate format pattern
                        }
                        return true;
                    } catch (IllegalArgumentException e) {
                        return false; // Invalid date format pattern
                    }
                }
            );
            constraintRegistry.addConstraint(formatValidation);

            // VALUE VALIDATION CONSTRAINT: Date range validation for timestamps
            ValidationConstraint rangeValidation = new ValidationConstraint(
                "timestampfield.range.validation",
                "TimestampField minDate must be less than or equal to maxDate",
                (metadata) -> metadata instanceof TimestampField &&
                              (metadata.hasMetaAttr(ATTR_MIN_DATE) || metadata.hasMetaAttr(ATTR_MAX_DATE)),
                (metadata, value) -> {
                    if (!metadata.hasMetaAttr(ATTR_MIN_DATE) || !metadata.hasMetaAttr(ATTR_MAX_DATE)) {
                        return true; // Only one bound specified - always valid
                    }

                    try {
                        String minDateStr = metadata.getMetaAttr(ATTR_MIN_DATE).getValueAsString();
                        String maxDateStr = metadata.getMetaAttr(ATTR_MAX_DATE).getValueAsString();

                        if (minDateStr != null && maxDateStr != null) {
                            // Basic lexicographic comparison for ISO timestamp strings
                            return minDateStr.compareTo(maxDateStr) <= 0;
                        }
                        return true;
                    } catch (Exception e) {
                        return false; // Invalid timestamp format
                    }
                }
            );
            constraintRegistry.addConstraint(rangeValidation);

            // VALUE VALIDATION CONSTRAINT: Precision validation for timestamps
            ValidationConstraint precisionValidation = new ValidationConstraint(
                "timestampfield.precision.validation",
                "TimestampField precision must be a non-negative integer (0-9 for timestamp precision)",
                (metadata) -> metadata instanceof TimestampField && metadata.hasMetaAttr(ATTR_PRECISION),
                (metadata, value) -> {
                    try {
                        int precision = Integer.parseInt(metadata.getMetaAttr(ATTR_PRECISION).getValueAsString());
                        return precision >= 0 && precision <= 9; // Typical timestamp precision range
                    } catch (NumberFormatException e) {
                        return false; // Invalid number format
                    }
                }
            );
            constraintRegistry.addConstraint(precisionValidation);

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