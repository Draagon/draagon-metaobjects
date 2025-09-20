package com.draagon.meta.registry.osgi;

import com.draagon.meta.registry.OSGIServiceRegistry;
import com.draagon.meta.registry.ServiceRegistry;
import com.draagon.meta.registry.ServiceRegistryFactory;
import com.draagon.meta.type.MetaDataTypeRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Integration tests for OSGI bundle lifecycle management.
 * 
 * <p>These tests simulate OSGI bundle scenarios to verify proper cleanup
 * and memory management when bundles are loaded and unloaded.</p>
 * 
 * @since 6.0.0
 */
public class OSGILifecycleTest {
    
    private static final Logger log = LoggerFactory.getLogger(OSGILifecycleTest.class);
    
    private MockBundleContext mockBundleContext;
    private OSGIServiceRegistry registry;
    
    @Before
    public void setUp() {
        mockBundleContext = new MockBundleContext();
        registry = new OSGIServiceRegistry(mockBundleContext);
        log.info("Set up OSGI lifecycle test with mock bundle context");
    }
    
    @After
    public void tearDown() {
        if (registry != null) {
            registry.close();
        }
        ServiceRegistryFactory.resetDefault();
        log.info("Tore down OSGI lifecycle test");
    }
    
    /**
     * Test basic bundle lifecycle event handling
     */
    @Test
    public void testBundleLifecycleEvents() {
        // Verify registry is active
        assertTrue("Registry should be active", registry.isBundleLifecycleActive());
        String description = registry.getDescription();
        assertTrue("Should have BundleContext in description", description.contains("OSGI BundleContext"));
        assertTrue("Should contain MockBundleContext", description.contains("MockBundleContext"));
        
        // Create mock bundle
        MockBundle bundle = new MockBundle("test.bundle", "1.0.0", 123L);
        
        // Simulate bundle events
        registry.onBundleEvent(bundle);
        registry.cleanupForBundle(bundle);
        
        // Verify no exceptions occurred
        assertNotNull("Registry should still be functional", registry.getDescription());
        log.info("Bundle lifecycle events processed successfully");
    }
    
    /**
     * Test bundle-aware MetaDataTypeRegistry instances
     */
    @Test
    public void testBundleAwareTypeRegistry() {
        MockBundle bundle1 = new MockBundle("bundle.one", "1.0.0", 100L);
        MockBundle bundle2 = new MockBundle("bundle.two", "2.0.0", 200L);
        
        // Get bundle-specific registries
        MetaDataTypeRegistry registry1 = MetaDataTypeRegistry.getInstance(bundle1);
        MetaDataTypeRegistry registry2 = MetaDataTypeRegistry.getInstance(bundle2);
        MetaDataTypeRegistry singleton = MetaDataTypeRegistry.getInstance();
        
        // Verify they are different instances
        assertNotSame("Bundle registries should be different", registry1, registry2);
        assertNotSame("Bundle registry should be different from singleton", registry1, singleton);
        
        // Verify bundle awareness
        assertTrue("Registry1 should be bundle-aware", registry1.isBundleAware());
        assertTrue("Registry2 should be bundle-aware", registry2.isBundleAware());
        assertFalse("Singleton should not be bundle-aware", singleton.isBundleAware());
        
        // Verify bundle availability
        assertTrue("Registry1 bundle should be available", registry1.isBundleAvailable());
        assertTrue("Registry2 bundle should be available", registry2.isBundleAvailable());
        
        // Verify different registry IDs
        assertNotEquals("Registry IDs should be different", 
                       registry1.getRegistryId(), registry2.getRegistryId());
        
        log.info("Bundle-aware type registries created and verified successfully");
    }
    
    /**
     * Test WeakReference cleanup when bundles are garbage collected
     * Note: GC behavior is not guaranteed in tests, so we focus on API correctness
     */
    @Test
    public void testWeakReferenceCleanup() throws InterruptedException {
        MockBundle bundle = new MockBundle("cleanup.test", "1.0.0", 300L);
        WeakReference<MockBundle> bundleRef = new WeakReference<>(bundle);
        
        // Get bundle-specific registry
        MetaDataTypeRegistry registry = MetaDataTypeRegistry.getInstance(bundle);
        assertTrue("Registry should be bundle-aware", registry.isBundleAware());
        assertTrue("Bundle should be available", registry.isBundleAvailable());
        
        // Verify registry reports correct bundle info
        String bundleInfo = registry.getBundleInfo();
        assertTrue("Bundle info should contain bundle name", bundleInfo.contains("cleanup.test"));
        
        // Clear strong reference
        bundle = null;
        
        // Attempt garbage collection (not guaranteed to work in tests)
        for (int i = 0; i < 10; i++) {
            System.gc();
            Thread.sleep(50);
            if (bundleRef.get() == null) {
                break;
            }
        }
        
        // Test cleanup functionality regardless of actual GC
        MetaDataTypeRegistry.cleanupStaleReferences();
        
        // Verify the registry still functions correctly
        assertNotNull("Registry should still be functional", registry.getDetailedStatus());
        assertTrue("Registry should still be bundle-aware", registry.isBundleAware());
        
        // If GC actually worked, verify the behavior
        if (bundleRef.get() == null) {
            log.info("Bundle was garbage collected - verifying cleanup behavior");
            assertFalse("Bundle should no longer be available", registry.isBundleAvailable());
            assertEquals("Bundle info should show GC'd", "bundle GC'd", registry.getBundleInfo());
        } else {
            log.info("Bundle was not garbage collected (normal in test environment) - verifying API functionality");
            // Verify that the API would work correctly if GC happened
            assertTrue("Bundle should still be available since not GC'd", registry.isBundleAvailable());
        }
        
        log.info("WeakReference cleanup test completed successfully");
    }
    
