/*
 * Copyright (c) 2002-2012 Blue Gnosis, LLC
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.draagon.meta.object;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.attr.MetaAttributeNotFoundException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.field.MetaFieldNotFoundException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.MetaDataRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class MetaObject extends MetaData {

    private static Log log = LogFactory.getLog(MetaObject.class);

    /** Object TYPE */
    public final static String TYPE_OBJECT = "object";

    /** Object class name attribute */
    public final static String ATTR_OBJECT = "object";

    /**
     * Legacy constructor used in unit tests
     * @param name Name of the MetaObject
     * @deprecated Use MetaObject( subtype, name )
     */
    public MetaObject( String name ) {
        this( "deprecated", name );
    }

    /**
     * Constructs the MetaObject
     */
    public MetaObject(String subtype, String name ) {
        super( TYPE_OBJECT, subtype, name );
        //if ( !type.equals(TYPE_OBJECT)) throw new IllegalArgumentException("MetaObjects can only support type=\""+TYPE_OBJECT+"\" ["+type+"]");
        //addAttributeDef(new AttributeDef(ATTR_OBJECT, String.class, true, "The object class to instantiate"));
    }

    /**
     * Gets the primary MetaData class
     */
    public final Class<MetaObject> getMetaDataClass() {
        return MetaObject.class;
    }

    /**
     * Returns the MetaObject for the specified Meta Object name
     *
     * @deprecated Use MetaDataRegistry.findMetaObjectByName(), if enabled in MetaDataLoader
     */
    public static MetaObject forName(String name) {
        return MetaDataRegistry.findMetaDataByName( MetaObject.class, name);
    }

    /**
     * Returns the MetaObject for the specified Object
     *
     * @deprecated Use MetaDataRegistry.findMetaObject(), if registry enabled in MetaDataLoader
     */
    public static MetaObject forObject(Object o) {
        return MetaDataRegistry.findMetaObject( o );
    }

    /**
     * Sets the Super Class
     */
    public void setSuperObject(MetaObject superObject) {
        setSuperData(superObject);
    }

    /**
     * Gets the Super Object
     */
    public MetaObject getSuperObject() {
        return (MetaObject) getSuperData();
    }

    ////////////////////////////////////////////////////
    // FIELD METHODS
    /**
     * Return the MetaField count
     */
    public Collection<MetaField> getMetaFields() {
        return getMetaFields(true);
    }

    /**
     * Return the MetaField count
     */
    public Collection<MetaField> getMetaFields( boolean includeParentData ) {
        return getChildren(MetaField.class, includeParentData);
    }

    /**
     * Add a field to the MetaObject
     */
    public void addMetaField(MetaField f) {
        addChild(f);
    }

    /**
     * Whether the named MetaField exists
     */
    public boolean hasMetaField(String name) {
        try {
            getMetaField(name);
            return true;
        } catch (MetaFieldNotFoundException e) {
            return false;
        }
    }

    /**
     * Return the specified MetaField of the MetaObject
     */
    public MetaField<?> getMetaField(String name) {

        final String KEY = "getMetaField(" + name + ")";

        MetaField<?> f = (MetaField<?>) getCacheValue(KEY);

        if (f == null) {
            try {
                f = (MetaField<?>) getChild(name, MetaField.class);
            } catch (MetaDataNotFoundException e) {
                if (getSuperObject() != null) {
                    try {
                        f = getSuperObject().getMetaField(name);
                    } catch (MetaFieldNotFoundException ex) {
                    }
                }

                throw new MetaFieldNotFoundException("MetaField [" + name + "] does not exist in MetaObject [" + toString() + "]", name);
            }

            setCacheValue(KEY, f);
        }

        return f;
    }

    /**
     * Whether the object is aware of its own state. This is false by default,
     * which means all state methods will throw an
     * UnsupportedOperationException.
     */
    public boolean isStateAware() {
        return false;
    }


    ////////////////////////////////////////////////////
    // OBJECT METHODS
    /**
     * Retrieves the object class of an object, or null if one is not specified
     */
    protected Class<?> getObjectClass() throws ClassNotFoundException {

        Class<?> c;

        if (hasAttr(ATTR_OBJECT)) {

            String ostr = null;
            try {
                ostr = getAttr(ATTR_OBJECT).getValueAsString();
                ostr = ostr.trim();
                if (log.isTraceEnabled())
                    log.trace(String.format("Attr [%s] yields classname [%s]", ATTR_OBJECT, ostr));
            }
            catch (MetaAttributeNotFoundException e) {
                throw new RuntimeException("Attribute was found but could not get it on MetaObject [" + this + "] and attribute [" + ATTR_OBJECT + "]");
            }

            try {
                c = Class.forName(ostr);
            }
            catch (ClassNotFoundException e) {
                if (log.isDebugEnabled())
                    log.debug(String.format("Specified Object Class [%s] not found, trying Loader", ostr));
                c = getLoader().loadClass(ostr);
            }
        }
        else {
            String ostr = getName().replaceAll(MetaDataLoader.PKG_SEPARATOR, ".");

            try {
                c = Class.forName(ostr);
            }
            catch (ClassNotFoundException e) {
                throw new ClassNotFoundException("Derived Object Class [" + ostr + "] was not found");
            }
        }

        return c;
    }

    /**
     * Whether the MetaObject produces the object specified
     */
    public abstract boolean produces(Object obj);

    /**
     * Attaches a MetaObject to an Object
     *
     * @param o Object to attach
     */
    public void attachMetaObject(Object o) {
        if (o instanceof MetaObjectAware) {
            ((MetaObjectAware) o).setMetaData(this);
        }
    }

    /**
     * Sets the default values on an object
     *
     * @param o Object to set the default values on
     */
    public void setDefaultValues(Object o) {

        getMetaFields().stream()
                .filter( f -> f.getDefaultValue()!=null)
                .forEach( f -> f.setObject( o, f.getDefaultValue() ));
    }

    /**
     * Return a new MetaObject instance from the MetaObject
     */
    public Object newInstance()  {

        final String KEY = "ObjectClass";

        // See if we have this cached already
        Class<?> oc = (Class<?>) getCacheValue( KEY );
        if ( oc == null ) {

            try {
                oc = getObjectClass();
                if (oc == null) {
                    throw new MetaDataException("No Object Class was found on MetaObject [" + getName() + "]");
                }
            } catch (ClassNotFoundException e) {
                throw new MetaDataException("Could not find Object Class for MetaObject [" + getName() + "]: " + e.getMessage(), e);
            }

            // Store the resulting Class in the cache
            setCacheValue( KEY, oc );
        }

        try {
            if (oc.isInterface()) {
                throw new IllegalArgumentException("Could not instantiate an Interface for MetaObject [" + getName() + "]");
            }

            Object o = null;

            try {
                // Construct the object and pass the MetaObject into the constructor
                Constructor c = oc.getConstructor( MetaObject.class );
                o = c.newInstance(this);
            }
            catch (NoSuchMethodException e) {
                // Construct with no arguments && attach the metaobject
                for( Constructor<?> c : oc.getDeclaredConstructors() ) {
                    if ( c.getParameterCount() == 0 ) {
                        try {
                            o = (oc.getDeclaredConstructors()[0]).newInstance();
                        } catch (InvocationTargetException ex) {
                            throw new RuntimeException("Could not instantiate a new Object of Class [" + oc + "] for MetaObject [" + getName() + "]: " + e.getMessage(), e);
                        }
                        attachMetaObject(o);
                        break;
                    }
                }
                //o = (oc.getDeclaredConstructors()[0]).newInstance();
                //attachMetaObject(o);
                if ( o == null ) throw new RuntimeException("Could not instantiate a new Object of Class [" + oc + "] for MetaObject [" + getName() + "]: No empty constructor existed" );
            }
            catch (InvocationTargetException e) {
                throw new RuntimeException("Could not instantiate a new Object of Class [" + oc + "] for MetaObject [" + getName() + "]: " + e.getMessage(), e);
            }

            // Set the Default Values
            setDefaultValues(o);

            return o;
        } catch (InstantiationException e) {
            throw new RuntimeException("Could not instantiate a new Object of Class [" + oc + "] for MetaObject [" + getName() + "]: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal Access Exception instantiating a new Object for MetaObject [" + getName() + "]: " + e.getMessage(), e);
        }
    }

    ////////////////////////////////////////////////////
    // ABSTRACT METHODS

    /**
     * Retrieves the value from the object
     */
    public abstract Object getValue(MetaField f, Object obj);

    /**
     * Sets the value on the object
     */
    public abstract void setValue(MetaField f, Object obj, Object val);

    ////////////////////////////////////////////////////
    // MISC METHODS

    public Object clone() {
        MetaObject mc = (MetaObject) super.clone();
        return mc;
    }
}
