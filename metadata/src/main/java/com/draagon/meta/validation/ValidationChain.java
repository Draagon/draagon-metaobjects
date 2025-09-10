package com.draagon.meta.validation;

import com.draagon.meta.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Chain of responsibility pattern for MetaData validation.
 * Allows combining multiple validators in a flexible, composable way.
 * 
 * @param <T> The type of object to validate
 */
public class ValidationChain<T> implements Validator<T> {
    
    private static final Logger log = LoggerFactory.getLogger(ValidationChain.class);
    
    private final List<Validator<T>> validators = new CopyOnWriteArrayList<>();
    private final boolean stopOnFirstError;
    private final String chainName;
    
    /**
     * Create a validation chain that continues validation even after errors
     */
    public ValidationChain() {
        this("DefaultChain", false);
    }
    
    /**
     * Create a validation chain with custom behavior
     * 
     * @param chainName Name for logging and debugging
     * @param stopOnFirstError If true, stop validation after first error
     */
    public ValidationChain(String chainName, boolean stopOnFirstError) {
        this.chainName = chainName;
        this.stopOnFirstError = stopOnFirstError;
    }
    
    /**
     * Add a validator to the chain
     * 
     * @param validator The validator to add
     * @return This chain for fluent configuration
     */
    public ValidationChain<T> addValidator(Validator<T> validator) {
        if (validator == null) {
            throw new IllegalArgumentException("Validator cannot be null");
        }
        
        validators.add(validator);
        log.trace("Added validator to chain '{}' (total: {})", chainName, validators.size());
        return this;
    }
    
    /**
     * Add multiple validators to the chain
     * 
     * @param validators The validators to add
     * @return This chain for fluent configuration
     */
    @SafeVarargs
    public final ValidationChain<T> addValidators(Validator<T>... validators) {
        for (Validator<T> validator : validators) {
            addValidator(validator);
        }
        return this;
    }
    
    /**
     * Remove a validator from the chain
     * 
     * @param validator The validator to remove
     * @return true if the validator was removed
     */
    public boolean removeValidator(Validator<T> validator) {
        boolean removed = validators.remove(validator);
        if (removed) {
            log.trace("Removed validator from chain '{}' (total: {})", chainName, validators.size());
        }
        return removed;
    }
    
    /**
     * Clear all validators from the chain
     */
    public void clearValidators() {
        int count = validators.size();
        validators.clear();
        log.debug("Cleared {} validators from chain '{}'", count, chainName);
    }
    
    /**
     * Get the number of validators in the chain
     * 
     * @return The number of validators
     */
    public int getValidatorCount() {
        return validators.size();
    }
    
    /**
     * Check if the chain has any validators
     * 
     * @return true if the chain is empty
     */
    public boolean isEmpty() {
        return validators.isEmpty();
    }
    
    /**
     * Validate the object using all validators in the chain
     * 
     * @param object The object to validate
     * @return Combined validation result from all validators
     */
    @Override
    public ValidationResult validate(T object) {
        if (validators.isEmpty()) {
            log.trace("Validation chain '{}' is empty, returning success", chainName);
            return ValidationResult.success();
        }
        
        log.trace("Starting validation chain '{}' with {} validators", chainName, validators.size());
        
        ValidationResult.Builder resultBuilder = ValidationResult.builder();
        int validatorIndex = 0;
        
        for (Validator<T> validator : validators) {
            try {
                ValidationResult result = validator.validate(object);
                
                if (!result.isValid()) {
                    log.trace("Validator {} in chain '{}' found errors: {}", 
                             validatorIndex, chainName, result.getErrors().size());
                    
                    // Add errors from this validator
                    resultBuilder.addErrors(result.getErrors());
                    
                    // Add child errors if any
                    result.getChildErrors().forEach(resultBuilder::addChildErrors);
                    
                    // Stop on first error if configured
                    if (stopOnFirstError) {
                        log.trace("Stopping validation chain '{}' on first error", chainName);
                        break;
                    }
                } else {
                    log.trace("Validator {} in chain '{}' passed", validatorIndex, chainName);
                }
                
            } catch (Exception e) {
                log.error("Validator {} in chain '{}' threw exception", validatorIndex, chainName, e);
                resultBuilder.addError("Validator " + validatorIndex + " failed with exception: " + e.getMessage());
                
                if (stopOnFirstError) {
                    break;
                }
            }
            
            validatorIndex++;
        }
        
        ValidationResult finalResult = resultBuilder.build();
        log.trace("Validation chain '{}' completed. Valid: {}, Errors: {}", 
                 chainName, finalResult.isValid(), finalResult.getErrors().size());
        
        return finalResult;
    }
    
    /**
     * Create a copy of this validation chain
     * 
     * @return A new ValidationChain with the same validators and configuration
     */
    public ValidationChain<T> copy() {
        ValidationChain<T> copy = new ValidationChain<>(chainName + "-copy", stopOnFirstError);
        copy.validators.addAll(this.validators);
        return copy;
    }
    
    /**
     * Get information about this validation chain
     * 
     * @return String description of the chain
     */
    public String getChainInfo() {
        return String.format("ValidationChain[name=%s, validators=%d, stopOnFirstError=%s]", 
                           chainName, validators.size(), stopOnFirstError);
    }
    
    /**
     * Create a builder for fluent chain construction
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }
    
    /**
     * Create a named builder for fluent chain construction
     */
    public static <T> Builder<T> builder(String chainName) {
        return new Builder<T>().withName(chainName);
    }
    
    /**
     * Builder class for fluent ValidationChain construction
     */
    public static class Builder<T> {
        private String chainName = "DefaultChain";
        private boolean stopOnFirstError = false;
        private final List<Validator<T>> validators = new ArrayList<>();
        
        public Builder<T> withName(String name) {
            this.chainName = name;
            return this;
        }
        
        public Builder<T> stopOnFirstError() {
            this.stopOnFirstError = true;
            return this;
        }
        
        public Builder<T> continueOnError() {
            this.stopOnFirstError = false;
            return this;
        }
        
        public Builder<T> addValidator(Validator<T> validator) {
            this.validators.add(validator);
            return this;
        }
        
        @SafeVarargs
        public final Builder<T> addValidators(Validator<T>... validators) {
            for (Validator<T> validator : validators) {
                this.validators.add(validator);
            }
            return this;
        }
        
        public ValidationChain<T> build() {
            ValidationChain<T> chain = new ValidationChain<>(chainName, stopOnFirstError);
            validators.forEach(chain::addValidator);
            return chain;
        }
    }
}