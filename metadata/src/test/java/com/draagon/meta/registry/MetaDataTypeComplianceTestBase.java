package com.draagon.meta.registry;

import com.draagon.meta.MetaData;
import com.draagon.meta.loader.MetaDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Base utility class for testing @MetaDataType compliance across modules.
 *
 * This utility provides methods to:
 * 1. Scan for MetaData derived classes in specific packages
 * 2. Validate @MetaDataType annotation presence and correctness
 * 3. Verify registration consistency with MetaDataRegistry
 * 4. Check inheritance relationship resolution
 */
public class MetaDataTypeComplianceTestBase {

    private static final Logger log = LoggerFactory.getLogger(MetaDataTypeComplianceTestBase.class);

    /**
     * Scan for all concrete MetaData subclasses in the given package prefixes
     */
    protected Set<Class<?>> scanForMetaDataClasses(String... packagePrefixes) {
        Set<Class<?>> metaDataClasses = new HashSet<>();

        for (String packagePrefix : packagePrefixes) {
            try {
                Set<Class<?>> classes = scanPackageForClasses(packagePrefix);

                for (Class<?> clazz : classes) {
                    if (isConcreteMetaDataClass(clazz)) {
                        metaDataClasses.add(clazz);
                        log.debug("Found MetaData class: {}", clazz.getName());
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to scan package {}: {}", packagePrefix, e.getMessage());
            }
        }

        log.info("Found {} MetaData classes in packages: {}",
                metaDataClasses.size(), Arrays.toString(packagePrefixes));

        return metaDataClasses;
    }

    /**
     * Check if a class is a concrete MetaData subclass that requires @MetaDataType annotation.
     *
     * Excludes MetaDataLoader and its subclasses since they are infrastructure classes
     * used to load metadata, not metadata types themselves.
     */
    private boolean isConcreteMetaDataClass(Class<?> clazz) {
        return MetaData.class.isAssignableFrom(clazz) &&
               !clazz.equals(MetaData.class) &&
               !Modifier.isAbstract(clazz.getModifiers()) &&
               !clazz.isInterface() &&
               !clazz.isEnum() &&
               !clazz.isMemberClass() &&  // Skip inner classes
               !clazz.getName().contains("$") && // Skip generated classes
               !MetaDataLoader.class.isAssignableFrom(clazz); // Skip MetaDataLoaders (infrastructure classes)
    }

    /**
     * Validate @MetaDataType annotations on a set of classes
     */
    protected List<String> validateAnnotationPresence(Set<Class<?>> metaDataClasses) {
        List<String> violations = new ArrayList<>();

        for (Class<?> clazz : metaDataClasses) {
            MetaDataType annotation = clazz.getAnnotation(MetaDataType.class);

            if (annotation == null) {
                violations.add("❌ MISSING @MetaDataType: " + clazz.getSimpleName() +
                              " (" + clazz.getName() + ")");
            } else {
                // Validate annotation values
                validateAnnotationValues(annotation, clazz, violations);
            }
        }

        return violations;
    }

    /**
     * Validate the values within @MetaDataType annotations
     */
    private void validateAnnotationValues(MetaDataType annotation, Class<?> clazz, List<String> violations) {
        String type = annotation.type();
        String subType = annotation.subType();
        String description = annotation.description();

        // Check for empty/invalid values
        if (type == null || type.trim().isEmpty()) {
            violations.add("❌ EMPTY TYPE: " + clazz.getSimpleName() + " has empty type()");
        }

        if (subType == null || subType.trim().isEmpty()) {
            violations.add("❌ EMPTY SUBTYPE: " + clazz.getSimpleName() + " has empty subType()");
        }

        if (description == null || description.trim().isEmpty()) {
            violations.add("⚠️  EMPTY DESCRIPTION: " + clazz.getSimpleName() + " has empty description()");
        }

        // Check for valid type values (basic validation)
        if (!isValidMetaDataType(type)) {
            violations.add("❌ INVALID TYPE: " + clazz.getSimpleName() + " has invalid type: " + type);
        }
    }

    /**
     * Check if the type value is a known MetaData type
     */
    private boolean isValidMetaDataType(String type) {
        Set<String> validTypes = Set.of("field", "object", "attr", "validator", "view", "key", "loader");
        return validTypes.contains(type);
    }

    /**
     * Verify that annotated classes are properly registered in MetaDataRegistry
     */
    protected List<String> validateRegistrationConsistency(Set<Class<?>> metaDataClasses) {
        List<String> violations = new ArrayList<>();

        // Force class loading to trigger static registration blocks
        forceClassLoading(metaDataClasses);

        MetaDataRegistry registry = MetaDataRegistry.getInstance();
        Set<String> registeredTypes = registry.getRegisteredTypeNames();

        // Check: Annotated classes should be registered
        for (Class<?> clazz : metaDataClasses) {
            MetaDataType annotation = clazz.getAnnotation(MetaDataType.class);

            if (annotation != null) {
                String expectedTypeName = annotation.type() + "." + annotation.subType();

                if (!registeredTypes.contains(expectedTypeName)) {
                    violations.add("❌ ANNOTATED but NOT REGISTERED: " + clazz.getSimpleName() +
                                  " -> expected: " + expectedTypeName);
                }
            }
        }

        // Check: Registered types should have corresponding annotations
        for (String typeName : registeredTypes) {
            if (!typeName.contains(".")) continue; // Skip invalid type names

            String[] parts = typeName.split("\\.");
            if (parts.length != 2) continue;

            TypeDefinition def = registry.getTypeDefinition(parts[0], parts[1]);
            if (def != null) {
                Class<?> implClass = def.getImplementationClass();

                // Only check classes from our scanned set (module-specific)
                if (metaDataClasses.contains(implClass)) {
                    MetaDataType annotation = implClass.getAnnotation(MetaDataType.class);

                    if (annotation == null) {
                        violations.add("❌ REGISTERED but NO ANNOTATION: " + typeName +
                                      " -> " + implClass.getSimpleName());
                    }
                }
            }
        }

        return violations;
    }

    /**
     * Verify inheritance relationships are properly resolved
     */
    protected List<String> validateInheritanceResolution(Set<Class<?>> metaDataClasses) {
        List<String> violations = new ArrayList<>();

        MetaDataRegistry registry = MetaDataRegistry.getInstance();

        // Get all type definitions for our classes
        for (Class<?> clazz : metaDataClasses) {
            MetaDataType annotation = clazz.getAnnotation(MetaDataType.class);

            if (annotation != null) {
                String typeName = annotation.type() + "." + annotation.subType();
                TypeDefinition def = registry.getTypeDefinition(annotation.type(), annotation.subType());

                if (def != null && def.hasParent()) {
                    // Check that parent type exists
                    TypeDefinition parent = registry.getTypeDefinition(
                        def.getParentType(), def.getParentSubType());

                    if (parent == null) {
                        violations.add("❌ UNRESOLVED INHERITANCE: " + typeName +
                                      " -> parent " + def.getParentQualifiedName() + " not found");
                    }
                }
            }
        }

        return violations;
    }

    /**
     * Force loading of classes to trigger static registration blocks
     */
    private void forceClassLoading(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            try {
                // Force static initialization
                Class.forName(clazz.getName(), true, clazz.getClassLoader());
            } catch (Exception e) {
                log.warn("Failed to force load class {}: {}", clazz.getName(), e.getMessage());
            }
        }

        // Small delay to allow static blocks to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Scan a package for all classes (simple implementation)
     */
    private Set<Class<?>> scanPackageForClasses(String packageName) throws Exception {
        Set<Class<?>> classes = new HashSet<>();
        String packagePath = packageName.replace('.', '/');

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL packageUrl = classLoader.getResource(packagePath);

        if (packageUrl != null) {
            File packageDir = new File(packageUrl.toURI());

            if (packageDir.exists() && packageDir.isDirectory()) {
                scanDirectory(packageDir, packageName, classes);
            }
        }

        return classes;
    }

    /**
     * Recursively scan directory for .class files
     */
    private void scanDirectory(File directory, String packageName, Set<Class<?>> classes) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                // Recurse into subdirectory
                scanDirectory(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                // Load class
                String className = file.getName().substring(0, file.getName().length() - 6);
                String fullClassName = packageName + "." + className;

                try {
                    Class<?> clazz = Class.forName(fullClassName);
                    classes.add(clazz);
                } catch (Exception e) {
                    // Skip problematic classes
                    log.debug("Could not load class {}: {}", fullClassName, e.getMessage());
                }
            }
        }
    }

