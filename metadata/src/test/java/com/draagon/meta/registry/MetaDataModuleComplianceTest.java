package com.draagon.meta.registry;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Comprehensive compliance test for @MetaDataType annotations in the metadata module.
 *
 * This test verifies that all MetaData derived classes in the metadata module have:
 * 1. Proper @MetaDataType annotations
 * 2. Correct registration in MetaDataRegistry
 * 3. Proper inheritance relationship resolution
 */
public class MetaDataModuleComplianceTest extends MetaDataTypeComplianceTestBase {

    private static final Logger log = LoggerFactory.getLogger(MetaDataModuleComplianceTest.class);

    /**
     * Test that all MetaData classes in metadata module have @MetaDataType annotations
     */
    @Test
    public void testMetaDataModuleAnnotationCompliance() {
        log.info("🔍 Testing @MetaDataType annotation compliance for metadata module");

        // Scan for MetaData classes in metadata module packages
        Set<Class<?>> metaDataClasses = scanForMetaDataClasses(
                "com.draagon.meta.field",
                "com.draagon.meta.object",
                "com.draagon.meta.attr",
                "com.draagon.meta.validator",
                "com.draagon.meta.view",
                "com.draagon.meta.key",
                "com.draagon.meta.loader"
        );

        log.info("Found {} MetaData classes in metadata module", metaDataClasses.size());

        // Validate annotations
        List<String> annotationViolations = validateAnnotationPresence(metaDataClasses);

        // Report results
        if (!annotationViolations.isEmpty()) {
            String report = createComplianceReport("metadata", metaDataClasses, annotationViolations, List.of(), List.of());
            log.error("\n{}", report);
            fail("❌ Metadata module has @MetaDataType annotation violations:\n" +
                 annotationViolations.stream().reduce((a, b) -> a + "\n" + b).orElse(""));
        }

        log.info("✅ All MetaData classes in metadata module have proper @MetaDataType annotations");
    }

    /**
     * Test that all annotated classes are properly registered in MetaDataRegistry
     */
    @Test
    public void testMetaDataModuleRegistrationCompliance() {
        log.info("🔍 Testing MetaDataRegistry registration compliance for metadata module");

        // Scan for MetaData classes
        Set<Class<?>> metaDataClasses = scanForMetaDataClasses(
                "com.draagon.meta.field",
                "com.draagon.meta.object",
                "com.draagon.meta.attr",
                "com.draagon.meta.validator",
                "com.draagon.meta.view",
                "com.draagon.meta.key",
                "com.draagon.meta.loader"
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
                "com.draagon.meta.field",
                "com.draagon.meta.object",
                "com.draagon.meta.attr",
                "com.draagon.meta.validator",
                "com.draagon.meta.view",
                "com.draagon.meta.key",
                "com.draagon.meta.loader"
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
        log.info("🔍 Running comprehensive @MetaDataType compliance test for metadata module");

        // Scan for MetaData classes
        Set<Class<?>> metaDataClasses = scanForMetaDataClasses(
                "com.draagon.meta.field",
                "com.draagon.meta.object",
                "com.draagon.meta.attr",
                "com.draagon.meta.validator",
                "com.draagon.meta.view",
                "com.draagon.meta.key",
                "com.draagon.meta.loader"
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

        log.info("🎉 Metadata module passes all @MetaDataType compliance checks!");
    }
}