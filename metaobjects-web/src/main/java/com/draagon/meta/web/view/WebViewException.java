/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.web.view;

import com.draagon.meta.*;

public class WebViewException extends MetaException
{
  public WebViewException( String msg, Throwable t )
  {
    super( msg, t );
  }

  public WebViewException( String msg )
  {
    super( msg );
  }

  public WebViewException()
  {
    super( "WebView Exception" );
  }

}
