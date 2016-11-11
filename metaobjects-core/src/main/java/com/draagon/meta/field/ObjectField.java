/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectNotFoundException;

/**
 * A Object Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class ObjectField extends MetaField<Object> {
    //private static Log log = LogFactory.getLog( ObjectField.class );

    /**
     * MetaObject name attribute
     */
    public final static String ATTR_OBJECT_REF = "objectRef";

    public ObjectField(String name) {
        super(name);
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
        return getObjectRef(this);
    }

    /** Gets the MetaObject referenced by this field */
    static MetaObject getObjectRef( MetaField f ) {

        final String KEY = "getObjectRef()";

        MetaObject o = (MetaObject) f.getCacheValue(KEY);

        if (o == null) {

            Object a = f.getAttribute( ATTR_OBJECT_REF );
            if ( a != null ) {
                String name = a.toString();

                try {
                    o = MetaDataLoader.findMetaDataByName( MetaObject.class, name);
                } catch (MetaDataNotFoundException e) {
                    throw new MetaObjectNotFoundException("MetaObject[" + name + "] referenced by MetaField [" + f.toString() + "] does not exist", name);
                }

                f.setCacheValue(KEY, o);
            }
        }

        return o;
    }
}
