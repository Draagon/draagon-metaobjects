package com.draagon.meta.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Registry for MetaDataContextProvider services that provide context-aware metadata creation rules.
 * 
 * <p>This registry discovers and manages MetaDataContextProvider services, allowing the system
 * to determine appropriate subtypes for attributes and child elements based on their parent context.</p>
 * 
 * <p>Context providers are consulted in priority order (highest first) until a rule is found.</p>
 */
public class MetaDataContextRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(MetaDataContextRegistry.class);
    
    private final List<MetaDataContextProvider> providers = new ArrayList<>();
    private volatile boolean initialized = false;
    
    /**
     * Gets the singleton instance of the MetaDataContextRegistry.
     */
    public static MetaDataContextRegistry getInstance() {
        return InstanceHolder.INSTANCE;
    }
    
    /**
     * Gets the appropriate subtype for an attribute based on its parent context.
     * 
     * @param parentType the type of the parent element (e.g., "key", "object", "field")
     * @param parentSubType the subtype of the parent element (e.g., "primary", "pojo")
     * @param attributeName the name of the attribute being created (e.g., "keys", "object")
     * @return the subtype that should be used for the attribute, or "string" as default
     */
    public String getContextSpecificAttributeSubType(String parentType, String parentSubType, String attributeName) {
        ensureInitialized();
        
        for (MetaDataContextProvider provider : providers) {
            String subType = provider.getContextSpecificAttributeSubType(parentType, parentSubType, attributeName);
            if (subType != null) {
                log.debug("Context provider [{}] resolved {}:{}:{} -> {}", 
                        provider.getDescription(), parentType, parentSubType, attributeName, subType);
                return subType;
            }
        }
        
        // Default to string if no context-specific rule found
        return "string";
    }
    
    /**
     * Gets the appropriate subtype for a child element based on its parent context.
     * 
     * @param parentType the type of the parent element
     * @param parentSubType the subtype of the parent element
     * @param childType the type of the child element being created
     * @param childName the name of the child element
     * @return the subtype that should be used for the child, or null if no specific rule applies
     */
    public String getContextSpecificChildSubType(String parentType, String parentSubType, String childType, String childName) {
        ensureInitialized();
        
        for (MetaDataContextProvider provider : providers) {
            String subType = provider.getContextSpecificChildSubType(parentType, parentSubType, childType, childName);
            if (subType != null) {
                log.debug("Context provider [{}] resolved {}:{}:{}:{} -> {}", 
                        provider.getDescription(), parentType, parentSubType, childType, childName, subType);
                return subType;
            }
        }
        
        return null; // No specific rule found
    }
    
    private synchronized void ensureInitialized() {
        if (!initialized) {
            discoverProviders();
            initialized = true;
        }
    }
    
    private void discoverProviders() {
        ServiceLoader<MetaDataContextProvider> serviceLoader = 
                ServiceLoader.load(MetaDataContextProvider.class);
                
        for (MetaDataContextProvider provider : serviceLoader) {
            providers.add(provider);
            log.info("Discovered MetaDataContextProvider: {} (priority: {})", 
                    provider.getDescription(), provider.getPriority());
        }
        
        // Sort by priority (highest first)
        providers.sort(Comparator.comparingInt(MetaDataContextProvider::getPriority).reversed());
        
        log.info("Context discovery complete. Registered {} providers", providers.size());
    }
    
    private static class InstanceHolder {
        private static final MetaDataContextRegistry INSTANCE = new MetaDataContextRegistry();
    }
}