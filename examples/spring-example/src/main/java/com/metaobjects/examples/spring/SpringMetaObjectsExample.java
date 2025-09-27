package com.metaobjects.examples.spring;

import com.metaobjects.spring.MetaDataService;
import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.loader.simple.SimpleLoader;
import com.metaobjects.registry.MetaDataLoaderRegistry;
import com.metaobjects.object.MetaObject;
import com.metaobjects.util.MetaDataUtil;

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
    
    // COMPLEX PATTERN: Convenient service wrapper for multi-loader scenarios (recommended)
    @Autowired
    private MetaDataService metaDataService;

    // SIMPLE PATTERN: Direct loader injection for single-loader scenarios
    @Autowired
    private MetaDataLoader primaryMetaDataLoader;

    // COMPLEX PATTERN: Full registry access for advanced multi-loader operations
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
        System.out.println("\n1. Spring class loading verification...");

        // Test Spring class availability
        try {
            Class.forName("com.metaobjects.spring.MetaDataAutoConfiguration");
            Class.forName("com.metaobjects.spring.MetaDataService");
            Class.forName("com.metaobjects.spring.MetaDataLoaderConfiguration");
            System.out.println("   Spring integration classes: SUCCESS");
        } catch (Exception e) {
            System.out.println("   Spring class loading failed: " + e.getMessage());
        }

        // Test basic metadata functionality using simple pattern
        System.out.println("\n2. Basic MetaObjects functionality...");

        // Simple pattern: Create one loader for single-loader scenario
        SimpleLoader loader = new SimpleLoader("spring-test");

        java.net.URL resourceUrl = SpringMetaObjectsExample.class.getResource("/metadata/examples-metadata.json");
        if (resourceUrl == null) {
            throw new RuntimeException("Could not find metadata resource");
        }

        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("examples-metadata", ".json");
        try (java.io.InputStream is = resourceUrl.openStream()) {
            java.nio.file.Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        loader.setSourceURIs(java.util.Arrays.asList(tempFile.toUri()));
        loader.init();

        System.out.println("   Loaded " + loader.getChildren().size() + " metadata items");

        // Simple pattern: Direct loader access instead of registry
        try {
            MetaObject userMeta = com.metaobjects.util.MetaDataUtil.findMetaObjectByName(loader, "com_example_model::User");
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
        
        // 3. SIMPLE PATTERN: Direct loader access for single-loader scenarios
        System.out.println("\n3. Simple pattern - direct loader access...");
        System.out.println("   Primary loader name: " + primaryMetaDataLoader.getName());
        System.out.println("   Primary loader objects: " +
            primaryMetaDataLoader.getChildren(MetaObject.class).size());

        // Demonstrate simple pattern utility methods
        try {
            MetaObject directUser = MetaDataUtil.findMetaObjectByName(primaryMetaDataLoader, "com_example_model::User");
            System.out.println("   Direct loader lookup: " + directUser.getName());
        } catch (Exception e) {
            System.out.println("   Direct lookup failed: " + e.getMessage());
        }
        
        // 4. COMPLEX PATTERN: Advanced registry operations for multi-loader scenarios
        System.out.println("\n4. Complex pattern - registry operations...");
        System.out.println("   Total registered loaders: " +
            metaDataLoaderRegistry.getDataLoaders().size());

        for (MetaDataLoader loader : metaDataLoaderRegistry.getDataLoaders()) {
            System.out.println("     - Loader: " + loader.getName() +
                " (" + loader.getChildren().size() + " children)");
        }

        // Demonstrate complex pattern utility methods
        try {
            MetaObject registryUser = MetaDataUtil.findMetaObjectByName("com_example_model::User", this);
            System.out.println("   Registry utility lookup: " + registryUser.getName());
        } catch (Exception e) {
            System.out.println("   Registry utility lookup failed: " + e.getMessage());
        }
        
        // 5. COMPLEX PATTERN: Service convenience methods (best for most Spring applications)
        System.out.println("\n5. Service convenience methods (recommended for most Spring apps)...");

        try {
            // Service wrapper with Optional support
            MetaObject user = metaDataService.findMetaObjectByName("com_example_model::User");
            System.out.println("   Service lookup successful: " + user.getName());

            // Field details
            user.getMetaFields().forEach(field -> {
                System.out.println("     Field: " + field.getName() +
                    " (" + field.getSubType() + ")");
            });

        } catch (Exception e) {
            System.out.println("   Service lookup failed: " + e.getMessage());
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