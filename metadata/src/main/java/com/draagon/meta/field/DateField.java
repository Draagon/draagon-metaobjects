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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


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

        log.debug("Registered DateField type with unified registry (auto-generated constraints)");
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
