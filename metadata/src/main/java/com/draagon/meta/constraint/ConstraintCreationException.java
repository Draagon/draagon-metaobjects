package com.draagon.meta.constraint;

import com.draagon.meta.MetaDataException;

/**
 * v6.0.0: Exception thrown when constraint creation fails due to invalid parameters or configuration.
 * Provides detailed information about what went wrong during constraint factory instantiation.
 */
public class ConstraintCreationException extends MetaDataException {
    
    private final String constraintType;
    
    public ConstraintCreationException(String message, String constraintType) {
        super(message);
        this.constraintType = constraintType;
    }
    
    public ConstraintCreationException(String message, String constraintType, Throwable cause) {
        super(message, cause);
        this.constraintType = constraintType;
    }
    
    public String getConstraintType() {
        return constraintType;
    }
    
    @Override
    public String getMessage() {
        String baseMessage = super.getMessage();
        if (constraintType != null) {
            return baseMessage + " (constraint type: " + constraintType + ")";
        }
        return baseMessage;
    }
    
    /**
     * Factory method for missing parameter errors
     */
    public static ConstraintCreationException missingParameter(String constraintType, String parameterName) {
        String message = String.format("Required parameter '%s' is missing for constraint type '%s'", parameterName, constraintType);
        return new ConstraintCreationException(message, constraintType);
    }
    
    /**
     * Factory method for invalid parameter type errors
     */
    public static ConstraintCreationException invalidParameterType(String constraintType, String parameterName, Class<?> expectedType, Object actualValue) {
        String message = String.format("Parameter '%s' for constraint type '%s' must be of type %s, but got %s", 
            parameterName, constraintType, expectedType.getSimpleName(), 
            actualValue != null ? actualValue.getClass().getSimpleName() : "null");
        return new ConstraintCreationException(message, constraintType);
    }
    
    /**
     * Factory method for invalid parameter value errors
     */
    public static ConstraintCreationException invalidParameterValue(String constraintType, String parameterName, Object value, String reason) {
        String message = String.format("Parameter '%s' for constraint type '%s' has invalid value '%s': %s", 
            parameterName, constraintType, value, reason);
        return new ConstraintCreationException(message, constraintType);
    }
}