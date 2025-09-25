/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.draagon.meta.validator;

import com.draagon.meta.*;
import com.draagon.meta.registry.MetaDataRegistry;

import org.apache.commons.validator.GenericValidator;

import static com.draagon.meta.validator.MetaValidator.TYPE_VALIDATOR;
import static com.draagon.meta.validator.MetaValidator.SUBTYPE_BASE;

/**
 * A Required validator that ensures a field has a value and is not null with provider-based registration.
 */
@SuppressWarnings("serial")
public class RequiredValidator extends MetaValidator
{
    public final static String SUBTYPE_REQUIRED = "required";

    /**
     * Register this type with the MetaDataRegistry (called by provider)
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(RequiredValidator.class, def -> def
            .type(TYPE_VALIDATOR).subType(SUBTYPE_REQUIRED)
            .description("Required validator ensures field has a value and is not null")
            .inheritsFrom(TYPE_VALIDATOR, SUBTYPE_BASE)
        );
    }

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
