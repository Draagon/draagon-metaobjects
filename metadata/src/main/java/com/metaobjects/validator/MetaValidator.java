/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.metaobjects.validator;

import com.metaobjects.InvalidMetaDataException;
import com.metaobjects.MetaData;
import com.metaobjects.MetaDataNotFoundException;
import com.metaobjects.attr.BooleanAttribute;
import com.metaobjects.attr.MetaAttribute;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.field.MetaField;
import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.util.MetaDataUtil;
import com.metaobjects.object.MetaObject;
import com.metaobjects.registry.MetaDataRegistry;
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

    /**
     * Register this type with the MetaDataRegistry (called by provider)
     * @param registry the MetaDataRegistry to register with
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(MetaValidator.class, def -> def
            .type(TYPE_VALIDATOR).subType(SUBTYPE_BASE)
            .description("Base validator metadata with common validator attributes")
            .inheritsFrom("metadata", "base")
            .optionalAttribute(ATTR_IS_ABSTRACT, BooleanAttribute.SUBTYPE_BOOLEAN)
            .optionalAttribute(ATTR_MSG, StringAttribute.SUBTYPE_STRING)
            .optionalChild(MetaAttribute.TYPE_ATTR, "*", "*")
        );
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
     * @return the MetaField that declares this validator, or null if attached to MetaDataLoader
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
     * @param obj the object to get the MetaField for
     * @return the MetaField associated with the object
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
     * @param superValidator the super validator to set
     */
    public void setSuperValidator(MetaValidator superValidator) {
        setSuperData(superValidator);
    }

    /**
     * Gets the Super Validator
     * @return the super validator
     */
    protected MetaValidator getSuperValidator() {
        return getSuperData();
    }

    /////////////////////////////////////////////////////////////
    // VALIDATION METHODS

    /**
     * Validates the value of the field in the specified object
     * @param object the object containing the field to validate
     * @param value the value to validate
     */
    public abstract void validate(Object object, Object value);

    /////////////////////////////////////////////////////////////
    // HELPER METHODS

    /**
     * Retrieves the message to use for displaying errors
     * @param defMsg the default message to use if no custom message is set
     * @return the error message to display
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
