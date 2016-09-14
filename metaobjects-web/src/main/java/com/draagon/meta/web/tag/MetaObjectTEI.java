/*
 * Copyright 2001 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.web.tag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.jsp.tagext.*;

public class MetaObjectTEI extends TagExtraInfo
{
  private static Log log = LogFactory.getLog( MetaObjectTEI.class );

  public MetaObjectTEI()
  {
  }

  public VariableInfo[] getVariableInfo(TagData data)
  {
    if ( data.getAttributeString( "var" ) == null )
    {
      return new VariableInfo[ 0 ];
    }
    else
    {
      return new VariableInfo[]
      {
        new VariableInfo
        (
          data.getAttributeString("var"),
          String.class.getName(),
          true,
          VariableInfo.AT_BEGIN
        ),
      };
    }
  }
}
