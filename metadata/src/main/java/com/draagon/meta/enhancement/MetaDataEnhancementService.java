package com.draagon.meta.enhancement;

import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.registry.ServiceRegistry;
import com.draagon.meta.registry.ServiceRegistryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * v6.0.0: Central service for enhancing MetaData instances with additional attributes and capabilities.
 * This replaces the TypesConfig overlay system with a flexible, service-based approach.
 * 
 * The service discovers MetaDataAttributeProvider and MetaDataEnhancer implementations
 * and applies them to MetaData instances based on context.
 */
public class MetaDataEnhancementService {
    
    private static final Logger log = LoggerFactory.getLogger(MetaDataEnhancementService.class);
    
    private final ServiceRegistry serviceRegistry;
    private final List<MetaDataAttributeProvider> attributeProviders;
    private final List<MetaDataEnhancer> enhancers;
    private final Map<String, List<AttributeDefinition>> attributeCache;
    
    public MetaDataEnhancementService() {
        this(ServiceRegistryFactory.getDefault());
    }
    
    public MetaDataEnhancementService(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.attributeCache = new ConcurrentHashMap<>();
        
        // Discover all attribute providers
        this.attributeProviders = serviceRegistry.getServices(MetaDataAttributeProvider.class)
            .stream()
            .sorted(Comparator.comparingInt(MetaDataAttributeProvider::getPriority))
            .collect(Collectors.toList());
            
        // Discover all enhancers
        this.enhancers = serviceRegistry.getServices(MetaDataEnhancer.class)
            .stream()
            .sorted(Comparator.comparingInt(MetaDataEnhancer::getPriority))
            .collect(Collectors.toList());
            
        log.info("Initialized MetaDataEnhancementService with {} attribute providers and {} enhancers",
                attributeProviders.size(), enhancers.size());
    }
    
    /**
     * Enhance MetaData for specific service context
     * @param metaData The MetaData instance to enhance
     * @param serviceName Name of the service requesting enhancement (e.g., "objectManagerDB", "jsonIO")
     * @param contextProperties Additional context properties
     */
    public void enhanceForService(MetaData metaData, String serviceName, Map<String, Object> contextProperties) {
        EnhancementContext context = EnhancementContext.builder()
            .serviceName(serviceName)
            .properties(contextProperties != null ? contextProperties : Collections.emptyMap())
            .build();
            
        enhanceWithContext(metaData, context);
    }
    
    /**
     * Enhance MetaData with specific context
     * @param metaData The MetaData instance to enhance
     * @param context Enhancement context
     */
    public void enhanceWithContext(MetaData metaData, EnhancementContext context) {
        if (metaData == null) {
            return;
        }
        
        log.debug("Enhancing MetaData [{}] for context: {}", metaData.getName(), context);
        
        // Apply all relevant enhancers
        for (MetaDataEnhancer enhancer : enhancers) {
            if (isEnhancerApplicable(enhancer, context)) {
                try {
                    boolean applied = enhancer.enhance(metaData, context);
                    if (applied) {
                        log.debug("Applied enhancer [{}] to MetaData [{}]", 
                                enhancer.getEnhancerId(), metaData.getName());
                    }
                } catch (Exception e) {
                    log.error("Error applying enhancer [{}] to MetaData [{}]: {}", 
                            enhancer.getEnhancerId(), metaData.getName(), e.getMessage(), e);
                }
            }
        }
    }
    
    /**
     * Load all available attributes into MetaData
     * @param metaData The MetaData instance to enhance with attributes
     */
    public void loadAllAttributes(MetaData metaData) {
        if (metaData == null) {
            return;
        }
        
        String metaDataType = metaData.getType();
        List<AttributeDefinition> applicableAttributes = getAttributesForType(metaDataType);
        
        log.debug("Loading {} attributes for MetaData [{}] of type [{}]", 
                applicableAttributes.size(), metaData.getName(), metaDataType);
        
        for (AttributeDefinition attrDef : applicableAttributes) {
            addAttributeToMetaData(metaData, attrDef);
        }
    }
    
    /**
     * Load specific attributes from a provider
     * @param metaData The MetaData instance to enhance
     * @param providerId The ID of the attribute provider
     */
    public void loadAttributesFromProvider(MetaData metaData, String providerId) {
        MetaDataAttributeProvider provider = attributeProviders.stream()
            .filter(p -> providerId.equals(p.getProviderId()))
            .findFirst()
            .orElse(null);
            
        if (provider == null) {
            log.warn("Attribute provider [{}] not found", providerId);
            return;
        }
        
        String metaDataType = metaData.getType();
        for (AttributeDefinition attrDef : provider.getAttributeDefinitions()) {
            if (attrDef.getApplicableTypes().contains(metaDataType)) {
                addAttributeToMetaData(metaData, attrDef);
            }
        }
    }
    
    /**
     * Get all attribute definitions applicable to a specific type
     * @param metaDataType The MetaData type (e.g., "object", "field", "attr")
     * @return List of applicable attribute definitions
     */
    public List<AttributeDefinition> getAttributesForType(String metaDataType) {
        return attributeCache.computeIfAbsent(metaDataType, type -> 
            attributeProviders.stream()
                .flatMap(provider -> provider.getAttributeDefinitions().stream())
                .filter(attr -> attr.getApplicableTypes().contains(type))
                .collect(Collectors.toList())
        );
    }
    
    /**
     * Get all registered attribute providers
     * @return List of attribute providers
     */
    public List<MetaDataAttributeProvider> getAttributeProviders() {
        return Collections.unmodifiableList(attributeProviders);
    }
    
    /**
     * Get all registered enhancers
     * @return List of enhancers
     */
    public List<MetaDataEnhancer> getEnhancers() {
        return Collections.unmodifiableList(enhancers);
    }
    
    /**
     * Clear attribute cache (useful after adding new providers dynamically)
     */
    public void clearCache() {
        attributeCache.clear();
    }
    
    private boolean isEnhancerApplicable(MetaDataEnhancer enhancer, EnhancementContext context) {
        if (context.getServiceName() == null) {
            return false;
        }
        
        Collection<String> supportedServices = enhancer.getSupportedServices();
        return supportedServices.contains(context.getServiceName()) || 
               supportedServices.contains("*"); // Wildcard for all services
    }
    
    private void addAttributeToMetaData(MetaData metaData, AttributeDefinition attrDef) {
        try {
            // Check if attribute already exists
            if (metaData.hasChild(attrDef.getName(), MetaAttribute.class)) {
                log.debug("Attribute [{}] already exists on MetaData [{}], skipping", 
                        attrDef.getName(), metaData.getName());
                return;
            }
            
            // Create MetaAttribute instance using registry
            MetaAttribute attribute = (MetaAttribute) metaData.getLoader()
                .getTypeRegistry()
                .createInstance(attrDef.getType(), attrDef.getSubType(), attrDef.getName());
                
            // Set default value if provided
            if (attrDef.getDefaultValue() != null) {
                attribute.setValueAsObject(attrDef.getDefaultValue());
            }
            
            // Add to MetaData
            metaData.addChild(attribute);
            
            log.debug("Added attribute [{}] to MetaData [{}]", attrDef.getName(), metaData.getName());
            
        } catch (Exception e) {
            log.error("Error adding attribute [{}] to MetaData [{}]: {}", 
                    attrDef.getName(), metaData.getName(), e.getMessage(), e);
        }
    }
}