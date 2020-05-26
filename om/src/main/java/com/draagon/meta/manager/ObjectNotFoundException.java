/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager;

import com.draagon.meta.*;

@SuppressWarnings("serial")
public class ObjectNotFoundException extends MetaDataException {

    private Object object = null;

    public ObjectNotFoundException(String msg) {
        super(msg);
    }

    public ObjectNotFoundException(Object o) {
        super("Object Not Found [" + o + "]");
        object = o;
    }

    public Object getObject() {
        return object;
    }

    @Override
    public String toString() {
        if (object == null) {
            return super.toString();
        } else {
            return "[" + object.toString() + "]" + super.toString();
        }
    }
}

