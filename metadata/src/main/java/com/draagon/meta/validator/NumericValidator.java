/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.draagon.meta.validator;

import java.util.ArrayList;
import java.util.Collection;

import com.draagon.meta.*;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.validator.GenericValidator;

/**
 * Numeric Validator that ensures a value is a number
 */
@MetaDataType(type = "validator", subType = "numeric", description = "Numeric validator ensuring values are numbers")
@SuppressWarnings("serial")
public class NumericValidator extends MetaValidator {

    private static final Logger log = LoggerFactory.getLogger(NumericValidator.class);
    public final static String SUBTYPE_NUMERIC = "numeric";

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.getInstance().registerType(NumericValidator.class, def -> def
                .type(TYPE_VALIDATOR).subType(SUBTYPE_NUMERIC)
                .inheritsFrom(TYPE_VALIDATOR, SUBTYPE_BASE)
                .description("Numeric validator ensuring values are numbers")
            );

            log.debug("Registered NumericValidator type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register NumericValidator type with unified registry", e);
        }
    }

    public NumericValidator(String name) {
        super(SUBTYPE_NUMERIC, name);
    }

    /**
     * Validates the value of the field in the specified object
     */
    public void validate(Object object, Object value) {

        String val = (value == null) ? null : value.toString();

        if (!GenericValidator.isBlankOrNull(val)) {

            for (int i = 0; i < val.length(); i++) {
                if (val.charAt(i) < '0' || val.charAt(i) > '9')
                    throw new InvalidValueException(getMessage("The value is not a valid number"));
            }
        }
    }
}
