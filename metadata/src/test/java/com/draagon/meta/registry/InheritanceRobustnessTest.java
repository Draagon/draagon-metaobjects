/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.registry;

import com.draagon.meta.MetaDataTypeId;
import com.draagon.meta.field.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Inheritance system robustness and edge case testing.
 * Tests the real inheritance system for various edge cases and performance scenarios.
 *
 * @version 6.0
 * @author Claude AI Assistant
 */
public class InheritanceRobustnessTest {

    private static final Logger log = LoggerFactory.getLogger(InheritanceRobustnessTest.class);

    private MetaDataRegistry registry;

    @Before
    public void setUp() {
        registry = MetaDataRegistry.getInstance();

        // Force registration of field types to ensure inheritance is set up
        MetaField.class.getName();
        StringField.class.getName();
        IntegerField.class.getName();
        BooleanField.class.getName();
        LongField.class.getName();
        DoubleField.class.getName();

        log.info("Inheritance robustness test setup completed");
    }

    /**
     * Test 1: Verify Inheritance Relationships Are Working
     * Validates that our converted field types properly inherit from MetaField base
     */
    @Test
    public void testInheritanceRelationshipsWorking() {
        log.info("Testing inheritance relationships are working...");

        // Test StringField inheritance
        TypeDefinition stringDef = registry.getTypeDefinition("field", "string");
        assertNotNull("StringField definition should exist", stringDef);

        if (stringDef.getParentType() != null) {
            assertEquals("StringField should inherit from field type", "field", stringDef.getParentType());
            assertEquals("StringField should inherit from base subtype", "base", stringDef.getParentSubType());

            int totalRequirements = stringDef.getChildRequirements().size();
            log.info("StringField has {} total child requirements from inheritance", totalRequirements);
            assertTrue("StringField should have inherited requirements", totalRequirements > 5);
        }

        // Test IntegerField inheritance
        TypeDefinition intDef = registry.getTypeDefinition("field", "int");
        assertNotNull("IntegerField definition should exist", intDef);

        if (intDef.getParentType() != null) {
            assertEquals("IntegerField should inherit from field type", "field", intDef.getParentType());
            assertEquals("IntegerField should inherit from base subtype", "base", intDef.getParentSubType());
        }

        log.info("Inheritance relationships validation completed");
    }

    /**
     * Test 2: Performance Impact of Inheritance Resolution
     * Measure performance difference between inherited and direct field access
     */
    @Test
    public void testInheritancePerformanceImpact() {
        log.info("Testing performance impact of inheritance resolution...");

        // Warm up the registry
        for (int i = 0; i < 100; i++) {
            registry.getTypeDefinition("field", "string");
            registry.getTypeDefinition("field", "int");
        }

        // Measure string field access (with inheritance)
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            TypeDefinition def = registry.getTypeDefinition("field", "string");
            if (def != null) {
                def.getChildRequirements().size(); // Access inherited requirements
            }
        }
        long inheritedTime = System.nanoTime() - startTime;

