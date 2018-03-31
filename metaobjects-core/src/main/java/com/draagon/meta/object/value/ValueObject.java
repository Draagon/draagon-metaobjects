/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.object.value;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.field.MetaFieldTypes;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.MetaDataRegistry;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.util.Converter;

import java.io.Serializable;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic Map of fields and values that can be associated with a MetaObject. The values are retrieved
 * as "attributes".  You can also associate "properties" to the object that can be used for special behaviors
 * when persisting or transforming objects.
 */
public class ValueObject implements java.util.Map<String, Object>, Serializable, MetaObjectAware {

    private static final long serialVersionUID = 6888178049723946186L;

    /** Holds a Value for the ValueObject.  
     * Used to know if null was explicitly set or the Value didn't exist.  Can also be enhanced to support the last 
     * time a value was created, updated, read, deleted, etc.
     */
    public class Value implements Serializable {

        private static final long serialVersionUID = 746942287755477951L;
        private ValueObject parent = null;
        private Object value = null;

        public Value(ValueObject parent, Object value) {
            this.parent = parent;
            this.value = value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }
    
    private final java.util.Map<String, Value> mAttributes = new java.util.concurrent.ConcurrentHashMap<String, Value>();
    private final java.util.Map<String, String> mProperties = new java.util.concurrent.ConcurrentHashMap<String, String>();

    private boolean allowExtensions = false;

    // The MetaObject is transient, but the loader name, and object name are needed
    private transient MetaObject mMetaObject = null;
    private transient java.util.List<String> metaFieldNamesCache = null;
    private transient boolean metaObjectForNameFailed = false;
    private boolean hasMetaData = false;
    private String mLoaderName = null;
    private String mObjectName = null;

    /**
     * Create a generic value object that supports extensions by default
     */
    public ValueObject() {
        allowExtensions = true;
    }

    /**
     * Create a generic value object associated to the MetaObject
     * @param mo MetaObject to associated
     */
    public ValueObject( MetaObject mo ) {
        this( mo, false );
    }

    /**
     * Create a value object associated to the MetaObject and set flag on extensions
     * @param mo MetaObject to associated
     * @param allowExtensions Whether to allow extensions
     */
    public ValueObject( MetaObject mo, boolean allowExtensions ) {
        setMetaData( mo );
        this.allowExtensions = allowExtensions;
    }

    /**
     * A generic value object with the specified name
     * @param name Name of the object
     */
    public ValueObject( String name ) {
        // TODO:  Do we force a MetaObject attached in the future to have the same name...?
        mObjectName = name;
        allowExtensions = true;
    }

    /**
     * Return the name of the object
     * @return the object name
     */
    public String getObjectName() {
        return mObjectName;
    }

    /**
     * Set whether to allow extensions (only works when no metadata is attached
     */
    public void allowExtensions( boolean allowExtensions ) {
        if ( !hasMetaDataAttached() ) throw new RuntimeException( "Cannot turn off extensions for ValueObject [" + mObjectName + "] as not MetaObject is attached");
        this.allowExtensions = allowExtensions;
    }

    /**
     * Whether this value object supports extensions
     * @return true if extensions are supported
     */
    public boolean allowsExtensions() {
        return allowExtensions;
    }
    
    @Override
    public void setMetaData(MetaObject mo) {

        mMetaObject = mo;

        // Flag that we have metadata set
        hasMetaData = true;
        // Clear flag on metadata failures now that we are setting it
        metaObjectForNameFailed = false;
        // Clear the meta field names cache
        metaFieldNamesCache = null;

        // Set these to handle serialization and re-attaching
        if (mo.getLoader() != null) {
            mLoaderName = mo.getLoader().getName();
        }
        mObjectName = mo.getName();
    }

    /**
     * Returns whether the ValueObject has MetaData attached to it
     * @return true if attached, false if not
     */
    public boolean hasMetaDataAttached() {
        try {
            return (getMetaData() != null);
        } catch( MetaDataNotFoundException e ) {}
        return false;
    }

