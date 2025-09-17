package com.draagon.meta.object.mapped;

import com.draagon.meta.ValueException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.model.MetaModel;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.Validatable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MappedObject extends HashMap<String,Object> implements MetaObjectAware, Validatable, MetaModel {

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
    
    // MetaModel interface implementation
    @Override
    public String getPackage() {
        return (String) get(MetaModel.FIELD_PACKAGE);
    }
    
    @Override
    public void setPackage(String pkg) {
        put(MetaModel.FIELD_PACKAGE, pkg);
    }
    
    @Override
    public String getType() {
        return (String) get(MetaModel.FIELD_TYPE);
    }
    
    @Override
    public void setType(String type) {
        put(MetaModel.FIELD_TYPE, type);
    }
    
    @Override
    public String getSubType() {
        return (String) get(MetaModel.FIELD_SUBTYPE);
    }
    
    @Override
    public void setSubType(String subType) {
        put(MetaModel.FIELD_SUBTYPE, subType);
    }
    
    @Override
    public String getName() {
        return (String) get(MetaModel.FIELD_NAME);
    }
    
    @Override
    public void setName(String name) {
        put(MetaModel.FIELD_NAME, name);
    }
    
    @Override
    public String getSuper() {
        return (String) get(MetaModel.FIELD_SUPER);
    }
    
    @Override
    public void setSuper(String superStr) {
        put(MetaModel.FIELD_SUPER, superStr);
    }
    
    @Override
    public String getValue() {
        return (String) get(MetaModel.FIELD_VALUE);
    }
    
    @Override
    public void setValue(String value) {
        put(MetaModel.FIELD_VALUE, value);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<MetaModel> getChildren() {
        return (List<MetaModel>) get(MetaModel.FIELD_CHILDREN);
    }
    
    @Override
    public void setChildren(List<MetaModel> children) {
        put(MetaModel.FIELD_CHILDREN, children);
    }
}
