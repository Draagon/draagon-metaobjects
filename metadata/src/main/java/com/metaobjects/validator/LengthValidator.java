/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.metaobjects.validator;

import com.metaobjects.*;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.field.MetaField;
import com.metaobjects.registry.MetaDataRegistry;
import org.apache.commons.validator.GenericValidator;

import static com.metaobjects.validator.MetaValidator.TYPE_VALIDATOR;
import static com.metaobjects.validator.MetaValidator.SUBTYPE_BASE;

/**
 * A length validator that ensures the string representation of a field value is of the min or max length.
 */
public class LengthValidator extends MetaValidator
{
    public final static String SUBTYPE_LENGTH = "length";

    /**
     * Minimum length attribute
     */
    public final static String ATTR_MIN = "min";
    /**
     * Maximum length attribute
     */
    public final static String ATTR_MAX = "max";

    /**
     * Register this type with the MetaDataRegistry (called by provider)
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(LengthValidator.class, def -> {
            def.type(TYPE_VALIDATOR).subType(SUBTYPE_LENGTH)
               .description("Length validator ensures field value is within min/max length")
               .inheritsFrom(TYPE_VALIDATOR, SUBTYPE_BASE);

            // LENGTH-SPECIFIC ATTRIBUTES WITH FLUENT CONSTRAINTS
            def.optionalAttributeWithConstraints(ATTR_MIN)
               .ofType(StringAttribute.SUBTYPE_STRING)
               .asSingle();

            def.optionalAttributeWithConstraints(ATTR_MAX)
               .ofType(StringAttribute.SUBTYPE_STRING)
               .asSingle();
        });
    }

    public LengthValidator(String name) {
        super(SUBTYPE_LENGTH, name);
    }

    /**
     * Validates the value of the field in the specified object
     */
    public void validate(Object object, Object value) {

        int min = hasMetaAttr(ATTR_MIN)
                ? Integer.parseInt(getMetaAttr(ATTR_MIN).getValueAsString())
                : 0;

        int max = hasMetaAttr(ATTR_MAX)
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
