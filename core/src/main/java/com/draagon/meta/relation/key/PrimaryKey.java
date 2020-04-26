package com.draagon.meta.relation.key;

public class PrimaryKey extends ObjectKey {

    public final static String SUBTYPE_PRIMARY = "primary";

    /**
     * Constructs the MetaData
     */
    public PrimaryKey(String name) {
        super(SUBTYPE_PRIMARY, name);
    }

    @Override
    public String getKeyForObject(Object o) {
        return "TODO";
    }
}
