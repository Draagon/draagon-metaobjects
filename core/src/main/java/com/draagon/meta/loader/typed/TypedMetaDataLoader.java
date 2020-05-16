package com.draagon.meta.loader.typed;

import com.draagon.meta.loader.LoaderOptions;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.typed.config.MetaDataConfig;

public class TypedMetaDataLoader extends MetaDataLoader {

    private final MetaDataConfig metaDataConfig = new MetaDataConfig();

    public final static String SUBTYPE_CONFIGURED = "configured";

    public TypedMetaDataLoader(LoaderOptions loaderConfig, String subtype) {
        super(loaderConfig, subtype);
    }

    public TypedMetaDataLoader(LoaderOptions loaderConfig, String subtype, String name ) {
        super(loaderConfig, subtype, name);
    }


    /**
     * Manually construct a MetaDataLoader.  Usually used for unit testing.
     * @param name The name of the Manually create MetaDataLoader
     * @return The created MetaDataLoader
     */
    public static TypedMetaDataLoader createManual( String name ) {
        return new TypedMetaDataLoader(
                LoaderOptions.create( true, false),
                SUBTYPE_CONFIGURED, name );
    }

    /** Return the MetaData Configuration */
    public MetaDataConfig getMetaDataConfig() {
        return metaDataConfig;
    }

    @Override
    public void validate() {

        super.validate();

        // Validate the metadata configuration
        metaDataConfig.validate();
    }
}
