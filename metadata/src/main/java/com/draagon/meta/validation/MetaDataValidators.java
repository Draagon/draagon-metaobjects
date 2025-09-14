package com.draagon.meta.validation;

import com.draagon.meta.MetaData;
import com.draagon.meta.ValidationResult;

import java.util.HashSet;
import java.util.Set;

/**
 * Collection of standard validators for MetaData objects
 */
public class MetaDataValidators {
    
    /**
     * Validates that the MetaData has a valid type system registration
     */
    public static final Validator<MetaData> TYPE_SYSTEM_VALIDATOR = metaData -> {
        ValidationResult.Builder builder = ValidationResult.builder();
        
        // Check if type is registered in the type system
        if (!metaData.hasRegisteredType()) {
            builder.addError("Type '" + metaData.getTypeName() + "' is not registered in the type system");
            return builder.build();
        }
        
        // Validate against type definition
        metaData.getTypeDefinition().ifPresent(def -> {
            // Validate subtype
            if (!def.isSubTypeAllowed(metaData.getSubTypeName())) {
                builder.addError("SubType '" + metaData.getSubTypeName() + 
                    "' is not allowed for type '" + metaData.getTypeName() + "'");
            }
            
            // Validate children constraints
            if (!def.allowsChildren() && !metaData.getChildren().isEmpty()) {
                builder.addError("Type '" + metaData.getTypeName() + 
                    "' does not allow child elements but has " + metaData.getChildren().size() + " children");
            }
        });
        
        return builder.build();
    };
    
    /**
     * Validates that MetaData names are valid (not null/empty, proper format)
     */
    public static final Validator<MetaData> NAME_VALIDATOR = metaData -> {
        ValidationResult.Builder builder = ValidationResult.builder();
        
        String name = metaData.getName();
        if (name == null || name.trim().isEmpty()) {
            builder.addError("MetaData name cannot be null or empty");
        } else {
            // Check for invalid characters
            if (name.contains("..")) {
                builder.addError("MetaData name cannot contain consecutive dots: " + name);
            }
            
            if (name.startsWith(".") || name.endsWith(".")) {
                builder.addError("MetaData name cannot start or end with a dot: " + name);
            }
            
            // Check for reserved names
            if (name.equals("null") || name.equals("undefined")) {
                builder.addError("MetaData name cannot be a reserved word: " + name);
            }
        }
        
        return builder.build();
    };
    
    /**
     * Validates that there are no circular references in the hierarchy
     */
    public static final Validator<MetaData> CIRCULAR_REFERENCE_VALIDATOR = metaData -> {
        ValidationResult.Builder builder = ValidationResult.builder();
        
        Set<MetaData> visited = new HashSet<>();
        MetaData current = metaData;
        
        while (current != null) {
            if (!visited.add(current)) {
                builder.addError("Circular reference detected in MetaData hierarchy for: " + metaData.getName());
                break;
            }
            current = current.getSuperData();
        }
        
        return builder.build();
    };
    
    /**
     * Validates that child names are unique within a MetaData object
     */
    public static final Validator<MetaData> UNIQUE_CHILD_NAMES_VALIDATOR = metaData -> {
        ValidationResult.Builder builder = ValidationResult.builder();
        
        Set<String> childNames = new HashSet<>();
        for (MetaData child : metaData.getChildren()) {
            String childName = child.getName();
            if (!childNames.add(childName)) {
                builder.addError("Duplicate child name found: " + childName);
            }
        }
        
        return builder.build();
    };
    
    /**
     * Validates that the MetaData hierarchy is consistent
     */
    public static final Validator<MetaData> HIERARCHY_VALIDATOR = metaData -> {
        ValidationResult.Builder builder = ValidationResult.builder();
        
        // Validate parent-child relationships
        for (MetaData child : metaData.getChildren()) {
            if (child.getParent() == null) {
                builder.addError("Child '" + child.getName() + "' has no parent reference");
            } else if (child.getParent() != metaData) {
                builder.addError("Child '" + child.getName() + "' has incorrect parent reference");
            }
        }
        
        // Validate super data relationship
        MetaData superData = metaData.getSuperData();
        if (superData != null) {
            // Check for same type compatibility
            if (!superData.getTypeName().equals(metaData.getTypeName())) {
                builder.addError("Super data type '" + superData.getTypeName() + 
                    "' does not match current type '" + metaData.getTypeName() + "'");
            }
        }
        
        return builder.build();
    };
    
