/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.draagon.meta.web.view;

import com.draagon.meta.attr.MetaAttributeNotFoundException;
import com.draagon.meta.view.MetaView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewHelper
{
  //////////////////////////////////////////////////////////
  // Attribute Helpers

  public static Boolean getBooleanAttribute( MetaView view, String label )
  {
    String tmp = getStringAttribute( view, label );
    if ( tmp != null && tmp.length() > 0 )
    {
      if ( tmp.charAt( 0 ) == 't' || tmp.charAt( 0 ) == 'y' )
        return Boolean.valueOf( true );

      else if ( tmp.charAt( 0 ) == 'f' || tmp.charAt( 0 ) == 'n' )
        return Boolean.valueOf( false );

      //log.error( "(getBooleanParam) Boolean format exception for field [" + toString() + "] with param [" + label + "][" + tmp + "]" );
    }

    return null;
  }

  public static String getStringAttribute( MetaView view, String label )
  {
    try {
      return (String) view.getMetaAttr( label ).getValue();
    } catch( MetaAttributeNotFoundException e ) {
      return null;
    }
  }

  public static String [] getStringArrayAttribute( MetaView view, String label )
  {
    String tmp = getStringAttribute( view, label );
    if ( tmp == null ) return null;

    List<String> v = new ArrayList<String>();

    while( true )
    {
      int j = tmp.indexOf( ',' );
      if ( j >= 0 )
      {
        v.add( tmp.substring( 0, j ));
        tmp = tmp.substring( j + 1 );
      }
      else
      {
        v.add( tmp );
        break;
      }
    }

    String [] ret = new String[ v.size() ];
    for( int j = 0; j < ret.length; j++ )
      ret[ j ] = v.get( j );

    return ret;
  }

  public static int [] getIntArrayAttribute( MetaView view, String label )
  {
    String tmp = getStringAttribute( view, label );
    if ( tmp == null ) return null;

    List<String> v = new ArrayList<String>();

    while( true )
    {
      int j = tmp.indexOf( ',' );
      if ( j >= 0 )
      {
        v.add( tmp.substring( 0, j ));
        tmp = tmp.substring( j + 1 );
      }
      else
      {
        v.add( tmp );
        break;
      }
    }

    try {
      int [] ret = new int[ v.size() ];
      for( int j = 0; j < ret.length; j++ )
        ret[ j ] = Integer.parseInt( v.get( j ));

      return ret;
    }
    catch ( NumberFormatException e ) {
      //log.error( "(getIntArrayParam) Number format exception for field ["
      //  + toString() + "] with param [" + label + "][" + tmp + "]", e );
    }

    return null;
  }

  public static int getIntAttribute( MetaView view, String label, int def )
    throws NumberFormatException
  {
    try {
      view.getMetaAttr( label ).getValue();
      return getIntAttribute( view, label );
    } catch( MetaAttributeNotFoundException e ) {
      return def;
    }
  }

  public static int getIntAttribute( MetaView view, String label )
    throws NumberFormatException
  {
    String tmp = getStringAttribute( view, label );
    if ( tmp == null )
      throw new NumberFormatException( "Attribute [" + label + "] not found" );

    return Integer.parseInt( tmp );
  }

  //////////////////////////////////////////////////////////
  // Parameter Helpers

  /**
   * Returns a String parameter from the properties
   */
  public static String getStringParam( Map p, String name, String def )
  {
    String val = (String) p.get( name );
    if ( val == null ) return def;
    else return val;
  }

  /**
   * Returns an integer from the parameter map and uses the
   * default override
   */
  public static int getIntParam( Map p, String name, int def )
  {
    String val = (String) p.get( name );

    try {
      if ( val != null ) return Integer.parseInt( val );
    }
    catch( Exception e ) { }

    return def;
  }

  public static boolean getBooleanParam( Map p, String name, boolean def )
  {
    String val = (String) p.get( name );
    if ( val == null || val.length() == 0 ) return def;

        val = val.toLowerCase();

        if ( val.charAt( 0 ) == 't' || val.charAt( 0 ) == 'y' )
            return true;

        else if ( val.charAt( 0 ) == 'f' || val.charAt( 0 ) == 'n' )
            return false;

    return def;
  }
}