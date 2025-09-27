package com.metaobjects.util;

import com.metaobjects.DataTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Util class to convert from one data type to another
 * 
 * @author      Douglas Mealing
 */
public final class DataConverter
{
	private static final Logger log = LoggerFactory.getLogger(DataConverter.class);

	/** Private constructor */
	private DataConverter() {}

	/** Converts the object to the specified MetaFieldType */
	public static Object toType( DataTypes dataType, Object val ) {

		if ( val == null ) return null;
		
		switch( dataType ) {
			case BOOLEAN: return toBoolean( val );
			case BYTE: return toByte( val );
			case SHORT: return toShort( val );
			case INT: return toInt( val );
			case LONG: return toLong( val );
			case FLOAT: return toFloat( val );
			case DOUBLE: return toDouble( val );
			case STRING: return toString( val );
			case DATE: return toDate( val );
			case OBJECT: return toObject( val );

			case BOOLEAN_ARRAY: //return toBooleanArray( val );
			case BYTE_ARRAY://return toByteArray( val );
			case SHORT_ARRAY: //return toShortArray( val );
			case INT_ARRAY: //return toIntArray( val );
			case LONG_ARRAY: //return toLongArray( val );
			case FLOAT_ARRAY: //return toFloatArray( val );
			case DOUBLE_ARRAY: //return toDoubleArray( val );
			case DATE_ARRAY: //toDateArray( val );
				return unsupported(dataType,val);

			case STRING_ARRAY: return toStringArray( val );
			case OBJECT_ARRAY: return toObjectArray( val );

			case CUSTOM: throw new IllegalStateException( "Cannot convert to a custom type, value passed: [" + val + "]" );

			default: throw new IllegalStateException( "Unknown type (" + dataType + "), cannot convert object [" + val + "]" );
		}
	}

	/**
	 * Type-safe conversion with runtime validation - eliminates unsafe casting
	 * @param dataType The target data type
	 * @param val The value to convert
	 * @param expectedType The expected class type for validation
	 * @return The converted value, guaranteed to be of expectedType
	 * @throws ClassCastException if the converted value is not assignable to expectedType
	 */
	public static <T> T toTypeSafe(DataTypes dataType, Object val, Class<T> expectedType) {
		Object result = toType(dataType, val);
		if (result == null) return null;
		
		if (!expectedType.isInstance(result)) {
			throw new ClassCastException("Converted value " + result + 
				" (" + result.getClass().getSimpleName() + ") is not assignable to " + expectedType.getName());
		}
		return expectedType.cast(result);
	}

	/**
	 * Type-safe conversion with Optional return - never throws ClassCastException
	 * @param dataType The target data type
	 * @param val The value to convert
	 * @param expectedType The expected class type for validation
	 * @return Optional containing the converted value if successful and type-compatible
	 */
	public static <T> java.util.Optional<T> toTypeOptional(DataTypes dataType, Object val, Class<T> expectedType) {
		try {
			Object result = toType(dataType, val);
			return result != null && expectedType.isInstance(result) 
				? java.util.Optional.of(expectedType.cast(result)) 
				: java.util.Optional.empty();
		} catch (Exception e) {
			log.debug("Type conversion failed for value {} to {}: {}", val, expectedType.getName(), e.getMessage());
			return java.util.Optional.empty();
		}
	}

	/** Convert to an Object, if a list returns null if empty, or object if length of 1, otherwise an exception */
	public static List<String> toStringArray( Object val ) {

		if ( val == null ) {
			return null;
		}
		else if (val instanceof List<?>) {
			List<?> list = (List<?>) val;
			// Stream-based safe conversion - each element becomes a String
			return list.stream()
				.map(item -> item != null ? item.toString() : null)
				.collect(java.util.stream.Collectors.toList());
		}
		else if ( val instanceof String ) {
			List<String> list = new ArrayList<>();
			if ( !((String) val).isEmpty()) {
				String s= (String) val;
				if (s.contains(",")) {
					Collections.addAll(list, ((String) val).split(","));
				} else {
					list.add( s );
				}
			}
			return list;
		}
		else {
			List<String> l = new ArrayList<String>();
			l.add( toString( val ));
			return l;
		}
	}

