/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.util;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.registry.MetaDataLoaderRegistry;
import com.draagon.meta.registry.ServiceRegistryFactory;
import com.draagon.meta.object.MetaObject;

/**
 * Utility class providing helper methods for MetaData operations.
 * Contains commonly used operations for working with MetaData objects, loaders, and registries.
 * 
 * @author dmealing
 * @since 1.0
 */
public class MetaDataUtil {

  public final static String ATTR_OBJECT_REF = MetaObject.ATTR_OBJECT_REF;
  public final static String SEP = MetaDataLoader.PKG_SEPARATOR;


  /** Find an actual package traversing parents if needed */
  public static String findPackageForMetaData( MetaData d ) {

    synchronized ( d ) {

      final String KEY = "findPackageForMetaData()";

      String pkg = (String) d.getCacheValue(KEY);

      if (pkg == null) {

        MetaData p = d; // d.getParent();
        pkg = p.getPackage();

        while ((pkg == null || pkg.equals("")) && p != null) {
          p = p.getParent();
          pkg = p != null ? p.getPackage() : "";
        }

        d.setCacheValue( KEY, pkg );
      }

      return pkg;
    }
  }

  public static boolean hasObjectRef( MetaField f ) {
    return f.hasMetaAttr(ATTR_OBJECT_REF);
  }

  /** Gets the MetaObject referenced by this MetaData using the objectRef attribute */
  public static MetaObject getObjectRef( MetaField d ) throws MetaDataNotFoundException {

    synchronized ( d ) {

      final String KEY = "getObjectRef()";

      MetaObject o = (MetaObject) d.getCacheValue(KEY);

      if (o == null) {

          String objectRef = d.getMetaAttr(ATTR_OBJECT_REF).getValueAsString();
          if (objectRef != null) {

            String name = expandPackageForMetaDataRef(findPackageForMetaData(d), objectRef);

            try {
              o = d.getLoader().getMetaObjectByName(name);
            } catch (MetaDataNotFoundException e) {
              throw MetaDataNotFoundException.forObject(name, d);
            }
          }

        d.setCacheValue(KEY, o);
      }

      return o;
    }
  }

  /** Expands the provided package if using relative package paths */
  public static String expandPackageForMetaDataRef(String basePkg, String metaDataRef ) {
    return expandPackageFor( basePkg, metaDataRef );
  }

  /** Expands the provided package if using relative package paths */
  public static String expandPackageForPath(String basePkg, String pkgPath ) {
    return expandPackageFor( basePkg, pkgPath );
  }

  /** Expands the provided value if using relative package paths */
  private static String expandPackageFor(String basePkg, String value ) {

    final String origPkg = value;

    // If there is no base package then handle a few default behaviors
    if ( basePkg == null || basePkg.isEmpty() ) basePkg = "";

    // If it's relative, then strip it off
    if ( value.startsWith( SEP )) {
      value = basePkg + value;
    }

    // Drop down the package paths
    else if ( value.startsWith( ".."+SEP )) {

      // Split out the base package in case we have to traverse downward
      String [] base = basePkg.split( SEP );
      int i = base.length;

      // Trim off all the proceeding dropdown paths
      while (value.startsWith(".."+SEP)) {
        value = value.substring(4);
        i--;
        if (i < 0) throw new IllegalStateException("Base package [" + basePkg + "] cannot drop that many relative paths for [" + origPkg + "]");
      }

      // Reform the package
      for( int x = i-1; x >= 0; x-- ) {
        value = base[x] + SEP + value;
      }
    }

    return value;
  }

  // ========================================================================
  // OSGi-Compatible Registry Helper Methods
  // ========================================================================

  /**
   * Gets an OSGi-compatible MetaDataLoaderRegistry instance.
   * 
   * <p>This helper method centralizes the creation of MetaDataLoaderRegistry instances
   * to ensure consistent OSGi compatibility across the entire codebase. If the registry
   * creation logic needs to change in the future, it only needs to be updated here.</p>
   * 
   * @param context The calling object context (for future extensibility)
   * @return OSGi-compatible MetaDataLoaderRegistry instance
   * @since 6.0.0
   */
  public static MetaDataLoaderRegistry getMetaDataLoaderRegistry(Object context) {
    // If context is a MetaData object, try to find a registry containing its loader
    if (context instanceof MetaData) {
      MetaData metaData = (MetaData) context;
      MetaDataLoader loader = metaData.getLoader();
      if (loader != null) {
        // Create a registry and register this loader
        // This ensures the context loader is available for lookups
        MetaDataLoaderRegistry registry = new MetaDataLoaderRegistry(ServiceRegistryFactory.getDefault());
        registry.registerLoader(loader);
        return registry;
      }
    }
    
    // Default behavior: create new registry with service discovery
    return new MetaDataLoaderRegistry(ServiceRegistryFactory.getDefault());
  }

  /**
   * Finds a MetaObject for the given object instance using OSGi-compatible registry.
   * 
   * <p>This is the preferred replacement for the legacy static 
   * {@code MetaDataRegistry.findMetaObject()} method.</p>
   * 
   * @param obj The object to find metadata for
   * @param context The calling object context (for future extensibility)
   * @return MetaObject that can handle the given object
   * @throws MetaDataNotFoundException if no suitable MetaObject is found
   * @since 6.0.0
   */
  public static MetaObject findMetaObject(Object obj, Object context) throws MetaDataNotFoundException {
    MetaDataLoaderRegistry registry = getMetaDataLoaderRegistry(context);
    return registry.findMetaObject(obj);
  }

  /**
   * Finds a MetaObject by name using OSGi-compatible registry.
   * 
   * <p>This is the preferred replacement for the legacy static 
   * {@code MetaDataRegistry.findMetaObjectByName()} method.</p>
   * 
   * @param name Fully qualified metadata name (e.g., "com.example::User")
   * @param context The calling object context (for future extensibility)
   * @return MetaObject with the specified name
   * @throws MetaDataNotFoundException if no MetaObject with the given name is found
   * @since 6.0.0
   */
  public static MetaObject findMetaObjectByName(String name, Object context) throws MetaDataNotFoundException {
    MetaDataLoaderRegistry registry = getMetaDataLoaderRegistry(context);
    return registry.findMetaObjectByName(name);
  }

  /**
   * Finds a MetaDataLoader by name using OSGi-compatible registry.
   * 
   * <p>This is the preferred replacement for the legacy static 
   * {@code MetaDataRegistry.getDataLoader()} method.</p>
   * 
   * @param loaderName Name of the loader to find
   * @param context The calling object context (for future extensibility)
   * @return MetaDataLoader with the specified name, or null if not found
   * @since 6.0.0
   */
  public static MetaDataLoader findMetaDataLoaderByName(String loaderName, Object context) {
    MetaDataLoaderRegistry registry = getMetaDataLoaderRegistry(context);
    return registry.getDataLoaders().stream()
        .filter(loader -> loaderName.equals(loader.getName()))
        .findFirst()
        .orElse(null);
  }

  /**
   * Gets all registered MetaDataLoaders using OSGi-compatible registry.
   * 
   * <p>This is the preferred replacement for the legacy static 
   * {@code MetaDataRegistry.getDataLoaders()} method.</p>
   * 
   * @param context The calling object context (for future extensibility)
   * @return Collection of all registered MetaDataLoaders
   * @since 6.0.0
   */
  public static java.util.Collection<MetaDataLoader> getAllMetaDataLoaders(Object context) {
    MetaDataLoaderRegistry registry = getMetaDataLoaderRegistry(context);
    return registry.getDataLoaders();
  }
}
