package com.draagon.meta.loader.model;

import com.draagon.meta.loader.LoaderOptions;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.model.MetaModel;
import com.draagon.meta.registry.MetaDataTypeRegistry;

/**
 * v6.0.0: Updated to use service-based MetaDataTypeRegistry instead of TypesConfig
 */
public class MetaModelLoader extends MetaDataLoader {

    public final static String SUBTYPE_METADATAMODEL = "metaDataModel";

    public MetaModelLoader(String name ) {
        super( LoaderOptions.create( false, false, true),
                SUBTYPE_METADATAMODEL, name );
    }

    public static MetaModelLoader create( ClassLoader classLoader, String name ) {
        return create( classLoader, name, null );
    }

    public static MetaModelLoader create( ClassLoader classLoader, String name, MetaDataTypeRegistry typeRegistry ) {
        MetaModelLoader loader = new MetaModelLoader( name );
        loader.setMetaDataClassLoader( classLoader );
        if (typeRegistry != null) loader.setTypeRegistry( typeRegistry );
        loader.init();
        return loader;
    }

    /** Override this for custom MetaDataModels */
    protected void generatedAndAddMetaModels() {
        // v6.0.0: Generate MetaModel using type registry instead of TypesConfig
        // The registry returns MetaData, but we know it will be a MetaModel type
        addChild( getTypeRegistry().createInstance("metaObject", "metaModel", MetaModel.OBJECT_NAME) );
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
