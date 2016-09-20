/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectNotFoundException;

import java.util.List;

/**
 * A Object Array Field.
 *
 * @version 1.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class ObjectArrayField extends MetaField {
    //private static Log log = LogFactory.getLog( ObjectField.class );

    /**
     * MetaObject name attribute
     */
    public final static String ATTR_OBJECT_REF = "objectRef";

    public ObjectArrayField(String name) {
        super(name);
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

    /**
     * Return the specified MetaObject
     */
    public MetaObject getMetaObject() //throws MetaFieldNotFoundException
    {
        final String KEY = "getMetaObject()";

        MetaObject o = (MetaObject) getCacheValue(KEY);

        if (o == null) {

            Object a = getAttribute( ATTR_OBJECT_REF );
            if ( a != null ) {
                String name = a.toString();

                try {
                    o = MetaDataLoader.findMetaDataByName( MetaObject.class, name);
                } catch (MetaDataNotFoundException e) {
                    throw new MetaObjectNotFoundException("MetaObject[" + name + "] referenced by MetaField [" + toString() + "] does not exist", name);
                }

                setCacheValue(KEY, o);
            }
        }

        return o;
    }
}
