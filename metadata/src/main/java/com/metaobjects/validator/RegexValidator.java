/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.metaobjects.validator;

import com.metaobjects.*;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.registry.MetaDataRegistry;
import org.apache.commons.validator.GenericValidator;

import static com.metaobjects.validator.MetaValidator.TYPE_VALIDATOR;
import static com.metaobjects.validator.MetaValidator.SUBTYPE_BASE;

/**
 * Regular expression validator that ensures a field value matches the specific expression mask.
 */
public class RegexValidator extends MetaValidator {

    public final static String SUBTYPE_REGEX = "regex";

    /** Mask attribute */
    public final static String ATTR_MASK = "mask";

    /**
     * Register this type with the MetaDataRegistry (called by provider)
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(RegexValidator.class, def -> def
            .type(TYPE_VALIDATOR).subType(SUBTYPE_REGEX)
            .description("Regular expression validator for pattern matching")
            .inheritsFrom(TYPE_VALIDATOR, SUBTYPE_BASE)
            .requiredAttribute(ATTR_MASK, StringAttribute.SUBTYPE_STRING)
        );
    }

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