    /**
     * Return the MetaObject associated with this ValueObject
     */
    @Override
    public synchronized MetaObject getMetaData() {

        // If we have the meta object, then return it
        if ( mMetaObject != null ) return mMetaObject;

        // If we don't have the object, but we already tried looking it up before, return null
        if (mMetaObject == null && metaObjectForNameFailed ) {
            return null;
        }

        // If the object name is set, this could be serialization, so try to reattach the MetaObject
        if ( mObjectName != null  ) {
            try {
                if (mLoaderName != null) {
                    MetaDataLoader mcl = MetaDataRegistry.getDataLoader(mLoaderName);
                    if (mcl != null) {
                        mMetaObject = mcl.getMetaDataByName(MetaObject.class, mObjectName);
                    } else {
                        mMetaObject = MetaDataRegistry.findMetaDataByName(MetaObject.class, mObjectName);
                    }
                }

            } catch (MetaDataNotFoundException e) {
                metaObjectForNameFailed = true;
                throw new RuntimeException("Could not re-attach MetaObject: " + e.getMessage(), e);
            }
        }
        // Otherwise, try to find the MetaObject by looking it up in the static MetaDataLoader method
        else {
            try {
                // If we find the MetaObject, then attach to this ValueObject
                MetaObject mo = MetaObject.forName(mObjectName);
                setMetaData( mo );

            } catch( MetaDataNotFoundException e ) {
                metaObjectForNameFailed = true;
            }
        }

        // The setMetaData call above would set this, but it's a bit sloppy (dtm)
        return mMetaObject;
    }

    //public String getMetaDataLoaderName() {
    //    return mLoaderName;
    //}
    
    //public String getMetaObjectName() {
    //    return mObjectName;
    //}

    /**
     * Sets an attribute of the MetaObject
     */
    public void setObjectAttribute(String name, Object value, int type ) {
        
        Value v = (Value) getObjectAttributeValue(name);                      

        // Convert to the proper object type
        if (hasMetaDataAttached()) {
            try {
                value = Converter.toType(getMetaData().getMetaField(name).getType(), value);
            }catch( MetaDataNotFoundException e ) {
                value = Converter.toType(type, value);
            }
        } else {
            value = Converter.toType(type, value);
        }

        // Set the value
        v.setValue(value);
    }

    /**
     * Checks is a property of this object is true based on a value of "true".  Null is false.
     * @param name Name of the property
     * @return Whether the property has a value of "true"
     */
    public boolean isObjectPropertyTrue( String name ) {
        String s = mProperties.get( name );
        if ( s != null && s.equalsIgnoreCase( "true" )) return true;
        return false;
    }

    /**
     * Get a property associated with this object.  Properties are used for special operations, not to define fields
     */
    public String getObjectProperty( String name ) {
        return mProperties.get( name );
    }

    /**
     * Sets a property to be associated with this object.  Properties are used for special operations, not to define fields
     */
    public void setObjectProperty( String name, String key ) {
        mProperties.put( name, key );
    }

    /**
     * Return the MetaField named, if this ValueObject has an associated MetaObject
     */
    /* protected MetaField getMetaField( String name ) {
        if ( !hasMetaDataAttached()) return null;
        return getMetaData().getMetaField(name);
    }*/

    /**
     * Returns the MetaFields based on the associated MetaObject
     * @return List of MetaFields or null if no MetaObject attached
     */
    /* public Collection<MetaField> getMetaFields() {
        if ( !hasMetaDataAttached()) return null;
        return getMetaData().getMetaFields();
    }*/

