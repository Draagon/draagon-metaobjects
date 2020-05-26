package com.draagon.meta.key;

import com.draagon.meta.object.MetaObject;

import java.lang.reflect.Array;

public class ObjectKey {

    private final MetaObject metaObject;
    private final MetaKey.KeyTypes keyType;
    private final Object [] key;


    public ObjectKey(Object key) {
        if ( key.getClass().isArray()) {
            this.key = (Object []) key;
        } else {
            this.key = new Object[1];
            this.key[0] = key;
        }
        this.keyType = MetaKey.KeyTypes.UNKNOWN;
        this.metaObject=null;
    }

    public ObjectKey(MetaObject metaObject, MetaKey.KeyTypes keyType, Object [] key ) {
        this.key=key;
        this.keyType = keyType;
        this.metaObject=metaObject;
    }

    public Object [] get() {
        return key;
    }

    public MetaObject getMetaObject() {
        return metaObject;
    }

    public String getAsString() {
        StringBuilder b = new StringBuilder();
        for( int i = 0; i < key.length; i++ ) {
            if (b.length()>0) b.append(",");
            b.append(String.valueOf(key[i]));
        }
        String keyStr = b.toString();
        return keyStr;
    }

    public String toString() {
        return (metaObject==null? MetaKey.KeyTypes.UNKNOWN :metaObject.getName()+":"+keyType)+"["+getAsString()+"]";
    }
}
