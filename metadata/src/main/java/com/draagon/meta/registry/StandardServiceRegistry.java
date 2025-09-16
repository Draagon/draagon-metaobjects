package com.draagon.meta.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ServiceLoader-based service registry for non-OSGI environments.
 * 
 * <p>This implementation uses Java's {@link ServiceLoader} to automatically discover
 * service providers listed in {@code META-INF/services/} files. It also supports
 * manual registration for testing and runtime service addition.</p>
 * 
 * <p>Thread-safe implementation using concurrent collections.</p>
 * 
 * @since 6.0.0
 */
public class StandardServiceRegistry implements ServiceRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(StandardServiceRegistry.class);
    
    private final ClassLoader classLoader;
    private final Map<Class<?>, Set<Object>> manualServices = new ConcurrentHashMap<>();
    private volatile boolean closed = false;
    
    /**
     * Create registry with current thread's context class loader
     */
    public StandardServiceRegistry() {
        this(Thread.currentThread().getContextClassLoader());
    }
    
    /**
     * Create registry with specific class loader
     * 
     * @param classLoader ClassLoader to use for service discovery
     */
    public StandardServiceRegistry(ClassLoader classLoader) {
        this.classLoader = classLoader != null ? classLoader : getClass().getClassLoader();
        log.debug("Created StandardServiceRegistry with ClassLoader: {}", this.classLoader);
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
            // 1. Load services via ServiceLoader
            ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceClass, classLoader);
            serviceLoader.forEach(service -> {
                services.add(service);
                log.debug("Discovered service via ServiceLoader: {} -> {}", 
                         serviceClass.getSimpleName(), service.getClass().getName());
            });
            
            // 2. Add manually registered services
            Set<Object> manual = manualServices.get(serviceClass);
            if (manual != null) {
                synchronized (manual) {
                    for (Object service : manual) {
                        if (serviceClass.isInstance(service)) {
                            services.add((T) service);
                            log.debug("Added manual service: {} -> {}", 
                                     serviceClass.getSimpleName(), service.getClass().getName());
                        }
                    }
                }
            }
            
            log.debug("Found {} services for {}", services.size(), serviceClass.getSimpleName());
            
        } catch (ServiceConfigurationError e) {
            log.error("Error loading services for {}: {}", serviceClass.getName(), e.getMessage(), e);
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
        
        manualServices.computeIfAbsent(serviceClass, k -> ConcurrentHashMap.newKeySet())
                     .add(service);
        
        log.debug("Manually registered service: {} -> {}", 
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
                log.debug("Unregistered manual service: {} -> {}", 
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
        return false;
    }
    
    @Override
    public String getDescription() {
        return "Java ServiceLoader (ClassLoader: " + classLoader + ")";
    }
    
    @Override
    public void close() {
        if (!closed) {
            log.debug("Closing StandardServiceRegistry");
            manualServices.clear();
            closed = true;
        }
    }
    
    /**
     * Get statistics about registered services
     * 
     * @return Map of service class to count of manually registered instances
     */
    public Map<String, Integer> getServiceStats() {
        Map<String, Integer> stats = new HashMap<>();
        manualServices.forEach((serviceClass, services) -> 
            stats.put(serviceClass.getSimpleName(), services.size())
        );
        return stats;
    }
    
    /**
     * Check if the registry has been closed
     * 
     * @return true if closed
     */
    public boolean isClosed() {
        return closed;
    }
}