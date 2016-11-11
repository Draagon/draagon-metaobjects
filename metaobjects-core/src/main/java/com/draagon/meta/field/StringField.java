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
@SuppressWarnings("serial")
public class StringField extends MetaField<String> {
    //private static Log log = LogFactory.getLog( StringField.class );

    public StringField(String name) {
        super(name);
    }

    /**
     * Returns the type of value
     */
    public int getType() {
        return STRING;
    }

    /**
     * Gets the type of value object class returned
     */
    public Class<?> getValueClass() {
        return String.class;
    }
}
