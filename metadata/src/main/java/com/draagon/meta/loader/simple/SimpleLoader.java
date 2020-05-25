package com.draagon.meta.loader.simple;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.loader.LoaderOptions;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.types.TypesConfig;
import com.draagon.meta.loader.types.TypesConfigLoader;
import com.draagon.meta.loader.model.MetaModelLoader;
import com.draagon.meta.loader.mojo.MojoSupport;
import com.draagon.meta.loader.uri.URIHelper;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SimpleLoader extends MetaDataLoader implements MojoSupport {

    public final static String SIMPLE_TYPES_XML = "com/draagon/meta/loader/simple/simple.types.xml";
    public final static String SUBTYPE_SIMPLE = "simple";

    private static List<URI> sourceURIs = null;
    private final TypesConfigLoader typesLoader;

    public static SimpleLoader createManual( String name, String resource ) {
        return createManualURIs( name, Arrays.asList(URIHelper.toURI( "model:resource:"+resource)));
    }

    public static SimpleLoader createManual( String name, List<String> resources ) {
        List<URI> uris = new ArrayList<>();
        for (String s : resources) uris.add(URIHelper.toURI("model:resource:"+s));
        return createManualURIs( name, uris);
    }

    public static SimpleLoader createManualURIs( String name, List<URI> uris ) {

        SimpleLoader simpleLoader = new SimpleLoader( name );
        simpleLoader.setSourceURIs(uris);
        simpleLoader.init();
        return simpleLoader;
    }

    public SimpleLoader(String name) {
        super(LoaderOptions.create( false, false, true), SUBTYPE_SIMPLE, name );

        // Set the TypesConfigLoader and a new TypesConfig
        typesLoader = TypesConfigLoader.create();
        setTypesConfig( typesLoader.newTypesConfig() );
    }

    public void setSourceURIs(List<URI> sourceData ) {
        this.sourceURIs = sourceData;
    }
    public List<URI> getSourceURIs() {
        return this.sourceURIs;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // MOJO Support Methods

    @Override
    protected void mojoProcessSources( String sourceDir, List<String> sourceList ) {

        String name = this.getClass().getSimpleName();
        if ( sourceList == null ) throw new IllegalArgumentException(
                "sourceURIList was null on setURIList for " + name);
        if ( sourceList.size() > 1 ) throw new IllegalArgumentException( name +
                " does not support more than one source file");

        List<URI> sourceURIs = new ArrayList<>();
        for( String s : sourceList) {
            if (s.indexOf(':') < 0) {
                if (sourceDir != null) s = "model:file:" + s + ";" + URIHelper.URI_ARG_SOURCEDIR + "=" + sourceDir;
                else if (new File(s).exists()) {
                    s = "model:file:" + s;
                } else {
                    s = "model:resource:" + s;
                }
            }
            sourceURIs.add(URIHelper.toURI(s));
        }

        setSourceURIs(sourceURIs);
    }

    @Override
    public void mojoInit( Map<String, String> args ) {
        mojoInitArgs( args );
        init();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Initialization Methods

    public SimpleLoader init() {

        if ( sourceURIs == null ) throw new MetaDataException( "No sourceData was specified" );

        super.init();

        // Load TypesConfig
        SimpleTypesParser simpleTypesParser = new SimpleTypesParser( typesLoader, SIMPLE_TYPES_XML );
        simpleTypesParser.loadAndMerge( this, URIHelper.toURI("types:resource:"+SIMPLE_TYPES_XML));

        // Load MetaData
        MetaModelLoader modelLoader = MetaModelLoader.create( "simple", getTypesConfig() );
        for( URI sourceURI : sourceURIs) {
            SimpleModelParser simpleModelParser = new SimpleModelParser(modelLoader, sourceURI.toString());
            simpleModelParser.loadAndMerge(this, sourceURI);
        }

        // Validate the MetaData
        validate();

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
