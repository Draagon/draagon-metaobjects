package com.draagon.meta.key;

public class SecondaryKey extends MetaKey {

    public final static String SUBTYPE = "secondary";

    public SecondaryKey(String name) {
        super(SUBTYPE, name);
    }

    private SecondaryKey(String subType, String name) {
        super(subType, name);
    }

    @Override
    public ObjectKey getObjectKey(Object o) {
        return getObjectKeyForKeyFields( getDeclaringObject(), KeyTypes.SECONDARY, getKeyFields(), o );
    }
}
