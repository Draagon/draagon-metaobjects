package com.draagon.meta.generator.direct;

import com.draagon.meta.MetaData;
import com.draagon.meta.generator.GeneratorIOWriter;

/**
 * Generic plugin interface for extending code generation behavior
 * This can be extended for specific MetaData types (MetaObject, MetaView, etc.)
 * 
 * @param <T> The specific MetaData type this plugin works with
 */
public interface BaseGenerationPlugin<T extends MetaData> {
    
    /**
     * Initialize the plugin with the generation context
     */
    default void initialize(BaseGenerationContext<T> context) {
        // Default implementation does nothing
    }
    
    /**
     * Called before generating code for an item of type T
     */
    default void beforeItemGeneration(T item, BaseGenerationContext<T> context, GeneratorIOWriter<?> writer) {
        // Default implementation does nothing
    }
    
    /**
     * Called after generating code for an item of type T
     */
    default void afterItemGeneration(T item, BaseGenerationContext<T> context, GeneratorIOWriter<?> writer) {
        // Default implementation does nothing  
    }
    
    /**
     * Allows plugin to customize type mapping for this MetaData type
     * Default implementation returns the original type
     */
    default String customizeType(T item, String defaultType, BaseGenerationContext<T> context) {
        return defaultType; // Return default if no customization
    }
    
    /**
     * Allows plugin to customize name generation for this MetaData type
     * Default implementation returns the original name
     */
    default String customizeName(T item, String nameType, String defaultName, BaseGenerationContext<T> context) {
        return defaultName; // Return default if no customization
    }
    
    /**
     * Allows plugin to add custom imports
     */
    default void contributeImports(T item, BaseGenerationContext<T> context) {
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
    
    /**
     * Get the type of MetaData this plugin is designed for
     * This helps ensure plugins are used with appropriate contexts
     */
    default String getSupportedMetaDataType() {
        return "generic";
    }
}