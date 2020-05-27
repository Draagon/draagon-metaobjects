/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.object.data;

import com.draagon.meta.DataTypes;
import com.draagon.meta.InvalidValueException;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.MetaDataRegistry;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.Validatable;
import com.draagon.meta.util.DataConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.*;

/**
 * Generic Map of fields and values that can be associated with a MetaObject. The values are retrieved
 * as "attributes".  You can also associate "properties" to the object that can be used for special behaviors
 * when persisting or transforming objects.
 */
public abstract class DataObjectBase implements Serializable, MetaObjectAware, Validatable {

    static final Log log = LogFactory.getLog( DataObjectBase.class );

    /** Holds a Value for the DataObject.
     * Used to know if null was explicitly set or the Value didn't exist.  Can also be enhanced to support the last
     * time a value was created, updated, read, deleted, etc.
     */
    public final class Value implements Serializable {

        private static final long serialVersionUID = 746942287755477951L;

        private DataObjectBase parent = null;
        private Object value = null;
        private DataTypes dataType = null;

        public Value(DataObjectBase parent, Object value, DataTypes dataType) {
            this.parent = parent;
            this.value = value;
            this.dataType = dataType;
        }

        public void setValue(Object value) {
            this.value = DataConverter.toType( dataType, value );
        }

        public DataTypes getDataType() { return dataType; }

        public Object getValue() {
            return value;
        }
    }

    private final Map<String, Value> valueAttributes = new java.util.concurrent.ConcurrentHashMap<String, Value>();
    private final Map<String, String> valueProperties = new java.util.concurrent.ConcurrentHashMap<String, String>();

    private boolean allowExtensions = false;
    private boolean enforceStrictness = true;

    // The MetaObject is transient, but the loader name, and object name are needed
    private transient MetaObject metaObject = null;
    private transient List<String> metaFieldNamesCache = null;
    private transient boolean metaObjectForNameFailed = false;
    private boolean hasMetaData = false;
    private String loaderName = null;
    private String objectName = null;

    /**
     * Create a generic value object that supports extensions by default
     */
    public DataObjectBase() { }

    /**
     * Create a generic value object associated to the MetaObject
     * @param mo MetaObject to associated
     */
    public DataObjectBase(MetaObject mo ) {
        setMetaData( mo );
    }

    /**
     * A generic value object with the specified name
     * @param name Name of the object
     */
    public DataObjectBase(String name ) {
        // TODO:  Do we force a MetaObject attached in the future to have the same name...?
        objectName = name;
    }

    protected static <T> T _newInstance( Class<T> clazz, MetaDataLoader loader, String objectName) {
        return (T) loader.getMetaObjectByName(objectName).newInstance();
    }

    /**
     * Return the name of the object
     * @return the object name
     */
    protected String _getObjectName() {
        return objectName;
    }

    /**
     * Whether this value object supports extensions
     * @return true if extensions are supported
     */
    protected boolean _allowsExtensions() {
        return allowExtensions;
    }

    protected void _allowExtensions( boolean allowExtensions ) {
        this.allowExtensions = allowExtensions;
    }

    protected boolean _enforcesStrictness() {
        return enforceStrictness;
    }

    protected void _enforceStrictness( boolean strict ) {
        this.enforceStrictness = strict;
    }
    
    @Override
    public void setMetaData(MetaObject mo) {

        metaObject = mo;

        // Set the values configured on the DataMetaObject
        if ( mo instanceof DataMetaObject ) {
            DataMetaObject dmo = (DataMetaObject) mo;
            this.allowExtensions = dmo.allowExtensions();
            this.enforceStrictness = dmo.isStrict();
        }

        // Flag that we have metadata set
        hasMetaData = true;

        // Clear flag on metadata failures now that we are setting it
        metaObjectForNameFailed = false;

        // Clear the meta field names cache
        metaFieldNamesCache = null;

        // Set these to handle serialization and re-attaching
        if (mo.getLoader() != null) {
            loaderName = mo.getLoader().getName();
        }
        
        objectName = mo.getName();
    }

    /**
     * Returns whether the DataObject has MetaData attached to it
     * @return true if attached, false if not
     */
    public boolean hasMetaDataAttached() {
        try {
            return (getMetaData() != null);
        } catch( MetaDataNotFoundException e ) {}
        return false;
    }

