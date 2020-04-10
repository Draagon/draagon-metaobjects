/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.util.MetaDataUtil;

/**
 * A Object Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
public class ObjectField extends MetaField<Object> {
    //private static Log log = LogFactory.getLog( ObjectField.class );

    /**
     * MetaObject name attribute
     */
    //public final static String ATTR_OBJECT_REF = "objectRef";

    public ObjectField( String name ) {
        super( SUBTYPE_OBJECT, name );
    }

    /**
     * Returns the type of value
     */
    public int getType() {
        return OBJECT;
    }

    /**
     * Gets the type of value object class returned
     */
    public Class<?> getValueClass() {
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
