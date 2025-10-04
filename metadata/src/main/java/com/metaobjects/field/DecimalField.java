/*
 * Copyright 2004 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.field;

import com.metaobjects.*;
import com.metaobjects.attr.IntAttribute;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * A Decimal Field for high-precision financial calculations.
 * Uses BigDecimal for exact decimal arithmetic without floating point precision errors.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class DecimalField extends PrimitiveField<BigDecimal> {

    private static final Logger log = LoggerFactory.getLogger(DecimalField.class);

    public static final String SUBTYPE_DECIMAL = "decimal";
    public static final String ATTR_PRECISION = "precision";  // Total number of digits
    public static final String ATTR_SCALE = "scale";          // Number of digits after decimal point
    public static final String ATTR_MIN_VALUE = "minValue";   // Minimum allowed value
    public static final String ATTR_MAX_VALUE = "maxValue";   // Maximum allowed value

    public DecimalField(String name) {
        super(SUBTYPE_DECIMAL, name, DataTypes.DOUBLE); // Use DOUBLE DataType for now, could add DECIMAL later
    }

    /**
     * Register DecimalField type and constraints with the registry
     *
     * @param registry The MetaDataRegistry to register with
     */
    public static void registerTypes(MetaDataRegistry registry) {
        try {
            // Register the type definition
            registry.registerType(DecimalField.class, def -> {
                def.type(TYPE_FIELD).subType(SUBTYPE_DECIMAL)
                   .description("High-precision decimal field for financial calculations")

                   // INHERIT FROM BASE FIELD
                   .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE);

                // DECIMAL-SPECIFIC ATTRIBUTES WITH FLUENT CONSTRAINTS
                def.optionalAttributeWithConstraints(ATTR_PRECISION)
                   .ofType(IntAttribute.SUBTYPE_INT)
                   .asSingle();   // Total digits (e.g., 10)

                def.optionalAttributeWithConstraints(ATTR_SCALE)
                   .ofType(IntAttribute.SUBTYPE_INT)
                   .asSingle();   // Decimal places (e.g., 2)

                def.optionalAttributeWithConstraints(ATTR_MIN_VALUE)
                   .ofType(StringAttribute.SUBTYPE_STRING)
                   .asSingle();   // String to preserve precision

                def.optionalAttributeWithConstraints(ATTR_MAX_VALUE)
                   .ofType(StringAttribute.SUBTYPE_STRING)
                   .asSingle();   // String to preserve precision
            });

            if (log != null) {
                log.debug("Registered DecimalField type with unified registry");
            }

        } catch (Exception e) {
            if (log != null) {
                log.error("Failed to register DecimalField type with unified registry", e);
            }
        }
    }

    /**
     * Get the precision (total number of digits) for this decimal field.
     * @return precision value or default of 18
     */
    public int getPrecision() {
        if (hasMetaAttr(ATTR_PRECISION)) {
            try {
                return Integer.parseInt(getMetaAttr(ATTR_PRECISION).getValueAsString());
            } catch (NumberFormatException e) {
                if (log != null) {
                    log.warn("Invalid precision value for DecimalField {}: {}", getName(),
                            getMetaAttr(ATTR_PRECISION).getValueAsString());
                }
            }
        }
        return 18; // Default precision
    }

    /**
     * Get the scale (number of digits after decimal point) for this decimal field.
     * @return scale value or default of 2
     */
    public int getScale() {
        if (hasMetaAttr(ATTR_SCALE)) {
            try {
                return Integer.parseInt(getMetaAttr(ATTR_SCALE).getValueAsString());
            } catch (NumberFormatException e) {
                if (log != null) {
                    log.warn("Invalid scale value for DecimalField {}: {}", getName(),
                            getMetaAttr(ATTR_SCALE).getValueAsString());
                }
            }
        }
        return 2; // Default scale (cents)
    }

    /**
     * Manually Create a DecimalField with precision and scale
     * @param name Name of the field
     * @param precision Total number of digits
     * @param scale Number of digits after decimal point
     * @return New DecimalField
     */
    public static DecimalField create(String name, int precision, int scale) {
        DecimalField f = new DecimalField(name);
        f.addMetaAttr(IntAttribute.create(ATTR_PRECISION, precision));
        f.addMetaAttr(IntAttribute.create(ATTR_SCALE, scale));
        return f;
    }

    /**
     * Manually Create a DecimalField with default precision/scale
     * @param name Name of the field
     * @return New DecimalField with precision=18, scale=2
     */
    public static DecimalField create(String name) {
        return create(name, 18, 2);
    }
}