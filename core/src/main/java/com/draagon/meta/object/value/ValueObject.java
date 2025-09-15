/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.object.value;

import com.draagon.meta.ValueException;
import com.draagon.meta.ValueNotFoundException;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.util.DataConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;


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
        _setObjectAttribute(name, value);
    }

    public void setByte(String name, Byte value)  {
        _setObjectAttribute(name, value);
    }

    public void setShort(String name, Short value) {
        _setObjectAttribute(name, value);
    }

    public void setInt(String name, Integer value) {
        _setObjectAttribute(name, value);
    }

    public void setInteger(String name, Integer value) {
        _setObjectAttribute(name, value);
    }

    public void setLong(String name, Long value) {
        _setObjectAttribute(name, value);
    }

    public void setFloat(String name, Float value) {
        _setObjectAttribute(name, value);
    }

    public void setDouble(String name, Double value) {
        _setObjectAttribute(name, value);
    }

    public void setString(String name, String value) {
        _setObjectAttribute(name, value);
    }

    public void setStringArray(String name, List<String> value) {
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
        return DataConverter.toBoolean(getObjectAttribute(name));
    }

    public Byte getByte(String name) {
        return DataConverter.toByte(getObjectAttribute(name));
    }

    public Short getShort(String name) {
        return DataConverter.toShort(getObjectAttribute(name));
    }

    public Integer getInt(String name) {
        return DataConverter.toInt(getObjectAttribute(name));
    }

    public Integer getInteger(String name) {
        return getInt(name);
    }

    public Long getLong(String name) {
        return DataConverter.toLong(getObjectAttribute(name));
    }

    public Float getFloat(String name) {
        return DataConverter.toFloat(getObjectAttribute(name));
    }

    public Double getDouble(String name)  {
        return DataConverter.toDouble(getObjectAttribute(name));
    }

    public String getString(String name) {
        return DataConverter.toString(getObjectAttribute(name));
    }

    public List<String> getStringArray(String name) {
        return DataConverter.toStringArray(getObjectAttribute(name));
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

    //////////////////////////////////////////////////////////////
    // MODERN OPTIONAL-BASED APIs

    /**
     * Find a string value, returning Optional to handle null cases safely
     * @param name Field name
     * @return Optional containing the string value, or empty if null/missing
     */
    public Optional<String> findString(String name) {
        return Optional.ofNullable(getString(name));
    }

    /**
     * Find a boolean value, returning Optional to handle null cases safely  
     * @param name Field name
     * @return Optional containing the boolean value, or empty if null/missing
     */
    public Optional<Boolean> findBoolean(String name) {
        return Optional.ofNullable(getBoolean(name));
    }

    /**
     * Find an integer value, returning Optional to handle null cases safely
     * @param name Field name
     * @return Optional containing the integer value, or empty if null/missing
     */
    public Optional<Integer> findInt(String name) {
        return Optional.ofNullable(getInt(name));
    }

    /**
     * Find an object value, returning Optional to handle null cases safely
     * @param name Field name  
     * @return Optional containing the object value, or empty if null/missing
     */
    public Optional<Object> findObject(String name) {
        return Optional.ofNullable(getObject(name));
    }

    /**
     * Require a string value, throwing exception if null/missing
     * @param name Field name
     * @return String value
     * @throws ValueNotFoundException if value is null or missing
     */
    public String requireString(String name) throws ValueNotFoundException {
        return findString(name).orElseThrow(() -> 
            new ValueNotFoundException("Required string field '" + name + "' is missing or null"));
    }

    /**
     * Require an integer value, throwing exception if null/missing
     * @param name Field name
     * @return Integer value
     * @throws ValueNotFoundException if value is null or missing
     */
    public Integer requireInt(String name) throws ValueNotFoundException {
        return findInt(name).orElseThrow(() -> 
            new ValueNotFoundException("Required int field '" + name + "' is missing or null"));
    }

    //////////////////////////////////////////////////////////////
    // STREAM-BASED COLLECTION ACCESS

    /**
     * Get a stream of all field names
     * @return Stream of field names
     */
    public Stream<String> getKeysStream() {
        return keySet().stream();
    }

    /**
     * Get a stream of all field values
     * @return Stream of field values
     */
    public Stream<Object> getValuesStream() {
        return values().stream();
    }

    /**
     * Get a stream of all entries (key-value pairs)
     * @return Stream of Map.Entry objects
     */
    public Stream<Map.Entry<String, Object>> getEntriesStream() {
        return entrySet().stream();
    }

    //////////////////////////////////////////////////////////////
    // BUILDER PATTERN SUPPORT

    /**
     * Create a new ValueObject builder
     * @return New ValueObject.Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new ValueObject builder with specified MetaObject
     * @param metaObject MetaObject to associate
     * @return New ValueObject.Builder instance
     */
    public static Builder builder(MetaObject metaObject) {
        return new Builder(metaObject);
    }

    /**
     * Builder pattern for ValueObject creation
     */
    public static class Builder {
        private MetaObject metaObject;
        private boolean allowExtensions = true;
        private final Map<String, Object> values = new HashMap<>();

        public Builder() {}

        public Builder(MetaObject metaObject) {
            this.metaObject = metaObject;
        }

        public Builder withMetaObject(MetaObject metaObject) {
            this.metaObject = metaObject;
            return this;
        }

        public Builder allowExtensions(boolean allow) {
            this.allowExtensions = allow;
            return this;
        }

        public Builder withString(String name, String value) {
            this.values.put(name, value);
            return this;
        }

        public Builder withInt(String name, Integer value) {
            this.values.put(name, value);
            return this;
        }

        public Builder withBoolean(String name, Boolean value) {
            this.values.put(name, value);
            return this;
        }

        public Builder withObject(String name, Object value) {
            this.values.put(name, value);
            return this;
        }

        public ValueObject build() {
            ValueObject obj;
            if (metaObject != null) {
                obj = new ValueObject(metaObject, allowExtensions);
            } else {
                obj = new ValueObject();
                obj._allowExtensions(allowExtensions);
            }
            
            // Set all values
            values.forEach(obj::setObject);
            
            return obj;
        }
    }

    @Override
    public void validate() throws ValueException {
        // ValueObject can exist without MetaData (generic value objects)
        // This is different from DataObject which requires MetaData
        // No validation needed for ValueObjects without MetaData
    }
}
