package com.draagon.meta.relation.key;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.relation.ObjectRelation;

import java.util.ArrayList;
import java.util.List;

public abstract class ObjectKey extends ObjectRelation {

    public final static String TYPE_OBJECTKEY = "objectKey";

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

        final String KEY = "getFieldKeys()";

        List<MetaField> keys = null;
        synchronized (this) {
            keys = (List<MetaField>) getCacheValue(KEY);
            if (keys == null) {
                for (MetaField f : getChildren(MetaField.class, true)) {
                    if (f.hasAttr(ATTR_ISKEY)
                            && Boolean.TRUE.equals( f.getMetaAttr(ATTR_ISKEY).getValue())) {
                        keys.add(f);
                    }
                }
                setCacheValue(KEY, new ArrayList<MetaField>());
            }
        }

        return keys;
    }
}
