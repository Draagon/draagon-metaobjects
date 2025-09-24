package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Constraint that validates values against regular expression patterns.
 *
 * <p>This constraint is commonly used for validating naming patterns, format requirements,
 * and other text-based validation rules. It provides regex pattern matching with
 * compiled pattern caching for performance.</p>
 *
 * <h3>Common Use Cases:</h3>
 * <ul>
 *   <li><strong>Naming Patterns:</strong> Field names, table names following identifier rules</li>
 *   <li><strong>Format Validation:</strong> Email addresses, phone numbers, URLs</li>
 *   <li><strong>Code Standards:</strong> Enforcing naming conventions in generated code</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // Validate field names follow identifier pattern
 * RegexValidationConstraint fieldNaming = new RegexValidationConstraint(
 *     "field.naming.pattern",
 *     "Field names must follow identifier pattern: ^[a-zA-Z][a-zA-Z0-9_]*$",
 *     (md) -> md instanceof MetaField,
 *     "^[a-zA-Z][a-zA-Z0-9_]*$"
 * );
 *
 * // Validate database table names
 * RegexValidationConstraint tableNaming = new RegexValidationConstraint(
 *     "database.table.naming",
 *     "Database table names must be valid SQL identifiers",
 *     (md) -> md instanceof MetaObject && md.hasAttribute("dbTable"),
 *     "^[a-zA-Z][a-zA-Z0-9_]*$"
 * );
 * }</pre>
 *
 * @since 6.2.0
 */
public class RegexValidationConstraint implements Constraint {

    private final String name;
    private final String description;
    private final Predicate<MetaData> applicabilityTest;
    private final String regex;
    private final Pattern pattern;

    /**
     * Create a new regex validation constraint.
     *
     * @param name Unique constraint identifier
     * @param description Human-readable description of the constraint
     * @param applicabilityTest Predicate that determines which MetaData this constraint applies to
     * @param regex Regular expression pattern to validate against
     * @throws IllegalArgumentException if any parameter is null or regex is invalid
     */
    public RegexValidationConstraint(String name, String description,
                                   Predicate<MetaData> applicabilityTest, String regex) {
        this.name = Objects.requireNonNull(name, "Constraint name cannot be null");
        this.description = Objects.requireNonNull(description, "Constraint description cannot be null");
        this.applicabilityTest = Objects.requireNonNull(applicabilityTest, "Applicability test cannot be null");
        this.regex = Objects.requireNonNull(regex, "Regex pattern cannot be null");

        try {
            this.pattern = Pattern.compile(regex);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid regex pattern: " + regex, e);
        }
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
        if (value != null && !pattern.matcher(value).matches()) {
            throw new ConstraintViolationException(generateErrorMessage(metaData, context), name, metaData);
        }
    }

    @Override
    public String generateErrorMessage(MetaData metaData, ValidationContext context) {
        String value = getValueToValidate(metaData, context);
        return String.format("%s '%s' does not match required pattern '%s'. %s",
                           metaData.getClass().getSimpleName(),
                           value != null ? value : "<null>",
                           regex,
                           description);
    }

    /**
     * Get the regular expression pattern.
     *
     * @return The regex pattern string (for schema generators)
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Get the compiled pattern.
     *
     * @return The compiled Pattern object
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Extract the value to validate from the MetaData and context.
     *
     * <p>This method determines what value should be validated against the regex pattern.
     * By default, it validates the MetaData name, but subclasses can override this to
     * validate different values (e.g., attribute values, computed properties).</p>
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
        return String.format("RegexValidationConstraint{name='%s', pattern='%s'}", name, regex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegexValidationConstraint that = (RegexValidationConstraint) o;
        return Objects.equals(name, that.name) && Objects.equals(regex, that.regex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, regex);
    }
}