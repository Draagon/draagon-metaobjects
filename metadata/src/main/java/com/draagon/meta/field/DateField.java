/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.draagon.meta.field.MetaField.TYPE_FIELD;
import static com.draagon.meta.field.MetaField.SUBTYPE_BASE;

/**
 * A Date Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@MetaDataType(type = "field", subType = "date", description = "Date field with format and range validation")
@SuppressWarnings("serial")
public class DateField extends PrimitiveField<Date> {

    private static final Logger log = LoggerFactory.getLogger(DateField.class);

    public final static String SUBTYPE_DATE     = "date";
    public final static String ATTR_DATE_FORMAT = "dateFormat";
    public final static String ATTR_FORMAT = "format";
    public final static String ATTR_MIN_DATE = "minDate";
    public final static String ATTR_MAX_DATE = "maxDate";

    public DateField( String name ) {
        super( SUBTYPE_DATE, name, DataTypes.DATE );
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

            MetaDataRegistry.registerType(DateField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_DATE)
                .description("Date field with format and range validation")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // DATE-SPECIFIC ATTRIBUTES (using new API)
                .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_DATE_FORMAT)
                .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_FORMAT)
                .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_MIN_DATE)
                .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_MAX_DATE)

            );

            log.debug("Registered DateField type with unified registry");

            // Register DateField-specific validation constraints only
            setupDateFieldValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register DateField type with unified registry", e);
        }
    }
    
    /**
     * Setup DateField-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupDateFieldValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALUE VALIDATION CONSTRAINT: Date format validation
            ValidationConstraint formatValidation = new ValidationConstraint(
                "datefield.format.validation",
                "DateField format attribute must be a valid SimpleDateFormat pattern",
                (metadata) -> metadata instanceof DateField && metadata.hasMetaAttr(ATTR_FORMAT),
                (metadata, value) -> {
                    try {
                        String format = metadata.getMetaAttr(ATTR_FORMAT).getValueAsString();
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

            // VALUE VALIDATION CONSTRAINT: Date range validation
            ValidationConstraint rangeValidation = new ValidationConstraint(
                "datefield.range.validation",
                "DateField minDate must be less than or equal to maxDate",
                (metadata) -> metadata instanceof DateField &&
                              (metadata.hasMetaAttr(ATTR_MIN_DATE) || metadata.hasMetaAttr(ATTR_MAX_DATE)),
                (metadata, value) -> {
                    if (!metadata.hasMetaAttr(ATTR_MIN_DATE) || !metadata.hasMetaAttr(ATTR_MAX_DATE)) {
                        return true; // Only one bound specified - always valid
                    }

                    try {
                        String minDateStr = metadata.getMetaAttr(ATTR_MIN_DATE).getValueAsString();
                        String maxDateStr = metadata.getMetaAttr(ATTR_MAX_DATE).getValueAsString();

                        if (minDateStr != null && maxDateStr != null) {
                            // Basic lexicographic comparison for ISO date strings
                            return minDateStr.compareTo(maxDateStr) <= 0;
                        }
                        return true;
                    } catch (Exception e) {
                        return false; // Invalid date format
                    }
                }
            );
            constraintRegistry.addConstraint(rangeValidation);

            log.debug("Registered DateField-specific constraints");

        } catch (Exception e) {
            log.error("Failed to register DateField constraints", e);
        }
    }

    /**
     * Manually Create a Date Filed
     * @param name Name of the field
     * @return New DateField
     */
    public static DateField create( String name ) {
        DateField f = new DateField( name );
        return f;
    }
}
