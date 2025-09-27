/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for creating LoaderConfiguration instances.
 * This provides a fluent API for build tools to configure loaders
 * without depending on build-tool-specific interfaces.
 */
public class LoaderConfigurationBuilder {
    
    private String sourceDir;
    private ClassLoader classLoader;
    private List<String> sources = new ArrayList<>();
    private Map<String, String> arguments = new HashMap<>();
    
    public LoaderConfigurationBuilder() {
    }
    
    public LoaderConfigurationBuilder sourceDir(String sourceDir) {
        this.sourceDir = sourceDir;
        return this;
    }
    
    public LoaderConfigurationBuilder classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }
    
    public LoaderConfigurationBuilder source(String source) {
        this.sources.add(source);
        return this;
    }
    
    public LoaderConfigurationBuilder sources(List<String> sources) {
        if (sources != null) {
            this.sources.addAll(sources);
        }
        return this;
    }
    
    public LoaderConfigurationBuilder argument(String key, String value) {
        this.arguments.put(key, value);
        return this;
    }
    
    public LoaderConfigurationBuilder arguments(Map<String, String> arguments) {
        if (arguments != null) {
            this.arguments.putAll(arguments);
        }
        return this;
    }
    
    public LoaderConfigurationBuilder register(boolean register) {
        return argument(LoaderConfigurationConstants.ARG_REGISTER, String.valueOf(register));
    }
    
    public LoaderConfigurationBuilder verbose(boolean verbose) {
        return argument(LoaderConfigurationConstants.ARG_VERBOSE, String.valueOf(verbose));
    }
    
    public LoaderConfigurationBuilder strict(boolean strict) {
        return argument(LoaderConfigurationConstants.ARG_STRICT, String.valueOf(strict));
    }
    
    public LoaderConfigurable.LoaderConfiguration build() {
        return new LoaderConfigurationImpl(sourceDir, classLoader, new ArrayList<>(sources), new HashMap<>(arguments));
    }
    
    /**
     * Internal implementation of LoaderConfiguration
     */
    private static class LoaderConfigurationImpl implements LoaderConfigurable.LoaderConfiguration {
        private final String sourceDir;
        private final ClassLoader classLoader;
        private final List<String> sources;
        private final Map<String, String> arguments;
        
        public LoaderConfigurationImpl(String sourceDir, ClassLoader classLoader, 
                                     List<String> sources, Map<String, String> arguments) {
            this.sourceDir = sourceDir;
            this.classLoader = classLoader;
            this.sources = sources;
            this.arguments = arguments;
        }
        
        @Override
        public String getSourceDir() {
            return sourceDir;
        }
        
        @Override
        public ClassLoader getClassLoader() {
            return classLoader;
        }
        
        @Override
        public List<String> getSources() {
            return sources;
        }
        
        @Override
        public Map<String, String> getArguments() {
            return arguments;
        }
    }
}