package com.draagon.meta.loader.types;

import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.Validatable;

import java.util.List;

public interface ChildConfig extends ChildConfigLogic, Validatable, MetaObjectAware {

    public final static String OBJECT_NAME          = "ChildConfig";
    public final static String OBJECT_IONAME        = "child";
    public final static String FIELD_TYPE           = "type";
    public final static String FIELD_SUBTYPE        = "subType";
    public final static String FIELD_NAME           = "name";
    public final static String FIELD_NAMEALIASES    = "nameAliases";

    ///////////////////////////////////////////////////////////
    // GETTERS

    public String getType();
    public void setType(String type);

    public String getSubType();
    public void setSubType(String subType);

    public String getName();
    public void setName(String name);

    public List<String> getNameAliases();
    public void setNameAliases(List<String> nameAliases);

}
