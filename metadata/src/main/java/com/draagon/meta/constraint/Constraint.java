package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;

/**
 * Base interface for all metadata constraints.
 *
 * <p>Constraints validate MetaData instances and their relationships according to specific rules.
 * They provide a unified way to define validation logic that can be easily converted to
 * schema restrictions in XSD, JSON Schema, and other formats.</p>
 *
 * <p>Key design principles:</p>
 * <ul>
 *   <li><strong>Applicability Testing:</strong> Each constraint determines which MetaData it applies to</li>
 *   <li><strong>Validation Logic:</strong> Concrete validation implementation</li>
 *   <li><strong>Error Generation:</strong> Rich error messages with context</li>
 *   <li><strong>Schema Integration:</strong> Constraints expose data for schema generators</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * Constraint namePattern = new RegexValidationConstraint(
 *     "field.naming.pattern",
 *     "Field names must follow identifier pattern",
 *     (md) -> md instanceof MetaField,
 *     "^[a-zA-Z][a-zA-Z0-9_]*$"
 * );
 *
 * constraintRegistry.addConstraint(namePattern);
 * }</pre>
 *
 * @since 6.2.0
 */
public interface Constraint {

    /**
     * Get the unique identifier for this constraint.
     *
     * @return Unique constraint name (e.g., "field.naming.pattern", "database.table.naming")
     */
    String getName();

    /**
     * Get a human-readable description of this constraint.
     *
     * @return Description explaining what this constraint validates
     */
    String getDescription();

    /**
     * Test whether this constraint applies to the given MetaData.
     *
     * <p>This method determines if the constraint should be evaluated for the
     * specified MetaData instance. It's called before {@link #validate} to
     * filter constraints to only those relevant to the current context.</p>
     *
     * @param metaData The MetaData to test applicability for
     * @return true if this constraint should validate the MetaData, false otherwise
     */
    boolean appliesTo(MetaData metaData);

    /**
     * Validate the MetaData according to this constraint's rules.
     *
     * <p>This method performs the actual validation logic. It should only be called
     * after {@link #appliesTo} returns true for the given MetaData.</p>
     *
     * @param metaData The MetaData to validate
     * @param context Validation context providing additional information
     * @throws ConstraintViolationException if the constraint is violated
     */
    void validate(MetaData metaData, ValidationContext context) throws ConstraintViolationException;

    /**
     * Generate a descriptive error message for a constraint violation.
     *
     * <p>This method creates rich error messages that help developers understand
     * what went wrong and how to fix it. The message should be specific to the
     * violation and include relevant context.</p>
     *
     * @param metaData The MetaData that failed validation
     * @param context Validation context with additional error details
     * @return Descriptive error message for the constraint violation
     */
    String generateErrorMessage(MetaData metaData, ValidationContext context);
}