    /**
     * Return the MetaObject associated with this DataObject
     */
    @Override
    public synchronized MetaObject getMetaData() {

        // If we have the meta object, then return it
        if ( metaObject != null ) return metaObject;

        // If we don't have the MetaObject, but we already tried looking it up before, return null
        if ( metaObjectForNameFailed ) return null;

        // If the object name is set, this could be serialization, so try to reattach the MetaObject
        if ( objectName != null  ) {
            try {
                if (loaderName != null) {
                    MetaDataLoader mcl = MetaDataRegistry.getDataLoader(loaderName);
                    if (mcl != null) {
                        metaObject = mcl.getMetaObjectByName(objectName);
                    } else {
                        metaObject = MetaDataRegistry.findMetaObjectByName(objectName);
                    }
                }

            } catch (MetaDataNotFoundException e) {
                metaObjectForNameFailed = true;
                throw new RuntimeException("Could not re-attach MetaObject: " + e.getMessage(), e);
            }
        }
        // Otherwise, try to find the MetaObject by looking it up in the static MetaDataRegistry method
        else {
            try {
                // If we find the MetaObject, then attach to this DataObject
                MetaObject mo = MetaDataRegistry.findMetaObjectByName(objectName);
                setMetaData( mo );

            } catch( MetaDataNotFoundException e ) {
                metaObjectForNameFailed = true;
            }
        }

        return metaObject;
    }

    /**
     * Check to see if the field is valid and if metadata exists, but only if extensions are allowed
     * @param name Field name to check
     * @return True if the field name is valid
     */
    protected boolean isValidFieldName(String name) {

        if ( allowExtensions ) return true;

        if ( !hasMetaDataAttached()) {
            throw new IllegalArgumentException( "There is no MetaObject attached to this DataObject and it is not extendable, so field ["+name+"] cannot be read or written to");
        }
        else if ( !getMetaData().hasMetaField( name )) {
            if ( enforceStrictness ) {
                throw new IllegalArgumentException( "No field with name ["+name+"] exists on MetaObject, and this is DataObject is set to strictly enforce that it must: " + getMetaData() );
            } else {
                Set<String> ignoreSet = getIgnoreSet( getMetaData() );
                if ( !ignoreSet.contains( name )) {
                    ignoreSet.add( name );
                    log.warn( "No field with name ["+name+"] exists on MetaObject ["+getMetaData()+"], ignored field get/set history: " + ignoreSet );
                }
                return false;
            }
        }
        return true;
    }

    /** Get the ignore sets for the specified MetaObject */
    private static Set<String> getIgnoreSet( MetaObject o ) {

        final String KEY = "DataObject-getIgnoreSet";
        Set<String> set = null;

        synchronized ( o ) {
            set = (Set<String>) o.getCacheValue(KEY);
            if (set == null) {
                set = new HashSet<>();
                o.setCacheValue(o, set);
            }
        }
        return set;
    }

    /**
     * Sets an attribute of the MetaObject
     */
    protected void _setObjectAttribute(String name, Object value ) {

        // Do not store invalid field values
        if (!isValidFieldName( name )) return;

        Value v = (Value) getObjectAttributeValue( name, value );

        value = DataConverter.toType( v.dataType, value );

        // Set the value
        v.setValue(value);
    }

    /**
     * Checks is a property of this object is true based on a value of "true".  Null is false.
     * @param name Name of the property
     * @return Whether the property has a value of "true"
     */
    protected boolean _isObjectPropertyTrue(String name) {
        String s = valueProperties.get( name );
        if ( s != null && s.equalsIgnoreCase( "true" )) return true;
        return false;
    }

    /**
     * Get a property associated with this object.  Properties are used for special operations, not to define fields
     */
    protected String _getObjectProperty(String name ) {
        return valueProperties.get( name );
    }

    /**
     * Sets a property to be associated with this object.  Properties are used for special operations, not to define fields
     */
    protected void _setObjectProperty(String name, String key ) {
        valueProperties.put( name, key );
    }

    /**
     * Returns the MetaFields based on the associated MetaObject
     * @return List of MetaFields or null if no MetaObject attached
     */
    protected List<String> getMetaFieldNames() {
        if ( metaFieldNamesCache != null ) return metaFieldNamesCache;
        metaFieldNamesCache = new ArrayList<String>();
        if ( hasMetaDataAttached() ) {
            for (MetaField f : getMetaData().getMetaFields()) {
                metaFieldNamesCache.add(f.getShortName());
            }
        }
        return metaFieldNamesCache;
    }

