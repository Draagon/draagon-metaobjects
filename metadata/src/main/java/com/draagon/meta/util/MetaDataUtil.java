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
import com.draagon.meta.relation.key.ObjectKey;
import com.draagon.meta.relation.ref.ObjectReference;

import java.lang.reflect.Field;
import java.util.List;

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
   * Returns the MetaObject for the class name string specified
   * @param name MetaObject name
   * @return MetaClass representing the name specified
   * @deprecated Use MetaDataRegistry.findMetaObjectByName( name )
   */
  public static MetaObject forName( String name ) {
      return MetaDataRegistry.findMetaObjectByName( name );
  }

  /**
   * Returns a new object based on the MetaClass name specified
   * @param metaClassName
   * @return new object instance
   * @deprecated Use MetaDataRegistry.findMetaObjectByName( name )
   */
  public static Object newInstance( String metaClassName ) {
    return newInstance( MetaDataRegistry.findMetaObjectByName( metaClassName ));
  }

  /**
   * Returns a new object based on the MetaClass name specified
   * @return new object instance
   * @deprecated Use MetaObject.newInstance()
   */
  public static Object newInstance( MetaObject mc ) {
      return mc.newInstance();
  }

  /**
   * Returns the MetaClass associated with the specified Class or
   * null if no CLASSNAME static field exists on the object.
   * @param c Class to retrieve
   * @return MetaClass
   * @deprecated Use MetaDataRegistry.findMetaObjectByName( name )
   */
  public static MetaObject forClass( Class<?> c )
  {
    try {
      Field f = c.getField( "CLASSNAME" );
      String metaclassname = (String) f.get( null );
      //return MetaObject.forName( metaclassname );
      return MetaDataRegistry.findMetaObjectByName( metaclassname );
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
   *
   * @deprecated Use MetaDataRegistry.findMetaObject( o )
   */
  public static MetaObject findMetaObject(Object o) {
    return MetaDataRegistry.findMetaObject( o );
  }


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


  /** Gets the MetaObject referenced by this MetaData using the objectRef attribute */
  public static MetaObject getObjectRef( MetaField d ) {

    synchronized ( d ) {

      final String KEY = "getObjectRef()";

      MetaObject o = (MetaObject) d.getCacheValue(KEY);

      if (o == null) {

        // Try to find an ObjectReference on a MetaField
        if ( d instanceof MetaField ) {
          List<MetaData> refs = ((MetaField) d).getChildrenOfType( ObjectReference.TYPE_OBJECTREF, true );
          if ( !refs.isEmpty() ) {
            o = ((ObjectReference) refs.get(0)).getReferencedObject();
          }
        }

        // If it's an ObjectReference access it directly
        //if ( o == null || d instanceof ObjectReference) {
        //  o = ((ObjectReference) d).getReferencedObject();
        //}

        // Look for the old way with the ATTR_OBJECT_REF
        if ( o == null ) {

          String objectRef = d.getMetaAttr(ATTR_OBJECT_REF).getValueAsString();
          if (objectRef != null) {

            String name = expandPackageForMetaDataRef(findPackageForMetaData(d), objectRef);

            try {
              o = d.getLoader().getMetaObjectByName(name);
            } catch (MetaDataNotFoundException e) {
              throw new MetaObjectNotFoundException("MetaObject[" + name + "] referenced by MetaData [" + d + "] does not exist", name);
            }
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
