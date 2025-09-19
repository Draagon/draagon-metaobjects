package com.draagon.meta.object.mapped;

import com.draagon.meta.ValueException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.Validatable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MappedObject extends HashMap<String,Object> implements MetaObjectAware, Validatable {

    private final MetaObject metaObject;

    public MappedObject( MetaObject metaObject ) {
        this.metaObject = metaObject;
    }

    @Override
    public MetaObject getMetaData() {
        return metaObject;
    }

    @Override
    public void setMetaData(MetaObject metaObject) {
        throw new IllegalStateException("Cannot set MetaData after newInstance");
    }

    @Override
    public void validate() throws ValueException {
        // Do nothing
    }

    protected MetaDataLoader _getLoader() {
        return metaObject.getLoader();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MappedObject that = (MappedObject) o;
        for(MetaField f : getMetaData().getMetaFields()) {
            String n = f.getName();
            if ( get(n) == null && that.get(n) != null ||
                    (get(n) != null && !get(n).equals(that.get(n)))) return false;
        }
        return Objects.equals(metaObject, that.metaObject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.entrySet().hashCode(), metaObject);
    }

    @Override
    public String toString() {
        return "MappedObject{" +
                "metaObject=" + metaObject +
                "map=" + super.toString() +
                '}';
    }
}