    /**
     * Returns the MetaFields based on the associated MetaObject
     * @return List of MetaFields or null if no MetaObject attached
     */
    protected java.util.List<String> getMetaFieldNames() {
        if ( metaFieldNamesCache != null ) return metaFieldNamesCache;
        metaFieldNamesCache = new java.util.ArrayList<String>();
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
    public java.util.Collection<String> getObjectFieldNames() {

        // For the clear cut cases, return the arrays
        if ( hasMetaDataAttached() && !allowExtensions ) return getMetaFieldNames();
        else if ( !hasMetaDataAttached() ) return mAttributes.keySet();

        // For the mixed case of metadata + extensions, add the extended names
        java.util.ArrayList<String> names = new java.util.ArrayList<String>();
        if ( hasMetaDataAttached() ) names.addAll( getMetaFieldNames() );
        for ( String name : mAttributes.keySet() ) {
            if ( !names.contains( name )) names.add( name );
        }
        return names;
    }

    /**
     * Returns whether the specified MetaField exists on the MetaObject attached
     * @param name Name of the MetaField
     * @return true if exists, false if not or no MetaObject is attached
     */
    //public boolean hasMetaField( String name ) {
    //    if ( !hasMetaDataAttached()) return false;
    //    return getMetaData().hasMetaField(name);
    //}

    /**
     * Returns whether the attribute is associated with this ValueObject (ignoring the MetaObject)
     * @param name Name of the attribute/value
     * @return True if it exists on this ValueObject
     */
    public boolean hasObjectAttribute( String name ) {
        return mAttributes.containsKey( name );
    }

    /**
     * Returns the names of all attribute values associated with this ValueObject (ignoring the MetaObject)
     * @return Collection of attribute value names
     */
    public java.util.Collection<String> getObjectAttributes() {
        return mAttributes.keySet();
    }

    /**
     * Retrieves an attribute of the MetaObject
     */
    public Object getObjectAttribute(String name) {
        Value v = mAttributes.get(name);
        if ( v != null ) {
            return v.getValue();
        }
        else {
            return null;
        }
    }

    /**
     * Returns the Value object which contains the actual value and information
     * about when it was created and when it was last modified.
     */
    protected Value getObjectAttributeValue(String name ) //throws ValueException
    {
        // Force an exception if extensions are not allowed and the MetaField didn't exist
        if ( !allowExtensions ) getMetaData().getMetaField( name );

        synchronized (mAttributes) {
            Value v = (Value) mAttributes.get(name);
            if (v == null) {
                v = new Value(this, null);
                mAttributes.put(name, v);
            }
            return v;
        }
    }

    /*public Object getObjectValue( String name )
     throws MetaException
     {
        return getMetaField( name ).getValue( this );
     }

     public void setObjectValue( String name, Object value )
     throws MetaException
     {
        getMetaField( name ).setValue( this, value );
     }*/
    
    //////////////////////////////////////////////////////////////
    // PRIMITIVE SETTER VALUES
    public void setBoolean(String name, boolean value) //throws ValueException
    {
        setBoolean(name, new Boolean( value ));
    }

    public void setByte(String name, byte value) //throws MetaException
    {
        setByte(name, new Byte(value));
    }

    public void setShort(String name, short value) //throws MetaException
    {
        setShort(name, new Short(value));
    }

    public void setInt(String name, int value) //throws MetaException
    {
        setInt(name, new Integer(value));
    }

    public void setLong(String name, long value) //throws MetaException
    {
        setLong(name, new Long(value));
    }

    public void setFloat(String name, float value) //throws MetaException
    {
        setFloat(name, new Float(value));
    }

    public void setDouble(String name, double value) //throws MetaException
    {
        setDouble(name, new Double(value));
    }

    //////////////////////////////////////////////////////////////
    // SETTER VALUES
    public void setBoolean(String name, Boolean value) //throws MetaException
    {
        setObjectAttribute(name, value, MetaFieldTypes.BOOLEAN);
        //getMetaField( name ).setBoolean( this, value );
    }

    public void setByte(String name, Byte value) //throws MetaException
    {
        setObjectAttribute(name, value, MetaFieldTypes.BYTE);
        //getMetaField( name ).setByte( this, value );
    }

    public void setShort(String name, Short value) //throws MetaException
    {
        setObjectAttribute(name, value, MetaFieldTypes.SHORT);
        //getMetaField( name ).setShort( this, value );
    }

    public void setInt(String name, Integer value) //throws MetaException
    {
        setObjectAttribute(name, value, MetaFieldTypes.INT);
        //getMetaField( name ).setInt( this, value );
    }

    public void setInteger(String name, Integer value) //throws MetaException
    {
        setObjectAttribute(name, value, MetaFieldTypes.INT);
        //setInt( name, value );
    }

    public void setLong(String name, Long value) //throws MetaException
    {
        setObjectAttribute(name, value, MetaFieldTypes.LONG);
        //getMetaField( name ).setLong( this, value );
    }

    public void setFloat(String name, Float value) //throws MetaException
    {
        setObjectAttribute(name, value, MetaFieldTypes.FLOAT);
        //getMetaField( name ).setFloat( this, value );
    }

    public void setDouble(String name, Double value) //throws MetaException
    {
        setObjectAttribute(name, value, MetaFieldTypes.DOUBLE);
        //getMetaField( name ).setDouble( this, value );
    }

    public void setString(String name, String value) //throws MetaException
    {
        setObjectAttribute(name, value, MetaFieldTypes.STRING);
        //getMetaField( name ).setString( this, value );
    }

    public void setDate(String name, java.util.Date value) //throws MetaException
    {
        setObjectAttribute(name, value, MetaFieldTypes.DATE);
        //getMetaField( name ).setDate( this, value );
    }

    public void setObject(String name, Object value) //throws MetaException
    {
        // Use the MetaField specific type when setting as an object
        int type = MetaFieldTypes.OBJECT;
        if ( hasMetaDataAttached() && getMetaData().hasMetaField( name )) {
            type = getMetaData().getMetaField( name ).getType();
        }

        setObjectAttribute(name, value, MetaFieldTypes.OBJECT);
        //getMetaField( name ).setObject( this, value );
    }

    //////////////////////////////////////////////////////////////
    // GETTER VALUES
    public Boolean getBoolean(String name) //throws MetaException
    {
        return Converter.toBoolean(getObjectAttribute(name));
        //return getMetaField(name).getBoolean(this);
    }

    public Byte getByte(String name) //throws MetaException
    {
        return Converter.toByte(getObjectAttribute(name));
        //return getMetaField(name).getByte(this);
    }

    public Short getShort(String name) //throws MetaException
    {
        return Converter.toShort(getObjectAttribute(name));
        //return getMetaField( name ).getShort( this );
    }

    public Integer getInt(String name) //throws MetaException
    {
        return Converter.toInt(getObjectAttribute(name));
        //return getMetaField( name ).getInt( this );
    }

    public Integer getInteger(String name) //throws MetaException
    {
        return getInt(name);
        //return getInt( name );
    }

    public Long getLong(String name) //throws MetaException
    {
        return Converter.toLong(getObjectAttribute(name));
        //return getMetaField( name ).getLong( this );
    }

    public Float getFloat(String name) //throws MetaException
    {
        return Converter.toFloat(getObjectAttribute(name));
        //return getMetaField( name ).getFloat( this );
    }

    public Double getDouble(String name) //throws MetaException
    {
        return Converter.toDouble(getObjectAttribute(name));
        //return getMetaField( name ).getDouble( this );
    }

    public String getString(String name) //throws MetaException
    {
        return Converter.toString(getObjectAttribute(name));
        //return getMetaField( name ).getString( this );
    }

    public java.util.Date getDate(String name) //throws MetaException
    {
        return Converter.toDate(getObjectAttribute(name));
        //return getMetaField( name ).getDate( this );
    }

    public Object getObject(String name) //throws MetaException
    {
        return getObjectAttribute(name);
        //return getMetaField( name ).getObject( this );
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // MISC
    public String toString() {
        
        StringBuilder b = new StringBuilder();

        try {

            b.append("[");
            //String ref = getObjectRef();
            //if ( ref != null )
            //	b.append( ref );
            //else
            //{
            //b.append( "{NEW}" );
            //b.append( '@' );
            b.append( getObjectName() );
            //}
            b.append("]");

            boolean first = true;
            b.append('{');
            for (String name : getObjectFieldNames() ) {
                if (first) {
                    first = false;
                } else {
                    b.append(',');
                }

                b.append(name);
                b.append(':');
                b.append(getString(name));
            }
            b.append('}');
        } catch (MetaDataException e) {
            // TODO: Eventually put all attributes here
        }

        return b.toString();
    }

    //////////////////////////////////////////////////////////
    // MAP METHODS
    @Override
    public void clear() {
        mAttributes.clear();
        //for (String name : getObjectFieldNames()) {
        //    setObject(name, null);
        //}
    }

    @Override
    public boolean containsKey(Object key) {
        return getObjectFieldNames().contains(key);
    }

    @Override
    public boolean containsValue(Object value) {

        for( String name : getObjectFieldNames() ) {
            if (value == null && getObject(name) == null) {
                return true;
            }
            if (value != null && value.equals(getObject(name))) {
                return true;
            }
        }
        return false;
    }


    /**
     * Creates a Map.Entry for a specific MetaField and Object
     */
    public class AttributeEntry implements java.util.Map.Entry<String, Object> {

        private String attr = null;

        public AttributeEntry(String attr) {
            this.attr = attr;
        }

        @Override
        public String getKey() {
            return attr;
        }

        @Override
        public Object getValue() {
            return getObject(attr);
        }

        @Override
        public Object setValue(Object value) {
            Object old = getObject(attr);
            setObject(attr,value);
            return old;
        }
    }

    @Override
    public java.util.Set<java.util.Map.Entry<String, Object>> entrySet() {

        java.util.Set<java.util.Map.Entry<String, Object>> s = new java.util.HashSet<java.util.Map.Entry<String, Object>>();
        for (String name : getObjectFieldNames()) {
            s.add(new AttributeEntry(name));
        }
        return s;
    }

    @Override
    public Object get(Object key) {
        return getObject(String.valueOf(key));
    }

    @Override
    public boolean isEmpty() {
        if ( getObjectFieldNames().size() == 0 ) return true;
        for ( String key : mAttributes.keySet() ) {
            if ( mAttributes.get(key).getValue() != null ) return false;
        }
        return true;
    }

    @Override
    public java.util.Set<String> keySet() {
        return new java.util.HashSet<String>( getObjectFieldNames() );
    }

    @Override
    public Object put(String key, Object value) {
        Object old = getObject( key );
        setObject( key, value );
        return old;
    }

    @Override
    public void putAll(java.util.Map<? extends String, ? extends Object> m) {
        for (String key : m.keySet()) {
            put(key, m.get(key));
        }
    }

    @Override
    public Object remove(Object key) {
        return mAttributes.remove(key);
    }

    @Override
    public int size() {
        return getObjectFieldNames().size();
    }

    @Override
    public java.util.Collection<Object> values() {
        java.util.Collection<Object> s = new java.util.ArrayList<Object>();
        for (String name : getObjectFieldNames()) {
            s.add(getObject(name));
        }
        return s;
    }
}
