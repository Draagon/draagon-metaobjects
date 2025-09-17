package com.draagon.meta.registry;

/**
 * Service interface for providing context-aware metadata creation rules.
 * 
 * <p>This service determines the appropriate subtype for attributes and child elements
 * based on their parent context, restoring the context-aware behavior that was 
 * defined in metaobjects.types.xml in pre-v6.0.0 versions.</p>
 * 
 * <p>For example, a 'keys' attribute under a 'key' element should automatically
 * be created as a 'stringArray' type rather than the default 'string' type.</p>
 * 
 * @since 6.0.0
 */
public interface MetaDataContextProvider {

    /**
     * Gets the appropriate subtype for an attribute based on its parent context.
     * 
     * @param parentType the type of the parent element (e.g., "key", "object", "field")
     * @param parentSubType the subtype of the parent element (e.g., "primary", "pojo")  
     * @param attributeName the name of the attribute being created (e.g., "keys", "object")
     * @return the subtype that should be used for the attribute, or null if no specific rule applies
     */
    String getContextSpecificAttributeSubType(String parentType, String parentSubType, String attributeName);
    
    /**
     * Gets the appropriate subtype for a child element based on its parent context.
     * 
     * @param parentType the type of the parent element
     * @param parentSubType the subtype of the parent element
     * @param childType the type of the child element being created
     * @param childName the name of the child element
     * @return the subtype that should be used for the child, or null if no specific rule applies
     */
    String getContextSpecificChildSubType(String parentType, String parentSubType, String childType, String childName);
    
    /**
     * Gets the priority for this context provider. Higher numbers indicate higher priority.
     * Context providers are consulted in priority order.
     * 
     * @return the priority value
     */
    int getPriority();
    
    /**
     * Gets a description of this context provider for debugging and logging.
     * 
     * @return a human-readable description
     */
    String getDescription();
}