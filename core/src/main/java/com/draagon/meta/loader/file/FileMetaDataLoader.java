/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.loader.file;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.MetaException;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObjectNotFoundException;
import com.draagon.meta.util.MetaDataUtil;
import com.draagon.meta.util.xml.XMLFileReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.draagon.meta.util.MetaDataUtil.expandPackageForPath;


/**
 * Meta Class loader for XML files
 */
public class FileMetaDataLoader extends MetaDataLoader {

    private static Log log = LogFactory.getLog(FileMetaDataLoader.class);

    /** Used to store the MetaData types and respective Java classes */
    public static class MetaDataTypes {

        public final Class<? extends MetaData> baseClass;
        private final Map<String,Class<? extends MetaData>> classes = new HashMap<String,Class<? extends MetaData>>();
        private String defaultType = null;

        public MetaDataTypes( Class<? extends MetaData> baseClass ) {
            this.baseClass = baseClass;
        }

        public void put( String name, Class<? extends MetaData> clazz, boolean def ) {
            classes.put( name, clazz );
            if ( def ) defaultType = name;
        }

        public Class<? extends MetaData> get( String name ) {
            return classes.get( name );
        }

        public Class<? extends MetaData> getDefaultTypeClass() {
            if ( defaultType == null ) return null;
            return get( defaultType );
        }

        public String getDefaultType() {
            return defaultType;
        }
    }

    private String typesRef = null;
    private boolean typesLoaded = false;
    private final ConcurrentHashMap<String, MetaDataTypes> typesMap = new ConcurrentHashMap<String,MetaDataTypes>();
    private String sourceDir = null;
    private final List<MetaDataSources> sources = new ArrayList<MetaDataSources>();

    public FileMetaDataLoader() {
        this( "file-" + System.currentTimeMillis() );
    }

    public FileMetaDataLoader(String name ) {
        super( "file", name );
    }

    public String getDefaultMetaDataTypes() {
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
            if ( !typesLoaded && getTypesRef() != null ) {
                loadTypesFromFile( getTypesRef() );
            }
        } catch (MetaException e) {
            log.error("Could not load metadata types [" + getTypesRef() + "]: " + e.getMessage());
            throw new IllegalStateException("Could not load metadata types [" + getTypesRef() + "]", e);
        }

        // Load all the Meta sources
        for (MetaDataSources s : sources ) {
            for ( String data : s.getSourceData() ) {
                loadFromStream( new ByteArrayInputStream( data.getBytes() ));
            }
        }

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
            loadTypesFromStream(is);
        } catch (MetaException e) {
            log.error("Meta Types XML [" + file + "]: " + e.getMessage());
            throw new MetaException("The Types XML file [" + file + "] could not be loaded: " + e.getMessage(), e);
        }

        typesLoaded = true;
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
