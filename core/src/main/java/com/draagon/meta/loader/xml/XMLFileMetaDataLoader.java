/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.loader.xml;

import com.draagon.meta.loader.file.FileMetaDataLoader;
import com.draagon.meta.loader.file.LocalFileMetaDataSources;
import com.draagon.meta.loader.file.FileLoaderOptions;
import com.draagon.meta.loader.file.xml.XMLMetaDataParser;


/**
 * Meta Class loader for XML files
 */
public class XMLFileMetaDataLoader extends FileMetaDataLoader {

    private String typesRef = null;
    private String sourceDir = null;

    public XMLFileMetaDataLoader() {
        this( "file-" + System.currentTimeMillis());
    }

    public XMLFileMetaDataLoader( String name ) {

        super( new FileLoaderOptions()
                    .addParser( "*.xml", XMLMetaDataParser.class )
                    .setShouldRegister( false )
                    .setAllowAutoAttrs( true )
                    .setStrict( false )
                    .setVerbose( false ),
                name );
    }

    public String getDefaultTypesRef() {
        return "com/draagon/meta/loader/xml/metaobjects.types.xml";
    }

    public void setSourceDir( String dir ) {
        sourceDir = dir;
        if ( !sourceDir.endsWith( "/" )) sourceDir += "/";
    }

    public String getSourceDir() {
        return sourceDir;
    }

    public void addSources( MetaDataSources sources ) {
        getLoaderOptions().addSources( sources );
    }

    /** Initialize with the metadata source being set */
    public XMLFileMetaDataLoader init( MetaDataSources sources ) {
        return init( sources, false );
    }

    /** Initialize with the metadata source being set */
    public XMLFileMetaDataLoader init( MetaDataSources sources, boolean shouldRegister ) {

        FileLoaderOptions options = getLoaderOptions();
        options.setShouldRegister( shouldRegister );

        // Prepend the Types XML to load using the default behavior of the original XMLFileMetaDataLoader
        if ( getDefaultTypesRef() != null ) {
            options.addSources( new LocalFileMetaDataSources( getMetaDataClassLoader(), getDefaultTypesRef()) );
        }

        // Add the Loader's ClassLoader source, which includes what is set by the MojoSupport
        sources.setLoaderClassLoader( getMetaDataClassLoader() );

        // Initialize with the specified sources
        return (XMLFileMetaDataLoader) super.init( sources );
    }
}