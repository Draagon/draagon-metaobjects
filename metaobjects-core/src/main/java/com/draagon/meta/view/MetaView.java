/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.draagon.meta.view;

import com.draagon.meta.attr.AttributeDef;
import com.draagon.meta.MetaData;
import com.draagon.meta.MetaException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.validator.MetaValidator;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.attr.MetaAttributeNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public abstract class MetaView extends MetaData
{
  //private static Log log = LogFactory.getLog( MetaView.class );

  public final static int READ = 0;
  public final static int EDIT = 1;
  public final static int HIDE = 2;

  public final static String ATTR_VALIDATION = "validation";

  public MetaView( String name )
  {
      super( name );
      addAttributeDef( new AttributeDef( ATTR_VALIDATION, String.class, false, "Comma delimited list of validators" ));
  }

  /**
   * Gets the primary MetaData class
   */
  public final Class<MetaView> getMetaDataClass()
  {
    return MetaView.class;
  }

  //public void attachMetaField( MetaField field )
  //{
  //  attachParent( field );
  //}

  public MetaField getDeclaringMetaField()
  {
    return (MetaField) getParent();
  }

  /**
   * Sets the Super View
   */
  public void setSuperView( MetaView superView )
  {
    setSuperData( superView );
  }

  /**
   * Gets the Super Field
   */
  protected MetaView getSuperView()
  {
    return (MetaView) getSuperData();
  }

  /**
   * Retrieves the MetaField for this view associated
   * with the specified object.
   * @param obj
   * @return
   */
  public MetaField getMetaField( Object obj )
  {
    MetaObject mc = MetaDataLoader.findMetaObject( obj );
    return mc.getMetaField( getParent().getName() );
  }

  /**
   * Retrieves the display string of the field for a simple display
   */
  public String getDisplayString( Object obj )
    //throws MetaException
  {
    MetaObject mc = MetaDataLoader.findMetaObject( obj );
    MetaField mf = mc.getMetaField( getParent().getName() );
    return "" + mf.getString( obj );
  }
  
  /**
   * Performs validation before setting the value
   */
  protected void performValidation( Object obj, Object val )
    throws MetaException
  {
    // Run any defined validators
    try {
      String list = (String) getAttribute( ATTR_VALIDATION );

      Collection<MetaValidator> val_list = getMetaField( obj ).getValidatorList( list );

      for( Iterator<MetaValidator> i = val_list.iterator(); i.hasNext(); )
      {
        MetaValidator v = (MetaValidator) i.next();
        v.validate( obj, val );
      }
    }
    catch( MetaAttributeNotFoundException e ) {}
  }
}
