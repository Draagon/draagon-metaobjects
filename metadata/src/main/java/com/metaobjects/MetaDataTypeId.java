package com.metaobjects;

import java.io.Serializable;
import java.util.Objects;

/**
 * Immutable record representing a MetaData type and subtype combination.
 * This provides a clean separation between the type concept ("field") and 
 * subtype implementation ("int", "string", "currency", etc.).
 * 
 * <p>Examples:</p>
 * <ul>
 *   <li>field.int - Integer field type</li>
 *   <li>field.string - String field type</li>
 *   <li>field.currency - Currency field type (future extension)</li>
 *   <li>view.text - Text view type</li>
 *   <li>validator.required - Required validator type</li>
 *   <li>object.account - Account object type (future extension)</li>
 * </ul>
 * 
 * @param type The primary type (e.g., "field", "view", "validator", "object")
 * @param subType The specific implementation subtype (e.g., "int", "string", "currency")
 * 
 * @since 6.0.0
 */
public record MetaDataTypeId(String type, String subType) implements Serializable {
    
    /**
     * Create a MetaDataTypeId with validation
     */
    public MetaDataTypeId {
        Objects.requireNonNull(type, "Type cannot be null");
        Objects.requireNonNull(subType, "SubType cannot be null");
        
        if (type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type cannot be empty");
        }
        if (subType.trim().isEmpty()) {
            throw new IllegalArgumentException("SubType cannot be empty");
        }
        
        // Normalize to lowercase for consistency
        type = type.trim().toLowerCase();
        subType = subType.trim().toLowerCase();
    }
    
    /**
     * Returns the fully qualified type name in the format "type.subType"
     * 
     * @return Qualified name like "field.int" or "view.text"
     */
    public String toQualifiedName() {
        return type + "." + subType;
    }
    
    /**
     * Check if this type matches a pattern where "*" means any subtype
     * 
     * @param pattern Pattern like "field.*" or "field.int" 
     * @return true if this type matches the pattern
     */
    public boolean matches(String pattern) {
        if (pattern == null) return false;
        
        String[] parts = pattern.split("\\.");
        if (parts.length != 2) return false;
        
        String patternType = parts[0].trim().toLowerCase();
        String patternSubType = parts[1].trim().toLowerCase();
        
        boolean typeMatches = "*".equals(patternType) || type.equals(patternType);
        boolean subTypeMatches = "*".equals(patternSubType) || subType.equals(patternSubType);
        
        return typeMatches && subTypeMatches;
    }
    
    /**
     * Check if this type matches another MetaDataTypeId pattern
     * 
     * @param pattern Pattern where type or subType can be "*"
     * @return true if this type matches the pattern
     */
    public boolean matches(MetaDataTypeId pattern) {
        if (pattern == null) return false;
        
        boolean typeMatches = "*".equals(pattern.type()) || type.equals(pattern.type());
        boolean subTypeMatches = "*".equals(pattern.subType()) || subType.equals(pattern.subType());
        
        return typeMatches && subTypeMatches;
    }
    
    /**
     * Create a pattern MetaDataTypeId for matching (using "*" wildcards)
     * 
     * @param type Type or "*" for any
     * @param subType SubType or "*" for any  
     * @return Pattern MetaDataTypeId for matching
     */
    public static MetaDataTypeId pattern(String type, String subType) {
        // Allow "*" wildcards without validation for pattern matching
        Objects.requireNonNull(type, "Type cannot be null");
        Objects.requireNonNull(subType, "SubType cannot be null");
        
        return new MetaDataTypeId(type.trim().toLowerCase(), subType.trim().toLowerCase());
    }
    
    /**
     * Create MetaDataTypeId from qualified name like "field.int"
     * 
     * @param qualifiedName Name in format "type.subType"
     * @return MetaDataTypeId instance
     * @throws IllegalArgumentException if format is invalid
     */
    public static MetaDataTypeId fromQualifiedName(String qualifiedName) {
        Objects.requireNonNull(qualifiedName, "Qualified name cannot be null");
        
        String[] parts = qualifiedName.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                "Invalid qualified name format. Expected 'type.subType', got: " + qualifiedName
            );
        }
        
        return new MetaDataTypeId(parts[0], parts[1]);
    }
    
    @Override
    public String toString() {
        return toQualifiedName();
    }
}