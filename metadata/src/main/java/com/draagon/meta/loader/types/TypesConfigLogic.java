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
public interface TypesConfigLogic {

    public TypeConfig addType(TypeConfig typeConfig);

    public void addOrMergeType(TypeConfig tc);

    public TypeConfig getTypeByName(String name);

    public Collection<String> getTypeNames();


    //////////////////////////////////////////////////////////////////////
    //  Model Generation Methods

    public MetaData getGeneratedMetaModel();


    /////////////////////////////////////////////////////////////////////
    // TODO:  Replace all of these once the new version exists on the core side

    public TypeConfig createAndAddType(String typeName, Class<? extends MetaData> clazz);
}
