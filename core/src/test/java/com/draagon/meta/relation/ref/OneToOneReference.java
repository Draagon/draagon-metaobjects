package com.draagon.meta.relation.ref;

public class OneToOneReference extends ObjectReference {

    public final static String SUBTYPE_ONETOONE = "oneToOne";

    /**
     * Constructs the MetaData
     */
    public OneToOneReference(String name) {
        super(SUBTYPE_ONETOONE, name);
    }
}
