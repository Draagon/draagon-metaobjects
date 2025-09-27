/*
 * Copyright 2001 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.web.tag;

import com.metaobjects.field.MetaField;
import com.metaobjects.util.MetaDataUtil;
import com.metaobjects.object.MetaObject;
import com.metaobjects.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.jsp.*;
import jakarta.servlet.jsp.tagext.*;

public class MetaIfValueTag extends TagSupport
{
  private static final Logger log = LoggerFactory.getLogger(MetaIfValueTag.class);

  private String mName = null;
  private String mField = null;
  private String mValue = null;
  private String mIf = null;

  public String getName() { return mName; }
  public void setName( String name ) { mName = name; }

  public String getField() { return mField; }
  public void setField( String field ) { mField = field; }

  public String getValue() { return mValue; }
  public void setValue( String value ) { mValue = value; }

  public String getIf() { return mIf; }
  public void setIf( String iff ) { mIf = iff; }

  public int doStartTag()
    throws JspException
  {
    try
    {
      String id = getName();
      String field = getField();
      String value = getValue();

      if ( id == null )
      {
        log.error( "(doStartTag) The 'name' attribute cannot be null!" );
        return SKIP_BODY;
      }

      if ( field == null )
      {
        log.error( "(doStartTag) The 'field' attribute cannot be null!" );
        return SKIP_BODY;
      }

      if ( value == null )
      {
        log.error( "(doStartTag) The 'value' attribute cannot be null!" );
        return SKIP_BODY;
      }

      boolean iff = false;
      if ( getIf() == null || getIf().equals( "true" ))
        iff = true;

      Object o = pageContext.getRequest().getAttribute( id );
      if ( o == null )
        return Tag.SKIP_BODY;
        // throw new JspException( "No object with id [" + id + "] was found" );

      MetaObject mc = MetaDataUtil.findMetaObject( o, this );
      if ( mc == null )
      {
          log.error( "Cannot find MetaClass for object [" + o + "]" );
          return Tag.SKIP_BODY;
      }

      MetaField f = mc.getMetaField( field );
      if ( f == null )
      {
          log.error( "Cannot find MetaField for MetaClass [" + mc + "] with name [" + field + "]" );
          return Tag.SKIP_BODY;
      }

      boolean equals = false;

      // Just compare the strings to see if they are equal
      String ov = "" + f.getString( o );
      if ( value.equals( ov ))
        equals = true;

      // Return the correct include
      if ( equals && iff )
        return Tag.EVAL_BODY_INCLUDE;
      else if ( !equals && !iff )
        return Tag.EVAL_BODY_INCLUDE;
    }
    catch( Exception e )
    {
      log.error( "Error processing If Value Tag", e );
      throw new JspException( "Error processing If Value Tag: " + e );
    }

    return Tag.SKIP_BODY;
  }
}
