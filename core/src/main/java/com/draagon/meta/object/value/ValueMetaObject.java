/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.object.value;

import com.draagon.meta.MetaException;
import com.draagon.meta.field.*;
import com.draagon.meta.object.pojo.PojoMetaObject;
import com.draagon.meta.util.Converter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;

@SuppressWarnings("serial")
public class ValueMetaObject extends PojoMetaObject //implements StatefulMetaObject 
{
    private final static Log log = LogFactory.getLog(ValueMetaObject.class);

    public final static String CACHE_PARAM_HAS_GETTER_METHOD = "hasGetterMethod";
    public final static String CACHE_PARAM_HAS_SETTER_METHOD = "hasSetterMethod";

    /**
     * Constructs the MetaClassObject for MetaObjects
     */
    public ValueMetaObject(String type, String subtype, String name) {
        super(type,subtype, name);
    }

    /*public static MetaObject createFromTemplate(String name, String template) {
        // Let's create one from scratch
        ValueMetaObject mc = new ValueMetaObject(name);
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
        } catch( ClassNotFoundException e ) {
            if (hasAttribute(ATTR_OBJECT)) {
                throw e;
            }
            //log.warn( "Could not find Java class for MetaObject [" + getName() + "], returning generic ValueObject instead" );
            return ValueObject.class;
        }
    }

    /**
     * Whether the MetaClass handles the object specified
     */
    public boolean produces(Object obj) {
        
        if (obj == null) {
            return false;
        }

        if (obj instanceof ValueObject) {
            
            ValueObject mo = (ValueObject) obj;

            if (mo.getObjectName() == null) {
                //log.warn("MetaObject with no MetaClassName: [" + obj.getClass() + "]");
                
                // See if we can match by the object produced
                return super.produces(obj);
            }

            // TODO: WARNING:  This doesn't match up class loaders!
            if (mo.getObjectName().equals(getName())) {
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
    /*public void attachManager( ObjectManager mm, Object obj )
     {
     try { getMetaObject( obj ).attachObjectManager( mm ); }
     catch( MetaException e ) { log.error( e.getMessage(), e ); }
     }*/
    /**
     * Gets the Object Manager for the object
     */
    /*public ObjectManager getManager( Object obj )
     {
     try { return getMetaObject( obj ).getObjectManager(); }
     catch( MetaException e ) { log.error( e.getMessage(), e ); }
     return null;
     }*/
    private ValueObject getMetaObject(Object o)
            throws MetaException {
        if (o == null) {
            throw new MetaException("Null value found, MetaObject expected");
        }

        if (!(o instanceof ValueObject)) {
            throw new MetaException("MetaObject expected, not [" + o.getClass().getName() + "]");
        }

        return (ValueObject) o;
    }

    /**
     * Retrieve the id of the object
     */
    //public String getId( Object obj )
    //  throws MetaException
    //{
    //  return getMetaObject( obj ).getObjectId();
    //}
    /**
     * Retrieve the id of the object
     */
    //public void setId( Object obj, String id )
    //  throws MetaException
    //{
    //  getMetaObject( obj ).setObjectId( id );
    //}
    ////////////////////////////////////////////////////
    // PERSISTENCE METHODS
    private ValueObject.Value getAttributeValue(MetaField f, Object obj)
            throws MetaException {
        if (!(obj instanceof ValueObject)) {
            throw new MetaException("MetaObject expected, not [" + obj.getClass().getName() + "]");
        }

        return ((ValueObject) obj).getObjectAttributeValue(f.getName());
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
        if (!(obj instanceof ValueObject)) {
            throw new IllegalArgumentException("MetaObject expected, Invalid object of class [" + obj.getClass().getName() + "]");
        }

        if (hasGetterMethod(f, obj.getClass())) {
            return super.getValue(f, obj);
        } else {
            return ((ValueObject) obj).getObjectAttribute(f.getName());
        }
    }

    /**
     * Sets the object attribute represented by this MetaField
     */
    public void setValue(MetaField f, Object obj, Object value) //throws MetaException
    {
        if (!(obj instanceof ValueObject)) {
            throw new IllegalArgumentException("MetaObject expected, Invalid object of class [" + obj.getClass().getName() + "]");
        }

        // Convert the value to the appropriate type
        if (value != null && f.getValueClass() != value.getClass()) {
            value = Converter.toType(f.getType(), value);
        }

        if (hasSetterMethod(f, obj.getClass())) {
            super.setValue(f, obj, value);
        } else {
            ((ValueObject) obj).setObjectAttribute(f.getName(), value, f.getType());
        }
    }
}
