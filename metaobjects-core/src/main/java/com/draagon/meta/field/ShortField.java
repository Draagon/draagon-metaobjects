/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;

/**
 * A Short Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class ShortField extends MetaField {
    //private static Log log = LogFactory.getLog( ShortField.class );

    public ShortField(String name) {
        super(name);
    }

    /**
     * Returns the type of value
     */
    public int getType() {
        return SHORT;
    }

    /**
     * Gets the type of value object class returned
     */
    public Class<?> getValueClass() {
        return Short.class;
    }
}
