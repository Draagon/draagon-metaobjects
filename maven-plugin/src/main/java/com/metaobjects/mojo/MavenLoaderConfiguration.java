/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.mojo;

import com.metaobjects.loader.LoaderConfigurable;
import com.metaobjects.loader.LoaderConfigurationBuilder;

import java.util.List;
import java.util.Map;

/**
 * Maven-specific implementation of LoaderConfiguration.
 * This bridges Maven Mojo configuration to the generic LoaderConfigurable interface.
 */
public class MavenLoaderConfiguration {
    
    /**
     * Configure a LoaderConfigurable instance using Maven-specific configuration
     * 
     * @param configurable The loader to configure
     * @param sourceDir The Maven source directory
     * @param classLoader The Maven project class loader
     * @param sources The list of source files
     * @param globals The global arguments map
     */
    public static void configure(LoaderConfigurable configurable, 
                               String sourceDir, 
                               ClassLoader classLoader,
                               List<String> sources, 
                               Map<String, String> globals) {
        
        
        // Trigger lazy initialization of ServiceLoader type discovery
        if (configurable instanceof com.metaobjects.loader.MetaDataLoader) {
            com.metaobjects.loader.MetaDataLoader loader = (com.metaobjects.loader.MetaDataLoader) configurable;
            // Just access registry to trigger ServiceLoader discovery - no manual registration needed
            loader.getTypeRegistry().getRegisteredTypes();
        }
        
        LoaderConfigurable.LoaderConfiguration config = new LoaderConfigurationBuilder()
                .sourceDir(sourceDir)
                .classLoader(classLoader)
                .sources(sources)
                .arguments(globals)
                .build();
                
        configurable.configure(config);
    }
}