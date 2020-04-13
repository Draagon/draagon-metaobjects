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
import com.draagon.meta.util.DataConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;

public class ValueMetaObject extends PojoMetaObject
{
    private final static Log log = LogFactory.getLog(ValueMetaObject.class);

    public final static String SUBTYPE_VALUE = "value";

    public final static String CACHE_PARAM_HAS_GETTER_METHOD = "hasGetterMethod";
    public final static String CACHE_PARAM_HAS_SETTER_METHOD = "hasSetterMethod";

    /**
     * Constructs the MetaClassObject for MetaObjects
     */
    public ValueMetaObject( String name ) {
        super( SUBTYPE_VALUE, name);
    }

    /**
     * Manually create a ValueMetaObject with the specified name
     * @param name Name for the ValueMetaObject
     * @return Created ValueObject
     */
    public static ValueMetaObject create( String name ) {
        return new ValueMetaObject( name );
    }

    /**
     * Retrieves the object class of an object
     */
    protected Class<?> getObjectClass() throws ClassNotFoundException {
        try { 
            return super.getObjectClass();
        } catch( ClassNotFoundException e ) {
            if (hasAttr(ATTR_OBJECT)) {
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

    private ValueObject getMetaObject(Object o) {

        if (o == null) {
            throw new MetaException("ValueObject expected, but was bnull");
        }

        if (!(o instanceof ValueObject)) {
            throw new MetaException("ValueObject expected, not [" + o.getClass().getName() + "]");
        }

        return (ValueObject) o;
    }


    ////////////////////////////////////////////////////
    // PERSISTENCE METHODS

    private ValueObject.Value getAttributeValue(MetaField f, Object obj)  {

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
            throw new IllegalArgumentException("ValueObject expected, Invalid object of class [" + obj.getClass().getName() + "]");
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
            throw new IllegalArgumentException("ValueObject expected, Invalid object of class [" + obj.getClass().getName() + "]");
        }

        // Convert the value to the appropriate type
        if (value != null && f.getValueClass() != value.getClass()) {
            value = DataConverter.toType(f.getDataType(), value);
        }

        if (hasSetterMethod(f, obj.getClass())) {
            super.setValue(f, obj, value);
        } else {
            ((ValueObject) obj).setObjectAttribute(f.getName(), value, f.getDataType());
        }
    }
}
