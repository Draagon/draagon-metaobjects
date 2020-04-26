package com.draagon.meta.loader.config;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;

import java.util.Collection;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Stores the MetaData Configuration data
 */
public class TypesConfig {

    private final TreeMap<String, TypeConfig> types;

    public TypesConfig() {
        types = new TreeMap<>();
    }

    /////////////////////////////////////////////////////////////////////
    // Type Configuration Methods

    public TypeConfig createType(String typeName, Class<? extends MetaData> clazz ) {

        if ( typeName == null ) throw new MetaDataException( "Cannot create a TypeModel with a null name and class [" + clazz + "]" );
        if ( clazz == null ) throw new MetaDataException( "Cannot create TypeModel for type [" +typeName + "] with a null Class" );

        return addType( typeName, new TypeConfig( typeName, clazz ) );
    }

    public TypeConfig addType(String typeName, TypeConfig typeConfig) {

        if ( typeName == null ) throw new MetaDataException( "Cannot add TypeModel with a null name" );
        if ( typeConfig == null ) throw new MetaDataException( "Cannot add TypeModel [" +typeName + "] with a null value" );

        if ( types.containsKey( typeName )) throw new MetaDataException( "MetaData Type [" + typeName + "] already exists as class [" + types.get(typeName).getBaseClass() + "]" );

        types.put( typeName, typeConfig);

        return typeConfig;
    }

    public TypeConfig getType(String name ) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypesConfig that = (TypesConfig) o;
        return Objects.equals(types, that.types);
    }

    @Override
    public int hashCode() {
        return Objects.hash(types);
    }

    @Override
    public String toString() {
        return "MetaDataTypes: " + types.toString();
    }
}
