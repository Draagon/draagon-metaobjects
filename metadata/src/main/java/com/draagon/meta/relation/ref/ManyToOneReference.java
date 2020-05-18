package com.draagon.meta.relation.ref;

public class ManyToOneReference extends ObjectReference {

    public final static String SUBTYPE_MANYTOONE = "manyToOne";

    /**
     * Constructs the MetaData
     */
    public ManyToOneReference(String name) {
        super(SUBTYPE_MANYTOONE, name);
    }
}