    /**
     * Create a comprehensive compliance report
     */
    protected String createComplianceReport(String moduleName, Set<Class<?>> scannedClasses,
                                          List<String> annotationViolations,
                                          List<String> registrationViolations,
                                          List<String> inheritanceViolations) {
        StringBuilder report = new StringBuilder();

        report.append("🔍 @MetaDataType Compliance Report for ").append(moduleName).append(" Module\n");
        report.append("=" .repeat(60)).append("\n\n");

        // Summary
        report.append("📊 SUMMARY:\n");
        report.append("  Scanned Classes: ").append(scannedClasses.size()).append("\n");
        report.append("  Annotation Violations: ").append(annotationViolations.size()).append("\n");
        report.append("  Registration Violations: ").append(registrationViolations.size()).append("\n");
        report.append("  Inheritance Violations: ").append(inheritanceViolations.size()).append("\n\n");

        // Scanned classes
        report.append("📋 SCANNED CLASSES:\n");
        for (Class<?> clazz : scannedClasses.stream().sorted(Comparator.comparing(Class::getSimpleName)).collect(Collectors.toSet())) {
            MetaDataType annotation = clazz.getAnnotation(MetaDataType.class);
            if (annotation != null) {
                report.append("  ✅ ").append(clazz.getSimpleName())
                      .append(" -> ").append(annotation.type()).append(".").append(annotation.subType()).append("\n");
            } else {
                report.append("  ❌ ").append(clazz.getSimpleName()).append(" -> NO ANNOTATION\n");
            }
        }
        report.append("\n");

        // Violations
        if (!annotationViolations.isEmpty()) {
            report.append("❌ ANNOTATION VIOLATIONS:\n");
            annotationViolations.forEach(v -> report.append("  ").append(v).append("\n"));
            report.append("\n");
        }

        if (!registrationViolations.isEmpty()) {
            report.append("❌ REGISTRATION VIOLATIONS:\n");
            registrationViolations.forEach(v -> report.append("  ").append(v).append("\n"));
            report.append("\n");
        }

        if (!inheritanceViolations.isEmpty()) {
            report.append("❌ INHERITANCE VIOLATIONS:\n");
            inheritanceViolations.forEach(v -> report.append("  ").append(v).append("\n"));
            report.append("\n");
        }

        if (annotationViolations.isEmpty() && registrationViolations.isEmpty() && inheritanceViolations.isEmpty()) {
            report.append("🎉 ALL COMPLIANCE CHECKS PASSED!\n");
        }

        return report.toString();
    }
}