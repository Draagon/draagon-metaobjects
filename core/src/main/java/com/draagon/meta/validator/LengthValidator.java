/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.draagon.meta.validator;

import com.draagon.meta.*;
import com.draagon.meta.attr.AttributeDef;
import org.apache.commons.validator.GenericValidator;

@SuppressWarnings("serial")
public class LengthValidator extends MetaValidator
{
  //private static Log log = LogFactory.getLog( LengthValidator.class );

  /** Minimum length attribute */
  public final static String ATTR_MIN = "min";
  /** Maximum length attribute */
  public final static String ATTR_MAX = "max";

  public LengthValidator(String type, String subtype, String name ) {
    super( type, subtype, name );
    addAttributeDef( new AttributeDef( ATTR_MIN, String.class, false, "Minimum length (0 default)" ));
    addAttributeDef( new AttributeDef( ATTR_MAX, String.class, false, "Maximum length (field length default)" ));
  }

  /**
   * Validates the value of the field in the specified object
   */
  public void validate( Object object, Object value )
    //throws MetaException
  {
    int min = 0;
    int max = getMetaField( object ).getLength();

    if ( hasAttribute( ATTR_MIN ))
      min = Integer.parseInt( (String) getAttribute( ATTR_MIN ));
    if ( hasAttribute( ATTR_MAX ))
      max = Integer.parseInt( (String) getAttribute( ATTR_MAX ));

    String msg = getMessage( "A valid length between " + min + " and " + max + " must be entered" );
    String val = (value==null)?null:value.toString();

    if ( !GenericValidator.isBlankOrNull( val )
      && ( val.length() < min || val.length() > max )) {
      throw new InvalidValueException( msg );
    }
  }
}
