package com.draagon.meta.constraint;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaData;

/**
 * v6.0.0: Exception thrown when a constraint validation fails.
 * Provides detailed information about the constraint violation for debugging.
 */
public class ConstraintViolationException extends MetaDataException {
    
    private final String constraintType;
    private final Object violatingValue;
    private final ValidationContext validationContext;
    private final MetaData metaData;
    
    public ConstraintViolationException(String message, String constraintType, Object violatingValue) {
        super(message);
        this.constraintType = constraintType;
        this.violatingValue = violatingValue;
        this.validationContext = null;
        this.metaData = null;
    }
    
    public ConstraintViolationException(String message, String constraintType, Object violatingValue, ValidationContext context) {
        super(message);
        this.constraintType = constraintType;
        this.violatingValue = violatingValue;
        this.validationContext = context;
        this.metaData = context != null ? context.getParentMetaData().orElse(null) : null;
    }
    
    public ConstraintViolationException(String message, String constraintType, Object violatingValue, Throwable cause) {
        super(message, cause);
        this.constraintType = constraintType;
        this.violatingValue = violatingValue;
        this.validationContext = null;
        this.metaData = null;
    }
    
    public ConstraintViolationException(String message, String constraintType, Object violatingValue, ValidationContext context, Throwable cause) {
        super(message, cause);
        this.constraintType = constraintType;
        this.violatingValue = violatingValue;
        this.validationContext = context;
        this.metaData = context != null ? context.getParentMetaData().orElse(null) : null;
    }

    /**
     * Constructor for constraint violations with explicit MetaData reference.
     * This is commonly used by concrete constraint implementations.
     *
     * @param message Descriptive error message
     * @param constraintName Unique constraint identifier
     * @param metaData The MetaData object that failed validation
     */
    public ConstraintViolationException(String message, String constraintName, MetaData metaData) {
        super(message);
        this.constraintType = constraintName;
        this.violatingValue = null;
        this.validationContext = null;
        this.metaData = metaData;
    }
    
    public String getConstraintType() {
        return constraintType;
    }
    
    public Object getViolatingValue() {
        return violatingValue;
    }
    
    public ValidationContext getValidationContext() {
        return validationContext;
    }

    /**
     * Get the MetaData object that failed validation.
     *
     * @return The MetaData object, or null if not available
     */
    public MetaData getMetaData() {
        return metaData;
    }
    
    @Override
    public String getMessage() {
        StringBuilder msg = new StringBuilder(super.getMessage());
        
        if (constraintType != null) {
            msg.append(" (constraint: ").append(constraintType).append(")");
        }
        
        if (violatingValue != null) {
            msg.append(" (value: ").append(violatingValue).append(")");
        }
        
        if (validationContext != null && validationContext.getOperation().isPresent()) {
            msg.append(" (operation: ").append(validationContext.getOperation().get()).append(")");
        }
        
        return msg.toString();
    }
    
    /**
     * Factory method for creating constraint violations with context
     */
    public static ConstraintViolationException forConstraint(String constraintType, String message, Object value, ValidationContext context) {
        return new ConstraintViolationException(message, constraintType, value, context);
    }
    
    /**
     * Factory method for required field violations
     */
    public static ConstraintViolationException requiredField(String fieldName, MetaData metaData) {
        String message = String.format("Required field '%s' is missing or null in %s", fieldName, metaData.getName());
        ValidationContext context = ValidationContext.builder()
            .operation("validation")
            .parentMetaData(metaData)
            .fieldName(fieldName)
            .build();
        return new ConstraintViolationException(message, "required", null, context);
    }
    
    /**
     * Factory method for pattern violations
     */
    public static ConstraintViolationException patternMismatch(String pattern, Object value, ValidationContext context) {
        String message = String.format("Value '%s' does not match required pattern: %s", value, pattern);
        return new ConstraintViolationException(message, "pattern", value, context);
    }
    
    /**
     * Factory method for length violations
     */
    public static ConstraintViolationException lengthViolation(int minLength, int maxLength, int actualLength, Object value, ValidationContext context) {
        String message;
        if (minLength > 0 && maxLength > 0) {
            message = String.format("Value length %d is not between %d and %d (value: '%s')", actualLength, minLength, maxLength, value);
        } else if (minLength > 0) {
            message = String.format("Value length %d is less than minimum %d (value: '%s')", actualLength, minLength, value);
        } else {
            message = String.format("Value length %d exceeds maximum %d (value: '%s')", actualLength, maxLength, value);
        }
        return new ConstraintViolationException(message, "length", value, context);
    }
}