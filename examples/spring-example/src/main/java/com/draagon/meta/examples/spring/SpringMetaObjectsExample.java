package com.draagon.meta.examples.spring;

import com.draagon.meta.spring.MetaDataService;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.registry.MetaDataLoaderRegistry;
import com.draagon.meta.object.MetaObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.net.URI;
import java.util.Arrays;

/**
 * Spring example demonstrating MetaObjects integration with Spring Framework.
 * 
 * This example shows:
 * 1. Spring auto-configuration of MetaObjects
 * 2. Dependency injection patterns
 * 3. MetaDataService wrapper usage
 * 4. Spring Boot integration
 */
@SpringBootApplication
public class SpringMetaObjectsExample implements CommandLineRunner {
    
    // Option 1: Convenient service wrapper (recommended)
    @Autowired
    private MetaDataService metaDataService;
    
    // Option 2: Backward compatible loader injection
    @Autowired
    private MetaDataLoader primaryMetaDataLoader;
    
    // Option 3: Full registry access for advanced operations
    @Autowired
    private MetaDataLoaderRegistry metaDataLoaderRegistry;
    
    public static void main(String[] args) {
        try {
            // Simple Spring test without Spring Boot auto-configuration
            System.out.println("=== Testing Spring Integration Manually ===");
            testSpringIntegrationManually();
        } catch (Exception e) {
            System.err.println("Spring integration test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Simple test of Spring integration without Spring Boot complexity
     */
    private static void testSpringIntegrationManually() throws Exception {
        System.out.println("\n1. Manual Spring integration test...");
        
        // Test direct Spring class usage
        com.draagon.meta.spring.MetaDataService service = null;
        com.draagon.meta.spring.MetaDataAutoConfiguration config = null;
        
        try {
            // Test that Spring classes can be loaded
            config = new com.draagon.meta.spring.MetaDataAutoConfiguration();
            System.out.println("   MetaDataAutoConfiguration class loaded: SUCCESS");
            
            // Test MetaDataService class loading
            Class<?> serviceClass = Class.forName("com.draagon.meta.spring.MetaDataService");
            System.out.println("   MetaDataService class loaded: SUCCESS");
            
            // Test MetaDataLoaderConfiguration class loading
            Class<?> configClass = Class.forName("com.draagon.meta.spring.MetaDataLoaderConfiguration");
            System.out.println("   MetaDataLoaderConfiguration class loaded: SUCCESS");
            
        } catch (Exception e) {
            System.out.println("   Spring class loading failed: " + e.getMessage());
        }
        
        // Test basic metadata functionality (same as other examples)
        System.out.println("\n2. Basic MetaObjects functionality...");
        
        com.draagon.meta.loader.simple.SimpleLoader loader = new com.draagon.meta.loader.simple.SimpleLoader("spring-test");
        
        // Load from classpath (same approach as other examples)
        java.net.URL resourceUrl = SpringMetaObjectsExample.class.getResource("/metadata/examples-metadata.json");
        if (resourceUrl == null) {
            throw new RuntimeException("Could not find metadata resource: /metadata/examples-metadata.json");
        }
        
        // Create temporary file and copy resource content
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("examples-metadata", ".json");
        try (java.io.InputStream is = resourceUrl.openStream()) {
            java.nio.file.Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        
        java.net.URI metadataUri = tempFile.toUri();
        loader.setSourceURIs(java.util.Arrays.asList(metadataUri));
        loader.init();
        
        System.out.println("   Loaded " + loader.getChildren().size() + " metadata items");
        
        // Test MetaObject access
        try {
            com.draagon.meta.object.MetaObject userMeta = loader.getMetaObjectByName("com_example_model::User");
            System.out.println("   Found User MetaObject: " + userMeta.getName());
            System.out.println("   User has " + userMeta.getMetaFields().size() + " fields");
        } catch (Exception e) {
            System.out.println("   MetaObject lookup failed: " + e.getMessage());
        }
        
        System.out.println("\n=== Manual Spring integration test completed ===");
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== MetaObjects Spring Example ===");
        
        demonstrateSpringIntegration();
    }
    
    private void demonstrateSpringIntegration() {
        System.out.println("\n1. Spring auto-configuration verification...");
        
        // Verify all injection options work
        System.out.println("   MetaDataService injected: " + 
            (metaDataService != null ? "SUCCESS" : "FAILED"));
        System.out.println("   Primary MetaDataLoader injected: " + 
            (primaryMetaDataLoader != null ? "SUCCESS" : "FAILED"));  
        System.out.println("   MetaDataLoaderRegistry injected: " + 
            (metaDataLoaderRegistry != null ? "SUCCESS" : "FAILED"));
        
        // 2. Using MetaDataService (recommended approach)
        System.out.println("\n2. Using MetaDataService wrapper...");
        
        // Get all available MetaObjects
        var allObjects = metaDataService.getAllMetaObjects();
        System.out.println("   Found " + allObjects.size() + " MetaObjects via service");
        
        for (MetaObject obj : allObjects) {
            System.out.println("     - " + obj.getName() + " (package: " + obj.getPackage() + ")");
        }
        
        // Optional-based null-safe access
        var userMeta = metaDataService.findMetaObjectByNameOptional("com_example_model::User");
        if (userMeta.isPresent()) {
            System.out.println("   User MetaObject found via optional access");
            System.out.println("     Fields: " + userMeta.get().getMetaFields().size());
        } else {
            System.out.println("   User MetaObject not found");
        }
        
        // Check if specific objects exist
        boolean hasUser = metaDataService.metaObjectExists("com_example_model::User");
        boolean hasProduct = metaDataService.metaObjectExists("com_example_model::Product");
        System.out.println("   User exists: " + hasUser + ", Product exists: " + hasProduct);
        
        // 3. Backward compatible loader access
        System.out.println("\n3. Backward compatible loader access...");
        System.out.println("   Primary loader name: " + primaryMetaDataLoader.getName());
        System.out.println("   Primary loader objects: " + 
            primaryMetaDataLoader.getChildren(MetaObject.class).size());
        
        // 4. Advanced registry operations
        System.out.println("\n4. Advanced registry operations...");
        System.out.println("   Total registered loaders: " + 
            metaDataLoaderRegistry.getDataLoaders().size());
        
        for (MetaDataLoader loader : metaDataLoaderRegistry.getDataLoaders()) {
            System.out.println("     - Loader: " + loader.getName() + 
                " (" + loader.getChildren().size() + " children)");
        }
        
        // 5. Demonstrate service convenience methods
        System.out.println("\n5. Service convenience methods...");
        
        try {
            // Direct lookup
            MetaObject user = metaDataService.findMetaObjectByName("com_example_model::User");
            System.out.println("   Direct lookup successful: " + user.getName());
            
            // Field details
            user.getMetaFields().forEach(field -> {
                System.out.println("     Field: " + field.getName() + 
                    " (" + field.getSubType() + ")");
            });
            
        } catch (Exception e) {
            System.out.println("   MetaObject lookup failed: " + e.getMessage());
        }
        
        System.out.println("\n=== Spring Example completed ===");
    }
    
    /**
     * Bean to configure test MetaDataLoader for the example
     */
    @Bean
    public MetaDataLoader exampleMetaDataLoader() throws Exception {
        SimpleLoader loader = new SimpleLoader("spring-example");
        
        // Load example metadata from shared resources
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
        
        return loader;
    }
}