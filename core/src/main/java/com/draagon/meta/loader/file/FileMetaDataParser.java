package com.draagon.meta.loader.file;

import com.draagon.meta.loader.base.BaseMetaDataParser;

/**
 * Adapter for FileMetaDataParser that extends the consolidated BaseMetaDataParser from metadata module.
 * 
 * <p>This adapter maintains backward compatibility for core module classes while delegating
 * to the unified parsing infrastructure in the metadata module. All parsing logic has been
 * consolidated into BaseMetaDataParser to eliminate code duplication.</p>
 * 
 * <p>Key responsibilities (delegated to BaseMetaDataParser):</p>
 * <ul>
 *   <li>Managing file parsing context and configuration</li>
 *   <li>Handling type configuration and metadata creation</li>
 *   <li>Providing common parsing utilities and error handling</li>
 *   <li>Tracking parsing statistics and information</li>
 * </ul>
 * 
 * @author Draagon Software
 * @since 6.0.0 (adapter pattern for consolidation)
 */
public abstract class FileMetaDataParser extends BaseMetaDataParser {

    /** Create the FileMetaDataParser adapter */
    protected FileMetaDataParser(FileMetaDataLoader loader, String filename ) {
        super( loader, filename );
    }

    /** Return the FileMetaDataLoader (override to provide specific type) */
    @Override
    public FileMetaDataLoader getLoader() {
        return (FileMetaDataLoader) super.getLoader();
    }
}