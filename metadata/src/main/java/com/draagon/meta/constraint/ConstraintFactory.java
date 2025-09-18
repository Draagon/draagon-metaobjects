package com.draagon.meta.constraint;

import java.util.Map;

/**
 * v6.0.0: Factory interface for creating constraint instances from parameters.
 * Allows pluggable constraint types with custom creation logic.
 */
public interface ConstraintFactory {
    
    /**
     * Create a constraint instance from parameters
     * @param parameters The constraint parameters from definition
     * @return A configured constraint instance
     * @throws ConstraintCreationException If constraint creation fails
     */
    Constraint createConstraint(Map<String, Object> parameters) throws ConstraintCreationException;
    
    /**
     * Get the constraint type this factory creates
     * @return The constraint type name
     */
    String getConstraintType();
    
    /**
     * Validate the parameters for constraint creation
     * @param parameters The parameters to validate
     * @throws ConstraintCreationException If parameters are invalid
     */
    default void validateParameters(Map<String, Object> parameters) throws ConstraintCreationException {
        // Default implementation does no validation
    }
}