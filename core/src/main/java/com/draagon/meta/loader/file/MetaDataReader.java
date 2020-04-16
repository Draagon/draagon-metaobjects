package com.draagon.meta.loader.file;

import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.config.MetaDataConfig;
import com.draagon.meta.loader.config.TypeConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Absract MetaDataReader for reading from source files
 */
public abstract class MetaDataReader {

    public final static String ATTR_NAME = "name";
    public final static String ATTR_TYPE = "type";
    public final static String ATTR_SUPER = "super";

    protected static List<String> reservedAttributes = new ArrayList<>();
    static {
        reservedAttributes.add( ATTR_NAME );
        reservedAttributes.add( ATTR_TYPE );
        reservedAttributes.add( ATTR_SUPER );
    }

    private FileMetaDataLoader loader;

    /** Create the MetaDataReader */
    protected MetaDataReader( FileMetaDataLoader loader ) {
        this.loader = loader;
    }

    /** Return the FileMetaDataLoader */
    public FileMetaDataLoader getLoader() {
        return this.loader;
    }

    /** Return the MetaDataConfig */
    public MetaDataConfig getConfig() {
        return this.loader.getConfig();
    }

    /** Load the types configuration from the inputstream */
    public abstract MetaDataConfig loadTypesFromStream( String filename, InputStream is );

    /** Load the metadata models from the inputstream */
    public abstract MetaDataConfig loadFromStream( String filename, InputStream is );
}
