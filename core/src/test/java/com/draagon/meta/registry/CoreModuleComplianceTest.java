package com.draagon.meta.registry;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 *
 * This test verifies that all MetaData derived classes in the core module have:
 * 2. Correct registration in MetaDataRegistry
 * 3. Proper inheritance relationship resolution
 */
public class CoreModuleComplianceTest extends com.draagon.meta.registry.MetaDataTypeComplianceTestBase {

    private static final Logger log = LoggerFactory.getLogger(CoreModuleComplianceTest.class);

    /**
     */
    @Test
    public void testCoreModuleAnnotationCompliance() {

        // Scan for MetaData classes in core module packages
        Set<Class<?>> metaDataClasses = scanForMetaDataClasses(
                "com.draagon.meta.object.data",
                "com.draagon.meta.object.value",
                "com.draagon.meta.loader.file"
        );

        log.info("Found {} MetaData classes in core module", metaDataClasses.size());

        // Validate annotations
        List<String> annotationViolations = validateAnnotationPresence(metaDataClasses);

        // Report results
        if (!annotationViolations.isEmpty()) {
            String report = createComplianceReport("core", metaDataClasses, annotationViolations, List.of(), List.of());
            log.error("\n{}", report);
            fail("❌ Core module has provider registration violations:\n" +
                 annotationViolations.stream().reduce((a, b) -> a + "\n" + b).orElse(""));
        }

    }

    /**
     * Test that all annotated classes are properly registered in MetaDataRegistry
     */
    @Test
    public void testCoreModuleRegistrationCompliance() {
        log.info("🔍 Testing MetaDataRegistry registration compliance for core module");

        // Scan for MetaData classes
        Set<Class<?>> metaDataClasses = scanForMetaDataClasses(
                "com.draagon.meta.object.data",
                "com.draagon.meta.object.value",
                "com.draagon.meta.loader.file"
        );

        // Validate registration consistency
        List<String> registrationViolations = validateRegistrationConsistency(metaDataClasses);

        // Report results
        if (!registrationViolations.isEmpty()) {
            String report = createComplianceReport("core", metaDataClasses, List.of(), registrationViolations, List.of());
            log.error("\n{}", report);
            fail("❌ Core module has registration compliance violations:\n" +
                 registrationViolations.stream().reduce((a, b) -> a + "\n" + b).orElse(""));
        }

        log.info("✅ All annotated classes in core module are properly registered");
    }

    /**
     * Test that inheritance relationships are properly resolved
     */
    @Test
    public void testCoreModuleInheritanceCompliance() {
        log.info("🔍 Testing inheritance resolution compliance for core module");

        // Scan for MetaData classes
        Set<Class<?>> metaDataClasses = scanForMetaDataClasses(
                "com.draagon.meta.object.data",
                "com.draagon.meta.object.value",
                "com.draagon.meta.loader.file"
        );

        // Validate inheritance resolution
        List<String> inheritanceViolations = validateInheritanceResolution(metaDataClasses);

        // Report results
        if (!inheritanceViolations.isEmpty()) {
            String report = createComplianceReport("core", metaDataClasses, List.of(), List.of(), inheritanceViolations);
            log.error("\n{}", report);
            fail("❌ Core module has inheritance resolution violations:\n" +
                 inheritanceViolations.stream().reduce((a, b) -> a + "\n" + b).orElse(""));
        }

        log.info("✅ All inheritance relationships in core module are properly resolved");
    }

    /**
     * Comprehensive compliance test that combines all validation checks
     */
    @Test
    public void testCoreModuleComprehensiveCompliance() {

        // Scan for MetaData classes
        Set<Class<?>> metaDataClasses = scanForMetaDataClasses(
                "com.draagon.meta.object.data",
                "com.draagon.meta.object.value",
                "com.draagon.meta.loader.file"
        );

        // Run all validation checks
        List<String> annotationViolations = validateAnnotationPresence(metaDataClasses);
        List<String> registrationViolations = validateRegistrationConsistency(metaDataClasses);
        List<String> inheritanceViolations = validateInheritanceResolution(metaDataClasses);

        // Create comprehensive report
        String report = createComplianceReport("core", metaDataClasses,
                annotationViolations, registrationViolations, inheritanceViolations);

        log.info("\n{}", report);

        // Check for any violations
        int totalViolations = annotationViolations.size() + registrationViolations.size() + inheritanceViolations.size();

        if (totalViolations > 0) {
            fail("❌ Core module has " + totalViolations + " compliance violations. See log for details.");
        }

    }
}