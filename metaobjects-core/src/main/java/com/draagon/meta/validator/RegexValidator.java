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

import org.apache.commons.validator.GenericValidator;

@SuppressWarnings("serial")
public class RegexValidator extends MetaValidator
{
  //private static Log log = LogFactory.getLog( RegexValidator.class );

  /** Mask attribute */
  public final static String ATTR_MASK = "mask";

  public RegexValidator(String name ) {
    super(name);
    addAttributeDef( new AttributeDef( ATTR_MASK, String.class, true, "Validation mask" ));
  }

  /**
   * Validates the value of the field in the specified object
   */
  public void validate( Object object, Object value )
    //throws MetaException
  {
    String mask = (String) getAttribute( ATTR_MASK );
    String msg = getMessage( "Invalid value format" );

    String val = (value==null)?null:value.toString();

    if ( !GenericValidator.isBlankOrNull( val )
        && !GenericValidator.matchRegexp( val, mask )) {
      throw new InvalidValueException( msg );
    }
  }
}
