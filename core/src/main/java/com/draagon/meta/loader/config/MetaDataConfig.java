package com.draagon.meta.loader.config;

import com.draagon.meta.MetaData;

import java.util.Collection;
import java.util.TreeMap;

/**
 * Stores the MetaData Configuration data
 */
public class MetaDataConfig {

    private final TreeMap<String,TypeConfig> types;

    public MetaDataConfig() {
        types = new TreeMap<>();
    }

    /////////////////////////////////////////////////////////////////////
    // Type Configuration Methods

    public TypeConfig createTypeConfig( String typeName, Class<? extends MetaData> clazz ) {

        if ( typeName == null ) throw new NullPointerException( "Cannot create a TypeConfig with a null name and class [" + clazz + "]" );
        if ( clazz == null ) throw new NullPointerException( "Cannot create TypeConfig for type [" +typeName + "] with a null Class" );

        return addTypeConfig( typeName, new TypeConfig( typeName, clazz ) );
    }

    public TypeConfig addTypeConfig( String typeName, TypeConfig typeConfig ) {

        if ( typeName == null ) throw new NullPointerException( "Cannot add TypeConfig with a null name" );
        if ( typeConfig == null ) throw new NullPointerException( "Cannot add TypeConfig [" +typeName + "] with a null value" );

        if ( types.containsKey( typeName )) throw new IllegalArgumentException( "MetaData Type [" + typeName + "] already exists as class [" + types.get(typeName).getBaseClass() + "]" );

        types.put( typeName, typeConfig );

        return typeConfig;
    }

    public TypeConfig getTypeConfig( String name ) {
        return types.get( name );
    }

    public Collection<String> getTypeNames() {
        return types.keySet();
    }

    public Collection<TypeConfig> getTypes() {
        return types.values();
    }

    /////////////////////////////////////////////////////////////////////
    // Misc Methods

    public String toString() {
        return "MetaDataConfig - Types: " + types.toString();
    }
}
