package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Concrete constraint implementation for enumerated value validation.
 *
 * <p>This constraint validates that values are within a specific set of allowed values.
 * It's commonly used for field types, status values, and other controlled vocabularies.</p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Field type constraint
 * EnumConstraint fieldTypes = new EnumConstraint(
 *     "field.type.values",
 *     "Field types must be valid primitive types",
 *     (md) -> md.getName().equals("type"),
 *     Set.of("string", "int", "long", "double", "boolean", "date")
 * );
 *
 * // Status value constraint
 * EnumConstraint statusValues = new EnumConstraint(
 *     "object.status.values",
 *     "Object status must be active, inactive, or deleted",
 *     (md) -> md.getName().equals("status"),
 *     Set.of("active", "inactive", "deleted")
 * );
 * }</pre>
 *
 * @since 6.2.0
 */
public class EnumConstraint implements Constraint {

    private final String name;
    private final String description;
    private final Predicate<MetaData> applicabilityTest;
    private final Set<String> allowedValues;
    private final boolean caseSensitive;

    /**
     * Create an enum validation constraint (case-sensitive).
     *
     * @param name Unique identifier for this constraint
     * @param description Human-readable description of what this constraint validates
     * @param applicabilityTest Predicate to determine which MetaData this constraint applies to
     * @param allowedValues Set of allowed values
     * @throws IllegalArgumentException if allowedValues is null or empty
     */
    public EnumConstraint(String name, String description, Predicate<MetaData> applicabilityTest, Set<String> allowedValues) {
        this(name, description, applicabilityTest, allowedValues, true);
    }

    /**
     * Create an enum validation constraint with configurable case sensitivity.
     *
     * @param name Unique identifier for this constraint
     * @param description Human-readable description of what this constraint validates
     * @param applicabilityTest Predicate to determine which MetaData this constraint applies to
     * @param allowedValues Set of allowed values
     * @param caseSensitive Whether value comparison should be case-sensitive
     * @throws IllegalArgumentException if allowedValues is null or empty
     */
    public EnumConstraint(String name, String description, Predicate<MetaData> applicabilityTest,
                        Set<String> allowedValues, boolean caseSensitive) {
        this.name = name;
        this.description = description;
        this.applicabilityTest = applicabilityTest;
        this.caseSensitive = caseSensitive;

        if (allowedValues == null || allowedValues.isEmpty()) {
            throw new IllegalArgumentException("allowedValues cannot be null or empty");
        }

        // Store values in the appropriate case for comparison
        if (caseSensitive) {
            this.allowedValues = new HashSet<>(allowedValues);
        } else {
            this.allowedValues = allowedValues.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        }
    }

    @Override
    public void validate(MetaData metaData, Object value) throws ConstraintViolationException {
        if (value == null) {
            return; // Null values are allowed - use separate RequiredConstraint for non-null validation
        }

        String stringValue = value.toString();
        String valueToCheck = caseSensitive ? stringValue : stringValue.toLowerCase();

        if (!allowedValues.contains(valueToCheck)) {
            throw new ConstraintViolationException(
                generateErrorMessage(metaData, stringValue),
                name,
                metaData
            );
        }
    }

    @Override
    public String getType() {
        return "enum-validation";
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isApplicableTo(String metaDataType) {
        // This is a legacy compatibility method - actual applicability is determined by the predicate
        return true;
    }

    /**
     * Test whether this constraint applies to the given MetaData.
     *
     * @param metaData The MetaData to test applicability for
     * @return true if this constraint should validate the MetaData, false otherwise
     */
    public boolean appliesTo(MetaData metaData) {
        return applicabilityTest.test(metaData);
    }

    /**
     * Generate a descriptive error message for the constraint violation.
     *
     * @param metaData The MetaData that failed validation
     * @param invalidValue The value that failed validation
     * @return Descriptive error message for the constraint violation
     */
    public String generateErrorMessage(MetaData metaData, String invalidValue) {
        List<String> sortedValues = new ArrayList<>(getOriginalAllowedValues());
        Collections.sort(sortedValues);

        return String.format(
            "Value '%s' for %s '%s' is not allowed. Allowed values: %s",
            invalidValue,
            metaData.getClass().getSimpleName(),
            metaData.getName(),
            sortedValues
        );
    }

    /**
     * Get the set of allowed values in their original case.
     * This reconstructs the original values for display purposes.
     *
     * @return Set of allowed values in original case
     */
    public Set<String> getOriginalAllowedValues() {
        // If case sensitive, return the stored values directly
        if (caseSensitive) {
            return new HashSet<>(allowedValues);
        }

        // For case-insensitive, we need to return the stored lowercase values
        // Note: This is a limitation - we lose the original casing for case-insensitive constraints
        return new HashSet<>(allowedValues);
    }

    /**
     * Get the processed allowed values used for comparison.
     *
     * @return Set of allowed values as stored for comparison
     */
    public Set<String> getAllowedValues() {
        return new HashSet<>(allowedValues);
    }

    /**
     * Check if this constraint is case-sensitive.
     *
     * @return true if case-sensitive, false otherwise
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    /**
     * Get the unique constraint name.
     *
     * @return The constraint name
     */
    public String getName() {
        return name;
    }

    /**
     * Check if a value would be valid according to this constraint.
     *
     * @param value The value to check
     * @return true if the value is allowed, false otherwise
     */
    public boolean isValueAllowed(String value) {
        if (value == null) {
            return true; // Null values are allowed
        }

        String valueToCheck = caseSensitive ? value : value.toLowerCase();
        return allowedValues.contains(valueToCheck);
    }

    /**
     * Get the number of allowed values.
     *
     * @return The size of the allowed values set
     */
    public int getValueCount() {
        return allowedValues.size();
    }

    @Override
    public String toString() {
        return "EnumConstraint{" +
               "name='" + name + '\'' +
               ", allowedValues=" + allowedValues +
               ", caseSensitive=" + caseSensitive +
               ", description='" + description + '\'' +
               '}';
    }
}