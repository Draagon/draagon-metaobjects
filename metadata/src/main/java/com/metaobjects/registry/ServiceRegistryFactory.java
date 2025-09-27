package com.metaobjects.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metaobjects.registry.osgi.OSGIServiceRegistry;

/**
 * Factory for creating the appropriate ServiceRegistry based on the runtime environment.
 * 
 * <p>This factory automatically detects whether OSGI is available and creates the
 * appropriate service registry implementation:</p>
 * 
 * <ul>
 *   <li><strong>OSGI Available:</strong> Creates {@link OSGIServiceRegistry}</li>
 *   <li><strong>Non-OSGI:</strong> Creates {@link StandardServiceRegistry}</li>
 * </ul>
 * 
 * <p>The detection is done by looking for OSGI classes in the classpath and
 * checking for a BundleContext in the system properties or thread context.</p>
 * 
 * @since 6.0.0
 */
public class ServiceRegistryFactory {
    
    private static final Logger log = LoggerFactory.getLogger(ServiceRegistryFactory.class);
    
    private static volatile ServiceRegistry defaultInstance;
    private static final Object LOCK = new Object();
    
    /**
     * Get the default ServiceRegistry instance (singleton)
     * 
     * @return ServiceRegistry appropriate for the current environment
     */
    public static ServiceRegistry getDefault() {
        if (defaultInstance == null) {
            synchronized (LOCK) {
                if (defaultInstance == null) {
                    defaultInstance = create();
                }
            }
        }
        return defaultInstance;
    }
    
    /**
     * Create a new ServiceRegistry appropriate for the current environment
     * 
     * @return ServiceRegistry instance
     */
    public static ServiceRegistry create() {
        if (isOSGIAvailable()) {
            Object bundleContext = getBundleContext();
            if (bundleContext != null) {
                log.info("Creating OSGIServiceRegistry with BundleContext");
                return new OSGIServiceRegistry(bundleContext);
            } else {
                log.info("OSGI detected but no BundleContext found, falling back to StandardServiceRegistry");
                return new StandardServiceRegistry();
            }
        } else {
            log.info("Creating StandardServiceRegistry (OSGI not available)");
            return new StandardServiceRegistry();
        }
    }
    
    /**
     * Create a ServiceRegistry with a specific ClassLoader (non-OSGI only)
     * 
     * @param classLoader ClassLoader to use for service discovery
     * @return StandardServiceRegistry instance
     */
    public static ServiceRegistry create(ClassLoader classLoader) {
        log.info("Creating StandardServiceRegistry with specific ClassLoader: {}", classLoader);
        return new StandardServiceRegistry(classLoader);
    }
    
    /**
     * Create an OSGI ServiceRegistry with a specific BundleContext
     * 
     * @param bundleContext OSGI BundleContext
     * @return OSGIServiceRegistry instance
     */
    public static ServiceRegistry createOSGI(Object bundleContext) {
        log.info("Creating OSGIServiceRegistry with provided BundleContext");
        return new OSGIServiceRegistry(bundleContext);
    }
    
    /**
     * Check if OSGI is available in the current classpath
     * 
     * @return true if OSGI classes are found
     */
    public static boolean isOSGIAvailable() {
        try {
            // Try to load key OSGI classes
            Class.forName("org.osgi.framework.BundleContext");
            Class.forName("org.osgi.framework.ServiceReference");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Try to find a BundleContext in the current environment
     * 
     * @return BundleContext if found, null otherwise
     */
    private static Object getBundleContext() {
        try {
            // Method 1: Check system property (common in some OSGI frameworks)
            String bundleContextProp = System.getProperty("org.osgi.framework.BundleContext");
            if (bundleContextProp != null) {
                log.debug("Found BundleContext system property: {}", bundleContextProp);
                // This would need framework-specific code to resolve
            }
            
            // Method 2: Check thread context (Framework-specific)
            // This is highly framework-dependent, so we'll leave it as a hook
            // for future enhancement
            
            // Method 3: Look for FrameworkUtil (if available)
            try {
                Class<?> frameworkUtilClass = Class.forName("org.osgi.framework.FrameworkUtil");
                var getBundleMethod = frameworkUtilClass.getMethod("getBundle", Class.class);
                Object bundle = getBundleMethod.invoke(null, ServiceRegistryFactory.class);
                
                if (bundle != null) {
                    var getBundleContextMethod = bundle.getClass().getMethod("getBundleContext");
                    Object bundleContext = getBundleContextMethod.invoke(bundle);
                    log.debug("Found BundleContext via FrameworkUtil: {}", bundleContext);
                    return bundleContext;
                }
            } catch (Exception e) {
                log.debug("Could not get BundleContext via FrameworkUtil: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            log.debug("Error trying to find BundleContext: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Reset the default instance (primarily for testing)
     */
    public static void resetDefault() {
        synchronized (LOCK) {
            if (defaultInstance != null) {
                defaultInstance.close();
                defaultInstance = null;
            }
        }
    }
    
    /**
     * Get information about the current environment
     * 
     * @return Environment description
     */
    public static String getEnvironmentInfo() {
        StringBuilder info = new StringBuilder();
        info.append("OSGI Available: ").append(isOSGIAvailable());
        
        if (isOSGIAvailable()) {
            Object bundleContext = getBundleContext();
            info.append(", BundleContext: ").append(bundleContext != null ? "Found" : "Not Found");
        }
        
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        info.append(", Context ClassLoader: ").append(contextLoader);
        
        return info.toString();
    }
}