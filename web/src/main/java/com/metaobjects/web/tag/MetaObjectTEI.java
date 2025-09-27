/*
 * Copyright 2001 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.web.tag;

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
