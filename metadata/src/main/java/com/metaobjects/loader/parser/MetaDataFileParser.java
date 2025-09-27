package com.metaobjects.loader.parser;

import java.io.InputStream;

import com.metaobjects.loader.MetaDataLoader;

/**
 * Interface for all metadata file parsers.
 * Replaces FileMetaDataParser inheritance hierarchy with composition.
 * 
 * <p>This interface allows FileMetaDataLoader to work with parsers without 
 * requiring inheritance from specific base classes, enabling a cleaner
 * architecture where all parsing logic is consolidated in the metadata module.</p>
 * 
 * @author Draagon Software
 * @since 6.0.0 (replaces FileMetaDataParser inheritance)
 */
public interface MetaDataFileParser {
    
    /**
     * Parse metadata from the provided input stream.
     * 
     * @param is the input stream containing metadata to parse
     */
    void loadFromStream(InputStream is);
    
    /**
     * Get the MetaDataLoader this parser is associated with.
     * 
     * @return the MetaDataLoader instance
     */
    MetaDataLoader getLoader();
    
    /**
     * Get the filename being parsed.
     * 
     * @return the filename being parsed
     */
    String getFilename();
}