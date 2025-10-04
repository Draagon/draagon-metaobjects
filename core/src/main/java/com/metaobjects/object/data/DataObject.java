
package com.metaobjects.object.data;

import com.metaobjects.ValueException;
import com.metaobjects.object.MetaObject;
import com.metaobjects.util.DataConverter;

import java.util.*;
import java.util.Arrays;

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

    protected void _setBoolean(String name, Boolean value) {
        if (isArrayField(name)) {
            // Field is defined as array - convert to single-element list
            List<Boolean> list = value != null ? Arrays.asList(value) : null;
            _set(name, list);
        } else {
            // Regular boolean field
            _set(name, value);
        }
    }

    protected void _setByte(String name, Byte value)  {
        if (isArrayField(name)) {
            // Field is defined as array - convert to single-element list
            List<Byte> list = value != null ? Arrays.asList(value) : null;
            _set(name, list);
        } else {
            // Regular byte field
            _set(name, value);
        }
    }

    protected void _setShort(String name, Short value) {
        if (isArrayField(name)) {
            // Field is defined as array - convert to single-element list
            List<Short> list = value != null ? Arrays.asList(value) : null;
            _set(name, list);
        } else {
            // Regular short field
            _set(name, value);
        }
    }

    protected void _setInt(String name, Integer value) {
        if (isArrayField(name)) {
            // Field is defined as array - convert to single-element list
            List<Integer> list = value != null ? Arrays.asList(value) : null;
            _set(name, list);
        } else {
            // Regular integer field
            _set(name, value);
        }
    }

    protected void _setInteger(String name, Integer value) {
        _setInt(name, value);
    }

    protected void _setLong(String name, Long value) {
        if (isArrayField(name)) {
            // Field is defined as array - convert to single-element list
            List<Long> list = value != null ? Arrays.asList(value) : null;
            _set(name, list);
        } else {
            // Regular long field
            _set(name, value);
        }
    }

    protected void _setFloat(String name, Float value) {
        if (isArrayField(name)) {
            // Field is defined as array - convert to single-element list
            List<Float> list = value != null ? Arrays.asList(value) : null;
            _set(name, list);
        } else {
            // Regular float field
            _set(name, value);
        }
    }

    protected void _setDouble(String name, Double value) {
        if (isArrayField(name)) {
            // Field is defined as array - convert to single-element list
            List<Double> list = value != null ? Arrays.asList(value) : null;
            _set(name, list);
        } else {
            // Regular double field
            _set(name, value);
        }
    }

    protected void _setString(String name, String value) {
        if (isArrayField(name)) {
            // Field is defined as array - parse string into list
            List<String> list = DataConverter.toStringArray(value);
            _set(name, list);
        } else {
            // Regular string field
            _set(name, value);
        }
    }

    protected void _setStringArray(String name, List<String> value ) {
        _set( name, value );
    }

    protected void _setIntArray(String name, List<Integer> value) {
        _set(name, value);
    }

    protected void _setLongArray(String name, List<Long> value) {
        _set(name, value);
    }

    protected void _setBooleanArray(String name, List<Boolean> value) {
        _set(name, value);
    }

    protected void _setDoubleArray(String name, List<Double> value) {
        _set(name, value);
    }

    protected void _setFloatArray(String name, List<Float> value) {
        _set(name, value);
    }

    protected void _setDate(String name, java.util.Date value) {
        _set(name, value);
    }

    protected <T> void _setClass( String name, Class<T> value ) {
        _set(name, value );
    }

    protected <T> void _setObject(String name, T value) {
        _set(name, value);
    }

    protected <T> void _setObjectArray(String name, List<T> value) {
        _set(name, value);
    }

    protected <T> void _addToObjectArray( String name, T value ) {
        List<T> current = _getAndCreateObjectArray( (Class<T>) value.getClass(), name );
        current.add( value );
    }


    //////////////////////////////////////////////////////////////
    // GETTER VALUES

    private Object _get(String name) {
        return _getObjectAttribute(name);
    }

    protected Boolean _getBoolean(String name) {
        Object value = _get(name);

        // Smart fallback: if field is an array, return first element
        if (isArrayField(name) && value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) return null;
            return DataConverter.toBoolean(list.get(0));
        }

        return DataConverter.toBoolean(value); // Standard behavior
    }

    protected Byte _getByte(String name) {
        Object value = _get(name);

        // Smart fallback: if field is an array, return first element
        if (isArrayField(name) && value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) return null;
            return DataConverter.toByte(list.get(0));
        }

        return DataConverter.toByte(value); // Standard behavior
    }

    protected Short _getShort(String name) {
        Object value = _get(name);

        // Smart fallback: if field is an array, return first element
        if (isArrayField(name) && value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) return null;
            return DataConverter.toShort(list.get(0));
        }

        return DataConverter.toShort(value); // Standard behavior
    }

    protected Integer _getInt(String name) {
        Object value = _get(name);

        // Smart fallback: if field is an array, return first element
        if (isArrayField(name) && value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) return null;
            return DataConverter.toInt(list.get(0));
        }

        return DataConverter.toInt(value); // Standard behavior
    }

    protected Integer _getInteger(String name) {
        return _getInt(name);
    }

    protected Long _getLong(String name) {
        Object value = _get(name);

        // Smart fallback: if field is an array, return first element
        if (isArrayField(name) && value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) return null;
            return DataConverter.toLong(list.get(0));
        }

        return DataConverter.toLong(value); // Standard behavior
    }

    protected Float _getFloat(String name) {
        Object value = _get(name);

        // Smart fallback: if field is an array, return first element
        if (isArrayField(name) && value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) return null;
            return DataConverter.toFloat(list.get(0));
        }

        return DataConverter.toFloat(value); // Standard behavior
    }

    protected Double _getDouble(String name)  {
        Object value = _get(name);

        // Smart fallback: if field is an array, return first element
        if (isArrayField(name) && value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) return null;
            return DataConverter.toDouble(list.get(0));
        }

        return DataConverter.toDouble(value); // Standard behavior
    }

    protected String _getString(String name) {
        Object value = _get(name);

        // Smart fallback: if field is an array, return comma-delimited string
        if (isArrayField(name) && value instanceof List) {
            return DataConverter.toString(value); // Already handles List → "item1,item2,item3"
        }

        return DataConverter.toString(value); // Standard behavior
    }

    protected List<String> _getStringArray( String fieldName ) {
        return DataConverter.toStringArray( _get( fieldName ));
    }

    protected List<Integer> _getIntArray(String name) {
        return DataConverter.toIntArray(_get(name));
    }

    protected List<Long> _getLongArray(String name) {
        return DataConverter.toLongArray(_get(name));
    }

    protected List<Boolean> _getBooleanArray(String name) {
        return DataConverter.toBooleanArray(_get(name));
    }

    protected List<Double> _getDoubleArray(String name) {
        return DataConverter.toDoubleArray(_get(name));
    }

    protected List<Float> _getFloatArray(String name) {
        return DataConverter.toFloatArray(_get(name));
    }

    protected java.util.Date _getDate(String name) {
        return DataConverter.toDate(_get(name));
    }

    protected <T> Class<T> _getClass(Class<T> clazz, String fieldName ) {
        return _objectToTypedClass( clazz, _get(fieldName));
    }

    protected <T> T _getObject( Class<T> clazz, String name) {
        return (T) _get(name);
    }

    protected <T> List<T> _getObjectArray( Class<T> clazz, String name ) {
        return _objectToTypedArray( clazz, _get(name));
    }

    @Override
    public void validate() throws ValueException {
        if ( getMetaData() == null ) throw new IllegalStateException( "No MetaData is associated with this object" );
    }
}
