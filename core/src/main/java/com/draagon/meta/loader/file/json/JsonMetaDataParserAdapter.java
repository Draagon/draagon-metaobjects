package com.draagon.meta.loader.file.json;

import com.draagon.meta.loader.file.FileMetaDataLoader;
import com.draagon.meta.loader.file.FileMetaDataParser;
import com.draagon.meta.loader.json.JsonMetaDataParser;

import java.io.InputStream;

/**
 * Adapter that wraps the enhanced JsonMetaDataParser from metadata module
 * to work with core module's FileMetaDataParser architecture.
 * 
 * This allows the core module to use the advanced JsonMetaDataParser while
 * maintaining compatibility with FileLoaderOptions.
 * 
 * @author Draagon Software
 * @since 6.0.0
 */
public class JsonMetaDataParserAdapter extends FileMetaDataParser {

    private final JsonMetaDataParser enhancedParser;

    public JsonMetaDataParserAdapter(FileMetaDataLoader loader, String filename) {
        super(loader, filename);
        this.enhancedParser = new JsonMetaDataParser(loader, filename);
    }

    @Override
    public void loadFromStream(InputStream is) {
        enhancedParser.loadFromStream(is);
    }
}