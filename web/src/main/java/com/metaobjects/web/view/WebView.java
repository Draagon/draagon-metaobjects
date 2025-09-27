/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.web.view;

import com.metaobjects.view.MetaView;
import com.metaobjects.*;
import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.attr.BooleanAttribute;
import com.metaobjects.attr.IntAttribute;
import static com.metaobjects.view.MetaView.TYPE_VIEW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import jakarta.servlet.http.*;
import jakarta.servlet.jsp.*;

public abstract class WebView extends MetaView
{
  private static final Logger log = LoggerFactory.getLogger(WebView.class);
  
  public final static String SUBTYPE_WEB = "web";

  /**
   * Register WebView type with registry.
   * Called by WebMetaDataProvider during service discovery.
   */
  public static void registerTypes(MetaDataRegistry registry) {
      registry.registerType(WebView.class, def -> def
          .type(TYPE_VIEW).subType(SUBTYPE_WEB)
          .description("Web-based view for HTML form rendering")

          // INHERIT FROM BASE VIEW
          .inheritsFrom("view", "base")

          // WEB-SPECIFIC CHILD REQUIREMENTS (base requirements inherited)
          .optionalChild("attr", "*")
          // Note: Base view attributes are inherited from view.base
      );
      // Registered WebView type with unified registry
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

  ///////////////////////////////////////////////////
  // Service Provider Pattern Registration

  // Base web view attribute constants
  public static final String WEB_LABEL = "webLabel";
  public static final String WEB_PLACEHOLDER = "webPlaceholder";
  public static final String WEB_CSS_CLASS = "webCssClass";
  public static final String WEB_INPUT_TYPE = "webInputType";
  public static final String WEB_VALIDATION_MESSAGE = "webValidationMessage";
  public static final String WEB_REQUIRED = "webRequired";
  public static final String WEB_READONLY = "webReadonly";
  public static final String WEB_HIDDEN = "webHidden";

  /**
   * Registers base web view attributes for use by the service provider pattern.
   * Called by WebMetaDataProvider to extend existing MetaData types with base web view attributes.
   */
  public static void registerWebViewAttributes(com.metaobjects.registry.MetaDataRegistry registry) {
      // Object-level web attributes
      registry.findType("object", "base")
          .optionalAttribute(WEB_CSS_CLASS, StringAttribute.SUBTYPE_STRING)
          .optionalAttribute(WEB_VALIDATION_MESSAGE, StringAttribute.SUBTYPE_STRING);

      registry.findType("object", "pojo")
          .optionalAttribute(WEB_CSS_CLASS, StringAttribute.SUBTYPE_STRING);

      // Field-level web attributes
      registry.findType("field", "base")
          .optionalAttribute(WEB_LABEL, StringAttribute.SUBTYPE_STRING)
          .optionalAttribute(WEB_PLACEHOLDER, StringAttribute.SUBTYPE_STRING)
          .optionalAttribute(WEB_CSS_CLASS, StringAttribute.SUBTYPE_STRING)
          .optionalAttribute(WEB_INPUT_TYPE, StringAttribute.SUBTYPE_STRING)
          .optionalAttribute(WEB_VALIDATION_MESSAGE, StringAttribute.SUBTYPE_STRING)
          .optionalAttribute(WEB_REQUIRED, BooleanAttribute.SUBTYPE_BOOLEAN)
          .optionalAttribute(WEB_READONLY, BooleanAttribute.SUBTYPE_BOOLEAN)
          .optionalAttribute(WEB_HIDDEN, BooleanAttribute.SUBTYPE_BOOLEAN);

      registry.findType("field", "string")
          .optionalAttribute(WEB_PLACEHOLDER, StringAttribute.SUBTYPE_STRING)
          .optionalAttribute(WEB_INPUT_TYPE, StringAttribute.SUBTYPE_STRING);
  }
}

