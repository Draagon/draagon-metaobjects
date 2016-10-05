/*
 * Copyright (c) 2002-2012 Blue Gnosis, LLC
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.draagon.meta.object;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.attr.AttributeDef;
import com.draagon.meta.attr.MetaAttributeNotFoundException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.field.MetaFieldNotFoundException;
import com.draagon.meta.loader.MetaDataLoader;
import java.util.ArrayList;
import java.util.Collection;

public abstract class MetaObject extends MetaData {

    //private static Log log = LogFactory.getLog(MetaObject.class);
    /**
     * Object class name attribute
     */
    public final static String ATTR_OBJECT = "object";

    /**
     * Constructs the MetaObject
     */
    public MetaObject(String name) {
        super(name);
        addAttributeDef(new AttributeDef(ATTR_OBJECT, String.class, true, "The object class to instantiate"));
    }

    /**
     * Gets the primary MetaData class
     */
    public final Class<MetaObject> getMetaDataClass() {
        return MetaObject.class;
    }

    /**
     * Returns the MetaObject for the specified Meta Class name
     */
    public static MetaObject forName(String name) //throws MetaObjectNotFoundException
    {
        return MetaDataLoader.findMetaDataByName( MetaObject.class, name);
    }

    /**
     * Returns the MetaObject for the specified Object
     */
    public static MetaObject forObject(Object o) //throws MetaObjectNotFoundException
    {
        return MetaDataLoader.findMetaObject( o );
    }
    /**
     * Sets the Package for the MetaObject
     */
    /*public void setPackage( String packageName )
     //throws MetaException
     {
     mPackage = packageName;
     }*/

    /**
     * Returns the MetaObjectLoader
     */
    public MetaDataLoader getClassLoader() {
        return (MetaDataLoader) getParent();
    }

    /**
     * Sets the Super Class
     */
    public void setSuperClass(MetaObject superClass) {
        setSuperData(superClass);

        // for each field, create a new one and attach the super field
    }

    /**
     * Gets the Super Class
     */
    public MetaObject getSuperClass() {
        return (MetaObject) getSuperData();
    }

    /**
     * Retrieve the name of the MetaObject
     */
    /*public String getFullName()
     {
     if ( getPackage() != null && getPackage().length() > 0 )
     {
     return getPackage() + SEPARATOR + getName();
     }
     else
     return getName();
     }*/
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
    public void addMetaField(MetaField f) //throws InvalidMetaDataException
    {
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
    public MetaField getMetaField(String name) //throws MetaFieldNotFoundException
    {
        final String KEY = "getMetaField(" + name + ")";

        MetaField f = (MetaField) getCacheValue(KEY);

        if (f == null) {
            try {
                f = (MetaField) getChild(name, MetaField.class);
            } catch (MetaDataNotFoundException e) {
                if (getSuperClass() != null) {
                    try {
                        f = getSuperClass().getMetaField(name);
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

        if (hasAttribute(ATTR_OBJECT)) {
            String ostr = null;
            try {
                ostr = (String) getAttribute(ATTR_OBJECT);
                ostr = ostr.trim();
            } catch (MetaAttributeNotFoundException e) {
                throw new RuntimeException("Attribute was found but could not get it on MetaObject [" + this + "] and attribute [" + ATTR_OBJECT + "]");
            }

            try {
                return Class.forName(ostr);
            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundException("Specified Object Class [" + ostr + "] was not found", e);
            }
        } 
        else {
            String ostr = getName().replaceAll(MetaDataLoader.PKG_SEPARATOR, ".");

            try {
                return Class.forName(ostr);
            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundException("Derived Object Class [" + ostr + "] was not found");
            }
        }

        //return null;
    }

    /**
     * Whether the MetaObject produces the object specified
     */
    public abstract boolean produces(Object obj);/* {
        try {
            Class<?> cl = getObjectClass();
            if (cl == null) {
                return false;
            }

            boolean rc = obj.getClass().equals( cl );
            if (rc) {
                return true;
            }
        } catch (ClassNotFoundException e) {
        };

        return false;
    }*/

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
    public void setDefaultValues(Object o) //throws MetaException
    {
        // Set the default values
        for (MetaField f : getMetaFields()) {
            Object val = f.getDefaultValue();
            if (val != null) {
                f.setObject(o, val);
            }
        }
    }

    /**
     * Return a new MetaObject instance from the MetaObject
     */
    public Object newInstance() //throws MetaException
    {
        Class<?> oc = null;

        try {
            oc = getObjectClass();
            if (oc == null) {
                throw new MetaDataException("Not Object Class was found");
            }
        } catch (ClassNotFoundException e) {
            throw new MetaDataException("Could find Object Class for MetaObject [" + toString() + "]", e);
        }

        try {
            if (oc.isInterface()) {
                throw new IllegalArgumentException("Can not instantiate an Interface for MetaObject [" + toString() + "]");
            }

            Object o = oc.newInstance();

            // Attach the MetaObject
            attachMetaObject(o);

            // Set the Default Values
            setDefaultValues(o);

            return o;
        } catch (InstantiationException e) {
            throw new RuntimeException("Could not instantiate a new Object of Class [" + oc + "] for MetaObject [" + toString() + "]", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal Access Exception instantiating a new Object for MetaObject [" + toString() + "]", e);
        }
    }

    ////////////////////////////////////////////////////
    // ABSTRACT METHODS
    /**
     * Retrieves the value from the object
     */
    public abstract Object getValue(MetaField f, Object obj); //throws MetaException;

    /**
     * Sets the value on the object
     */
    public abstract void setValue(MetaField f, Object obj, Object val); //throws MetaException;

    ////////////////////////////////////////////////////
    // MISC METHODS
    public Object clone() {
        MetaObject mc = (MetaObject) super.clone();
        //mc.mPackage = mPackage;
        return mc;
    }

    /* public String getDirtyFieldName() {
     return dirtyFieldName;
     }

     public void setDirtyFieldName(String dirtyFieldName) {
     this.dirtyFieldName = dirtyFieldName;
     }*/
}
