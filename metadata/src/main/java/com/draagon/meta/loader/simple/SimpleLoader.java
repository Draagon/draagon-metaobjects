package com.draagon.meta.loader.simple;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.loader.LoaderOptions;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.config.TypesConfig;
import com.draagon.meta.loader.config.TypesConfigLoader;
import com.draagon.meta.loader.model.MetaDataModel;
import com.draagon.meta.loader.model.MetaDataModelLoader;

import java.io.*;

public class SimpleLoader extends MetaDataLoader {

    public final static String SIMPLE_TYPES_XML = "com/draagon/meta/loader/simple/simple.types.xml";
    public final static String SUBTYPE_SIMPLE = "simple";

    private static String sourceData = null;
    private final TypesConfigLoader typesLoader;

    public SimpleLoader(String name) {
        super(LoaderOptions.create( false, false, true), SUBTYPE_SIMPLE, name );

        // Set the TypesConfigLoader and a new TypesConfig
        typesLoader = TypesConfigLoader.create();
        setTypesConfig( typesLoader.newTypesConfig() );
    }

    public void setSourceData( String sourceData ) {
        this.sourceData = sourceData;
    }

    public SimpleLoader init() {

        if ( sourceData == null ) throw new MetaDataException( "No sourceData was specified" );

        super.init();

        // Load TypesConfig
        SimpleTypesParser simpleTypesParser = new SimpleTypesParser( typesLoader, SIMPLE_TYPES_XML );
        simpleTypesParser.loadAndMerge( this, SIMPLE_TYPES_XML);

        // Load MetaData
        MetaDataModelLoader modelLoader = MetaDataModelLoader.create( "simple", getTypesConfig() );
        SimpleModelParser simpleModelParser = new SimpleModelParser( modelLoader, sourceData );
        simpleModelParser.loadAndMerge( this, sourceData );
        //TODO: Finish me

        return this;
    }

    protected InputStream getResourceInputStream(String resource) throws FileNotFoundException {

        InputStream is = null;
        File f = new File(resource);
        if (!f.exists()) {
            is = this.getClass().getClassLoader().getResourceAsStream(resource);
        } else {
            is = new FileInputStream(f);
        }
        return is;
    }
}
