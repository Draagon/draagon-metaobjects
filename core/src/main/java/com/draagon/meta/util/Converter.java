package com.draagon.meta.util;

import com.draagon.meta.field.MetaFieldTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Util class to convert from one data type to another
 * 
 * @author      Douglas Mealing
 *
 * @deprecated Use DataConverter instead
 */
public final class Converter 
{        
	/**
	 * Private constructor
	 */
	private Converter()
	{
	}

	/** Converts the object to the specified MetaFieldType */
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
			case MetaFieldTypes.OBJECT: return toObject( val );

			case MetaFieldTypes.BOOLEAN_ARRAY: //return toBooleanArray( val );
			case MetaFieldTypes.BYTE_ARRAY://return toByteArray( val );
			case MetaFieldTypes.SHORT_ARRAY: //return toShortArray( val );
			case MetaFieldTypes.INT_ARRAY: //return toIntArray( val );
			case MetaFieldTypes.LONG_ARRAY: //return toLongArray( val );
			case MetaFieldTypes.FLOAT_ARRAY: //return toFloatArray( val );
			case MetaFieldTypes.DOUBLE_ARRAY: //return toDoubleArray( val );
			case MetaFieldTypes.DATE_ARRAY: //toDateArray( val );

			case MetaFieldTypes.CLOB: //return toClob( val );
			case MetaFieldTypes.BLOB: //return toBlob( val );
			case MetaFieldTypes.XML: //return toXml( val );
			case MetaFieldTypes.JSON: //return toJson( val );
			case MetaFieldTypes.HTML5: //return toHtml5( val );
				return unsupported(type,val);

			case MetaFieldTypes.STRING_ARRAY: return toStringArray( val );
			case MetaFieldTypes.OBJECT_ARRAY: return toObjectArray( val );

			case MetaFieldTypes.CUSTOM: throw new IllegalStateException( "Cannot convert to a custom type, value passed: [" + val + "]" );

			default: throw new IllegalStateException( "Unknown type (" + type + "), cannot convert object [" + val + "]" );
		}
	}

	/** Convert to an Object, if a list returns null if empty, or object if length of 1, otherwise an exception */
	public static List<String> toStringArray( Object val ) {
		return DataConverter.toStringArray( val );
	}

	/** Convert to an Object, if a list returns null if empty, or object if length of 1, otherwise an exception */
	public static List<Object> toObjectArray( Object val ) {
		return DataConverter.toObjectArray( val );
	}

	protected static Object unsupported( int type, Object val ) {
		if ( val == null ) return null;
		throw new UnsupportedOperationException( "Cannot currently support converting an Object ["+val+"] to type ("+type+")" );
	}

	/** Convert to an Object, if a list returns null if empty, or object if length of 1, otherwise an exception */
	public static Object toObject( Object val ) {
		return DataConverter.toObject( val );
	}
	
	/**
	 * Converts the object value to double value
	 * 
	 * @param val Value
	 * @return Double value
	 */
	public static Boolean toBoolean( Object val )
	{
	    return DataConverter.toBoolean( val );
	} // toDouble
	
	/**
	 * Converts the object value to double value
	 * 
	 * @param val Value
	 * @return Double value
	 */
	public static Double toDouble( Object val )
	{
	    return DataConverter.toDouble( val );
	} // toDouble
	
	/**
	 * Converts the object value to Float value
	 * 
	 * @param val Value
	 * @return Float value
	 */
	public static Float toFloat( Object val )
	{
	    return DataConverter.toFloat( val );
	} // toFloat
	
	/**
	 * Converts the object value to Long value
	 * 
	 * @param val Value
 	 * @return Long value
	 */
	public static Long toLong( Object val )
	{
	    return DataConverter.toLong( val );
	} // toLong
	
	/**
	 * Converts the object value to Integer value
	 * 
	 * @param val Value
	 * @return Integer Value
	 */
	public static Integer toInt( Object val )
	{
	    return DataConverter.toInt( val );
	} // toInt
	
	/**
	 * Convert the object value to short value
	 * 
	 * @param val Value
	 * @return Short value
	 */
	public static Short toShort( Object val )
	{
	    return DataConverter.toShort( val );
	} // toShort
	
	/**
	 * Convert the object value to byte value
	 * 
	 * @param val Value
	 * @return Short value
	 */
	public static Byte toByte( Object val )
	{
	    return DataConverter.toByte( val );
	} // toByte
	
	/**
	 * Convert the object value to byte value
	 * 
	 * @param val Value
	 * @return Short value
	 */
	public static String toString( Object val )
	{
		return DataConverter.toString( val );
	} // toString
	
	/**
	 * Convert the object value to Date value
	 * 
	 * @param val Value
	 * @return Short value
	 */
	public static Date toDate( Object val )
	{
	    return DataConverter.toDate( val );
	} // toDate
	
} // Converter
