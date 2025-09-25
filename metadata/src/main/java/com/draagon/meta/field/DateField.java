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
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static com.draagon.meta.field.MetaField.TYPE_FIELD;
import static com.draagon.meta.field.MetaField.SUBTYPE_BASE;

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

    // Static registration block - automatically registers when class is loaded
    static {
        try {
            registerTypes(MetaDataRegistry.getInstance());
        } catch (Exception e) {
            log.error("Failed to register DateField type during class loading", e);
        }
    }

    public DateField( String name ) {
        super( SUBTYPE_DATE, name, DataTypes.DATE );
    }

    /**
     * Register DateField type and constraints with the registry
     *
     * @param registry The MetaDataRegistry to register with
     */
    public static void registerTypes(MetaDataRegistry registry) {
        try {
            // Register the type definition
            MetaDataRegistry.getInstance().registerType(DateField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_DATE)
                .description("Date field with format and range validation")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // DATE-SPECIFIC ATTRIBUTES ONLY
                .optionalAttribute(ATTR_DATE_FORMAT, "string")
                .optionalAttribute(ATTR_FORMAT, "string")
                .optionalAttribute(ATTR_MIN_DATE, "string")
                .optionalAttribute(ATTR_MAX_DATE, "string")
            );

            log.debug("Registered DateField type with unified registry");

            // Register DateField-specific constraints using concrete constraint classes
            registerDateFieldConstraints(registry);

        } catch (Exception e) {
            log.error("Failed to register DateField type with unified registry", e);
        }
    }
    
    /**
     * Register DateField-specific constraints using consolidated registry
     *
     * @param registry The MetaDataRegistry to use for constraint registration
     */
    private static void registerDateFieldConstraints(MetaDataRegistry registry) {
        try {
            // PLACEMENT CONSTRAINT: DateField CAN have format attribute
            registry.registerPlacementConstraint(
                "datefield.format.placement",
                "DateField can optionally have format attribute",
                (metadata) -> metadata instanceof DateField,
                (child) -> child instanceof StringAttribute &&
                          child.getName().equals(ATTR_FORMAT)
            );

            log.debug("Registered DateField-specific constraints using consolidated registry");

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
