package com.draagon.meta.loader.types;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.loader.LoaderOptions;
import com.draagon.meta.loader.MetaDataLoader;

public class TypesConfigLoader<T extends TypesConfig> extends MetaDataLoader {

    public final static String SUBTYPE_TYPECONFIG = "typesConfig";

    public TypesConfigLoader( String name ) {
        super( LoaderOptions.create( false, false, true),
                SUBTYPE_TYPECONFIG, name );
    }

    public static TypesConfigLoader create( ClassLoader classLoader ) {
        TypesConfigLoader loader = new TypesConfigLoader( SUBTYPE_TYPECONFIG );
        loader.setMetaDataClassLoader( classLoader );
        loader.init();
        return loader;
    }

    /** Override this for custom MetaDataModels */
    protected void generatedAndAddTypes() {
        TypesConfigBuilder.buildDefaultTypesConfig(this);
    }

    protected void initDefaultTypesConfig() {
        // Do Nothing
    }

    @Override
    public TypesConfigLoader init() {
        super.init();
        generatedAndAddTypes();
        return this;
    }

    @Override
    public T getTypesConfig() {
        throw new UnsupportedOperationException("Use newTypesConfig() to get a new TypesConfig on "+getClass().getSimpleName() );
    }

    public <T extends TypesConfig> T newTypesConfig() {
        return (T) getMetaObjectByName( TypesConfig.OBJECT_NAME ).newInstance();
    }
}
