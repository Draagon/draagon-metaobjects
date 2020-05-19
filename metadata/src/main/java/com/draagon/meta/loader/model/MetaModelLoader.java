package com.draagon.meta.loader.model;

import com.draagon.meta.loader.LoaderOptions;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.types.TypesConfig;

public class MetaModelLoader extends MetaDataLoader {

    public final static String SUBTYPE_METADATAMODEL = "metaDataModel";

    public MetaModelLoader(String name ) {
        super( LoaderOptions.create( false, false, true),
                SUBTYPE_METADATAMODEL, name );
    }

    public static MetaModelLoader create(String name ) {
        return new MetaModelLoader( name ).init();
    }

    public static MetaModelLoader create(String name, TypesConfig typesConfig ) {
        return new MetaModelLoader( name ).setTypesConfig( typesConfig ).init();
    }

    public MetaModelLoader setTypesConfig(TypesConfig typesConfig ) {
        super.setTypesConfig( typesConfig );
        return this;
    }

    @Override
    public MetaModelLoader init() {
        super.init();

        if ( getTypesConfig() == null )
            MetaModelBuilder.buildDefaultMetaDataModels( this );
        else
            MetaModelBuilder.buildMetaDataModels( this, getTypesConfig() );

        return this;
    }

    public MetaModel newMetaDataModel() {
        return (MetaModel) getMetaObjectByName( MetaModel.OBJECT_NAME ).newInstance();
    }
}
