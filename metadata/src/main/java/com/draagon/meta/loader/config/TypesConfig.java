package com.draagon.meta.loader.config;

import com.draagon.meta.InvalidValueException;
import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;

import java.util.*;

/**
 * Stores the MetaData Configuration data
 */
public class TypesConfig extends ConfigObjectBase {

    public final static String OBJECT_NAME      = "TypesConfig";
    public final static String OBJECT_IONAME   = "typesConfig";
    public final static String FIELD_TYPES      = "types";
    public final static String OBJREF_TYPE      = "typeRef";

    public TypesConfig() {
        super( TypesConfigBuilder.createTypesConfig() );
    }

    public TypesConfig( MetaObject mo ) {
        super( mo );
    }

    public static TypesConfig create( MetaDataLoader loader) {
        return _newInstance( TypesConfig.class, loader, OBJECT_NAME );
    }

    /////////////////////////////////////////////////////////////////////
    // Type Configuration Methods

    public List<TypeConfig> getTypes() {
        return _getAndCreateObjectArray(TypeConfig.class, FIELD_TYPES);
    }

    public TypeConfig createType(String typeName, Class<? extends MetaData> clazz ) {

        if ( typeName == null ) throw new MetaDataException( "Cannot create a TypeModel with a null name and class [" + clazz + "]" );
        if ( clazz == null ) throw new MetaDataException( "Cannot create TypeModel for type [" +typeName + "] with a null Class" );

        TypeConfig tc = new TypeConfig( getMetaData().getLoader().getMetaObjectByName(TypeConfig.OBJECT_NAME));
        tc.setTypeName( typeName );
        tc.setBaseClass( clazz );

        return addType( typeName, tc );
    }

    public TypeConfig addType(String typeName, TypeConfig typeConfig) {

        if ( typeName == null ) throw new MetaDataException( "Cannot add TypeModel with a null name" );
        if ( typeConfig == null ) throw new MetaDataException( "Cannot add TypeModel [" +typeName + "] with a null value" );

        if ( getType( typeName ) != null ) throw new MetaDataException( "MetaData Type [" + typeName + "] "+
                "already exists as class [" + getType(typeName).getBaseClass() + "]" );

        _addToObjectArray( FIELD_TYPES, typeConfig );

        return typeConfig;
    }

    public void addOrMergeType(TypeConfig tc) {
        TypeConfig tc2 = getType( tc.getTypeName() );
        if ( tc2 == null ) {
            _addToObjectArray( FIELD_TYPES, tc );
        } else {
            tc2.merge( tc );
        }
    }

    public TypeConfig getType( String name ) {
        for ( TypeConfig tc : getTypes() ) {
            if ( tc.getTypeName().equals( name )) return tc;
        }
        return null;
    }

    public Collection<String> getTypeNames() {
        List<String> names = new ArrayList<>();
        for ( TypeConfig tc : getTypes() ) {
            names.add( tc.getTypeName() );
        }
        return names;
    }

    //////////////////////////////////////////////////////////////////////
    // Validation Method

    public void validate() {
        super.validate();
        if (getTypes() == null ) throw new InvalidValueException( "types field is null on TypeConfig "+toString() );
        getTypes().forEach( tc -> tc.validate());
    }
}
