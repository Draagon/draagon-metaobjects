package com.draagon.meta.validator;

import com.draagon.meta.DataTypes;
import com.draagon.meta.InvalidMetaDataException;
import com.draagon.meta.InvalidValueException;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

@MetaDataType(type = "validator", subType = "array", description = "Array validator for size constraints")
public class ArrayValidator extends MetaValidator {

    private static final Logger log = LoggerFactory.getLogger(ArrayValidator.class);

    public final static String SUBTYPE_ARRAY = "array";

    public final static String ATTR_MINSIZE = "minSize";
    public final static String ATTR_MAXSIZE = "maxSize";

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.getInstance().registerType(ArrayValidator.class, def -> def
                .type(TYPE_VALIDATOR).subType(SUBTYPE_ARRAY)
                .inheritsFrom(TYPE_VALIDATOR, SUBTYPE_BASE)
                .optionalAttribute(ATTR_MINSIZE, "int")
                .optionalAttribute(ATTR_MAXSIZE, "int")
                .description("Array validator for size constraints")
            );

            log.debug("Registered ArrayValidator type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register ArrayValidator type with unified registry", e);
        }
    }

    // Cache for frequently accessed size values
    private Integer cachedMinSize;
    private Integer cachedMaxSize;
    private boolean minSizeCached = false;
    private boolean maxSizeCached = false;

    public ArrayValidator(String name) {
        super(SUBTYPE_ARRAY, name);
    }

    protected ArrayValidator(String subType, String name) {
        super(subType, name);
    }

    public int getMinSize() {
        if (!minSizeCached) {
            if (hasMetaAttr(ATTR_MINSIZE)) {
                MetaAttribute<?> attr = getMetaAttr(ATTR_MINSIZE);
                try {
                    cachedMinSize = getAttrValueAsInt(attr);
                }
                catch(NumberFormatException e ) {
                    throw new InvalidMetaDataException( attr, "Invalid min value of ["+attr.getValueAsString()+"]");
                }
            } else {
                cachedMinSize = 0;
            }
            minSizeCached = true;
        }
        return cachedMinSize;
    }

    protected int getAttrValueAsInt(MetaAttribute<?> attr) {
        // Optimized: direct access for INT type, fallback to string parsing for other types
        if (attr.getDataType() == DataTypes.INT) {
            return (Integer) attr.getValue();
        }

        return Integer.parseInt(attr.getValueAsString());
    }

    public boolean hasMaxSize() {
        return hasMetaAttr(ATTR_MAXSIZE);
    }

    public Integer getMaxSize() {
        if (!maxSizeCached) {
            if (hasMetaAttr(ATTR_MAXSIZE)) {
                MetaAttribute<?> attr = getMetaAttr(ATTR_MAXSIZE);
                try {
                    cachedMaxSize = getAttrValueAsInt(attr);
                }
                catch(NumberFormatException e ) {
                    throw new InvalidMetaDataException( attr, "Invalid max value of ["+attr.getValueAsString()+"]");
                }
            } else {
                cachedMaxSize = null;
            }
            maxSizeCached = true;
        }
        return cachedMaxSize;
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
