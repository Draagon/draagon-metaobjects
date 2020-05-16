/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.draagon.meta.view;

import com.draagon.meta.InvalidMetaDataException;
import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.MetaAttributeNotFoundException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataRegistry;
import com.draagon.meta.object.MetaObject;

public abstract class MetaView extends MetaData<MetaView>
{
  //private static Log log = LogFactory.getLog( MetaView.class );

  public final static int READ = 0;
  public final static int EDIT = 1;
  public final static int HIDE = 2;

  public final static String TYPE_VIEW = "view";

  public final static String ATTR_VALIDATION = "validation";

  public MetaView(  String subtype, String name ) {
    super( TYPE_VIEW, subtype, name );
    //addAttributeDef( new AttributeDef( ATTR_VALIDATION, String.class, false, "Comma delimited list of validators" ));
  }

  /**
   * Gets the primary MetaData class
   */
  public final Class<MetaView> getMetaDataClass()  {
    return MetaView.class;
  }

  /** Add Child to the MetaView */
  public MetaView addChild(MetaData data) throws InvalidMetaDataException {
    return super.addChild( data );
  }

  /** Wrap the MetaView */
  public MetaView overload() {
    return super.overload();
  }

  /**
   * Sets an attribute of the MetaClass
   */
  public MetaView addMetaAttr(MetaAttribute attr) {
    return addChild(attr);
  }

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
  public MetaField<?> getMetaField( Object obj )
  {
    MetaObject mc = MetaDataRegistry.findMetaObject( obj );
    return mc.getMetaField( getParent().getName() );
  }

  /**
   * Retrieves the display string of the field for a simple display
   */
  public String getDisplayString( Object obj ) {

    MetaObject mc = getLoader().getMetaObjectFor( obj );
    if ( mc == null ) MetaDataRegistry.findMetaObject( obj );
    MetaField mf = mc.getMetaField( getParent().getName() );
    return "" + mf.getString( obj );
  }
  
  /**
   * Performs validation before setting the value
   */
  protected void performValidation( Object obj, Object val )
    throws MetaDataException
  {
    // Run any defined validators
    try {
      String list = getMetaAttr( ATTR_VALIDATION ).getValueAsString();

      getMetaField( obj ).getValidatorList( list ).forEach( v->v.validate( obj, val ));
    }
    catch( MetaAttributeNotFoundException ignored ) {}
  }
}
