/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.draagon.meta.validator;

import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttributeNotFoundException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataRegistry;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.MetaObjectNotFoundException;

public abstract class MetaValidator extends MetaData {

    public final static String TYPE_VALIDATOR = "validator";

    public final static String ATTR_MSG = "msg";

    public MetaValidator(String subtype, String name) {
        super(TYPE_VALIDATOR, subtype, name);
    }

    /**
     * Gets the primary MetaData class
     */
    public final Class<MetaValidator> getMetaDataClass() {
        return MetaValidator.class;
    }

    /**
     * Gets the declaring meta field.<br>
     * NOTE: This may not be the MetaField from which the view
     * was retrieved, so be careful!
     */
    public MetaField getDeclaringMetaField() {
        return (MetaField) getParent();
    }

    /**
     * Retrieves the MetaField for this view associated
     * with the specified object.
     */
    public MetaField getMetaField(Object obj) {
        return MetaDataRegistry.findMetaObject(obj).getMetaField(getParent().getName());
    }

    /**
     * Sets the Super Validator
     */
    public void setSuperValidator(MetaValidator superValidator) {
        setSuperData(superValidator);
    }

    /**
     * Gets the Super Validator
     */
    protected MetaValidator getSuperValidator() {
        return (MetaValidator) getSuperData();
    }

    /////////////////////////////////////////////////////////////
    // VALIDATION METHODS

    /**
     * Validates the value of the field in the specified object
     */
    public abstract void validate(Object object, Object value);

    /////////////////////////////////////////////////////////////
    // HELPER METHODS

    /**
     * Retrieves the message to use for displaying errors
     */
    public String getMessage(String defMsg) {
        String msg = defMsg;
        try {
            msg = getAttr(ATTR_MSG).getValueAsString();
        } catch (MetaAttributeNotFoundException ignoreException) {
        }
        return msg;
    }
}
