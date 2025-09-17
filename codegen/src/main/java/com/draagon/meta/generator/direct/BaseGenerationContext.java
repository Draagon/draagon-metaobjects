package com.draagon.meta.generator.direct;

import com.draagon.meta.MetaData;
import com.draagon.meta.loader.MetaDataLoader;

import java.util.*;

/**
 * Generic base context for code generation containing configuration, state, and utilities
 * This can be extended for specific MetaData types (MetaObject, MetaView, etc.)
 * 
 * @param <T> The specific MetaData type this context works with
 */
public abstract class BaseGenerationContext<T extends MetaData> {
    
    protected final MetaDataLoader loader;
    protected final Map<String, Object> properties = new HashMap<>();
    protected final Map<String, CodeFragment> codeFragments = new HashMap<>();
    protected final List<BaseGenerationPlugin<T>> plugins = new ArrayList<>();
    protected final Map<String, Object> cache = new HashMap<>();
    
    // Generation state - generic to all MetaData types
    protected T currentItem;
    protected String currentPackage;
    protected String currentClassName;
    protected final Set<String> imports = new LinkedHashSet<>();
    
    public BaseGenerationContext(MetaDataLoader loader) {
        this.loader = loader;
        initializeDefaultFragments();
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
    
    // Code fragment management
    public BaseGenerationContext<T> addCodeFragment(String name, CodeFragment fragment) {
        codeFragments.put(name, fragment);
        return this;
    }
    
    public CodeFragment getCodeFragment(String name) {
        return codeFragments.get(name);
    }
    
    public boolean hasCodeFragment(String name) {
        return codeFragments.containsKey(name);
    }
    
    // Plugin management
    public BaseGenerationContext<T> addPlugin(BaseGenerationPlugin<T> plugin) {
        plugins.add(plugin);
        plugin.initialize(this);
        return this;
    }
    
    public List<BaseGenerationPlugin<T>> getPlugins() {
        return Collections.unmodifiableList(plugins);
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
    
    // Abstract methods for subclasses to implement
    
    /**
     * Resolve variables in templates based on current context state.
     * Each subclass implements this for their specific MetaData type.
     */
    public abstract String resolveVariables(String template);
    
    /**
     * Initialize default code fragments appropriate for this MetaData type.
     * Each subclass implements this for their specific use case.
     */
    protected abstract void initializeDefaultFragments();
    
    /**
     * Get the type name for this generation context (e.g., "object", "view", "validator")
     */
    public abstract String getContextType();
}