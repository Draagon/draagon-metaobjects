package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;

/**
 * ConstraintViolation represents a specific violation of a constraint during validation.
 * This provides detailed information about what constraint was violated, where it occurred,
 * and the context of the violation for debugging and reporting purposes.
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Create violation for placement constraint
 * ConstraintViolation violation = new ConstraintViolation(
 *     "field.placement.invalid",
 *     "StringField cannot be placed under IntField",
 *     parentMetaData,
 *     childMetaData,
 *     "Invalid parent-child relationship",
 *     context
 * );
 *
 * // Create violation for validation constraint
 * ConstraintViolation violation = new ConstraintViolation(
 *     "field.maxLength.exceeded",
 *     "Field value exceeds maximum length",
 *     fieldMetaData,
 *     "This is a very long string that exceeds the limit",
 *     "Value length: 45, Max length: 20",
 *     context
 * );
 * }</pre>
 *
 * @since 6.1.0
 */
public class ConstraintViolation {

    private final String constraintId;
    private final String constraintDescription;
    private final MetaData sourceMetaData;
    private final Object violatingValue;
    private final String violationMessage;
    private final ValidationContext context;
    private final long timestamp;

    /**
     * Create a new constraint violation
     *
     * @param constraintId The unique identifier of the violated constraint
     * @param constraintDescription Description of the constraint that was violated
     * @param sourceMetaData The MetaData where the violation occurred
     * @param violatingValue The value that caused the violation (may be null)
     * @param violationMessage Specific message describing the violation
     * @param context The validation context when the violation occurred
     */
    public ConstraintViolation(String constraintId,
                              String constraintDescription,
                              MetaData sourceMetaData,
                              Object violatingValue,
                              String violationMessage,
                              ValidationContext context) {
        this.constraintId = constraintId;
        this.constraintDescription = constraintDescription;
        this.sourceMetaData = sourceMetaData;
        this.violatingValue = violatingValue;
        this.violationMessage = violationMessage;
        this.context = context;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Get the unique identifier of the violated constraint
     * @return The constraint ID
     */
    public String getConstraintId() {
        return constraintId;
    }

    /**
     * Get the description of the violated constraint
     * @return The constraint description
     */
    public String getConstraintDescription() {
        return constraintDescription;
    }

    /**
     * Get the MetaData where the violation occurred
     * @return The source MetaData
     */
    public MetaData getSourceMetaData() {
        return sourceMetaData;
    }

    /**
     * Get the value that caused the violation
     * @return The violating value (may be null for structural violations)
     */
    public Object getViolatingValue() {
        return violatingValue;
    }

    /**
     * Get the specific violation message
     * @return The violation message
     */
    public String getViolationMessage() {
        return violationMessage;
    }

    /**
     * Get the validation context
     * @return The validation context
     */
    public ValidationContext getContext() {
        return context;
    }

    /**
     * Get the timestamp when the violation occurred
     * @return The violation timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get the severity level of this violation
     * @return The violation severity
     */
    public ViolationSeverity getSeverity() {
        // Determine severity based on constraint type and context
        if (constraintId.contains("required") || constraintId.contains("foreignkey")) {
            return ViolationSeverity.ERROR;
        } else if (constraintId.contains("warning") || constraintId.contains("deprecated")) {
            return ViolationSeverity.WARNING;
        } else {
            return ViolationSeverity.INFO;
        }
    }

    /**
     * Get a formatted error message suitable for logging or display
     * @return Formatted error message
     */
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();

        sb.append("[").append(getSeverity()).append("] ");
        sb.append("Constraint '").append(constraintId).append("' violated");

        if (sourceMetaData != null) {
            sb.append(" at ").append(sourceMetaData.getName());
            if (sourceMetaData.getParent() != null) {
                sb.append(" (in ").append(sourceMetaData.getParent().getName()).append(")");
            }
        }

        sb.append(": ").append(violationMessage);

        if (violatingValue != null) {
            sb.append(" [Value: ").append(violatingValue).append("]");
        }

        return sb.toString();
    }

    /**
     * Get the path to the violation in the metadata hierarchy
     * @return Hierarchical path string
     */
    public String getMetaDataPath() {
        if (sourceMetaData == null) {
            return "unknown";
        }

        StringBuilder path = new StringBuilder();
        MetaData current = sourceMetaData;

        while (current != null) {
            if (path.length() > 0) {
                path.insert(0, " -> ");
            }
            path.insert(0, current.getName());
            current = current.getParent();
        }

        return path.toString();
    }

    /**
     * Check if this violation should block operation
     * @return True if this is a blocking violation
     */
    public boolean isBlocking() {
        return getSeverity() == ViolationSeverity.ERROR;
    }

    @Override
    public String toString() {
        return getFormattedMessage();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ConstraintViolation that = (ConstraintViolation) obj;

        return constraintId.equals(that.constraintId) &&
               sourceMetaData.equals(that.sourceMetaData) &&
               (violatingValue != null ? violatingValue.equals(that.violatingValue) : that.violatingValue == null);
    }

    @Override
    public int hashCode() {
        int result = constraintId.hashCode();
        result = 31 * result + sourceMetaData.hashCode();
        result = 31 * result + (violatingValue != null ? violatingValue.hashCode() : 0);
        return result;
    }

    /**
     * Enumeration of violation severity levels
     */
    public enum ViolationSeverity {
        /**
         * Informational violation - does not prevent operation
         */
        INFO,

        /**
         * Warning violation - operation can continue but should be reviewed
         */
        WARNING,

        /**
         * Error violation - operation should be blocked
         */
        ERROR
    }
}