package com.metaobjects;

import java.util.*;

/**
 * Immutable result of metadata validation operations.
 * Uses modern Java patterns for error handling and reporting.
 */
public class ValidationResult {
    
    private final List<String> errors;
    private final Map<String, List<String>> childErrors;
    private final boolean valid;
    
    private ValidationResult(List<String> errors, Map<String, List<String>> childErrors) {
        this.errors = List.copyOf(errors);
        this.childErrors = Map.copyOf(childErrors);
        this.valid = errors.isEmpty() && childErrors.isEmpty();
    }
    
    /**
     * Create a successful validation result
     */
    public static ValidationResult success() {
        return new ValidationResult(List.of(), Map.of());
    }
    
    /**
     * Create a successful validation result (alias for success)
     */
    public static ValidationResult valid() {
        return success();
    }
    
    /**
     * Create a validation result with errors
     */
    public static ValidationResult withErrors(List<String> errors) {
        return new ValidationResult(errors, Map.of());
    }
    
    /**
     * Create a validation result with a single error
     */
    public static ValidationResult withError(String error) {
        return new ValidationResult(List.of(error), Map.of());
    }
    
    /**
     * Create a builder for complex validation results
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public Map<String, List<String>> getChildErrors() {
        return childErrors;
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public boolean hasChildErrors() {
        return !childErrors.isEmpty();
    }
    
    /**
     * Get all errors including child errors as a flattened list
     */
    public List<String> getAllErrors() {
        List<String> allErrors = new ArrayList<>(errors);
        childErrors.forEach((child, childErrorList) -> {
            childErrorList.forEach(error -> 
                allErrors.add(child + ": " + error));
        });
        return allErrors;
    }
    
    /**
     * Convenience method for legacy-style exception behavior.
     * Throws MetaDataException if this result contains errors.
     * 
     * @throws MetaDataException if validation failed
     */
    public void throwIfInvalid() throws MetaDataException {
        if (!isValid()) {
            String errorMessage = String.join("; ", getAllErrors());
            throw new MetaDataException("Validation failed: " + errorMessage);
        }
    }
    
    /**
     * Combine this result with another
     */
    public ValidationResult combine(ValidationResult other) {
        if (other == null) return this;
        
        List<String> combinedErrors = new ArrayList<>(this.errors);
        combinedErrors.addAll(other.errors);
        
        Map<String, List<String>> combinedChildErrors = new HashMap<>(this.childErrors);
        other.childErrors.forEach((key, value) -> 
            combinedChildErrors.merge(key, value, (existing, newList) -> {
                List<String> merged = new ArrayList<>(existing);
                merged.addAll(newList);
                return merged;
            }));
        
        return new ValidationResult(combinedErrors, combinedChildErrors);
    }
    
    @Override
    public String toString() {
        if (valid) {
            return "ValidationResult: VALID";
        }
        
        StringBuilder sb = new StringBuilder("ValidationResult: INVALID\n");
        if (!errors.isEmpty()) {
            sb.append("Errors:\n");
            errors.forEach(error -> sb.append("  - ").append(error).append("\n"));
        }
        
        if (!childErrors.isEmpty()) {
            sb.append("Child Errors:\n");
            childErrors.forEach((child, childErrorList) -> {
                sb.append("  ").append(child).append(":\n");
                childErrorList.forEach(error -> 
                    sb.append("    - ").append(error).append("\n"));
            });
        }
        
        return sb.toString();
    }
    
    /**
     * Builder for ValidationResult
     */
    public static class Builder {
        private final List<String> errors = new ArrayList<>();
        private final Map<String, List<String>> childErrors = new HashMap<>();
        
        public Builder addError(String error) {
            if (error != null && !error.trim().isEmpty()) {
                errors.add(error.trim());
            }
            return this;
        }
        
        public Builder addErrors(Collection<String> errors) {
            if (errors != null) {
                errors.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(this.errors::add);
            }
            return this;
        }
        
        public Builder addChildError(String childName, String error) {
            if (childName != null && error != null && !error.trim().isEmpty()) {
                childErrors.computeIfAbsent(childName, k -> new ArrayList<>())
                    .add(error.trim());
            }
            return this;
        }
        
        public Builder addChildErrors(String childName, Collection<String> errors) {
            if (childName != null && errors != null) {
                List<String> childErrorList = childErrors.computeIfAbsent(childName, k -> new ArrayList<>());
                errors.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(childErrorList::add);
            }
            return this;
        }
        
        public Builder addChildResult(String childName, ValidationResult childResult) {
            if (childName != null && childResult != null && !childResult.isValid()) {
                addChildErrors(childName, childResult.getErrors());
                
                // Flatten nested child errors
                childResult.getChildErrors().forEach((nestedChild, nestedErrors) -> 
                    addChildErrors(childName + "." + nestedChild, nestedErrors));
            }
            return this;
        }
        
        public ValidationResult build() {
            return new ValidationResult(errors, childErrors);
        }
    }
}