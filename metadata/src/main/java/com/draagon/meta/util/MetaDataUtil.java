/*
 * Created on Jul 23, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.draagon.meta.util;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.MetaDataRegistry;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectNotFoundException;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author dmealing
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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
  public static MetaObject getObjectRef( MetaField d ) throws MetaObjectNotFoundException {

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
              throw new MetaObjectNotFoundException("MetaObject[" + name + "] referenced by MetaData [" + d + "] does not exist", name);
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
}
