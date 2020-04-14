package com.draagon.meta;

import java.lang.reflect.Array;
import java.util.Date;
import java.util.List;

/**
 * Data Types enum
 */
public enum DataTypes {

    // Numeric
    BOOLEAN(1), BYTE(2), SHORT(3), INT(4), LONG(5),
    FLOAT(6), DOUBLE(7),

    // String & Date
    STRING(8), DATE(9),

    // Numeric  Arrays
    BOOLEAN_ARRAY(11), BYTE_ARRAY(12), SHORT_ARRAY(13), INT_ARRAY(14), LONG_ARRAY(15),
    FLOAT_ARRAY(16), DOUBLE_ARRAY(17),

    // String & Date
    STRING_ARRAY(18), DATE_ARRAY(19),

    // Object and Object Array
    OBJECT(20), OBJECT_ARRAY(21),

    // Custom
    CUSTOM(99);

    private int id;

    private boolean isBoolean = false;
    private boolean isNumeric = false;
    private boolean isDate = false;
    private boolean isString = false;

    private boolean isArray = false;
    private boolean isBooleanArray = false;
    private boolean isNumericArray = false;
    private boolean isDateArray = false;
    private boolean isStringArray = false;

    private boolean isObject = false;
    private boolean isObjectArray = false;

    private boolean isCustom = false;

    private Class<?> valueClass = null;
    private Class<?> itemClass = null;

    private DataTypes( int id ) {
        this.id = id;
        switch( id) {
            case 1:  valueClass=Boolean.class; isBoolean=true; break;
            case 2:  valueClass=Byte.class;    isNumeric=true; break;
            case 3:  valueClass=Short.class;   isNumeric=true; break;
            case 4:  valueClass=Integer.class; isNumeric=true; break;
            case 5:  valueClass=Long.class;    isNumeric=true; break;
            case 6:  valueClass=Float.class;   isNumeric=true; break;
            case 7:  valueClass=Double.class;  isNumeric=true; break;
            case 8:  valueClass=String.class;  isString=true;  break;
            case 9:  valueClass=Date.class;    isDate=true;    break;
            case 20: valueClass=Object.class;  isObject=true;  break;

            case 11: valueClass=List.class; isArray=true; itemClass=Boolean.class; isBooleanArray=true; break;
            case 12: valueClass=List.class; isArray=true; itemClass=Byte.class;    isNumericArray=true; break;
            case 13: valueClass=List.class; isArray=true; itemClass=Short.class;   isNumericArray=true; break;
            case 14: valueClass=List.class; isArray=true; itemClass=Integer.class; isNumericArray=true; break;
            case 15: valueClass=List.class; isArray=true; itemClass=Long.class;    isNumericArray=true; break;
            case 16: valueClass=List.class; isArray=true; itemClass=Float.class;   isNumericArray=true; break;
            case 17: valueClass=List.class; isArray=true; itemClass=Double.class;  isNumericArray=true; break;
            case 18: valueClass=List.class; isArray=true; itemClass=String.class;  isStringArray=true;  break;
            case 19: valueClass=List.class; isArray=true; itemClass=Date.class;    isDateArray=true;    break;
            case 21: valueClass=List.class; isArray=true; itemClass=Object.class;  isObjectArray=true;  break;

            case 99: valueClass=Object.class; isCustom=true; break;

            default: throw new IllegalStateException( "Unexpected DataType: " + this.getClass().getName() );
        }
    }

    public int getId() {
        return id;
    }

    public boolean isBoolean() {
        return isBoolean;
    }
    public boolean isNumeric() {
        return isNumeric;
    }
    public boolean isString() {
        return isString;
    }
    public boolean isDate() {
        return isDate;
    }
    public boolean isObject() {
        return isObject;
    }

    public boolean isArray() {
        return isArray;
    }
    public boolean isBooleanArray() {
        return isBooleanArray;
    }
    public boolean isNumericArray() {
        return isNumericArray;
    }
    public boolean isStringArray() {
        return isStringArray;
    }
    public boolean isDateArray() {
        return isDateArray;
    }
    public boolean isObjectArray() {
        return isObjectArray;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public Class<?> getValueClass() {
        return valueClass;
    }
    public Class<?> getArrayItemClass() {
        return itemClass;
    }

    public static DataTypes forValueClass( Class<?> c ) {

        if ( c == null ) throw new IllegalArgumentException( "Cannot retrieve DataTypes for null Class" );

        // Iterate through the enum
        for ( DataTypes t : DataTypes.values() ) {

            // If we are comparing arrays
            if ( t.isArray()
                    && List.class.isAssignableFrom( c )
                        && t.getArrayItemClass().equals( c.getComponentType())) {
                return t;
            }

            // If we are comparing non-arrays
            else if ( !List.class.isAssignableFrom( c ) &&
                    !t.isObject() && !t.isCustom()
                    && t.getValueClass().equals( c )) {
                return t;
            }
        }

        // Return this as the default
        return OBJECT;
    }
}
