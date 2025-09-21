package com.draagon.meta.examples.osgi;

import com.draagon.meta.registry.MetaDataLoaderRegistry;
import com.draagon.meta.registry.ServiceRegistryFactory;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.object.MetaObject;

import java.net.URI;
import java.util.Arrays;

/**
 * OSGI example demonstrating MetaObjects usage in bundle environments.
 * 
 * This example shows:
 * 1. OSGI service discovery patterns
 * 2. Bundle lifecycle handling with WeakHashMap cleanup
 * 3. ServiceRegistry usage for environment auto-detection
 * 4. Proper resource management in dynamic environments
 */
public class OSGiMetaObjectsExample {
    
    private MetaDataLoaderRegistry registry;
    
    public void activate() {
        System.out.println("=== MetaObjects OSGI Example Activated ===");
        
        try {
            demonstrateOSGiIntegration();
        } catch (Exception e) {
            System.err.println("Error in OSGI example: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void deactivate() {
        System.out.println("=== MetaObjects OSGI Example Deactivated ===");
        
        // Cleanup happens automatically via WeakHashMap patterns
        // and OSGI service lifecycle management
    }
    
    private void demonstrateOSGiIntegration() throws Exception {
        System.out.println("\n1. OSGI Service Discovery...");
        
        // Create OSGi-compatible registry (auto-detects environment)
        registry = new MetaDataLoaderRegistry(ServiceRegistryFactory.getDefault());
        System.out.println("   MetaDataLoaderRegistry created successfully");
        
        // Show ServiceRegistryFactory auto-detection
        System.out.println("   ServiceRegistry type: " + 
            ServiceRegistryFactory.getDefault().getClass().getSimpleName());
        
        // 2. Create and register a test loader
        System.out.println("\n2. Creating and registering MetaDataLoader...");
        
        SimpleLoader loader = new SimpleLoader("osgiExample");
        
        // Load from classpath (provided by shared-resources module)
        java.net.URL resourceUrl = getClass().getResource("/metadata/examples-metadata.json");
        if (resourceUrl == null) {
            throw new RuntimeException("Could not find metadata resource: /metadata/examples-metadata.json");
        }
        
        // Create temporary file and copy resource content
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("examples-metadata", ".json");
        try (java.io.InputStream is = resourceUrl.openStream()) {
            java.nio.file.Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        
        URI metadataUri = tempFile.toUri();
        loader.setSourceURIs(Arrays.asList(metadataUri));
        loader.init();
        
        // Register with OSGI-compatible registry
        registry.registerLoader(loader);
        System.out.println("   Registered loader: " + loader.getName());
        
        // 3. Working with registered loaders
        System.out.println("\n3. Working with registered loaders...");
        
        for (MetaDataLoader registeredLoader : registry.getDataLoaders()) {
            System.out.println("   Found loader: " + registeredLoader.getName());
            
            for (MetaObject metaObject : registeredLoader.getChildren(MetaObject.class)) {
                System.out.println("     - MetaObject: " + metaObject.getName());
                System.out.println("       Package: " + metaObject.getPackage());
                System.out.println("       Fields: " + metaObject.getMetaFields().size());
            }
        }
        
        // 4. Demonstrate bundle lifecycle awareness
        System.out.println("\n4. Bundle lifecycle patterns...");
        
        // Show WeakHashMap cleanup - computed caches can be GC'd
        // when bundles unload, while core metadata remains
        System.out.println("   Computed caches use WeakHashMap for bundle cleanup");
        System.out.println("   Core metadata uses strong references for permanence");
        System.out.println("   Registry handles " + registry.getDataLoaders().size() + " loaders");
        
        // 5. Environment detection
        System.out.println("\n5. Environment detection...");
        String serviceRegistryClass = ServiceRegistryFactory.getDefault().getClass().getName();
        System.out.println("   OSGI environment detected: " + 
            serviceRegistryClass.contains("OSGI"));
        System.out.println("   Service registry implementation: " + serviceRegistryClass);
        
        // 6. Demonstrate proper resource management
        System.out.println("\n6. Resource management...");
        System.out.println("   MetaDataLoaderRegistry manages " + 
            registry.getDataLoaders().size() + " active loaders");
        System.out.println("   Total MetaObjects available: " + 
            registry.getDataLoaders().stream()
                .mapToInt(l -> l.getChildren(MetaObject.class).size())
                .sum());
        
        System.out.println("\n=== OSGI Example completed ===");
    }
    
    /**
     * Demonstrate finding MetaObjects across all registered loaders
     */
    public void demonstrateMetaObjectLookup() {
        try {
            System.out.println("\n=== MetaObject Lookup Demo ===");
            
            // Try to find a specific MetaObject (using package-qualified names)
            MetaObject userMeta = registry.findMetaObjectByName("com_example_model::User");
            System.out.println("Found User MetaObject: " + userMeta.getName());
            
            MetaObject productMeta = registry.findMetaObjectByName("com_example_model::Product");
            System.out.println("Found Product MetaObject: " + productMeta.getName());
            
        } catch (Exception e) {
            System.out.println("MetaObject lookup failed (expected in some environments): " + e.getMessage());
        }
    }
    
    /**
     * Main method for testing OSGi functionality outside of OSGi container
     */
    public static void main(String[] args) {
        try {
            System.out.println("Testing OSGi MetaObjects functionality...");
            
            OSGiMetaObjectsExample example = new OSGiMetaObjectsExample();
            example.activate();
            example.demonstrateMetaObjectLookup();
            example.deactivate();
            
            System.out.println("OSGi test completed successfully");
            
        } catch (Exception e) {
            System.err.println("OSGi test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}