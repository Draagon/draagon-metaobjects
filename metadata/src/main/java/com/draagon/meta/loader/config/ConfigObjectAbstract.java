package com.draagon.meta.loader.config;

import com.draagon.meta.InvalidValueException;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.data.DataObject;

import java.util.ArrayList;
import java.util.List;

public abstract class ConfigObjectAbstract extends DataObject {

    public final static String FIELD_TYPE       = "type";
    public final static String FIELD_BASECLASS  = "class";
    public final static String FIELD_CHILDREN   = "children";
    public final static String OBJREF_CHILD     = "childRef";

    protected ConfigObjectAbstract(MetaObject mo ) {
        super( mo );
    }

    /////////////////////////////////////////////////////////////////////
    // Helper ConfigValue Methods

    protected void mergeChildConfigs(String childrenField, ChildConfig config) {

        // TODO:  Merge same children together
        _addToObjectArray( childrenField, config );
    }

    /////////////////////////////////////////////////////////////////////
    // Helper ValueObject Methods

    public abstract void validate();
}
