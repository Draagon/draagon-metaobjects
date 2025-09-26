package com.draagon.meta.registry;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.MetaDataConfigurationException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for runtime MetaDataLoader management in OSGI and non-OSGI environments.
 *
 * <p>This registry provides dynamic create/destroy capabilities for MetaDataLoader instances,
 * supporting legitimate runtime use cases such as:</p>
 *
 * <ul>
 *   <li><strong>Multi-tenant Applications:</strong> Different metadata sources per tenant</li>
 *   <li><strong>Plugin Systems:</strong> Runtime loader registration/unregistration</li>
 *   <li><strong>Configuration Switching:</strong> Dev/test/prod metadata sources</li>
 *   <li><strong>Hot Reloading:</strong> Update metadata sources without restart</li>
 * </ul>
 *
 * <p>For simple single-loader scenarios, consider using MetaDataLoader directly instead.</p>
 *
 * @since 6.0.0
 */
public class MetaDataLoaderRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(MetaDataLoaderRegistry.class);
    
    private final ServiceRegistry serviceRegistry;
    private final Map<String, MetaDataLoader> loaders = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;
    
    /**
     * Create registry with service discovery
     * 
     * @param serviceRegistry Service registry for discovering loader providers
     */
    public MetaDataLoaderRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = Objects.requireNonNull(serviceRegistry, "ServiceRegistry cannot be null");
        log.debug("Created MetaDataLoaderRegistry with {}", serviceRegistry.getDescription());
    }
    
    /**
     * Create registry with default service registry for current environment
     */
    public MetaDataLoaderRegistry() {
        this(ServiceRegistryFactory.getDefault());
    }
    
    /**
     * Ensure the registry is initialized (simplified - no auto-discovery)
     */
    private void ensureInitialized() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    // Simplified: No auto-discovery of providers
                    // Loaders must be registered explicitly via registerLoader()
                    log.debug("MetaDataLoaderRegistry initialized - ready for explicit loader registration");
                    initialized = true;
                }
            }
        }
    }
    
    /**
     * Register a MetaDataLoader
     * 
     * @param loader The loader to register
     * @throws IllegalStateException if a loader with the same name already exists
     */
    public void registerLoader(MetaDataLoader loader) {
        Objects.requireNonNull(loader, "MetaDataLoader cannot be null");
        Objects.requireNonNull(loader.getName(), "MetaDataLoader name cannot be null");
        
        MetaDataLoader existing = loaders.putIfAbsent(loader.getName(), loader);
        if (existing != null) {
            throw new IllegalStateException(
                "A MetaDataLoader with name [" + loader.getName() + "] is already registered"
            );
        }
        
        log.debug("Registered MetaDataLoader: {}", loader.getName());
    }
    
    /**
     * Unregister a MetaDataLoader
     * 
     * @param loader The loader to unregister
     * @return true if the loader was found and removed
     */
    public boolean unregisterLoader(MetaDataLoader loader) {
        Objects.requireNonNull(loader, "MetaDataLoader cannot be null");
        
        boolean removed = loaders.remove(loader.getName(), loader);
        if (removed) {
            log.debug("Unregistered MetaDataLoader: {}", loader.getName());
        }
        return removed;
    }
    
    /**
     * Get all registered MetaDataLoaders
     * 
     * @return Collection of all loaders
     */
    public Collection<MetaDataLoader> getDataLoaders() {
        ensureInitialized();
        return new ArrayList<>(loaders.values());
    }
    
    /**
     * Get MetaDataLoader by name
     * 
     * @param loaderName Name of the loader
     * @return MetaDataLoader instance
     * @throws MetaDataConfigurationException if not found
     */
    public MetaDataLoader getDataLoader(String loaderName) {
        Objects.requireNonNull(loaderName, "Loader name cannot be null");
        
        ensureInitialized();
        
        MetaDataLoader loader = loaders.get(loaderName);
        if (loader == null) {
            throw new MetaDataConfigurationException(
                "No MetaDataLoader exists with name [" + loaderName + "]");
        }
        
        return loader;
    }
    
    /**
     * Find the first MetaDataLoader that can handle the given object
     * 
     * @param obj Object to find loader for
     * @return MetaDataLoader that can handle the object, or null if none found
     */
    public MetaDataLoader findLoader(Object obj) {
        Objects.requireNonNull(obj, "Object cannot be null");
        
        ensureInitialized();
        
        for (MetaDataLoader loader : loaders.values()) {
            try {
                MetaObject mo = loader.getMetaObjectFor(obj);
                if (mo != null) {
                    log.debug("Found loader {} for object of type {}", 
                             loader.getName(), obj.getClass().getName());
                    return loader;
                }
            } catch (Exception e) {
                // Continue to next loader if this one can't handle the object
                log.trace("Loader {} cannot handle object of type {}: {}", 
                         loader.getName(), obj.getClass().getName(), e.getMessage());
            }
        }
        
        log.debug("No loader found for object of type {}", obj.getClass().getName());
        return null;
    }
    
    /**
     * Find MetaObject for the given object
     * 
     * @param obj Object to find MetaObject for
     * @return MetaObject instance
     * @throws MetaDataNotFoundException if no loader can handle the object
     */
    public MetaObject findMetaObject(Object obj) throws MetaDataNotFoundException {
        Objects.requireNonNull(obj, "Object cannot be null");
        
        // High-performance optimization for MetaObjectAware objects
        if (obj instanceof MetaObjectAware aware) {
            MetaObject mo = aware.getMetaData();
            if (mo != null) {
                log.debug("Retrieved cached MetaObject for MetaObjectAware: {}", 
                         obj.getClass().getName());
                return mo;
            }
        }
        
        MetaDataLoader loader = findLoader(obj);
        if (loader == null) {
            throw new MetaDataNotFoundException(
                "No MetaDataLoader exists for object of class [" + obj.getClass().getName() + "]", 
                obj.getClass().getSimpleName());
        }
        
        MetaObject mo = loader.getMetaObjectFor(obj);
        
        // Cache the MetaObject in the object if possible
        if (obj instanceof MetaObjectAware aware) {
            aware.setMetaData(mo);
        }
        
        return mo;
    }
    
    /**
     * Find MetaObject by name across all loaders
     * 
     * <p><strong>Important:</strong> This traverses ALL loaders. Use {@code getMetaDataByName}
     * if you know the specific loader to use for better performance.</p>
     * 
     * @param name MetaObject name to search for
     * @return MetaObject instance
     * @throws MetaDataNotFoundException if not found in any loader
     */
    public MetaObject findMetaObjectByName(String name) throws MetaDataNotFoundException {
        Objects.requireNonNull(name, "Name cannot be null");
        
        ensureInitialized();
        
        for (MetaDataLoader loader : loaders.values()) {
            try {
                MetaObject mo = loader.getMetaObjectByName(name);
                if (mo != null) {
                    log.debug("Found MetaObject '{}' in loader '{}'", name, loader.getName());
                    return mo;
                }
            } catch (MetaDataNotFoundException e) {
                // Continue searching in other loaders
            }
        }
        
        throw new MetaDataNotFoundException(
            "MetaObject with name [" + name + "] not found in any registered loader", name
        );
    }
    
    /**
     * Find MetaData by name and type across all loaders
     * 
     * @param <T> Expected MetaData type
     * @param type Expected class type
     * @param name MetaData name to search for
     * @return MetaData instance
     * @throws MetaDataNotFoundException if not found in any loader
     */
    @SuppressWarnings("unchecked")
    public <T extends MetaData> T findMetaDataByName(Class<T> type, String name) throws MetaDataNotFoundException {
        Objects.requireNonNull(type, "Type cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        
        ensureInitialized();
        
        for (MetaDataLoader loader : loaders.values()) {
            try {
                T data = (T) loader.getChild(name, type);
                if (data != null) {
                    log.debug("Found MetaData '{}' of type {} in loader '{}'", 
                             name, type.getSimpleName(), loader.getName());
                    return data;
                }
            } catch (MetaDataNotFoundException e) {
                // Continue searching in other loaders
            }
        }
        
        throw new MetaDataNotFoundException(
            "MetaData of type " + type.getSimpleName() + " with name [" + name + 
            "] not found in any registered loader", name
        );
    }
    
    /**
     * Clear all registered loaders (primarily for testing)
     */
    public void clear() {
        loaders.clear();
        initialized = false;
        log.debug("Cleared all registered loaders");
    }
    
    /**
     * Get registry statistics
     * 
     * @return RegistryStats with counts and information
     */
    public RegistryStats getStats() {
        ensureInitialized();
        
        return new RegistryStats(
            loaders.size(),
            Set.copyOf(loaders.keySet()),
            serviceRegistry.getDescription()
        );
    }
    
    
    /**
     * Registry statistics record
     */
    public record RegistryStats(
        int totalLoaders,
        Set<String> loaderNames,
        String serviceRegistryDescription
    ) {}
}