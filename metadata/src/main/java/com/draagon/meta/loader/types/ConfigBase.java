package com.draagon.meta.loader.types;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.data.DataObject;

public abstract class ConfigBase extends DataObject {

    protected ConfigBase(MetaObject mo ) {
        super( mo );
    }

    /////////////////////////////////////////////////////////////////////
    // Helper ConfigValue Methods

    protected void overwriteAttributeIfNotNull(String name, ConfigBase co ) {
        if ( co._getObjectAttribute(name) != null ) _setObjectAttribute( name, co._getObjectAttribute( name ));
    }
}
