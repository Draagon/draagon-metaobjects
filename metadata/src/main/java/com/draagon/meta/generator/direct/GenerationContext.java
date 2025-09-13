package com.draagon.meta.generator.direct;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.loader.MetaDataLoader;

import java.util.*;

/**
 * Central context for code generation containing configuration, state, and utilities
 */
public class GenerationContext {
    
    private final MetaDataLoader loader;
    private final Map<String, Object> properties = new HashMap<>();
    private final Map<String, CodeFragment> codeFragments = new HashMap<>();
    private final List<GenerationPlugin> plugins = new ArrayList<>();
    private final Map<String, Object> cache = new HashMap<>();
    
    // Generation state
    private MetaObject currentObject;
    private MetaField currentField;
    private String currentPackage;
    private String currentClassName;
    private final Set<String> imports = new LinkedHashSet<>();
    
    public GenerationContext(MetaDataLoader loader) {
        this.loader = loader;
        initializeDefaultFragments();
    }
    
    // Property management
    public GenerationContext setProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }
    
    public <T> T getProperty(String key, T defaultValue) {
        return (T) properties.getOrDefault(key, defaultValue);
    }
    
    public String getStringProperty(String key, String defaultValue) {
        return (String) properties.getOrDefault(key, defaultValue);
    }
    
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        return (Boolean) properties.getOrDefault(key, defaultValue);
    }
    
    // Code fragment management
    public GenerationContext addCodeFragment(String name, CodeFragment fragment) {
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
    public GenerationContext addPlugin(GenerationPlugin plugin) {
        plugins.add(plugin);
        plugin.initialize(this);
        return this;
    }
    
    public List<GenerationPlugin> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }
    
    // State management
    public MetaDataLoader getLoader() { return loader; }
    
    public GenerationContext setCurrentObject(MetaObject object) {
        this.currentObject = object;
        return this;
    }
    
    public MetaObject getCurrentObject() { return currentObject; }
    
    public GenerationContext setCurrentField(MetaField field) {
        this.currentField = field;
        return this;
    }
    
    public MetaField getCurrentField() { return currentField; }
    
    public GenerationContext setCurrentPackage(String packageName) {
        this.currentPackage = packageName;
        return this;
    }
    
    public String getCurrentPackage() { return currentPackage; }
    
    public GenerationContext setCurrentClassName(String className) {
        this.currentClassName = className;
        return this;
    }
    
    public String getCurrentClassName() { return currentClassName; }
    
    public GenerationContext addImport(String importName) {
        imports.add(importName);
        return this;
    }
    
    public Set<String> getImports() { return Collections.unmodifiableSet(imports); }
    
    public void clearImports() { imports.clear(); }
    
    // Cache management
    public GenerationContext putCache(String key, Object value) {
        cache.put(key, value);
        return this;
    }
    
    public <T> T getCache(String key) {
        return (T) cache.get(key);
    }
    
    public boolean hasCache(String key) {
        return cache.containsKey(key);
    }
    
    // Utility methods
    public String resolveVariables(String template) {
        if (template == null) return null;
        
        String result = template;
        
        // Simple variable substitution
        if (currentObject != null) {
            result = result.replace("${object.name}", currentObject.getName())
                          .replace("${object.shortName}", currentObject.getShortName())
                          .replace("${object.package}", currentObject.getPackage());
        }
        
        if (currentField != null) {
            result = result.replace("${field.name}", currentField.getName())
                          .replace("${field.type}", currentField.getDataType().toString());
        }
        
        if (currentPackage != null) {
            result = result.replace("${package}", currentPackage);
        }
        
        if (currentClassName != null) {
            result = result.replace("${className}", currentClassName);
        }
        
        return result;
    }
    
    private void initializeDefaultFragments() {
        // Add default Java code fragments
        addCodeFragment("java.getter.javadoc", new CodeFragment(
            "/**\n" +
            " * Gets the ${field.name} value\n" +
            " * @return ${field.javaType} the ${field.name}\n" +
            " */"
        ));
        
        addCodeFragment("java.setter.javadoc", new CodeFragment(
            "/**\n" +
            " * Sets the ${field.name} value\n" +
            " * @param ${field.name} the ${field.name} to set\n" +
            " */"
        ));
        
        addCodeFragment("java.getter.signature", new CodeFragment(
            "public ${field.javaType} get${field.nameCapitalized}()"
        ));
        
        addCodeFragment("java.setter.signature", new CodeFragment(
            "public void set${field.nameCapitalized}(${field.javaType} ${field.name})"
        ));
        
        addCodeFragment("java.interface.getter", new CodeFragment(
            "public ${field.javaType} get${field.nameCapitalized}();"
        ));
        
        addCodeFragment("java.interface.setter", new CodeFragment(
            "public void set${field.nameCapitalized}(${field.javaType} ${field.name});"
        ));
        
        addCodeFragment("java.class.header", new CodeFragment(
            "/**\n" +
            " * Generated class for ${object.name}\n" +
            " * @generated by MetaObjects Direct Generator\n" +
            " */\n" +
            "public ${classType} ${className}${superClass} {"
        ));
    }
}