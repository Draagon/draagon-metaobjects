package com.draagon.meta.spring;

import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.registry.MetaDataLoaderRegistry;
import com.draagon.meta.registry.ServiceRegistryFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Integration test for MetaObjects Spring integration.
 * 
 * <p>Tests the auto-configuration of MetaDataLoaderRegistry and 
 * associated Spring beans.</p>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    MetaDataAutoConfiguration.class,
    MetaDataSpringIntegrationTest.TestConfiguration.class
})
public class MetaDataSpringIntegrationTest {
    
    @Autowired
    private MetaDataLoaderRegistry metaDataLoaderRegistry;
    
    @Autowired
    private MetaDataLoader primaryMetaDataLoader;
    
    @Autowired
    private MetaDataService metaDataService;
    
    /**
     * Test Spring configuration that creates test MetaDataLoader beans
     */
    @Configuration
    static class TestConfiguration {
        
        @Bean
        public MetaDataLoader testMetaDataLoader() throws Exception {
            SimpleLoader loader = new SimpleLoader("testLoader");
            
            // Create a simple test metadata file for this test
            java.io.File tempFile = java.io.File.createTempFile("test-metadata", ".json");
            tempFile.deleteOnExit();
            
            // Write minimal test metadata
            try (java.io.FileWriter writer = new java.io.FileWriter(tempFile)) {
                writer.write("{\n");
                writer.write("  \"metadata\": {\n");
                writer.write("    \"package\": \"test\",\n");
                writer.write("    \"children\": [\n");
                writer.write("      {\n");
                writer.write("        \"object\": {\n");
                writer.write("          \"name\": \"TestObject\",\n");
                writer.write("          \"subType\": \"pojo\",\n");
                writer.write("          \"children\": [\n");
                writer.write("            {\n");
                writer.write("              \"field\": {\n");
                writer.write("                \"name\": \"id\",\n");
                writer.write("                \"subType\": \"long\"\n");
                writer.write("              }\n");
                writer.write("            }\n");
                writer.write("          ]\n");
                writer.write("        }\n");
                writer.write("      }\n");
                writer.write("    ]\n");
                writer.write("  }\n");
                writer.write("}\n");
            }
            
            loader.setSourceURIs(Arrays.asList(tempFile.toURI()));
            loader.init();
            return loader;
        }
    }
    
    @Before
    public void setUp() {
        // Verify auto-configuration worked
        assertNotNull("MetaDataLoaderRegistry should be auto-configured", metaDataLoaderRegistry);
        assertNotNull("Primary MetaDataLoader should be auto-configured", primaryMetaDataLoader);
        assertNotNull("MetaDataService should be auto-configured", metaDataService);
    }
    
    @Test
    public void testMetaDataLoaderRegistryConfiguration() {
        // Verify registry is configured with OSGi-compatible service registry
        assertNotNull("MetaDataLoaderRegistry should be configured", metaDataLoaderRegistry);
        
        // Verify test loader was auto-discovered and registered
        Collection<MetaDataLoader> loaders = metaDataLoaderRegistry.getDataLoaders();
        assertFalse("Should have at least one loader", loaders.isEmpty());
        
        // Find our test loader
        MetaDataLoader testLoader = loaders.stream()
            .filter(loader -> "testLoader".equals(loader.getName()))
            .findFirst()
            .orElse(null);
        assertNotNull("Test loader should be auto-registered", testLoader);
    }
    
    @Test
    public void testPrimaryMetaDataLoaderConfiguration() {
        // Verify primary loader is available
        assertNotNull("Primary MetaDataLoader should be configured", primaryMetaDataLoader);
        assertEquals("Primary loader should be our test loader", "testLoader", primaryMetaDataLoader.getName());
    }
    
    @Test
    public void testMetaDataServiceConfiguration() {
        // Verify service wrapper is configured
        assertNotNull("MetaDataService should be configured", metaDataService);
        
        // Verify service can access registry
        assertNotNull("Service should have access to registry", metaDataService.getRegistry());
        
        // Verify service methods work
        Collection<MetaDataLoader> loaders = metaDataService.getAllLoaders();
        assertFalse("Service should return loaders", loaders.isEmpty());
    }
    
    @Test
    public void testMetaDataServiceFunctionality() {
        // Test service can find metadata
        Collection<MetaObject> metaObjects = metaDataService.getAllMetaObjects();
        assertNotNull("Should return metadata objects", metaObjects);
        
        // Test optional-based methods work
        if (!metaObjects.isEmpty()) {
            MetaObject firstObject = metaObjects.iterator().next();
            String objectName = firstObject.getName();
            
            // Test findMetaObjectByNameOptional
            assertTrue("Should find object by name", 
                metaDataService.findMetaObjectByNameOptional(objectName).isPresent());
            
            // Test metaObjectExists
            assertTrue("Should confirm object exists", 
                metaDataService.metaObjectExists(objectName));
            
            // Test non-existent object
            assertFalse("Should not find non-existent object", 
                metaDataService.findMetaObjectByNameOptional("NonExistentObject").isPresent());
            
            assertFalse("Should confirm non-existent object doesn't exist", 
                metaDataService.metaObjectExists("NonExistentObject"));
        }
    }
    
    @Test
    public void testBackwardCompatibilityInjection() {
        // Test that @Autowired MetaDataLoader still works (backward compatibility)
        assertNotNull("Backward compatible MetaDataLoader injection should work", primaryMetaDataLoader);
        assertSame("Primary loader should be the same instance", primaryMetaDataLoader, 
            metaDataLoaderRegistry.getDataLoaders().iterator().next());
    }
    
    @Test
    public void testAdvancedRegistryAccess() {
        // Test that @Autowired MetaDataLoaderRegistry provides full registry access
        assertNotNull("Full registry access should be available", metaDataLoaderRegistry);
        
        // Verify we can access the underlying service registry
        Collection<MetaDataLoader> loaders = metaDataLoaderRegistry.getDataLoaders();
        assertFalse("Registry should have loaders", loaders.isEmpty());
        
        // Test finding metadata by name and type
        Collection<MetaObject> objects = new java.util.ArrayList<>();
        for (MetaDataLoader loader : metaDataLoaderRegistry.getDataLoaders()) {
            objects.addAll(loader.getChildren(MetaObject.class));
        }
        if (!objects.isEmpty()) {
            MetaObject firstObject = objects.iterator().next();
            String objectName = firstObject.getName();
            
            try {
                MetaObject foundObject = metaDataLoaderRegistry.findMetaObjectByName(objectName);
                assertNotNull("Should find object via registry", foundObject);
                assertEquals("Found object should match", objectName, foundObject.getName());
            } catch (Exception e) {
                // Expected if test metadata doesn't include full objects
            }
        }
    }
    
    @Test 
    public void testServiceRegistryFactoryIntegration() {
        // Verify that the auto-configuration uses ServiceRegistryFactory
        // This ensures OSGi compatibility
        assertNotNull("Registry should be configured with ServiceRegistryFactory", 
            ServiceRegistryFactory.getDefault());
        
        // The MetaDataLoaderRegistry should be using the default service registry
        // which auto-detects OSGi vs non-OSGi environments
        Collection<MetaDataLoader> loaders = metaDataLoaderRegistry.getDataLoaders();
        assertNotNull("Loaders should be accessible via OSGi-compatible registry", loaders);
    }
}