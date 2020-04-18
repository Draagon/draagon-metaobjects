/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.loader.file;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.file.config.FileLoaderConfig;
import com.draagon.meta.loader.file.json.JsonMetaDataParser;
import com.draagon.meta.loader.file.xml.XMLMetaDataParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.*;


/**
 * Meta Class loader for Files
 */
public class FileMetaDataLoader extends MetaDataLoader {

    private static Log log = LogFactory.getLog(FileMetaDataLoader.class);

    private final FileLoaderConfig fileConfig;

    public FileMetaDataLoader( FileLoaderConfig fileConfig, String name ) {
        super( "file", name );
        this.fileConfig = fileConfig;
    }

    public FileLoaderConfig getFileConfig() {
        return fileConfig;
    }

    /** Initialize with the metadata source being set */
    public MetaDataLoader init( MetaDataSources sources ) {
        getFileConfig().addSources( sources );
        return init();
    }

    /** Initialize the MetaDataLoader */
    @Override
    public MetaDataLoader init() {

        if ( getFileConfig().getSources().isEmpty() ) {
            throw new IllegalStateException( "No Metadata Sources defined [" + this + "]" );
        }

        super.init();

        // Load all the source data
        getFileConfig().getSources().forEach( s -> s.getSourceData().forEach( d -> {
            getFileConfig().getParserForFile( this, d.filename)
                    .loadFromStream( new ByteArrayInputStream( d.sourceData.getBytes() ));
        }));

        // Register with the static MetaDataRegistry
        if ( getFileConfig().shouldRegister() ) register();

        return this;
    }

    /**
     * Lookup the specified class by name, include the classloaders provided by the metadata sources
     * NOTE:  This was done to handle OSGi and other complex ClassLoader scenarios
     */
    @Override
    public Class<?> loadClass( String className ) throws ClassNotFoundException {

        for (MetaDataSources s : getFileConfig().getSources() ) {
            try {
                return s.getClass().getClassLoader().loadClass(className);
            } catch( ClassNotFoundException ignore ) {}
        }

        // Use the default class loader
        return super.loadClass( className );
    }
}
