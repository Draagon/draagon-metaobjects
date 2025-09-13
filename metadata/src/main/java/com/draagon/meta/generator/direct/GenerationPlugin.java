package com.draagon.meta.generator.direct;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.generator.GeneratorIOWriter;

/**
 * Plugin interface for extending code generation behavior
 */
public interface GenerationPlugin {
    
    /**
     * Initialize the plugin with the generation context
     */
    default void initialize(GenerationContext context) {
        // Default implementation does nothing
    }
    
    /**
     * Called before generating code for an object
     */
    default void beforeObjectGeneration(MetaObject object, GenerationContext context, GeneratorIOWriter<?> writer) {
        // Default implementation does nothing
    }
    
    /**
     * Called after generating code for an object
     */
    default void afterObjectGeneration(MetaObject object, GenerationContext context, GeneratorIOWriter<?> writer) {
        // Default implementation does nothing  
    }
    
    /**
     * Called before generating code for a field
     */
    default void beforeFieldGeneration(MetaField field, GenerationContext context, GeneratorIOWriter<?> writer) {
        // Default implementation does nothing
    }
    
    /**
     * Called after generating code for a field
     */
    default void afterFieldGeneration(MetaField field, GenerationContext context, GeneratorIOWriter<?> writer) {
        // Default implementation does nothing
    }
    
    /**
     * Allows plugin to customize field type mapping
     */
    default String customizeFieldType(MetaField field, String defaultType, GenerationContext context) {
        return defaultType; // Return default if no customization
    }
    
    /**
     * Allows plugin to customize method names
     */
    default String customizeMethodName(MetaField field, String methodType, String defaultName, GenerationContext context) {
        return defaultName; // Return default if no customization
    }
    
    /**
     * Allows plugin to add custom imports
     */
    default void contributeImports(MetaObject object, GenerationContext context) {
        // Default implementation does nothing
    }
    
    /**
     * Plugin name for identification
     */
    String getName();
    
    /**
     * Plugin version for compatibility checking
     */
    default String getVersion() {
        return "1.0.0";
    }
}