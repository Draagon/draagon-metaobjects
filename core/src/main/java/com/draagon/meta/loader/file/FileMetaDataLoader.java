/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.loader.file;

import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.config.TypesConfigLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.List;
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
        return (FileLoaderOptions) super.getloaderOptions();
    }

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
