package com.draagon.meta.loader.file;

import com.draagon.meta.MetaData;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.file.json.JsonMetaDataParser;
import com.draagon.meta.loader.file.xml.XMLMetaDataParser;
import com.draagon.meta.loader.uri.URIHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    private static Log log = LogFactory.getLog(FileMetaDataLoader.class);

    public final static String SUBTYPE_FILE = "file";

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
    protected void mojoProcessSources( String sourceDir, List<String> rawSources ) {

        if ( rawSources == null ) throw new IllegalArgumentException(
                "sourceURIList was null on setURIList for Loader: " + toString());

        List<String> localSourceList = new ArrayList<>();
        List<URI> uriSourceList = new ArrayList<>();

        // See if the raw input is a URI or not and add to appropriate list
        for ( String raw: rawSources ) {
            if ( raw.indexOf(":") > 0) uriSourceList.add(URIHelper.toURI(raw));
            else localSourceList.add(raw);
        }

        // Set URI Souces
        if (!uriSourceList.isEmpty()) {
            URIFileMetaDataSources uriSources = new URIFileMetaDataSources(uriSourceList);
            getLoaderOptions().addSources(uriSources);
            uriSources.setLoaderClassLoader( getMetaDataClassLoader() );
        }

        // Set Local Sources
        if (!localSourceList.isEmpty()) {
            LocalFileMetaDataSources localSources = null;
            if ( sourceDir != null ) localSources = new LocalFileMetaDataSources(sourceDir,localSourceList);
            else localSources = new LocalFileMetaDataSources(localSourceList);
            getLoaderOptions().addSources(localSources);
            localSources.setLoaderClassLoader( getMetaDataClassLoader() );
        }
    }

    @Override
    public void mojoInit( Map<String, String> args ) {

        mojoInitArgs( args );

        FileLoaderOptions options = getLoaderOptions()
                .addParser( "*.xml", XMLMetaDataParser.class)
                .addParser( "*.json", JsonMetaDataParser.class);

        init();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Initialization Methods

    /** Initialize the MetaDataLoader */
    @Override
    public FileMetaDataLoader init() {

        if ( !getLoaderOptions().hasSources() ) {
            throw new IllegalStateException( "No Metadata Sources were defined [" + this + "]" );
        }
        if ( !getLoaderOptions().hasParsers() ) {
            throw new IllegalStateException( "No Metadata Parsers were defined [" + this + "]" );
        }

        super.init();

        loadSourceFiles();

        return this;
    }

    protected void loadSourceFiles() {

        AtomicInteger i = new AtomicInteger();

        // Load all the source data
        List<FileMetaDataSources> sources = (List<FileMetaDataSources>) getLoaderOptions().getSources();
        sources.forEach( s -> s.getSourceData().forEach( d -> {

            if ( log.isDebugEnabled() ) log.debug( "LOADING: " + d.filename );
            FileMetaDataParser p = getLoaderOptions().getParserForFile( this, d.filename);
            p.loadFromStream( new ByteArrayInputStream( d.sourceData.getBytes() ));
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
