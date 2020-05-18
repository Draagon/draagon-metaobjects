package com.draagon.meta.loader.model;

import com.draagon.meta.loader.LoaderOptions;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.config.TypesConfig;

public class MetaDataModelLoader extends MetaDataLoader {

    public final static String SUBTYPE_METADATAMODEL = "metaDataModel";

    public MetaDataModelLoader( String name ) {
        super( LoaderOptions.create( false, false, true),
                SUBTYPE_METADATAMODEL, name );
    }

    public static MetaDataModelLoader create( String name ) {
        return new MetaDataModelLoader( name ).init();
    }

    public static MetaDataModelLoader create( String name, TypesConfig typesConfig ) {
        return new MetaDataModelLoader( name ).setTypesConfig( typesConfig ).init();
    }

    public MetaDataModelLoader setTypesConfig( TypesConfig typesConfig ) {
        super.setTypesConfig( typesConfig );
        return this;
    }

    @Override
    public MetaDataModelLoader init() {
        super.init();

        if ( getTypesConfig() == null )
            MetaDataModelBuilder.buildDefaultMetaDataModels( this );
        else
            MetaDataModelBuilder.buildMetaDataModels( this, getTypesConfig() );

        return this;
    }

    public MetaDataModel newMetaDataModel() {
        return (MetaDataModel) getMetaObjectByName( MetaDataModel.OBJECT_NAME ).newInstance();
    }
}
