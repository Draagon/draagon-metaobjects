package com.draagon.meta.relation.key;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.field.MetaField;

public class PrimaryKey extends ObjectKey {

    public final static String SUBTYPE_PRIMARY = "primary";

    /**
     * Constructs the MetaData
     */
    public PrimaryKey(String name) {
        super(SUBTYPE_PRIMARY, name);
    }

    @Override
    public String getKeyAsString(Object o) {
        return getPrimaryKey().getString(o);
    }

    /** Get the Primary Key */
    public MetaField getPrimaryKey() {
        return getFieldKeys().iterator().next();
    }

    @Override
    public void validate() {
        if ( getFieldKeys().size() != 1 ) {
            throw new MetaDataException( "PrimaryKey must have one and only one MetaField with the "+ObjectKey.ATTR_ISKEY+" attribute: " + getFieldKeys());
        }
    }
}
