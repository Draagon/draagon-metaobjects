package com.draagon.meta.relation.key;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.relation.ObjectRelation;

import java.util.ArrayList;
import java.util.List;

public class ObjectKey extends ObjectRelation {

    public final static String TYPE_OBJECTKEY = "objectKey";

    public final static String ATTR_KEY = "key";
    public final static String ATTR_ISKEY = "isKey";

    public final static String SUBTYPE_KEY = "key";

    /**
     * Constructs the MetaData
     */
    public ObjectKey() {
        super(TYPE_OBJECTKEY, SUBTYPE_KEY, ATTR_KEY );
    }

    /**
     * Constructs the MetaData
     */
    protected ObjectKey(String subType, String name) {
        super(TYPE_OBJECTKEY, subType, name);
    }

    public MetaObject getParentObject() {
        MetaData parent = getParent();
        if ( !( parent instanceof MetaObject )) throw new MetaDataException(
                "getParentObject() called, but parent is NOT a MetaObject: objectKey="
                + getName() +", parent=" + parent.toString() );
        return (MetaObject) parent;
    }

    /** Return the key as a string for the provided object */
    public String getKeyAsString( Object o ) {
        MetaField keyField = getFieldKeys().iterator().next();
        if ( keyField == null ) throw new MetaDataException(
                "getKeyAsString(o) called, but no MetaFields with isKey=true existed on parent MetaObject: "
                        + "parent=" + getParent().getName() );
        return keyField.getString( o );
    }

    /** Get Field Keys */
    protected List<MetaField> getFieldKeys() {

        final String KEY = "getFieldKeys()";

        List<MetaField> keys = null;
        synchronized (this) {
            keys = (List<MetaField>) getCacheValue(KEY);
            if (keys == null) {
                for (MetaField f : getParentObject().getChildren(MetaField.class, true)) {
                    if (f.hasMetaAttr(ATTR_ISKEY)
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
