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
// Constraint registration now handled by consolidated MetaDataRegistry
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.draagon.meta.field.MetaField.TYPE_FIELD;
import static com.draagon.meta.field.MetaField.SUBTYPE_BASE;

/**
 * An Integer Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class IntegerField extends PrimitiveField<Integer> {

    private static final Logger log = LoggerFactory.getLogger(IntegerField.class);

    public final static String SUBTYPE_INT = "int";
    public final static String ATTR_MIN_VALUE = "minValue";
    public final static String ATTR_MAX_VALUE = "maxValue";

    public IntegerField( String name ) {
        super( SUBTYPE_INT, name, DataTypes.INT );
    }

    /**
     * Register IntegerField type and constraints with the registry
     *
     * @param registry The MetaDataRegistry to register with
     */
    public static void registerTypes(MetaDataRegistry registry) {
        try {
            // Register the type definition
            MetaDataRegistry.getInstance().registerType(IntegerField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_INT)
                .description("Integer field with range validation")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // INTEGER-SPECIFIC ATTRIBUTES ONLY
                .optionalAttribute(ATTR_MIN_VALUE, "int")
                .optionalAttribute(ATTR_MAX_VALUE, "int")
            );

            log.debug("Registered IntegerField type with unified registry");

            // Register IntegerField-specific constraints using concrete constraint classes
            registerIntegerFieldConstraints(registry);

        } catch (Exception e) {
            log.error("Failed to register IntegerField type with unified registry", e);
        }
    }
    
    /**
     * Register IntegerField-specific constraints using consolidated registry
     *
     * @param registry The MetaDataRegistry to use for constraint registration
     */
    private static void registerIntegerFieldConstraints(MetaDataRegistry registry) {
        try {
            // PLACEMENT CONSTRAINT: IntegerField CAN have minValue attribute
            registry.registerPlacementConstraint(
                "integerfield.minvalue.placement",
                "IntegerField can optionally have minValue attribute",
                (metadata) -> metadata instanceof IntegerField,
                (child) -> (child instanceof IntAttribute || child instanceof StringAttribute) &&
                          child.getName().equals(ATTR_MIN_VALUE)
            );

            // PLACEMENT CONSTRAINT: IntegerField CAN have maxValue attribute
            registry.registerPlacementConstraint(
                "integerfield.maxvalue.placement",
                "IntegerField can optionally have maxValue attribute",
                (metadata) -> metadata instanceof IntegerField,
                (child) -> (child instanceof IntAttribute || child instanceof StringAttribute) &&
                          child.getName().equals(ATTR_MAX_VALUE)
            );

            log.debug("Registered IntegerField-specific constraints using consolidated registry");

        } catch (Exception e) {
            log.error("Failed to register IntegerField constraints", e);
        }
    }

    public static IntegerField create( String name, Integer defaultValue ) {
        IntegerField f = new IntegerField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(StringAttribute.create( ATTR_DEFAULT_VALUE, defaultValue.toString() ));
        }
        return f;
    }
}
