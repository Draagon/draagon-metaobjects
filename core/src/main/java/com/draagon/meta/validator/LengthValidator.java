/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.draagon.meta.validator;

import com.draagon.meta.*;
import com.draagon.meta.field.MetaField;
import org.apache.commons.validator.GenericValidator;

// TODO:  Make this work for numeric fields and even Date fields, or create new validator types
/**
 * A length validator, that ensures the string representation of a field value is of the min or max length
 */
@SuppressWarnings("serial")
public class LengthValidator extends MetaValidator {

    public final static String SUBTYPE_LENGTH = "length";

    /**
     * Minimum length attribute
     */
    public final static String ATTR_MIN = "min";
    /**
     * Maximum length attribute
     */
    public final static String ATTR_MAX = "max";

    public LengthValidator(String name) {
        super(SUBTYPE_LENGTH, name);
        //addAttributeDef( new AttributeDef( ATTR_MIN, String.class, false, "Minimum length (0 default)" ));
        //addAttributeDef( new AttributeDef( ATTR_MAX, String.class, false, "Maximum length (field length default)" ));
    }

    /**
     * Validates the value of the field in the specified object
     */
    public void validate(Object object, Object value) {

        int min = hasAttr(ATTR_MIN)
                ? Integer.parseInt(getMetaAttr(ATTR_MIN).getValueAsString())
                : 0;

        int max = hasAttr(ATTR_MAX)
                ? Integer.parseInt(getMetaAttr(ATTR_MAX).getValueAsString())
                : getDefaultMax( getMetaField( object ));

        String msg = getMessage("A valid length between " + min + " and " + max + " must be entered");
        String val = (value == null) ? null : value.toString();

        if (!GenericValidator.isBlankOrNull(val)
                && (val.length() < min || val.length() > max)) {
            throw new InvalidValueException(msg);
        }
    }

    /** Get the default max string size based on the MetaField DataType */
    protected int getDefaultMax( MetaField f ) {
        switch( f.getDataType() )
        {
            case BOOLEAN: return 1;
            case BYTE: return 4;
            case SHORT: return 6;
            case INT: return 10;
            case LONG: return 15;
            case FLOAT: return 12;
            case DOUBLE: return 16;
            case STRING: return 100;
            case DATE: return 15;
            default:  return 1000000;
        }
    }
}
