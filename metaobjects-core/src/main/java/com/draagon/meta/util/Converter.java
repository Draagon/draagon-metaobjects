package com.draagon.meta.util;

import java.util.Date;

import com.draagon.meta.field.MetaFieldTypes;

/**
 * Util class to convert from one data type to another
 * 
 * @author      Douglas Mealing
 */
public final class Converter 
{        
	/**
	 * Private constructor
	 */
	private Converter()
	{
	} // Converter
	
	public static Object toType( int type, Object val ) {
		
		switch( type ) {
		case MetaFieldTypes.BOOLEAN: return toBoolean( val );
		case MetaFieldTypes.BYTE: return toByte( val );
		case MetaFieldTypes.SHORT: return toShort( val );
		case MetaFieldTypes.INT: return toInt( val );
		case MetaFieldTypes.LONG: return toLong( val );
		case MetaFieldTypes.FLOAT: return toFloat( val );
		case MetaFieldTypes.DOUBLE: return toDouble( val );
		case MetaFieldTypes.STRING: return toString( val );
		case MetaFieldTypes.DATE: return toDate( val );
		case MetaFieldTypes.OBJECT: return val;		
		}
		
		return val;
	}
	
	/**
	 * Converts the object value to double value
	 * 
	 * @param val Value
	 * @return Double value
	 */
	public static Boolean toBoolean( Object val )
	{
	    if ( val == null ) return null;
	
	    if ( val instanceof Boolean ) {
	        return (Boolean) val;
	      }
	      else if ( val instanceof Byte ) {
	        if ( ((Byte) val ).byteValue() != 0 ) return new Boolean( true );
	      }
	      else if ( val instanceof Short ) {
	        if ( ((Short) val ).shortValue() != 0 ) return new Boolean( true );
	      }
	      else if ( val instanceof Integer ) {
	        if ( ((Integer) val ).intValue() != 0 ) return new Boolean( true );
	      }
	      else if ( val instanceof Long ) {
	        if ( ((Long) val ).longValue() != 0 ) return new Boolean( true );
	      }
	      else if ( val instanceof Float ) {
	        if ( ((Float) val ).floatValue() != 0 ) return new Boolean( true );
	      }
	      else if ( val instanceof Double ) {
	        if ( ((Double) val ).doubleValue() != 0 ) return new Boolean( true );
	      }
	      else if ( val instanceof String ) {
	        if ( val.toString().length() > 0 && ( val.toString().charAt( 0 ) == 't'
	            || val.toString().charAt( 0 ) == 'T' )) return new Boolean( true );
	      }
	      else if ( val instanceof Date ) {
	              if ( ((Date) val ).getTime() == 0 ) return new Boolean( false );
	              else return new Boolean( true );
	      }
	      else if ( val instanceof Object ) {
	        if ( val.toString().length() > 0 && ( val.toString().charAt( 0 ) == 't'
	            || val.toString().charAt( 0 ) == 'T' )) return new Boolean( true );
	      }

	      return new Boolean( false );
	} // toDouble
	
	/**
	 * Converts the object value to double value
	 * 
	 * @param val Value
	 * @return Double value
	 */
	public static Double toDouble( Object val )
	{
	    if ( val == null ) return null;
	
	    if ( val instanceof Boolean ) {
	      if ( ((Boolean) val ).booleanValue() ) return new Double( 1.0 );
	    }
	    else if ( val instanceof Byte ) {
	      return new Double( (double) ((Byte) val ).byteValue() );
	    }
	    else if ( val instanceof Short ) {
	      return new Double( (double) ((Short) val ).shortValue() );
	    }
	    else if ( val instanceof Integer ) {
	      return new Double( (double) ((Integer) val ).intValue() );
	    }
	    else if ( val instanceof Long ) {
	      return new Double( (double) ((Long) val ).longValue() );
	    }
	    else if ( val instanceof Float ) {
	      return new Double( (double) ((Float) val ).floatValue() );
	    }
	    else if ( val instanceof Double ) {
	      return (Double) val;
	    }
	    else if ( val instanceof String ) {
	      try { return new Double( Double.parseDouble( (String) val )); } catch( Exception e ) {}
	    }
	    else if ( val instanceof Date ) {
	      return new Double( (double) ((Date) val ).getTime() );
	    }
	    else if ( val instanceof Object ) {
	      try { return new Double( Double.parseDouble( val.toString() )); } catch( Exception e ) {}
	    }
	
	    return new Double( 0.0 );
	} // toDouble
	
