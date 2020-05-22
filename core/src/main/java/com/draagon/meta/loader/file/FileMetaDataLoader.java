/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.loader.file;

import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.types.TypesConfig;
import com.draagon.meta.loader.types.TypesConfigLoader;
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

    protected final TypesConfigLoader typesLoader;

    public FileMetaDataLoader(FileLoaderOptions fileConfig, String name ) {
        super( fileConfig, SUBTYPE_FILE, name );

        // Create the TypesConfigLoader and set a new TypesConfig
        typesLoader = TypesConfigLoader.create();
        setTypesConfig( typesLoader.newTypesConfig() );
    }

    /** Initialize with the metadata source being set */
    public FileMetaDataLoader init( MetaDataSources sources ) {
        getLoaderOptions().addSources( sources );
        return (FileMetaDataLoader) init();
    }

    public FileLoaderOptions getLoaderOptions() {
        return (FileLoaderOptions) super.getLoaderOptions();
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
            URIMetaDataSources uriSources = new URIMetaDataSources(uriSourceList);
            getLoaderOptions().addSources(uriSources);
        }

        // Set Local Sources
        if (!localSourceList.isEmpty()) {
            LocalMetaDataSources localSources = null;
            if ( sourceDir != null ) localSources = new LocalMetaDataSources(sourceDir,localSourceList);
            else localSources = new LocalMetaDataSources(localSourceList);
            getLoaderOptions().addSources(localSources);
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
        List<MetaDataSources> sources = (List<MetaDataSources>) getLoaderOptions().getSources();
        sources.forEach( s -> s.getSourceData().forEach( d -> {

            log.warn( "LOADING: " + d.filename );
            MetaDataParser p = getLoaderOptions().getParserForFile( this, d.filename);
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

        for (MetaDataSources s : (List<MetaDataSources>) getLoaderOptions().getSources() ) {
            try {
                return s.getClass().getClassLoader().loadClass(className);
            } catch( ClassNotFoundException ignore ) {}
        }

        // Use the default class loader
        return super.loadClass( className );
    }
}
