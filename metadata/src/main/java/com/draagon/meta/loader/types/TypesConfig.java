package com.draagon.meta.loader.types;

import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.types.pojo.TypesConfigPojo;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.Validatable;

import java.util.*;

/**
 * Stores the MetaData Configuration data
 */
public interface TypesConfig extends TypesConfigLogic, Validatable, MetaObjectAware {

    public final static String OBJECT_NAME = "TypesConfig";
    public final static String OBJECT_IONAME = "typesConfig";
    public final static String FIELD_TYPES = "types";
    public final static String OBJREF_TYPE = "typeRef";

    /////////////////////////////////////////////////////////////////////
    // Getter Methods

    public List<TypeConfig> getTypes();

    public void setTypes(List<TypeConfig> types);


    //////////////////////////////////////////////////////////////////////
    //  Helper Methods

    public static TypesConfig create(MetaDataLoader loader) {
        return new TypesConfigPojo( loader.getMetaObjectByName( OBJECT_NAME ));
    }
}
