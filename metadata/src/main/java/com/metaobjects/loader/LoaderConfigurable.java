/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.loader;

import java.util.List;
import java.util.Map;

/**
 * Generic interface for configuring MetaDataLoaders from external build tools.
 * This abstraction keeps build-tool-specific dependencies out of the core metadata package.
 * 
 * Implementation Pattern:
 * - Core metadata classes implement this interface
 * - Build tools (Maven, Gradle, etc.) create specific configuration builders
 * - Configuration is applied through this generic interface
 */
public interface LoaderConfigurable {
    
    /**
     * Configure the loader with a configuration object
     * @param config The configuration to apply
     */
    void configure(LoaderConfiguration config);
    
    /**
     * Get the configured MetaDataLoader instance
     * @return The configured loader
     */
    MetaDataLoader getLoader();
    
    /**
     * Configuration data holder
     */
    interface LoaderConfiguration {
        String getSourceDir();
        ClassLoader getClassLoader(); 
        List<String> getSources();
        Map<String, String> getArguments();
    }
}