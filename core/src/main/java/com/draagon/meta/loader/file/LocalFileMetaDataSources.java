package com.draagon.meta.loader.file;

import java.lang.annotation.Documented;
import java.util.List;

/**
 * Handles loading
 *
 * Created by dmealing on 11/30/16.
 */
public class LocalFileMetaDataSources extends FileMetaDataSources {

    public LocalFileMetaDataSources(ClassLoader classLoader, String file ) {
        super(classLoader);
        read( file );
    }

    public LocalFileMetaDataSources(ClassLoader classLoader, List<String> files ) {
        super(classLoader);
        for ( String file : files ) {
            read( file );
        }
    }

    public LocalFileMetaDataSources(ClassLoader classLoader, String baseDir, String file ) {
        super(classLoader);
        setSourceDir( baseDir );
        read( file );
    }

    public LocalFileMetaDataSources(ClassLoader classLoader, String baseDir, List<String> files ) {
        super(classLoader);
        setSourceDir( baseDir );
        for ( String file : files ) {
            read( file );
        }
    }

    public LocalFileMetaDataSources(String file ) {
        read( file );
    }

    public LocalFileMetaDataSources(List<String> files ) {
        for ( String file : files ) {
            read( file );
        }
    }

    public LocalFileMetaDataSources(String baseDir, String file ) {
        setSourceDir( baseDir );
        read( file );
    }

    public LocalFileMetaDataSources(String baseDir, List<String> files ) {
        setSourceDir( baseDir );
        for ( String file : files ) {
            read( file );
        }
    }
}
