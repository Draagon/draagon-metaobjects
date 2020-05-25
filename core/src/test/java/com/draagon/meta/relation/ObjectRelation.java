package com.draagon.meta.relation;

import com.draagon.meta.MetaData;

public abstract class ObjectRelation extends MetaData {

    /**
     * Constructs the MetaData
     */
    public ObjectRelation( String type, String subType, String name) {
        super(type, subType, name);
    }
}
