/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.draagon.meta.validator;

import com.draagon.meta.*;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;

import org.apache.commons.validator.GenericValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.draagon.meta.validator.MetaValidator.SUBTYPE_BASE;

/**
 * A Required validator that ensures a field has a value and is not null with unified registry registration.
 *
 * @version 6.0
 */
@MetaDataType(type = "validator", subType = "required", description = "Required validator for field validation")
@SuppressWarnings("serial")
public class RequiredValidator extends MetaValidator
{
    private static final Logger log = LoggerFactory.getLogger(RequiredValidator.class);

    public final static String SUBTYPE_REQUIRED = "required";

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.registerType(RequiredValidator.class, def -> def
                .type(TYPE_VALIDATOR).subType(SUBTYPE_REQUIRED)
                .description("Required validator ensures field has a value and is not null")

                // INHERIT FROM BASE VALIDATOR
                .inheritsFrom(TYPE_VALIDATOR, SUBTYPE_BASE)

                // NO REQUIRED-SPECIFIC ATTRIBUTES (only uses inherited base attributes)
            );
            
            log.debug("Registered RequiredValidator type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register RequiredValidator type with unified registry", e);
        }
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
