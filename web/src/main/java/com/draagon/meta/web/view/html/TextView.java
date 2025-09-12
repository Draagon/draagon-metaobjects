/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.web.view.html;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.*;
import com.draagon.meta.web.view.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.*;
import javax.servlet.jsp.*;

public class TextView extends HtmlView
{
  private static final Logger log = LoggerFactory.getLogger(TextView.class);

  public TextView(String name) {
        super(name);
    }

  /**
   * Draws a Textbox view of the field
   */
  public void doView( PageContext page, Object o, String label, int mode, Map params )
    throws MetaException
  {
    MetaField mf = getMetaField( o );
    //MetaClass mc = mf.getMetaClass();

    try
    {
      int maxlength = 255; // Default length since MetaField.getLength() is no longer available
      int size = ViewHelper.getIntParam( params, "size", ViewHelper.getIntAttribute( this, "size", maxlength ));

      String value = mf.getString( o );

      doTextView( page, label, mode, size, maxlength, value, params );
    }
    catch( IOException e )
    {
      log.error( "Error displaying TextView for field [" + mf + "] with mode [" + modeToString( mode ) + "]", e );
      throw new WebViewException( "Cannot render TextView for field [" + mf + "] in mode [" + modeToString( mode ) + "]: " + e.getMessage(), e );
    }
  }

  public static void doTextView( PageContext page, String label, int mode, int size, int maxlength, String text, Map params )
    throws IOException
  {
    if ( mode == READ )
      HtmlViewHelper.drawText( page, text, params );

    else if ( mode == EDIT )
      HtmlViewHelper.drawTextBox( page, label, text, size, maxlength, params );

    else if ( mode == HIDE )
      HtmlViewHelper.drawHidden( page, params );
  }
  
  /**
   * Retrieve the value for the field and place it into the object
   */
  public void getValue( HttpServletRequest request, Object o, String label )
    throws MetaException
  {
    MetaField mf = getMetaField( o );

    // Pull the value
    String value = HtmlViewHelper.getStringValue( request, label );

    // Validate the value before setting
    performValidation( o, value );

    // If the current value is null and the field has no input, don't set anything
    if ( mf.getObject( o ) == null && value.length() == 0 )
      return;

    // Set the value on the object
    mf.setString( o, value );
  }
}
