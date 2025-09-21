package com.draagon.meta.generator.direct;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.loader.MetaDataLoader;

/**
 * Object-specific generation context for MetaObject code generation
 * Contains state and utilities specific to object/field generation patterns
 */
public class GenerationContext extends BaseGenerationContext<MetaObject> {
    
    // Object-specific state
    private MetaField currentField;
    
    public GenerationContext(MetaDataLoader loader) {
        super(loader);
    }
    
    // Object-specific state management
    public GenerationContext setCurrentObject(MetaObject object) {
        setCurrentItem(object);
        return this;
    }
    
    public MetaObject getCurrentObject() { 
        return getCurrentItem(); 
    }
    
    public GenerationContext setCurrentField(MetaField field) {
        this.currentField = field;
        return this;
    }
    
    public MetaField getCurrentField() { 
        return currentField; 
    }
    
    @Override
    public String getContextType() {
        return "object";
    }
    
    
    // Convenience methods for fluent interface
    @Override
    public GenerationContext setProperty(String key, Object value) {
        super.setProperty(key, value);
        return this;
    }
    
    
    
    @Override
    public GenerationContext setCurrentPackage(String packageName) {
        super.setCurrentPackage(packageName);
        return this;
    }
    
    @Override
    public GenerationContext setCurrentClassName(String className) {
        super.setCurrentClassName(className);
        return this;
    }
    
    @Override
    public GenerationContext addImport(String importName) {
        super.addImport(importName);
        return this;
    }
    
    @Override
    public GenerationContext putCache(String key, Object value) {
        super.putCache(key, value);
        return this;
    }
}