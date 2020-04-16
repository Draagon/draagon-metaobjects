package com.draagon.meta.loader.file.json;

import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.config.MetaDataConfig;
import com.draagon.meta.loader.file.FileMetaDataLoader;
import com.draagon.meta.loader.file.MetaDataReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;

/**
 * Json MetaData Loader
 */
public class JsonMetaDataReader extends MetaDataReader {

    private static Log log = LogFactory.getLog(JsonMetaDataReader.class);

    public JsonMetaDataReader( FileMetaDataLoader loader, String filename ) {
        super( loader, filename );
    }

    @Override
    public MetaDataConfig loadTypesFromStream( InputStream is) {
        throw new UnsupportedOperationException( "JsonMetaData Reader is not yet functional [" + getFilename() + "]" );
        //return getConfig();
    }

    @Override
    public MetaDataConfig loadFromStream( InputStream is) {
        throw new UnsupportedOperationException( "JsonMetaData Reader is not yet functional [" + getFilename() + "]" );
        //return getConfig();
    }
}
