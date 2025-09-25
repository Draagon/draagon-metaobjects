/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.LongAttribute;
import com.draagon.meta.attr.StringAttribute;
// Constraint registration now handled by consolidated MetaDataRegistry
import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.draagon.meta.field.MetaField.TYPE_FIELD;
import static com.draagon.meta.field.MetaField.SUBTYPE_BASE;


/**
 * A Long Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
public class LongField extends PrimitiveField<Long> {

    private static final Logger log = LoggerFactory.getLogger(LongField.class);

    public final static String SUBTYPE_LONG = "long";
    public final static String ATTR_MIN_VALUE = "minValue";
    public final static String ATTR_MAX_VALUE = "maxValue";

    public LongField( String name ) {
        super( SUBTYPE_LONG, name, DataTypes.LONG );
    }

    /**
     * Register LongField type and constraints with the registry
     *
     * @param registry The MetaDataRegistry to register with
     */
    public static void registerTypes(MetaDataRegistry registry) {
        try {
            // Register the type definition
            MetaDataRegistry.getInstance().registerType(LongField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_LONG)
                .description("Long field with numeric validation")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // LONG-SPECIFIC ATTRIBUTES ONLY
                .optionalAttribute(ATTR_MIN_VALUE, LongAttribute.SUBTYPE_LONG)
                .optionalAttribute(ATTR_MAX_VALUE, LongAttribute.SUBTYPE_LONG)
            );

            log.debug("Registered LongField type with unified registry");

            // Register LongField-specific constraints using concrete constraint classes
            registerLongFieldConstraints(registry);

        } catch (Exception e) {
            log.error("Failed to register LongField type with unified registry", e);
        }
    }
    
    /**
     * Register LongField-specific constraints using consolidated registry
     *
     * @param registry The MetaDataRegistry to use for constraint registration
     */
    private static void registerLongFieldConstraints(MetaDataRegistry registry) {
        try {
            // PLACEMENT CONSTRAINT: LongField CAN have minValue attribute
            registry.addConstraint(new PlacementConstraint(
                "longfield.minvalue.placement",
                "LongField can optionally have minValue attribute",
                "field.long",             // Parent pattern
                "attr.*[minValue]",       // Child pattern (allows long or string)
                true                      // Allowed
            ));

            // PLACEMENT CONSTRAINT: LongField CAN have maxValue attribute
            registry.addConstraint(new PlacementConstraint(
                "longfield.maxvalue.placement",
                "LongField can optionally have maxValue attribute",
                "field.long",             // Parent pattern
                "attr.*[maxValue]",       // Child pattern (allows long or string)
                true                      // Allowed
            ));

            log.debug("Registered LongField-specific constraints using consolidated registry");

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
            f.addMetaAttr(StringAttribute.create( ATTR_DEFAULT_VALUE, defaultValue.toString() ));
        }
        return f;
    }
}
