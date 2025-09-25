package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;

/**
 * Length validation constraint that's easy to serialize to schemas.
 *
 * Example usage:
 * - Min length: new LengthConstraint("field.name.min", "Field names must be at least 2 chars",
 *                                   "field", "*", "*", 2, null)
 * - Max length: new LengthConstraint("attr.description.max", "Descriptions max 255 chars",
 *                                   "attr", "string", "description", null, 255)
 * - Range: new LengthConstraint("field.name.range", "Field names 2-64 characters",
 *                              "field", "*", "*", 2, 64)
 */
public class LengthConstraint extends BaseConstraint {

    private final Integer minLength;
    private final Integer maxLength;
    private final boolean allowNull;

    /**
     * Create a length constraint that doesn't allow null values
     * @param constraintId Unique identifier
     * @param description Human-readable description
     * @param targetType Target type pattern
     * @param targetSubType Target subtype pattern
     * @param targetName Target name pattern
     * @param minLength Minimum length (null for no minimum)
     * @param maxLength Maximum length (null for no maximum)
     */
    public LengthConstraint(String constraintId, String description,
                           String targetType, String targetSubType, String targetName,
                           Integer minLength, Integer maxLength) {
        this(constraintId, description, targetType, targetSubType, targetName,
             minLength, maxLength, false);
    }

    /**
     * Create a length constraint with null handling option
     * @param constraintId Unique identifier
     * @param description Human-readable description
     * @param targetType Target type pattern
     * @param targetSubType Target subtype pattern
     * @param targetName Target name pattern
     * @param minLength Minimum length (null for no minimum)
     * @param maxLength Maximum length (null for no maximum)
     * @param allowNull Whether null values are allowed
     */
    public LengthConstraint(String constraintId, String description,
                           String targetType, String targetSubType, String targetName,
                           Integer minLength, Integer maxLength, boolean allowNull) {
        super(constraintId, description, targetType, targetSubType, targetName);
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.allowNull = allowNull;

        if (minLength == null && maxLength == null) {
            throw new IllegalArgumentException("At least one of minLength or maxLength must be specified");
        }

        if (minLength != null && minLength < 0) {
            throw new IllegalArgumentException("minLength cannot be negative: " + minLength);
        }

        if (maxLength != null && maxLength < 0) {
            throw new IllegalArgumentException("maxLength cannot be negative: " + maxLength);
        }

        if (minLength != null && maxLength != null && minLength > maxLength) {
            throw new IllegalArgumentException("minLength (" + minLength + ") cannot be greater than maxLength (" + maxLength + ")");
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
        int length = stringValue.length();

        if (minLength != null && length < minLength) {
            throw new ConstraintViolationException(
                String.format("Value '%s' for %s '%s' is too short: %d characters (minimum: %d)",
                    truncateForDisplay(stringValue), metaData.getType(), metaData.getName(), length, minLength),
                constraintId,
                metaData
            );
        }

        if (maxLength != null && length > maxLength) {
            throw new ConstraintViolationException(
                String.format("Value '%s' for %s '%s' is too long: %d characters (maximum: %d)",
                    truncateForDisplay(stringValue), metaData.getType(), metaData.getName(), length, maxLength),
                constraintId,
                metaData
            );
        }
    }

    private String truncateForDisplay(String value) {
        if (value.length() <= 50) {
            return value;
        }
        return value.substring(0, 47) + "...";
    }

    @Override
    public String getType() {
        return "length";
    }

    /**
     * Get minimum length requirement
     * @return Minimum length or null if no minimum
     */
    public Integer getMinLength() {
        return minLength;
    }

    /**
     * Get maximum length requirement
     * @return Maximum length or null if no maximum
     */
    public Integer getMaxLength() {
        return maxLength;
    }

    /**
     * Check if null values are allowed
     * @return True if null values are allowed
     */
    public boolean isAllowNull() {
        return allowNull;
    }

    /**
     * Check if this is a minimum-only constraint
     * @return True if only minimum is specified
     */
    public boolean isMinimumOnly() {
        return minLength != null && maxLength == null;
    }

    /**
     * Check if this is a maximum-only constraint
     * @return True if only maximum is specified
     */
    public boolean isMaximumOnly() {
        return minLength == null && maxLength != null;
    }

    /**
     * Check if this is a range constraint
     * @return True if both minimum and maximum are specified
     */
    public boolean isRangeConstraint() {
        return minLength != null && maxLength != null;
    }

    @Override
    public String toString() {
        return "LengthConstraint{" +
               "id='" + constraintId + '\'' +
               ", min=" + minLength +
               ", max=" + maxLength +
               ", target=" + getTargetDescription() +
               ", allowNull=" + allowNull +
               '}';
    }
}