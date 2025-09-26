/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.StringAttribute;
// Constraint registration now handled by consolidated MetaDataRegistry
import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static com.draagon.meta.field.MetaField.TYPE_FIELD;
import static com.draagon.meta.field.MetaField.SUBTYPE_BASE;
import static com.draagon.meta.attr.MetaAttribute.TYPE_ATTR;

/**
 * A Date Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
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

    /**
     * Register DateField type and constraints with the registry (called by provider)
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(DateField.class, def -> def
            .type(TYPE_FIELD).subType(SUBTYPE_DATE)
            .description("Date field with format and range validation")
            .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)
            .optionalAttribute(ATTR_DATE_FORMAT, StringAttribute.SUBTYPE_STRING)
            .optionalAttribute(ATTR_FORMAT, StringAttribute.SUBTYPE_STRING)
            .optionalAttribute(ATTR_MIN_DATE, StringAttribute.SUBTYPE_STRING)
            .optionalAttribute(ATTR_MAX_DATE, StringAttribute.SUBTYPE_STRING)
        );

        // Register DateField-specific constraints
        registerDateFieldConstraints(registry);
    }
    
    /**
     * Register DateField-specific constraints using consolidated registry
     */
    private static void registerDateFieldConstraints(MetaDataRegistry registry) {
        // PLACEMENT CONSTRAINT: DateField CAN have format attribute
        registry.addConstraint(new PlacementConstraint(
            "datefield.format.placement",
            "DateField can optionally have format attribute",
            TYPE_FIELD, SUBTYPE_DATE,                         // Parent: field.date
            TYPE_ATTR, StringAttribute.SUBTYPE_STRING, ATTR_FORMAT, // Child: attr.string[format]
            true                                              // Allowed
        ));

        // PLACEMENT CONSTRAINT: DateField CAN have dateFormat attribute
        registry.addConstraint(new PlacementConstraint(
            "datefield.dateformat.placement",
            "DateField can optionally have dateFormat attribute",
            TYPE_FIELD, SUBTYPE_DATE,                             // Parent: field.date
            TYPE_ATTR, StringAttribute.SUBTYPE_STRING, ATTR_DATE_FORMAT, // Child: attr.string[dateFormat]
            true                                                  // Allowed
        ));

        // PLACEMENT CONSTRAINT: DateField CAN have minDate attribute
        registry.addConstraint(new PlacementConstraint(
            "datefield.mindate.placement",
            "DateField can optionally have minDate attribute",
            TYPE_FIELD, SUBTYPE_DATE,                         // Parent: field.date
            TYPE_ATTR, StringAttribute.SUBTYPE_STRING, ATTR_MIN_DATE, // Child: attr.string[minDate]
            true                                              // Allowed
        ));

        // PLACEMENT CONSTRAINT: DateField CAN have maxDate attribute
        registry.addConstraint(new PlacementConstraint(
            "datefield.maxdate.placement",
            "DateField can optionally have maxDate attribute",
            TYPE_FIELD, SUBTYPE_DATE,                         // Parent: field.date
            TYPE_ATTR, StringAttribute.SUBTYPE_STRING, ATTR_MAX_DATE, // Child: attr.string[maxDate]
            true                                              // Allowed
        ));
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
