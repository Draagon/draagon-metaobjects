package com.metaobjects.registry;

import java.util.Collection;

/**
 * Service registry abstraction that works in both OSGI and non-OSGI environments.
 * 
 * <p>This abstraction allows the MetaData type system to discover and register
 * service providers dynamically regardless of the runtime environment:</p>
 * 
 * <ul>
 *   <li><strong>OSGI:</strong> Uses BundleContext to find services across bundles</li>
 *   <li><strong>Non-OSGI:</strong> Uses Java ServiceLoader for provider discovery</li>
 *   <li><strong>Testing:</strong> Allows manual service registration for unit tests</li>
 * </ul>
 * 
 * @since 6.0.0
 */
public interface ServiceRegistry {
    
    /**
     * Get all services of the specified type
     * 
     * @param <T> The service type
     * @param serviceClass The service interface class
     * @return Collection of all registered services of that type
     */
    <T> Collection<T> getServices(Class<T> serviceClass);
    
    /**
     * Register a service instance
     * 
     * @param <T> The service type
     * @param serviceClass The service interface class
     * @param service The service instance to register
     */
    <T> void registerService(Class<T> serviceClass, T service);
    
    /**
     * Unregister a service instance
     * 
     * @param <T> The service type
     * @param serviceClass The service interface class
     * @param service The service instance to unregister
     * @return true if the service was found and removed
     */
    <T> boolean unregisterService(Class<T> serviceClass, T service);
    
    /**
     * Check if running in an OSGI environment
     * 
     * @return true if OSGI is available
     */
    boolean isOSGIEnvironment();
    
    /**
     * Get a human-readable description of this registry
     * 
     * @return Description like "OSGI BundleContext" or "Java ServiceLoader"
     */
    String getDescription();
    
    /**
     * Handle OSGI bundle lifecycle events
     * 
     * <p>This method is called when bundle lifecycle events occur in OSGI environments.
     * Non-OSGI implementations can provide a no-op implementation.</p>
     * 
     * @param bundle OSGI Bundle that had a lifecycle event (as Object to avoid compile dependency)
     */
    default void onBundleEvent(Object bundle) {
        // Default no-op implementation for non-OSGI registries
    }
    
    /**
     * Clean up services specific to a bundle
     * 
     * <p>This method is called when a bundle is being unloaded to clean up
     * any services or references specific to that bundle.</p>
     * 
     * @param bundle OSGI Bundle to clean up (as Object to avoid compile dependency)
     */
    default void cleanupForBundle(Object bundle) {
        // Default no-op implementation for non-OSGI registries
    }
    
    /**
     * Check if bundle lifecycle management is supported and active
     * 
     * @return true if this registry supports and is actively handling bundle lifecycle events
     */
    default boolean isBundleLifecycleActive() {
        return false; // Default: no bundle lifecycle support
    }
    
    /**
     * Get status information about bundle lifecycle management
     * 
     * @return Status description, or "Not supported" for non-OSGI registries
     */
    default String getBundleLifecycleStatus() {
        return "Not supported";
    }
    
    /**
     * Close/cleanup the service registry
     */
    void close();
}