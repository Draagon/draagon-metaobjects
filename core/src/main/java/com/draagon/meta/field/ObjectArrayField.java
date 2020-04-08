/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.util.MetaDataUtil;

import java.util.List;

/**
 * A Object Array Field.
 *
 * @version 1.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class ObjectArrayField extends ArrayField<List<Object>> {
    //private static Log log = LogFactory.getLog( ObjectField.class );

    /**
     * MetaObject name attribute
     */
    public final static String ATTR_OBJECT_REF = ObjectField.ATTR_OBJECT_REF;

    public ObjectArrayField(String type, String subtype, String name ) {
        super( type, subtype, name );
    }

    /**
     * Returns the type of value
     */
    public int getType() {
        return OBJECT_ARRAY;
    }

    /**
     * Gets the type of value object class returned
     */
    public Class<?> getValueClass() {
        return List.class;
    }

    /** Return the Class type for items in the array */
    @Override
    public Class getItemClass() {
        return Object.class;
    }

    /**
     * Return the specified MetaObject
     */
    public MetaObject getObjectRef() //throws MetaFieldNotFoundException
    {
        return MetaDataUtil.getObjectRef(this);
    }
}
