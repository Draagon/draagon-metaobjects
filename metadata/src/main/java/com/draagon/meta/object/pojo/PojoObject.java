package com.draagon.meta.object.pojo;

import com.draagon.meta.MetaDataAware;
import com.draagon.meta.ValueException;
import com.draagon.meta.ValueNotFoundException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.Validatable;

/**
 * This can be useful for Pojos to extend and implement the MetaDataAware
 *     and Validatable interfaces
 */
public abstract class PojoObject implements MetaObjectAware, Validatable {

    private final MetaObject metaObject;

    protected PojoObject(MetaObject mo ) {
        metaObject = mo;
    }

    @Override
    public MetaObject getMetaData() {
        return metaObject;
    }

    @Override
    public void setMetaData(MetaObject metaObject) {
        metaObject = metaObject;
    }

    @Override
    public void validate() throws ValueException {
        if ( metaObject == null ) throw new ValueNotFoundException( "No MetaObject is associated with this object" );
    }

    protected MetaDataLoader _getLoader() {
        return metaObject.getLoader();
    }
}
