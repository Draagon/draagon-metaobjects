package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Constraint that validates string length requirements.
 *
 * <p>This constraint validates that string values fall within specified minimum
 * and maximum length bounds. It supports both minimum-only, maximum-only, and
 * range validation scenarios.</p>
 *
 * <h3>Common Use Cases:</h3>
 * <ul>
 *   <li><strong>Field Names:</strong> Ensuring field names are not too short or too long</li>
 *   <li><strong>Database Columns:</strong> Validating column lengths match database constraints</li>
 *   <li><strong>User Input:</strong> Enforcing reasonable limits on text input fields</li>
 *   <li><strong>Identifiers:</strong> Ensuring identifiers meet system requirements</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Validate field names are reasonable length
 * LengthConstraint fieldNameLength = new LengthConstraint(
 *     "field.name.length",
 *     "Field names must be between 1 and 64 characters",
 *     (md) -> md instanceof MetaField,
 *     1, 64
 * );
 *
 * // Validate minimum length only
 * LengthConstraint minLength = new LengthConstraint(
 *     "description.minimum",
 *     "Descriptions must be at least 10 characters",
 *     (md) -> md.hasAttribute("description"),
 *     10, null
 * );
 *
 * // Validate maximum length only
 * LengthConstraint maxLength = new LengthConstraint(
 *     "title.maximum",
 *     "Titles must be no more than 255 characters",
 *     (md) -> md.hasAttribute("title"),
 *     null, 255
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
     * Create a new length validation constraint.
     *
     * @param name Unique constraint identifier
     * @param description Human-readable description of the constraint
     * @param applicabilityTest Predicate that determines which MetaData this constraint applies to
     * @param minLength Minimum required length (null for no minimum)
     * @param maxLength Maximum allowed length (null for no maximum)
     * @throws IllegalArgumentException if parameters are invalid
     */
    public LengthConstraint(String name, String description,
                          Predicate<MetaData> applicabilityTest,
                          Integer minLength, Integer maxLength) {
        this.name = Objects.requireNonNull(name, "Constraint name cannot be null");
        this.description = Objects.requireNonNull(description, "Constraint description cannot be null");
        this.applicabilityTest = Objects.requireNonNull(applicabilityTest, "Applicability test cannot be null");

        if (minLength != null && minLength < 0) {
            throw new IllegalArgumentException("Minimum length cannot be negative: " + minLength);
        }
        if (maxLength != null && maxLength < 0) {
            throw new IllegalArgumentException("Maximum length cannot be negative: " + maxLength);
        }
        if (minLength != null && maxLength != null && minLength > maxLength) {
            throw new IllegalArgumentException(
                String.format("Minimum length (%d) cannot be greater than maximum length (%d)",
                             minLength, maxLength));
        }
        if (minLength == null && maxLength == null) {
            throw new IllegalArgumentException("At least one of minLength or maxLength must be specified");
        }

        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean appliesTo(MetaData metaData) {
        return applicabilityTest.test(metaData);
    }

    @Override
    public void validate(MetaData metaData, ValidationContext context) throws ConstraintViolationException {
        String value = getValueToValidate(metaData, context);
        if (value != null) {
            int length = value.length();

            if (minLength != null && length < minLength) {
                throw new ConstraintViolationException(generateErrorMessage(metaData, context), name, metaData);
            }
            if (maxLength != null && length > maxLength) {
                throw new ConstraintViolationException(generateErrorMessage(metaData, context), name, metaData);
            }
        }
    }

    @Override
    public String generateErrorMessage(MetaData metaData, ValidationContext context) {
        String value = getValueToValidate(metaData, context);
        int actualLength = value != null ? value.length() : 0;

        String lengthRequirement;
        if (minLength != null && maxLength != null) {
            lengthRequirement = String.format("between %d and %d characters", minLength, maxLength);
        } else if (minLength != null) {
            lengthRequirement = String.format("at least %d characters", minLength);
        } else {
            lengthRequirement = String.format("at most %d characters", maxLength);
        }

        return String.format("%s '%s' (length: %d) must be %s. %s",
                           metaData.getClass().getSimpleName(),
                           value != null ? value : "<null>",
                           actualLength,
                           lengthRequirement,
                           description);
    }

    /**
     * Get the minimum required length.
     *
     * @return Minimum length, or null if no minimum
     */
    public Integer getMinLength() {
        return minLength;
    }

    /**
     * Get the maximum allowed length.
     *
     * @return Maximum length, or null if no maximum
     */
    public Integer getMaxLength() {
        return maxLength;
    }

    /**
     * Check if this constraint has a minimum length requirement.
     *
     * @return true if minimum length is specified
     */
    public boolean hasMinLength() {
        return minLength != null;
    }

    /**
     * Check if this constraint has a maximum length requirement.
     *
     * @return true if maximum length is specified
     */
    public boolean hasMaxLength() {
        return maxLength != null;
    }

    /**
     * Extract the value to validate from the MetaData and context.
     *
     * <p>This method determines what string value should be validated for length.
     * By default, it validates the MetaData name, but subclasses can override this to
     * validate different values (e.g., attribute values, descriptions).</p>
     *
     * @param metaData The MetaData being validated
     * @param context Validation context with additional information
     * @return The string value to validate, or null if no value to validate
     */
    protected String getValueToValidate(MetaData metaData, ValidationContext context) {
        // Default implementation validates the MetaData name
        return metaData.getName();
    }

    @Override
    public String toString() {
        return String.format("LengthConstraint{name='%s', min=%s, max=%s}",
                           name, minLength, maxLength);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LengthConstraint that = (LengthConstraint) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(minLength, that.minLength) &&
               Objects.equals(maxLength, that.maxLength);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, minLength, maxLength);
    }
}