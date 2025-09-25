package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Enumeration validation constraint that's easy to serialize to schemas.
 *
 * Example usage:
 * - Field types: new EnumConstraint("field.type.enum", "Valid field types",
 *                                  "field", "*", "type",
 *                                  Set.of("string", "int", "long", "double", "boolean"))
 * - Status values: new EnumConstraint("object.status.enum", "Valid status values",
 *                                    "object", "*", "status",
 *                                    Set.of("active", "inactive", "deleted"))
 */
public class EnumConstraint extends BaseConstraint {

    private final Set<String> allowedValues;
    private final boolean caseSensitive;
    private final boolean allowNull;

    /**
     * Create a case-sensitive enum constraint that doesn't allow null values
     * @param constraintId Unique identifier
     * @param description Human-readable description
     * @param targetType Target type pattern
     * @param targetSubType Target subtype pattern
     * @param targetName Target name pattern
     * @param allowedValues Set of allowed values
     */
    public EnumConstraint(String constraintId, String description,
                         String targetType, String targetSubType, String targetName,
                         Set<String> allowedValues) {
        this(constraintId, description, targetType, targetSubType, targetName,
             allowedValues, true, false);
    }

    /**
     * Create an enum constraint with full configuration
     * @param constraintId Unique identifier
     * @param description Human-readable description
     * @param targetType Target type pattern
     * @param targetSubType Target subtype pattern
     * @param targetName Target name pattern
     * @param allowedValues Set of allowed values
     * @param caseSensitive Whether comparison should be case-sensitive
     * @param allowNull Whether null values are allowed
     */
    public EnumConstraint(String constraintId, String description,
                         String targetType, String targetSubType, String targetName,
                         Set<String> allowedValues, boolean caseSensitive, boolean allowNull) {
        super(constraintId, description, targetType, targetSubType, targetName);
        this.caseSensitive = caseSensitive;
        this.allowNull = allowNull;

        if (allowedValues == null || allowedValues.isEmpty()) {
            throw new IllegalArgumentException("allowedValues cannot be null or empty");
        }

        // Store values in appropriate case for comparison
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
            if (!allowNull) {
                throw new ConstraintViolationException(
                    String.format("Value for %s '%s' cannot be null", metaData.getType(), metaData.getName()),
                    constraintId,
                    metaData
                );
            }
            return; // Null allowed
        }

        String stringValue = value.toString();
        String valueToCheck = caseSensitive ? stringValue : stringValue.toLowerCase();

        if (!allowedValues.contains(valueToCheck)) {
            throw new ConstraintViolationException(
                String.format("Value '%s' for %s '%s' is not allowed. Allowed values: %s",
                    stringValue, metaData.getType(), metaData.getName(), getSortedAllowedValues()),
                constraintId,
                metaData
            );
        }
    }

    @Override
    public String getType() {
        return "enum";
    }

    /**
     * Get allowed values for display (sorted)
     * @return Sorted list of allowed values
     */
    public List<String> getSortedAllowedValues() {
        List<String> sorted = new ArrayList<>(allowedValues);
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * Get the set of allowed values
     * @return Set of allowed values (as stored for comparison)
     */
    public Set<String> getAllowedValues() {
        return new HashSet<>(allowedValues);
    }

    /**
     * Check if null values are allowed
     * @return True if null values are allowed
     */
    public boolean isAllowNull() {
        return allowNull;
    }

    /**
     * Check if comparison is case-sensitive
     * @return True if case-sensitive
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    /**
     * Check if a value would be valid
     * @param value Value to check
     * @return True if valid according to this constraint
     */
    public boolean isValueAllowed(String value) {
        if (value == null) {
            return allowNull;
        }

        String valueToCheck = caseSensitive ? value : value.toLowerCase();
        return allowedValues.contains(valueToCheck);
    }

    /**
     * Get the number of allowed values
     * @return Size of allowed values set
     */
    public int getValueCount() {
        return allowedValues.size();
    }

    @Override
    public String toString() {
        return "EnumConstraint{" +
               "id='" + constraintId + '\'' +
               ", values=" + getSortedAllowedValues() +
               ", target=" + getTargetDescription() +
               ", caseSensitive=" + caseSensitive +
               ", allowNull=" + allowNull +
               '}';
    }
}