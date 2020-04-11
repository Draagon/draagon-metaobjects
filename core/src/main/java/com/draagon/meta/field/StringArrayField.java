/*
 * Copyright 2016 Doug Mealing LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.DataTypes;

import java.util.List;

/**
 * A String Array Field.
 *
 * @version 1.0
 * @author Doug Mealing
 */
public class StringArrayField extends ArrayField<List<String>> {

    public final static String SUBTYPE_STRING_ARRAY = "stringArray";

    public StringArrayField( String name ) {
        super( SUBTYPE_STRING_ARRAY, name, DataTypes.STRING_ARRAY );
    }
}