	/**
	 * Type-safe string array conversion - eliminates unsafe List&lt;?&gt; to List&lt;String&gt; cast
	 * @param val The value to convert to string array
	 * @return List of strings, with each element safely converted to String
	 */
	public static List<String> toStringArraySafe( Object val ) {

		if ( val == null ) {
			return null;
		}
		else if (val instanceof List<?>) {
			List<?> list = (List<?>) val;
			// Stream-based safe conversion - each element becomes a String
			return list.stream()
				.map(item -> item != null ? item.toString() : null)
				.collect(java.util.stream.Collectors.toList());
		}
		else if ( val instanceof String ) {
			List<String> list = new ArrayList<>();
			if ( !((String) val).isEmpty()) {
				String s= (String) val;
				if (s.contains(",")) {
					Collections.addAll(list, ((String) val).split(","));
				} else {
					list.add( s );
				}
			}
			return list;
		}
		else {
			List<String> l = new ArrayList<String>();
			l.add( toString( val ));
			return l;
		}
	}

	/** Convert to an Object, if a list returns null if empty, or object if length of 1, otherwise an exception */
	public static List<Object> toObjectArray( Object val ) {
		if ( val == null ) {
			return null;
		}
		else if (val instanceof List<?>) {
			List<?> list = (List<?>) val;
			// If it's already a List<Object> or unparameterized List, safe to cast
			// Otherwise create safe copy to avoid ClassCastException
			try {
				@SuppressWarnings("unchecked")
				List<Object> result = (List<Object>) list;
				return result; // Safe cast - preserve reference semantics
			} catch (ClassCastException e) {
				// Unsafe cast - create safe copy
				return new ArrayList<>(list);
			}
		}
		else {
			List<Object> l = new ArrayList<Object>();
			l.add( val );
			return l;
		}
	}

	/**
	 * Type-safe object array conversion - eliminates unsafe List&lt;?&gt; to List&lt;Object&gt; cast
	 * @param val The value to convert to object array
	 * @return List of objects, safely created from input without unsafe casting
	 */
	public static List<Object> toObjectArraySafe( Object val ) {
		if ( val == null ) {
			return null;
		}
		else if (val instanceof List<?>) {
			// Create new ArrayList to avoid unsafe cast - safe but creates copy
			return new ArrayList<>((List<?>) val);
		}
		else {
			List<Object> l = new ArrayList<>();
			l.add( val );
			return l;
		}
	}

	protected static Object unsupported( DataTypes dataType, Object val ) {
		if ( val == null ) return null;
		throw new UnsupportedOperationException( "Cannot currently support converting an Object ["+val+"] to type ("+dataType+")" );
	}

