/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.draagon.meta.web.view.html;

import com.draagon.meta.*;
import com.draagon.meta.web.view.*;
import com.draagon.meta.web.util.Param;

import java.io.*;
import java.util.*;

import jakarta.servlet.http.*;
import jakarta.servlet.jsp.*;

public class HtmlViewHelper
{
  //////////////////////////////////////////////////////////
  // Parameter Helpers

  public static String getOnChange( Map params )
  {
    String action = (String) params.get( "onChange" );
    if ( action == null || action.equals( "" )) return "";

    //return " onChange='" + action + "'";
    return " onChange=\"" + action + "\"";
  }

  public static String getReadOnly( Map params )
  {
    String action = (String) params.get( "isReadOnly" );

    if ( "true".equals( action ))
      return " readonly";
    else
      return "";
  }

  public static String getStyle( Map params )
  {
    StringBuilder html = new StringBuilder();

    String style = (String) params.get( "styleClass" );
    if ( style != null && !style.equals( "" ))
    {
      html.append( " class=\"" );
      html.append( style );
      html.append( "\"" );
    }

    String attrs = getStyleAttributes( params );
    if ( attrs != null && !attrs.equals( "" ))
    {
      html.append( " style=\"" );
      html.append( attrs );
      html.append( "\"" );
    }

    return html.toString();
  }

  private static String getStyleAttributes( Map params )
  {
    StringBuilder buf = new StringBuilder();

    appendStyleAttribute( buf, params, "width" );
    appendStyleAttribute( buf, params, "height" );

    return buf.toString();
  }

  private static void appendStyleAttribute( StringBuilder buf, Map params, String attr )
  {
    String attrVal = (String) params.get( attr );
    if ( attrVal != null && attrVal.length() > 0 )
    {
      buf.append( ' ' );
      buf.append( attr );
      buf.append( '=' );
      buf.append( attrVal );
      buf.append( ';' );
    }
  }


  //////////////////////////////////////////////////////////
  // Value Helpers

  /**
   * Retrieve the value for the field and place it into the object
   */
  public static String getStringValue( HttpServletRequest request, String label )
    throws ValueException
  {
    // Pull the value from the form
    String value = request.getParameter( label );

    // If the value is null, then it wasn't in the form
    if ( value == null )
      throw new ValueNotFoundException( "MetaField [" + label + "] was not found in the request" );

    return value;
  }

  /**
   * Retrieve the integer value from the request
   */
  public static int getIntValue( HttpServletRequest request, String label )
    throws ValueException
  {
    // Pull the value from the form
    String value = request.getParameter( label );

    // If the value is null, then it wasn't in the form
    if ( value == null )
      throw new ValueNotFoundException( "[" + label + "] was not found in the request" );

    int i = 0;
    try {
      if ( value.trim().length() > 0 )
        i = Integer.parseInt( value );
    }
    catch( NumberFormatException e ) {
      throw new InvalidValueException( "[" + label + "] had an invalid integer value [" + value + "]" );
    }

    return i;
  }

  /**
   * Retrieve the long value form the request
   */
  public static long getLongValue( HttpServletRequest request, String label )
    throws ValueException
  {
    // Pull the value from the form
    String value = request.getParameter( label );

    // If the value is null, then it wasn't in the form
    if ( value == null )
      throw new ValueNotFoundException( "[" + label + "] was not found in the request" );

    long l = 0;
    try {
      if ( value.trim().length() > 0 )
        l = Long.parseLong( value );
    }
    catch( NumberFormatException e ) {
      throw new InvalidValueException( "[" + label + "] had an invalid long value [" + value + "]" );
    }

    return l;
  }

  //////////////////////////////////////////////////////////
  // HTML Renderers

  public static void drawHidden( PageContext page, Map params )
    throws IOException
  {
    JspWriter out = page.getOut();
    out.print( "******" );
  }

  public static void drawText( PageContext page, String text, Map params )
    throws IOException
  {
    if ( text == null ) text = "";
    JspWriter out = page.getOut();
    out.print( text );
  }

  public static void drawTextBox( PageContext page, String label, String value, int size, int maxlength, Map params )
    throws IOException
  {
    JspWriter out = page.getOut();

    if ( value == null ) value = "";
    size = ViewHelper.getIntParam( params, "size", size );

    out.print(
        "<input "
        + getStyle( params )
        + getOnChange( params )
        + getReadOnly( params )
        + " type=text"
        + " size=" + size
        + " maxlength=" + maxlength
        + " name=\"" + label + "\""
        + " value=\"" + value + "\""
        + ">" );
  }

  /*public static void drawSelectBox( PageContext page, String label, int sel, String [] names, Map params )
    throws IOException
  {
    ArrayList data = new ArrayList();

    // Add the additional field names
    for( int i = 0; i < names.length; i++ ) {
      data.add( new Param( names[ i ], names[ i ] ));
    }

    // Draw the select box
    drawSelectBox( page, label, "" + sel, data, params );
  }*/

  public static void drawSelectBox( PageContext page, String label, String defSel, Collection<Param> data, Map params )
    throws IOException
  {
    JspWriter out = page.getOut();

    out.println(
        "<select "
        + getStyle( params )
        + getOnChange( params )
        + " name=\"" + label + "\""
        + ">" );

    int j = 0;
    for( Param nv : data )
    {
      out.print( "<option" );
      if ( nv.getValue().equals( defSel )) out.print( " selected" );
      out.print( " value=\"" + nv.getValue() + "\">" );
      out.println( nv.getName() );
    }

    out.print( "</select>" );
  }

  public static void drawTextArea( PageContext page, String label, String text, int cols, int rows, Map params )
    throws IOException
  {
    JspWriter out = page.getOut();

    if ( text == null ) text = "";
    cols = ViewHelper.getIntParam( params, "cols", cols );
    rows = ViewHelper.getIntParam( params, "rows", rows );

    out.print(
        "<textarea "
        + getStyle( params )
        + getOnChange( params )
        + getReadOnly( params )
        + " name=\"" + label + "\""
        + " cols=" + cols
        + " rows=" + rows
        + ">" );

    out.print( text );

    out.print( "</textarea>" );
  }

  public static void drawLink( PageContext page, String link, String text, String linkClass, Map params )
    throws IOException
  {
    JspWriter out = page.getOut();

    if ( text == null ) text = "&nbsp;";

    if ( link != null )
    {
      out.print("<a" );
      if ( linkClass != null ) out.print( " class=\"" + linkClass + "\"" );
      out.print(" href=\"" + link + "\">");
    }

    out.print( text );

    if ( link != null ) out.print("</a>");
  }

  /////////////////////////////////////////////////////
  // Miscellaneous Helpers

  /**
   * Converts a text String to HTML
   */
  public static String textToHtml( String text )
  {
    if ( text == null ) return "";
    StringBuilder b = new StringBuilder();
    for( int i = 0; i < text.length(); i++ )
    {
      int c = text.charAt( i );
      if ( c == '\n' ) b.append( "<br>\n" );
      else if ( c == '\r' ) ; // do nothing
      else if ( c == '<' ) b.append( "&lt;" );
      else if ( c == '>' ) b.append( "&gt;" );
      //else if ( c == '"' ) b.append( "&amp;" );
      else if ( c == '&' ) b.append( "&amp;" );
      //else if ( c == ' ' ) b.append( "&nbsp;" );
      else b.append( (char) c );
    }

    return b.toString();
  }
}
