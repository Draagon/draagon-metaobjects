package com.draagon.meta.registry;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Comprehensive compliance test for @MetaDataType annotations in the web module.
 *
 * This test verifies that all MetaData derived classes in the web module have:
 * 1. Proper @MetaDataType annotations
 * 2. Correct registration in MetaDataRegistry
 * 3. Proper inheritance relationship resolution
 */
public class WebModuleComplianceTest extends com.draagon.meta.registry.MetaDataTypeComplianceTestBase {

    private static final Logger log = LoggerFactory.getLogger(WebModuleComplianceTest.class);

    /**
     * Test that all MetaData classes in web module have @MetaDataType annotations
     */
    @Test
    public void testWebModuleAnnotationCompliance() {
        log.info("🔍 Testing @MetaDataType annotation compliance for web module");

        // Scan for MetaData classes in web module packages
        Set<Class<?>> metaDataClasses = scanForMetaDataClasses(
                "com.draagon.meta.web.view"
        );

        log.info("Found {} MetaData classes in web module", metaDataClasses.size());

        // Validate annotations
        List<String> annotationViolations = validateAnnotationPresence(metaDataClasses);

        // Report results
        if (!annotationViolations.isEmpty()) {
            String report = createComplianceReport("web", metaDataClasses, annotationViolations, List.of(), List.of());
            log.error("\n{}", report);
            fail("❌ Web module has provider registration violations:\n" +
                 annotationViolations.stream().reduce((a, b) -> a + "\n" + b).orElse(""));
        }

        log.info("✅ All MetaData classes in web module have proper registerTypes methods");
    }

    /**
     * Test that all annotated classes are properly registered in MetaDataRegistry
     */
    @Test
    public void testWebModuleRegistrationCompliance() {
        log.info("🔍 Testing MetaDataRegistry registration compliance for web module");

        // Scan for MetaData classes
        Set<Class<?>> metaDataClasses = scanForMetaDataClasses(
                "com.draagon.meta.web.view"
        );

        // Validate registration consistency
        List<String> registrationViolations = validateRegistrationConsistency(metaDataClasses);

        // Report results
        if (!registrationViolations.isEmpty()) {
            String report = createComplianceReport("web", metaDataClasses, List.of(), registrationViolations, List.of());
            log.error("\n{}", report);
            fail("❌ Web module has registration compliance violations:\n" +
                 registrationViolations.stream().reduce((a, b) -> a + "\n" + b).orElse(""));
        }

        log.info("✅ All annotated classes in web module are properly registered");
    }

    /**
     * Test that inheritance relationships are properly resolved
     */
    @Test
    public void testWebModuleInheritanceCompliance() {
        log.info("🔍 Testing inheritance resolution compliance for web module");

        // Scan for MetaData classes
        Set<Class<?>> metaDataClasses = scanForMetaDataClasses(
                "com.draagon.meta.web.view"
        );

        // Validate inheritance resolution
        List<String> inheritanceViolations = validateInheritanceResolution(metaDataClasses);

        // Report results
        if (!inheritanceViolations.isEmpty()) {
            String report = createComplianceReport("web", metaDataClasses, List.of(), List.of(), inheritanceViolations);
            log.error("\n{}", report);
            fail("❌ Web module has inheritance resolution violations:\n" +
                 inheritanceViolations.stream().reduce((a, b) -> a + "\n" + b).orElse(""));
        }

        log.info("✅ All inheritance relationships in web module are properly resolved");
    }

    /**
     * Comprehensive compliance test that combines all validation checks
     */
    @Test
    public void testWebModuleComprehensiveCompliance() {
        log.info("🔍 Running comprehensive @MetaDataType compliance test for web module");

        // Scan for MetaData classes
        Set<Class<?>> metaDataClasses = scanForMetaDataClasses(
                "com.draagon.meta.web.view"
        );

        // Run all validation checks
        List<String> annotationViolations = validateAnnotationPresence(metaDataClasses);
        List<String> registrationViolations = validateRegistrationConsistency(metaDataClasses);
        List<String> inheritanceViolations = validateInheritanceResolution(metaDataClasses);

        // Create comprehensive report
        String report = createComplianceReport("web", metaDataClasses,
                annotationViolations, registrationViolations, inheritanceViolations);

        log.info("\n{}", report);

        // Check for any violations
        int totalViolations = annotationViolations.size() + registrationViolations.size() + inheritanceViolations.size();

        if (totalViolations > 0) {
            fail("❌ Web module has " + totalViolations + " compliance violations. See log for details.");
        }

        log.info("🎉 Web module passes all @MetaDataType compliance checks!");
    }
}