/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;

/**
 * A String Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
public class StringField extends MetaField<String> {

    public final static String SUBTYPE_STRING   = "string";

    public StringField( String name ) {
        super( SUBTYPE_STRING, name, DataTypes.STRING );
    }
}
