package com.draagon.meta.enhancement;

import java.util.Collection;

/**
 * v6.0.0: Service interface for providing attribute definitions that can be applied to MetaData instances.
 * This replaces the TypesConfig overlay system with a more flexible, service-based approach.
 * 
 * Implementations of this interface are discovered using ServiceLoader and can provide
 * attributes for cross-cutting concerns like database mapping, IO serialization, validation, etc.
 */
@FunctionalInterface
public interface MetaDataAttributeProvider {
    
    /**
     * Provides attribute definitions that can be added to MetaData instances
     * @return Collection of attribute definitions this provider supports
     */
    Collection<AttributeDefinition> getAttributeDefinitions();
    
    /**
     * @return Unique identifier for this attribute provider
     */
    default String getProviderId() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * @return Priority for attribute loading order (higher = loaded later, can override earlier attributes)
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * @return Description of what this provider does
     */
    default String getDescription() {
        return "Provides " + getAttributeDefinitions().size() + " attribute definitions";
    }
}