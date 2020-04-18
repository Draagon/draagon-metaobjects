package com.draagon.meta.loader.file.json;

import com.draagon.meta.loader.config.MetaDataConfig;
import com.draagon.meta.loader.file.FileMetaDataLoader;
import com.draagon.meta.loader.file.MetaDataParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;

/**
 * Json MetaData Loader
 */
public class JsonMetaDataParser extends MetaDataParser {

    private static Log log = LogFactory.getLog(JsonMetaDataParser.class);

    public JsonMetaDataParser(FileMetaDataLoader loader, String filename ) {
        super( loader, filename );
    }

    @Override
    public MetaDataConfig loadFromStream( InputStream is) {
        throw new UnsupportedOperationException( "JsonMetaData Reader is not yet functional [" + getFilename() + "]" );
        //return getMetaDataConfig();
    }
}
