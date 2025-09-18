package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;
import java.util.Map;

/**
 * v6.0.0: Factory for creating required field constraints.
 * Validates that required metadata fields are not null or empty.
 */
public class RequiredConstraintFactory implements ConstraintFactory {
    
    @Override
    public Constraint createConstraint(Map<String, Object> parameters) throws ConstraintCreationException {
        return new RequiredConstraint();
    }
    
    @Override
    public String getConstraintType() {
        return "required";
    }
    
    /**
     * Constraint implementation for required fields
     */
    private static class RequiredConstraint implements Constraint {
        
        @Override
        public void validate(MetaData metaData, Object value, ValidationContext context) throws ConstraintViolationException {
            if (value == null) {
                throw ConstraintViolationException.requiredField(
                    context.getFieldName().orElse("unknown"), metaData);
            }
            
            // Check for empty strings
            if (value instanceof String && ((String) value).trim().isEmpty()) {
                throw ConstraintViolationException.requiredField(
                    context.getFieldName().orElse("unknown"), metaData);
            }
        }
        
        @Override
        public String getType() {
            return "required";
        }
        
        @Override
        public String getDescription() {
            return "Validates that a field is not null or empty";
        }
    }
}