	/** Convert to an Object, if a list returns null if empty, or object if length of 1, otherwise an exception */
	public static Object toObject( Object val ) {
		if (val instanceof List) {
			List<?> l = (List<?>) val;
			if ( l.size() == 0 ) return null;
			else if ( l.size() == 1 ) return l.get(0);
			else throw new IllegalStateException( "Cannot convert List to to Object as it contains more than 1 value" );
		}
		else {
			return val;
		}
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

		if ( val instanceof String && ((String) val).isEmpty()) return null;
	
	    if ( val instanceof Boolean ) {
	        return (Boolean) val;
	      }
	      else if ( val instanceof Byte ) {
			return (Byte) val != 0;
	      }
	      else if ( val instanceof Short ) {
			return (Short) val != 0;
	      }
	      else if ( val instanceof Integer ) {
			return (Integer) val != 0;
	      }
	      else if ( val instanceof Long ) {
			return (Long) val != 0;
	      }
	      else if ( val instanceof Float ) {
			return (Float) val != 0;
	      }
	      else if ( val instanceof Double ) {
			return (Double) val != 0;
	      }
	      else if ( val instanceof Date ) {
			return ((Date) val).getTime() != 0;
	      }
	      else {
			return val.toString().length() > 0 && (val.toString().charAt(0) == 't'
					|| val.toString().charAt(0) == 'T');
	      }

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

		if ( val instanceof String && ((String) val).isEmpty()) return null;
	
	    if ( val instanceof Boolean ) {
	      if ((Boolean) val) return 1.0;
	    }
	    else if ( val instanceof Byte ) {
	      return (double) val;
	    }
	    else if ( val instanceof Short ) {
	      return (double) val;
	    }
	    else if ( val instanceof Integer ) {
	      return (double) val;
	    }
	    else if ( val instanceof Long ) {
	      return (double) val;
	    }
	    else if ( val instanceof Float ) {
	      return (double)  val;
	    }
	    else if ( val instanceof Double ) {
	      return (Double) val;
	    }
	    else if ( val instanceof String ) {
	      try { return Double.parseDouble((String) val); } catch( Exception ignored) {}
	    }
	    else if ( val instanceof Date ) {
	      return (double) ((Date) val).getTime();
	    }
	    else {
	      try { return Double.parseDouble(val.toString()); } catch( Exception ignored) {}
	    }
	
	    return 0.0;
	} // toDouble
	
	/**
	 * Converts the object value to Float value
	 * 
	 * @param val Value
	 * @return Float value
	 */
	public static Float toFloat( Object val )
	{
	    if ( val == null ) {
	    	return null;
		}
		else if ( val instanceof String ) {
			if (((String) val).isEmpty()) return null;
			else return Float.parseFloat((String) val);
		}
	    else if ( val instanceof Boolean ) {
	      	if ((Boolean) val) return 1f;
	    }
	    else if ( val instanceof Byte ) {
	      return (float) (Byte) val;
	    }
	    else if ( val instanceof Short ) {
	      	return (float) (Short) val;
	    }
	    else if ( val instanceof Integer ) {
	      	return (float) (Integer) val;
	    }
	    else if ( val instanceof Long ) {
	      	return (float) (Long) val;
	    }
	    else if ( val instanceof Float ) {
	      	return (float) val;
	    }
	    else if ( val instanceof Double ) {
			return (float) (double) val; // downScaleCheck( "double", "float", (double) val, (double) Float.MIN_VALUE+1f, (double) Float.MAX_VALUE-1f );
	    }
		else if ( val instanceof Date ) {
	     	 return (float) ((Date) val).getTime();
	    }
	    else {
	      	return Float.parseFloat(val.toString());
	    }
	
	    return (float) 0;
	} // toFloat
	
	/**
	 * Converts the object value to Long value
	 * 
	 * @param val Value
 	 * @return Long value
	 */
	public static Long toLong( Object val )
	{
	    if ( val == null ) {
	    	return null;
		}
		else if ( val instanceof String ) {
			if (((String) val).isEmpty()) return null;
			else return Long.parseLong((String) val);
		}
	    else if ( val instanceof Boolean ) {
	      	if ((Boolean) val) return 1L;
	    }
	    else if ( val instanceof Byte ) {
	      	return (long) (Byte) val;
	    }
	    else if ( val instanceof Short ) {
	      	return (long) (Short) val;
	    }
	    else if ( val instanceof Integer ) {
	      	return (long) (Integer) val;
	    }
	    else if ( val instanceof Long ) {
	      	return (long) val;
	    }
	    else if ( val instanceof Float ) {
			return (long) (float) val;
	    }
	    else if ( val instanceof Double ) {
			return (long) downScaleCheck( "double", "long", (double) val, (double) Long.MIN_VALUE, (double) Long.MAX_VALUE );
	    }
	    else if ( val instanceof Date ) {
	      	return ((Date) val).getTime();
	    }
	    else {
	    	return Long.parseLong(val.toString());
	    }
	
	    return 0L;
	} // toLong
	
