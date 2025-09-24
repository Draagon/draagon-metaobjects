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
 * A Long Attribute with unified registry registration and parent acceptance.
 *
 * @version 6.2
 */
@MetaDataType(type = "attr", subType = "long", description = "Long attribute for large integer numeric metadata")
public class LongAttribute extends MetaAttribute<Long> {

    private static final Logger log = LoggerFactory.getLogger(LongAttribute.class);

    public final static String SUBTYPE_LONG = "long";

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

            MetaDataRegistry.registerType(LongAttribute.class, def -> def
                .type(TYPE_ATTR).subType(SUBTYPE_LONG)
                .description("Long attribute for large integer numeric metadata")

                // INHERIT FROM BASE ATTRIBUTE
                .inheritsFrom(TYPE_ATTR, SUBTYPE_BASE)

                // Universal fallback for any long-based attribute
                .acceptsParents("*", "*")  // LongAttribute can be used for any long-based attribute
            );

            log.debug("Registered LongAttribute type with unified registry");

            // Register LongAttribute-specific validation constraints only
            setupLongAttributeValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register LongAttribute type with unified registry", e);
        }
    }

    /**
     * Setup LongAttribute-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupLongAttributeValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALIDATION CONSTRAINT: Long attribute values
            ValidationConstraint longAttributeValidation = new ValidationConstraint(
                "longattribute.value.validation",
                "LongAttribute values must be valid long integers",
                (metadata) -> metadata instanceof LongAttribute,
                (metadata, value) -> {
                    if (metadata instanceof LongAttribute) {
                        LongAttribute longAttr = (LongAttribute) metadata;
                        String valueStr = longAttr.getValueAsString();
                        if (valueStr == null || valueStr.isEmpty()) {
                            return true;
                        }
                        try {
                            Long.parseLong(valueStr);
                            return true;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }
                    return true;
                }
            );
            constraintRegistry.addConstraint(longAttributeValidation);

            log.debug("Registered LongAttribute-specific constraints");

        } catch (Exception e) {
            log.error("Failed to register LongAttribute constraints", e);
        }
    }

    /**
     * Constructs the Integer MetaAttribute
     */
    public LongAttribute(String name ) {
        super( SUBTYPE_LONG, name, DataTypes.LONG);
    }

    /**
     * Manually create an Integer MetaAttribute with a value
     */
    public static LongAttribute create(String name, Long value ) {
        LongAttribute a = new LongAttribute( name );
        a.setValue( value );
        return a;
    }
}
