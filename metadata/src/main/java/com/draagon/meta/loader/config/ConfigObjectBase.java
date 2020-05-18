package com.draagon.meta.loader.config;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.data.DataObject;

public abstract class ConfigObjectBase extends DataObject {

    protected ConfigObjectBase(MetaObject mo ) {
        super( mo );
    }

    /////////////////////////////////////////////////////////////////////
    // Helper ConfigValue Methods

    protected void overwriteAttributeIfNotNull(String name, ConfigObjectBase co ) {
        if ( co._getObjectAttribute(name) != null ) _setObjectAttribute( name, co._getObjectAttribute( name ));
    }
}
