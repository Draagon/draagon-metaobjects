/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.draagon.meta.validator;

import com.draagon.meta.attr.AttributeDef;
import java.util.ArrayList;
import java.util.Collection;

import com.draagon.meta.*;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

import org.apache.commons.validator.GenericValidator;

@SuppressWarnings("serial")
public class RequiredValidator extends MetaValidator
{
  //private static Log log = LogFactory.getLog( RequiredValidator.class );

  public RequiredValidator(String name ) {
    super(name);
  }

  /**
   * Validates the value of the field in the specified object
   */
  public void validate( Object object, Object value )
    //throws MetaException
  {
    String msg = getMessage( "A value is required" );
    String val = (value==null)?null:value.toString();

    if ( GenericValidator.isBlankOrNull( val )) {
      throw new InvalidValueException( msg );
    }
  }
}
