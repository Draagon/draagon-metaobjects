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
 *
 * This utility provides methods to:
 * 1. Scan for MetaData derived classes in specific packages
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
     * Validate provider-based registration pattern (registerTypes method)
     */
    protected List<String> validateAnnotationPresence(Set<Class<?>> metaDataClasses) {
        List<String> violations = new ArrayList<>();

        for (Class<?> clazz : metaDataClasses) {
            try {
                clazz.getDeclaredMethod("registerTypes", MetaDataRegistry.class);
                log.debug("✅ Found registerTypes method: {} ({})", clazz.getSimpleName(), clazz.getName());
            } catch (NoSuchMethodException e) {
                violations.add("❌ MISSING registerTypes method: " + clazz.getSimpleName() +
                              " (" + clazz.getName() + ")");
            } catch (UnsupportedClassVersionError e) {
                log.warn("⚠️ Skipping class due to version incompatibility: {} - {}", clazz.getName(), e.getMessage());
                // Skip this class - it was compiled with incompatible Java version
            } catch (ClassFormatError e) {
                log.warn("⚠️ Skipping class due to ClassFormatError: {} - {}", clazz.getName(), e.getMessage());
                // Skip this class - it has incompatible bytecode (likely old JSP/servlet classes)
            } catch (LinkageError e) {
                log.warn("⚠️ Skipping class due to linkage error: {} - {}", clazz.getName(), e.getMessage());
                // Skip this class - it has dependency issues
            } catch (Exception e) {
                log.warn("⚠️ Skipping class due to unexpected error: {} - {}", clazz.getName(), e.getMessage());
                // Skip this class - other reflection/loading issues
            }
        }

        return violations;
    }

    // validateAnnotationValues method removed - no longer needed with provider-based registration

    /**
     * Check if the type value is a known MetaData type
     */
    private boolean isValidMetaDataType(String type) {
        Set<String> validTypes = Set.of("field", "object", "attr", "validator", "view", "key", "loader");
        return validTypes.contains(type);
    }

    /**
     * Verify that classes are properly registered in MetaDataRegistry through providers
     */
    protected List<String> validateRegistrationConsistency(Set<Class<?>> metaDataClasses) {
        List<String> violations = new ArrayList<>();

        // Force class loading to trigger provider registration
        forceClassLoading(metaDataClasses);

        MetaDataRegistry registry = MetaDataRegistry.getInstance();
        Set<String> registeredTypes = registry.getRegisteredTypeNames();

        // Check: All concrete MetaData classes should be registered through providers
        for (Class<?> clazz : metaDataClasses) {
            // Check if this class is registered in the registry
            boolean isRegistered = false;
            for (String typeName : registeredTypes) {
                if (!typeName.contains(".")) continue;
                String[] parts = typeName.split("\\.");
                if (parts.length != 2) continue;

                TypeDefinition def = registry.getTypeDefinition(parts[0], parts[1]);
                if (def != null && def.getImplementationClass().equals(clazz)) {
                    isRegistered = true;
                    break;
                }
            }

            if (!isRegistered) {
                violations.add("❌ CLASS NOT REGISTERED: " + clazz.getSimpleName() +
                              " (" + clazz.getName() + ") - should be registered through MetaDataTypeProvider");
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
        Set<String> registeredTypes = registry.getRegisteredTypeNames();

        // Check all registered type definitions that correspond to our scanned classes
        for (String typeName : registeredTypes) {
            if (!typeName.contains(".")) continue;
            String[] parts = typeName.split("\\.");
            if (parts.length != 2) continue;

            TypeDefinition def = registry.getTypeDefinition(parts[0], parts[1]);
            if (def != null && metaDataClasses.contains(def.getImplementationClass())) {
                if (def.hasParent()) {
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
     * Force loading of classes to trigger provider registration
     */
    private void forceClassLoading(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            try {
                // Force class loading to trigger provider registration
                Class.forName(clazz.getName(), true, clazz.getClassLoader());
            } catch (Exception e) {
                log.warn("Failed to force load class {}: {}", clazz.getName(), e.getMessage());
            }
        }

        // Small delay to allow provider registration to complete
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

        report.append("=" .repeat(60)).append("\n\n");

        // Summary
        report.append("📊 SUMMARY:\n");
        report.append("  Scanned Classes: ").append(scannedClasses.size()).append("\n");
        report.append("  Annotation Violations: ").append(annotationViolations.size()).append("\n");
        report.append("  Registration Violations: ").append(registrationViolations.size()).append("\n");
        report.append("  Inheritance Violations: ").append(inheritanceViolations.size()).append("\n\n");

        // Scanned classes
        report.append("📋 SCANNED CLASSES:\n");
        MetaDataRegistry registry = MetaDataRegistry.getInstance();
        Set<String> registeredTypes = registry.getRegisteredTypeNames();

        for (Class<?> clazz : scannedClasses.stream().sorted(Comparator.comparing(Class::getSimpleName)).collect(Collectors.toSet())) {
            // Find the type registration for this class
            String registeredAs = null;
            for (String typeName : registeredTypes) {
                if (!typeName.contains(".")) continue;
                String[] parts = typeName.split("\\.");
                if (parts.length != 2) continue;

                TypeDefinition def = registry.getTypeDefinition(parts[0], parts[1]);
                if (def != null && def.getImplementationClass().equals(clazz)) {
                    registeredAs = typeName;
                    break;
                }
            }

            if (registeredAs != null) {
                report.append("  ✅ ").append(clazz.getSimpleName())
                      .append(" -> ").append(registeredAs).append("\n");
            } else {
                report.append("  ❌ ").append(clazz.getSimpleName()).append(" -> NOT REGISTERED\n");
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