package com.draagon.meta.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared registry for all test suite execution.
 *
 * <p>Follows the MetaObjects READ-OPTIMIZED architecture principle:
 * registry is loaded once per application lifetime, not repeatedly
 * created/destroyed. Only OSGi lifecycle tests should reset this.</p>
 *
 * <p>This eliminates test interference and reflects real-world usage
 * where there's one registry per application instance.</p>
 */
public class SharedTestRegistry {

    private static final Logger log = LoggerFactory.getLogger(SharedTestRegistry.class);

    private static volatile MetaDataRegistry sharedRegistry;
    private static volatile boolean initialized = false;
    private static final Object lock = new Object();

    /**
     * Get the shared registry instance for all tests.
     * Initializes once per test suite execution.
     */
    public static MetaDataRegistry getInstance() {
        if (!initialized) {
            synchronized (lock) {
                if (!initialized) {
                    log.info("Initializing shared test registry for entire test suite");
                    sharedRegistry = MetaDataRegistry.getInstance();
                    initialized = true;
                    log.info("Shared test registry initialized with {} types",
                            sharedRegistry.getRegisteredTypeNames().size());
                }
            }
        }
        return sharedRegistry;
    }

    /**
     * Reset the shared registry - ONLY for OSGi lifecycle tests.
     * Regular tests should NOT call this method.
     */
    public static void resetForOSGiTest() {
        synchronized (lock) {
            log.warn("RESETTING shared registry for OSGi lifecycle test - this should be rare!");
            initialized = false;
            sharedRegistry = null;
            // Force new registry creation on next getInstance() call
        }
    }

    /**
     * Check if the shared registry is properly initialized
     */
    public static boolean isInitialized() {
        return initialized && sharedRegistry != null;
    }

    /**
     * Get registry status for debugging
     */
    public static String getStatus() {
        if (!initialized) {
            return "SharedTestRegistry: NOT INITIALIZED";
        }
        return String.format("SharedTestRegistry: INITIALIZED with %d types",
                sharedRegistry.getRegisteredTypeNames().size());
    }
}