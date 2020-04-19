package com.draagon.meta.loader.config;

import com.draagon.meta.MetaData;

import java.util.Collection;
import java.util.TreeMap;

/**
 * Stores the MetaData Configuration data
 */
public class MetaDataTypes {

    private final TreeMap<String, TypeModel> types;

    public MetaDataTypes() {
        types = new TreeMap<>();
    }

    /////////////////////////////////////////////////////////////////////
    // Type Configuration Methods

    public TypeModel createType(String typeName, Class<? extends MetaData> clazz ) {

        if ( typeName == null ) throw new NullPointerException( "Cannot create a TypeModel with a null name and class [" + clazz + "]" );
        if ( clazz == null ) throw new NullPointerException( "Cannot create TypeModel for type [" +typeName + "] with a null Class" );

        return addType( typeName, new TypeModel( typeName, clazz ) );
    }

    public TypeModel addType(String typeName, TypeModel typeModel) {

        if ( typeName == null ) throw new NullPointerException( "Cannot add TypeModel with a null name" );
        if ( typeModel == null ) throw new NullPointerException( "Cannot add TypeModel [" +typeName + "] with a null value" );

        if ( types.containsKey( typeName )) throw new IllegalArgumentException( "MetaData Type [" + typeName + "] already exists as class [" + types.get(typeName).getBaseClass() + "]" );

        types.put( typeName, typeModel);

        return typeModel;
    }

    public TypeModel getType(String name ) {
        return types.get( name );
    }

    public Collection<String> getTypeNames() {
        return types.keySet();
    }

    public Collection<TypeModel> getTypes() {
        return types.values();
    }

    /////////////////////////////////////////////////////////////////////
    // Misc Methods

    public String toString() {
        return "MetaDataTypes: " + types.toString();
    }
}
