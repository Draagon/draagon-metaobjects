package com.draagon.meta.loader.model;

import com.draagon.meta.loader.LoaderOptions;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.types.TypesConfig;
import com.draagon.meta.loader.types.TypesConfigLoader;

public class MetaModelLoader extends MetaDataLoader {

    public final static String SUBTYPE_METADATAMODEL = "metaDataModel";

    public MetaModelLoader(String name ) {
        super( LoaderOptions.create( false, false, true),
                SUBTYPE_METADATAMODEL, name );
    }

    public static MetaModelLoader create( ClassLoader classLoader, String name ) {
        return create( classLoader, name, null );
    }

    public static MetaModelLoader create( ClassLoader classLoader, String name, TypesConfigLoader typesLoader ) {
        MetaModelLoader loader = new MetaModelLoader( name );
        loader.setMetaDataClassLoader( classLoader );
        if (typesLoader != null) loader.setTypesLoader( typesLoader );
        loader.init();
        return loader;
    }

    /** Override this for custom MetaDataModels */
    protected void generatedAndAddMetaModels() {
        addChild( getTypesConfig().getGeneratedMetaModel() );
    }

    @Override
    public MetaModelLoader init() {
        super.init();
        generatedAndAddMetaModels();
        return this;
    }

    public MetaModel newMetaDataModel() {
        return (MetaModel) getMetaObjectByName( MetaModel.OBJECT_NAME).newInstance();
    }
}
