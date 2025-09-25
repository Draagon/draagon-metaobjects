/*
 * Copyright 2001 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.web.tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.jsp.tagext.*;

public class MetaObjectTEI extends TagExtraInfo
{
  private static final Logger log = LoggerFactory.getLogger(MetaObjectTEI.class);

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
