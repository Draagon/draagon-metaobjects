package com.draagon.meta.loader.simple;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.loader.LoaderOptions;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.types.TypesConfigLoader;
import com.draagon.meta.loader.model.MetaModelLoader;
import com.draagon.meta.loader.mojo.MojoSupport;
import com.draagon.meta.loader.uri.URIHelper;

import java.io.*;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class SimpleLoader extends MetaDataLoader implements MojoSupport {

    public final static String SIMPLE_TYPES_XML = "com/draagon/meta/loader/simple/simple.types.xml";
    public final static String SUBTYPE_SIMPLE = "simple";

    private static URI sourceURI = null;
    private final TypesConfigLoader typesLoader;

    public static SimpleLoader createManual( String name, String resource ) {
        return createManual( name, URIHelper.toURI( "model:resource:"+resource));
    }

    public static SimpleLoader createManual( String name, URI uri ) {

        SimpleLoader simpleLoader = new SimpleLoader( name );
        simpleLoader.setSourceURI( uri );
        simpleLoader.init();
        return simpleLoader;
    }

    public SimpleLoader(String name) {
        super(LoaderOptions.create( false, false, true), SUBTYPE_SIMPLE, name );

        // Set the TypesConfigLoader and a new TypesConfig
        typesLoader = TypesConfigLoader.create();
        setTypesConfig( typesLoader.newTypesConfig() );
    }

    public void setSourceURI(URI sourceData ) {
        this.sourceURI = sourceData;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // MOJO Support Methods

    @Override
    public void mojoSetURISources(List<URI> sourceURIList) {
        String name = this.getClass().getSimpleName();
        if ( sourceURIList == null ) throw new IllegalArgumentException(
                "sourceURIList was null on setURIList for " + name);
        if ( sourceURIList.size() > 1 ) throw new IllegalArgumentException( name +
                " does not support more than one source file");

        setSourceURI( sourceURIList.get(0));
    }

    @Override
    public void mojoInit( Map<String, String> args ) {
        mojoInitArgs( args );
        init();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Initialization Methods

    public SimpleLoader init() {

        if ( sourceURI == null ) throw new MetaDataException( "No sourceData was specified" );

        super.init();

        // Load TypesConfig
        SimpleTypesParser simpleTypesParser = new SimpleTypesParser( typesLoader, SIMPLE_TYPES_XML );
        simpleTypesParser.loadAndMerge( this, URIHelper.toURI("types:resource:"+SIMPLE_TYPES_XML));

        // Load MetaData
        MetaModelLoader modelLoader = MetaModelLoader.create( "simple", getTypesConfig() );
        SimpleModelParser simpleModelParser = new SimpleModelParser( modelLoader, sourceURI.toString() );
        simpleModelParser.loadAndMerge( this, sourceURI);
        //TODO: Finish me

        return this;
    }

    /*protected InputStream getResourceInputStream(String resource) throws FileNotFoundException {

        InputStream is = null;
        File f = new File(resource);
        if (!f.exists()) {
            is = this.getClass().getClassLoader().getResourceAsStream(resource);
        } else {
            is = new FileInputStream(f);
        }
        return is;
    }*/
}
