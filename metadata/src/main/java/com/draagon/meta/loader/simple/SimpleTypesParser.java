package com.draagon.meta.loader.simple;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.registry.MetaDataTypeRegistry;
import com.draagon.meta.loader.uri.URIHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * v6.0.0: Simplified parser that replaces TypesConfig-based parsing with service discovery.
 * 
 * <p>This class maintains API compatibility with the old TypesConfigParser system
 * but now uses the service-based MetaDataTypeRegistry for type discovery instead
 * of parsing JSON configuration files.</p>
 * 
 * <p>The actual type registration now happens automatically via ServiceLoader
 * discovery of MetaDataTypeProvider implementations.</p>
 */
public class SimpleTypesParser {

    private static final Logger log = LoggerFactory.getLogger(SimpleTypesParser.class);

    private final MetaDataTypeRegistry typeRegistry;
    private final ClassLoader classLoader;
    private final String sourceName;

    protected SimpleTypesParser(MetaDataTypeRegistry typeRegistry, ClassLoader classLoader, String sourceName) {
        this.typeRegistry = typeRegistry;
        this.classLoader = classLoader;
        this.sourceName = sourceName;
    }

    /**
     * v6.0.0: Load and merge types from URI (now a no-op - types loaded via service discovery)
     * 
     * <p>This method maintains API compatibility but no longer parses TypesConfig files.
     * Type registration now happens automatically via ServiceLoader discovery of 
     * MetaDataTypeProvider implementations.</p>
     */
    public void loadAndMerge(SimpleLoader simpleLoader, URI uri) {
        log.debug("loadAndMerge called for URI: {} (v6.0.0: no-op - using service discovery instead)", uri);
        
        // v6.0.0: Types are now registered automatically via service discovery
        // This method is maintained for API compatibility but does nothing
        
        // Log the registry stats for debugging
        if (log.isDebugEnabled()) {
            var stats = typeRegistry.getStats();
            log.debug("TypeRegistry stats: {} total types registered via service discovery", 
                     stats.totalTypes());
        }
    }

    /**
     * v6.0.0: Load and merge types from InputStream (now a no-op - types loaded via service discovery) 
     */
    public void loadAndMerge(Object intoConfig, InputStream is) {
        log.debug("loadAndMerge with InputStream called (v6.0.0: no-op - using service discovery instead)");
        
        // v6.0.0: TypesConfig system removed - types registered via ServiceLoader
        // This method maintained for API compatibility
        
        // Close the input stream to prevent resource leaks
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                log.warn("Failed to close input stream: {}", e.getMessage());
            }
        }
    }
    
    // Getter methods for compatibility
    public ClassLoader getClassLoader() {
        return classLoader;
    }
    
    public String getSourceName() {
        return sourceName;
    }
}
