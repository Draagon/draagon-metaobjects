package com.metaobjects.registry;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 *
 * This test verifies that all MetaData derived classes in the metadata module have:
 * 2. Correct registration in MetaDataRegistry
 * 3. Proper inheritance relationship resolution
 */
public class MetaDataModuleComplianceTest extends MetaDataTypeComplianceTestBase {

    private static final Logger log = LoggerFactory.getLogger(MetaDataModuleComplianceTest.class);

    /**
     */
    @Test
    public void testMetaDataModuleAnnotationCompliance() {

        // Scan for MetaData classes in metadata module packages
        Set<Class<?>> metaDataClasses = scanForMetaDataClasses(
                "com.metaobjects.field",
                "com.metaobjects.object",
                "com.metaobjects.attr",
                "com.metaobjects.validator",
                "com.metaobjects.view",
                "com.metaobjects.key",
                "com.metaobjects.loader"
        );

        log.info("Found {} MetaData classes in metadata module", metaDataClasses.size());

        // Validate annotations
        List<String> annotationViolations = validateAnnotationPresence(metaDataClasses);

        // Report results
        if (!annotationViolations.isEmpty()) {
            String report = createComplianceReport("metadata", metaDataClasses, annotationViolations, List.of(), List.of());
            log.error("\n{}", report);
            fail("❌ Metadata module has provider registration violations:\n" +
                 annotationViolations.stream().reduce((a, b) -> a + "\n" + b).orElse(""));
        }

    }

    /**
     * Test that all annotated classes are properly registered in MetaDataRegistry
     */
    @Test
    public void testMetaDataModuleRegistrationCompliance() {
        log.info("🔍 Testing MetaDataRegistry registration compliance for metadata module");

        // Scan for MetaData classes
        Set<Class<?>> metaDataClasses = scanForMetaDataClasses(
                "com.metaobjects.field",
                "com.metaobjects.object",
                "com.metaobjects.attr",
                "com.metaobjects.validator",
                "com.metaobjects.view",
                "com.metaobjects.key",
                "com.metaobjects.loader"
        );

        // Validate registration consistency
        List<String> registrationViolations = validateRegistrationConsistency(metaDataClasses);

        // Report results
        if (!registrationViolations.isEmpty()) {
            String report = createComplianceReport("metadata", metaDataClasses, List.of(), registrationViolations, List.of());
            log.error("\n{}", report);
            fail("❌ Metadata module has registration compliance violations:\n" +
                 registrationViolations.stream().reduce((a, b) -> a + "\n" + b).orElse(""));
        }

        log.info("✅ All annotated classes in metadata module are properly registered");
    }

    /**
     * Test that inheritance relationships are properly resolved
     */
    @Test
    public void testMetaDataModuleInheritanceCompliance() {
        log.info("🔍 Testing inheritance resolution compliance for metadata module");

        // Scan for MetaData classes
        Set<Class<?>> metaDataClasses = scanForMetaDataClasses(
                "com.metaobjects.field",
                "com.metaobjects.object",
                "com.metaobjects.attr",
                "com.metaobjects.validator",
                "com.metaobjects.view",
                "com.metaobjects.key",
                "com.metaobjects.loader"
        );

        // Validate inheritance resolution
        List<String> inheritanceViolations = validateInheritanceResolution(metaDataClasses);

        // Report results
        if (!inheritanceViolations.isEmpty()) {
            String report = createComplianceReport("metadata", metaDataClasses, List.of(), List.of(), inheritanceViolations);
            log.error("\n{}", report);
            fail("❌ Metadata module has inheritance resolution violations:\n" +
                 inheritanceViolations.stream().reduce((a, b) -> a + "\n" + b).orElse(""));
        }

        log.info("✅ All inheritance relationships in metadata module are properly resolved");
    }

    /**
     * Comprehensive compliance test that combines all validation checks
     */
    @Test
    public void testMetaDataModuleComprehensiveCompliance() {

        // Scan for MetaData classes
        Set<Class<?>> metaDataClasses = scanForMetaDataClasses(
                "com.metaobjects.field",
                "com.metaobjects.object",
                "com.metaobjects.attr",
                "com.metaobjects.validator",
                "com.metaobjects.view",
                "com.metaobjects.key",
                "com.metaobjects.loader"
        );

        // Run all validation checks
        List<String> annotationViolations = validateAnnotationPresence(metaDataClasses);
        List<String> registrationViolations = validateRegistrationConsistency(metaDataClasses);
        List<String> inheritanceViolations = validateInheritanceResolution(metaDataClasses);

        // Create comprehensive report
        String report = createComplianceReport("metadata", metaDataClasses,
                annotationViolations, registrationViolations, inheritanceViolations);

        log.info("\n{}", report);

        // Check for any violations
        int totalViolations = annotationViolations.size() + registrationViolations.size() + inheritanceViolations.size();

        if (totalViolations > 0) {
            fail("❌ Metadata module has " + totalViolations + " compliance violations. See log for details.");
        }

    }
}