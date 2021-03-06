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

import java.util.List;

/**
 * An Object Array Field.
 *
 * @version 1.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class ObjectArrayField extends ArrayField<Object,List<Object>> {

    public final static String SUBTYPE_OBJECT_ARRAY = "objectArray";

    public ObjectArrayField(String name ) {
        super( SUBTYPE_OBJECT_ARRAY, name, DataTypes.OBJECT_ARRAY );
    }

    /**
     * Manually Create an Object Filed
     * @param name Name of the field
     * @return New ObjectField
     */
    public static ObjectArrayField create( String name ) {
        return new ObjectArrayField( name );

    }

    public static ObjectArrayField create( String name, boolean emptyDefault ) {
        return new ObjectArrayField( name );
    }

    /**
     * Return the specified MetaObject
     */
    public MetaObject getObjectRef() {
        return MetaDataUtil.getObjectRef(this);
    }

    /** Return the array item type */
    public Class getArrayItemClass() {

        if ( getObjectRef() != null ) {
            MetaObject mo = getObjectRef();
            try {
                return mo.getObjectClass();
            } catch (ClassNotFoundException e) {
            }
        }

        return Object.class;
    }
}
