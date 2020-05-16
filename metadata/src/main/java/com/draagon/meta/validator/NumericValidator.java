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

import org.apache.commons.validator.GenericValidator;

/**
 * Numeric Validator that ensures a value is a number
 */
@SuppressWarnings("serial")
public class NumericValidator extends MetaValidator {
    public final static String SUBTYPE_NUMERIC = "numeric";

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
