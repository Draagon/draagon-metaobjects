package com.draagon.meta.loader.xml;

import com.draagon.meta.loader.file.MetaDataSources;

import java.util.List;

/**
 * Handles loading
 *
 * Created by dmealing on 11/30/16.
 *
 * @deprecated Use com.draagon.meta.loader.file.LocalMetaDataSources
 */
public class LocalMetaDataSources extends com.draagon.meta.loader.xml.MetaDataSources {

    public LocalMetaDataSources(String file ) {
        read( file );
    }

    public LocalMetaDataSources(List<String> files ) {
        for ( String file : files ) {
            read( file );
        }
    }

    public LocalMetaDataSources(String baseDir, String file ) {
        setSourceDir( baseDir );
        read( file );
    }

    public LocalMetaDataSources(String baseDir, List<String> files ) {
        setSourceDir( baseDir );
        for ( String file : files ) {
            read( file );
        }
    }

    /** Returns the class loader */
    public ClassLoader getClassLoader() {
        return this.getClassLoader();
    }
}
