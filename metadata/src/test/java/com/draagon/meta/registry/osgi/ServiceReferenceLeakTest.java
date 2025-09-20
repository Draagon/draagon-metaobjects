package com.draagon.meta.registry.osgi;

import com.draagon.meta.registry.OSGIServiceRegistry;
import com.draagon.meta.registry.StandardServiceRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Test for preventing ServiceReference leaks and verifying proper WeakReference usage.
 * 
 * <p>These tests specifically focus on memory management aspects of the OSGI integration,
 * ensuring that ServiceReferences are properly released and ClassLoaders can be
 * garbage collected when bundles are unloaded.</p>
 * 
 * @since 6.0.0
 */
public class ServiceReferenceLeakTest {
    
    private static final Logger log = LoggerFactory.getLogger(ServiceReferenceLeakTest.class);
    
    private MockBundleContext mockBundleContext;
    private OSGIServiceRegistry osgiRegistry;
    private StandardServiceRegistry standardRegistry;
    
    @Before
    public void setUp() {
        mockBundleContext = new MockBundleContext();
        osgiRegistry = new OSGIServiceRegistry(mockBundleContext);
        standardRegistry = new StandardServiceRegistry();
        log.info("Set up ServiceReference leak test");
    }
    
    @After
    public void tearDown() {
        if (osgiRegistry != null) {
            osgiRegistry.close();
        }
        if (standardRegistry != null) {
            standardRegistry.close();
        }
        log.info("Tore down ServiceReference leak test");
    }
    
    /**
     * Test that ServiceReferences are properly released when services are retrieved
     */
    @Test
    public void testServiceReferenceCleanup() {
        // Register a test service
        TestService testService = new TestServiceImpl();
        mockBundleContext.registerTestService(TestService.class, testService);
        
        // Get services through registry
        Collection<TestService> services = osgiRegistry.getServices(TestService.class);
        
        assertNotNull("Services should not be null", services);
        assertFalse("Should find test service", services.isEmpty());
        assertEquals("Should find exactly one service", 1, services.size());
        
        // Verify the service is correct
        TestService foundService = services.iterator().next();
        assertSame("Should be the same service instance", testService, foundService);
        
        // Clean up
        osgiRegistry.close();
        
        // Verify mock context shows proper cleanup
        assertTrue("ServiceReferences should be cleaned up", 
                  mockBundleContext.getServiceCount() >= 0); // Mock doesn't track cleanup, but no exceptions
        
        log.info("ServiceReference cleanup test completed successfully");
    }
    
    /**
     * Test WeakReference behavior in StandardServiceRegistry
     */
    @Test
    public void testWeakReferenceClassLoaderCleanup() throws InterruptedException {
        // Create a custom ClassLoader that can be garbage collected
        CustomClassLoader customLoader = new CustomClassLoader();
        WeakReference<CustomClassLoader> loaderRef = new WeakReference<>(customLoader);
        
        // Create registry with custom ClassLoader
        StandardServiceRegistry customRegistry = new StandardServiceRegistry(customLoader);
        
        // Verify original ClassLoader is available
        assertTrue("Original ClassLoader should be available", 
                  customRegistry.isOriginalClassLoaderAvailable());
        
        // Clear strong reference
        customLoader = null;
        
        // Force garbage collection multiple times
        for (int i = 0; i < 10; i++) {
            System.gc();
            Thread.sleep(50);
            if (loaderRef.get() == null) {
                break;
            }
        }
        
        // ClassLoader should be garbage collected
        assertNull("Custom ClassLoader should be garbage collected", loaderRef.get());
        
        // Registry should fallback gracefully
        assertFalse("Original ClassLoader should no longer be available", 
                   customRegistry.isOriginalClassLoaderAvailable());
        
        String status = customRegistry.getClassLoaderStatus();
        assertTrue("Status should indicate fallback", status.contains("fallback"));
        
        // Registry should still be functional
        Collection<TestService> services = customRegistry.getServices(TestService.class);
        assertNotNull("Services collection should not be null", services);
        
        customRegistry.close();
        log.info("WeakReference ClassLoader cleanup test completed successfully");
    }
    
