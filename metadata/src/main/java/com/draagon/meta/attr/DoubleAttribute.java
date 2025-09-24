/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.attr;

import com.draagon.meta.DataTypes;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Double Attribute with unified registry registration and parent acceptance.
 *
 * @version 6.2
 */
@MetaDataType(type = "attr", subType = "double", description = "Double attribute for floating-point numeric metadata")
@SuppressWarnings("serial")
public class DoubleAttribute extends MetaAttribute<Double> {

    private static final Logger log = LoggerFactory.getLogger(DoubleAttribute.class);

    public final static String SUBTYPE_DOUBLE = "double";

    // Unified registry self-registration
    static {
        try {
            // Explicitly trigger MetaAttribute static initialization first
            try {
                Class.forName(MetaAttribute.class.getName());
                // Add a small delay to ensure MetaAttribute registration completes
                Thread.sleep(1);
            } catch (ClassNotFoundException | InterruptedException e) {
                log.warn("Could not force MetaAttribute class loading", e);
            }

            MetaDataRegistry.registerType(DoubleAttribute.class, def -> def
                .type(TYPE_ATTR).subType(SUBTYPE_DOUBLE)
                .description("Double attribute for floating-point numeric metadata")

                // INHERIT FROM BASE ATTRIBUTE
                .inheritsFrom(TYPE_ATTR, SUBTYPE_BASE)
            );

            log.debug("Registered DoubleAttribute type with unified registry");

            // Register DoubleAttribute-specific validation constraints only
            setupDoubleAttributeValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register DoubleAttribute type with unified registry", e);
        }
    }

    /**
     * Setup DoubleAttribute-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupDoubleAttributeValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALIDATION CONSTRAINT: Double attribute values
            ValidationConstraint doubleAttributeValidation = new ValidationConstraint(
                "doubleattribute.value.validation",
                "DoubleAttribute values must be valid double floating-point numbers",
                (metadata) -> metadata instanceof DoubleAttribute,
                (metadata, value) -> {
                    if (metadata instanceof DoubleAttribute) {
                        DoubleAttribute doubleAttr = (DoubleAttribute) metadata;
                        String valueStr = doubleAttr.getValueAsString();
                        if (valueStr == null || valueStr.isEmpty()) {
                            return true;
                        }
                        try {
                            Double.parseDouble(valueStr);
                            return true;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }
                    return true;
                }
            );
            constraintRegistry.addConstraint(doubleAttributeValidation);

            log.debug("Registered DoubleAttribute-specific constraints");

        } catch (Exception e) {
            log.error("Failed to register DoubleAttribute constraints", e);
        }
    }

    /**
     * Constructs the Double MetaAttribute
     */
    public DoubleAttribute(String name) {
        super(SUBTYPE_DOUBLE, name, DataTypes.DOUBLE);
    }

    /**
     * Manually create a Double MetaAttribute with a value
     */
    public static DoubleAttribute create(String name, Double value) {
        DoubleAttribute a = new DoubleAttribute(name);
        a.setValue(value);
        return a;
    }
}