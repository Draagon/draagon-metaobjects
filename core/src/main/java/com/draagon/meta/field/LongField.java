/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;

/**
 * A Long Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
public class LongField extends MetaField<Long> {

    public LongField( String name ) {
        super( SUBTYPE_LONG, name );
    }

    /**
     * Returns the type of value
     */
    public int getType() {
        return LONG;
    }

    /**
     * Gets the type of value object class returned
     */
    public Class<?> getValueClass() {
        return Long.class;
    }
}
