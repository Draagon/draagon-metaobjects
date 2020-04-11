/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;

/**
 * A Float Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
public class FloatField extends MetaField<Float> {

    public final static String SUBTYPE_FLOAT    = "float";

    public FloatField( String name ) {
        super( SUBTYPE_FLOAT, name, DataTypes.FLOAT );
    }
}
