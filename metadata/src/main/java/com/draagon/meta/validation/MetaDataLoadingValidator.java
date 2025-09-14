package com.draagon.meta.validation;

import com.draagon.meta.MetaData;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.util.MetaDataCasting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive validation for MetaData loading process.
 * Performs multi-phase validation including structural integrity,
 * reference validation, semantic validation, and performance characteristics.
 */
public class MetaDataLoadingValidator {
    
    private static final Logger log = LoggerFactory.getLogger(MetaDataLoadingValidator.class);
    
    // Performance thresholds
    private static final int MAX_TOTAL_FIELDS_WARNING = 10000;
    private static final int MAX_HIERARCHY_DEPTH_WARNING = 10;
    private static final int MAX_CHILDREN_PER_NODE_WARNING = 100;
    
    /**
     * Validation report containing all validation results
     */
    public static class ValidationReport {
        private final boolean valid;
        private final List<ValidationIssue> errors;
        private final List<ValidationIssue> warnings;
        private final Map<String, Object> metrics;
        private final long validationTime;
        
        private ValidationReport(Builder builder) {
            this.valid = builder.errors.isEmpty();
            this.errors = Collections.unmodifiableList(new ArrayList<>(builder.errors));
            this.warnings = Collections.unmodifiableList(new ArrayList<>(builder.warnings));
            this.metrics = Collections.unmodifiableMap(new HashMap<>(builder.metrics));
            this.validationTime = builder.validationTime;
        }
        
        public boolean isValid() { 
            return valid; 
        }
        
        public List<ValidationIssue> getErrors() { 
            return errors; 
        }
        
        public List<ValidationIssue> getWarnings() { 
            return warnings; 
        }
        
        public Map<String, Object> getMetrics() { 
            return metrics; 
        }
        
        public long getValidationTime() { 
            return validationTime; 
        }
        
        public void throwIfInvalid() throws MetaDataValidationException {
            if (!valid) {
                throw new MetaDataValidationException("Validation failed", errors);
            }
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private final List<ValidationIssue> errors = new ArrayList<>();
            private final List<ValidationIssue> warnings = new ArrayList<>();
            private final Map<String, Object> metrics = new HashMap<>();
            private long validationTime = 0;
            
            public Builder addError(String message, String metaDataPath, String component) {
                errors.add(new ValidationIssue(ValidationIssue.Severity.ERROR, message, metaDataPath, component, null));
                return this;
            }
            
            public Builder addError(String message, String metaDataPath, String component, Exception cause) {
                errors.add(new ValidationIssue(ValidationIssue.Severity.ERROR, message, metaDataPath, component, cause));
                return this;
            }
            
            public Builder addWarning(String message, String metaDataPath, String component) {
                warnings.add(new ValidationIssue(ValidationIssue.Severity.WARNING, message, metaDataPath, component, null));
                return this;
            }
            
            public Builder addMetric(String key, Object value) {
                metrics.put(key, value);
                return this;
            }
            
            public Builder setValidationTime(long time) {
                this.validationTime = time;
                return this;
            }
            
            public ValidationReport build() {
                return new ValidationReport(this);
            }
        }
    }
    
    /**
     * Individual validation issue
     */
    public static class ValidationIssue {
        public enum Severity { ERROR, WARNING, INFO }
        
        private final Severity severity;
        private final String message;
        private final String metaDataPath;
        private final String component;
        private final Exception cause;
        
        public ValidationIssue(Severity severity, String message, String metaDataPath, 
                              String component, Exception cause) {
            this.severity = severity;
            this.message = message;
            this.metaDataPath = metaDataPath;
            this.component = component;
            this.cause = cause;
        }
        
