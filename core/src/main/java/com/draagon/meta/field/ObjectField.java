/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.DataTypes;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.util.MetaDataUtil;

/**
 * A Object Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
public class ObjectField extends MetaField<Object> {

    public final static String SUBTYPE_OBJECT       = "object";

    public ObjectField( String name ) {
        super( SUBTYPE_OBJECT, name, DataTypes.OBJECT );
    }

    /**
     * Manually Create an Object Filed
     * @param name Name of the field
     * @return New ObjectField
     */
    public static ObjectField create( String name ) {
        ObjectField f = new ObjectField( name );
        return f;
    }

    /**
     * Return the referenced MetaObject
     */
    public MetaObject getObjectRef() {
        return MetaDataUtil.getObjectRef(this);
    }
}
