package com.draagon.meta.relation.ref;

public class ManyToManyReference extends ObjectReference {

    public final static String SUBTYPE_MANYTOMANY = "manyToMany";

    /**
     * Constructs the MetaData
     */
    public ManyToManyReference(String name) {
        super(SUBTYPE_MANYTOMANY, name);
    }
}
