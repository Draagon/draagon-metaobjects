package com.metaobjects.spring;

import com.metaobjects.MetaData;
import com.metaobjects.MetaDataNotFoundException;
import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.object.MetaObject;
import com.metaobjects.registry.MetaDataLoaderRegistry;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

/**
 * Spring Service wrapper for MetaData operations.
 * 
 * <p>Provides a clean, convenient API for the most common MetaData operations
 * while hiding the complexity of the underlying OSGi-compatible registry system.</p>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>{@code
 * @RestController
 * public class ApiController {
 *     
 *     @Autowired
 *     private MetaDataService metaDataService;
 *     
 *     @GetMapping("/api/users/{id}")
 *     public ResponseEntity<?> getUser(@PathVariable String id) {
 *         // Find metadata for any object type
 *         MetaObject userMeta = metaDataService.findMetaObject(userObject);
 *         
 *         // Or find by name
 *         MetaObject userMeta2 = metaDataService.findMetaObjectByName("User");
 *         
 *         // Get all objects for a service
 *         Collection<MetaObject> allObjects = metaDataService.getAllMetaObjects();
 *         
 *         return ResponseEntity.ok(userMeta);
 *     }
 * }
 * }</pre>
 * 
 * @since 6.0.0
 */
@Service
public class MetaDataService {
    
    private final MetaDataLoaderRegistry registry;
    
    /**
     * Creates service with the provided registry
     */
    public MetaDataService(MetaDataLoaderRegistry registry) {
        this.registry = registry;
    }
    
    /**
     * Find MetaObject by name across all registered loaders.
     * 
     * @param name Fully qualified metadata name (e.g., "com.example::User")
     * @return MetaObject instance
     * @throws MetaDataNotFoundException if not found
     */
    public MetaObject findMetaObjectByName(String name) throws MetaDataNotFoundException {
        return registry.findMetaObjectByName(name);
    }
    
    /**
     * Find MetaObject by name, returning Optional for null-safe handling.
     * 
     * @param name Fully qualified metadata name
     * @return Optional containing MetaObject if found
     */
    public Optional<MetaObject> findMetaObjectByNameOptional(String name) {
        try {
            return Optional.of(findMetaObjectByName(name));
        } catch (MetaDataNotFoundException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Find the MetaObject that can handle the given object instance.
     * 
     * @param obj Object instance to find metadata for
     * @return MetaObject that can handle this object
     * @throws MetaDataNotFoundException if no suitable MetaObject found
     */
    public MetaObject findMetaObject(Object obj) throws MetaDataNotFoundException {
        return registry.findMetaObject(obj);
    }
    
    /**
     * Find MetaObject for object instance, returning Optional for null-safe handling.
     * 
     * @param obj Object instance to find metadata for
     * @return Optional containing MetaObject if found
     */
    public Optional<MetaObject> findMetaObjectOptional(Object obj) {
        try {
            return Optional.of(findMetaObject(obj));
        } catch (MetaDataNotFoundException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Get all MetaObjects from all registered loaders.
     * 
     * @return Collection of all available MetaObjects
     */
    public Collection<MetaObject> getAllMetaObjects() {
        Collection<MetaObject> allMetaObjects = new java.util.ArrayList<>();
        for (MetaDataLoader loader : registry.getDataLoaders()) {
            allMetaObjects.addAll(loader.getChildren(MetaObject.class));
        }
        return allMetaObjects;
    }
    
    /**
     * Get all registered MetaDataLoaders.
     * 
     * @return Collection of registered loaders
     */
    public Collection<MetaDataLoader> getAllLoaders() {
        return registry.getDataLoaders();
    }
    
    /**
     * Find MetaData by name and type across all loaders.
     * 
     * @param name Name to search for
     * @param type Type to filter by
     * @return MetaData instance of the specified type
     * @throws MetaDataNotFoundException if not found
     */
    @SuppressWarnings("unchecked")
    public <T extends MetaData> T findMetaDataByName(String name, Class<T> type) 
            throws MetaDataNotFoundException {
        return registry.findMetaDataByName(type, name);
    }
    
    /**
     * Find MetaData by name and type, returning Optional for null-safe handling.
     * 
     * @param name Name to search for
     * @param type Type to filter by
     * @return Optional containing MetaData if found
     */
    public <T extends MetaData> Optional<T> findMetaDataByNameOptional(String name, Class<T> type) {
        try {
            return Optional.of(findMetaDataByName(name, type));
        } catch (MetaDataNotFoundException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Check if a MetaObject with the given name exists.
     * 
     * @param name Name to check for
     * @return true if MetaObject exists
     */
    public boolean metaObjectExists(String name) {
        return findMetaObjectByNameOptional(name).isPresent();
    }
    
    /**
     * Get the underlying registry for advanced operations.
     * 
     * <p>Most users should use the convenience methods above, but this provides
     * access to the full registry API when needed.</p>
     * 
     * @return The underlying MetaDataLoaderRegistry
     */
    public MetaDataLoaderRegistry getRegistry() {
        return registry;
    }
}