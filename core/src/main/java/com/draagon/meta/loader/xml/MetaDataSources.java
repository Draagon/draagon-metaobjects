package com.draagon.meta.loader.xml;

import com.draagon.meta.loader.file.FileMetaDataSources;

/**
 * Created by dmealing on 11/30/16.
 */
public class MetaDataSources extends FileMetaDataSources {

    protected MetaDataSources() {
        super( null );
    }

    protected MetaDataSources( ClassLoader classLoader ) {
        super( classLoader );
    }
}
