/*
 * Created on Jul 23, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.draagon.meta.util;

import java.lang.reflect.Field;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.loader.MetaDataLoader;

/**
 * @author dmealing
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MetaClassUtil {

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
    return newInstance( MetaClassUtil.forName( metaClassName ));
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
  public static MetaObject findMetaClass(Object o) {
    return MetaDataLoader.findMetaObject( o );
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
}
