package com.draagon.meta.enhancement;

import com.draagon.meta.MetaData;

import java.util.Collection;

/**
 * v6.0.0: Service interface for enhancing MetaData instances with additional capabilities.
 * This replaces the TypesConfig overlay system with a more flexible, service-based approach.
 * 
 * Implementations of this interface are discovered using ServiceLoader and can enhance
 * MetaData with attributes, validation rules, or other capabilities based on the target service context.
 */
public interface MetaDataEnhancer {
    
    /**
     * Enhances MetaData instance with additional attributes/capabilities
     * @param metaData The MetaData to enhance
     * @param context Enhancement context (target service, template, etc.)
     * @return true if enhancement was applied, false if this enhancer doesn't apply to the context
     */
    boolean enhance(MetaData metaData, EnhancementContext context);
    
    /**
     * @return Services this enhancer supports (e.g., "objectManagerDB", "jsonIO", "codeGen")
     */
    Collection<String> getSupportedServices();
    
    /**
     * @return Unique identifier for this enhancer
     */
    default String getEnhancerId() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * @return Priority for enhancement execution order (higher = applied later)
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * @return Description of what this enhancer does
     */
    default String getDescription() {
        return "Enhances MetaData for services: " + getSupportedServices();
    }
}