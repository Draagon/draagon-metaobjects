package com.draagon.meta.validator;

import com.draagon.meta.DataTypes;
import com.draagon.meta.InvalidMetaDataException;
import com.draagon.meta.InvalidValueException;
import com.draagon.meta.attr.MetaAttribute;

import java.util.Collection;

public class ArrayValidator extends MetaValidator {

    public final static String SUBTYPE_ARRAY = "array";

    public final static String ATTR_MINSIZE = "minSize";
    public final static String ATTR_MAXSIZE = "maxSize";

    public ArrayValidator(String name) {
        super(SUBTYPE_ARRAY, name);
    }

    protected ArrayValidator(String subType, String name) {
        super(subType, name);
    }

    public int getMinSize() {
        // TODO: Add Caching
        if (hasMetaAttr(ATTR_MINSIZE)) {
            MetaAttribute<?> attr = getMetaAttr(ATTR_MINSIZE);
            try {
                return getAttrValueAsInt(attr);
            }
            catch(NumberFormatException e ) {
                throw new InvalidMetaDataException( attr, "Invalid min value of ["+attr.getValueAsString()+"]");
            }
        }
        return 0;
    }

    protected int getAttrValueAsInt(MetaAttribute<?> attr) {

        if (attr.getDataType() == DataTypes.INT) {
            return (Integer) attr.getValue();
        }

        // TODO: Make this more efficient without parsing strings
        return Integer.parseInt(attr.getValueAsString());
    }

    public boolean hasMaxSize() {
        return hasMetaAttr(ATTR_MAXSIZE);
    }

    public Integer getMaxSize() {
        // TODO: Add Caching
        if (hasMetaAttr(ATTR_MAXSIZE)) {
            MetaAttribute<?> attr = getMetaAttr(ATTR_MAXSIZE);
            try {
                return getAttrValueAsInt(attr);
            }
            catch(NumberFormatException e ) {
                throw new InvalidMetaDataException( attr, "Invalid max value of ["+attr.getValueAsString()+"]");
            }
        } else {
            return null;
            //throw new InvalidMetaDataException( this,
            //        "You cannot call getMax() on ArrayValidator when no max attribute is set");
        }
    }

    /**
     * Validates the value of the field in the specified object
     */
    public void validate(Object object, Object value) {

        if ( value != null ) {

            if (value instanceof Collection) {
                int size = ((Collection) value).size();
                if ( size < getMinSize() ) {
                    throw new InvalidValueException( "Minimum array size is ("+ getMinSize()+"), array was ("+size+")");
                }
                if ( hasMaxSize() && size > getMaxSize() ) {
                    throw new InvalidValueException( "Maximum array size is ("+ getMaxSize()+"), array was ("+size+")");
                }
            }
            else {
                // For non-array values, only validate if minimum size constraint would be violated
                // This allows single values to pass when minSize <= 1
                if ( getMinSize() > 1 ) {
                    throw new InvalidValueException( "The value was not an array and the size must be at least "+ getMinSize());
                }
            }
        }
    }
}
