package com.draagon.meta.loader.file;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.parser.json.JsonMetaDataParser;
import com.draagon.meta.loader.parser.xml.XMLMetaDataParser;
import com.draagon.meta.loader.uri.URIHelper;
import com.draagon.meta.registry.CoreTypeInitializer;
import com.draagon.meta.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Meta Class loader for Files
 */
public class FileMetaDataLoader extends MetaDataLoader {

    private static final Logger log = LoggerFactory.getLogger(FileMetaDataLoader.class);

    public final static String SUBTYPE_FILE = "file";
    
    // File extension constants
    public static final String XML_EXTENSION = "*.xml";
    public static final String JSON_EXTENSION = "*.json";

    // Self-registration with unified registry
    static {
        try {
            MetaDataRegistry.registerType(FileMetaDataLoader.class, def -> def
                .type("loader").subType(SUBTYPE_FILE)
                .description("File-based metadata loader for XML and JSON files")
                .optionalChild("field", "*")
                .optionalChild("object", "*")
                .optionalChild("attr", "*")
                .optionalChild("validator", "*")
                .optionalChild("key", "*")
                .optionalChild("view", "*")
            );
            log.debug("Registered FileMetaDataLoader type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register FileMetaDataLoader type with unified registry", e);
        }
    }

    public FileMetaDataLoader(String name) {
        this( new FileLoaderOptions(), name );
    }

    public FileMetaDataLoader(FileLoaderOptions fileConfig, String name ) {
        super( fileConfig, SUBTYPE_FILE, name );
    }

    /** Initialize with the metadata source being set */
    public FileMetaDataLoader init( FileMetaDataSources sources ) {
        getLoaderOptions().addSources( sources );
        return init();
    }

    public FileLoaderOptions getLoaderOptions() {
        return (FileLoaderOptions) super.getLoaderOptions();
    }

    @Override
    public FileMetaDataLoader setMetaDataClassLoader(ClassLoader classLoader ) {
        return super.setMetaDataClassLoader(classLoader);
    }

    @Override
    protected ClassLoader getDefaultMetaDataClassLoader() {
        if ( log.isWarnEnabled() && !getName().toLowerCase().contains("test"))
            log.warn("A MetaDataClassLoader should have been set on loader: " + toString() );
        return super.getDefaultMetaDataClassLoader();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // MOJO Support Methods

    @Override
    protected void processSources(String sourceDir, List<String> rawSources) {
        if (rawSources == null) throw new IllegalArgumentException(
                "sourceURIList was null on setURIList for Loader: " + toString());

        List<String> localSourceList = new ArrayList<>();
        List<URI> uriSourceList = new ArrayList<>();

        // See if the raw input is a URI or not and add to appropriate list
        for (String raw : rawSources) {
            if (raw.indexOf(":") > 0) uriSourceList.add(URIHelper.toURI(raw));
            else localSourceList.add(raw);
        }

        // Set URI Sources
        if (!uriSourceList.isEmpty()) {
            URIFileMetaDataSources uriSources = new URIFileMetaDataSources(uriSourceList);
            getLoaderOptions().addSources(uriSources);
            uriSources.setLoaderClassLoader(getMetaDataClassLoader());
        }

        // Set Local Sources
        if (!localSourceList.isEmpty()) {
            LocalFileMetaDataSources localSources = null;
            if (sourceDir != null) localSources = new LocalFileMetaDataSources(sourceDir, localSourceList);
            else localSources = new LocalFileMetaDataSources(localSourceList);
            getLoaderOptions().addSources(localSources);
            localSources.setLoaderClassLoader(getMetaDataClassLoader());
        }
    }

    @Override
    public void configure(LoaderConfiguration config) {
        // Process configuration arguments first
        processArguments(config.getArguments());

        // Call parent to handle the rest of the configuration
        super.configure(config);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Initialization Methods

    /** Initialize the MetaDataLoader */
    @Override
    public FileMetaDataLoader init() {

        if ( !getLoaderOptions().hasSources() ) {
            throw new IllegalStateException( "No Metadata Sources were defined [" + this + "]" );
        }

        // Ensure all core types are loaded and registered
        CoreTypeInitializer.initializeCoreTypes();

        super.init();

        loadSourceFiles();

        return this;
    }

    protected void loadSourceFiles() {

        AtomicInteger i = new AtomicInteger();

        // Load all the source data using direct parser selection
        List<FileMetaDataSources> sources = (List<FileMetaDataSources>) getLoaderOptions().getSources();
        sources.forEach( s -> s.getSourceData().forEach( d -> {

            if ( log.isDebugEnabled() ) log.debug( "LOADING: " + d.filename );
            
            // Direct parser selection based on file extension
            if (d.filename.endsWith(".json")) {
                JsonMetaDataParser parser = new JsonMetaDataParser(this, d.filename);
                parser.loadFromStream(new ByteArrayInputStream(d.sourceData.getBytes()));
            } else if (d.filename.endsWith(".xml")) {
                XMLMetaDataParser parser = new XMLMetaDataParser(this, d.filename);
                parser.loadFromStream(new ByteArrayInputStream(d.sourceData.getBytes()));
            } else if (!d.filename.endsWith(".bundle")) {
                // Bundle files are handled by FileMetaDataSources itself, ignore here
                throw new MetaDataException("Unsupported file type: " + d.filename + 
                    ". Supported types: .json, .xml, .bundle");
            }
            
            i.getAndIncrement();
        }));

        if ( getLoaderOptions().isVerbose() ) {
            log.info( "METADATA - ("+i+") Source Files Loaded in " +toString() );
        }
    }

    /**
     * Lookup the specified class by name, include the classloaders provided by the metadata sources
     * NOTE:  This was done to handle OSGi and other complex ClassLoader scenarios
     */
    @Override
    public Class<?> loadClass( String className ) throws ClassNotFoundException {

        for (FileMetaDataSources s : (List<FileMetaDataSources>) getLoaderOptions().getSources() ) {
            try {
                return s.getClass().getClassLoader().loadClass(className);
            } catch( ClassNotFoundException ignore ) {}
        }

        // Use the default class loader
        return super.loadClass( className );
    }
}
