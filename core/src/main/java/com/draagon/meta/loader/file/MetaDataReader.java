package com.draagon.meta.loader.file;

import com.draagon.meta.loader.MetaDataLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Absract MetaDataReader for reading from source files
 */
public abstract class MetaDataReader<T extends Closeable> {

    public final static String ATTR_NAME = "name";
    public final static String ATTR_TYPE = "type";
    public final static String ATTR_SUPER = "super";

    protected static List<String> reservedAttributes = new ArrayList<>();
    static {
        reservedAttributes.add( ATTR_NAME );
        reservedAttributes.add( ATTR_TYPE );
        reservedAttributes.add( ATTR_SUPER );
        InputStreamReader r = null;
    }

    private MetaDataLoader loader = null;
    protected final T input;

    /** Create the MetaDataReader */
    public MetaDataReader( T in ) {
        this.input = in;
    }

    /** Get the Loader */
    protected MetaDataLoader getLoader() {
        return loader;
    }

    public void loadTypesFromStream( MetaDataLoader loader, InputStream is) {

    }

    /**
     * Close the InputSream
     * @throws IOException
     */
    public void close() throws IOException {
        input.close();
    }
}
