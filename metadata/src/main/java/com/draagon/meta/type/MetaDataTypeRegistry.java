package com.draagon.meta.type;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe registry for MetaData type definitions that supports both
 * core types and plugin-registered custom types.
 * 
 * This replaces the string-based type system with a more robust,
 * extensible approach that provides compile-time safety where possible
 * and runtime validation for dynamically registered types.
 */
public class MetaDataTypeRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(MetaDataTypeRegistry.class);
    
    private static volatile MetaDataTypeRegistry instance;
    private static final Object INSTANCE_LOCK = new Object();
    
    private final Map<String, MetaDataTypeDefinition> types = new ConcurrentHashMap<>();
    
    /**
     * Singleton instance accessor
     */
    public static MetaDataTypeRegistry getInstance() {
        if (instance == null) {
            synchronized (INSTANCE_LOCK) {
                if (instance == null) {
                    instance = new MetaDataTypeRegistry();
                    instance.registerCoreTypes();
                }
            }
        }
        return instance;
    }
    
    /**
     * Package-private constructor for testing
     */
    MetaDataTypeRegistry() {
    }
    
    /**
     * Register a new MetaData type definition
     */
    public synchronized void registerType(MetaDataTypeDefinition typeDefinition) {
        String typeName = typeDefinition.typeName();
        
        if (types.containsKey(typeName)) {
            MetaDataTypeDefinition existing = types.get(typeName);
            if (!existing.implementationClass().equals(typeDefinition.implementationClass())) {
                throw new MetaDataException(
                    "Type already registered with different implementation: " + typeName +
                    ". Existing: " + existing.implementationClass().getName() +
                    ", New: " + typeDefinition.implementationClass().getName()
                );
            }
            log.warn("Type definition already exists, skipping: {}", typeName);
            return;
        }
        
        types.put(typeName, typeDefinition);
        log.debug("Registered MetaData type: {} -> {}", typeName, typeDefinition.implementationClass().getSimpleName());
    }
    
    /**
     * Get type definition by name
     */
    public Optional<MetaDataTypeDefinition> getType(String typeName) {
        return Optional.ofNullable(types.get(typeName));
    }
    
    /**
     * Get type definition by name, throwing exception if not found
     */
    public MetaDataTypeDefinition requireType(String typeName) {
        return getType(typeName)
            .orElseThrow(() -> new MetaDataException("Unknown MetaData type: " + typeName));
    }
    
    /**
     * Check if a type is registered
     */
    public boolean hasType(String typeName) {
        return types.containsKey(typeName);
    }
    
    /**
     * Get all registered type names
     */
    public Set<String> getRegisteredTypes() {
        return Set.copyOf(types.keySet());
    }
    
    /**
     * Get all type definitions
     */
    public Set<MetaDataTypeDefinition> getAllTypeDefinitions() {
        return Set.copyOf(types.values());
    }
    
    /**
     * Get type definitions by implementation class
     */
    public Set<MetaDataTypeDefinition> getTypesByImplementationClass(Class<? extends MetaData> implementationClass) {
        return types.values().stream()
            .filter(def -> def.implementationClass().equals(implementationClass))
            .collect(Collectors.toSet());
    }
    
    /**
     * Validate if a subtype is allowed for the given type
     */
    public boolean isSubTypeValid(String typeName, String subType) {
        return getType(typeName)
            .map(def -> def.isSubTypeAllowed(subType))
            .orElse(false);
    }
    
    /**
     * Create a new MetaData instance of the specified type
     */
    public <T extends MetaData> T createInstance(String typeName, String subType, String name) {
        MetaDataTypeDefinition definition = requireType(typeName);
        
        if (!definition.isSubTypeAllowed(subType)) {
            throw new MetaDataException(
                "SubType '" + subType + "' is not allowed for type '" + typeName + "'. " +
                "Allowed subtypes: " + definition.allowedSubTypes()
            );
        }
        
        try {
            // Try constructor with type, subType, name
            try {
                var constructor = definition.implementationClass()
                    .getConstructor(String.class, String.class, String.class);
                return (T) constructor.newInstance(typeName, subType, name);
            } catch (NoSuchMethodException e) {
                // Fallback to legacy constructors for backward compatibility
                try {
                    var constructor = definition.implementationClass()
                        .getConstructor(String.class, String.class);
                    return (T) constructor.newInstance(subType, name);
                } catch (NoSuchMethodException e2) {
                    var constructor = definition.implementationClass()
                        .getConstructor(String.class);
                    return (T) constructor.newInstance(name);
                }
            }
        } catch (Exception e) {
            throw new MetaDataException(
                "Failed to create instance of type: " + typeName + 
                " with implementation: " + definition.implementationClass().getName(), e
            );
        }
    }
    
    /**
     * Register core MetaData types that ship with the framework
     */
    private void registerCoreTypes() {
        try {
            // Register core types using reflection to avoid circular dependencies
            // These will be replaced with proper references once other classes are updated
            
            registerCoreType("attr", "MetaAttribute", "Metadata attribute");
            registerCoreType("field", "MetaField", "Metadata field");  
            registerCoreType("object", "MetaObject", "Metadata object");
            registerCoreType("loader", "MetaDataLoader", "Metadata loader");
            registerCoreType("view", "MetaView", "Metadata view");
            registerCoreType("validator", "MetaValidator", "Metadata validator");
            
        } catch (Exception e) {
            log.error("Failed to register core MetaData types", e);
        }
    }
    
    /**
     * Helper method to register core types by name
     */
    @SuppressWarnings("unchecked")
    private void registerCoreType(String typeName, String className, String description) {
        try {
            // Try to load the class - if it doesn't exist yet, we'll register it later
            String fullClassName = "com.draagon.meta." + 
                (typeName.equals("attr") ? "attr.MetaAttribute" :
                 typeName.equals("field") ? "field.MetaField" :
                 typeName.equals("object") ? "object.MetaObject" :
                 typeName.equals("loader") ? "loader.MetaDataLoader" :
                 typeName.equals("view") ? "view.MetaView" :
                 typeName.equals("validator") ? "validator.MetaValidator" :
                 className);
            
            Class<?> clazz = Class.forName(fullClassName);
            if (MetaData.class.isAssignableFrom(clazz)) {
                MetaDataTypeDefinition definition = MetaDataTypeDefinition.builder(
                    typeName, (Class<? extends MetaData>) clazz)
                    .description(description)
                    .build();
                types.put(typeName, definition);
                log.debug("Registered core type: {}", typeName);
            }
        } catch (ClassNotFoundException e) {
            log.debug("Core type class not found yet, will register later: {}", className);
        }
    }
    
    /**
     * Clear all registered types (primarily for testing)
     */
    protected synchronized void clear() {
        types.clear();
    }
    
    /**
     * Get registry statistics for monitoring
     */
    public RegistryStats getStats() {
        return new RegistryStats(
            types.size(),
            types.values().stream().mapToInt(def -> def.allowedSubTypes().size()).sum(),
            types.values().stream().filter(MetaDataTypeDefinition::isAbstract).count()
        );
    }
    
    /**
     * Registry statistics record
     */
    public record RegistryStats(
        int totalTypes,
        int totalAllowedSubTypes,
        long abstractTypes
    ) {}
}