	/**
	 * Converts the object value to Float value
	 * 
	 * @param val Value
	 * @return Float value
	 */
	public static Float toFloat( Object val )
	{
	    if ( val == null ) return null;
	
	    if ( val instanceof Boolean ) {
	      if ( ((Boolean) val ).booleanValue() ) return new Float( 1 );
	    }
	    else if ( val instanceof Byte ) {
	      return new Float( (float) ((Byte) val ).byteValue() );
	    }
	    else if ( val instanceof Short ) {
	      return new Float( (float) ((Short) val ).shortValue() );
	    }
	    else if ( val instanceof Integer ) {
	      return new Float( (float) ((Integer) val ).intValue() );
	    }
	    else if ( val instanceof Long ) {
	      return new Float( (float) ((Long) val ).longValue() );
	    }
	    else if ( val instanceof Float ) {
	      return (Float) val;
	    }
	    else if ( val instanceof Double ) {
	      return new Float( (float) ((Double) val ).doubleValue() );
	    }
	    else if ( val instanceof String ) {
	      try { return new Float( Float.parseFloat( (String) val )); } catch( Exception e ) {}
	    }
	    else if ( val instanceof Date ) {
	      return new Float( (float) ((Date) val ).getTime() );
	    }
	    else if ( val instanceof Object ) {
	      try { return new Float( Float.parseFloat( val.toString() )); } catch( Exception e ) {}
	    }
	
	    return new Float( 0 );
	} // toFloat
	
	/**
	 * Converts the object value to Long value
	 * 
	 * @param val Value
 	 * @return Long value
	 */
	public static Long toLong( Object val )
	{
	    if ( val == null ) return null;
	
	    if ( val instanceof Boolean ) {
	      if ( ((Boolean) val ).booleanValue() ) return new Long( 1 );
	    }
	    else if ( val instanceof Byte ) {
	      return new Long( (long) ((Byte) val ).byteValue() );
	    }
	    else if ( val instanceof Short ) {
	      return new Long( (long) ((Short) val ).shortValue() );
	    }
	    else if ( val instanceof Integer ) {
	      return new Long( (long) ((Integer) val ).intValue() );
	    }
	    else if ( val instanceof Long ) {
	      return (Long) val;
	    }
	    else if ( val instanceof Float ) {
	      return new Long( (long) ((Float) val ).floatValue() );
	    }
	    else if ( val instanceof Double ) {
	      return new Long( (long) ((Double) val ).doubleValue() );
	    }
	    else if ( val instanceof String ) {
	      try { return new Long( Long.parseLong( (String) val )); } catch( Exception e ) {}
	    }
	    else if ( val instanceof Date ) {
	      return new Long( ((Date) val ).getTime() );
	    }
	    else if ( val instanceof Object ) {
	      try { return new Long( Long.parseLong( val.toString() )); } catch( Exception e ) {}
	    }
	
	    return new Long( 0 );
	} // toLong
	
	/**
	 * Converts the object value to Integer value
	 * 
	 * @param val Value
	 * @return Integer Value
	 */
	public static Integer toInt( Object val )
	{
	    if ( val == null ) return null;
	
	    if ( val instanceof Boolean ) {
	      if ( ((Boolean) val ).booleanValue() ) return new Integer( 1 );
	    }
	    else if ( val instanceof Byte ) {
	      return new Integer( (int) ((Byte) val ).byteValue() );
	    }
	    else if ( val instanceof Short ) {
	      return new Integer( (int) ((Short) val ).shortValue() );
	    }
	    else if ( val instanceof Integer ) {
	      return (Integer) val;
	    }
	    else if ( val instanceof Long ) {
	      return new Integer( (int) ((Long) val ).longValue() );
	    }
	    else if ( val instanceof Float ) {
	      return new Integer( (int) ((Float) val ).floatValue() );
	    }
	    else if ( val instanceof Double ) {
	      return new Integer( (int) ((Double) val ).doubleValue() );
	    }
	    else if ( val instanceof String ) {
	      try { return new Integer( Integer.parseInt( (String) val )); } catch( Exception e ) {}
	    }
	    else if ( val instanceof Date ) {
	      return new Integer( (int) ((Date) val ).getTime() );
	    }
	    else if ( val instanceof Object ) {
	      try { return new Integer( Integer.parseInt( val.toString() )); } catch( Exception e ) {}
	    }
	
	    return new Integer( 0 );
	} // toInt
	
