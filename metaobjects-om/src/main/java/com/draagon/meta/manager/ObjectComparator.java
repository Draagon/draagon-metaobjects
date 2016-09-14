/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.*;
import com.draagon.meta.manager.exp.SortOrder;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ObjectComparator implements Comparator<Object>
{
  private static Log log = LogFactory.getLog( ObjectComparator.class );

  private SortOrder mSort = null;

  public ObjectComparator( SortOrder sort )
  {
    mSort = sort;
  }

  public int compare( Object a, Object b )
  {
    try {
      return performCompare( a, b, mSort );
    }
    catch( MetaException e ) {
      log.error( e.getMessage(), e );
      return 0;
    }
  }

  private int performCompare( Object a, Object b, SortOrder sort )
    throws MetaException
  {
    int rc = 0;

    if ( sort == null ) return 0;

    MetaObject ma = MetaDataLoader.findMetaObject( a );
    MetaObject mb = MetaDataLoader.findMetaObject( b );

    if ( !ma.equals( mb ))
      throw new MetaException( "Objects are not of the same MetaClass: [" + ma + "] != [" + mb + "]" );

    MetaField mf = ma.getMetaField( sort.getField() );

    //try
    //{
      Object aVal = mf.getObject( a );
      Object bVal = mf.getObject( b );

      if( aVal == null && bVal == null )
        rc = 0;
      else if ( aVal == null && bVal != null )
        rc = -1;
      else if ( aVal != null && bVal == null )
        rc = 1;
      else
      {
        if ( !aVal.getClass().equals( bVal.getClass() ))
          throw new MetaException( "Object data values are not of the same type" );

        switch( mf.getType() )
        {
          case MetaField.BOOLEAN:
          {
            if ( !((Boolean) aVal ).booleanValue() && ((Boolean) bVal ).booleanValue() ) rc = -1;
            else if ( ((Boolean) aVal ).booleanValue() == ((Boolean) bVal ).booleanValue() ) rc = 0;
            else if ( ((Boolean) aVal ).booleanValue() && !((Boolean) bVal ).booleanValue() ) rc = 1;
            break;
          }
          case MetaField.BYTE:    rc = ((Byte) aVal ).compareTo( (Byte) bVal ); break;
          case MetaField.SHORT:   rc = ((Short) aVal ).compareTo( (Short) bVal ); break;
          case MetaField.INT:     rc = ((Integer) aVal ).compareTo( (Integer) bVal ); break;
          case MetaField.LONG:    rc = ((Long) aVal ).compareTo( (Long) bVal ); break;
          case MetaField.FLOAT:   rc = ((Float) aVal ).compareTo( (Float) bVal ); break;
          case MetaField.DOUBLE:  rc = ((Double) aVal ).compareTo( (Double) bVal ); break;
          case MetaField.STRING:  rc = ((String) aVal ).compareToIgnoreCase( (String) bVal ); break;
          case MetaField.DATE:    rc = ((Date) aVal ).compareTo( (Date) bVal ); break;

          // WARN:  Maybe try some reflection here or something for a compareTo operator
          case MetaField.OBJECT:  rc = aVal.toString().compareTo( bVal.toString() ); break;
        }
      }
    //}
    //catch( Exception e ) {
    //  log.warn( "Error comparing field [" + a + "] with [" + b + "] for sort [" + sort + "]: " + e.getMessage(), e );
    //}

    if ( rc == 0 )
    {
      if ( sort.hasNext() ) rc = performCompare( a, b, sort.getNext() );
    }
    else
    {
      // If descending, then reverse the order
      if ( sort.getOrder() == SortOrder.DESC )
        rc = -rc;
    }

    return rc;
  }
}
