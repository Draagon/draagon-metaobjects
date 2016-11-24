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
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.MetaObjectNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

//import com.draagon.meta.object.MetaObjectNotFoundException;
//import java.util.Iterator;

/**
 * Abstract MetaDataLoader with common functions for all MetaDataLoaders
 */
public abstract class MetaDataLoader extends MetaData {

    private final static Log log = LogFactory.getLog(MetaDataLoader.class);
    
    public final static String PKG_SEPARATOR = "::";

    private final static Map<String,MetaDataLoader> metaDataLoaders = Collections.synchronizedMap(new WeakHashMap<String,MetaDataLoader>());
    private final Map<String, MetaData> metaDataCache = Collections.synchronizedMap(new WeakHashMap<String, MetaData>());

    public MetaDataLoader( String name ) {
        super(name);
        registerLoader(this);
    }

    public void init() {
        log.info("Loading the [" + getClass().getSimpleName() + "] MetaDataLoader with name [" + getName() + "]" );
    }

    /**
     * Gets the primary MetaData class
     */
    public final Class<? extends MetaData> getMetaDataClass() {
        return MetaDataLoader.class;
    }

    /**
     * Retrieves the MetaDataLoader with the specified Name
     */
    public static Collection<MetaDataLoader> getDataLoaders() {
        return metaDataLoaders.values();
    }

    /**
     * Retrieves the MetaDataLoader with the specified Name
     */
    public static MetaDataLoader getDataLoader(String loaderName) {

        MetaDataLoader l = metaDataLoaders.get(loaderName);
        if (l == null) {
            throw new MetaDataLoaderNotFoundException("No MetaDataLoader exists with name [" + loaderName + "]" );
        }

        return l;
    }

    /**
     * Retrieves the MetaDataLoader for the specified Object
     */
    public static MetaDataLoader findLoader(Object obj) {
        for (MetaDataLoader l : getDataLoaders()) {
            if (l.handles(obj)) {
                return l;
            }
        }

        return null;
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
     * Retrieves the MetaObject for the specified Object
     */
    public static MetaObject findMetaObject( Object obj ) throws MetaDataNotFoundException {
        
        // This is a High-Performance addition for MetaObjects
        if (obj instanceof MetaObjectAware) {
            MetaObject mo = ((MetaObjectAware) obj).getMetaData();
            if ( mo != null ) return mo;
        }

        MetaDataLoader l = findLoader(obj);
        if (l == null) {
            throw new MetaObjectNotFoundException("No MetaDataLoader exists for object of class [" + obj.getClass().getName() + "]", obj );
        }

        MetaObject mo = l.getMetaObjectFor( obj );
        
        if (obj instanceof MetaObjectAware) {
            ((MetaObjectAware) obj).setMetaData( mo );
        }
        
        return mo;
    }

    /**
     * Retrieves the MetaObject with the specified name
     * IMPORTANT:  This traverses ALL classloaders, use getMetaDataByName if you know the metadataloader to use
     */
    public static  <T extends MetaData> T findMetaDataByName( Class<T> c, String name ) throws MetaDataNotFoundException {
        
        for (MetaDataLoader l : getDataLoaders()) {
            T d = l.getMetaDataByName( c, name );
            if ( d != null ) return d;
        }

        throw new MetaDataNotFoundException("MetaData of type ["+c.getName()+"] and name [" + name + "] not found", name);
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
        
        MetaData mc = metaDataCache.get(metaDataName);
        if (mc == null) {
            synchronized (metaDataCache) {

                mc = metaDataCache.get(metaDataName);
                if (mc == null) {
                    for (MetaData mc2 : getMetaData( c )) {
                        if (mc2.getName().equals(metaDataName)) {
                            mc = mc2;
                            break;
                        }
                    }

                    if (mc != null) {
                        metaDataCache.put(metaDataName, mc);
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
     * Registers a new MetaDataLoader
     */
    protected static void registerLoader(MetaDataLoader loader) {
        if ( metaDataLoaders.containsKey( loader.getName() )) {
            throw new IllegalStateException( "A MetaDataLoader with name [" + loader.getName() + "] is already loaded" );
        }
        metaDataLoaders.put( loader.getName(), loader);
    }

    /**
     * Registers a new MetaDataLoader
     */
    protected static void unregisterLoader(MetaDataLoader mcl) {
        metaDataLoaders.remove(mcl.getName());
    }

    /**
     * Unloads the MetaDataLoader
     */
    public void destroy() {

        log.info("Destroying the [" + getName() + "] MetaDataLoader");

        // Remove all classes
        clearChildren();

        // Unregister the class loader
        unregisterLoader(this);
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