	/**
	 * Convert the object value to short value
	 * 
	 * @param val Value
	 * @return Short value
	 */
	public static Short toShort( Object val )
	{
	    if ( val == null ) return null;
	
	    if ( val instanceof Boolean ) {
	      if ( ((Boolean) val ).booleanValue() ) return new Short( (short)1 );
	    }
	    else if ( val instanceof Byte ) {
	      return new Short( (short) ((Byte) val ).shortValue() );
	    }
	    else if ( val instanceof Short ) {
	      return (Short) val;
	    }
	    else if ( val instanceof Integer ) {
	      return new Short( (short) ((Integer) val ).intValue() );
	    }
	    else if ( val instanceof Long ) {
	      return new Short( (short) ((Long) val ).longValue() );
	    }
	    else if ( val instanceof Float ) {
	      return new Short( (short) ((Float) val ).floatValue() );
	    }
	    else if ( val instanceof Double ) {
	      return new Short( (short) ((Double) val ).doubleValue() );
	    }
	    else if ( val instanceof String ) {
	      try { return new Short( Short.parseShort( (String) val )); } catch( Exception e ) {}
	    }
	    else if ( val instanceof Date ) {
	      return new Short( (short) ((Date) val ).getTime() );
	    }
	    else if ( val instanceof Object ) {
	      try { return new Short( Short.parseShort( val.toString() )); } catch( Exception e ) {}
	    }
	
	    return new Short( (short)0 );
	} // toShort
	
	/**
	 * Convert the object value to byte value
	 * 
	 * @param val Value
	 * @return Short value
	 */
	public static Byte toByte( Object val )
	{
	    if ( val == null ) return null;
	
	    if ( val instanceof Boolean ) {
	      if ( ((Boolean) val ).booleanValue() ) return new Byte( (byte)1 );
	    }
	    else if ( val instanceof Byte ) {
	      return (Byte) val;
	    }
	    else if ( val instanceof Short ) {
	      return new Byte( (byte) ((Integer) val ).intValue() );
	    }
	    else if ( val instanceof Integer ) {
	      return new Byte( (byte) ((Integer) val ).intValue() );
	    }
	    else if ( val instanceof Long ) {
	      return new Byte( (byte) ((Long) val ).longValue() );
	    }
	    else if ( val instanceof Float ) {
	      return new Byte( (byte) ((Float) val ).floatValue() );
	    }
	    else if ( val instanceof Double ) {
	      return new Byte( (byte) ((Double) val ).doubleValue() );
	    }
	    else if ( val instanceof String ) {
	      try { return new Byte( Byte.parseByte( (String) val )); } catch( Exception e ) {}
	    }
	    else if ( val instanceof Date ) {
	      return new Byte( (byte) ((Date) val ).getTime() );
	    }
	    else if ( val instanceof Object ) {
	      try { return new Byte( Byte.parseByte( val.toString() )); } catch( Exception e ) {}
	    }
	
	    return new Byte( (byte)0 );
	} // toByte
	
	/**
	 * Convert the object value to byte value
	 * 
	 * @param val Value
	 * @return Short value
	 */
	public static String toString( Object val )
	{
	    if ( val == null ) return null;
	
	    if ( val instanceof Date ) {
	        return "" + ((Date) val ).getTime();
	      }

	      return val.toString();
	} // toString
	
	/**
	 * Convert the object value to Date value
	 * 
	 * @param val Value
	 * @return Short value
	 */
	public static Date toDate( Object val )
	{
	    if ( val == null ) return null;
	
	    if ( val instanceof Boolean ) {
	        if ( ((Boolean) val ).booleanValue() ) return new Date();
	        else return new Date( 0 );
	      }
	      else if ( val instanceof Byte ) {
	        return new Date( (long) ((Byte) val ).byteValue() );
	      }
	      else if ( val instanceof Short ) {
	        return new Date( (long) ((Short) val ).shortValue() );
	      }
	      else if ( val instanceof Integer ) {
	        return new Date( (long) ((Integer) val ).intValue() );
	      }
	      else if ( val instanceof Long ) {
	        return new Date( (long) ((Long) val ).longValue() );
	      }
	      else if ( val instanceof Float ) {
	        return new Date( (long) ((Float) val ).floatValue() );
	      }
	      else if ( val instanceof Double ) {
	        return new Date( (long) ((Double) val ).doubleValue() );
	      }
	      else if ( val instanceof String ) {
	        try { return new Date( Long.parseLong( (String) val )); } catch( Exception e ) {}
	      }
	      else if ( val instanceof Date ) {
	        return (Date) val;
	      }

	          // Catch anything else
	      try { return new Date( Long.parseLong( val.toString() )); }
	      catch( Exception e ) { return new Date( 0 ); }
	} // toDate
	
} // Converter
