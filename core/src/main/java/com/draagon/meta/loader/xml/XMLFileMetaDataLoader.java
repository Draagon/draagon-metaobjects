/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.loader.xml;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.file.FileMetaDataLoader;
import com.draagon.meta.loader.file.LocalMetaDataSources;
import com.draagon.meta.loader.file.MetaDataParser;
import com.draagon.meta.loader.file.config.FileLoaderConfig;
import com.draagon.meta.loader.file.xml.XMLMetaDataParser;
import com.draagon.meta.object.MetaObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Meta Class loader for XML files
 *
 * @deprecated Use com.draagon.meta.loader.FileMetaDataLoader
 */
public class XMLFileMetaDataLoader extends FileMetaDataLoader {

    private String typesRef = null;
    private String sourceDir = null;

    public XMLFileMetaDataLoader() {
        this( "file-" + System.currentTimeMillis());
    }

    public XMLFileMetaDataLoader( String name ) {

        super( new FileLoaderConfig()
                    .addParser( "*.xml", XMLMetaDataParser.class )
                    .setShouldRegister( false ),
                name );
    }

    public String getDefaultTypesRef() {
        return "com/draagon/meta/loader/meta.types.xml";
    }

    public void setSourceDir( String dir ) {
        sourceDir = dir;
        if ( !sourceDir.endsWith( "/" )) sourceDir += "/";
    }

    public String getSourceDir() {
        return sourceDir;
    }

    public void addSources( MetaDataSources sources ) {
        getFileConfig().addSources( sources );
    }

    public void setTypesRef(String types) {
        if ( types.isEmpty() ) types = null;
        typesRef = types;
    }

    public String getTypesRef() {
        return typesRef;
    }

    /** Initialize with the metadata source being set */
    public MetaDataLoader init( MetaDataSources sources ) {
        return init( sources, false );
    }

    /** Initialize with the metadata source being set */
    public MetaDataLoader init( MetaDataSources sources, boolean shouldRegister ) {

        FileLoaderConfig config = getFileConfig();
        config.setShouldRegister( shouldRegister );

        // Prepend the Types XML to load using the default behavior of the original XMLFileMetaDataLoader
        if ( getTypesRef() != null ) {
            config.addSources( new LocalMetaDataSources( getTypesRef()) );
        } else if ( getDefaultTypesRef() != null ) {
            config.addSources( new LocalMetaDataSources( getDefaultTypesRef()) );
        }

        return super.init( sources );
    }
}