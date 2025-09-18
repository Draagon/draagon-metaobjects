package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;
import java.util.Map;

/**
 * v6.0.0: Factory for creating string length constraints.
 * Validates that string values are within specified length bounds.
 */
public class LengthConstraintFactory implements ConstraintFactory {
    
    @Override
    public Constraint createConstraint(Map<String, Object> parameters) throws ConstraintCreationException {
        validateParameters(parameters);
        
        Integer minLength = getIntegerParameter(parameters, "min", 0);
        Integer maxLength = getIntegerParameter(parameters, "max", null);
        
        if (minLength < 0) {
            throw ConstraintCreationException.invalidParameterValue("length", "min", minLength, "minimum length cannot be negative");
        }
        
        if (maxLength != null && maxLength < 0) {
            throw ConstraintCreationException.invalidParameterValue("length", "max", maxLength, "maximum length cannot be negative");
        }
        
        if (maxLength != null && minLength > maxLength) {
            throw ConstraintCreationException.invalidParameterValue("length", "max", maxLength, "maximum length cannot be less than minimum length");
        }
        
        return new LengthConstraint(minLength, maxLength);
    }
    
    @Override
    public String getConstraintType() {
        return "length";
    }
    
    @Override
    public void validateParameters(Map<String, Object> parameters) throws ConstraintCreationException {
        // At least one of min or max must be specified
        if (!parameters.containsKey("min") && !parameters.containsKey("max")) {
            throw new ConstraintCreationException("At least one of 'min' or 'max' parameters must be specified", "length");
        }
    }
    
    private Integer getIntegerParameter(Map<String, Object> parameters, String name, Integer defaultValue) throws ConstraintCreationException {
        if (!parameters.containsKey(name)) {
            return defaultValue;
        }
        
        Object value = parameters.get(name);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                throw ConstraintCreationException.invalidParameterValue("length", name, value, "not a valid integer");
            }
        } else {
            throw ConstraintCreationException.invalidParameterType("length", name, Integer.class, value);
        }
    }
    
    /**
     * Constraint implementation for string length validation
     */
    private static class LengthConstraint implements Constraint {
        private final int minLength;
        private final Integer maxLength;
        
        public LengthConstraint(int minLength, Integer maxLength) {
            this.minLength = minLength;
            this.maxLength = maxLength;
        }
        
        @Override
        public void validate(MetaData metaData, Object value, ValidationContext context) throws ConstraintViolationException {
            if (value == null) {
                return; // Length constraints don't validate null values - use required constraint for that
            }
            
            String stringValue = value.toString();
            int actualLength = stringValue.length();
            
            if (actualLength < minLength) {
                throw ConstraintViolationException.lengthViolation(minLength, maxLength != null ? maxLength : -1, actualLength, value, context);
            }
            
            if (maxLength != null && actualLength > maxLength) {
                throw ConstraintViolationException.lengthViolation(minLength, maxLength, actualLength, value, context);
            }
        }
        
        @Override
        public String getType() {
            return "length";
        }
        
        @Override
        public String getDescription() {
            if (maxLength != null) {
                return String.format("Validates that string length is between %d and %d characters", minLength, maxLength);
            } else {
                return String.format("Validates that string length is at least %d characters", minLength);
            }
        }
    }
}