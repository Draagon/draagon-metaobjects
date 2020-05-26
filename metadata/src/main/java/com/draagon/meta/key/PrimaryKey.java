package com.draagon.meta.key;

public class PrimaryKey extends MetaKey {

    public final static String SUBTYPE = "primary";
    public final static String NAME = "primary";

    public PrimaryKey() {
        super(SUBTYPE, NAME);
    }

    @Override
    public ObjectKey getObjectKey(Object o) {
        return getObjectKeyForKeyFields( getDeclaringObject(), KeyTypes.PRIMARY, getKeyFields(), o );
    }
}
