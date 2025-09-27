package com.metaobjects.registry;

import com.metaobjects.MetaData;
import com.metaobjects.constraint.ConstraintViolationException;

import java.util.function.Predicate;
import java.util.function.BiPredicate;

/**
 * Enhanced requirement for child MetaData elements with integrated constraint validation.
 *
 * <p>This unified class supports both placement rules and value validation:</p>
 *
 * <ul>
 *   <li><strong>Pattern matching:</strong> {@code name="pattern", type="attr", subType="string"}</li>
 *   <li><strong>Wildcard matching:</strong> {@code name="*", type="field", subType="*"}</li>
 *   <li><strong>Placement validation:</strong> Predicate-based parent-child validation</li>
 *   <li><strong>Value validation:</strong> Custom validation logic for attribute values</li>
 * </ul>
 *
 * @since 6.2.0
 */
public class ChildRequirement {

    private final String name;           // "pattern", "email", "*" for any
    private final String expectedType;   // "field", "attr", "validator", "*" for any
    private final String expectedSubType; // "string", "int", "*" for any
    private final boolean required;

    // Enhanced constraint validation support
    private final Predicate<MetaData> parentMatcher;    // Custom parent validation
    private final Predicate<MetaData> childMatcher;     // Custom child validation
    private final BiPredicate<MetaData, Object> valueValidator; // Value validation logic
    private final String constraintId;                  // Unique constraint identifier
    private final String validationDescription;         // Human-readable validation description
    
    /**
     * Create a basic child requirement with pattern matching only
     *
     * @param name Expected child name or "*" for any name
     * @param expectedType Expected child type or "*" for any type
     * @param expectedSubType Expected child subType or "*" for any subType
     * @param required Whether this child is required (true) or optional (false)
     */
    public ChildRequirement(String name, String expectedType, String expectedSubType, boolean required) {
        this(name, expectedType, expectedSubType, required, null, null, null, null, null);
    }

    /**
     * Create an enhanced child requirement with constraint validation support
     *
     * @param name Expected child name or "*" for any name
     * @param expectedType Expected child type or "*" for any type
     * @param expectedSubType Expected child subType or "*" for any subType
     * @param required Whether this child is required (true) or optional (false)
     * @param parentMatcher Custom predicate for parent validation (null for none)
     * @param childMatcher Custom predicate for child validation (null for none)
     * @param valueValidator Custom value validation logic (null for none)
     * @param constraintId Unique constraint identifier (null for none)
     * @param validationDescription Human-readable validation description (null for none)
     */
    public ChildRequirement(String name, String expectedType, String expectedSubType, boolean required,
                          Predicate<MetaData> parentMatcher, Predicate<MetaData> childMatcher,
                          BiPredicate<MetaData, Object> valueValidator, String constraintId, String validationDescription) {
        this.name = name != null ? name : "*";
        // Normalize types to lowercase for consistency with MetaDataTypeId
        this.expectedType = expectedType != null ? expectedType.toLowerCase() : "*";
        this.expectedSubType = expectedSubType != null ? expectedSubType.toLowerCase() : "*";
        this.required = required;
        this.parentMatcher = parentMatcher;
        this.childMatcher = childMatcher;
        this.valueValidator = valueValidator;
        this.constraintId = constraintId;
        this.validationDescription = validationDescription;
    }
    
    /**
     * Create an optional child requirement
     *
     * @param name Expected child name or "*" for any name
     * @param expectedType Expected child type or "*" for any type
     * @param expectedSubType Expected child subType or "*" for any subType
     * @return New optional ChildRequirement
     */
    public static ChildRequirement optional(String name, String expectedType, String expectedSubType) {
        return new ChildRequirement(name, expectedType, expectedSubType, false);
    }

    /**
     * Create a required child requirement
     *
     * @param name Expected child name or "*" for any name
     * @param expectedType Expected child type or "*" for any type
     * @param expectedSubType Expected child subType or "*" for any subType
     * @return New required ChildRequirement
     */
    public static ChildRequirement required(String name, String expectedType, String expectedSubType) {
        return new ChildRequirement(name, expectedType, expectedSubType, true);
    }

    /**
     * Create a placement constraint (equivalent to PlacementConstraint)
     *
     * @param constraintId Unique constraint identifier
     * @param description Human-readable description of the placement rule
     * @param parentMatcher Predicate to test if a parent MetaData can contain the child
     * @param childMatcher Predicate to test if a child MetaData can be placed under the parent
     * @return New placement constraint as ChildRequirement
     */
    public static ChildRequirement placementConstraint(String constraintId, String description,
                                                      Predicate<MetaData> parentMatcher,
                                                      Predicate<MetaData> childMatcher) {
        return new ChildRequirement("*", "*", "*", false, parentMatcher, childMatcher, null, constraintId, description);
    }

    /**
     * Create a validation constraint (equivalent to ValidationConstraint)
     *
     * @param constraintId Unique constraint identifier
     * @param description Human-readable description of the validation rule
     * @param applicabilityTest Predicate to determine which MetaData this constraint applies to
     * @param valueValidator Custom value validation logic
     * @return New validation constraint as ChildRequirement
     */
    public static ChildRequirement validationConstraint(String constraintId, String description,
                                                       Predicate<MetaData> applicabilityTest,
                                                       BiPredicate<MetaData, Object> valueValidator) {
        return new ChildRequirement("*", "*", "*", false, applicabilityTest, null, valueValidator, constraintId, description);
    }
    
