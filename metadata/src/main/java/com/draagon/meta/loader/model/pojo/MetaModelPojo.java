package com.draagon.meta.loader.model.pojo;

import com.draagon.meta.InvalidValueException;
import com.draagon.meta.ValueException;
import com.draagon.meta.ValueNotFoundException;
import com.draagon.meta.loader.model.MetaModel;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.pojo.PojoObject;

import java.util.List;
import java.util.Objects;

public class MetaModelPojo extends PojoObject implements MetaModel, MetaObjectAware {

    private String pkg;
    private String type;
    private String subType;
    private String name;
    private String superRef;
    private List<MetaModel> children;

    public MetaModelPojo( MetaObject mo ) {
        super( mo );
    }

    @Override
    public String getPackage() {
        return pkg;
    }

    public void setPackage(String pkg) {
        this.pkg = pkg;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getSuper() {
        return superRef;
    }

    public void setSuper(String superRef) {
        this.superRef = superRef;
    }

    @Override
    public List<MetaModel> getChildren() {
        return children;
    }

    public void setChildren(List<MetaModel> children) {
        this.children = children;
    }

    @Override
    public void validate() throws ValueException {
        super.validate();
        if ( getType() == null || !getType().equals(MetaModel.OBJECT_NAME )) {
            if (getName() == null && getType() == null) {
                throw new InvalidValueException("Both name and type cannot be null");
            }
            if (getName() == null && getSuper() == null) {
                throw new InvalidValueException("Both name and super cannot be null");
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaModelPojo that = (MetaModelPojo) o;
        return Objects.equals(pkg, that.pkg) &&
                Objects.equals(type, that.type) &&
                Objects.equals(subType, that.subType) &&
                Objects.equals(name, that.name) &&
                Objects.equals(superRef, that.superRef) &&
                Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkg, type, subType, name, superRef, children);
    }

    @Override
    public String toString() {
        return "MetaModelPojo{" +
                "pkg='" + pkg + '\'' +
                ", type='" + type + '\'' +
                ", subType='" + subType + '\'' +
                ", name='" + name + '\'' +
                ", superRef='" + superRef + '\'' +
                '}';
    }
}
