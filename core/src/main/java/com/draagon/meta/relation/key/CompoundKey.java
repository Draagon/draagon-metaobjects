package com.draagon.meta.relation.key;

public class CompoundKey extends ObjectKey {

    public final static String SUBTYPE_COMPOUND = "compound";

    public CompoundKey( String name ) {
        super(SUBTYPE_COMPOUND, name );
    }

    @Override
    public String getKeyForObject(Object o) {
        return "TODO";
    }
}
