/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.draagon.meta.validator;

//import com.draagon.meta.attr.AttributeDef;

import java.util.ArrayList;
import java.util.Collection;

import com.draagon.meta.*;

import org.apache.commons.validator.GenericValidator;

/**
 * Regular expression validator, that ensures a field value matches the specific expression mask
 */
@SuppressWarnings("serial")
public class RegexValidator extends MetaValidator {

    public final static String SUBTYPE_REGEX = "regex";

    /** Mask attribute */
    public final static String ATTR_MASK = "mask";

    public RegexValidator(String name) {
        super(SUBTYPE_REGEX, name);
    }

    /**
     * Validates the value of the field in the specified object
     */
    public void validate(Object object, Object value)
    //throws MetaException
    {
        String mask = getMetaAttr(ATTR_MASK).getValueAsString();
        String msg = getMessage("Invalid value format");

        String val = (value == null) ? null : value.toString();

        if (!GenericValidator.isBlankOrNull(val)
                && !GenericValidator.matchRegexp(val, mask)) {
            throw new InvalidValueException(msg);
        }
    }
}