        public Severity getSeverity() { return severity; }
        public String getMessage() { return message; }
        public String getMetaDataPath() { return metaDataPath; }
        public String getComponent() { return component; }
        public Exception getCause() { return cause; }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(severity).append("] ");
            sb.append(message);
            if (metaDataPath != null) {
                sb.append(" at ").append(metaDataPath);
            }
            if (component != null) {
                sb.append(" (").append(component).append(")");
            }
            if (cause != null) {
                sb.append(" - Caused by: ").append(cause.getMessage());
            }
            return sb.toString();
        }
    }
    
    /**
     * Exception thrown when validation fails
     */
    public static class MetaDataValidationException extends RuntimeException {
        private final List<ValidationIssue> issues;
        
        public MetaDataValidationException(String message, List<ValidationIssue> issues) {
            super(buildMessage(message, issues));
            this.issues = Collections.unmodifiableList(new ArrayList<>(issues));
        }
        
        public List<ValidationIssue> getIssues() {
            return issues;
        }
        
        private static String buildMessage(String message, List<ValidationIssue> issues) {
            StringBuilder sb = new StringBuilder(message);
            sb.append("\n--- Validation Issues ---");
            for (ValidationIssue issue : issues) {
                sb.append("\n").append(issue);
            }
            return sb.toString();
        }
    }
    
    /**
     * Perform complete validation of a loaded MetaDataLoader
     */
    public ValidationReport validateComplete(MetaDataLoader loader) {
        long startTime = System.currentTimeMillis();
        ValidationReport.Builder builder = ValidationReport.builder();
        
        try {
            log.debug("Starting comprehensive validation for loader: {}", loader.getName());
            
            // Phase 1: Structural validation
            validateStructuralIntegrity(loader, builder);
            
            // Phase 2: Reference validation
            validateReferences(loader, builder);
            
            // Phase 3: Semantic validation
            validateSemantics(loader, builder);
            
            // Phase 4: Performance validation
            validatePerformanceCharacteristics(loader, builder);
            
            long validationTime = System.currentTimeMillis() - startTime;
            builder.setValidationTime(validationTime);
            builder.addMetric("validationTimeMs", validationTime);
            
            log.debug("Validation completed in {}ms", validationTime);
            
        } catch (Exception e) {
            builder.addError("Validation process failed: " + e.getMessage(), 
                           buildPath(loader), "validation-framework", e);
        }
        
        return builder.build();
    }
    
    /**
     * Phase 1: Validate structural integrity
     */
    private void validateStructuralIntegrity(MetaDataLoader loader, ValidationReport.Builder builder) {
        // Validate hierarchy consistency
        validateHierarchy(loader, builder);
        
        // Validate naming conventions
        validateNamingConventions(loader, builder);
        
        // Validate required components
        validateRequiredComponents(loader, builder);
        
        // Check for circular references
        validateNoCircularReferences(loader, builder);
    }
    
    /**
     * Validate parent-child hierarchy consistency
     */
    private void validateHierarchy(MetaData metaData, ValidationReport.Builder builder) {
        Set<MetaData> visited = new HashSet<>();
        validateHierarchyRecursive(metaData, builder, visited, 0);
    }
    
    private void validateHierarchyRecursive(MetaData metaData, ValidationReport.Builder builder, 
                                          Set<MetaData> visited, int depth) {
        if (metaData == null) return;
        
        if (visited.contains(metaData)) {
            builder.addError("Circular reference detected in hierarchy", 
                           buildPath(metaData), "hierarchy");
            return;
        }
        
        visited.add(metaData);
        
        if (depth > MAX_HIERARCHY_DEPTH_WARNING) {
            builder.addWarning("Deep hierarchy detected (depth: " + depth + ")", 
                             buildPath(metaData), "hierarchy");
        }
        
        for (MetaData child : metaData.getChildren()) {
            // Validate parent reference consistency
            MetaData parent = child.getParent();
            if (parent != null && parent != metaData) {
                builder.addError("Inconsistent parent reference", 
                               buildPath(child), "hierarchy");
            }
            
            validateHierarchyRecursive(child, builder, visited, depth + 1);
        }
        
        visited.remove(metaData);
    }
    
    /**
     * Validate naming conventions
     */
    private void validateNamingConventions(MetaDataLoader loader, ValidationReport.Builder builder) {
        for (MetaData metaData : loader.getChildren()) {
            validateNaming(metaData, builder);
        }
    }
    
    private void validateNaming(MetaData metaData, ValidationReport.Builder builder) {
        String name = metaData.getName();
        String typeName = metaData.getTypeName();
        
        if (name == null || name.trim().isEmpty()) {
            builder.addError("MetaData has null or empty name", 
                           buildPath(metaData), "naming");
        }
        
        if (typeName == null || typeName.trim().isEmpty()) {
            builder.addError("MetaData has null or empty type", 
                           buildPath(metaData), "naming");
        }
        
        // Recursively validate children
        for (MetaData child : metaData.getChildren()) {
            validateNaming(child, builder);
        }
    }
    
    /**
     * Validate required components are present
     */
    private void validateRequiredComponents(MetaDataLoader loader, ValidationReport.Builder builder) {
        // Check that loader has at least some metadata
        if (loader.getChildren().isEmpty()) {
            builder.addWarning("MetaDataLoader has no children", 
                             buildPath(loader), "components");
        }
        
        // Validate MetaObjects have fields
        for (MetaObject metaObject : loader.getMetaObjects()) {
            List<MetaField> fields = metaObject.findChildren(MetaField.class).collect(Collectors.toList());
            if (fields.isEmpty()) {
                builder.addWarning("MetaObject has no fields: " + metaObject.getName(), 
                                 buildPath(metaObject), "components");
            }
        }
    }
    
    /**
     * Validate no circular references exist
     */
    private void validateNoCircularReferences(MetaDataLoader loader, ValidationReport.Builder builder) {
        Set<MetaData> visiting = new HashSet<>();
        Set<MetaData> visited = new HashSet<>();
        
        for (MetaData root : loader.getChildren()) {
            if (!visited.contains(root)) {
                checkCircularReferences(root, visiting, visited, builder);
            }
        }
    }
    
    private void checkCircularReferences(MetaData node, Set<MetaData> visiting, 
                                       Set<MetaData> visited, ValidationReport.Builder builder) {
        if (visiting.contains(node)) {
            builder.addError("Circular reference detected", 
                           buildPath(node), "circular-reference");
            return;
        }
        
        if (visited.contains(node)) {
            return;
        }
        
        visiting.add(node);
        
        for (MetaData child : node.getChildren()) {
            checkCircularReferences(child, visiting, visited, builder);
        }
        
        visiting.remove(node);
        visited.add(node);
    }
    
    /**
     * Phase 2: Validate references
     */
    private void validateReferences(MetaDataLoader loader, ValidationReport.Builder builder) {
        // Validate class references
        validateClassReferences(loader, builder);
        
        // Validate field type references
        validateFieldTypeReferences(loader, builder);
    }
    
    private void validateClassReferences(MetaDataLoader loader, ValidationReport.Builder builder) {
        for (MetaObject metaObject : loader.getMetaObjects()) {
            try {
                Class<?> objectClass = metaObject.getObjectClass();
                if (objectClass != null) {
                    validateObjectClass(metaObject, objectClass, builder);
                }
            } catch (ClassNotFoundException e) {
                builder.addError("Object class not found for " + metaObject.getName(), 
                               buildPath(metaObject), "class-loading", e);
            }
        }
    }
    
    private void validateObjectClass(MetaObject metaObject, Class<?> objectClass, 
                                   ValidationReport.Builder builder) {
        // Validate that the class is accessible
        if (objectClass.isInterface()) {
            builder.addWarning("Object class is interface: " + objectClass.getName(), 
                             buildPath(metaObject), "class-validation");
        }
        
        // Check for default constructor if needed
        try {
            objectClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            builder.addWarning("Object class has no default constructor: " + objectClass.getName(), 
                             buildPath(metaObject), "class-validation");
        }
    }
    
    private void validateFieldTypeReferences(MetaDataLoader loader, ValidationReport.Builder builder) {
        for (MetaObject metaObject : loader.getMetaObjects()) {
            for (MetaData child : metaObject.getChildren()) {
                if (child instanceof MetaField) {
                    MetaField field = (MetaField) child;
                    validateFieldType(field, builder);
                }
            }
        }
    }
    
    private void validateFieldType(MetaField field, ValidationReport.Builder builder) {
        try {
            // Validate that field type is consistent
            if (field.getDataType() == null) {
                builder.addError("MetaField has null data type: " + field.getName(), 
                               buildPath(field), "field-validation");
            }
        } catch (Exception e) {
            builder.addError("Error validating field type: " + e.getMessage(), 
                           buildPath(field), "field-validation", e);
        }
    }
    
    /**
     * Phase 3: Validate semantics
     */
    private void validateSemantics(MetaDataLoader loader, ValidationReport.Builder builder) {
        // Validate business rules
        validateBusinessRules(loader, builder);
        
        // Validate default values
        validateDefaultValues(loader, builder);
    }
    
    private void validateBusinessRules(MetaDataLoader loader, ValidationReport.Builder builder) {
        // Check for duplicate names within the same parent
        validateUniqueNames(loader, builder);
    }
    
    private void validateUniqueNames(MetaData parent, ValidationReport.Builder builder) {
        Map<String, List<MetaData>> nameGroups = parent.getChildren().stream()
            .collect(Collectors.groupingBy(MetaData::getName));
        
        for (Map.Entry<String, List<MetaData>> entry : nameGroups.entrySet()) {
            if (entry.getValue().size() > 1) {
                builder.addError("Duplicate child names: " + entry.getKey(), 
                               buildPath(parent), "uniqueness");
            }
        }
        
        // Recursively check children
        for (MetaData child : parent.getChildren()) {
            validateUniqueNames(child, builder);
        }
    }
    
    private void validateDefaultValues(MetaDataLoader loader, ValidationReport.Builder builder) {
        for (MetaObject metaObject : loader.getMetaObjects()) {
            for (MetaData child : metaObject.getChildren()) {
                if (child instanceof MetaField) {
                    MetaField field = (MetaField) child;
                    validateFieldDefaultValue(field, builder);
                }
            }
        }
    }
    
    private void validateFieldDefaultValue(MetaField field, ValidationReport.Builder builder) {
        try {
            Object defaultValue = field.getDefaultValue();
            if (defaultValue != null) {
                // Validate that default value is compatible with field type
                // This is a basic check - more sophisticated validation could be added
                if (field.getDataType() != null) {
                    // Type compatibility check would go here
                }
            }
        } catch (Exception e) {
            builder.addError("Error validating default value: " + e.getMessage(), 
                           buildPath(field), "default-value", e);
        }
    }
    
    /**
     * Phase 4: Validate performance characteristics
     */
    private void validatePerformanceCharacteristics(MetaDataLoader loader, ValidationReport.Builder builder) {
        // Check for performance anti-patterns
        int totalFields = countTotalFields(loader);
        builder.addMetric("totalFields", totalFields);
        
        if (totalFields > MAX_TOTAL_FIELDS_WARNING) {
            builder.addWarning("Large number of fields (" + totalFields + ") may impact performance", 
                             buildPath(loader), "performance");
        }
        
        // Check for nodes with too many children
        validateChildrenCount(loader, builder);
        
        // Check hierarchy depth
        int maxDepth = calculateMaxDepth(loader);
        builder.addMetric("maxHierarchyDepth", maxDepth);
        
        if (maxDepth > MAX_HIERARCHY_DEPTH_WARNING) {
            builder.addWarning("Deep hierarchy (depth: " + maxDepth + ") may impact performance", 
                             buildPath(loader), "performance");
        }
    }
    
    private int countTotalFields(MetaDataLoader loader) {
        return loader.getMetaObjects().stream()
            .mapToInt(mo -> (int) mo.findChildren(MetaField.class).count())
            .sum();
    }
    
    private void validateChildrenCount(MetaData metaData, ValidationReport.Builder builder) {
        int childCount = metaData.getChildren().size();
        if (childCount > MAX_CHILDREN_PER_NODE_WARNING) {
            builder.addWarning("Node has many children (" + childCount + ") which may impact performance", 
                             buildPath(metaData), "performance");
        }
        
        // Recursively check children
        for (MetaData child : metaData.getChildren()) {
            validateChildrenCount(child, builder);
        }
    }
    
    private int calculateMaxDepth(MetaData metaData) {
        if (metaData.getChildren().isEmpty()) {
            return 1;
        }
        
        return 1 + metaData.getChildren().stream()
            .mapToInt(this::calculateMaxDepth)
            .max()
            .orElse(0);
    }
    
    /**
     * Build a human-readable path for MetaData
     */
    private String buildPath(MetaData metaData) {
        return MetaDataCasting.buildMetaDataPath(metaData);
    }
}