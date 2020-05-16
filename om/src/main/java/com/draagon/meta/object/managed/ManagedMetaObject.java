/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.object.managed;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.field.*;
import com.draagon.meta.manager.ManagerAwareMetaObject;
import com.draagon.meta.manager.ObjectManager;
import com.draagon.meta.manager.StateAwareMetaObject;
import com.draagon.meta.object.pojo.PojoMetaObject;
import com.draagon.meta.util.Converter;
import java.lang.reflect.Method;

@SuppressWarnings("serial")
public class ManagedMetaObject extends PojoMetaObject implements StateAwareMetaObject, ManagerAwareMetaObject 
{
    public final static String CACHE_PARAM_HAS_GETTER_METHOD = "hasGetterMethod";
    public final static String CACHE_PARAM_HAS_SETTER_METHOD = "hasSetterMethod";

    /**
     * Constructs the MetaClassObject for MetaObjects
     */
    public ManagedMetaObject(String name) {
        super(name);
    }

    /*public static MetaObject createFromTemplate(String name, String template) {
        // Let's create one from scratch
        ManagedMetaObject mc = new ManagedMetaObject(name);
        //mc.setName( name );

        if (template.length() == 0) {
            template = null;
        }

        while (template != null) {
            String param = null;

            int i = template.indexOf(',');
            if (i >= 0) {
                param = template.substring(0, i).trim();
                template = template.substring(i + 1).trim();
                if (template.length() == 0) {
                    template = null;
                }
            } else {
                param = template.trim();
                template = null;
            }

            i = param.indexOf(':');
            if (i <= 0) {
                throw new IllegalArgumentException("Malformed template field parameter [" + param + "]");
            }

            String field = param.substring(0, i).trim();
            String type = param.substring(i + 1).trim();

            if (field.length() == 0) {
                throw new IllegalArgumentException("Malformed template field name parameter [" + param + "]");
            }

            if (type.length() == 0) {
                throw new IllegalArgumentException("Malformed template field type parameter [" + param + "]");
            }

            MetaField mf = null;
            if (type.equals("int")) {
                mf = new IntegerField(field);
            } else if (type.equals("long")) {
                mf = new LongField(field);
            } else if (type.equals("short")) {
                mf = new ShortField(field);
            } else if (type.equals("byte")) {
                mf = new ByteField(field);
            } else if (type.equals("boolean")) {
                mf = new BooleanField(field);
            } else if (type.equals("float")) {
                mf = new FloatField(field);
            } else if (type.equals("double")) {
                mf = new DoubleField(field);
            } else if (type.equals("date")) {
                mf = new DateField(field);
            } else {
                mf = new StringField(field);
            }

            //mf.setName(field);

            mc.addMetaField(mf);
        }

        return mc;
    }*/

    /**
     * Retrieves the object class of an object
     */
    protected Class<?> getObjectClass() throws ClassNotFoundException {
        try { 
            return super.getObjectClass();
        } catch( MetaDataException e ) {
            return ManagedObject.class;
        }
    }

    /**
     * Whether the MetaClass handles the object specified
     */
    public boolean produces(Object obj) {
        
        if (obj == null) {
            return false;
        }

        if (obj instanceof ManagedObject) {
            
            ManagedObject mo = (ManagedObject) obj;

            if (mo.getMetaObjectName() == null) {
                //log.warn("MetaObject with no MetaClassName: [" + obj.getClass() + "]");
                
                // See if we can match by the object produced
                return super.produces(obj);
            }

            // TODO: WARNING:  This doesn't match up class loaders!
            if (mo.getMetaObjectName().equals(getName())) {
                return true;
            }
        }

        return false;
    }

    ////////////////////////////////////////////////////
    // PERSISTENCE METHODS
    
    /**
     * Attaches a Object Manager to the object
     */
    @Override
    public void attachManager( ObjectManager mm, Object obj )
     {
         //try { 
             getManagedObject( obj ).attachObjectManager( mm ); 
         //} catch( MetaException e ) { System.err.println( "Failed to attach: " + e.getMessage() ); }
     }
    
    /**
     * Gets the Object Manager for the object
     */
    @Override
    public ObjectManager getManager( Object obj )
    {
        return getManagedObject( obj ).getObjectManager();
    }
    
    /**
     * Get the ManagedObject instance
     */
    private ManagedObject getManagedObject(Object o) {
        
        if (o == null) {
            throw new MetaDataException("Null value found, MetaObject expected");
        }

        if (!(o instanceof ManagedObject)) {
            throw new MetaDataException("MetaObject expected, not [" + o.getClass().getName() + "]");
        }

        return (ManagedObject) o;
    }

    ////////////////////////////////////////////////////
    // PERSISTENCE METHODS

    private ManagedObject.Value getAttributeValue(MetaField f, Object obj)
            throws MetaDataException {
        if (!(obj instanceof ManagedObject)) {
            throw new MetaDataException("MetaObject expected, not [" + obj.getClass().getName() + "]");
        }

        return ((ManagedObject) obj).getObjectAttributeValue(f.getName());
    }

