package com.draagon.meta.util;

import com.draagon.meta.DataTypes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	private static Log log = LogFactory.getLog( DataConverter.class );

	/** Private constructor */
	private DataConverter() {}

	/** Converts the object to the specified MetaFieldType */
	public static Object toType( DataTypes dataType, Object val ) {
		
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

	/** Convert to an Object, if a list returns null if empty, or object if length of 1, otherwise an exception */
	public static List<String> toStringArray( Object val ) {

		if ( val == null ) {
			return null;
		}
		else if (val instanceof List<?>) {
			return (List<String>) val;
		}
		else if ( val instanceof String ) {
			List<String> list = new ArrayList<>();
			if ( !((String) val).isEmpty()) {
				Collections.addAll(list, ((String) val).split(","));
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
			return (List<Object>) val;
		}
		else {
			List<Object> l = new ArrayList<Object>();
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
	      //else if ( val instanceof String ) {
	      //  if ( val.toString().length() > 0 && ( val.toString().charAt( 0 ) == 't'
	      //      || val.toString().charAt( 0 ) == 'T' )) return true;
	      //}
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
	    if ( val == null ) return null;

		if ( val instanceof String && ((String) val).isEmpty()) return null;
	
	    if ( val instanceof Boolean ) {
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
	      return (Float) val;
	    }
	    else if ( val instanceof Double ) {
	      return (float) ((Double) val).doubleValue();
	    }
	    else if ( val instanceof String ) {
	      try { return Float.parseFloat((String) val); } catch( Exception ignored) {}
	    }
	    else if ( val instanceof Date ) {
	      return (float) ((Date) val).getTime();
	    }
	    else {
	      try { return Float.parseFloat(val.toString()); } catch( Exception ignored) {}
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
	    if ( val == null ) return null;

		if ( val instanceof String && ((String) val).isEmpty()) return null;
	
	    if ( val instanceof Boolean ) {
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
	      return (Long) val;
	    }
	    else if ( val instanceof Float ) {
	      return (long) ((Float) val).floatValue();
	    }
	    else if ( val instanceof Double ) {
	      return (long) ((Double) val).doubleValue();
	    }
	    else if ( val instanceof String ) {
	      try { return Long.parseLong((String) val); } catch( Exception ignored) {}
	    }
	    else if ( val instanceof Date ) {
	      return ((Date) val).getTime();
	    }
	    else {
	      try { return Long.parseLong(val.toString()); } catch( Exception ignored) {}
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
	    if ( val == null ) return null;

		if ( val instanceof String && ((String) val).isEmpty()) return null;
	
	    if ( val instanceof Boolean ) {
	      if ((Boolean) val) return 1;
	    }
	    else if ( val instanceof Byte ) {
	      return (int) (Byte) val;
	    }
	    else if ( val instanceof Short ) {
	      return (int) (Short) val;
	    }
	    else if ( val instanceof Integer ) {
	      return (Integer) val;
	    }
	    else if ( val instanceof Long ) {
	      return (int) ((Long) val).longValue();
	    }
	    else if ( val instanceof Float ) {
	      return (int) ((Float) val).floatValue();
	    }
	    else if ( val instanceof Double ) {
	      return (int) ((Double) val).doubleValue();
	    }
	    else if ( val instanceof String ) {
	      try { return Integer.parseInt((String) val); } catch( Exception ignored) {}
	    }
	    else if ( val instanceof Date ) {
	      return (int) ((Date) val).getTime();
	    }
	    else {
	      try { return Integer.parseInt(val.toString()); } catch( Exception ignored) {}
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
	    if ( val == null ) return null;

		if ( val instanceof String && ((String) val).isEmpty()) return null;
	
	    if ( val instanceof Boolean ) {
	      if ((Boolean) val) return (short) 1;
	    }
	    else if ( val instanceof Byte ) {
	      return (short) (byte) val;
	    }
	    else if ( val instanceof Short ) {
	      return (Short) val;
	    }
	    else if ( val instanceof Integer ) {
	      return (short) ((Integer) val).intValue();
	    }
	    else if ( val instanceof Long ) {
	      return (short) ((Long) val).longValue();
	    }
	    else if ( val instanceof Float ) {
	      return (short) ((Float) val).floatValue();
	    }
	    else if ( val instanceof Double ) {
	      return (short) ((Double) val).doubleValue();
	    }
	    else if ( val instanceof String ) {
	      try { return Short.parseShort((String) val); } catch( Exception ignored) {}
	    }
	    else if ( val instanceof Date ) {
	      return (short) ((Date) val).getTime();
	    }
	    else {
	      try { return Short.parseShort(val.toString()); } catch( Exception ignored) {}
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
	    if ( val == null ) return null;

		if ( val instanceof String && ((String) val).isEmpty()) return null;
	
	    if ( val instanceof Boolean ) {
	      if ((Boolean) val) return (byte) 1;
	    }
	    else if ( val instanceof Byte ) {
	      return (Byte) val;
	    }
	    else if ( val instanceof Short ) {
	      return (byte) ((Short) val).intValue();
	    }
	    else if ( val instanceof Integer ) {
	      return (byte) ((Integer) val).intValue();
	    }
	    else if ( val instanceof Long ) {
	      return (byte) ((Long) val).longValue();
	    }
	    else if ( val instanceof Float ) {
	      return (byte) ((Float) val).floatValue();
	    }
	    else if ( val instanceof Double ) {
	      return (byte) ((Double) val).doubleValue();
	    }
	    else if ( val instanceof String ) {
	      try { return Byte.parseByte((String) val); } catch( Exception ignored) {}
	    }
	    else if ( val instanceof Date ) {
	      return (byte) ((Date) val).getTime();
	    }
	    else {
	      try { return Byte.parseByte(val.toString()); } catch( Exception ignored) {}
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

		if ( val instanceof String && ((String) val).isEmpty()) return null;

	    if ( val instanceof Boolean ) {
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
	        return new Date( (long) ((Double) val ).doubleValue() );
	      }
	      else if ( val instanceof String ) {
	      	// TODO:  Implement date format support
			try {
				return new Date(Long.parseLong((String) val));
			} catch (NumberFormatException e ) {
				log.error( "Unable to parse Date, expected long value: " + val );
			}
	      }
	      else if ( val instanceof Date ) {
	        return (Date) val;
	      }
	      // Catch anything else
	      try { return new Date( Long.parseLong( val.toString() )); }
	      catch( Exception e ) { return new Date( 0 ); }
	} // toDate
	
} // Converter
