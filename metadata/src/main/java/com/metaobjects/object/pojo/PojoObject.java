package com.metaobjects.object.pojo;

import com.metaobjects.MetaDataAware;
import com.metaobjects.ValueException;
import com.metaobjects.ValueNotFoundException;
import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.object.MetaObject;
import com.metaobjects.object.MetaObjectAware;
import com.metaobjects.object.Validatable;

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
