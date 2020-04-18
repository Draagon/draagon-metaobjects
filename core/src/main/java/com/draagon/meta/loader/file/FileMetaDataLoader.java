/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.loader.file;

import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.file.config.FileLoaderConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.List;


/**
 * Meta Class loader for Files
 */
public class FileMetaDataLoader extends MetaDataLoader {

    private static Log log = LogFactory.getLog(FileMetaDataLoader.class);

    public final static String SUBTYPE_FILE = "file";

    public FileMetaDataLoader( FileLoaderConfig fileConfig, String name ) {
        super( fileConfig, SUBTYPE_FILE, name );
    }

    /** Initialize with the metadata source being set */
    public FileMetaDataLoader init( MetaDataSources sources ) {
        getLoaderConfig().addSources( sources );
        return (FileMetaDataLoader) init();
    }

    public FileLoaderConfig getLoaderConfig() {
        return (FileLoaderConfig) super.getLoaderConfig();
    }

    /** Initialize the MetaDataLoader */
    @Override
    public FileMetaDataLoader init() {

        if ( !getLoaderConfig().hasSources() ) {
            throw new IllegalStateException( "No Metadata Sources were defined [" + this + "]" );
        }
        if ( !getLoaderConfig().hasParsers() ) {
            throw new IllegalStateException( "No Metadata Parsers were defined [" + this + "]" );
        }

        super.init();

        // Load all the source data
        List<MetaDataSources> sources = (List<MetaDataSources>) getLoaderConfig().getSources();
        sources.forEach( s -> s.getSourceData().forEach( d -> {
            getLoaderConfig().getParserForFile( this, d.filename)
                    .loadFromStream( new ByteArrayInputStream( d.sourceData.getBytes() ));
        }));

        return this;
    }

    /**
     * Lookup the specified class by name, include the classloaders provided by the metadata sources
     * NOTE:  This was done to handle OSGi and other complex ClassLoader scenarios
     */
    @Override
    public Class<?> loadClass( String className ) throws ClassNotFoundException {

        for (MetaDataSources s : (List<MetaDataSources>) getLoaderConfig().getSources() ) {
            try {
                return s.getClass().getClassLoader().loadClass(className);
            } catch( ClassNotFoundException ignore ) {}
        }

        // Use the default class loader
        return super.loadClass( className );
    }
}
