/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.object.managed;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObjectNotFoundException;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.*;
import com.draagon.meta.manager.ObjectManager;
//import com.draagon.meta.manager.*;
import com.draagon.meta.util.Converter;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ManagedObject implements Map<String, Object>, Serializable, MetaObjectAware {

    private static final long serialVersionUID = 6888178049723946186L;

    //private static Log log = LogFactory.getLog( MetaObject.class );
    public class Value implements Serializable {

        private static final long serialVersionUID = 746942287755477951L;
        private ManagedObject parent = null;
        private Object value = null;
        private boolean isModified = false;
        private boolean isNew = false;
        private long creationDt = 0;
        private long modifiedDt = 0;

        public Value(ManagedObject parent, Object value) {
            this.parent = parent;
            this.value = value;

            isModified = true;
            isNew = true;
            creationDt = System.currentTimeMillis();
            modifiedDt = System.currentTimeMillis();
        }

        public void setValue(Object value) {
            this.value = value;

            setModified(true);
            parent.setModified(true);
        }

        public Object getValue() {
            return value;
        }

        public long getCreationTime() {
            return creationDt;
        }

        public long getModifiedTime() {
            return modifiedDt;
        }

        public boolean isModified() {
            return isModified;
        }

        public boolean isNew() {
            return isNew;
        }

        public void setModified(boolean state) {
            isModified = state;
        }

        public void setNew(boolean state) {
            isNew = state;
        }
    }
    
    //private String mId = null;
    private final Map<String, Value> mAttributes = new ConcurrentHashMap<String, Value>();
    
    private boolean mDeleted = false;
    private boolean mModified = false;
    private boolean mNew = true;
    // The MetaObject is transient, but the loader name, package name, and class name are needed
    private transient MetaObject mMetaObject = null;
    private String mLoaderName = null;
    //private String mPackageName = null;
    private String mObjectName = null;
    private transient ObjectManager mManager = null;

    public ManagedObject() {
        //mId = "";
        mNew = true;
    }

    public void attachObjectManager(ObjectManager mm) {
        mManager = mm;
    }

    public ObjectManager getObjectManager() {
        return mManager;
    }
     
    public String getObjectRef()
    {
        //return mId;
        try {
            return getObjectManager().getObjectRef( this ).toString();
        } catch( Exception e ) {
            return null;
        }
    }
    
    @Override
    public void setMetaData(MetaObject mc) {
        mMetaObject = mc;

        if (mc.getLoader() != null) {
            mLoaderName = mc.getLoader().getName();
        }

        mObjectName = mc.getName();
    }

    @Override
    public synchronized MetaObject getMetaData() {
        if (mMetaObject == null
                && mObjectName == null) {
            return null;
        }
        // throw new RuntimeException( "MetaObject is not linked to a MetaObject" );

        if (mMetaObject == null) {
            try {
                if (mLoaderName != null) {
                    MetaDataLoader mcl = MetaDataLoader.getDataLoader(mLoaderName);
                    if (mcl != null) {
                        mMetaObject = mcl.getMetaDataByName( MetaObject.class, mObjectName);
                    }
                }

                mMetaObject = MetaObject.forName(mObjectName);
            } catch (MetaObjectNotFoundException e) {
                throw new RuntimeException("Could not re-attach MetaObject: " + e.getMessage(), e);
            }
        }

        return mMetaObject;
    }

    public String getMetaDataLoaderName() {
        return mLoaderName;
    }

    //public String getMetaPackageName()
    //{
    //      return mPackageName;
    //}
    public String getMetaObjectName() {
        return mObjectName;
    }

    /**
     * Sets an attribute of the MetaObject
     */
    public void setObjectAttribute(String name, Object attr) {
        
        Value v = (Value) getObjectAttributeValue(name);                      

        // Convert to the proper object type       
        attr = Converter.toType(getMetaField(name).getType(), attr);

        // Set the value
        v.setValue(attr);
    }
    
    protected MetaField getMetaField( String name ) {
        return MetaObject.forObject(this).getMetaField(name);
    }

    public Collection<MetaField> getMetaFields() {
        return MetaObject.forObject(this).getMetaFields();
    }
    
    public boolean hasMetaField( String name ) {
        return MetaObject.forObject(this).hasMetaField(name);
    }
            
    /**
     * Retrieves an attribute of the MetaObject
     */
    public Object getObjectAttribute(String name) {
        Value v = (Value) getObjectAttributeValue(name);
        return v.getValue();
    }

    /**
     * Returns the Value object which contains the actual value and information
     * about when it was created and when it was last modified.
     */
    protected Value getObjectAttributeValue(String name) //throws ValueException
    {
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
        setBoolean(name, new Boolean(value));
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
        setObjectAttribute(name, value);
        //getMetaField( name ).setBoolean( this, value );
    }

    public void setByte(String name, Byte value) //throws MetaException
    {
        setObjectAttribute(name, value);
        //getMetaField( name ).setByte( this, value );
    }

    public void setShort(String name, Short value) //throws MetaException
    {
        setObjectAttribute(name, value);
        //getMetaField( name ).setShort( this, value );
    }

    public void setInt(String name, Integer value) //throws MetaException
    {
        setObjectAttribute(name, value);
        //getMetaField( name ).setInt( this, value );
    }

    public void setInteger(String name, Integer value) //throws MetaException
    {
        setObjectAttribute(name, value);
        //setInt( name, value );
    }

    public void setLong(String name, Long value) //throws MetaException
    {
        setObjectAttribute(name, value);
        //getMetaField( name ).setLong( this, value );
    }

    public void setFloat(String name, Float value) //throws MetaException
    {
        setObjectAttribute(name, value);
        //getMetaField( name ).setFloat( this, value );
    }

    public void setDouble(String name, Double value) //throws MetaException
    {
        setObjectAttribute(name, value);
        //getMetaField( name ).setDouble( this, value );
    }

    public void setString(String name, String value) //throws MetaException
    {
        setObjectAttribute(name, value);
        //getMetaField( name ).setString( this, value );
    }

    public void setDate(String name, Date value) //throws MetaException
    {
        setObjectAttribute(name, value);
        //getMetaField( name ).setDate( this, value );
    }

    public void setObject(String name, Object value) //throws MetaException
    {
        setObjectAttribute(name, value);
        //getMetaField( name ).setObject( this, value );
    }

    //////////////////////////////////////////////////////////////
    // GETTER VALUES
    public Boolean getBoolean(String name) //throws MetaException
    {
        return getMetaField(name).getBoolean(this);
    }

    public Byte getByte(String name) //throws MetaException
    {
        return getMetaField(name).getByte(this);
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

    public Date getDate(String name) //throws MetaException
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
    // MODIFICATION FLAGS
    public void setDeleted(boolean value) {
        mDeleted = value;
    }

    public boolean isDeleted() {
        return mDeleted;
    }

    public void setModified(boolean value) {
        mModified = value;

        // Set all the values
        if (value == false) {
            synchronized (mAttributes) {
                for (Value v : mAttributes.values()) {
                    v.setModified(value);
                }
            }
        }
    }

    // Return whether the specified field is modified
    public boolean isFieldModified(String name) {
        return getObjectAttributeValue(name).isModified();
    }

    public boolean isModified() {
        return mModified;
    }

    public void setNew(boolean state) {
        mNew = state;

        if (state == false) {
            synchronized (mAttributes) {
                // Set all the values to be non-modified
                for (Value v : mAttributes.values()) {
                    v.setNew(state);
                }
            }
        }
    }

    public boolean isNew() {
        return mNew;
    }

    public long getCreationTime() {
        throw new UnsupportedOperationException("getCreationTime not implemented in MetaObject");
    }

    public long getModifiedTime() {
        throw new UnsupportedOperationException("getModifiedTime not implemented in MetaObject");
    }

    public long getDeletedTime() {
        throw new UnsupportedOperationException("getDeletedTime not implemented in MetaObject");
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // MISC
    @Override
    public String toString() {
        
        StringBuilder b = new StringBuilder();

        try {
            MetaObject mc = getMetaData();

            b.append("[");
            //String ref = getObjectRef();
            //if ( ref != null )
            //	b.append( ref );
            //else
            //{
            //b.append( "{NEW}" );
            //b.append( '@' );
            b.append(mc.getName());
            //}
            b.append("]");

            boolean first = true;
            b.append('{');
            for (MetaField f : mc.getMetaFields()) {
                if (first) {
                    first = false;
                } else {
                    b.append(',');
                }

                b.append(f.getName());
                b.append(':');
                b.append(f.getString(this));
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
        for (MetaField mf : getMetaFields()) {
            mf.setString(this, null);
        }
    }

    @Override
    public boolean containsKey(Object key) {
        return hasMetaField(String.valueOf(key));
    }

    @Override
    public boolean containsValue(Object value) {
        for (MetaField mf : getMetaFields()) {
            if (value == null && mf.getObject(this) == null) {
                return true;
            }
            if (value != null && value.equals(mf.getObject(this))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a Map.Entry for a specific MetaField and Object
     */
    public class MetaObjectEntry implements Map.Entry<String, Object> {

        private MetaField mf = null;
        private Object o = null;

        public MetaObjectEntry(MetaField mf, Object o) {
            this.o = o;
            this.mf = mf;
        }

        @Override
        public String getKey() {
            return mf.getName();
        }

        @Override
        public Object getValue() {
            return mf.getObject(o);
        }

        @Override
        public Object setValue(Object value) {
            Object old = mf.getObject(o);
            mf.setObject(o, value);
            return old;
        }
    }

    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {

        Set<java.util.Map.Entry<String, Object>> s = new HashSet<java.util.Map.Entry<String, Object>>();
        for (MetaField mf : getMetaFields()) {
            s.add(new MetaObjectEntry(mf, this));
        }
        return s;
    }

    @Override
    public Object get(Object key) {
        MetaField mf = getMetaField(String.valueOf(key));
        if (mf == null) {
            return null;
        }
        return mf.getObject(this);
    }

    @Override
    public boolean isEmpty() {
        return getMetaFields().isEmpty();
    }

    @Override
    public Set<String> keySet() {
        Set<String> s = new HashSet<String>();
        for (MetaField mf : getMetaFields()) {
            s.add(mf.getName());
        }
        return s;
    }

    @Override
    public Object put(String key, Object value) {
        MetaField mf = getMetaField(String.valueOf(key));
        if (mf == null) {
            return null;
        }
        mf.setObject(this, value);
        return mf.getObject(this);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        for (String key : m.keySet()) {
            put(key, m.get(key));
        }
    }

    @Override
    public Object remove(Object key) {
        MetaField mf = getMetaField(String.valueOf(key));
        if (mf == null) {
            return null;
        }
        Object o = mf.getObject(key);
        mf.setObject(this, null);
        return o;
    }

    @Override
    public int size() {
        return getMetaFields().size();
    }

    @Override
    public Collection<Object> values() {
        Collection<Object> s = new ArrayList<Object>();
        for (MetaField mf : getMetaFields()) {
            s.add(mf.getObject(this));
        }
        return s;
    }
}
