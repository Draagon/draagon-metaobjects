/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.web.view;

import com.draagon.meta.view.MetaView;
import com.draagon.meta.*;

import java.util.Map;

import javax.servlet.http.*;
import javax.servlet.jsp.*;

public abstract class WebView extends MetaView
{
  //private static Log log = LogFactory.getLog( WebView.class );

  public WebView( String name )
  {
      super( name );
  }

  /**
   * Calling doView without overriding the label
   */
  public final void doView( PageContext page, Object o, int mode, Map<String,String> params )
    throws MetaException
  {
      doView( page, o, getParent().getName(), mode, params );
  }

  /**
   * Basic doView with a label
   */
  public abstract void doView( PageContext page, Object o, String label, int mode, Map<String,String> params ) throws MetaException;

  /**
   * Retrieve the value for the field and place it into the object
   */
  public final void getValue( HttpServletRequest request, Object o ) throws MetaException
  {
    getValue( request, o, getParent().getName() );
  }

  /**
   * Retrieve the value for the field and place it into the object
   */
  public abstract void getValue( HttpServletRequest request, Object o, String label ) throws MetaException;

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