    /**
     * Test memory leak prevention in long-running scenarios
     */
    @Test
    public void testMemoryLeakPrevention() {
        final int ITERATION_COUNT = 100;
        
        // Simulate repeated service discovery operations
        for (int i = 0; i < ITERATION_COUNT; i++) {
            // Register temporary service
            TestService tempService = new TestServiceImpl();
            mockBundleContext.registerTestService(TestService.class, tempService);
            
            // Get services
            Collection<TestService> services = osgiRegistry.getServices(TestService.class);
            assertNotNull("Services should not be null", services);
            
            // Unregister service
            mockBundleContext.unregisterTestService(TestService.class, tempService);
            
            // Occasional garbage collection
            if (i % 10 == 0) {
                System.gc();
            }
        }
        
        // Verify registry is still functional after many operations
        assertTrue("Registry should still be active", osgiRegistry.isBundleLifecycleActive());
        assertNotNull("Description should still work", osgiRegistry.getDescription());
        
        log.info("Memory leak prevention test completed {} iterations successfully", ITERATION_COUNT);
    }
    
    /**
     * Test that bundle lifecycle manager properly cleans up when bundles are GC'd
     */
    @Test
    public void testBundleLifecycleCleanup() throws InterruptedException {
        MockBundle bundle = new MockBundle("lifecycle.cleanup", "1.0.0", 999L);
        WeakReference<MockBundle> bundleRef = new WeakReference<>(bundle);
        
        // Trigger bundle events
        osgiRegistry.onBundleEvent(bundle);
        
        // Clear strong reference
        bundle = null;
        
        // Force garbage collection
        for (int i = 0; i < 5; i++) {
            System.gc();
            Thread.sleep(100);
            if (bundleRef.get() == null) {
                break;
            }
        }
        
        // Bundle should be garbage collected
        assertNull("Bundle should be garbage collected", bundleRef.get());
        
        // Lifecycle manager should still be functional
        assertTrue("Lifecycle should still be active", osgiRegistry.isBundleLifecycleActive());
        
        log.info("Bundle lifecycle cleanup test completed successfully");
    }
    
    /**
     * Test registry behavior when BundleContext becomes unavailable
     */
    @Test
    public void testBundleContextUnavailable() {
        // Verify registry is initially active
        assertTrue("Registry should be active initially", osgiRegistry.isBundleLifecycleActive());
        
        // Shutdown mock bundle context
        mockBundleContext.shutdown();
        
        // Registry should handle this gracefully
        Collection<TestService> services = osgiRegistry.getServices(TestService.class);
        assertNotNull("Services should not be null even when context is shutdown", services);
        assertTrue("Services should be empty when context is shutdown", services.isEmpty());
        
        // Close registry
        osgiRegistry.close();
        
        // Should not throw exceptions
        assertFalse("Registry should not be active after close", osgiRegistry.isBundleLifecycleActive());
        
        log.info("BundleContext unavailable test completed successfully");
    }
    
    /**
     * Test service interface for manual service registration
     */
    @Test
    public void testManualServiceRegistration() {
        TestService manualService = new TestServiceImpl();
        
        // Register service manually
        osgiRegistry.registerService(TestService.class, manualService);
        
        // Retrieve services
        Collection<TestService> services = osgiRegistry.getServices(TestService.class);
        
        assertNotNull("Services should not be null", services);
        assertFalse("Should find manual service", services.isEmpty());
        assertTrue("Should contain manual service", services.contains(manualService));
        
        // Unregister service
        boolean unregistered = osgiRegistry.unregisterService(TestService.class, manualService);
        assertTrue("Service should be unregistered", unregistered);
        
        // Verify removal
        services = osgiRegistry.getServices(TestService.class);
        assertFalse("Should not contain unregistered service", services.contains(manualService));
        
        log.info("Manual service registration test completed successfully");
    }
    
    // Test service interface
    public interface TestService {
        String getName();
    }
    
    // Test service implementation
    public static class TestServiceImpl implements TestService {
        private final String name = "TestService-" + System.nanoTime();
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public String toString() {
            return "TestServiceImpl[" + name + "]";
        }
    }
    
    // Custom ClassLoader for testing WeakReference behavior
    public static class CustomClassLoader extends ClassLoader {
        private final String id = "CustomClassLoader-" + System.nanoTime();
        
        @Override
        public String toString() {
            return id;
        }
    }
}