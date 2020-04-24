package com.draagon.meta.relation.key;

import com.draagon.meta.relation.ObjectRelation;

public abstract class ObjectKey extends ObjectRelation {

    public final static String TYPE_OBJECTKEY = "objectKey";

    public final static String ATTR_ISKEY = "isKey";

    /**
     * Constructs the MetaData
     */
    public ObjectKey(String subType, String name) {
        super(TYPE_OBJECTKEY, subType, name);
    }

    /** Return the key as a string for the provided object */
    public abstract String getKeyForObject( Object o );
}