    protected boolean hasGetterMethod(MetaField f, Class<?> objClass) {

        // Try the cache value first
        Boolean b = (Boolean) f.getCacheValue(CACHE_PARAM_HAS_GETTER_METHOD + "." + objClass.getName());
        if (b != null) {
            return b.booleanValue();
        }

        // Now try to actually get the method
        Method m = null;
        try {
            m = retrieveGetterMethod(f, objClass);
        } catch (NoSuchMethodError e) {
        }

        // Return whether the setter existed
        if (m != null) {
            f.setCacheValue(CACHE_PARAM_HAS_GETTER_METHOD + "." + objClass.getName(), Boolean.TRUE);
            return true;
        } else {
            f.setCacheValue(CACHE_PARAM_HAS_GETTER_METHOD + "." + objClass.getName(), Boolean.FALSE);
            return false;
        }
    }

    protected boolean hasSetterMethod(MetaField f, Class<?> objClass) {

        // Try the cache value first
        Boolean b = (Boolean) f.getCacheValue(CACHE_PARAM_HAS_SETTER_METHOD + "." + objClass.getName());
        if (b != null) {
            return b.booleanValue();
        }

        // Now try to actually get the method
        Method m = null;
        try {
            m = retrieveSetterMethod(f, objClass);
        } catch (NoSuchMethodError e) {
        }

        // Return whether the setter existed
        if (m != null) {
            f.setCacheValue(CACHE_PARAM_HAS_SETTER_METHOD + "." + objClass.getName(), Boolean.TRUE);
            return true;
        } else {
            f.setCacheValue(CACHE_PARAM_HAS_SETTER_METHOD + "." + objClass.getName(), Boolean.FALSE);
            return false;
        }
    }

    /**
     * Gets the object attribute represented by this MetaField
     */
    public Object getValue(MetaField f, Object obj) //throws MetaException
    {
        if (!(obj instanceof ManagedObject)) {
            throw new IllegalArgumentException("MetaObject expected, Invalid object of class [" + obj.getClass().getName() + "]");
        }

        if (hasGetterMethod(f, obj.getClass())) {
            return super.getValue(f, obj);
        } else {
            return ((ManagedObject) obj).getObjectAttribute(f.getName());
        }
    }

    /**
     * Sets the object attribute represented by this MetaField
     */
    public void setValue(MetaField f, Object obj, Object value) //throws MetaException
    {
        if (!(obj instanceof ManagedObject)) {
            throw new IllegalArgumentException("MetaObject expected, Invalid object of class [" + obj.getClass().getName() + "]");
        }

        // Convert the value to the appropriate type
        if (value != null && f.getValueClass() != value.getClass()) {
            value = Converter.toType(f.getType(), value);
        }

        if (hasSetterMethod(f, obj.getClass())) {
            super.setValue(f, obj, value);
        } else {
            ((ManagedObject) obj).setObjectAttribute(f.getName(), value);
        }
    }

    //////////////////////////////////////////////////////////////
    // Stateful methods
    public boolean isNew(Object obj)
            throws MetaDataException {
        return getManagedObject(obj).isNew();
    }

    public boolean isModified(Object obj)
            throws MetaDataException {
        return getManagedObject(obj).isModified();
    }

    public boolean isDeleted(Object obj)
            throws MetaDataException {
        return getManagedObject(obj).isDeleted();
    }

    public void setNew(Object obj, boolean state)
            throws MetaDataException {
        getManagedObject(obj).setNew(state);
    }

    public void setModified(Object obj, boolean state)
            throws MetaDataException {
        getManagedObject(obj).setModified(state);
    }

    public void setDeleted(Object obj, boolean state)
            throws MetaDataException {
        getManagedObject(obj).setDeleted(state);
    }

    public long getCreationTime(Object obj)
            throws MetaDataException {
        return getManagedObject(obj).getCreationTime();
    }

    public long getModifiedTime(Object obj)
            throws MetaDataException {
        return getManagedObject(obj).getModifiedTime();
    }

    public long getDeletedTime(Object obj)
            throws MetaDataException {
        return getManagedObject(obj).getDeletedTime();
    }

    /**
     * Returns whether the field on the object was modified
     */
    public boolean isFieldModified(MetaField f, Object obj)
            throws MetaDataException {
        return getAttributeValue(f, obj).isModified();
    }

    /**
     * Sets whether the field is modified
     */
    public void setFieldModified(MetaField f, Object obj, boolean state)
            throws MetaDataException {
        getAttributeValue(f, obj).setModified(state);
    }

    /**
     * Gets the time the field was modified
     */
    public long getFieldModifiedTime(MetaField f, Object obj)
            throws MetaDataException {
        return getAttributeValue(f, obj).getModifiedTime();
    }
}
