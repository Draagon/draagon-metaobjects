/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.web.view.html;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.*;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import com.draagon.meta.web.view.*;
import static com.draagon.meta.view.MetaView.TYPE_VIEW;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.*;
import javax.servlet.jsp.*;

public class TextView extends HtmlView
{
  private static final Logger log = LoggerFactory.getLogger(TextView.class);

  /**
   * Register TextView type with registry.
   * Called by WebMetaDataProvider during service discovery.
   */
  public static void registerTypes(MetaDataRegistry registry) {
    registry.registerType(TextView.class, def -> def
      .type(TYPE_VIEW).subType("text")
      .description("HTML text input view")

      // INHERIT FROM BASE VIEW
      .inheritsFrom("view", "base")

      // NO TEXT-SPECIFIC ATTRIBUTES (only uses inherited base attributes)
      // CHILD REQUIREMENTS INHERITED FROM BASE VIEW:
      // - Attributes (attr.*)
    );

    log.debug("Registered TextView with unified registry");
  }

  public TextView(String name) {
        super(name);
    }

  /**
   * Draws a Textbox view of the field
   */
  public void doView( PageContext page, Object o, String label, int mode, Map params )
    throws MetaDataException
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
    throws MetaDataException
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

  ///////////////////////////////////////////////////
  // Service Provider Pattern Registration

  // Text view attribute constants
  public static final String TEXT_MAX_LENGTH = "textMaxLength";
  public static final String TEXT_SIZE = "textSize";
  public static final String TEXT_PATTERN = "textPattern";
  public static final String TEXT_AUTOCOMPLETE = "textAutocomplete";

  /**
   * Registers text view attributes for use by the service provider pattern.
   * Called by WebMetaDataProvider to extend existing MetaData types with text view-specific attributes.
   */
  public static void registerTextViewAttributes(com.draagon.meta.registry.MetaDataRegistry registry) {
      // Text input specific attributes
      registry.findType("field", "string")
          .optionalAttribute(TEXT_MAX_LENGTH, "int")
          .optionalAttribute(TEXT_SIZE, "int")
          .optionalAttribute(TEXT_PATTERN, "string")
          .optionalAttribute(TEXT_AUTOCOMPLETE, "string");
  }
}
