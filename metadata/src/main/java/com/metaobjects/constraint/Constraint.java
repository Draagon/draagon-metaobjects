package com.metaobjects.constraint;

import com.metaobjects.MetaData;

/**
 * v6.0.0: Interface for constraint implementations that can validate metadata structure.
 * Constraints are checked during metadata construction (addChild/setAttribute) to ensure
 * metadata structure integrity.
 */
public interface Constraint {
    
    /**
     * Validate a metadata value against this constraint
     * @param metaData The metadata object being validated
     * @param value The value being validated (can be null)
     * @throws ConstraintViolationException If the constraint is violated
     */
    void validate(MetaData metaData, Object value) throws ConstraintViolationException;
    
    /**
     * Get the constraint type name
     * @return The constraint type identifier
     */
    String getType();
    
    /**
     * Get a human-readable description of this constraint
     * @return Description of what this constraint validates
     */
    String getDescription();

    /**
     * Get the constraint ID
     * @return Unique constraint identifier
     */
    String getConstraintId();

    /**
     * Check if this constraint is applicable to the given metadata type
     * @param metaDataType The metadata type to check
     * @return True if this constraint can be applied to the metadata type
     */
    default boolean isApplicableTo(String metaDataType) {
        return true; // By default, constraints apply to all metadata types
    }
}