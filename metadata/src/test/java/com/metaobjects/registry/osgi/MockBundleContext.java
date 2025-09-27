package com.metaobjects.registry.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Mock OSGI BundleContext for testing service registry scenarios.
 * 
 * <p>This class simulates an OSGI BundleContext without requiring actual OSGI dependencies,
 * allowing us to test service discovery and bundle lifecycle management in unit tests.</p>
 * 
 * @since 6.0.0
 */
public class MockBundleContext {
    
    private static final Logger log = LoggerFactory.getLogger(MockBundleContext.class);
    
    private final List<Object> bundleListeners = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<Class<?>, List<Object>> services = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Object, Object> serviceReferences = new ConcurrentHashMap<>();
    private volatile boolean active = true;
    
    /**
     * Add bundle listener (simulates BundleContext.addBundleListener())
     * 
     * @param listener Bundle listener to add
     */
    public void addBundleListener(Object listener) {
        if (active && listener != null) {
            bundleListeners.add(listener);
            log.debug("Added bundle listener: {}", listener.getClass().getSimpleName());
        }
    }
    
    /**
     * Remove bundle listener (simulates BundleContext.removeBundleListener())
     * 
     * @param listener Bundle listener to remove
     */
    public void removeBundleListener(Object listener) {
        boolean removed = bundleListeners.remove(listener);
        if (removed) {
            log.debug("Removed bundle listener: {}", listener.getClass().getSimpleName());
        }
    }
    
    /**
     * Get service references (simulates BundleContext.getServiceReferences())
     * 
     * @param serviceClass Service class
     * @param filter Service filter (ignored in mock)
     * @return Array of service references or null if none found
     */
    public Object[] getServiceReferences(Class<?> serviceClass, String filter) {
        if (!active) {
            return null;
        }
        
        List<Object> serviceList = services.get(serviceClass);
        if (serviceList == null || serviceList.isEmpty()) {
            return null;
        }
        
        // Create mock service references
        List<MockServiceReference> references = new ArrayList<>();
        for (Object service : serviceList) {
            MockServiceReference ref = new MockServiceReference(serviceClass, service);
            serviceReferences.put(ref, service);
            references.add(ref);
        }
        
        return references.toArray(new Object[0]);
    }
    
    /**
     * Get service from reference (simulates BundleContext.getService())
     * 
     * @param serviceReference Service reference
     * @return Service instance or null
     */
    public Object getService(Object serviceReference) {
        if (!active) {
            return null;
        }
        
        return serviceReferences.get(serviceReference);
    }
    
    /**
     * Release service reference (simulates BundleContext.ungetService())
     * 
     * @param serviceReference Service reference to release
     * @return true if service was released
     */
    public boolean ungetService(Object serviceReference) {
        Object removed = serviceReferences.remove(serviceReference);
        return removed != null;
    }
    
    /**
     * Register a service for testing
     * 
     * @param serviceClass Service interface class
     * @param service Service implementation
     */
    public void registerTestService(Class<?> serviceClass, Object service) {
        services.computeIfAbsent(serviceClass, k -> new CopyOnWriteArrayList<>()).add(service);
    }
    
    /**
     * Unregister a test service
     * 
     * @param serviceClass Service interface class
     * @param service Service implementation
     */
    public void unregisterTestService(Class<?> serviceClass, Object service) {
        List<Object> serviceList = services.get(serviceClass);
        if (serviceList != null) {
            serviceList.remove(service);
            if (serviceList.isEmpty()) {
                services.remove(serviceClass);
            }
        }
    }
    
    /**
     * Simulate bundle context shutdown
     */
    public void shutdown() {
        active = false;
        bundleListeners.clear();
        services.clear();
        serviceReferences.clear();
    }
    
    /**
     * Check if bundle context is active
     * 
     * @return true if active
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Get number of registered bundle listeners
     * 
     * @return Number of bundle listeners
     */
    public int getBundleListenerCount() {
        return bundleListeners.size();
    }
    
    /**
     * Get number of registered services
     * 
     * @return Number of service types registered
     */
    public int getServiceCount() {
        return services.size();
    }
    
    @Override
    public String toString() {
        return "MockBundleContext[active=" + active + ", listeners=" + bundleListeners.size() + 
               ", services=" + services.size() + "]";
    }
    
    /**
     * Mock service reference for testing
     */
    public static class MockServiceReference {
        private final Class<?> serviceClass;
        private final Object service;
        private final long serviceId;
        
        public MockServiceReference(Class<?> serviceClass, Object service) {
            this.serviceClass = serviceClass;
            this.service = service;
            this.serviceId = System.nanoTime(); // Unique ID
        }
        
        public Class<?> getServiceClass() {
            return serviceClass;
        }
        
        public Object getService() {
            return service;
        }
        
        public long getServiceId() {
            return serviceId;
        }
        
        @Override
        public String toString() {
            return "MockServiceReference[" + serviceClass.getSimpleName() + ":" + serviceId + "]";
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            
            MockServiceReference that = (MockServiceReference) obj;
            return serviceId == that.serviceId;
        }
        
        @Override
        public int hashCode() {
            return Long.hashCode(serviceId);
        }
    }
}