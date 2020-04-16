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
import com.draagon.meta.MetaException;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.file.FileMetaDataLoader;
import com.draagon.meta.loader.file.MetaDataReader;
import com.draagon.meta.loader.file.MetaDataSources;
import com.draagon.meta.loader.file.json.JsonMetaDataReader;
import com.draagon.meta.loader.file.xml.XMLMetaDataReader;
import com.draagon.meta.object.MetaObjectNotFoundException;
import com.draagon.meta.util.MetaDataUtil;
import com.draagon.meta.util.xml.XMLFileReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.draagon.meta.util.MetaDataUtil.expandPackageForPath;


/**
 * Meta Class loader for XML files
 *
 * @deprecated Use com.draagon.meta.loader.FileMetaDataLoader
 */
public class XMLFileMetaDataLoader extends FileMetaDataLoader {

    public XMLFileMetaDataLoader() {
        super();
    }

    public XMLFileMetaDataLoader( String name ) {
        super( name );
    }

    /** Load the MetaDataReader for the specified file */
    protected MetaDataReader getReaderForFile(String file ) {

        if ( file.endsWith( ".xml" ))
            return new XMLMetaDataReader( this, file );
        else
            throw new MetaDataException( "There is no MetaDataReader supporting the file: " + file );
    }
}
