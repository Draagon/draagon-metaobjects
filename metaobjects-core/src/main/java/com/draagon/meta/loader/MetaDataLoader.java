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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import java.util.Iterator;

public abstract class MetaDataLoader extends MetaData {

    private final static Log log = LogFactory.getLog(MetaDataLoader.class);
    
    public final static String PKG_SEPARATOR = "::";
    
    private final static List<MetaDataLoader> metaClassLoaders = new CopyOnWriteArrayList<MetaDataLoader>();
    private final Map<String, MetaData> metaDataCache = Collections.synchronizedMap(new WeakHashMap<String, MetaData>());

    public MetaDataLoader() {
        super(null);
        registerLoader(this);
    }

    public void init() {
        log.info("Loading the [" + getClass().getSimpleName() + "] MetaClassLoader");
    }

    /**
     * Gets the primary MetaData class
     */
    public final Class<? extends MetaData> getMetaDataClass() {
        return MetaDataLoader.class;
    }

    /**
     * Retrieves the MetaClassLoader with the specified Name
     */
    public static Collection<MetaDataLoader> getClassLoaders() {
        return metaClassLoaders;
    }

    /**
     * Retrieves the MetaClassLoader with the specified Name
     */
    public static MetaDataLoader getClassLoader(String name) {
        for (MetaDataLoader mcl : getClassLoaders()) {
            if (name.equals(mcl.getName())) {
                return mcl;
            }
        }

        return null;
    }

    /**
     * Retrieves the MetaClassLoader for the specified Object
     */
    public static MetaDataLoader findClassLoader(Object obj) {
        for (MetaDataLoader l : getClassLoaders()) {
            if (l.handles(obj)) {
                return l;
            }
        }

        return null;
    }

    /**
     * Whether the MetaClassLoader handles the object specified
     */
    protected boolean handles(Object obj) {
        if (getMetaObjectFor(obj) != null) {
            return true;
        }
        return false;
    }

    /**
     * Retrieves the MetaClass for the specified Object
     */
    public static MetaObject findMetaObject( Object obj ) throws MetaDataNotFoundException {
        
        // This is a High-Performance addition for MetaObjects
        if (obj instanceof MetaObjectAware) {
            MetaObject mo = ((MetaObjectAware) obj).getMetaData();
            if ( mo != null ) return mo;
        }

        MetaDataLoader l = findClassLoader(obj);
        if (l == null) {
            throw new MetaObjectNotFoundException("No MetaClass exists for object of class [" + obj.getClass().getName() + "]", obj );
        }

        MetaObject mo = l.getMetaObjectFor( obj );
        
        if (obj instanceof MetaObjectAware) {
            ((MetaObjectAware) obj).setMetaData( mo );
        }
        
        return mo;
    }

    /**
     * Retrieves the MetaClass with the specified name
     */
    public static  <T extends MetaData> T findMetaDataByName( Class<T> c, String name ) throws MetaDataNotFoundException {
        
        for (MetaDataLoader l : getClassLoaders()) {
            return l.getMetaDataByName( c, name );
        }

        throw new MetaDataNotFoundException("MetaClass with name [" + name + "] not found", name);
    }

    /**
     * Retrieves a collection of all Meta Classes
     */
    public <T extends MetaData> List<T> getMetaData( Class<T> c ) {
        Collection<T> children = getChildren(c);
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
     * Gets the MetaClass of the specified Object
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
                throw new MetaDataNotFoundException( "MetaData with name [" + metaDataName + "] not found in MetaClassLoader [" + toString() + "]", metaDataName );
            }
        }


        return (T) mc;
    }

    /**
     * Adds the MetaClass
     */
    public void addMetaData(MetaData mc) {
        addChild(mc);
    }

    /**
     * Removes the MetaClass
     */
    public void removeMetaData( Class<MetaData> c, String className) throws MetaDataNotFoundException {
        deleteChild(getMetaDataByName( c, className));
    }

    /**
     * Registers a new MetaClassLoader
     */
    protected static void registerLoader(MetaDataLoader loader) {
        metaClassLoaders.add(loader);
    }

    /**
     * Registers a new MetaClassLoader
     */
    protected static void unregisterLoader(MetaDataLoader mcl) {
        metaClassLoaders.remove(mcl);
    }

    /**
     * Unloads the MetaClassLoader
     */
    public void destroy() {
        log.info("Destroying the [" + getClass().getSimpleName() + "] MetaClassLoader");

        // Remove all classes
        clearChildren();

        // Unregister the class loader
        unregisterLoader(this);
    }

    ////////////////////////////////////////////////////
    // MISC METHODS
    public String toString() {
        if (getParent() == null) {
            return "MetaClassLoader[" + getName() + "]";
        } else {
            return "MetaClassLoader[" + getName() + "@" + getParent().toString() + "]";
        }
    }
}