	/**
	 * Converts the object value to Integer value
	 * 
	 * @param val Value
	 * @return Integer Value
	 */
	public static Integer toInt( Object val )
	{
	    if ( val == null ) {
	    	return null;
		}
		else if ( val instanceof Integer ) {
			return (int) val;
		}
		else if ( val instanceof String ) {
			if (((String) val).isEmpty()) return null;
			else return Integer.parseInt((String)val);
		}
	    else if ( val instanceof Boolean ) {
	      	if ((Boolean) val) return 1;
	    }
	    else if ( val instanceof Byte ) {
	      	return (int) (byte) val;
	    }
	    else if ( val instanceof Short ) {
	      	return (int) (short) val;
	    }
	    else if ( val instanceof Long ) {
			return (int) downScaleCheck( "long", "int", (long) val, (long) Integer.MIN_VALUE, (long) Integer.MAX_VALUE );
	    }
	    else if ( val instanceof Float ) {
			return (int) downScaleCheck( "float", "int", (double) (float) val, (double) Integer.MIN_VALUE, (double) Integer.MAX_VALUE );
	    }
	    else if ( val instanceof Double ) {
			return (int) downScaleCheck( "double", "int", (double) val, (double) Integer.MIN_VALUE, (double) Integer.MAX_VALUE );
		}
	    else if ( val instanceof Date ) {
			return (int) downScaleCheck( "Date", "int", ((Date) val).getTime(), (long) Integer.MIN_VALUE, (long) Integer.MAX_VALUE );
	    }
	    else {
	      	return Integer.parseInt(val.toString());
	    }
	
	    return 0;
	} // toInt

	/**
	 * Convert the object value to short value
	 * 
	 * @param val Value
	 * @return Short value
	 */
	public static Short toShort( Object val )
	{
	    if ( val == null ) {
	    	return null;
		}
		else if ( val instanceof String ) {
			if (((String) val).isEmpty()) return null;
			else return Short.parseShort((String) val);
		}
	    else if ( val instanceof Boolean ) {
	      	if ((Boolean) val) return (short) 1;
	    }
	    else if ( val instanceof Byte ) {
	      	return (short) (byte) val;
	    }
	    else if ( val instanceof Short ) {
	      	return (short) val;
	    }
	    else if ( val instanceof Integer ) {
			return (short) downScaleCheck( "int", "short", (long) (int) val, (long) Short.MIN_VALUE, (long) Short.MAX_VALUE );
	    }
	    else if ( val instanceof Long ) {
			return (short) downScaleCheck( "long", "short", (long) val, (long) Short.MIN_VALUE, (long) Short.MAX_VALUE );
	    }
	    else if ( val instanceof Float ) {
			return (short) downScaleCheck( "float", "short", (double) (float) val, (double) Short.MIN_VALUE, (double) Short.MAX_VALUE );
	    }
	    else if ( val instanceof Double ) {
			return (short) downScaleCheck( "double", "short", (double) val, (double) Short.MIN_VALUE, (double) Short.MAX_VALUE );
	    }
	    else if ( val instanceof Date ) {
			return (short) downScaleCheck( "Date", "short", (long) ((Date) val).getTime(), (long) Short.MIN_VALUE, (long) Short.MAX_VALUE );
	    }
	    else {
	      	return Short.parseShort(val.toString());
	    }
	
	    return (short) 0;
	} // toShort
	
