package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;

import java.util.function.Predicate;

/**
 * Concrete constraint implementation for string length validation.
 *
 * <p>This constraint validates that string values meet minimum and/or maximum length requirements.
 * It supports min-only, max-only, and range validation patterns.</p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Minimum length constraint
 * LengthConstraint minLength = new LengthConstraint(
 *     "field.name.minlength",
 *     "Field names must be at least 2 characters",
 *     (md) -> md instanceof MetaField,
 *     2, null
 * );
 *
 * // Maximum length constraint
 * LengthConstraint maxLength = new LengthConstraint(
 *     "field.description.maxlength",
 *     "Field descriptions must be no more than 255 characters",
 *     (md) -> md.hasAttribute("description"),
 *     null, 255
 * );
 *
 * // Range constraint
 * LengthConstraint rangeLength = new LengthConstraint(
 *     "field.name.range",
 *     "Field names must be 2-64 characters",
 *     (md) -> md instanceof MetaField,
 *     2, 64
 * );
 * }</pre>
 *
 * @since 6.2.0
 */
public class LengthConstraint implements Constraint {

    private final String name;
    private final String description;
    private final Predicate<MetaData> applicabilityTest;
    private final Integer minLength;
    private final Integer maxLength;

    /**
     * Create a length validation constraint.
     *
     * @param name Unique identifier for this constraint
     * @param description Human-readable description of what this constraint validates
     * @param applicabilityTest Predicate to determine which MetaData this constraint applies to
     * @param minLength Minimum required length (null for no minimum)
     * @param maxLength Maximum allowed length (null for no maximum)
     * @throws IllegalArgumentException if both minLength and maxLength are null, or if minLength > maxLength
     */
    public LengthConstraint(String name, String description, Predicate<MetaData> applicabilityTest,
                          Integer minLength, Integer maxLength) {
        this.name = name;
        this.description = description;
        this.applicabilityTest = applicabilityTest;
        this.minLength = minLength;
        this.maxLength = maxLength;

        if (minLength == null && maxLength == null) {
            throw new IllegalArgumentException("At least one of minLength or maxLength must be specified");
        }

        if (minLength != null && maxLength != null && minLength > maxLength) {
            throw new IllegalArgumentException("minLength (" + minLength + ") cannot be greater than maxLength (" + maxLength + ")");
        }

        if (minLength != null && minLength < 0) {
            throw new IllegalArgumentException("minLength cannot be negative: " + minLength);
        }

        if (maxLength != null && maxLength < 0) {
            throw new IllegalArgumentException("maxLength cannot be negative: " + maxLength);
        }
    }

    @Override
    public void validate(MetaData metaData, Object value, ValidationContext context) throws ConstraintViolationException {
        if (value == null) {
            return; // Null values are allowed - use separate RequiredConstraint for non-null validation
        }

        String stringValue = value.toString();
        int length = stringValue.length();

        if (minLength != null && length < minLength) {
            throw new ConstraintViolationException(
                generateMinLengthErrorMessage(metaData, stringValue, length, context),
                name,
                metaData
            );
        }

        if (maxLength != null && length > maxLength) {
            throw new ConstraintViolationException(
                generateMaxLengthErrorMessage(metaData, stringValue, length, context),
                name,
                metaData
            );
        }
    }

    @Override
    public String getType() {
        return "length-validation";
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
     * Generate error message for minimum length violation.
     */
    private String generateMinLengthErrorMessage(MetaData metaData, String value, int actualLength, ValidationContext context) {
        return String.format(
            "Value '%s' for %s '%s' is too short: %d characters (minimum: %d)",
            truncateForDisplay(value),
            metaData.getClass().getSimpleName(),
            metaData.getName(),
            actualLength,
            minLength
        );
    }

    /**
     * Generate error message for maximum length violation.
     */
    private String generateMaxLengthErrorMessage(MetaData metaData, String value, int actualLength, ValidationContext context) {
        return String.format(
            "Value '%s' for %s '%s' is too long: %d characters (maximum: %d)",
            truncateForDisplay(value),
            metaData.getClass().getSimpleName(),
            metaData.getName(),
            actualLength,
            maxLength
        );
    }

    /**
     * Truncate long values for display in error messages.
     */
    private String truncateForDisplay(String value) {
        if (value.length() <= 50) {
            return value;
        }
        return value.substring(0, 47) + "...";
    }

    /**
     * Get the minimum length requirement.
     *
     * @return The minimum length, or null if no minimum is set
     */
    public Integer getMinLength() {
        return minLength;
    }

    /**
     * Get the maximum length requirement.
     *
     * @return The maximum length, or null if no maximum is set
     */
    public Integer getMaxLength() {
        return maxLength;
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
     * Check if this is a minimum-only constraint.
     *
     * @return true if only minimum length is specified
     */
    public boolean isMinimumOnly() {
        return minLength != null && maxLength == null;
    }

    /**
     * Check if this is a maximum-only constraint.
     *
     * @return true if only maximum length is specified
     */
    public boolean isMaximumOnly() {
        return minLength == null && maxLength != null;
    }

    /**
     * Check if this is a range constraint.
     *
     * @return true if both minimum and maximum lengths are specified
     */
    public boolean isRangeConstraint() {
        return minLength != null && maxLength != null;
    }

    @Override
    public String toString() {
        return "LengthConstraint{" +
               "name='" + name + '\'' +
               ", minLength=" + minLength +
               ", maxLength=" + maxLength +
               ", description='" + description + '\'' +
               '}';
    }
}