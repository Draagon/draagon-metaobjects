package com.draagon.meta.loader.xml;

import java.util.List;

/**
 * Handles loading
 *
 * Created by dmealing on 11/30/16.
 */
public class LocalMetaDataSources extends MetaDataSources {

    public LocalMetaDataSources(ClassLoader classLoader, String file ) {
        super(classLoader);

        read( file );
    }

    public LocalMetaDataSources(ClassLoader classLoader, List<String> files ) {
        super(classLoader);

        for ( String file : files ) {
            read( file );
        }
    }

    public LocalMetaDataSources(ClassLoader classLoader, String baseDir, String file ) {
        super(classLoader);

        setSourceDir( baseDir );
        read( file );
    }

    public LocalMetaDataSources(ClassLoader classLoader, String baseDir, List<String> files ) {
        super(classLoader);

        setSourceDir( baseDir );
        for ( String file : files ) {
            read( file );
        }
    }

    /** Returns the class loader */
    //public ClassLoader getClassLoader() {
    //    return this.getClassLoader();
    //}
}
