package com.draagon.meta.relation.key;

public class CompoundKey extends ObjectKey {

    public final static String SUBTYPE_COMPOUNDKEY = "compoundKey";

    public CompoundKey( String name ) {
        super(SUBTYPE_COMPOUNDKEY, name );
    }

    @Override
    public String getKeyForObject(Object o) {
        return "TODO";
    }
}