    /**
     * Return all field names associated with this object.  Handles all MetaFields on the associated MetaObject
     * if one exists.  Also handles adding any extended fields
     *
     * @return All field names
     */
    protected Collection<String> _getObjectFieldNames() {

        // For the clear cut cases, return the arrays
        if ( hasMetaDataAttached() && !allowExtensions ) return getMetaFieldNames();
        else if ( !hasMetaDataAttached() ) return valueAttributes.keySet();

        // For the mixed case of metadata + extensions, add the extended names
        ArrayList<String> names = new ArrayList<String>();
        if ( hasMetaDataAttached() ) names.addAll( getMetaFieldNames() );
        for ( String name : valueAttributes.keySet() ) {
            if ( !names.contains( name )) names.add( name );
        }
        return names;
    }

    /**
     * Returns whether the attribute is associated with this DataObject (ignoring the MetaObject)
     * @param name Name of the attribute/value
     * @return True if it exists on this DataObject
     */
    protected boolean _hasObjectAttribute( String name ) {
        return valueAttributes.containsKey( name );
    }

    /**
     * Returns the names of all attribute values associated with this DataObject (ignoring the MetaObject)
     * @return Collection of attribute value names
     */
    protected Collection<String> _getObjectAttributes() {
        return valueAttributes.keySet();
    }

    /**
     * Retrieves an attribute of the MetaObject
     */
    protected Object _getObjectAttribute(String name) {

        if (!isValidFieldName( name )) return null;

        Value v = valueAttributes.get(name);
        if ( v != null ) {
            return v.getValue();
        }
        else {
            return null;
        }
    }

    /**
     * Creates a new DataObject.Value
     * @param name Name of the value
     * @param dataType DataType to use
     * @return Created Value
     */
    private Value createObjectAttributeValue( String name, DataTypes dataType ) {

        synchronized (valueAttributes) {
            Value v = (Value) valueAttributes.get(name);
            if (v == null) {
                v = new Value(this, null, dataType);
                valueAttributes.put(name, v);
            } else if ( v.dataType != dataType ) {
                throw new IllegalArgumentException( "A DataObject.Value of name [" + name + "] with dataType [" + v.dataType + "] already exists, cannot create a new Value with dataType [" + dataType + "]" );
            }
            return v;
        }
    }

    /** Get the data type for the specified field */
    private DataTypes getDataType( String name, Object forValue ) {

        // Use the MetaField specific type when setting as an object
        DataTypes type = DataTypes.OBJECT;
        if ( hasMetaDataAttached() && getMetaData().hasMetaField( name )) {
            type = getMetaData().getMetaField(name).getDataType();
        }

        // If no MetaData is attached, then use the value being set to define the data type
        else if ( forValue != null ) {
            return DataTypes.forValueClass( forValue.getClass() );
        }

        return type;
    }

    /**
     * Returns the Value object which contains the actual value and information
     * about when it was created and when it was last modified.
     *
     * @param name The name of the field
     * @param forValue Optional value arg to determine the DataType to assign
     * @return Return the Valuebject.Value
     */
    private Value getObjectAttributeValue(String name, Object forValue) //throws ValueException
    {
        if (!isValidFieldName( name )) return null;

        Value v = valueAttributes.get(name);
        if ( v == null ) {
            // This method is thread safe, so no need to synchronize here
            v = createObjectAttributeValue( name, getDataType( name, forValue ));
        }

        return v;
    }


    /**
     * Returns the Value object which contains the actual value and information
     * about when it was created and when it was last modified.
     *
     * @return Return the DataObject.Value
     */
    protected Value getObjectAttributeValue(String name )  {
        return getObjectAttributeValue( name, null );
    }

    /**
     * Remove a specific object attribute value
     * @param key attribute value to remove
     */
    protected Value _removeObjectAttributeValue(Object key) {
        return valueAttributes.remove(key);
    }

