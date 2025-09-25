/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.draagon.meta.field.MetaField.TYPE_FIELD;
import static com.draagon.meta.field.MetaField.SUBTYPE_BASE;

/**
 * A Float Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class FloatField extends PrimitiveField<Float>
{
    private static final Logger log = LoggerFactory.getLogger(FloatField.class);

    public final static String SUBTYPE_FLOAT = "float";
    public final static String ATTR_MIN_VALUE = "minValue";
    public final static String ATTR_MAX_VALUE = "maxValue";
    public final static String ATTR_PRECISION = "precision";

    public FloatField( String name ) {
        super( SUBTYPE_FLOAT, name, DataTypes.FLOAT );
    }

    /**
     * Register FloatField type using the standardized registerTypes() pattern.
     */
    public static void registerTypes(MetaDataRegistry registry) {
        try {
            MetaDataRegistry.getInstance().registerType(FloatField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_FLOAT)
                .description("Float field with numeric and precision validation")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // FLOAT-SPECIFIC ATTRIBUTES ONLY
                .optionalAttribute(ATTR_MIN_VALUE, "float")
                .optionalAttribute(ATTR_MAX_VALUE, "float")
                .optionalAttribute(ATTR_PRECISION, "int")

            );

            log.debug("Registered FloatField type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register FloatField type with unified registry", e);
        }
    }


    /**
     * Manually Create a FloatField
     * @param name Name of the field
     * @return New FloatField
     */
    public static FloatField create( String name ) {
        FloatField f = new FloatField( name );
        return f;
    }
}
