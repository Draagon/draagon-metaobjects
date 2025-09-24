package com.draagon.meta.loader.simple;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.loader.LoaderOptions;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.parser.json.JsonMetaDataParser;
import com.draagon.meta.loader.uri.URIHelper;
import com.draagon.meta.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SimpleLoader with unified registry registration for accepting all MetaData types.
 *
 * @version 6.0
 */
public class SimpleLoader extends MetaDataLoader
{
    private static final Logger log = LoggerFactory.getLogger(SimpleLoader.class);

    public final static String SUBTYPE_SIMPLE = "simple";

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.getInstance().registerType(SimpleLoader.class, def -> def
                .type(TYPE_LOADER).subType(SUBTYPE_SIMPLE)
                .description("Simple JSON-based metadata loader")
                
                // LOADER ACCEPTS ALL FIELD TYPES
                .optionalChild("field", "string")
                .optionalChild("field", "int")
                .optionalChild("field", "long")
                .optionalChild("field", "double")
                .optionalChild("field", "float")
                .optionalChild("field", "short")
                .optionalChild("field", "byte")
                .optionalChild("field", "boolean")
                .optionalChild("field", "date")
                .optionalChild("field", "object")
                .optionalChild("field", "class")
                .optionalChild("field", "stringArray")
                .optionalChild("field", "objectArray")
                
                // LOADER ACCEPTS ALL OBJECT TYPES
                .optionalChild("object", "base")
                .optionalChild("object", "pojo")
                .optionalChild("object", "map")
                .optionalChild("object", "proxy")
                
                // LOADER ACCEPTS ALL ATTRIBUTE TYPES
                .optionalChild("attr", "string")
                .optionalChild("attr", "int")
                .optionalChild("attr", "boolean")
            );
            
            log.debug("Registered SimpleLoader type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register SimpleLoader type with unified registry", e);
        }
    }

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
    protected void processSources(String sourceDir, List<String> sourceList) {
        String name = this.getClass().getSimpleName();
        if (sourceList == null) throw new IllegalArgumentException(
                "sourceList was null on setURIList for " + name);

        List<URI> sourceURIs = new ArrayList<>();
        for (String s : sourceList) {
            if (s.indexOf(':') < 0) {
                if (sourceDir != null) {
                    s = "model:file:" + s + ";" + URIHelper.URI_ARG_SOURCEDIR + "=" + sourceDir;
                } else if (new File(s).exists()) {
                    s = "model:file:" + s;
                } else {
                    s = "model:resource:" + s;
                }
            }
            sourceURIs.add(URIHelper.toURI(s));
        }

        setSourceURIs(sourceURIs);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Initialization Methods


    public SimpleLoader init() {

        if ( sourceURIs == null ) throw new MetaDataException( "No sourceData was specified" );

        super.init();

        // Load MetaData using direct JSON parser approach
        for( URI sourceURI : sourceURIs) {
            String filename = sourceURI.toString();
            JsonMetaDataParser jsonParser = new JsonMetaDataParser(this, filename);
            
            try (InputStream is = URIHelper.getInputStream(sourceURI)) {
                jsonParser.loadFromStream(is);
            } catch (IOException e) {
                throw new MetaDataException("Failed to load metadata from [" + filename + "]: " + e.getMessage(), e);
            }
        }

        // Validation is now enforced during construction (constraint system)

        return this;
    }
}
