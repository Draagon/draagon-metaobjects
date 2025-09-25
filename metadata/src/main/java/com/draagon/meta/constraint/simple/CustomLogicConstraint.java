package com.draagon.meta.constraint.simple;

import com.draagon.meta.MetaData;
import com.draagon.meta.constraint.Constraint;
import com.draagon.meta.constraint.ConstraintViolationException;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Custom logic constraint for complex validation that cannot be expressed
 * in simple, schema-friendly constraint types.
 *
 * This constraint uses functional interfaces for complex validation logic
 * that requires dynamic evaluation, cross-references, or complex business rules
 * that cannot be serialized to XSD or JSON Schema.
 *
 * Usage guidelines:
 * - Use simple constraints (SimpleRegexConstraint, SimpleLengthConstraint, etc.) whenever possible
 * - Only use CustomLogicConstraint when validation logic cannot be expressed declaratively
 * - Schema generators will skip CustomLogicConstraints and document them as "custom validation"
 *
 * Example usage:
 * - Cross-reference validation: Ensure foreign key references exist
 * - Complex business rules: Validate field combinations based on state
 * - Dynamic validation: Rules that depend on runtime configuration
 */
public class CustomLogicConstraint implements Constraint {

    private final String constraintId;
    private final String description;
    private final Predicate<MetaData> applicabilityTest;
    private final BiPredicate<MetaData, Object> validator;
    private final String logicDescription;

    /**
     * Create a custom logic constraint
     * @param constraintId Unique identifier
     * @param description Human-readable description
     * @param applicabilityTest Predicate to determine which MetaData this applies to
     * @param validator Predicate to validate values
     * @param logicDescription Description of the custom logic for documentation
     */
    public CustomLogicConstraint(String constraintId, String description,
                               Predicate<MetaData> applicabilityTest,
                               BiPredicate<MetaData, Object> validator,
                               String logicDescription) {
        this.constraintId = constraintId;
        this.description = description;
        this.applicabilityTest = applicabilityTest;
        this.validator = validator;
        this.logicDescription = logicDescription != null ? logicDescription : "Custom validation logic";
    }

    /**
     * Check if this constraint applies to the given MetaData
     * @param metaData MetaData to check
     * @return True if this constraint should be applied
     */
    public boolean appliesTo(MetaData metaData) {
        return applicabilityTest != null && applicabilityTest.test(metaData);
    }

    /**
     * Validate using the custom logic
     * @param metaData MetaData context
     * @param value Value to validate
     * @return True if valid according to custom logic
     */
    public boolean isValid(MetaData metaData, Object value) {
        if (validator == null) {
            return true; // No validator - assume valid
        }
        return validator.test(metaData, value);
    }

    @Override
    public void validate(MetaData metaData, Object value) throws ConstraintViolationException {
        if (appliesTo(metaData) && !isValid(metaData, value)) {
            throw new ConstraintViolationException(
                String.format("Custom validation constraint '%s' failed for %s '%s' with value: %s - %s",
                    constraintId, metaData.getType(), metaData.getName(), value, logicDescription),
                constraintId,
                metaData
            );
        }
    }

    @Override
    public String getType() {
        return "custom-logic";
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Get the constraint ID
     * @return Unique constraint identifier
     */
    public String getConstraintId() {
        return constraintId;
    }

    /**
     * Get the logic description for documentation
     * @return Description of what the custom logic does
     */
    public String getLogicDescription() {
        return logicDescription;
    }

    /**
     * Get the applicability test predicate
     * @return Predicate for testing applicability
     */
    public Predicate<MetaData> getApplicabilityTest() {
        return applicabilityTest;
    }

    /**
     * Get the validator predicate
     * @return Predicate for validation
     */
    public BiPredicate<MetaData, Object> getValidator() {
        return validator;
    }

    /**
     * Check if this constraint can be serialized to schemas
     * @return False (custom logic cannot be serialized)
     */
    public boolean isSchemaSerializable() {
        return false;
    }

    @Override
    public String toString() {
        return "CustomLogicConstraint{" +
               "id='" + constraintId + '\'' +
               ", logicDescription='" + logicDescription + '\'' +
               ", description='" + description + '\'' +
               '}';
    }
}