    /**
     * Test if this requirement matches a specific child
     * 
     * @param childType Actual child type
     * @param childSubType Actual child subType
     * @param childName Actual child name
     * @return true if the child matches this requirement
     */
    public boolean matches(String childType, String childSubType, String childName) {
        return matchesPattern(this.expectedType, childType) &&
               matchesPattern(this.expectedSubType, childSubType) &&
               matchesPattern(this.name, childName);
    }
    
    /**
     * Wildcard pattern matching - "*" matches anything
     * 
     * @param pattern Pattern string, "*" for wildcard
     * @param value Actual value to test
     * @return true if pattern matches value
     */
    private boolean matchesPattern(String pattern, String value) {
        if (pattern == null || "*".equals(pattern)) {
            return true;
        }
        return pattern.equals(value);
    }
    
    /**
     * Get the expected name pattern
     * 
     * @return Expected name or "*" for any name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the expected type pattern
     * 
     * @return Expected type or "*" for any type
     */
    public String getExpectedType() {
        return expectedType;
    }
    
    /**
     * Get the expected subType pattern
     * 
     * @return Expected subType or "*" for any subType
     */
    public String getExpectedSubType() {
        return expectedSubType;
    }
    
    /**
     * Check if this child is required
     * 
     * @return true if required, false if optional
     */
    public boolean isRequired() {
        return required;
    }
    
    /**
     * Check if this requirement uses wildcard matching
     *
     * @return true if any field uses "*" pattern
     */
    public boolean isWildcard() {
        return "*".equals(name) || "*".equals(expectedType) || "*".equals(expectedSubType);
    }

    /**
     * Check if this is a placement constraint (has parent/child matchers)
     *
     * @return true if this requirement has placement validation logic
     */
    public boolean isPlacementConstraint() {
        return parentMatcher != null && childMatcher != null;
    }

    /**
     * Check if this is a value validation constraint
     *
     * @return true if this requirement has value validation logic
     */
    public boolean isValidationConstraint() {
        return valueValidator != null;
    }

    /**
     * Check if a placement is allowed using predicate-based validation
     *
     * @param parent The parent MetaData
     * @param child The child MetaData to be added
     * @return true if this placement is allowed by this constraint
     */
    public boolean isPlacementAllowed(MetaData parent, MetaData child) {
        if (!isPlacementConstraint()) {
            return true; // No placement constraint means allowed
        }
        return parentMatcher.test(parent) && childMatcher.test(child);
    }

    /**
     * Validate a value using the constraint validation logic
     *
     * @param metaData The metadata object being validated
     * @param value The value being validated (can be null)
     * @throws ConstraintViolationException If the constraint is violated
     */
    public void validateValue(MetaData metaData, Object value)
            throws ConstraintViolationException {
        if (!isValidationConstraint()) {
            return; // No validation constraint means valid
        }

        // Apply the constraint if it applies to this metadata
        if (parentMatcher != null && !parentMatcher.test(metaData)) {
            return; // Constraint doesn't apply to this metadata
        }

        if (!valueValidator.test(metaData, value)) {
            String message = validationDescription != null ? validationDescription :
                            "Value validation failed for " + metaData.getName();
            throw new ConstraintViolationException(message, constraintId, metaData);
        }
    }

    /**
     * Get the unique constraint identifier
     *
     * @return The constraint ID or null if not a constraint
     */
    public String getConstraintId() {
        return constraintId;
    }

    /**
     * Get the validation description
     *
     * @return The validation description or null if not provided
     */
    public String getValidationDescription() {
        return validationDescription;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ChildRequirement that = (ChildRequirement) obj;
        return required == that.required &&
               name.equals(that.name) &&
               expectedType.equals(that.expectedType) &&
               expectedSubType.equals(that.expectedSubType);
    }
    
    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + expectedType.hashCode();
        result = 31 * result + expectedSubType.hashCode();
        result = 31 * result + Boolean.hashCode(required);
        return result;
    }
    
    @Override
    public String toString() {
        String qualifier = required ? "required" : "optional";
        return String.format("%s child[name=%s, type=%s.%s]", 
            qualifier, name, expectedType, expectedSubType);
    }
    
    /**
     * Create a human-readable description for error messages
     *
     * @return Description like "required attribute 'pattern' of type string" or constraint description
     */
    public String getDescription() {
        // If this is a constraint with custom description, use that
        if (validationDescription != null) {
            return validationDescription;
        }

        StringBuilder desc = new StringBuilder();
        desc.append(required ? "required" : "optional");

        if ("attr".equals(expectedType)) {
            desc.append(" attribute");
        } else if ("field".equals(expectedType)) {
            desc.append(" field");
        } else {
            desc.append(" child");
        }

        if (!"*".equals(name)) {
            desc.append(" '").append(name).append("'");
        }

        if (!"*".equals(expectedSubType)) {
            desc.append(" of type ").append(expectedSubType);
        }

        return desc.toString();
    }
}