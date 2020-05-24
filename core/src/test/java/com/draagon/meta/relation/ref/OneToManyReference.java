package com.draagon.meta.relation.ref;

public class OneToManyReference extends ObjectReference {

    public final static String SUBTYPE_ONETOMANY = "oneToMany";

    /**
     * Constructs the MetaData
     */
    public OneToManyReference(String name) {
        super(SUBTYPE_ONETOMANY, name);
    }
}
