package com.draagon.meta.loader.config;

import com.draagon.meta.InvalidValueException;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.data.DataObject;

import java.util.ArrayList;
import java.util.List;

public abstract class ConfigObjectAbstract extends DataObject {

    protected ConfigObjectAbstract(String name) {
        super( name );
    }
    protected ConfigObjectAbstract(MetaObject mo ) {
        super( mo );
    }

    /////////////////////////////////////////////////////////////////////
    // Helper ConfigValue Methods

    protected void overwriteAttributeIfNotNull(String name, ConfigObjectAbstract co ) {
        if ( co._getObjectAttribute(name) != null ) _setObjectAttribute( name, co._getObjectAttribute( name ));
    }

    /////////////////////////////////////////////////////////////////////
    // Helper ValueObject Methods

    public abstract void validate();
}
