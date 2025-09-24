package com.draagon.meta.registry;

import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.validator.RequiredValidator;
import com.draagon.meta.validator.LengthValidator;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Shared base class for all MetaData tests that provides a single, static, fully-initialized
 * MetaDataRegistry instance. This prevents registry conflicts between tests and ensures
 * all types and constraints are properly loaded.
 *
 * Key benefits:
 * - Single shared MetaDataRegistry across ALL tests
 * - All types loaded once during class initialization
 * - No registry teardown between tests (eliminates conflicts)
 * - Proper initialization order to prevent missing type registrations
 */
public abstract class SharedRegistryTestBase {

    private static final Logger log = LoggerFactory.getLogger(SharedRegistryTestBase.class);

    /**
     * Shared static registry instance used by ALL tests.
     * This is initialized once and never torn down.
     */
    protected static MetaDataRegistry sharedRegistry;

    /**
     * Shared static loader instance used by tests that need a loader.
     * This uses the shared registry and is initialized once.
     */
    protected static SimpleLoader sharedLoader;

    /**
     * Flag to ensure initialization happens only once across all test classes
     */
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * Initialize the shared registry and loader once for all tests.
     * This method is called by JUnit before any test methods run.
     */
    @BeforeClass
    public static void initializeSharedRegistry() {
        if (initialized.compareAndSet(false, true)) {
            log.info("Initializing shared MetaDataRegistry for all tests...");

            try {
                // Get the singleton registry instance
                sharedRegistry = MetaDataRegistry.getInstance();

                // Force loading of all MetaData classes to trigger their static registration blocks
                forceLoadAllMetaDataClasses();

                // Create a shared loader that uses the same registry
                sharedLoader = createSharedLoader();

                // Log registry state
                var stats = sharedRegistry.getStats();
                log.info("Shared registry initialized successfully:");
                log.info("  - Total types: {}", stats.totalTypes());
                log.info("  - Constraint stats: {}", stats.constraintStats().size());
                log.info("  - Service registry: {}", stats.serviceRegistryDescription());

            } catch (Exception e) {
                log.error("Failed to initialize shared registry", e);
                throw new RuntimeException("Failed to initialize shared registry for tests", e);
            }
        }
    }

    /**
     * Force loading of all MetaData classes to ensure their static registration blocks
     * execute and register all types and constraints.
     */
    private static void forceLoadAllMetaDataClasses() {
        log.debug("Force loading MetaData classes to trigger registrations...");

        try {
            // Field classes
            Class.forName("com.draagon.meta.field.MetaField");
            Class.forName("com.draagon.meta.field.BooleanField");
            Class.forName("com.draagon.meta.field.StringField");
            Class.forName("com.draagon.meta.field.IntegerField");
            Class.forName("com.draagon.meta.field.LongField");
            Class.forName("com.draagon.meta.field.DoubleField");
            Class.forName("com.draagon.meta.field.DateField");
            Class.forName("com.draagon.meta.field.TimestampField");

            // Attribute classes
            Class.forName("com.draagon.meta.attr.MetaAttribute");
            Class.forName("com.draagon.meta.attr.StringAttribute");
            Class.forName("com.draagon.meta.attr.IntAttribute");
            Class.forName("com.draagon.meta.attr.BooleanAttribute");
            Class.forName("com.draagon.meta.attr.StringArrayAttribute");

            // Object classes
            Class.forName("com.draagon.meta.object.MetaObject");
            Class.forName("com.draagon.meta.object.pojo.PojoMetaObject");
            Class.forName("com.draagon.meta.object.proxy.ProxyMetaObject");

            // Validator classes
            Class.forName("com.draagon.meta.validator.RequiredValidator");
            Class.forName("com.draagon.meta.validator.LengthValidator");

            // View classes (if available)
            try {
                Class.forName("com.draagon.meta.view.BasicMetaView");
                Class.forName("com.draagon.meta.view.MetaView");
            } catch (ClassNotFoundException e) {
                log.debug("View classes not available in this module: {}", e.getMessage());
            }

            // Trigger validator instances to ensure registration
            try {
                new RequiredValidator("test");
                new LengthValidator("test");
            } catch (Exception e) {
                log.debug("Validator instantiation for registration: {}", e.getMessage());
            }

            // Allow time for static blocks to complete
            Thread.sleep(100);

        } catch (ClassNotFoundException | InterruptedException e) {
            log.warn("Some classes could not be loaded during initialization: {}", e.getMessage());
        }

        log.debug("Completed force loading of MetaData classes");
    }

    /**
     * Create the shared SimpleLoader instance that all tests can use.
     */
    private static SimpleLoader createSharedLoader() {
        return new SimpleLoader("shared-test-loader");
    }

    /**
     * Get the shared MetaDataRegistry instance.
     * This is the same instance used by all tests.
     */
    protected static MetaDataRegistry getSharedRegistry() {
        if (sharedRegistry == null) {
            throw new IllegalStateException("Shared registry not initialized. Make sure test class calls super.initializeSharedRegistry()");
        }
        return sharedRegistry;
    }

    /**
     * Get the shared SimpleLoader instance for tests that need a loader.
     */
    protected static SimpleLoader getSharedLoader() {
        if (sharedLoader == null) {
            throw new IllegalStateException("Shared loader not initialized. Make sure test class calls super.initializeSharedRegistry()");
        }
        return sharedLoader;
    }

    /**
     * Create a loader for specific test resources while still using the shared registry.
     * This allows tests to load specific metadata files without creating registry conflicts.
     */
    protected SimpleLoader createTestLoader(String testName, List<URI> sources) {
        SimpleLoader loader = new SimpleLoader("test-" + testName)
            .setSourceURIs(sources)
            .init();

        return loader;
    }

    /**
     * Get registry statistics for debugging.
     */
    protected static void logRegistryState(String context) {
        var stats = getSharedRegistry().getStats();
        log.info("Registry state [{}]: {} types, {} constraints",
                context, stats.totalTypes(), stats.constraintStats().size());
    }
}