	/**
	 * Convert the object value to byte value
	 * 
	 * @param val Value
	 * @return Short value
	 */
	public static Byte toByte( Object val )
	{
	    if ( val == null ) {
	    	return null;
		}
		else if ( val instanceof Byte ) {
			return (Byte) val;
		}
		else if ( val instanceof String ) {
			if (((String) val).isEmpty()) return null;
			return Byte.parseByte((String) val);
		}
	    else if ( val instanceof Boolean ) {
	      if ((Boolean) val) return (byte) 1;
	    }
	    else if ( val instanceof Short ) {
			return (byte) downScaleCheck( "short", "byte", (long) (short) val, (long) Byte.MIN_VALUE, (long) Byte.MAX_VALUE );
	    }
	    else if ( val instanceof Integer ) {
			return (byte) downScaleCheck( "int", "byte", (long) (int) val, (long) Byte.MIN_VALUE, (long) Byte.MAX_VALUE );
	    }
	    else if ( val instanceof Long ) {
			return (byte) downScaleCheck( "long", "byte", (long) val, (long) Byte.MIN_VALUE, (long) Byte.MAX_VALUE );
	    }
	    else if ( val instanceof Float ) {
			return (byte) downScaleCheck( "float", "byte", (double) (float) val, (double) Byte.MIN_VALUE, (double) Byte.MAX_VALUE );
	    }
	    else if ( val instanceof Double ) {
			return (byte) downScaleCheck( "double", "byte", (double) val, (double) Byte.MIN_VALUE, (double) Byte.MAX_VALUE );
	    }
	    else if ( val instanceof Date ) {
			return (byte) downScaleCheck( "Date", "byte", ((Date) val).getTime(), (long) Byte.MIN_VALUE, (long) Byte.MAX_VALUE );
	    }
	    else {
	      	return Byte.parseByte(val.toString());
	    }
	
	    return (byte) 0;
	} // toByte
	
	/**
	 * Convert the object value to byte value
	 * 
	 * @param val Value
	 * @return Short value
	 */
	public static String toString( Object val )
	{
	    if ( val == null ) {
	    	return null;
		} else if ( val instanceof List<?> ) {
			List<?> list = (List<?>) val;
			// Safe stream-based conversion - eliminates raw List usage
			return list.stream()
				.map(item -> item != null ? item.toString() : "null")
				.collect(java.util.stream.Collectors.joining(","));
		} else if ( val instanceof Date ) {
	        return String.valueOf(((Date) val ).getTime());
		} else if ( val instanceof Class<?> ) {
			return ((Class<?>) val ).getName();
	    } else {
			return val.toString();
		}
	} // toString
	
	/**
	 * Convert the object value to Date value
	 * 
	 * @param val Value
	 * @return Short value
	 */
	public static Date toDate( Object val )
	{
	    if ( val == null ) {
	    	return null;
		}
		else if ( val instanceof Date ) {
			return (Date) val;
	    }
		else if ( val instanceof String ) {
			if (((String)val).isEmpty()) return null;
			else return new Date(Long.parseLong((String) val));
		}
	    else if ( val instanceof Boolean ) {
	        if ((Boolean) val) return new Date();
	        else return new Date( 0 );
	    }
	    else if ( val instanceof Byte ) {
	        return new Date( (long) (Byte) val);
	    }
	    else if ( val instanceof Short ) {
	        return new Date( (long) (Short) val);
	    }
	    else if ( val instanceof Integer ) {
	        return new Date( (long) (Integer) val);
	    }
	    else if ( val instanceof Long ) {
	        return new Date( (long) val);
	    }
	    else if ( val instanceof Float ) {
	        return new Date( (long) ((Float) val ).floatValue() );
	    }
	    else if ( val instanceof Double ) {
			return new Date( (long) downScaleCheck( "double", "long", (double) val, (double) Long.MIN_VALUE, (double) Long.MAX_VALUE ));
	    }

	    // Catch anything else
		return new Date( Long.parseLong( val.toString() ));
	} // toDate

	/** Check down scaling for decimals */
	private static double downScaleCheck( String from, String to, double val, double min, double max ) {
		if ( val > max  || val < min) {
			throw new NumberFormatException( "Cannot convert "+from+" to "+to+" as it exceeds the max or min value: " + val );
		}
		return val;
	}

	/** Check down scaling for numbers */
	private static long downScaleCheck( String from, String to, long val, long min, long max ) {
		if ( val > max  || val < min) {
			throw new NumberFormatException( "Cannot convert "+from+" to "+to+" as it exceeds the max or min value: " + val );
		}
		return val;
	}

} // Converter
