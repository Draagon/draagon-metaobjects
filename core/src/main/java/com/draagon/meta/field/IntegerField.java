/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;

/**
 * An Integer Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
public class IntegerField extends MetaField<Integer> {

    public IntegerField( String name ) {
        super( SUBTYPE_INT, name );
    }

    /**
     * Returns the type of value
     */
    public int getType() {
        return INT;
    }

    /**
     * Gets the type of value object class returned
     */
    public Class<?> getValueClass() {
        return Integer.class;
    }
}
