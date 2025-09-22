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
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.validator.GenericValidator;

/**
 * Regular expression validator, that ensures a field value matches the specific expression mask
 */
@MetaDataType(type = "validator", subType = "regex", description = "Regular expression validator for pattern matching")
@SuppressWarnings("serial")
public class RegexValidator extends MetaValidator {

    private static final Logger log = LoggerFactory.getLogger(RegexValidator.class);

    public final static String SUBTYPE_REGEX = "regex";

    /** Mask attribute */
    public final static String ATTR_MASK = "mask";

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.registerType(RegexValidator.class, def -> def
                .type(TYPE_VALIDATOR).subType(SUBTYPE_REGEX)
                .inheritsFrom(TYPE_VALIDATOR, SUBTYPE_BASE)
                .requiredAttribute(ATTR_MASK, "string")
                .description("Regular expression validator for pattern matching")
            );

            log.debug("Registered RegexValidator type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register RegexValidator type with unified registry", e);
        }
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
