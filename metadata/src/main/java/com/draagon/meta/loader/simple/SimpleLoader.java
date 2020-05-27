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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SimpleLoader extends MetaDataLoader implements MojoSupport {

    public final static String SIMPLE_TYPES_XML = "com/draagon/meta/loader/simple/simple.types.xml";
    public final static String SUBTYPE_SIMPLE = "simple";

    private static List<URI> sourceURIs = null;

    public SimpleLoader(String name) {
        super(LoaderOptions.create( false, false, true), SUBTYPE_SIMPLE, name );
    }

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

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Set ClassLoader and Sources

    @Override
    protected ClassLoader getDefaultMetaDataClassLoader() {
        return super.getDefaultMetaDataClassLoader();
    }

    @Override
    public SimpleLoader setMetaDataClassLoader( ClassLoader classLoader ) {
        return super.setMetaDataClassLoader( classLoader );
    }

    public SimpleLoader setSourceURIs( List<URI> sourceData ) {
        this.sourceURIs = sourceData;
        return this;
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
                "sourceList was null on setURIList for " + name);

        List<URI> sourceURIs = new ArrayList<>();
        for( String s : sourceList) {
            if (s.indexOf(':') < 0) {
                if (sourceDir != null) {
                    s = "model:file:" + s + ";" + URIHelper.URI_ARG_SOURCEDIR + "=" + sourceDir;
                }
                else if (new File(s).exists()) {
                    s = "model:file:" + s;
                }
                else {
                    s = "model:resource:" + s;
                }
            }
            sourceURIs.add(URIHelper.toURI(s));
        }

        setSourceURIs(sourceURIs);
    }

    @Override
    public void mojoInit( Map<String, String> args ) {
        if ( args != null ) mojoInitArgs( args );
        init();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Initialization Methods

    public SimpleLoader init() {

        if ( sourceURIs == null ) throw new MetaDataException( "No sourceData was specified" );

        super.init();

        // Load TypesConfig
        //SimpleTypesParser simpleTypesParser = new SimpleTypesParser( typesLoader, SIMPLE_TYPES_XML );
        //simpleTypesParser.loadAndMerge( this, URIHelper.toURI("types:resource:"+SIMPLE_TYPES_XML));

        boolean typesLoaded = false;

        // Load MetaData
        MetaModelLoader modelLoader = MetaModelLoader.create(
                getMetaDataClassLoader(),
                "simple",
                getTypesLoader() );
        for( URI sourceURI : sourceURIs) {

            if (URIHelper.isTypesURI(sourceURI)) {
                SimpleTypesParser simpleTypesParser = new SimpleTypesParser(
                        getTypesLoader(),
                        getMetaDataClassLoader(),
                        sourceURI.toString() );
                simpleTypesParser.loadAndMerge( this, sourceURI);
                typesLoaded = true;
            }
            else {
                if ( !typesLoaded ) {
                    loadDefaultSimpleTypes();
                    typesLoaded = true;
                }

                SimpleModelParser simpleModelParser = new SimpleModelParser(
                        modelLoader, getMetaDataClassLoader(), sourceURI.toString());
                simpleModelParser.loadAndMerge(this, sourceURI);
            }
        }

        // Only occurs if no sourceURIs were loaded
        if ( !typesLoaded ) loadDefaultSimpleTypes();

        // Validate the MetaData
        validate();

        return this;
    }

    protected void loadDefaultSimpleTypes() {
        SimpleTypesParser simpleTypesParser = new SimpleTypesParser(
                getTypesLoader(),
                getMetaDataClassLoader(),
                SIMPLE_TYPES_XML );
        simpleTypesParser.loadAndMerge( this, URIHelper.toURI("types:resource:"+SIMPLE_TYPES_XML));
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
