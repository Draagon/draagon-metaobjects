package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Constraint that validates values against a predefined set of allowed values.
 *
 * <p>This constraint ensures that string values match one of a specified set of
 * allowed values, similar to an enumeration. It's useful for validating types,
 * categories, status values, and other constrained vocabularies.</p>
 *
 * <h3>Common Use Cases:</h3>
 * <ul>
 *   <li><strong>Type Validation:</strong> Ensuring type names are from allowed set</li>
 *   <li><strong>Status Values:</strong> Validating status fields against allowed states</li>
 *   <li><strong>Category Selection:</strong> Ensuring categories are from predefined list</li>
 *   <li><strong>Schema Types:</strong> Validating XSD/JSON Schema type values</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Validate JSON Schema types
 * EnumConstraint jsonSchemaTypes = new EnumConstraint(
 *     "jsonschema.type.valid",
 *     "JSON Schema type must be valid",
 *     (md) -> md.hasAttribute("jsonSchemaType"),
 *     Set.of("string", "number", "integer", "boolean", "array", "object", "null")
 * );
 *
 * // Validate XSD types
 * EnumConstraint xsdTypes = new EnumConstraint(
 *     "xsd.type.valid",
 *     "XSD type must be valid XML Schema type",
 *     (md) -> md.hasAttribute("xsdType"),
 *     Set.of("string", "int", "long", "double", "boolean", "date", "dateTime")
 * );
 *
 * // Validate status values
 * EnumConstraint statusValues = new EnumConstraint(
 *     "record.status.valid",
 *     "Record status must be valid",
 *     (md) -> md.hasAttribute("status"),
 *     Set.of("draft", "active", "inactive", "archived")
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

    /**
     * Create a new enum validation constraint.
     *
     * @param name Unique constraint identifier
     * @param description Human-readable description of the constraint
     * @param applicabilityTest Predicate that determines which MetaData this constraint applies to
     * @param allowedValues Set of allowed string values (case-sensitive)
     * @throws IllegalArgumentException if parameters are invalid
     */
    public EnumConstraint(String name, String description,
                        Predicate<MetaData> applicabilityTest, Set<String> allowedValues) {
        this.name = Objects.requireNonNull(name, "Constraint name cannot be null");
        this.description = Objects.requireNonNull(description, "Constraint description cannot be null");
        this.applicabilityTest = Objects.requireNonNull(applicabilityTest, "Applicability test cannot be null");

        if (allowedValues == null || allowedValues.isEmpty()) {
            throw new IllegalArgumentException("Allowed values cannot be null or empty");
        }

        // Create immutable copy with null values filtered out
        this.allowedValues = allowedValues.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableSet());

        if (this.allowedValues.isEmpty()) {
            throw new IllegalArgumentException("Allowed values cannot contain only null values");
        }
    }

    /**
     * Create a new enum validation constraint with array of allowed values.
     *
     * @param name Unique constraint identifier
     * @param description Human-readable description of the constraint
     * @param applicabilityTest Predicate that determines which MetaData this constraint applies to
     * @param allowedValues Array of allowed string values (case-sensitive)
     */
    public EnumConstraint(String name, String description,
                        Predicate<MetaData> applicabilityTest, String... allowedValues) {
        this(name, description, applicabilityTest,
             allowedValues != null ? Set.of(allowedValues) : Set.of());
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
        if (value != null && !allowedValues.contains(value)) {
            throw new ConstraintViolationException(generateErrorMessage(metaData, context), name, metaData);
        }
    }

    @Override
    public String generateErrorMessage(MetaData metaData, ValidationContext context) {
        String value = getValueToValidate(metaData, context);

        // Sort allowed values for consistent error messages
        List<String> sortedValues = allowedValues.stream()
            .sorted()
            .collect(Collectors.toList());

        String allowedList;
        if (sortedValues.size() <= 5) {
            // Show all values if there aren't too many
            allowedList = sortedValues.stream().collect(Collectors.joining("', '", "'", "'"));
        } else {
            // Show first few values and count if there are many
            List<String> firstFew = sortedValues.subList(0, 4);
            allowedList = firstFew.stream().collect(Collectors.joining("', '", "'", "'")) +
                         " (and " + (sortedValues.size() - 4) + " others)";
        }

        return String.format("%s '%s' must be one of: %s. %s",
                           metaData.getClass().getSimpleName(),
                           value != null ? value : "<null>",
                           allowedList,
                           description);
    }

    /**
     * Get the set of allowed values.
     *
     * @return Immutable set of allowed string values
     */
    public Set<String> getAllowedValues() {
        return allowedValues;
    }

    /**
     * Check if a value is allowed by this constraint.
     *
     * @param value The value to check
     * @return true if the value is in the allowed set
     */
    public boolean isAllowed(String value) {
        return allowedValues.contains(value);
    }

    /**
     * Get the number of allowed values.
     *
     * @return Number of values in the allowed set
     */
    public int getAllowedValueCount() {
        return allowedValues.size();
    }

    /**
     * Extract the value to validate from the MetaData and context.
     *
     * <p>This method determines what string value should be validated against the enum.
     * By default, it validates the MetaData name, but subclasses can override this to
     * validate different values (e.g., attribute values, type properties).</p>
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
        // Show first few values to keep toString() readable
        String valuesList;
        if (allowedValues.size() <= 3) {
            valuesList = allowedValues.toString();
        } else {
            List<String> firstThree = allowedValues.stream()
                .sorted()
                .limit(3)
                .collect(Collectors.toList());
            valuesList = firstThree + " (+" + (allowedValues.size() - 3) + " more)";
        }

        return String.format("EnumConstraint{name='%s', values=%s}", name, valuesList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnumConstraint that = (EnumConstraint) o;
        return Objects.equals(name, that.name) && Objects.equals(allowedValues, that.allowedValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, allowedValues);
    }
}