/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.draagon.meta.validator;

import com.draagon.meta.InvalidMetaDataException;
import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.attr.BooleanAttribute;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.util.MetaDataUtil;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MetaValidator that performs validations on a MetaField
 */
public abstract class MetaValidator extends MetaData {

    private static final Logger log = LoggerFactory.getLogger(MetaValidator.class);

    public final static String TYPE_VALIDATOR = "validator";
    public final static String SUBTYPE_BASE = "base";
    public final static String ATTR_MSG = "msg";

    // Base validator type registration
    static {
        try {
            MetaDataRegistry.registerType(MetaValidator.class, def -> def
                .type(TYPE_VALIDATOR).subType(SUBTYPE_BASE)
                .description("Base validator metadata with common validator attributes")

                // UNIVERSAL ATTRIBUTES (all MetaData inherit these)
                .optionalAttribute(ATTR_IS_ABSTRACT, BooleanAttribute.SUBTYPE_BOOLEAN)

                // VALIDATOR-SPECIFIC ATTRIBUTES
                .optionalAttribute(ATTR_MSG, StringAttribute.SUBTYPE_STRING)

                // VALIDATORS CAN CONTAIN ATTRIBUTES
                .optionalChild(MetaAttribute.TYPE_ATTR, "*", "*")
            );

            log.debug("Registered base MetaValidator type with unified registry");

        } catch (Exception e) {
            log.error("Failed to register MetaValidator type with unified registry", e);
        }
    }

    public MetaValidator(String subtype, String name) {
        super(TYPE_VALIDATOR, subtype, name);
    }

    // Note: getMetaDataClass() is now inherited from MetaData base class

    /** Add Child to the MetaValidator */
    //public MetaValidator addChild(MetaData data) throws InvalidMetaDataException {
    //    return super.addChild( data );
    //}

    /** Wrap the MetaValidator */
    //public MetaValidator overload() {
    //    return super.overload();
    //}

    /**
     * Sets an attribute of the MetaClass
     */
    //public MetaValidator addMetaAttr(MetaAttribute attr) {
    //    return addChild(attr);
    //}

    /**
     * Gets the declaring meta field.<br>
     * NOTE: This may not be the MetaField from which the view
     * was retrieved, so be careful!
     */
    public MetaField getDeclaringMetaField() {
        if ( getParent() instanceof MetaDataLoader) return null;
        if ( getParent() instanceof MetaField ) return (MetaField) getParent();
        throw new InvalidMetaDataException(this, "MetaValidators can only be attached to MetaFields " +
                "or MetaDataLoaders as abstracts");
    }

    /**
     * Retrieves the MetaField for this view associated
     * with the specified object.
     */
    public MetaField getMetaField(Object obj) {
        MetaObject mo = MetaDataUtil.findMetaObject(obj, this);
        MetaField mf = getDeclaringMetaField();
        if ( mo != null ) {
            return mo.getMetaField(mf.getName());
        }
        else if ( mf != null ) {
            return mf;
        }
        return null;
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
        return getSuperData();
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
            msg = getMetaAttr(ATTR_MSG).getValueAsString();
        } catch (MetaDataNotFoundException ignoreException) {
        }
        return msg;
    }
}
