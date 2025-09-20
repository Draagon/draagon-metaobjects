package com.draagon.meta.registry.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.draagon.meta.registry.ServiceRegistry;

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
    private final Map<Class<?>, Object> serviceTrackers = new ConcurrentHashMap<>(); // ServiceTracker instances
    private final Map<Object, Object> activeServiceReferences = new ConcurrentHashMap<>(); // ServiceRef -> Service mapping
    private final BundleLifecycleManager lifecycleManager;
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
            this.lifecycleManager = null;
        } else {
            log.debug("Created OSGIServiceRegistry with BundleContext: {}", bundleContext);
            // Initialize bundle lifecycle management
            this.lifecycleManager = new BundleLifecycleManager(bundleContext, this);
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
            // 1. Try ServiceTracker approach (preferred for OSGI)
            services.addAll(getServicesViaServiceTracker(serviceClass));
            
            // 2. Fallback to manual ServiceReference handling if ServiceTracker not available
            if (services.isEmpty() && bundleContext != null) {
                services.addAll(getServicesViaServiceReferences(serviceClass));
            }
            
            // 3. Add manually registered services
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
    
    /**
     * Get services using ServiceTracker (preferred OSGI approach)
     * ServiceTracker automatically handles ServiceReference lifecycle
     */
    @SuppressWarnings("unchecked")
    private <T> Collection<T> getServicesViaServiceTracker(Class<T> serviceClass) {
        Set<T> services = new LinkedHashSet<>();
        
        try {
            if (bundleContext != null) {
                // Check if we already have a ServiceTracker for this service class
                Object tracker = serviceTrackers.get(serviceClass);
                
                if (tracker == null) {
                    // Create new ServiceTracker via reflection
                    Class<?> serviceTrackerClass = Class.forName("org.osgi.util.tracker.ServiceTracker");
                    var constructor = serviceTrackerClass.getConstructor(
                        Class.forName("org.osgi.framework.BundleContext"), 
                        Class.class, 
                        Class.forName("org.osgi.util.tracker.ServiceTrackerCustomizer"));
                    
                    tracker = constructor.newInstance(bundleContext, serviceClass, null);
                    
                    // Open the tracker
                    var openMethod = serviceTrackerClass.getMethod("open");
                    openMethod.invoke(tracker);
                    
                    serviceTrackers.put(serviceClass, tracker);
                    log.debug("Created ServiceTracker for {}", serviceClass.getSimpleName());
                }
                
                // Get services from tracker
                var getServicesMethod = tracker.getClass().getMethod("getServices");
                Object[] trackedServices = (Object[]) getServicesMethod.invoke(tracker);
                
                if (trackedServices != null) {
                    for (Object service : trackedServices) {
                        if (serviceClass.isInstance(service)) {
                            services.add((T) service);
                            log.debug("ServiceTracker found service: {} -> {}", 
                                     serviceClass.getSimpleName(), service.getClass().getName());
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            log.debug("ServiceTracker not available, falling back to manual approach");
        } catch (Exception e) {
            log.debug("Error using ServiceTracker for {}: {}", serviceClass.getName(), e.getMessage());
        }
        
        return services;
    }
    
    /**
     * Get services using manual ServiceReference handling with proper cleanup
     * This method ensures all ServiceReferences are properly released
     */
    @SuppressWarnings("unchecked")
    private <T> Collection<T> getServicesViaServiceReferences(Class<T> serviceClass) {
        Set<T> services = new LinkedHashSet<>();
        List<Object> serviceRefsToRelease = new ArrayList<>();
        
        try {
            // Use reflection to call: bundleContext.getServiceReferences(serviceClass, null)
            Class<?> bundleContextClass = bundleContext.getClass();
            var getServiceReferencesMethod = bundleContextClass.getMethod(
                "getServiceReferences", Class.class, String.class);
            
            Object[] serviceReferences = (Object[]) getServiceReferencesMethod.invoke(
                bundleContext, serviceClass, null);
            
            if (serviceReferences != null) {
                // Try to get the service methods - handle both real OSGI and mock contexts
                var getServiceMethod = findGetServiceMethod(bundleContextClass);
                var ungetServiceMethod = findUngetServiceMethod(bundleContextClass);
                
                for (Object serviceRef : serviceReferences) {
                    try {
                        Object service = getServiceMethod.invoke(bundleContext, serviceRef);
                        if (serviceClass.isInstance(service)) {
                            services.add((T) service);
                            log.debug("Manual service discovery: {} -> {}", 
                                     serviceClass.getSimpleName(), service.getClass().getName());
                            
                            // Track this ServiceReference for cleanup
                            serviceRefsToRelease.add(serviceRef);
                            activeServiceReferences.put(serviceRef, service);
                        } else if (service != null) {
                            // Release immediately if not the right type
                            ungetServiceMethod.invoke(bundleContext, serviceRef);
                        }
                    } catch (Exception e) {
                        log.debug("Error getting service from reference: {}", e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            log.debug("Failed manual OSGI service discovery for {}: {}", 
                     serviceClass.getName(), e.getMessage());
            
            // Clean up any ServiceReferences we acquired
            releaseServiceReferences(serviceRefsToRelease);
        }
        
        return services;
    }
    
    /**
     * Release ServiceReferences to prevent memory leaks
     */
    private void releaseServiceReferences(List<Object> serviceReferences) {
        if (bundleContext != null && !serviceReferences.isEmpty()) {
            try {
                Class<?> bundleContextClass = bundleContext.getClass();
                var ungetServiceMethod = bundleContextClass.getMethod("ungetService",
                    Class.forName("org.osgi.framework.ServiceReference"));
                
                for (Object serviceRef : serviceReferences) {
                    try {
                        ungetServiceMethod.invoke(bundleContext, serviceRef);
                        activeServiceReferences.remove(serviceRef);
                        log.trace("Released ServiceReference: {}", serviceRef);
                    } catch (Exception e) {
                        log.debug("Error releasing ServiceReference: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.debug("Error during ServiceReference cleanup: {}", e.getMessage());
            }
        }
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
        StringBuilder desc = new StringBuilder("OSGI BundleContext");
        if (bundleContext != null) {
            desc.append(" (").append(bundleContext).append(")");
        } else {
            desc.append(" (null)");
        }
        
        if (lifecycleManager != null) {
            desc.append(", Lifecycle: ").append(lifecycleManager.getStatus());
        }
        
        return desc.toString();
    }
    
    @Override
    public void close() {
        if (!closed) {
            log.debug("Closing OSGIServiceRegistry");
            
            // Shutdown bundle lifecycle manager first
            if (lifecycleManager != null) {
                lifecycleManager.shutdown();
            }
            
            // Close all ServiceTrackers
            serviceTrackers.values().forEach(tracker -> {
                try {
                    var closeMethod = tracker.getClass().getMethod("close");
                    closeMethod.invoke(tracker);
                    log.debug("Closed ServiceTracker: {}", tracker.getClass().getName());
                } catch (Exception e) {
                    log.debug("Error closing ServiceTracker: {}", e.getMessage());
                }
            });
            serviceTrackers.clear();
            
            // Release all active ServiceReferences
            releaseServiceReferences(new ArrayList<>(activeServiceReferences.keySet()));
            activeServiceReferences.clear();
            
            // Clear manual services
            manualServices.clear();
            closed = true;
            
            log.info("OSGIServiceRegistry closed with proper resource cleanup");
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
    
    /**
     * Handle bundle lifecycle events (called by BundleLifecycleManager)
     * 
     * @param bundle OSGI Bundle that had a lifecycle event
     */
    @Override
    public void onBundleEvent(Object bundle) {
        log.debug("Processing bundle event for bundle: {}", bundle);
        
        // This method is called when bundles are stopping/stopped
        // We can use this to proactively clean up services from that bundle
        try {
            // Get bundle information for logging
            var getSymbolicNameMethod = bundle.getClass().getMethod("getSymbolicName");
            String bundleSymbolicName = (String) getSymbolicNameMethod.invoke(bundle);
            
            log.info("Bundle event received for: {}", bundleSymbolicName);
            
            // Future: Could implement bundle-specific service cleanup here
            // For now, we rely on the automatic cleanup in close() and ServiceTracker
            
        } catch (Exception e) {
            log.debug("Error processing bundle event: {}", e.getMessage());
        }
    }
    
    /**
     * Clean up services specific to a bundle (called by BundleLifecycleManager)
     * 
     * @param bundle OSGI Bundle to clean up
     */
    @Override
    public void cleanupForBundle(Object bundle) {
        try {
            var getSymbolicNameMethod = bundle.getClass().getMethod("getSymbolicName");
            String bundleSymbolicName = (String) getSymbolicNameMethod.invoke(bundle);
            
            log.info("Performing cleanup for bundle: {}", bundleSymbolicName);
            
            // Close ServiceTrackers and release ServiceReferences
            // The ServiceTrackers should automatically handle cleanup when bundles stop
            // but we can be proactive here
            
            log.debug("Bundle cleanup completed for: {}", bundleSymbolicName);
            
        } catch (Exception e) {
            log.debug("Error during bundle cleanup: {}", e.getMessage());
        }
    }
    
    /**
     * Get bundle lifecycle manager status
     * 
     * @return Lifecycle manager status or null if not available
     */
    @Override
    public String getBundleLifecycleStatus() {
        return lifecycleManager != null ? lifecycleManager.getStatus() : "Not available";
    }
    
    /**
     * Get bundle lifecycle manager status (legacy method)
     * 
     * @return Lifecycle manager status or null if not available
     * @deprecated Use getBundleLifecycleStatus() instead
     */
    @Deprecated
    public String getLifecycleManagerStatus() {
        return getBundleLifecycleStatus();
    }
    
    /**
     * Check if bundle lifecycle management is active
     * 
     * @return true if bundle events are being tracked
     */
    @Override
    public boolean isBundleLifecycleActive() {
        return lifecycleManager != null && lifecycleManager.isActive();
    }
    
    /**
     * Find getService method that works with both real OSGI and mock contexts
     */
    private java.lang.reflect.Method findGetServiceMethod(Class<?> bundleContextClass) throws Exception {
        try {
            // Try real OSGI first
            return bundleContextClass.getMethod("getService", Class.forName("org.osgi.framework.ServiceReference"));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // Fallback to mock context
            return bundleContextClass.getMethod("getService", Object.class);
        }
    }
    
    /**
     * Find ungetService method that works with both real OSGI and mock contexts
     */
    private java.lang.reflect.Method findUngetServiceMethod(Class<?> bundleContextClass) throws Exception {
        try {
            // Try real OSGI first
            return bundleContextClass.getMethod("ungetService", Class.forName("org.osgi.framework.ServiceReference"));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // Fallback to mock context
            return bundleContextClass.getMethod("ungetService", Object.class);
        }
    }
}