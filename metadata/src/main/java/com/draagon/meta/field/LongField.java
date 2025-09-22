/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.attr.LongAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import com.draagon.meta.util.MetaDataConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.draagon.meta.util.MetaDataConstants.TYPE_FIELD;
import static com.draagon.meta.field.MetaField.SUBTYPE_BASE;

/**
 * A Long Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@MetaDataType(type = "field", subType = "long", description = "Long field with numeric validation")
@SuppressWarnings("serial")
public class LongField extends PrimitiveField<Long> {

    private static final Logger log = LoggerFactory.getLogger(LongField.class);

    public final static String SUBTYPE_LONG = "long";
    public final static String ATTR_MIN_VALUE = "minValue";
    public final static String ATTR_MAX_VALUE = "maxValue";

    public LongField( String name ) {
        super( SUBTYPE_LONG, name, DataTypes.LONG );
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

            MetaDataRegistry.registerType(LongField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_LONG)
                .description("Long field with numeric validation")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // LONG-SPECIFIC ATTRIBUTES ONLY
                .optionalAttribute(ATTR_MIN_VALUE, "long")
                .optionalAttribute(ATTR_MAX_VALUE, "long")

                // SERVICE-SPECIFIC ATTRIBUTES (for cross-module compatibility)
                .optionalAttribute(MetaDataConstants.ATTR_IS_ID, "boolean")
                .optionalAttribute(MetaDataConstants.ATTR_DB_COLUMN, "string")
                .optionalAttribute(MetaDataConstants.ATTR_IS_SEARCHABLE, "boolean")
                .optionalAttribute(MetaDataConstants.ATTR_IS_OPTIONAL, "boolean")
            );

            log.debug("Registered LongField type with unified registry");

            // Register LongField-specific constraints
            setupLongFieldConstraints();

        } catch (Exception e) {
            log.error("Failed to register LongField type with unified registry", e);
        }
    }
    
    /**
     * Setup LongField-specific constraints in the constraint registry
     */
    private static void setupLongFieldConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();
            
            // PLACEMENT CONSTRAINT: LongField CAN have minValue attribute
            PlacementConstraint minValuePlacement = new PlacementConstraint(
                "longfield.minvalue.placement",
                "LongField can optionally have minValue attribute",
                (metadata) -> metadata instanceof LongField,
                (child) -> (child instanceof LongAttribute || child instanceof StringAttribute) && 
                          child.getName().equals(ATTR_MIN_VALUE)
            );
            constraintRegistry.addConstraint(minValuePlacement);
            
            // PLACEMENT CONSTRAINT: LongField CAN have maxValue attribute
            PlacementConstraint maxValuePlacement = new PlacementConstraint(
                "longfield.maxvalue.placement",
                "LongField can optionally have maxValue attribute",
                (metadata) -> metadata instanceof LongField,
                (child) -> (child instanceof LongAttribute || child instanceof StringAttribute) && 
                          child.getName().equals(ATTR_MAX_VALUE)
            );
            constraintRegistry.addConstraint(maxValuePlacement);
            
            log.debug("Registered LongField-specific constraints");
            
        } catch (Exception e) {
            log.error("Failed to register LongField constraints", e);
        }
    }

    /**
     * Manually Create a LongField
     * @param name Name of the field
     * @param defaultValue Default value for the field
     * @return New LongField
     */
    public static LongField create( String name, Long defaultValue ) {
        LongField f = new LongField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(StringAttribute.create( MetaDataConstants.ATTR_DEFAULT_VALUE, defaultValue.toString() ));
        }
        return f;
    }
}
