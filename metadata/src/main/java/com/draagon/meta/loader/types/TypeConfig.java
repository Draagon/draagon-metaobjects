package com.draagon.meta.loader.types;

import com.draagon.meta.*;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.Validatable;

import java.util.*;

/** Used to store the MetaData Type config and respective SubTypes and their classes */
public interface TypeConfig extends TypeConfigLogic, Validatable, MetaObjectAware {
    
    public final static String OBJECT_NAME          = "TypeConfig";
    public final static String OBJECT_IONAME        = "type";
    public final static String FIELD_NAME           = "name";
    public final static String FIELD_BASECLASS      = "baseClass";
    public final static String FIELD_IO_CLASS       = "class";
    public final static String FIELD_SUBTYPES       = "subTypes";
    public final static String OBJREF_SUBTYPE       = "subTypeRef";
    public final static String FIELD_DEFSUBTYPE     = "defaultSubType";
    public final static String FIELD_DEFNAME        = "defaultName";
    public final static String FIELD_DEFPREFIX      = "defaultNamePrefix";
    public final static String FIELD_TYPECHILDREN   = "typeChildConfigs";
    public final static String FIELD_IO_CHILDREN    = "children";
    public final static String OBJREF_CHILD         = "childRef";

    /////////////////////////////////////////////////////////////////////
    // Getter  Methods

    public String getName();
    public void setName(String name);

    public Class<? extends MetaData> getBaseClass();
    public void setBaseClass(Class<? extends MetaData> baseClass);

    public Collection<SubTypeConfig> getSubTypes();
    public void setSubTypes(List<SubTypeConfig> subTypes);

    public List<ChildConfig> getTypeChildConfigs();
    public void setTypeChildConfigs(List<ChildConfig> children);


    /////////////////////////////////////////////////////////////////////
    // TODO:  Replace all of these once the new version exists on the core side

    public String getDefaultName();
    public void setDefaultName(String name);

    public String getDefaultNamePrefix();
    public void setDefaultNamePrefix(String namePrefix);

    public String getDefaultSubType();
    public void setDefaultSubType(String subTypeName);
}
