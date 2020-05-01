package com.draagon.meta.relation.key;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.relation.ObjectRelation;

import java.util.ArrayList;
import java.util.List;

public abstract class ObjectKey extends ObjectRelation {

    public final static String TYPE_OBJECTKEY = "objectKey";

    public final static String CACHE_FIELDKEYS = "ObjectKey-fieldKeys";

    public final static String ATTR_KEY = "key";
    public final static String ATTR_ISKEY = "isKey";

    /**
     * Constructs the MetaData
     */
    public ObjectKey(String subType, String name) {
        super(TYPE_OBJECTKEY, subType, name);
    }

    /** Return the key as a string for the provided object */
    public abstract String getKeyAsString( Object o );

    /** Get Field Keys */
    protected List<MetaField> getFieldKeys() {

        List<MetaField> keys = null;
        synchronized (this) {
            keys = (List<MetaField>) getCacheValue(CACHE_FIELDKEYS);
            if (keys == null) {
                for (MetaField f : getChildren(MetaField.class, true)) {
                    if (f.hasAttr(ATTR_ISKEY)) keys.add(f);
                }
                setCacheValue(CACHE_FIELDKEYS, new ArrayList<MetaField>());
            }
        }

        return keys;
    }
}
