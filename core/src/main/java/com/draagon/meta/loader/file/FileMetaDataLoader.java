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
import com.draagon.meta.loader.file.json.JsonMetaDataReader;
import com.draagon.meta.loader.file.xml.XMLMetaDataReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.*;


/**
 * Meta Class loader for XML files
 */
public class FileMetaDataLoader extends MetaDataLoader {

    private static Log log = LogFactory.getLog(FileMetaDataLoader.class);

    private String typesRef = null;
    private boolean typesLoaded = false;
    private String sourceDir = null;
    private final List<MetaDataSources> sources = new ArrayList<MetaDataSources>();

    public FileMetaDataLoader() {
        this( "file-" + System.currentTimeMillis() );
    }

    public FileMetaDataLoader(String name ) {
        super( "file", name );
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
        this.sources.add( sources );
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
        addSources( sources );
        return init();
    }

    /** Initialize with the metadata source being set */
    public MetaDataLoader init( MetaDataSources sources, boolean shouldRegister ) {
        addSources( sources );
        if ( shouldRegister ) register();
        return init();
    }

    /** Initialize the MetaDataLoader */
    @Override
    public MetaDataLoader init() {

        if ( sources == null || sources.isEmpty() ) {
            throw new IllegalStateException("No Metadata Sources defined");
        }

        super.init();

        try {
            if ( getConfig().getTypeNames().isEmpty() ) {

                if ( getTypesRef() != null ) {
                    loadTypesFromFile(getTypesRef());
                }
                else if ( getDefaultTypesRef() != null ) {
                    loadTypesFromFile( getDefaultTypesRef() );
                }
            }
        } catch (MetaException e) {
            log.error("Could not load metadata types [" + getTypesRef() + "]: " + e.getMessage());
            throw new IllegalStateException("Could not load metadata types [" + getTypesRef() + "]", e);
        }

        // Load all the source data
        sources.forEach( s -> s.getSourceData().forEach( d -> {
            getReaderForFile( d.sourceName ).loadFromStream( new ByteArrayInputStream( d.sourceData.getBytes() ));
        }));

        return this;
    }

    /**
     * Lookup the specified class by name, include the classloaders provided by the metadata sources
     * NOTE:  This was done to handle OSGi and other complex ClassLoader scenarios
     */
    @Override
    public Class<?> loadClass( String className ) throws ClassNotFoundException {

        for (MetaDataSources s : sources ) {
            try {
                return s.getClass().getClassLoader().loadClass(className);
            } catch( ClassNotFoundException e ) {
                // Do nothing
            }
        }

        // Use the default class loader
        return super.loadClass( className );
    }

    /**
     * Loads the specified resource
     */
    public void loadTypesFromFile(String file) {
        loadTypesFromFile( this.getClass().getClassLoader(), file );
    }

    /**
     * Loads the specified resource
     */
    public void loadTypesFromFile( ClassLoader cl, String file) {

        checkState();
        
        // LOAD THE TYPES XML FILE
        if (file == null) {
            throw new IllegalArgumentException( "The Types XML reference file was not specified" );
        }

        InputStream is = null;

        // See if the filename exists
        String fn = (sourceDir==null) ? file : sourceDir + file;
        File f = new File(fn);

        if (f.exists()) {
            try {
                is = new FileInputStream(f);
            } catch (Exception e) {
                log.error("Can not read Types XML file [" + file + "]: " + e.getMessage());
                throw new MetaException("Can not read Types XML file [" + file + "]: " + e.getMessage(), e);
            }
        } else {
            is = cl.getResourceAsStream(file);
            if (is == null) {
                log.error("Types XML file [" + file + "] does not exist");
                throw new MetaException("The Types XML item file [" + file + "] was not found");
            }
        }

        try {
            getReaderForFile( file ).loadTypesFromStream( is );
        }
        catch (MetaException e) {
            log.error("Meta Types XML [" + file + "]: " + e.getMessage());
            throw new MetaException("The Types XML file [" + file + "] could not be loaded: " + e.getMessage(), e);
        }

        typesLoaded = true;
    }

    /** Load the MetaDataReader for the specified file */
    protected MetaDataReader getReaderForFile( String file ) {

        if ( file.endsWith( ".xml" ))
            return new XMLMetaDataReader( this, file );
        else if ( file.endsWith( ".json" ))
            return new JsonMetaDataReader( this, file );
        else
            throw new MetaDataException( "There is no MetaDataReader supporting the file: " + file );
    }

    /**
     * Returns a collection of child elements of the given name
     */
    /*protected Collection<Element> getElementsOfNames(Node n, String[] names) {
        ArrayList<Element> elements = new ArrayList<>();
        if (n == null) {
            return elements;
        }

        NodeList list = n.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node instanceof Element) {
                for (int j = 0; j < names.length; j++) {
                    if (node.getNodeName().equals(names[ j])) {
                        elements.add((Element) node);
                    }
                }
            }
        }

        return elements;
    }*/
}
