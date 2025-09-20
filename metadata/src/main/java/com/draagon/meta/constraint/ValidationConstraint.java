package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * ValidationConstraint defines HOW values are validated for specific MetaData types.
 * This implements value validation rules that can be applied during setValue operations.
 * 
 * Example: "StringField with maxLength attribute must validate string length"
 * - applicableTo: checks if this constraint applies to the MetaData type
 * - validator: validates the value against the constraint logic
 */
public class ValidationConstraint implements Constraint {
    
    /**
     * Functional interface for context-aware validation
     */
    @FunctionalInterface
    public interface ContextAwareValidator {
        boolean test(MetaData metaData, Object value, ValidationContext context);
    }
    
    private final String id;
    private final String description;
    private final Predicate<MetaData> applicableTo;
    private final BiPredicate<MetaData, Object> validator;
    private final ContextAwareValidator contextAwareValidator;
    
    /**
     * Create a validation constraint (legacy constructor)
     * @param id Unique identifier for this constraint
     * @param description Human-readable description of the validation rule
     * @param applicableTo Predicate to test if this constraint applies to a MetaData type
     * @param validator Predicate to validate a value against the MetaData constraints
     */
    public ValidationConstraint(String id, String description,
                               Predicate<MetaData> applicableTo,
                               BiPredicate<MetaData, Object> validator) {
        this.id = id;
        this.description = description;
        this.applicableTo = applicableTo;
        this.validator = validator;
        this.contextAwareValidator = null;
    }
    
    /**
     * Create a validation constraint with context-aware validator
     * @param id Unique identifier for this constraint
     * @param description Human-readable description of the validation rule
     * @param applicableTo Predicate to test if this constraint applies to a MetaData type
     * @param contextAwareValidator Validator that can use ValidationContext for more sophisticated validation
     */
    public ValidationConstraint(String id, String description,
                               Predicate<MetaData> applicableTo,
                               ContextAwareValidator contextAwareValidator) {
        this.id = id;
        this.description = description;
        this.applicableTo = applicableTo;
        this.validator = null;
        this.contextAwareValidator = contextAwareValidator;
    }
    
    /**
     * Check if this constraint applies to the given MetaData
     * @param metaData The MetaData to check
     * @return True if this validation constraint should be applied
     */
    public boolean appliesTo(MetaData metaData) {
        return applicableTo.test(metaData);
    }
    
    /**
     * Validate a value against this constraint (without context)
     * @param metaData The MetaData context for validation
     * @param value The value to validate
     * @return True if the value is valid according to this constraint
     */
    public boolean isValid(MetaData metaData, Object value) {
        if (contextAwareValidator != null) {
            // Use context-aware validator with null context
            return contextAwareValidator.test(metaData, value, null);
        } else if (validator != null) {
            return validator.test(metaData, value);
        } else {
            // No validator configured - assume valid
            return true;
        }
    }
    
    /**
     * Validate a value against this constraint (with context)
     * @param metaData The MetaData context for validation
     * @param value The value to validate
     * @param context The validation context
     * @return True if the value is valid according to this constraint
     */
    public boolean isValid(MetaData metaData, Object value, ValidationContext context) {
        if (contextAwareValidator != null) {
            return contextAwareValidator.test(metaData, value, context);
        } else if (validator != null) {
            return validator.test(metaData, value);
        } else {
            // No validator configured - assume valid
            return true;
        }
    }
    
    @Override
    public void validate(MetaData metaData, Object value, ValidationContext context) 
            throws ConstraintViolationException {
        if (appliesTo(metaData) && !isValid(metaData, value, context)) {
            throw new ConstraintViolationException(
                String.format("Validation constraint '%s' failed for %s with value: %s", 
                    id, metaData.getName(), value),
                getType(),
                value,
                context
            );
        }
    }
    
    @Override
    public String getType() {
        return "validation";
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public boolean isApplicableTo(String metaDataType) {
        // ValidationConstraints determine applicability dynamically via the applicableTo predicate
        return true; // Let the predicate decide during actual validation
    }
    
    /**
     * Get the unique identifier for this constraint
     * @return The constraint ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Get the applicability predicate
     * @return Predicate for testing if this constraint applies to MetaData
     */
    public Predicate<MetaData> getApplicabilityPredicate() {
        return applicableTo;
    }
    
    /**
     * Get the validator predicate
     * @return Predicate for validating values against MetaData
     */
    public BiPredicate<MetaData, Object> getValidator() {
        return validator;
    }
    
    @Override
    public String toString() {
        return "ValidationConstraint{" +
               "id='" + id + '\'' +
               ", description='" + description + '\'' +
               '}';
    }
}