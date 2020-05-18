package com.draagon.meta.loader.config;

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

    @Override
    public TypesConfigLoader init() {
        super.init();
        TypesConfigBuilder.buildTypesConfig( this );
        return this;
    }

    public TypesConfig newTypesConfig() {
        return (TypesConfig) getMetaObjectByName( TypesConfig.OBJECT_NAME ).newInstance();
    }
}
