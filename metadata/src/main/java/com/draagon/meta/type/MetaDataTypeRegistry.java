package com.draagon.meta.type;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
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
    
    // Bundle-aware instance management for OSGI environments
    private static final Map<Object, WeakReference<MetaDataTypeRegistry>> bundleInstances = new ConcurrentHashMap<>();
    private static final Object BUNDLE_LOCK = new Object();
    
    private final Map<String, MetaDataTypeDefinition> types = new ConcurrentHashMap<>();
    private final WeakReference<Object> bundleRef; // Bundle reference for OSGI environments
    private final String registryId; // Unique identifier for this registry instance
    
    /**
     * Singleton instance accessor (for non-OSGI environments)
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
     * Get bundle-specific instance for OSGI environments
     * 
     * @param bundle OSGI Bundle (as Object to avoid compile dependency)
     * @return MetaDataTypeRegistry instance specific to this bundle
     */
    public static MetaDataTypeRegistry getInstance(Object bundle) {
        if (bundle == null) {
            return getInstance(); // Fall back to singleton for null bundle
        }
        
        synchronized (BUNDLE_LOCK) {
            // Check if we have an existing instance for this bundle
            WeakReference<MetaDataTypeRegistry> registryRef = bundleInstances.get(bundle);
            MetaDataTypeRegistry registry = null;
            
            if (registryRef != null) {
                registry = registryRef.get();
            }
            
            if (registry == null) {
                // Create new bundle-specific instance
                registry = new MetaDataTypeRegistry(bundle);
                registry.registerCoreTypes();
                bundleInstances.put(bundle, new WeakReference<>(registry));
                
                log.debug("Created new bundle-specific MetaDataTypeRegistry for: {}", getBundleId(bundle));
            } else {
                log.trace("Reusing existing MetaDataTypeRegistry for: {}", getBundleId(bundle));
            }
            
            return registry;
        }
    }
    
    /**
     * Clean up stale bundle references (called periodically or on bundle events)
     */
    public static void cleanupStaleReferences() {
        synchronized (BUNDLE_LOCK) {
            bundleInstances.entrySet().removeIf(entry -> {
                WeakReference<MetaDataTypeRegistry> registryRef = entry.getValue();
                if (registryRef.get() == null) {
                    log.debug("Cleaning up stale registry reference for bundle: {}", getBundleId(entry.getKey()));
                    return true;
                }
                return false;
            });
        }
    }
    
    /**
     * Get bundle identifier for logging
     */
    private static String getBundleId(Object bundle) {
        if (bundle == null) {
            return "null";
        }
        
        try {
            var getSymbolicNameMethod = bundle.getClass().getMethod("getSymbolicName");
            var getBundleIdMethod = bundle.getClass().getMethod("getBundleId");
            
            String symbolicName = (String) getSymbolicNameMethod.invoke(bundle);
            long bundleId = (Long) getBundleIdMethod.invoke(bundle);
            
            return symbolicName + "[" + bundleId + "]";
        } catch (Exception e) {
            return bundle.toString();
        }
    }
    
    /**
     * Package-private constructor for testing
     */
    MetaDataTypeRegistry() {
        this.bundleRef = null;
        this.registryId = "default-" + System.currentTimeMillis();
    }
    
    /**
     * Bundle-aware constructor for OSGI environments
     * 
     * @param bundle OSGI Bundle (as Object to avoid compile dependency)
     */
    private MetaDataTypeRegistry(Object bundle) {
        this.bundleRef = bundle != null ? new WeakReference<>(bundle) : null;
        this.registryId = bundle != null ? getBundleId(bundle) : "unknown-" + System.currentTimeMillis();
        log.debug("Created bundle-aware MetaDataTypeRegistry: {}", registryId);
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
     * Create a new MetaData instance of the specified type
     */
    public <T extends MetaData> T createInstance(String typeName, String subType, String name) {
        MetaDataTypeDefinition definition = requireType(typeName);
        
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
            types.values().stream().filter(MetaDataTypeDefinition::isAbstract).count()
        );
    }
    
    /**
     * Check if this registry is bundle-aware
     * 
     * @return true if this registry is associated with a specific OSGI bundle
     */
    public boolean isBundleAware() {
        return bundleRef != null;
    }
    
    /**
     * Check if the associated bundle is still available
     * 
     * @return true if bundle-aware and bundle has not been GC'd
     */
    public boolean isBundleAvailable() {
        return bundleRef != null && bundleRef.get() != null;
    }
    
    /**
     * Get the registry identifier
     * 
     * @return Unique identifier for this registry instance
     */
    public String getRegistryId() {
        return registryId;
    }
    
    /**
     * Get bundle information if available
     * 
     * @return Bundle description or "not bundle-aware"
     */
    public String getBundleInfo() {
        if (bundleRef == null) {
            return "not bundle-aware";
        }
        
        Object bundle = bundleRef.get();
        if (bundle == null) {
            return "bundle GC'd";
        }
        
        return getBundleId(bundle);
    }
    
    /**
     * Get detailed status of this registry
     * 
     * @return Status description including bundle info and type counts
     */
    public String getDetailedStatus() {
        String staleInfo = (isBundleAware() && !isBundleAvailable()) ? " (STALE - bundle was GC'd)" : "";
        return String.format("MetaDataTypeRegistry[%s] Bundle: %s Types: %d%s", 
            registryId, getBundleInfo(), types.size(), staleInfo);
    }
    
    /**
     * Get global statistics about all registry instances
     * 
     * @return Global registry information
     */
    public static String getGlobalStats() {
        String singletonStatus = instance != null ? "created" : "not created";
        
        synchronized (BUNDLE_LOCK) {
            int activeInstances = 0;
            int staleInstances = 0;
            
            for (Map.Entry<Object, WeakReference<MetaDataTypeRegistry>> entry : bundleInstances.entrySet()) {
                if (entry.getValue().get() != null) {
                    activeInstances++;
                } else {
                    staleInstances++;
                }
            }
            
            return String.format(
                "Global MetaDataTypeRegistry Stats:\\n" +
                "Singleton instance: %s\\n" +
                "Bundle instances: %d\\n" +
                "Active bundle instances: %d\\n" +
                "Stale bundle references: %d",
                singletonStatus, bundleInstances.size(), activeInstances, staleInstances);
        }
    }
    
    /**
     * Registry statistics record
     */
    public record RegistryStats(
        int totalTypes,
        long abstractTypes
    ) {}
}