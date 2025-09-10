package com.draagon.meta.validation;

import com.draagon.meta.ValidationResult;

/**
 * Generic validator interface for validating MetaData objects
 * 
 * @param <T> The type of object to validate
 */
@FunctionalInterface
public interface Validator<T> {
    
    /**
     * Validate the given object
     * 
     * @param object The object to validate
     * @return ValidationResult containing any errors or success
     */
    ValidationResult validate(T object);
    
    /**
     * Create a validator that always succeeds
     */
    static <T> Validator<T> alwaysValid() {
        return object -> ValidationResult.success();
    }
    
    /**
     * Create a validator that always fails with the given error
     */
    static <T> Validator<T> alwaysFails(String error) {
        return object -> ValidationResult.withError(error);
    }
    
    /**
     * Combine this validator with another using AND logic
     * Both validators must pass for the result to be valid
     */
    default Validator<T> and(Validator<T> other) {
        return object -> {
            ValidationResult first = this.validate(object);
            ValidationResult second = other.validate(object);
            return first.combine(second);
        };
    }
    
    /**
     * Combine this validator with another using OR logic
     * At least one validator must pass for the result to be valid
     */
    default Validator<T> or(Validator<T> other) {
        return object -> {
            ValidationResult first = this.validate(object);
            if (first.isValid()) {
                return first;
            }
            return other.validate(object);
        };
    }
    
    /**
     * Create a conditional validator that only runs if the predicate is true
     */
    static <T> Validator<T> when(java.util.function.Predicate<T> condition, Validator<T> validator) {
        return object -> {
            if (condition.test(object)) {
                return validator.validate(object);
            }
            return ValidationResult.success();
        };
    }
}