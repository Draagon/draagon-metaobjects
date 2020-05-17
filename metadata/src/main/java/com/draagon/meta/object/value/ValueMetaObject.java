/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.object.value;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.object.data.DataMetaObject;
import com.draagon.meta.object.data.DataObject;

public class ValueMetaObject extends DataMetaObject
{
    public final static String SUBTYPE_VALUE = "value";

    /**
     * Constructs the MetaClassObject for MetaObjects
     */
    public ValueMetaObject( String name ) {
        super( SUBTYPE_VALUE, name);
    }

    /**
     * Manually create a ValueMetaObject with the specified name
     * @param name Name for the ValueMetaObject
     * @return Created ValueObject
     */
    public static ValueMetaObject create( String name ) {
        return new ValueMetaObject( name );
    }

    @Override
    public boolean allowExtensions() {
        if ( hasMetaAttr(ATTR_ALLOWEXTENSIONS)) {
            return super.allowExtensions();
        }
        return false;
    }

    @Override
    public boolean isStrict() {
        if ( hasMetaAttr(ATTR_ISSTRICT)) {
            return super.isStrict();
        }
        return false;
    }


    /**
     * Whether the MetaClass handles the object specified
     */
    @Override
    public boolean produces(Object obj) {

        if (obj != null && obj instanceof ValueObject) {
            return super.produces( obj );
        }

        return false;
    }

    @Override
    protected Class<?> getDefaultObjectClass() {
        return ValueObject.class;
    }
}
