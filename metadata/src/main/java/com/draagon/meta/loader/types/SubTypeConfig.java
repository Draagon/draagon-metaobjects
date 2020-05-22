package com.draagon.meta.loader.types;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataAware;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.Validatable;

import java.util.List;

/** Used to store the MetaData Type config and respective SubTypes and their classes */
public interface SubTypeConfig extends Validatable, MetaObjectAware {

    public final static String OBJECT_NAME              = "SubTypeConfig";
    public final static String OBJECT_IONAME            = "subType";
    public final static String FIELD_NAME               = "name";
    public final static String FIELD_BASECLASS          = "baseClass";
    public final static String FIELD_SUBTYPECHILDREN    = "childConfigs";
    public final static String OBJREF_CHILD             = "childRef";

    /////////////////////////////////////////////////////////////////////
    // Type  Methods

    public String getName();

    public void setName(String name);

    public Class<? extends MetaData> getBaseClass();

    public void setBaseClass( Class<? extends MetaData> clazz ) ;

    public List<ChildConfig> getChildConfigs();

    public void setChildConfigs( List<ChildConfig> children );
}
