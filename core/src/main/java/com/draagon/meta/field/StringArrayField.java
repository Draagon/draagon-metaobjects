/*
 * Copyright 2016 Doug Mealing LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import java.util.List;

/**
 * A String Array Field.
 *
 * @version 1.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class StringArrayField extends ArrayField<List<String>> {

    public StringArrayField(String name) {
        super(name);
    }

    /**
     * Returns the type of value
     */
    @Override
    public int getType() {
        return STRING_ARRAY;
    }

    /**
     * Gets the type of value object class returned
     */
    @Override
    public Class<?> getValueClass() {
        return List.class;
    }

    /** Get the Class for the items in the array */
    @Override
    public Class getItemClass() {
        return String.class;
    }
}
