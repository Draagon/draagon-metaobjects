/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.draagon.meta.validator;

import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttributeNotFoundException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataRegistry;
import com.draagon.meta.object.MetaObject;

public abstract class MetaValidator extends MetaData
{
  //private static Log log = LogFactory.getLog( MetaValidator.class );

  public MetaValidator( String type, String subtype, String name ) {
    super( type, subtype, name );
  }

  /**
   * Gets the primary MetaData class
   */
  public final Class<MetaValidator> getMetaDataClass()
  {
    return MetaValidator.class;
  }

  /**
   * Gets the declaring meta field.<br>
   * NOTE: This may not be the MetaField from which the view
   * was retrieved, so be careful!
   */
  public MetaField getDeclaringMetaField()
  {
    return (MetaField) getParent();
  }

  /**
   * Retrieves the MetaField for this view associated
   * with the specified object.
   */
  public MetaField getMetaField( Object obj )
  {
    MetaObject mc = MetaDataRegistry.findMetaObject( obj );
    return mc.getMetaField( getParent().getName() );
  }

  /**
   * Sets the Super Validator
   */
  public void setSuperValidator( MetaValidator superValidator )
  {
    setSuperData( superValidator );
  }

  /**
   * Gets the Super Validator
   */
  protected MetaValidator getSuperValidator()
  {
    return (MetaValidator) getSuperData();
  }

  /////////////////////////////////////////////////////////////
  // VALIDATION METHODS

  /**
   * Validates the value of the field in the specified object
   */
  public abstract void validate( Object object, Object value );

  /////////////////////////////////////////////////////////////
  // HELPER METHODS

  /**
   * Retrieves the message to use for displaying errors
   */
  public String getMessage( String defMsg )
  {
    String msg = defMsg;
    try {
      msg = (String) getAttribute( "msg" );
    }
    catch( MetaAttributeNotFoundException e ) {}
    return msg;
  }
}
