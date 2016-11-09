/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.field;

import com.draagon.meta.attr.AttributeDef;
import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.validator.MetaValidator;
import com.draagon.meta.validator.MetaValidatorNotFoundException;
import com.draagon.meta.ValueException;
import com.draagon.meta.view.MetaView;
import com.draagon.meta.view.MetaViewNotFoundException;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.attr.MetaAttributeNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.draagon.meta.util.Converter;
import java.util.List;

/**
 * A MetaField represents a field of an object and is contained within a MetaClass.
 * It functions as both a proxy to get/set data within an object and also handles
 * accessing meta data about a field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
public abstract class MetaField extends MetaData
  implements MetaFieldTypes
{
    //private static Log log = LogFactory.getLog( MetaField.class );

    public final static String ATTR_LEN           = "len";
    public final static String ATTR_VALIDATION    = "validation";
    public final static String ATTR_DEFAULT_VIEW  = "defaultView";
    public final static String ATTR_DEF_VAL  = "defVal";

    //private int mType = 0;
    private Object mDefaultValue = null;
    private int mLength = -1;

    public MetaField( String name ) {
        super( name );
        addAttributeDef( new AttributeDef( ATTR_LEN, String.class, false, "Length of the field" ));
        addAttributeDef( new AttributeDef( ATTR_VALIDATION, String.class, false, "Comma delimited list of validators" ));
        addAttributeDef( new AttributeDef( ATTR_DEF_VAL, String.class, false, "Default value for the MetaField" ));
    }
    
    /**
     * Gets the primary MetaData class
     */
    @Override
    public final Class<MetaField> getMetaDataClass() {
      return MetaField.class;
    }

    /**
     * Returns the specific MetaClass in which this class is declared.<br>
     * WARNING: This may not return the MetaClass from which this MetaField was retrieved.
     * @return The declaring MetaClass
     */
    public MetaObject getDeclaringObject()
    {
      return (MetaObject) getParent();
    }

    /*protected MetaClass getMetaClass()
    {
      return (MetaClass) getParent();
    }*/

    /**
     * Sets the Super Field
     */
    public void setSuperField( MetaField superField )
    {
      setSuperData( superField );
    }

    /**
     * Gets the Super Field
     */
    public MetaField getSuperField()
    {
      return (MetaField) getSuperData();
    }

    /**
     * Sets the default field value
     */
    public void setDefaultValue( Object defVal )
      //throws MetaException
    {
      if ( !getValueClass().isInstance( defVal ))
      {
        String def = defVal.toString();

        // Massage data if needed
        switch( getType() )
        {
          case BOOLEAN: defVal = Boolean.valueOf( def ); break;
          case BYTE:    defVal = Byte.valueOf( def ); break;
          case SHORT:   defVal = Short.valueOf( def ); break;
          case INT:     defVal = Integer.valueOf( def ); break;
          case LONG:    defVal = Long.valueOf( def ); break;
          case FLOAT:   defVal = Float.valueOf( def ); break;
          case DOUBLE:  defVal = Double.valueOf( def ); break;
          case STRING:  defVal = def; break;
          case DATE:    defVal = new Date( Long.parseLong( def )); break;
          //default:
          //  throw new MetaException( "Default value [" + defVal + "] is not a supported value class" );
        }
      }

      mDefaultValue = defVal;
    }

    /**
     * Gets the default field value
     */
    public Object getDefaultValue()
    {
      return mDefaultValue;
    }

    /**
     * Returns the type of value
     */
    public abstract int getType();
    /*{
      return mType;
    }*/

    /**
     * Gets the type of value object class returned
     */
    public abstract Class<?> getValueClass();
    /*{
      switch( getType() )
      {
        case BOOLEAN: return Boolean.class;
        case BYTE:    return Byte.class;
        case SHORT:   return Short.class;
        case INT:     return Integer.class;
        case LONG:    return Long.class;
        case FLOAT:   return Float.class;
        case DOUBLE:  return Double.class;
        case STRING:  return String.class;
        case DATE:    return Date.class;
        default:  return Object.class;
      }
    }*/

    /**
     * Sets the object attribute represented by this MetaField
     */
    protected void setObjectAttribute( Object obj, Object val )
      //throws MetaException
    {
      // Ensure the data types are accurate
      if ( val != null && !getValueClass().isInstance( val ))
        throw new ValueException( "Invalid value [" + val + "], expected class [" + getValueClass().getName() + "]" );

      // Perform validation
      //performValidation( obj, val );

      // Set the value on the object
      getDeclaringObject().setValue( this, obj, val );
    }

    /**
     * Gets the object attribute represented by this MetaField
     */
    protected Object getObjectAttribute( Object obj )
      //throws MetaException
    {
      Object val = getDeclaringObject().getValue( this, obj );
      //if ( !getValueClass().isInstance( val ))
      //  throw new DataException( "Invalid value [" + val + "], expected class [" + getValueClass().getName() + "]" );
      return val;
    }

  ////////////////////////////////////////////////////
  // VIEW METHODS

  /**
   * Whether the named MetaView exists
   */
  public boolean hasView( String name )
  {
    try {
      getView( name );
      return true;
    }
    catch( MetaViewNotFoundException e ) {
      return false;
    }
  }

  public void addMetaView( MetaView view )
    //throws InvalidMetaDataException
  {
     addChild( view );
  }

  public Collection<MetaView> getViews()
  {
    return getChildren( MetaView.class, true );
  }

  public MetaView getDefaultView()
    //throws MetaViewNotFoundException
  {
    if ( hasAttribute( ATTR_DEFAULT_VIEW ))
      return getView( (String) getAttribute( ATTR_DEFAULT_VIEW ));
    else
      return getFirstChild( MetaView.class );
  }

  public MetaView getView( String name )
    //throws MetaViewNotFoundException
  {
    try {
      return (MetaView) getChild( name, MetaView.class );
    }
    catch( MetaDataNotFoundException e ) {
      throw new MetaViewNotFoundException( "MetaView with name [" + name + "] not found in MetaField [" + toString() + "]", name );
    }
  }

  ////////////////////////////////////////////////////
  // VALIDATOR METHODS

  protected void performValidation( Object obj, Object val )
    //throws ValueException
  {
    // Run any defined validators
    try {
      String list = (String) getAttribute( ATTR_VALIDATION );
      for( MetaValidator v : getValidatorList( list ))
      {
        v.validate( obj, val );
      }
    }
    catch( MetaAttributeNotFoundException e ) {}
  }

  /**
   * Whether the named MetaValidator exists
   */
  public boolean hasValidator( String name )
  {
    try {
      getValidator( name );
      return true;
    }
    catch( MetaValidatorNotFoundException e ) {
      return false;
    }
  }

  public void addMetaValidator( MetaValidator validator )
    //throws InvalidMetaDataException
  {
     addChild( validator );
  }

  public Collection<?> getValidators()
  {
    return getChildren( MetaValidator.class, true );
  }

  /**
   * This method returns the list of validators based on the
   * comma delimited string name provided
   */
  public Collection<MetaValidator> getValidatorList( String list )
    //throws MetaValidatorNotFoundException
  {
    ArrayList<MetaValidator> validators = new ArrayList<MetaValidator>();

    while ( list != null ) {

      String validator = null;

      int i = list.indexOf( ',' );
      if ( i >= 0 ) {
        validator = list.substring( 0, i ).trim();
        list = list.substring( i + 1 );
      }
      else {
        validator = list.trim();
        list = null;
      }

      if ( validator.length() > 0 )
        validators.add( getValidator( validator ));
    }

    return validators;
  }

  /*public MetaValidator getDefaultValidator()
    throws MetaValidatorNotFoundException
  {
    return (MetaValidator) getFirstChild( MetaValidator.class );
  }*/

  public MetaValidator getValidator( String name )
    //throws MetaValidatorNotFoundException
  {
    try {
      return (MetaValidator) getChild( name, MetaValidator.class );
    }
    catch( MetaDataNotFoundException e ) {
      throw new MetaValidatorNotFoundException( "MetaValidator with name [" + name + "] not found in MetaField [" + toString() + "]", name );
    }
  }

  ////////////////////////////////////////////////////
  // OBJECT SETTER METHODS

  public void setBoolean( Object obj, Boolean boolval )
    //throws MetaException
  {
     Object bv = boolval;

     if ( boolval != null )
     {
    	 bv = Converter.toType( getType(), boolval );
    }

    setObjectAttribute( obj, bv );
  }

  public void setByte( Object obj, Byte byteval )
    //throws MetaException
  {
     Object bv = byteval;

     if ( byteval != null )
     {
    	 bv = Converter.toType( getType(), byteval );
    }

    setObjectAttribute( obj, bv );
  }

  public void setShort( Object obj, Short shortval )
    //throws MetaException
  {
     Object sv = shortval;

     if ( shortval != null )
     {
    	 sv = Converter.toType( getType(), shortval );
    }

    setObjectAttribute( obj, sv );
  }

  public void setInt( Object obj, Integer intval )
    //throws MetaException
  {
     Object iv = intval;

     if ( intval != null )
     {
    	 iv = Converter.toType( getType(), intval );
    }

    setObjectAttribute( obj, iv );
  }

  public void setLong( Object obj, Long longval )
    //throws MetaException
  {
     Object lv = longval;

     if ( longval != null )
     {
    	 lv = Converter.toType( getType(), longval );
    }

    setObjectAttribute( obj, lv );
  }

  public void setFloat( Object obj, Float floatval )
    //throws MetaException
  {
     Object fv = floatval;

     if ( floatval != null )
     {
    	 fv = Converter.toType( getType(), floatval );
    }

    setObjectAttribute( obj, fv );
  }

  public void setDouble( Object obj, Double doubval )
    //throws MetaException
  {
     Object dv = doubval;

     if ( doubval != null )
     {
    	dv = Converter.toType( getType(), doubval );
    }

    setObjectAttribute( obj, dv );
  }

  public void setString( Object obj, String strval )
    //throws MetaException
  {
     Object s = strval;

     // If an empty string, convert to a null
     if ( strval != null && strval.trim().length() == 0
        && getType() < OBJECT && getType() != STRING )
      strval = null;

     // Handle the values
     if ( strval != null )
     {
    	 s = Converter.toType( getType(), strval );
     }

     setObjectAttribute( obj, s );
  }

  public void setDate( Object obj, Date dateval )
    //throws MetaException
  {
     Object lv = dateval;

     if ( dateval != null )
     {
    	 lv = Converter.toType( getType(), dateval );
    }

    setObjectAttribute( obj, lv );
  }

  public void setObject( Object obj, Object objval )
    //throws MetaException
  {
    Object o = objval;

    if ( objval != null )
    {
   	 o = Converter.toType( getType(), objval );
    }

    setObjectAttribute( obj, o );
  }


  ////////////////////////////////////////////////////
  // OBJECT GETTER METHODS


  public Boolean getBoolean( Object obj )
    //throws MetaException
  {
   return Converter.toBoolean( getObjectAttribute( obj ));
  }

  public Byte getByte( Object obj )
    //throws MetaException
  {
    return Converter.toByte( getObjectAttribute( obj ));
  }

  public Short getShort( Object obj )
    //throws MetaException
  {
    return Converter.toShort(getObjectAttribute( obj ));
  }

  public Integer getInt( Object obj )
    //throws MetaException
  {
    return Converter.toInt( getObjectAttribute( obj ));
  }

  public Long getLong( Object obj )
    //throws MetaException
  {
    return Converter.toLong( getObjectAttribute( obj ));
  }

  public Float getFloat( Object obj )
    //throws MetaException
  {
    return Converter.toFloat( getObjectAttribute( obj ));
  }

  public Double getDouble( Object obj )
    //throws MetaException
  {
    return Converter.toDouble( getObjectAttribute( obj ));
  }

  public String getString( Object obj )
    //throws MetaException
  {
    return Converter.toString( getObjectAttribute( obj ));
  }

  public Date getDate( Object obj )
    //throws MetaException
  {
    return Converter.toDate( getObjectAttribute( obj ));
  }

  public Object getObject( Object obj )
    //throws MetaException
  {
    return getObjectAttribute( obj );
  }

  ////////////////////////////////////////////////////
  // MISC METHODS

  // WARNING:  Where should this really go?
  public int getLength()
  {
    if ( mLength >= 0 ) return mLength;

    int length = 0;

    if ( getSuperField() != null )
      length = getSuperField().getLength();

    try {
      length = Integer.parseInt( (String) getAttribute( ATTR_LEN ));
    } catch( Exception e ) {}

    if ( length <= 0 )
      switch( getType() )
      {
        case MetaField.BOOLEAN: length = 1; break;
        case MetaField.BYTE: length = 4; break;
        case MetaField.SHORT: length = 6; break;
        case MetaField.INT: length = 10; break;
        case MetaField.LONG: length = 15; break;
        case MetaField.FLOAT: length = 12; break;
        case MetaField.DOUBLE: length = 16; break;
        case MetaField.STRING: length = 50; break;
        case MetaField.DATE: length = 15; break;
        default:  length = 10; break;
      }

    mLength = length;
    return mLength;
  }

  public Object clone()
  {
    MetaField mf = (MetaField) super.clone();
    mf.mDefaultValue = mDefaultValue;
    return mf;
  }
}