    /**
     * Test ServiceRegistry interface compliance
     */
    @Test
    public void testServiceRegistryInterfaceCompliance() {
        // Test interface methods
        assertTrue("Should be OSGI environment", registry.isOSGIEnvironment());
        assertTrue("Bundle lifecycle should be active", registry.isBundleLifecycleActive());
        assertNotEquals("Bundle lifecycle status should not be default", 
                       "Not supported", registry.getBundleLifecycleStatus());
        
        // Test bundle event methods (should not throw)
        MockBundle bundle = new MockBundle("interface.test", "1.0.0", 400L);
        
        try {
            registry.onBundleEvent(bundle);
            registry.cleanupForBundle(bundle);
        } catch (Exception e) {
            fail("Bundle event methods should not throw exceptions: " + e.getMessage());
        }
        
        log.info("ServiceRegistry interface compliance verified");
    }
    
    /**
     * Test proper cleanup when registry is closed
     */
    @Test
    public void testRegistryCleanup() {
        MockBundle bundle = new MockBundle("cleanup.registry", "1.0.0", 500L);
        
        // Verify registry is active
        assertTrue("Registry should be active initially", registry.isBundleLifecycleActive());
        
        // Add some bundle events
        registry.onBundleEvent(bundle);
        
        // Close registry
        registry.close();
        
        // Verify proper cleanup
        assertFalse("Registry should not be active after close", registry.isBundleLifecycleActive());
        
        // Should still be able to call methods without exceptions
        try {
            registry.onBundleEvent(bundle);
            registry.getDescription();
        } catch (Exception e) {
            fail("Registry methods should be safe to call after close: " + e.getMessage());
        }
        
        log.info("Registry cleanup verified successfully");
    }
    
    /**
     * Test concurrent bundle operations
     */
    @Test
    public void testConcurrentBundleOperations() throws InterruptedException {
        final int THREAD_COUNT = 10;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);
        
        // Start multiple threads that create bundle-specific registries
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    
                    MockBundle bundle = new MockBundle("thread.bundle." + threadId, "1.0.0", 1000L + threadId);
                    MetaDataTypeRegistry registry = MetaDataTypeRegistry.getInstance(bundle);
                    
                    // Verify registry is bundle-aware
                    assertTrue("Registry should be bundle-aware", registry.isBundleAware());
                    
                    // Simulate some operations
                    registry.getDetailedStatus();
                    registry.getRegistryId();
                    
                    log.debug("Thread {} completed bundle operations", threadId);
                    
                } catch (Exception e) {
                    log.error("Thread {} failed", threadId, e);
                    fail("Concurrent operations should not fail: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }
        
        // Start all threads
        startLatch.countDown();
        
        // Wait for completion
        assertTrue("All threads should complete within 10 seconds", 
                  doneLatch.await(10, TimeUnit.SECONDS));
        
        log.info("Concurrent bundle operations completed successfully");
    }
    
    /**
     * Test registry statistics and monitoring
     */
    @Test
    public void testRegistryStatistics() {
        // Create multiple bundle-specific registries
        MockBundle bundle1 = new MockBundle("stats.bundle.1", "1.0.0", 700L);
        MockBundle bundle2 = new MockBundle("stats.bundle.2", "1.0.0", 800L);
        
        MetaDataTypeRegistry registry1 = MetaDataTypeRegistry.getInstance(bundle1);
        MetaDataTypeRegistry registry2 = MetaDataTypeRegistry.getInstance(bundle2);
        
        // Get global statistics
        String globalStats = MetaDataTypeRegistry.getGlobalStats();
        
        // Verify statistics contain expected information
        assertNotNull("Global stats should not be null", globalStats);
        assertTrue("Stats should mention bundle instances", globalStats.contains("Bundle instances:"));
        assertTrue("Stats should mention active instances", globalStats.contains("Active bundle instances:"));
        
        // Get individual registry status
        String status1 = registry1.getDetailedStatus();
        String status2 = registry2.getDetailedStatus();
        
        assertNotNull("Registry status should not be null", status1);
        assertNotNull("Registry status should not be null", status2);
        assertTrue("Status should contain registry ID", status1.contains("MetaDataTypeRegistry["));
        assertTrue("Status should contain bundle info", status1.contains("Bundle:"));
        
        log.info("Registry statistics verified: {}", globalStats);
    }
}