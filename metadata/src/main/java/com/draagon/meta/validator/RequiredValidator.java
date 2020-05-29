/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.draagon.meta.validator;

import com.draagon.meta.*;

import org.apache.commons.validator.GenericValidator;

/**
 * A Reguired validator that ensures a field has a value and is not null
 */
@SuppressWarnings("serial")
public class RequiredValidator extends MetaValidator {

    public final static String SUBTYPE_REQUIRED = "required";

    public RequiredValidator(String name) {
        super(SUBTYPE_REQUIRED, name);
    }

    /**
     * Validates the value of the field in the specified object
     */
    public void validate(Object object, Object value) {

        String msg = getMessage("A value is required on field "+getParent().getShortName());
        String val = (value == null) ? null : value.toString();

        if (GenericValidator.isBlankOrNull(val)) {
            throw new InvalidValueException(msg);
        }
    }
}
