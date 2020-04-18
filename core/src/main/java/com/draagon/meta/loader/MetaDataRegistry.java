package com.draagon.meta.loader;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.MetaObjectNotFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Used to store a static registry of MetaDataLoaders.  Allows for static methods to find metadata.
 * Not for use with OSGi
 *
 * Created by dmealing on 11/30/16.
 */
public class MetaDataRegistry {

    private final static Map<String,MetaDataLoader> metaDataLoaders = Collections.synchronizedMap(new WeakHashMap<String,MetaDataLoader>());

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
     * Retrieves the MetaObject for the specified Object
     */
    public static MetaObject findMetaObject(Object obj ) throws MetaDataNotFoundException {

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
    public static MetaObject findMetaObjectByName( String name ) throws MetaDataNotFoundException {

        for (MetaDataLoader l : getDataLoaders()) {
            MetaObject d = l.getMetaObjectByName( name );
            if ( d != null ) return d;
        }

        throw new MetaDataNotFoundException( "MetaObject with name [" + name + "] not found in MetaDataRegistry", name);
    }

    /**
     * Retrieves the MetaObject with the specified name
     * IMPORTANT:  This traverses ALL classloaders, use getMetaDataByName if you know the metadataloader to use
     */
    public static <T extends MetaData> T findMetaDataByName( Class<T> c, String name ) throws MetaDataNotFoundException {

        for (MetaDataLoader l : getDataLoaders()) {
            T d = (T) l.getChild( name, c );
            if ( d != null ) return (T) d;
        }

        throw new MetaDataNotFoundException( "MetaObject with name [" + name + "] not found in MetaDataRegistry", name);
    }
}
