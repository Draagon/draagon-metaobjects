/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.object.value;

import com.metaobjects.ValueException;
import com.metaobjects.object.MetaObject;
import com.metaobjects.util.DataConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Generic Map of fields and values that can be associated with a MetaObject. The values are retrieved
 * as "attributes".  You can also associate "properties" to the object that can be used for special behaviors
 * when persisting or transforming objects.
 */
public class ValueObject extends ValueObjectBase implements Map<String, Object> {

    private static final long serialVersionUID = 6888178049723946186L;

    static final Logger log = LoggerFactory.getLogger(ValueObject.class);

    /**
     * Create a generic value object that supports extensions by default
     */
    public ValueObject() {
        _allowExtensions( false );
    }

    /**
     * Create a generic value object associated to the MetaObject
     * @param mo MetaObject to associated
     */
    public ValueObject(MetaObject mo ) {
        super( mo );
    }

    /**
     * Create a value object associated to the MetaObject and set flag on extensions
     * @param mo MetaObject to associated
     * @param allowExtensions Whether to allow extensions
     */
    public ValueObject(MetaObject mo, boolean allowExtensions ) {
        setMetaData( mo );
        _allowExtensions( allowExtensions );
    }

    /**
     * A generic value object with the specified name
     * @param name Name of the object
     */
    public ValueObject(String name ) {
        super(name);
        _allowExtensions( true );
    }

    //////////////////////////////////////////////////////////////
    // SETTER VALUES

    public void setBoolean(String name, Boolean value) {
        if (isArrayField(name)) {
            // Field is defined as array - convert to single-element list
            List<Boolean> list = value != null ? Arrays.asList(value) : null;
            _setObjectAttribute(name, list);
        } else {
            // Regular boolean field
            _setObjectAttribute(name, value);
        }
    }

    public void setByte(String name, Byte value)  {
        if (isArrayField(name)) {
            // Field is defined as array - convert to single-element list
            List<Byte> list = value != null ? Arrays.asList(value) : null;
            _setObjectAttribute(name, list);
        } else {
            // Regular byte field
            _setObjectAttribute(name, value);
        }
    }

    public void setShort(String name, Short value) {
        if (isArrayField(name)) {
            // Field is defined as array - convert to single-element list
            List<Short> list = value != null ? Arrays.asList(value) : null;
            _setObjectAttribute(name, list);
        } else {
            // Regular short field
            _setObjectAttribute(name, value);
        }
    }

    public void setInt(String name, Integer value) {
        if (isArrayField(name)) {
            // Field is defined as array - convert to single-element list
            List<Integer> list = value != null ? Arrays.asList(value) : null;
            _setObjectAttribute(name, list);
        } else {
            // Regular integer field
            _setObjectAttribute(name, value);
        }
    }

    public void setInteger(String name, Integer value) {
        setInt(name, value);
    }

    public void setLong(String name, Long value) {
        if (isArrayField(name)) {
            // Field is defined as array - convert to single-element list
            List<Long> list = value != null ? Arrays.asList(value) : null;
            _setObjectAttribute(name, list);
        } else {
            // Regular long field
            _setObjectAttribute(name, value);
        }
    }

    public void setFloat(String name, Float value) {
        if (isArrayField(name)) {
            // Field is defined as array - convert to single-element list
            List<Float> list = value != null ? Arrays.asList(value) : null;
            _setObjectAttribute(name, list);
        } else {
            // Regular float field
            _setObjectAttribute(name, value);
        }
    }

    public void setDouble(String name, Double value) {
        if (isArrayField(name)) {
            // Field is defined as array - convert to single-element list
            List<Double> list = value != null ? Arrays.asList(value) : null;
            _setObjectAttribute(name, list);
        } else {
            // Regular double field
            _setObjectAttribute(name, value);
        }
    }

    public void setString(String name, String value) {
        if (isArrayField(name)) {
            // Field is defined as array - parse string into list
            List<String> list = DataConverter.toStringArray(value);
            _setObjectAttribute(name, list);
        } else {
            // Regular string field
            _setObjectAttribute(name, value);
        }
    }

    public void setStringArray(String name, List<String> value) {
        _setObjectAttribute(name, value);
    }

    public void setIntArray(String name, List<Integer> value) {
        _setObjectAttribute(name, value);
    }

    public void setLongArray(String name, List<Long> value) {
        _setObjectAttribute(name, value);
    }

    public void setBooleanArray(String name, List<Boolean> value) {
        _setObjectAttribute(name, value);
    }

