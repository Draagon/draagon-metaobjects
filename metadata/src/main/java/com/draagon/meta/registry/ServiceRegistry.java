package com.draagon.meta.registry;

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
     * Close/cleanup the service registry
     */
    void close();
}