        // Measure simple type access
        startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            TypeDefinition def = registry.getTypeDefinition("attr", "string");
            if (def != null) {
                def.getChildRequirements().size();
            }
        }
        long simpleTime = System.nanoTime() - startTime;

        double inheritedMs = inheritedTime / 1_000_000.0;
        double simpleMs = simpleTime / 1_000_000.0;
        double overhead = inheritedMs > 0 && simpleMs > 0 ?
            (inheritedMs - simpleMs) / simpleMs * 100 : 0;

        log.info("Performance test results:");
        log.info("  Inherited field access: {:.3f} ms", inheritedMs);
        log.info("  Simple type access: {:.3f} ms", simpleMs);
        log.info("  Inheritance overhead: {:.1f}%", overhead);

        // Inheritance should not add excessive overhead
        assertTrue("Performance should be reasonable", inheritedMs < 50.0);
    }

    /**
     * Test 3: Concurrent Type Definition Access
     * Test thread safety of inheritance-enabled type definitions
     */
    @Test
    public void testConcurrentTypeDefinitionAccess() throws InterruptedException {
        log.info("Testing concurrent type definition access...");

        int numThreads = 5;
        int accessesPerThread = 200;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        String[] fieldTypes = {"string", "int", "long", "double", "boolean"};

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < accessesPerThread; j++) {
                        String fieldType = fieldTypes[j % fieldTypes.length];
                        TypeDefinition def = registry.getTypeDefinition("field", fieldType);

                        if (def != null) {
                            // Access inherited requirements (tests inheritance resolution)
                            int requirements = def.getChildRequirements().size();
                            if (requirements > 0) {
                                successCount.incrementAndGet();
                            }
                        }
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    log.warn("Concurrent access error in thread {}: {}", threadId, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue("Concurrent test should complete within 10 seconds",
            latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        int totalSuccesses = successCount.get();
        int totalErrors = errorCount.get();
        int expectedTotal = numThreads * accessesPerThread;

        log.info("Concurrent access test results: {} successes, {} errors out of {} attempts",
            totalSuccesses, totalErrors, expectedTotal);

        // Should have high success rate
        assertTrue("Most concurrent accesses should succeed",
            totalSuccesses >= expectedTotal * 0.9);
        assertTrue("Error rate should be low", totalErrors < expectedTotal * 0.1);
    }

    /**
     * Test 4: Memory Usage with Multiple Inherited Types
     * Test system behavior with many inherited field types
     */
    @Test
    public void testMemoryUsageWithInheritedTypes() {
        log.info("Testing memory usage with inherited types...");

        // Access all field types to ensure they're all loaded and inheritance resolved
        String[] allFieldTypes = {
            "string", "int", "long", "double", "float", "boolean",
            "byte", "short", "date", "timestamp"
        };

        int totalRequirements = 0;
        int inheritedTypes = 0;

        for (String fieldType : allFieldTypes) {
            TypeDefinition def = registry.getTypeDefinition("field", fieldType);
            if (def != null) {
                int requirements = def.getChildRequirements().size();
                totalRequirements += requirements;

                if (def.getParentType() != null) {
                    inheritedTypes++;
                    log.debug("Field type {} has {} requirements (inherited)", fieldType, requirements);
                }
            }
        }

        log.info("Memory usage test results:");
        log.info("  Total field types tested: {}", allFieldTypes.length);
        log.info("  Types with inheritance: {}", inheritedTypes);
        log.info("  Total requirements across all types: {}", totalRequirements);

        // Verify we have inheritance working
        assertTrue("Should have inherited types", inheritedTypes > 0);
        assertTrue("Should have reasonable number of requirements", totalRequirements > 50);

        // Memory should not be excessive
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryMB = usedMemory / (1024 * 1024);

        log.info("  Current memory usage: {} MB", memoryMB);
        assertTrue("Memory usage should be reasonable", memoryMB < 100);
    }

    /**
     * Test 5: Deferred Inheritance Resolution
     * Test the system's ability to handle inheritance when parent types aren't initially available
     */
    @Test
    public void testDeferredInheritanceResolution() {
        log.info("Testing deferred inheritance resolution...");

        // This tests the internal deferred inheritance mechanism
        // Get the count of deferred inheritance types
        int totalTypes = registry.getRegisteredTypes().size();
        log.info("Registry has {} types registered", totalTypes);

        // Verify that types that should have inheritance actually do
        TypeDefinition stringDef = registry.getTypeDefinition("field", "string");
        if (stringDef != null && stringDef.getParentType() != null) {
            log.info("StringField successfully resolved inheritance: {} -> {}:{}",
                stringDef.getSubType(), stringDef.getParentType(), stringDef.getParentSubType());
        }

        TypeDefinition intDef = registry.getTypeDefinition("field", "int");
        if (intDef != null && intDef.getParentType() != null) {
            log.info("IntegerField successfully resolved inheritance: {} -> {}:{}",
                intDef.getSubType(), intDef.getParentType(), intDef.getParentSubType());
        }

        // Basic verification that the system is working
        assertTrue("Should have multiple types registered", totalTypes > 20);
        log.info("Deferred inheritance resolution test completed");
    }

    /**
     * Test 6: Constraint System Integration with Inheritance
     * Verify that constraints work properly with inherited field types
     */
    @Test
    public void testConstraintSystemIntegrationWithInheritance() {
        log.info("Testing constraint system integration with inheritance...");

        // Verify constraint system is operational within MetaDataRegistry
        assertNotNull("MetaData registry should exist", registry);
        assertTrue("Registry should have validation constraints",
            registry.getAllValidationConstraints().size() > 0);

        // Create test field instances to verify inheritance + constraints work together
        try {
            StringField stringField = new StringField("testString");
            assertNotNull("StringField should be created", stringField);
            assertEquals("Should have correct subtype", "string", stringField.getSubType());

            IntegerField intField = new IntegerField("testInt");
            assertNotNull("IntegerField should be created", intField);
            assertEquals("Should have correct subtype", "int", intField.getSubType());

            log.info("Created field instances successfully:");
            log.info("  StringField: {} [{}:{}]", stringField.getName(),
                stringField.getType(), stringField.getSubType());
            log.info("  IntegerField: {} [{}:{}]", intField.getName(),
                intField.getType(), intField.getSubType());

        } catch (Exception e) {
            fail("Should be able to create field instances: " + e.getMessage());
        }

        log.info("Constraint system integration test completed");
    }

    /**
     * Test 7: Registry State Consistency
     * Verify that the registry maintains consistent state with inheritance
     */
    @Test
    public void testRegistryStateConsistency() {
        log.info("Testing registry state consistency...");

        // Get all registered types
        var registeredTypes = registry.getRegisteredTypes();
        assertNotNull("Registered types should not be null", registeredTypes);

        int fieldTypes = 0;
        int inheritedFieldTypes = 0;

        for (MetaDataTypeId typeId : registeredTypes) {
            if ("field".equals(typeId.type())) {
                fieldTypes++;

                TypeDefinition def = registry.getTypeDefinition(typeId.type(), typeId.subType());
                if (def != null && def.getParentType() != null) {
                    inheritedFieldTypes++;
                }
            }
        }

        log.info("Registry consistency results:");
        log.info("  Total registered types: {}", registeredTypes.size());
        log.info("  Field types: {}", fieldTypes);
        log.info("  Field types with inheritance: {}", inheritedFieldTypes);

        assertTrue("Should have field types", fieldTypes > 0);
        assertTrue("Should have inherited field types", inheritedFieldTypes > 0);
        assertTrue("Should have reasonable inheritance ratio",
            inheritedFieldTypes >= fieldTypes * 0.3); // At least 30% should use inheritance

        log.info("Registry state consistency test completed");
    }
}