    public void setDoubleArray(String name, List<Double> value) {
        _setObjectAttribute(name, value);
    }

    public void setFloatArray(String name, List<Float> value) {
        _setObjectAttribute(name, value);
    }

    public void setDate(String name, java.util.Date value) {
        _setObjectAttribute(name, value);
    }

    public void setObject(String name, Object value) {
        _setObjectAttribute(name, value);
    }

    public <T> void setObjectArray( String name, List<T> value) {
        _setObjectAttribute(name, value);
    }


    //////////////////////////////////////////////////////////////
    // GETTER VALUES

    public Boolean getBoolean(String name) {
        Object value = getObjectAttribute(name);

        // Smart fallback: if field is an array, return first element
        if (isArrayField(name) && value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) return null;
            return DataConverter.toBoolean(list.get(0));
        }

        return DataConverter.toBoolean(value); // Standard behavior
    }

    public Byte getByte(String name) {
        Object value = getObjectAttribute(name);

        // Smart fallback: if field is an array, return first element
        if (isArrayField(name) && value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) return null;
            return DataConverter.toByte(list.get(0));
        }

        return DataConverter.toByte(value); // Standard behavior
    }

    public Short getShort(String name) {
        Object value = getObjectAttribute(name);

        // Smart fallback: if field is an array, return first element
        if (isArrayField(name) && value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) return null;
            return DataConverter.toShort(list.get(0));
        }

        return DataConverter.toShort(value); // Standard behavior
    }

    public Integer getInt(String name) {
        Object value = getObjectAttribute(name);

        // Smart fallback: if field is an array, return first element
        if (isArrayField(name) && value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) return null;
            return DataConverter.toInt(list.get(0));
        }

        return DataConverter.toInt(value); // Standard behavior
    }

    public Integer getInteger(String name) {
        return getInt(name);
    }

    public Long getLong(String name) {
        Object value = getObjectAttribute(name);

        // Smart fallback: if field is an array, return first element
        if (isArrayField(name) && value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) return null;
            return DataConverter.toLong(list.get(0));
        }

        return DataConverter.toLong(value); // Standard behavior
    }

    public Float getFloat(String name) {
        Object value = getObjectAttribute(name);

        // Smart fallback: if field is an array, return first element
        if (isArrayField(name) && value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) return null;
            return DataConverter.toFloat(list.get(0));
        }

        return DataConverter.toFloat(value); // Standard behavior
    }

    public Double getDouble(String name)  {
        Object value = getObjectAttribute(name);

        // Smart fallback: if field is an array, return first element
        if (isArrayField(name) && value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) return null;
            return DataConverter.toDouble(list.get(0));
        }

        return DataConverter.toDouble(value); // Standard behavior
    }

    public String getString(String name) {
        Object value = getObjectAttribute(name);

        // Smart fallback: if field is an array, return comma-delimited string
        if (isArrayField(name) && value instanceof List) {
            return DataConverter.toString(value); // Already handles List → "item1,item2,item3"
        }

        return DataConverter.toString(value); // Standard behavior
    }

    public List<String> getStringArray(String name) {
        return DataConverter.toStringArray(getObjectAttribute(name));
    }

    public List<Integer> getIntArray(String name) {
        return DataConverter.toIntArray(getObjectAttribute(name));
    }

    public List<Long> getLongArray(String name) {
        return DataConverter.toLongArray(getObjectAttribute(name));
    }

    public List<Boolean> getBooleanArray(String name) {
        return DataConverter.toBooleanArray(getObjectAttribute(name));
    }

    public List<Double> getDoubleArray(String name) {
        return DataConverter.toDoubleArray(getObjectAttribute(name));
    }

    public List<Float> getFloatArray(String name) {
        return DataConverter.toFloatArray(getObjectAttribute(name));
    }

    public java.util.Date getDate(String name) {
        return DataConverter.toDate(getObjectAttribute(name));
    }

    public Object getObject(String name) {
        return getObjectAttribute(name);
    }

    public <T> List<T> getObjectArray(Class<T> clazz, String name) {
        return _objectToTypedArray( clazz, getObjectAttribute(name));
    }

    public <T> List<T> getAndCreateObjectArray( Class<T> clazz, String name) {
        return _getAndCreateObjectArray( clazz, name );
    }

    @Override
    public void validate() throws ValueException {
        // ValueObject can exist without MetaData (generic value objects)
        // This is different from DataObject which requires MetaData
        // No validation needed for ValueObjects without MetaData
    }
}
