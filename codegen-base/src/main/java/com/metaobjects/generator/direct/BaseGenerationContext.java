package com.metaobjects.generator.direct;

import com.metaobjects.MetaData;
import com.metaobjects.loader.MetaDataLoader;

import java.util.*;

/**
 * Simplified base context for code generation containing basic configuration and state.
 * This provides essential functionality for direct generators.
 * 
 * @param <T> The specific MetaData type this context works with
 */
public abstract class BaseGenerationContext<T extends MetaData> {
    
    protected final MetaDataLoader loader;
    protected final Map<String, Object> properties = new HashMap<>();
    protected final Map<String, Object> cache = new HashMap<>();
    
    // Generation state - generic to all MetaData types
    protected T currentItem;
    protected String currentPackage;
    protected String currentClassName;
    protected final Set<String> imports = new LinkedHashSet<>();
    
    public BaseGenerationContext(MetaDataLoader loader) {
        this.loader = loader;
    }
    
    // Property management
    public BaseGenerationContext<T> setProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public <V> V getProperty(String key, V defaultValue) {
        return (V) properties.getOrDefault(key, defaultValue);
    }
    
    public String getStringProperty(String key, String defaultValue) {
        return (String) properties.getOrDefault(key, defaultValue);
    }
    
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        return (Boolean) properties.getOrDefault(key, defaultValue);
    }
    
    // State management - common to all types
    public MetaDataLoader getLoader() { return loader; }
    
    public BaseGenerationContext<T> setCurrentItem(T item) {
        this.currentItem = item;
        return this;
    }
    
    public T getCurrentItem() { return currentItem; }
    
    public BaseGenerationContext<T> setCurrentPackage(String packageName) {
        this.currentPackage = packageName;
        return this;
    }
    
    public String getCurrentPackage() { return currentPackage; }
    
    public BaseGenerationContext<T> setCurrentClassName(String className) {
        this.currentClassName = className;
        return this;
    }
    
    public String getCurrentClassName() { return currentClassName; }
    
    public BaseGenerationContext<T> addImport(String importName) {
        imports.add(importName);
        return this;
    }
    
    public Set<String> getImports() { return Collections.unmodifiableSet(imports); }
    
    public void clearImports() { imports.clear(); }
    
    // Cache management
    public BaseGenerationContext<T> putCache(String key, Object value) {
        cache.put(key, value);
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public <V> V getCache(String key) {
        return (V) cache.get(key);
    }
    
    public boolean hasCache(String key) {
        return cache.containsKey(key);
    }
    
    /**
     * Get the type name for this generation context (e.g., "object", "view", "validator")
     */
    public abstract String getContextType();
}