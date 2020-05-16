/*
 * Copyright 2003-2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.field;

/**
 * A ValueType interface contains the int values for various
 * native datatypes.
 *
 * @version 2.0
 * @author Doug Mealing
 *
 * @deprecated Use DataTypes enum instead
 */
public interface MetaFieldTypes
{
    public final static int BOOLEAN  = 1;
    public final static int BYTE     = 2;
    public final static int SHORT    = 3;
    public final static int INT      = 4;
    public final static int LONG     = 5;
    public final static int FLOAT    = 6;
    public final static int DOUBLE   = 7;
    public final static int STRING   = 8;
    public final static int DATE     = 9;

    public final static int BOOLEAN_ARRAY  = 11;
    public final static int BYTE_ARRAY     = 12;
    public final static int SHORT_ARRAY    = 13;
    public final static int INT_ARRAY      = 14;
    public final static int LONG_ARRAY     = 15;
    public final static int FLOAT_ARRAY    = 16;
    public final static int DOUBLE_ARRAY   = 17;
    public final static int STRING_ARRAY   = 18;
    public final static int DATE_ARRAY     = 19;

    public final static int OBJECT          = 20;
    public final static int OBJECT_ARRAY    = 21;

    public final static int CLOB     = 30;
    public final static int BLOB     = 31;
    public final static int XML      = 32;
    public final static int JSON     = 33;
    public final static int HTML5    = 34;

    public final static int CUSTOM   = 99;
}
