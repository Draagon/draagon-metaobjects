package com.draagon.meta.relation.key;

public class PrimaryKey extends ObjectKey {

    public final static String SUBTYPE_PRIMARYKEY = "primaryKey";

    /**
     * Constructs the MetaData
     */
    public PrimaryKey(String name) {
        super(SUBTYPE_PRIMARYKEY, name);
    }

    @Override
    public String getKeyForObject(Object o) {
        return "TODO";
    }
}
