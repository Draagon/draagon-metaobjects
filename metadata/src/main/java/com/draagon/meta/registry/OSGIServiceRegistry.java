package com.draagon.meta.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OSGI-based service registry implementation.
 * 
 * <p><strong>Note:</strong> This implementation requires OSGI to be available at runtime.
 * It uses reflection to avoid compile-time dependencies on OSGI classes, making
 * the metadata module work in non-OSGI environments.</p>
 * 
 * <p>If OSGI BundleContext is not available, this registry will fall back to
 * manual service registration only.</p>
 * 
 * @since 6.0.0
 */
public class OSGIServiceRegistry implements ServiceRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(OSGIServiceRegistry.class);
    
    private final Object bundleContext; // Actually BundleContext but avoid compile dependency
    private final Map<Class<?>, Set<Object>> manualServices = new ConcurrentHashMap<>();
    private volatile boolean closed = false;
    
    /**
     * Create OSGI service registry with BundleContext
     * 
     * @param bundleContext OSGI BundleContext (passed as Object to avoid compile dependency)
     */
    public OSGIServiceRegistry(Object bundleContext) {
        this.bundleContext = bundleContext;
        
        if (bundleContext == null) {
            log.warn("BundleContext is null - OSGI service discovery will not work");
        } else {
            log.debug("Created OSGIServiceRegistry with BundleContext: {}", bundleContext);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> getServices(Class<T> serviceClass) {
        if (closed) {
            throw new IllegalStateException("ServiceRegistry has been closed");
        }
        
        Objects.requireNonNull(serviceClass, "Service class cannot be null");
        
        Set<T> services = new LinkedHashSet<>();
        
        try {
            // 1. Try OSGI service discovery via reflection
            if (bundleContext != null) {
                try {
                    // Use reflection to call: bundleContext.getServiceReferences(serviceClass, null)
                    Class<?> bundleContextClass = bundleContext.getClass();
                    var getServiceReferencesMethod = bundleContextClass.getMethod(
                        "getServiceReferences", Class.class, String.class);
                    
                    Object[] serviceReferences = (Object[]) getServiceReferencesMethod.invoke(
                        bundleContext, serviceClass, null);
                    
                    if (serviceReferences != null) {
                        // Use reflection to call: bundleContext.getService(serviceReference)  
                        var getServiceMethod = bundleContextClass.getMethod("getService", 
                            Class.forName("org.osgi.framework.ServiceReference"));
                        
                        for (Object serviceRef : serviceReferences) {
                            Object service = getServiceMethod.invoke(bundleContext, serviceRef);
                            if (serviceClass.isInstance(service)) {
                                services.add((T) service);
                                log.debug("Discovered OSGI service: {} -> {}", 
                                         serviceClass.getSimpleName(), service.getClass().getName());
                            }
                        }
                    }
                    
                } catch (Exception e) {
                    log.debug("Failed to discover OSGI services for {}: {}", 
                             serviceClass.getName(), e.getMessage());
                    // Fall through to manual services only
                }
            }
            
            // 2. Add manually registered services
            Set<Object> manual = manualServices.get(serviceClass);
            if (manual != null) {
                synchronized (manual) {
                    for (Object service : manual) {
                        if (serviceClass.isInstance(service)) {
                            services.add((T) service);
                            log.debug("Added manual OSGI service: {} -> {}", 
                                     serviceClass.getSimpleName(), service.getClass().getName());
                        }
                    }
                }
            }
            
            log.debug("Found {} services for {} in OSGI environment", 
                     services.size(), serviceClass.getSimpleName());
            
        } catch (Exception e) {
            log.error("Error loading OSGI services for {}: {}", serviceClass.getName(), e.getMessage(), e);
        }
        
        return services;
    }
    
    @Override
    public <T> void registerService(Class<T> serviceClass, T service) {
        if (closed) {
            throw new IllegalStateException("ServiceRegistry has been closed");
        }
        
        Objects.requireNonNull(serviceClass, "Service class cannot be null");
        Objects.requireNonNull(service, "Service instance cannot be null");
        
        if (!serviceClass.isInstance(service)) {
            throw new IllegalArgumentException(
                "Service " + service.getClass().getName() + 
                " does not implement " + serviceClass.getName()
            );
        }
        
        // For now, we only support manual registration
        // Full OSGI service registration would require proper ServiceRegistration handling
        manualServices.computeIfAbsent(serviceClass, k -> ConcurrentHashMap.newKeySet())
                     .add(service);
        
        log.debug("Manually registered OSGI service: {} -> {}", 
                 serviceClass.getSimpleName(), service.getClass().getName());
    }
    
    @Override
    public <T> boolean unregisterService(Class<T> serviceClass, T service) {
        if (closed) {
            return false;
        }
        
        Objects.requireNonNull(serviceClass, "Service class cannot be null");
        Objects.requireNonNull(service, "Service instance cannot be null");
        
        Set<Object> services = manualServices.get(serviceClass);
        if (services != null) {
            boolean removed = services.remove(service);
            if (removed) {
                log.debug("Unregistered manual OSGI service: {} -> {}", 
                         serviceClass.getSimpleName(), service.getClass().getName());
                
                // Clean up empty sets
                if (services.isEmpty()) {
                    manualServices.remove(serviceClass);
                }
            }
            return removed;
        }
        
        return false;
    }
    
    @Override
    public boolean isOSGIEnvironment() {
        return true;
    }
    
    @Override
    public String getDescription() {
        return "OSGI BundleContext" + (bundleContext != null ? " (" + bundleContext + ")" : " (null)");
    }
    
    @Override
    public void close() {
        if (!closed) {
            log.debug("Closing OSGIServiceRegistry");
            manualServices.clear();
            closed = true;
        }
    }
    
    /**
     * Get the BundleContext (for OSGI-specific operations)
     * 
     * @return BundleContext as Object (to avoid compile dependency)
     */
    public Object getBundleContext() {
        return bundleContext;
    }
    
    /**
     * Check if BundleContext is available
     * 
     * @return true if BundleContext is not null
     */
    public boolean hasBundleContext() {
        return bundleContext != null;
    }
}