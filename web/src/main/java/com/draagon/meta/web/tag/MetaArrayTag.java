/*
 * Copyright 2001 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.web.tag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

public class MetaArrayTag extends TagSupport
{
  private static Log log = LogFactory.getLog( MetaArrayTag.class );

  private String name = null;

  private Collection mCollection = null;
  private Iterator mIter = null;

  public String getName() { return name; }
  public void setName( String name ) { this.name = name; }

  public int doStartTag()
    throws JspException
  {
    mIter = null;

    try
    {
      mCollection = (Collection) pageContext.getRequest().getAttribute( name );
    }
    catch( Exception e )
    {
      log.error( "Error retrieving Collection in request with name(" + name + ")", e );
      return SKIP_BODY;
    }

    if ( mCollection == null )
    {
      log.error( "No Collection was found in the request with name (" + name + ")" );
      return SKIP_BODY;
    }

    mIter = mCollection.iterator();

    pageContext.getRequest().removeAttribute( name );

    if ( !mIter.hasNext() ) return Tag.SKIP_BODY;

    pageContext.getRequest().setAttribute( name, mIter.next() );
    return EVAL_BODY_INCLUDE;
  }

  public int doAfterBody()
    throws JspException
  {
    if ( mIter.hasNext() )
    {
      pageContext.getRequest().setAttribute( name, mIter.next() );
      return EVAL_BODY_AGAIN;
    }

    return SKIP_BODY;
  }

  public int doEndTag()
    throws JspException
  {
	pageContext.getRequest().setAttribute( name, mCollection );
	
    mCollection = null;
    mIter = null;
    
    return EVAL_PAGE;
  }
}
