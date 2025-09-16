package com.draagon.meta.registry;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataTypeId;
import com.draagon.meta.validation.ValidationChain;
import com.draagon.meta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service-based registry for MetaData type definitions and factory methods.
 * 
 * <p>This registry replaces the legacy TypesConfig system with a dynamic,
 * service-based approach that supports:</p>
 * 
 * <ul>
 *   <li><strong>Dynamic Type Discovery:</strong> Uses ServiceRegistry to find type providers</li>
 *   <li><strong>OSGI Compatibility:</strong> No global static state, context-aware</li>
 *   <li><strong>Cross-Language Foundation:</strong> No Java class references in type definitions</li>
 *   <li><strong>Validation Enhancement:</strong> Plugins can enhance existing type validation</li>
 *   <li><strong>Factory Methods:</strong> Central location for creating MetaData instances</li>
 * </ul>
 * 
 * @since 6.0.0
 */
public class MetaDataTypeRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(MetaDataTypeRegistry.class);
    
    private final ServiceRegistry serviceRegistry;
    private final Map<MetaDataTypeId, Class<? extends MetaData>> typeHandlers = new ConcurrentHashMap<>();
    private final Map<MetaDataTypeId, ValidationChain<MetaData>> validationChains = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;
    
    /**
     * Create registry with service discovery
     * 
     * @param serviceRegistry Service registry for discovering type providers
     */
    public MetaDataTypeRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = Objects.requireNonNull(serviceRegistry, "ServiceRegistry cannot be null");
        log.debug("Created MetaDataTypeRegistry with {}", serviceRegistry.getDescription());
    }
    
    /**
     * Create registry with default service registry for current environment
     */
    public MetaDataTypeRegistry() {
        this(ServiceRegistryFactory.getDefault());
    }
    
    /**
     * Ensure the registry is initialized (lazy initialization)
     */
    private void ensureInitialized() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    discoverAndRegisterTypes();
                    initialized = true;
                }
            }
        }
    }
    
    /**
     * Main factory method - creates MetaData instances by type and subtype
     * 
     * @param <T> Expected MetaData type
     * @param type Primary type (e.g., "field", "view", "validator")
     * @param subType Specific subtype (e.g., "int", "string", "currency")
     * @param name MetaData instance name
     * @return New MetaData instance
     * @throws MetaDataException if type is not registered or instance creation fails
     */
    @SuppressWarnings("unchecked")
    public <T extends MetaData> T createInstance(String type, String subType, String name) {
        Objects.requireNonNull(type, "Type cannot be null");
        Objects.requireNonNull(subType, "SubType cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        
        MetaDataTypeId typeId = new MetaDataTypeId(type, subType);
        return (T) createInstance(typeId, name);
    }
    
    /**
     * Factory method using MetaDataTypeId
     * 
     * @param <T> Expected MetaData type
     * @param typeId Type identifier
     * @param name MetaData instance name
     * @return New MetaData instance
     */
    @SuppressWarnings("unchecked")
    public <T extends MetaData> T createInstance(MetaDataTypeId typeId, String name) {
        Objects.requireNonNull(typeId, "TypeId cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        
        ensureInitialized();
        
        Class<? extends MetaData> handlerClass = typeHandlers.get(typeId);
        if (handlerClass == null) {
            throw new MetaDataException(
                "No handler registered for type: " + typeId.toQualifiedName() + 
                ". Available types: " + getRegisteredTypes()
            );
        }
        
        try {
            // Try the standard 3-parameter constructor first
            Constructor<? extends MetaData> constructor = handlerClass.getConstructor(
                String.class, String.class, String.class);
            T instance = (T) constructor.newInstance(typeId.type(), typeId.subType(), name);
            
            log.debug("Created {} instance: {}", typeId.toQualifiedName(), name);
            return instance;
            
        } catch (NoSuchMethodException e) {
            // Fall back to other constructor patterns for backward compatibility
            try {
                Constructor<? extends MetaData> constructor2 = handlerClass.getConstructor(
                    String.class, String.class);
                return (T) constructor2.newInstance(typeId.type(), name);
            } catch (NoSuchMethodException e2) {
                try {
                    Constructor<? extends MetaData> constructor1 = handlerClass.getConstructor(String.class);
                    return (T) constructor1.newInstance(name);
                } catch (Exception e3) {
                    throw new MetaDataException(
                        "No suitable constructor found for type: " + typeId.toQualifiedName() + 
                        " in class: " + handlerClass.getName(), e3);
                }
            } catch (Exception e2) {
                throw new MetaDataException(
                    "Failed to create instance of type: " + typeId.toQualifiedName(), e2);
            }
        } catch (Exception e) {
            throw new MetaDataException(
                "Failed to create instance of type: " + typeId.toQualifiedName() + 
                " with class: " + handlerClass.getName(), e);
        }
    }
    
    /**
     * Register a type handler class
     * 
     * @param typeId Type identifier
     * @param handlerClass Java class that implements this type
     */
    public void registerHandler(MetaDataTypeId typeId, Class<? extends MetaData> handlerClass) {
        Objects.requireNonNull(typeId, "TypeId cannot be null");
        Objects.requireNonNull(handlerClass, "Handler class cannot be null");
        
        Class<? extends MetaData> existing = typeHandlers.get(typeId);
        if (existing != null && !existing.equals(handlerClass)) {
            throw new MetaDataException(
                "Type handler already registered for " + typeId.toQualifiedName() + 
                ". Existing: " + existing.getName() + ", New: " + handlerClass.getName()
            );
        }
        
        typeHandlers.put(typeId, handlerClass);
        
        // Initialize default validation chain for this type
        ValidationChain<MetaData> defaultChain = createDefaultValidationChain(typeId);
        validationChains.put(typeId, defaultChain);
        
        log.debug("Registered type handler: {} -> {}", typeId.toQualifiedName(), handlerClass.getSimpleName());
    }
    
    /**
     * Get validation chain for a specific type
     * 
     * @param typeId Type identifier
     * @return ValidationChain for the type, or empty chain if not found
     */
    public ValidationChain<MetaData> getValidationChain(MetaDataTypeId typeId) {
        Objects.requireNonNull(typeId, "TypeId cannot be null");
        
        ensureInitialized();
        
        ValidationChain<MetaData> chain = validationChains.get(typeId);
        return chain != null ? chain : ValidationChain.<MetaData>builder("EmptyChain").build();
    }
    
    /**
     * Enhance validation chain for a type (used by plugins)
     * 
     * @param typeId Type identifier (can use "*" wildcards)
     * @param validator Additional validator to add
     */
    public void enhanceValidationChain(MetaDataTypeId typeId, Validator<MetaData> validator) {
        Objects.requireNonNull(typeId, "TypeId cannot be null");
        Objects.requireNonNull(validator, "Validator cannot be null");
        
        ensureInitialized();
        
        // Apply to matching types
        for (Map.Entry<MetaDataTypeId, ValidationChain<MetaData>> entry : validationChains.entrySet()) {
            if (entry.getKey().matches(typeId)) {
                entry.getValue().addValidator(validator);
                log.debug("Enhanced validation for {} with {}", 
                         entry.getKey().toQualifiedName(), validator.getClass().getSimpleName());
            }
        }
    }
    
    /**
     * Check if a type is registered
     * 
     * @param typeId Type identifier
     * @return true if registered
     */
    public boolean isRegistered(MetaDataTypeId typeId) {
        ensureInitialized();
        return typeHandlers.containsKey(typeId);
    }
    
    /**
     * Get all registered type identifiers
     * 
     * @return Set of registered MetaDataTypeId instances
     */
    public Set<MetaDataTypeId> getRegisteredTypes() {
        ensureInitialized();
        return Set.copyOf(typeHandlers.keySet());
    }
    
    /**
     * Get registered types for a specific primary type
     * 
     * @param type Primary type (e.g., "field")
     * @return Set of MetaDataTypeId instances with that primary type
     */
    public Set<MetaDataTypeId> getRegisteredTypes(String type) {
        Objects.requireNonNull(type, "Type cannot be null");
        
        ensureInitialized();
        return typeHandlers.keySet().stream()
            .filter(typeId -> type.equals(typeId.type()))
            .collect(Collectors.toSet());
    }
    
    /**
     * Get handler class for a type
     * 
     * @param typeId Type identifier
     * @return Handler class or null if not registered
     */
    public Class<? extends MetaData> getHandlerClass(MetaDataTypeId typeId) {
        ensureInitialized();
        return typeHandlers.get(typeId);
    }
    
    /**
     * Clear all registered types (primarily for testing)
     */
    public void clear() {
        typeHandlers.clear();
        validationChains.clear();
        initialized = false;
        log.debug("Cleared all registered types");
    }
    
    /**
     * Get registry statistics
     * 
     * @return RegistryStats with counts and information
     */
    public RegistryStats getStats() {
        ensureInitialized();
        
        Map<String, Integer> typeCount = new HashMap<>();
        for (MetaDataTypeId typeId : typeHandlers.keySet()) {
            typeCount.merge(typeId.type(), 1, Integer::sum);
        }
        
        return new RegistryStats(
            typeHandlers.size(),
            typeCount,
            serviceRegistry.getDescription()
        );
    }
    
    /**
     * Service discovery - find and register all type providers
     */
    private void discoverAndRegisterTypes() {
        log.debug("Discovering type providers via {}", serviceRegistry.getDescription());
        
        try {
            Collection<MetaDataTypeProvider> providers = serviceRegistry.getServices(MetaDataTypeProvider.class);
            
            log.info("Found {} MetaDataTypeProvider services", providers.size());
            
            for (MetaDataTypeProvider provider : providers) {
                try {
                    log.debug("Registering types from provider: {}", provider.getClass().getName());
                    provider.registerTypes(this);
                    
                    log.debug("Enhancing validation from provider: {}", provider.getClass().getName());
                    provider.enhanceValidation(this);
                    
                } catch (Exception e) {
                    log.error("Error processing type provider {}: {}", 
                             provider.getClass().getName(), e.getMessage(), e);
                }
            }
            
            log.info("Type discovery complete. Registered {} types", typeHandlers.size());
            
        } catch (Exception e) {
            log.error("Error during type discovery: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Create default validation chain for a type
     */
    private ValidationChain<MetaData> createDefaultValidationChain(MetaDataTypeId typeId) {
        return ValidationChain.<MetaData>builder("DefaultChain-" + typeId.toQualifiedName())
            .continueOnError()
            .build();
    }
    
    /**
     * Registry statistics record
     */
    public record RegistryStats(
        int totalTypes,
        Map<String, Integer> typesByPrimary,
        String serviceRegistryDescription
    ) {}
}