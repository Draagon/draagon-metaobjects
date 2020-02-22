/*
 * Created on Jul 23, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.draagon.meta.util;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.MetaDataRegistry;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectNotFoundException;

import java.lang.reflect.Field;

/**
 * @author dmealing
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MetaDataUtil {

  public final static String ATTR_OBJECT_REF = "objectRef";
  public final static String SEP = MetaDataLoader.PKG_SEPARATOR;

  /**
   * Returns the MetaClass for the class name string specified
   * @param metaClassName MetaClass name
   * @return MetaClass representing the name specified
   */
  public static MetaObject forName( String metaClassName ) {
      return MetaObject.forName( metaClassName );
  }

  /**
   * Returns a new object based on the MetaClass name specified
   * @param metaClassName
   * @return new object instance
   */
  public static Object newInstance( String metaClassName ) {
    return newInstance( MetaDataUtil.forName( metaClassName ));
  }

  /**
   * Returns a new object based on the MetaClass name specified
   * @return new object instance
   */
  public static Object newInstance( MetaObject mc ) {
      return mc.newInstance();
  }

  /**
   * Returns the MetaClass associated with the specified Class or
   * null if no CLASSNAME static field exists on the object.
   * @param c Class to retrieve
   * @return MetaClass
   */
  public static MetaObject forClass( Class<?> c )
  {
    try {
      Field f = c.getField( "CLASSNAME" );
      String metaclassname = (String) f.get( null );
      return MetaObject.forName( metaclassname );
    }
    catch (SecurityException e1) {
      throw new RuntimeException( "Security violation accessing CLASSNAME field on class [" + c + "]", e1 );
    }
    catch (NoSuchFieldException e1) {
      // If no CLASSNAME field exists, then no logic bean exists for it
      return null;
    }
    catch (IllegalArgumentException e2) {
      throw new RuntimeException( "Illegal argument violation accessing CLASSNAME field on class [" + c + "]", e2 );
    }
    catch (IllegalAccessException e2) {
      throw new RuntimeException( "Illegal access violation accessing CLASSNAME field on class [" + c + "]", e2 );
    }
  }

  /**
   * @param o
   * @return
   */
  public static MetaObject findMetaObject(Object o) {
    return MetaDataRegistry.findMetaObject( o );
  }

  /*public static String getMetaFieldLabel(HttpServletRequest request, MetaField mf) {
    MessageResources resources = (MessageResources) request.getAttribute(Globals.MESSAGES_KEY);
    String label = mf.getName();
    if(mf.hasAttribute("labelKey")) {
      label = (String) mf.getAttribute("labelKey");
      label = resources.getMessage( request.getLocale(), label );
    }
    return label;
  }*/

  /** Find an actual package traversing parents if needed */
  public static String findPackageForMetaData( MetaData d ) {

    synchronized ( d ) {

      final String KEY = "findPackageForMetaData()";

      String pkg = (String) d.getCacheValue(KEY);

      if (pkg == null) {

        MetaData p = d.getParent();
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


  /** Gets the MetaObject referenced by this MetaData using the objectRef attribute */
  public static MetaObject getObjectRef( MetaData d ) {

    synchronized ( d ) {

      final String KEY = "getObjectRef()";

      MetaObject o = (MetaObject) d.getCacheValue(KEY);

      if (o == null) {

        Object a = d.getAttribute(ATTR_OBJECT_REF);
        if (a != null) {

          String name = expandPackageForMetaDataRef(findPackageForMetaData(d), a.toString());

          try {
            o = d.getLoader().getMetaDataByName(MetaObject.class, name);
          } catch (MetaDataNotFoundException e) {
            throw new MetaObjectNotFoundException("MetaObject[" + name + "] referenced by MetaData [" + d + "] does not exist", name);
          }

          d.setCacheValue(KEY, o);
        }
      }

      return o;
    }
  }

  /** Expands the provided package if using relative package paths */
  public static String expandPackageForMetaDataRef(String basePkg, String metaDataRef ) {

    final String origRef = metaDataRef;

    // If there is no base package then handle a few default behaviors
    if ( basePkg == null || basePkg.isEmpty() ) basePkg = "";

    // If it's relative, then strip it off
    if ( metaDataRef.startsWith( SEP )) {
      metaDataRef = basePkg + metaDataRef;
    }
    else if ( metaDataRef.startsWith( ".."+SEP )) {

      // Split out the base package in case we have to traverse downward
      String [] base = basePkg.split( SEP );
      int i = base.length;

      // Trim off all the proceeding dropdown paths
      while (metaDataRef.startsWith(".."+SEP)) {
        metaDataRef = metaDataRef.substring(4);
        i--;
        if (i < 0) throw new IllegalStateException("Base package [" + basePkg + "] cannot drop that many relative paths for [" + origRef + "]");
      }

      // Reform the package
      for( int x = i-1; x >= 0; x-- ) {
        metaDataRef = base[x] + SEP + metaDataRef;
      }
    }

    return metaDataRef;
  }

  /** Expands the provided package if using relative package paths */
  public static String expandPackageForPath(String basePkg, String pkgPath ) {

    final String origPkg = pkgPath;

    // If there is no base package then handle a few default behaviors
    if ( basePkg == null || basePkg.isEmpty() ) basePkg = "";

    // If it's relative, then strip it off
    if ( pkgPath.startsWith( SEP )) {
      pkgPath = basePkg + pkgPath;
    }

    // Drop down the package paths
    else if ( pkgPath.startsWith( ".."+SEP )) {

      // Split out the base package in case we have to traverse downward
      String [] base = basePkg.split( SEP );
      int i = base.length;

      // Trim off all the proceeding dropdown paths
      while (pkgPath.startsWith(".."+SEP)) {
        pkgPath = pkgPath.substring(4);
        i--;
        if (i < 0) throw new IllegalStateException("Base package [" + basePkg + "] cannot drop that many relative paths for [" + origPkg + "]");
      }

      // Reform the package
      for( int x = i-1; x >= 0; x-- ) {
        pkgPath = base[x] + SEP + pkgPath;
      }
    }

    return pkgPath;
  }
}
