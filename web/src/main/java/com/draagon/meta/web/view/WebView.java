/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.web.view;

import com.draagon.meta.view.MetaView;
import com.draagon.meta.*;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataTypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import javax.servlet.http.*;
import javax.servlet.jsp.*;

@MetaDataTypeHandler(type = "view", subType = "web", description = "Web-based view for HTML form rendering")
public abstract class WebView extends MetaView
{
  private static final Logger log = LoggerFactory.getLogger(WebView.class);
  
  public final static String SUBTYPE_WEB = "web";

  // Self-registration with unified registry
  static {
      try {
          MetaDataRegistry.registerType(WebView.class, def -> def
              .type("view").subType(SUBTYPE_WEB)
              .description("Web-based view for HTML form rendering")
              .optionalChild("attr", "*")
          );
          log.debug("Registered WebView type with unified registry");
      } catch (Exception e) {
          log.error("Failed to register WebView type with unified registry", e);
      }
  }

  public WebView( String name )
  {
      super( SUBTYPE_WEB, name );
  }

  /**
   * Calling doView without overriding the label
   */
  public final void doView( PageContext page, Object o, int mode, Map<String,String> params )
    throws MetaDataException
  {
      doView( page, o, getParent().getName(), mode, params );
  }

  /**
   * Basic doView with a label
   */
  public abstract void doView( PageContext page, Object o, String label, int mode, Map<String,String> params ) throws MetaDataException;

  /**
   * Retrieve the value for the field and place it into the object
   */
  public final void getValue( HttpServletRequest request, Object o ) throws MetaDataException
  {
    getValue( request, o, getParent().getName() );
  }

  /**
   * Retrieve the value for the field and place it into the object
   */
  public abstract void getValue( HttpServletRequest request, Object o, String label ) throws MetaDataException;

  /**
   * Converts the mode value to an english readable mode
   */
  protected String modeToString( int mode )
  {
    switch( mode ) {
      case READ: return "READ";
      case EDIT: return "EDIT";
      case HIDE: return "HIDE";
    }

    return "UNKNOWN";
  }  
}

