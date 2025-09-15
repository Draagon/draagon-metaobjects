
package com.draagon.meta.object.data;

import com.draagon.meta.ValueException;
import com.draagon.meta.ValueNotFoundException;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.util.DataConverter;

import java.util.*;
import java.util.stream.Stream;

/**
 * Generic Map of fields and values that can be associated with a MetaObject. The values are retrieved
 * as "attributes".  You can also associate "properties" to the object that can be used for special behaviors
 * when persisting or transforming objects.
 */
public class DataObject extends DataObjectBase {

    /**
     * Create a generic data object associated to the MetaObject
     * @param mo MetaObject to associated
     */
    public DataObject(MetaObject mo ) {
        setMetaData( mo );
    }

    //////////////////////////////////////////////////////////////
    // SETTER VALUES
    
    private void _set( String name, Object value ) {
        _setObjectAttribute(name, value);
    }

    public void setBoolean(String name, Boolean value) {
        _set(name, value);
    }

    public void setByte(String name, Byte value)  {
        _set(name, value);
    }

    public void setShort(String name, Short value) {
        _set(name, value);
    }

    public void setInt(String name, Integer value) {
        _set(name, value);
    }

    public void setInteger(String name, Integer value) {
        _set(name, value);
    }

    public void setLong(String name, Long value) {
        _set(name, value);
    }

    public void setFloat(String name, Float value) {
        _set(name, value);
    }

    public void setDouble(String name, Double value) {
        _set(name, value);
    }

    public void setString(String name, String value) {
        _set(name, value);
    }

    public void setStringArray(String name, List<String> value ) {
        _set( name, value );
    }

    public void setDate(String name, java.util.Date value) {
        _set(name, value);
    }

    public <T> void setClass( String name, Class<T> value ) {
        _set(name, value );
    }

    public <T> void setObject(String name, T value) {
        _set(name, value);
    }

    public <T> void setObjectArray(String name, List<T> value) {
        _set(name, value);
    }

    public <T> void addToObjectArray( String name, T value ) {
        List<T> current = _getAndCreateObjectArray( (Class<T>) value.getClass(), name );
        current.add( value );
    }


    //////////////////////////////////////////////////////////////
    // GETTER VALUES

    private Object _get(String name) {
        return _getObjectAttribute(name);
    }

    public Boolean getBoolean(String name) {
        return DataConverter.toBoolean(_get(name));
    }

    public Byte getByte(String name) {
        return DataConverter.toByte(_get(name));
    }

    public Short getShort(String name) {
        return DataConverter.toShort(_get(name));
    }

    public Integer getInt(String name) {
        return DataConverter.toInt(_get(name));
    }

    public Integer getInteger(String name) {
        return getInt(name);
    }

    public Long getLong(String name) {
        return DataConverter.toLong(_get(name));
    }

    public Float getFloat(String name) {
        return DataConverter.toFloat(_get(name));
    }

    public Double getDouble(String name)  {
        return DataConverter.toDouble(_get(name));
    }

    public String getString(String name) {
        return DataConverter.toString(_get(name));
    }

    public List<String> getStringArray( String fieldName ) {
        return DataConverter.toStringArray( _get( fieldName ));
    }

    public java.util.Date getDate(String name) {
        return DataConverter.toDate(_get(name));
    }

    public <T> Class<T> getClass(Class<T> clazz, String fieldName ) {
        return _objectToTypedClass( clazz, _get(fieldName));
    }

    public <T> T getObject( Class<T> clazz, String name) {
        return (T) _get(name);
    }

    public <T> List<T> getObjectArray( Class<T> clazz, String name ) {
        return _objectToTypedArray( clazz, _get(name));
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
    public <T> Optional<T> findObject(Class<T> clazz, String name) {
        return Optional.ofNullable(getObject(clazz, name));
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
    // BUILDER PATTERN SUPPORT

    /**
     * Create a new DataObject builder
     * @param metaObject MetaObject to associate (required for DataObject)
     * @return New DataObject.Builder instance
     */
    public static Builder builder(MetaObject metaObject) {
        return new Builder(metaObject);
    }

    /**
     * Builder pattern for DataObject creation
     */
    public static class Builder {
        private final MetaObject metaObject;
        private final Map<String, Object> values = new HashMap<>();

        public Builder(MetaObject metaObject) {
            if (metaObject == null) {
                throw new IllegalArgumentException("MetaObject is required for DataObject");
            }
            this.metaObject = metaObject;
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

        public DataObject build() {
            DataObject obj = new DataObject(metaObject);
            
            // Set all values
            values.forEach(obj::setObject);
            
            return obj;
        }
    }

    @Override
    public void validate() throws ValueException {
        if ( getMetaData() == null ) throw new IllegalStateException( "No MetaData is associated with this object" );
    }
}