    /**
     * Validates that the MetaData object is in a consistent state
     */
    public static final Validator<MetaData> CONSISTENCY_VALIDATOR = metaData -> {
        ValidationResult.Builder builder = ValidationResult.builder();
        
        // Validate basic properties
        if (metaData.getTypeName() == null || metaData.getTypeName().trim().isEmpty()) {
            builder.addError("Type name cannot be null or empty");
        }
        
        if (metaData.getSubTypeName() == null || metaData.getSubTypeName().trim().isEmpty()) {
            builder.addError("SubType name cannot be null or empty");
        }
        
        // Validate package and short name consistency (handle null values)
        String fullName = metaData.getName();
        String pkg = metaData.getPackage();
        String shortName = metaData.getShortName();
        
        // Skip package validation if any of the name components are null
        if (fullName != null && pkg != null && shortName != null) {
            if (pkg.isEmpty()) {
                // No package, short name should equal full name
                if (!shortName.equals(fullName)) {
                    builder.addError("Short name '" + shortName + "' should equal full name '" + fullName + "' when no package");
                }
            } else {
                // Has package, full name should be package + separator + short name
                String expectedFullName = pkg + MetaData.PKG_SEPARATOR + shortName;
                if (!expectedFullName.equals(fullName)) {
                    builder.addError("Full name '" + fullName + "' should equal '" + expectedFullName + "'");
                }
            }
        }
        
        return builder.build();
    };
    
    /**
     * Create a comprehensive validation chain with all standard validators
     */
    public static ValidationChain<MetaData> createStandardValidationChain() {
        return ValidationChain.<MetaData>builder("StandardMetaDataValidation")
            .continueOnError()
            .addValidators(
                CONSISTENCY_VALIDATOR,
                NAME_VALIDATOR,
                TYPE_SYSTEM_VALIDATOR,
                CIRCULAR_REFERENCE_VALIDATOR,
                UNIQUE_CHILD_NAMES_VALIDATOR,
                HIERARCHY_VALIDATOR
            )
            .build();
    }
    
    /**
     * Create a basic validation chain with essential validators only
     */
    public static ValidationChain<MetaData> createBasicValidationChain() {
        return ValidationChain.<MetaData>builder("BasicMetaDataValidation")
            .stopOnFirstError()
            .addValidators(
                CONSISTENCY_VALIDATOR,
                NAME_VALIDATOR,
                CIRCULAR_REFERENCE_VALIDATOR
            )
            .build();
    }
    
    /**
     * Create a performance-focused validation chain
     */
    public static ValidationChain<MetaData> createPerformanceValidationChain() {
        return ValidationChain.<MetaData>builder("PerformanceMetaDataValidation")
            .stopOnFirstError()
            .addValidators(
                NAME_VALIDATOR,
                UNIQUE_CHILD_NAMES_VALIDATOR
            )
            .build();
    }
    
    // ========== CONVENIENCE METHODS FOR METADATA CLASS ==========
    
    /**
     * Get type system validator
     */
    public static Validator<MetaData> typeSystemValidator() {
        return TYPE_SYSTEM_VALIDATOR;
    }
    
    /**
     * Get children validator
     */
    public static Validator<MetaData> childrenValidator() {
        return UNIQUE_CHILD_NAMES_VALIDATOR;
    }
    
    /**
     * Get legacy validator (consistency + hierarchy)
     */
    public static Validator<MetaData> legacyValidator() {
        return metaData -> {
            ValidationResult consistencyResult = CONSISTENCY_VALIDATOR.validate(metaData);
            ValidationResult hierarchyResult = HIERARCHY_VALIDATOR.validate(metaData);
            
            if (consistencyResult.isValid() && hierarchyResult.isValid()) {
                return ValidationResult.success();
            }
            
            ValidationResult.Builder builder = ValidationResult.builder();
            consistencyResult.getErrors().forEach(builder::addError);
            hierarchyResult.getErrors().forEach(builder::addError);
            return builder.build();
        };
    }
}