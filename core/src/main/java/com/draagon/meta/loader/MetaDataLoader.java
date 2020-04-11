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

/**
 * Abstract MetaDataLoader with common functions for all MetaDataLoaders
 */
public abstract class MetaDataLoader extends MetaData {

    private final static Log log = LogFactory.getLog(MetaDataLoader.class);

    public final static String TYPE_LOADER = "loader";

    private boolean isRegistered = false;
    private boolean isInitialized = false;
    private boolean isDestroyed = false;

    public MetaDataLoader( String subtype ) {
        this( subtype, TYPE_LOADER + "-" + System.currentTimeMillis());
    }

    public MetaDataLoader( String subtype, String name ) {
        super( TYPE_LOADER, subtype, name );
    }

    protected void checkState() {
        if ( !isInitialized ) throw new IllegalStateException( "MetaDataLoader [" + getName() + "] was not initialized" );
        if ( isDestroyed ) throw new IllegalStateException( "MetaDataLoader [" + getName() + "] is destroyed" );
    }

    public MetaDataLoader init() {
        if ( isInitialized ) throw new IllegalStateException( "MetaDataLoader [" + getName() + "] was already initialized" );
        log.info("Loading the [" + getClass().getSimpleName() + "] MetaDataLoader with name [" + getName() + "]" );
        isInitialized = true;
        return this;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Register this MetaDataLoader with the MetaDataRegistry
     */
    public void register() {
        MetaDataRegistry.registerLoader( this );
        isRegistered = true;
    }

    /**
     * Returns whether the MetaDataLoader in the MetaDataRegistry
     */
    public boolean isRegistered() {
        return isRegistered;
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
        checkState();
        if (getMetaObjectFor(obj) != null) {
            return true;
        }
        return false;
    }

    /**
     * Retrieves a collection of all Meta Classes
     */
    public <T extends MetaData> List<T> getMetaData( Class<T> c ) {
        return getMetaData(c, true);
    }

    /**
     * Retrieves a collection of all Meta Classes
     */
    public <T extends MetaData> List<T> getMetaData( Class<T> c, boolean includeParentData ) {
        checkState();
        Collection<T> children = getChildren(c,includeParentData);
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
        checkState();
        return getMetaData( MetaObject.class );
    }
    
    /**
     * Gets the MetaObject of the specified Object
     */
    public MetaObject getMetaObjectFor(Object obj) {
        checkState();
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

        checkState();

        String KEY = "QuickCache-"+c.getName()+"-"+metaDataName;

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
     * Gets the MetaData with the specified name in parent hierarchy.
     * <p>
     * Only uses direct 'super' relationship, not 'inherits'
     */
    protected <T extends MetaData> List<T> getMetaDataBySuper(String metaDataName, List<MetaObject> objects) throws MetaDataNotFoundException {

        checkState();

        String KEY = "QuickCacheDerived-" + metaDataName;
        List<T> result = (List<T>) getCacheValue(KEY);
        if (result == null) {
            synchronized (this) {
                result = (List<T>) getCacheValue(KEY);
                if (result == null) {
                    result = new ArrayList<>();

                    for (MetaObject mo : objects) {
                        if (null != mo.getSuperObject()) {
                            if (mo.getSuperObject().getName().equals(metaDataName)) {
                                result.add((T) mo);
                                result.addAll((Collection<T>) getMetaDataBySuper(mo.getName(), objects));
                            }
                        }
                    }
                    setCacheValue(KEY, result);  // Build the sub-trees as we go
                }
            }
        }
        return result;
    }

    /**
     * Gets the MetaData with the specified name in parent hierarchy.
     * <p>
     * Only uses direct 'super' relationship, not 'inherits'
     */
    public <T extends MetaData> List<T> getMetaDataBySuper(String metaDataName) throws MetaDataNotFoundException {

        checkState();

        String KEY = "QuickCacheDerived-" + metaDataName;
        List<T> result;
        result = (List<T>) getCacheValue(KEY);
        if (result == null) {
            synchronized (this) {
                result = (List<T>) getCacheValue(KEY);
                if (result == null) {
                    List<MetaObject> objects = getMetaObjects();
                    // Delegate to a second level, so we don't have to keep retrieving the list of all MetaObjects
                    result = getMetaDataBySuper(metaDataName, objects);
                    // rely on delegate function to set the cache
                }
            }

        }

        return result;
    }

    /**
     * Lookup the specified class by name
     * @param className
     * @return
     */
    public Class<?> loadClass(String className ) throws ClassNotFoundException {

        checkState();
        try {
            return getClass().getClassLoader().loadClass( className );
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Specified Java Class [" + className + "] was not found: " + e.getMessage(), e);
        }
    }

    /**
     * Adds the MetaData
     * @deprecated Use MetaData.addChild
     */
    public void addMetaData(MetaData mc) {
        checkState();
        addChild(mc);
    }

    /**
     * Adds the child MetaData
     */
    @Override
    public void addChild(MetaData mc) {
        checkState();
        super.addChild(mc);
    }

    /**
     * Removes the MetaData
     * @deprecated Use MetaData.deleteChild()
     */
    public void removeMetaData( Class<MetaData> c, String name) throws MetaDataNotFoundException {
        checkState();
        deleteChild(getMetaDataByName( c, name));
    }

    /**
     * Unloads the MetaDataLoader
     */
    public void destroy() {

        if ( isDestroyed ) throw new IllegalStateException( "MetaDataLoader [" + getName() + "] was already destroyed!" );

        log.info("Destroying the [" + getName() + "] MetaDataLoader");

        // Remove all classes
        clearChildren();

        isDestroyed = true;

        // Unregister the class loader
        if ( isRegistered ) {
            MetaDataRegistry.unregisterLoader(this);
        }
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    ////////////////////////////////////////////////////
    // MISC METHODS

    public String toString() {
        if (getParent() == null) {
            return "MetaDataLoader[" + getSubTypeName() + ":" + getName() + "]";
        } else {
            return "MetaDataLoader[" + getSubTypeName() + ":" + getName() + "@" + getParent().toString() + "]";
        }
    }
}
