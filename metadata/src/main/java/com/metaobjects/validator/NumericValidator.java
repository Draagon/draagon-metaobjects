/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.metaobjects.validator;

import java.util.ArrayList;
import java.util.Collection;

import com.metaobjects.*;
import com.metaobjects.registry.MetaDataRegistry;
import org.apache.commons.validator.GenericValidator;

import static com.metaobjects.validator.MetaValidator.TYPE_VALIDATOR;
import static com.metaobjects.validator.MetaValidator.SUBTYPE_BASE;

/**
 * Numeric Validator that ensures a value is a number.
 */
@SuppressWarnings("serial")
public class NumericValidator extends MetaValidator {

    public final static String SUBTYPE_NUMERIC = "numeric";

    /**
     * Register this type with the MetaDataRegistry (called by provider)
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(NumericValidator.class, def -> def
            .type(TYPE_VALIDATOR).subType(SUBTYPE_NUMERIC)
            .description("Numeric validator ensuring values are numbers")
            .inheritsFrom(TYPE_VALIDATOR, SUBTYPE_BASE)
        );
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
