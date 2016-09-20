/*
 * Copyright 2003-2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.field;

/**
 * A ValueType interface contains the int values for various
 * datatypes.
 *
 * @version 2.0
 * @author Doug Mealing
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
    public final static int OBJECT   = 10;

    public final static int CLOB     = 11;
    public final static int BLOB     = 12;
    public final static int XML      = 13;

    public final static int OBJECT_ARRAY    = 14;
}
