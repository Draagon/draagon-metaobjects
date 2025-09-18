package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;
import java.util.*;
import java.util.stream.Collectors;

/**
 * v6.0.0: Factory for creating enumeration constraints.
 * Validates that values are from a predefined set of allowed values.
 */
public class EnumConstraintFactory implements ConstraintFactory {
    
    @Override
    public Constraint createConstraint(Map<String, Object> parameters) throws ConstraintCreationException {
        validateParameters(parameters);
        
        Object valuesParam = parameters.get("values");
        Boolean caseSensitive = getBooleanParameter(parameters, "caseSensitive", true);
        
        Set<String> allowedValues = parseAllowedValues(valuesParam, caseSensitive);
        
        return new EnumConstraint(allowedValues, caseSensitive);
    }
    
    @Override
    public String getConstraintType() {
        return "enum";
    }
    
    @Override
    public void validateParameters(Map<String, Object> parameters) throws ConstraintCreationException {
        if (!parameters.containsKey("values")) {
            throw ConstraintCreationException.missingParameter("enum", "values");
        }
        
        Object values = parameters.get("values");
        if (values == null) {
            throw ConstraintCreationException.invalidParameterValue("enum", "values", values, "values cannot be null");
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
            throw ConstraintCreationException.invalidParameterType("enum", name, Boolean.class, value);
        }
    }
    
    @SuppressWarnings("unchecked")
    private Set<String> parseAllowedValues(Object valuesParam, boolean caseSensitive) throws ConstraintCreationException {
        Set<String> allowedValues = new HashSet<>();
        
        if (valuesParam instanceof List) {
            List<?> valuesList = (List<?>) valuesParam;
            for (Object value : valuesList) {
                if (value != null) {
                    String stringValue = value.toString();
                    allowedValues.add(caseSensitive ? stringValue : stringValue.toLowerCase());
                }
            }
        } else if (valuesParam instanceof String) {
            // Support comma-separated values
            String[] values = ((String) valuesParam).split(",");
            for (String value : values) {
                String trimmedValue = value.trim();
                if (!trimmedValue.isEmpty()) {
                    allowedValues.add(caseSensitive ? trimmedValue : trimmedValue.toLowerCase());
                }
            }
        } else {
            throw ConstraintCreationException.invalidParameterType("enum", "values", List.class, valuesParam);
        }
        
        if (allowedValues.isEmpty()) {
            throw ConstraintCreationException.invalidParameterValue("enum", "values", valuesParam, "at least one allowed value must be specified");
        }
        
        return allowedValues;
    }
    
    /**
     * Constraint implementation for enumeration validation
     */
    private static class EnumConstraint implements Constraint {
        private final Set<String> allowedValues;
        private final boolean caseSensitive;
        
        public EnumConstraint(Set<String> allowedValues, boolean caseSensitive) {
            this.allowedValues = Collections.unmodifiableSet(new HashSet<>(allowedValues));
            this.caseSensitive = caseSensitive;
        }
        
        @Override
        public void validate(MetaData metaData, Object value, ValidationContext context) throws ConstraintViolationException {
            if (value == null) {
                return; // Enum constraints don't validate null values - use required constraint for that
            }
            
            String stringValue = value.toString();
            String compareValue = caseSensitive ? stringValue : stringValue.toLowerCase();
            
            if (!allowedValues.contains(compareValue)) {
                String allowedValuesStr = allowedValues.stream()
                    .sorted()
                    .collect(Collectors.joining(", "));
                    
                throw ConstraintViolationException.forConstraint("enum",
                    String.format("Value '%s' is not one of the allowed values: [%s]", stringValue, allowedValuesStr), 
                    value, context);
            }
        }
        
        @Override
        public String getType() {
            return "enum";
        }
        
        @Override
        public String getDescription() {
            String allowedValuesStr = allowedValues.stream()
                .sorted()
                .collect(Collectors.joining(", "));
            String sensitivity = caseSensitive ? "case-sensitive" : "case-insensitive";
            return String.format("Validates that value is one of: [%s] (%s)", allowedValuesStr, sensitivity);
        }
    }
}