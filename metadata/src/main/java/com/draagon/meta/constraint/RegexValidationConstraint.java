package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Concrete constraint implementation for regex pattern validation.
 *
 * <p>This constraint validates that string values match a specific regular expression pattern.
 * It's commonly used for field naming constraints, identifier patterns, and value format validation.</p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Field naming pattern constraint
 * RegexValidationConstraint fieldNaming = new RegexValidationConstraint(
 *     "field.naming.pattern",
 *     "Field names must follow identifier pattern",
 *     (md) -> md instanceof MetaField,
 *     "^[a-zA-Z][a-zA-Z0-9_]*$"
 * );
 *
 * // Email format validation constraint
 * RegexValidationConstraint emailFormat = new RegexValidationConstraint(
 *     "field.email.format",
 *     "Email fields must contain valid email addresses",
 *     (md) -> md.getName().equals("email"),
 *     "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
 * );
 * }</pre>
 *
 * @since 6.2.0
 */
public class RegexValidationConstraint implements Constraint {

    private final String name;
    private final String description;
    private final Predicate<MetaData> applicabilityTest;
    private final Pattern pattern;
    private final String regex;

    /**
     * Create a regex validation constraint.
     *
     * @param name Unique identifier for this constraint
     * @param description Human-readable description of what this constraint validates
     * @param applicabilityTest Predicate to determine which MetaData this constraint applies to
     * @param regex Regular expression pattern to validate against
     * @throws IllegalArgumentException if the regex pattern is invalid
     */
    public RegexValidationConstraint(String name, String description, Predicate<MetaData> applicabilityTest, String regex) {
        this.name = name;
        this.description = description;
        this.applicabilityTest = applicabilityTest;
        this.regex = regex;

        try {
            this.pattern = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid regex pattern: " + regex, e);
        }
    }

    @Override
    public void validate(MetaData metaData, Object value) throws ConstraintViolationException {
        if (value == null) {
            return; // Null values are allowed - use separate RequiredConstraint for non-null validation
        }

        String stringValue = value.toString();
        if (!pattern.matcher(stringValue).matches()) {
            throw new ConstraintViolationException(
                generateErrorMessage(metaData, stringValue),
                name,
                metaData
            );
        }
    }

    @Override
    public String getType() {
        return "regex-validation";
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
        return String.format(
            "Value '%s' for %s '%s' does not match required pattern: %s",
            invalidValue,
            metaData.getClass().getSimpleName(),
            metaData.getName(),
            regex
        );
    }

    /**
     * Get the regex pattern string.
     *
     * @return The regex pattern used for validation
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Get the compiled Pattern object.
     *
     * @return The compiled regex Pattern
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Get the unique constraint name.
     *
     * @return The constraint name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "RegexValidationConstraint{" +
               "name='" + name + '\'' +
               ", regex='" + regex + '\'' +
               ", description='" + description + '\'' +
               '}';
    }
}