package com.draagon.meta.generator.direct;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.generator.GeneratorIOWriter;

/**
 * Plugin interface for extending MetaObject code generation behavior
 * Provides object and field-specific lifecycle hooks
 */
public interface GenerationPlugin extends BaseGenerationPlugin<MetaObject> {
    
    /**
     * Called before generating code for an object
     */
    default void beforeObjectGeneration(MetaObject object, GenerationContext context, GeneratorIOWriter<?> writer) {
        // Default implementation delegates to base method
        beforeItemGeneration(object, context, writer);
    }
    
    /**
     * Called after generating code for an object
     */
    default void afterObjectGeneration(MetaObject object, GenerationContext context, GeneratorIOWriter<?> writer) {
        // Default implementation delegates to base method
        afterItemGeneration(object, context, writer);
    }
    
    /**
     * Called before generating code for a field
     */
    default void beforeFieldGeneration(MetaField field, GenerationContext context, GeneratorIOWriter<?> writer) {
        // Default implementation does nothing - fields are not MetaData items themselves
    }
    
    /**
     * Called after generating code for a field
     */
    default void afterFieldGeneration(MetaField field, GenerationContext context, GeneratorIOWriter<?> writer) {
        // Default implementation does nothing - fields are not MetaData items themselves
    }
    
    /**
     * Allows plugin to customize field type mapping
     */
    default String customizeFieldType(MetaField field, String defaultType, GenerationContext context) {
        return defaultType; // Return default if no customization
    }
    
    /**
     * Allows plugin to customize method names for fields
     */
    default String customizeMethodName(MetaField field, String methodType, String defaultName, GenerationContext context) {
        return defaultName; // Return default if no customization
    }
    
    /**
     * Allows plugin to add custom imports for objects
     */
    default void contributeImports(MetaObject object, GenerationContext context) {
        // Default implementation delegates to base method
        contributeImports(object, (BaseGenerationContext<MetaObject>) context);
    }
    
    @Override
    default String getSupportedMetaDataType() {
        return "object";
    }
}