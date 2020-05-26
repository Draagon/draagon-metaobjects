package com.draagon.meta.loader.types;

import com.draagon.meta.*;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.Validatable;

import java.util.*;

/** Used to store the MetaData Type config and respective SubTypes and their classes */
public interface TypeConfigLogic {

    /////////////////////////////////////////////////////////////////////
    // Helper methods

    public void addTypeChildConfig( ChildConfig config );

    public Collection<String> getSubTypeNames();

    public SubTypeConfig getSubType( String name );

    public void addSubTypeConfig( SubTypeConfig subType );

    public void addSubTypeConfig(String subtypeName, String baseClass );

    public Class<? extends MetaData> getDefaultTypeClass();


    /////////////////////////////////////////////////////////////////////
    // TODO:  Replace all of these once the new version exists on the core side

    public void merge(TypeConfig tc);

    public ChildConfig getBestMatchChildConfig( List<ChildConfig> children, String type, String subType, String name );

    public List<ChildConfig> getSubTypeChildConfigs( String subType );

    public void addSubTypeChild( String subTypeName, ChildConfig config );

    public Class<? extends MetaData> getSubTypeClass( String subTypeName );

    public ChildConfig createChildConfig(String type, String subType, String name);
}
