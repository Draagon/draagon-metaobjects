package com.draagon.meta.loader.types;

import com.draagon.meta.*;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.types.pojo.TypesConfigPojo;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.Validatable;

import java.util.*;

/**
 * Stores the MetaData Configuration data
 */
public interface TypesConfig extends MetaDataAware<MetaObject>, Validatable {

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

    //public TypeConfig addType(TypeConfig typeConfig);

    //public void addOrMergeType(TypeConfig tc);

    //public TypeConfig getTypeByName(String name);

    //public Collection<String> getTypeNames();


    //////////////////////////////////////////////////////////////////////
    //  Model Generation Methods

    /**
     * Generate the MetaModel MetaData
     */
    //public MetaData getGeneratedMetaModel();


    /////////////////////////////////////////////////////////////////////
    // TODO:  Replace all of these once the new version exists on the core side

    //public TypeConfig createAndAddType(String typeName, Class<? extends MetaData> clazz);
}