    /**
     * Clears all object attribute values
     */
    protected void _clearObjectAttributeValues() {
        valueAttributes.clear();
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // GET/SET HELPER METHODS

    /** If string is trimmed and empty, set it to null */
    protected String _trimStringToNull(String s ) {
        if ( s != null && s.trim().isEmpty()) s = null;
        return s;
    }

    protected <T> List<T> _getAndCreateObjectArray( Class<T> clazz, String name ) {
        List<T> current = _objectToTypedArray( clazz, _getObjectAttribute( name ));
        if ( current == null ) {
            current = new ArrayList<>();
            _setObjectAttribute( name, current );
        }
        return current;
    }

    protected <T> List<T> _objectToTypedArray( Class<T> clazz, Object o ) {
        try {
            return (List<T>) o;
        } catch( ClassCastException e ) {
            throw new InvalidValueException("Expected List of ["+clazz.getName()+"]"+
                    ", but encountered ClassCastException on object ["+o+"]"+
                    " on MetaObject ["+getMetaData().getName()+"]: "+e, e );
        }
    }

    protected <T> Class<T> _objectToTypedClass( Class<T> clazz, Object o ) {
        Class<T> out = null;
        if ( o != null ) {
            try {
                if (o instanceof Class) {
                    out = (Class<T>) o;
                }
                else if (o instanceof String) {
                    try {
                        if ( hasMetaDataAttached() ) {
                            out = getMetaData().loadClass( clazz, o.toString());
                        } else {
                            out = (Class<T>) Class.forName(o.toString());
                        }
                    } catch (ClassNotFoundException e) {
                        throw new InvalidValueException("ClassNotFoundException for value ["+o+"]"
                                +" on MetaObject ["+getMetaData().getName()+"]: ");
                    }
                } else {
                    throw new InvalidValueException("Expected Class or String, but value was "+
                            "[" + o.getClass().getName() + "] on MetaObject "+
                            "[" + getMetaData().getName() + "]");
                }
            } catch (ClassCastException e) {
                throw new InvalidValueException("Expected Class of ["+clazz.getName()+"]"+
                        ", but found class ["+o.getClass().getName()+"]"+
                        " on MetaObject ["+getMetaData().getName()+"]");
            }
        }
        return out;
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // MISC METHODS

    /**
     * Determines if this object is equivalent to the passed in object.
     * If both objects are not associated to a MetaObject, it just
     * compares the attribute key values for equality.  If MetaObjects
     * are attached then it compares each value for all MetaFields.
     *
     * @param o Object to compare
     * @return True if equals, false if not
     */
    @Override
    public boolean equals( Object o ) {

        if (!(o instanceof DataObjectBase))
            return false;

        DataObjectBase v = (DataObjectBase) o;
        MetaObject mo = getMetaData();
        MetaObject mv = v.getMetaData();

        // If they are mismatched on having MetaData attached, they are not equal
        if ( mo == null && mv != null || mo != null && mv == null )
            return false;

        // Compare each field in the MetaObject, if they have the same type, subtype, name
        if ( mo != null
                && mo.isSameTypeSubTypeName( mv )
                && compareValues(v)) {

            return true;
        }

        // Compare the object names && attribute map
        else if ( mo == null
                && objectName.equals( v.objectName )
                && compareValues(v)) {

            return true;
        }

        return false;
    }

    private boolean compareValues(DataObjectBase v) {

        // Get a union of all field names
        Set<String> fields = new HashSet<>( valueAttributes.keySet() );
        fields.addAll( v.valueAttributes.keySet() );

        // Compare all fields' values
        for ( String f : fields ) {
            if ( !compareValue(
                    _getObjectAttribute( f ),
                    v._getObjectAttribute( f ))) {
                return false;
            }
        }

        return true;
    }

    /** Compare one value to another */
    private boolean compareValue( Object o1, Object o2 ) {
        if (( o1 == null && o2 == null )
                || ( o1 != null && o1.equals( o2 ))) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        
        StringBuilder b = new StringBuilder();

        try {

            b.append("[").append(this.getClass().getSimpleName())
                    .append(":").append( _getObjectName() ).append("]");

            boolean first = true;
            b.append('{');
            for (String name : _getObjectFieldNames() ) {
                if (first) {
                    first = false;
                } else {
                    b.append(',');
                }

                b.append(name);
                b.append(':');
                b.append(_getObjectAttribute(name));
            }
            b.append('}');
        } catch (MetaDataException e) {
            // TODO: Eventually put all attributes here
        }

        return b.toString();
    }
}
