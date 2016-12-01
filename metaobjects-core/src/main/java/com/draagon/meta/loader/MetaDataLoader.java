/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.loader;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.object.MetaObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//import com.draagon.meta.object.MetaObjectNotFoundException;
//import java.util.Iterator;

/**
 * Abstract MetaDataLoader with common functions for all MetaDataLoaders
 */
public abstract class MetaDataLoader extends MetaData {

    private final static Log log = LogFactory.getLog(MetaDataLoader.class);
    
    public final static String PKG_SEPARATOR = "::";

    private boolean isRegistered = false;

    //private final Map<String, MetaData> metaDataCache = Collections.synchronizedMap(new WeakHashMap<String, MetaData>());

    public MetaDataLoader() {
        super( "loader-" + System.currentTimeMillis());
        //registerLoader(this);
    }

    public MetaDataLoader( String name ) {
        super(name);
        //registerLoader(this);
    }

    public MetaDataLoader init() {
        log.info("Loading the [" + getClass().getSimpleName() + "] MetaDataLoader with name [" + getName() + "]" );
        return this;
    }

    public void register() {
        MetaDataRegistry.registerLoader( this );
        isRegistered = true;
    }

    /**
     * Gets the primary MetaData class
     */
    public final Class<? extends MetaData> getMetaDataClass() {
        return MetaDataLoader.class;
    }

    /**
     * Whether the MetaDataLoader handles the object specified
     */
    protected boolean handles(Object obj) {
        if (getMetaObjectFor(obj) != null) {
            return true;
        }
        return false;
    }

    /**
     * Retrieves a collection of all Meta Classes
     */
    public <T extends MetaData> List<T> getMetaData( Class<T> c ) {
        Collection<T> children = getChildren(c,true);
        ArrayList<T> classes = new ArrayList<T>(children.size());
        for (MetaData md : children) {
            classes.add((T) md);
        }
        return classes;
    }

    /**
     * Retrieves a collection of all Meta Classes
     */
    public List<MetaObject> getMetaObjects() {
        return getMetaData( MetaObject.class );
    }
    
    /**
     * Gets the MetaObject of the specified Object
     */
    public MetaObject getMetaObjectFor(Object obj) {
        for (MetaObject mc : getMetaData( MetaObject.class )) {
            if (mc.produces(obj)) {
                return mc;
            }
        }

        return null;
    }

    /**
     * Gets the MetaData with the specified Class type and name
     */
    public <T extends MetaData> T getMetaDataByName( Class<T> c, String metaDataName) throws MetaDataNotFoundException {

        String KEY = "QuickCache-"+metaDataName;

        MetaData mc = (MetaData) getCacheValue(KEY);
        if (mc == null) {
            synchronized( this ) {

                mc = (MetaData) getCacheValue(KEY);
                if (mc == null) {
                    for (MetaData mc2 : getMetaData( c )) {
                        if (mc2.getName().equals(metaDataName)) {
                            mc = mc2;
                            break;
                        }
                    }

                    if (mc != null) {
                        setCacheValue(KEY, mc);
                    }
                }
            }

            if (mc == null) {
                throw new MetaDataNotFoundException( "MetaData with name [" + metaDataName + "] not found in MetaDataLoader [" + toString() + "]", metaDataName );
            }
        }


        return (T) mc;
    }

    /**
     * Lookup the specified class by name
     * @param className
     * @return
     */
    public Class<?> loadClass(String className ) throws ClassNotFoundException {

        try {
            return getClass().getClassLoader().loadClass( className );
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Specified Java Class [" + className + "] was not found: " + e.getMessage(), e);
        }
    }

    /**
     * Adds the MetaData
     */
    public void addMetaData(MetaData mc) {
        addChild(mc);
    }

    /**
     * Removes the MetaData
     */
    public void removeMetaData( Class<MetaData> c, String name) throws MetaDataNotFoundException {
        deleteChild(getMetaDataByName( c, name));
    }

    /**
     * Unloads the MetaDataLoader
     */
    public void destroy() {

        log.info("Destroying the [" + getName() + "] MetaDataLoader");

        // Remove all classes
        clearChildren();

        // Unregister the class loader
        if ( isRegistered ) {
            MetaDataRegistry.unregisterLoader(this);
        }
    }

    ////////////////////////////////////////////////////
    // MISC METHODS
    public String toString() {
        if (getParent() == null) {
            return "MetaDataLoader[" + getName() + "]";
        } else {
            return "MetaDataLoader[" + getName() + "@" + getParent().toString() + "]";
        }
    }
}
