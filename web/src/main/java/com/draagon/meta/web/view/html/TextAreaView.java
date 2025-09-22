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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import javax.servlet.jsp.*;

/**
 * <p>Renders a textarea view.</p>
 *
 * <p>The possible MetaView attributes are the following:
 * <ul>
 *   <li>cols - (Optional) The number of columns for the textarea</li>
 *   <li>rows - (Optional) The number of rows for the textarea</li>
 * </ul></p>
 */
@MetaDataType(type = "view", subType = "textarea", description = "HTML textarea input view")
public class TextAreaView extends TextView
{
  private static final Logger log = LoggerFactory.getLogger(TextAreaView.class);

  // Unified registry self-registration for textarea view
  static {
    try {
      MetaDataRegistry.registerType(TextAreaView.class, def -> def
        .type("view").subType("textarea")
        .description("HTML textarea input view")
        .optionalAttribute("cols", "int")
        .optionalAttribute("rows", "int")
        .optionalChild("attr", "string")
        .optionalChild("attr", "int")
        .optionalChild("attr", "boolean")
      );
      
      log.debug("Registered TextAreaView with unified registry");
    } catch (Exception e) {
      log.error("Failed to register TextAreaView with unified registry", e);
    }
  }

  public TextAreaView(String name) {
        super(name);
    }

  public void doView( PageContext page, Object o, String label, int mode, Map params )
      throws MetaDataException
  {
    MetaField mf = getMetaField( o );

    try
    {
      String val = mf.getString( o );
      String text = (val==null)?null:val.toString();

      int cols = ViewHelper.getIntAttribute( this, "cols", 40 );
      int rows = ViewHelper.getIntAttribute( this, "rows", 2 );

      // Render the text area
      doTextAreaView( page, label, mode, cols, rows, text, params );
    }
    catch( IOException e )
    {
      log.error( "Error displaying TextAreaView for field [" + mf + "] with mode [" + modeToString( mode ) + "]", e );
      throw new WebViewException( "Cannot render TextAreaView for field [" + mf + "] in mode [" + modeToString( mode ) + "]: " + e.getMessage(), e );
    }
  }

  public static void doTextAreaView( PageContext page, String label, int mode, int cols, int rows, String text, Map params )
    throws IOException
  {
    if ( mode == READ )
      HtmlViewHelper.drawText( page, HtmlViewHelper.textToHtml( text ), params );

    else if ( mode == EDIT )
      HtmlViewHelper.drawTextArea( page, label, text, cols, rows, params );

    else if ( mode == HIDE )
      HtmlViewHelper.drawHidden( page, params );
  }
}
