
package com.metaobjects.object.data;

import com.metaobjects.ValueException;
import com.metaobjects.object.MetaObject;
import com.metaobjects.util.DataConverter;

import java.util.*;

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
        _set(name, value);
    }

    protected void _setByte(String name, Byte value)  {
        _set(name, value);
    }

    protected void _setShort(String name, Short value) {
        _set(name, value);
    }

    protected void _setInt(String name, Integer value) {
        _set(name, value);
    }

    protected void _setInteger(String name, Integer value) {
        _set(name, value);
    }

    protected void _setLong(String name, Long value) {
        _set(name, value);
    }

    protected void _setFloat(String name, Float value) {
        _set(name, value);
    }

    protected void _setDouble(String name, Double value) {
        _set(name, value);
    }

    protected void _setString(String name, String value) {
        _set(name, value);
    }

    protected void _setStringArray(String name, List<String> value ) {
        _set( name, value );
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
        return DataConverter.toBoolean(_get(name));
    }

    protected Byte _getByte(String name) {
        return DataConverter.toByte(_get(name));
    }

    protected Short _getShort(String name) {
        return DataConverter.toShort(_get(name));
    }

    protected Integer _getInt(String name) {
        return DataConverter.toInt(_get(name));
    }

    protected Integer _getInteger(String name) {
        return _getInt(name);
    }

    protected Long _getLong(String name) {
        return DataConverter.toLong(_get(name));
    }

    protected Float _getFloat(String name) {
        return DataConverter.toFloat(_get(name));
    }

    protected Double _getDouble(String name)  {
        return DataConverter.toDouble(_get(name));
    }

    protected String _getString(String name) {
        return DataConverter.toString(_get(name));
    }

    protected List<String> _getStringArray( String fieldName ) {
        return DataConverter.toStringArray( _get( fieldName ));
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
