package com.draagon.meta.loader.model;

import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.Validatable;

import java.util.List;

public interface MetaModel extends MetaObjectAware, Validatable {

    public final static String OBJECT_NAME      = "metadata";
    public final static String FIELD_PACKAGE    = "package";
    public final static String FIELD_TYPE       = "type";
    public final static String FIELD_SUBTYPE    = "subType";
    public final static String FIELD_NAME       = "name";
    public final static String FIELD_SUPER      = "super";
    public final static String FIELD_CHILDREN   = "children";
    public final static String OBJREF_CHILDREF  = "childRef";

    public String getPackage();
    public void setPackage(String pkg);

    public String getType();
    public void setType(String type);

    public String getSubType();
    public void setSubType(String subType);

    public String getName();
    public void setName(String name);

    public String getSuper();
    public void setSuper(String superStr);

    public List<MetaModel> getChildren();
    public void setChildren(List<MetaModel> children);
}
