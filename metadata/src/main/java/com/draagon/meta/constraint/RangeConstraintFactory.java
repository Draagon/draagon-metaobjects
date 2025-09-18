package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;
import java.util.Map;

/**
 * v6.0.0: Factory for creating numeric range constraints.
 * Validates that numeric values are within specified bounds.
 */
public class RangeConstraintFactory implements ConstraintFactory {
    
    @Override
    public Constraint createConstraint(Map<String, Object> parameters) throws ConstraintCreationException {
        validateParameters(parameters);
        
        Double minValue = getDoubleParameter(parameters, "min", null);
        Double maxValue = getDoubleParameter(parameters, "max", null);
        Boolean inclusive = getBooleanParameter(parameters, "inclusive", true);
        
        if (minValue != null && maxValue != null && minValue > maxValue) {
            throw ConstraintCreationException.invalidParameterValue("range", "max", maxValue, "maximum value cannot be less than minimum value");
        }
        
        return new RangeConstraint(minValue, maxValue, inclusive);
    }
    
    @Override
    public String getConstraintType() {
        return "range";
    }
    
    @Override
    public void validateParameters(Map<String, Object> parameters) throws ConstraintCreationException {
        // At least one of min or max must be specified
        if (!parameters.containsKey("min") && !parameters.containsKey("max")) {
            throw new ConstraintCreationException("At least one of 'min' or 'max' parameters must be specified", "range");
        }
    }
    
    private Double getDoubleParameter(Map<String, Object> parameters, String name, Double defaultValue) throws ConstraintCreationException {
        if (!parameters.containsKey(name)) {
            return defaultValue;
        }
        
        Object value = parameters.get(name);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                throw ConstraintCreationException.invalidParameterValue("range", name, value, "not a valid number");
            }
        } else {
            throw ConstraintCreationException.invalidParameterType("range", name, Number.class, value);
        }
    }
    
    private Boolean getBooleanParameter(Map<String, Object> parameters, String name, Boolean defaultValue) throws ConstraintCreationException {
        if (!parameters.containsKey(name)) {
            return defaultValue;
        }
        
        Object value = parameters.get(name);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        } else {
            throw ConstraintCreationException.invalidParameterType("range", name, Boolean.class, value);
        }
    }
    
    /**
     * Constraint implementation for numeric range validation
     */
    private static class RangeConstraint implements Constraint {
        private final Double minValue;
        private final Double maxValue;
        private final boolean inclusive;
        
        public RangeConstraint(Double minValue, Double maxValue, boolean inclusive) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.inclusive = inclusive;
        }
        
        @Override
        public void validate(MetaData metaData, Object value, ValidationContext context) throws ConstraintViolationException {
            if (value == null) {
                return; // Range constraints don't validate null values - use required constraint for that
            }
            
            Double numericValue = convertToDouble(value);
            if (numericValue == null) {
                throw ConstraintViolationException.forConstraint("range", 
                    "Value must be numeric for range validation: " + value, value, context);
            }
            
            if (minValue != null) {
                if (inclusive ? numericValue < minValue : numericValue <= minValue) {
                    String operator = inclusive ? ">=" : ">";
                    throw ConstraintViolationException.forConstraint("range",
                        String.format("Value %s must be %s %s", numericValue, operator, minValue), value, context);
                }
            }
            
            if (maxValue != null) {
                if (inclusive ? numericValue > maxValue : numericValue >= maxValue) {
                    String operator = inclusive ? "<=" : "<";
                    throw ConstraintViolationException.forConstraint("range",
                        String.format("Value %s must be %s %s", numericValue, operator, maxValue), value, context);
                }
            }
        }
        
        private Double convertToDouble(Object value) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof String) {
                try {
                    return Double.parseDouble((String) value);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }
        
        @Override
        public String getType() {
            return "range";
        }
        
        @Override
        public String getDescription() {
            String bounds = inclusive ? "inclusive" : "exclusive";
            if (minValue != null && maxValue != null) {
                return String.format("Validates that numeric value is between %s and %s (%s)", minValue, maxValue, bounds);
            } else if (minValue != null) {
                return String.format("Validates that numeric value is %s %s (%s)", inclusive ? ">=" : ">", minValue, bounds);
            } else {
                return String.format("Validates that numeric value is %s %s (%s)", inclusive ? "<=" : "<", maxValue, bounds);
            }
        }
    }
}