package com.draagon.meta.loader.types;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.loader.LoaderOptions;
import com.draagon.meta.loader.MetaDataLoader;

public class TypesConfigLoader extends MetaDataLoader {

    public final static String SUBTYPE_TYPECONFIG = "typesConfig";

    public TypesConfigLoader( String name ) {
        super( LoaderOptions.create( false, false, true),
                SUBTYPE_TYPECONFIG, name );
    }

    public static TypesConfigLoader create() {
        return new TypesConfigLoader( SUBTYPE_TYPECONFIG ).init();
    }

    /** Override this for custom MetaDataModels */
    protected void generatedAndAddTypes() {
        TypesConfigBuilder.buildDefaultTypesConfig(this);
    }


    @Override
    public TypesConfigLoader init() {
        super.init();
        generatedAndAddTypes();
        return this;
    }

    @Override
    public TypesConfig getTypesConfig() {
        throw new UnsupportedOperationException("Use newTypesConfig() to get a new TypesConfig on "+getClass().getSimpleName() );
    }

    public TypesConfig newTypesConfig() {
        return (TypesConfig) getMetaObjectByName( TypesConfig.OBJECT_NAME ).newInstance();
    }
}
