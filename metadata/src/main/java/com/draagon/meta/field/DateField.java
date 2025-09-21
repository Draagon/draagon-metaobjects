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
import com.draagon.meta.constraint.PlacementConstraint;
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

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.registerType(DateField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_DATE)
                .description("Date field with format and range validation")
                
                // DATE-SPECIFIC ATTRIBUTES
                .optionalAttribute(ATTR_DATE_FORMAT, "string")
                .optionalAttribute(ATTR_FORMAT, "string")
                .optionalAttribute(ATTR_MIN_DATE, "string")
                .optionalAttribute(ATTR_MAX_DATE, "string")
                
                // COMMON FIELD ATTRIBUTES
                .optionalAttribute("isAbstract", "string")
                .optionalAttribute("validation", "string")
                .optionalAttribute("required", "string")
                .optionalAttribute("defaultValue", "string")
                .optionalAttribute("defaultView", "string")
                
                // TEST-SPECIFIC ATTRIBUTES (for codegen tests)
                .optionalAttribute("isId", "boolean")
                .optionalAttribute("dbColumn", "string")
                .optionalAttribute("isSearchable", "boolean")
                .optionalAttribute("isOptional", "boolean")
                
                // ACCEPTS VALIDATORS
                .optionalChild("validator", "*")
                
                // ACCEPTS COMMON ATTRIBUTES
                .optionalChild("attr", "string")
                .optionalChild("attr", "int")
                .optionalChild("attr", "boolean")
            );
            
            log.debug("Registered DateField type with unified registry");
            
            // Register DateField-specific constraints
            setupDateFieldConstraints();
            
        } catch (Exception e) {
            log.error("Failed to register DateField type with unified registry", e);
        }
    }
    
    /**
     * Setup DateField-specific constraints in the constraint registry
     */
    private static void setupDateFieldConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();
            
            // PLACEMENT CONSTRAINT: DateField CAN have format attribute
            PlacementConstraint dateFormatPlacement = new PlacementConstraint(
                "datefield.format.placement",
                "DateField can optionally have format attribute",
                (metadata) -> metadata instanceof DateField,
                (child) -> child instanceof StringAttribute && 
                          child.getName().equals(ATTR_FORMAT)
            );
            constraintRegistry.addConstraint(dateFormatPlacement);
            
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
