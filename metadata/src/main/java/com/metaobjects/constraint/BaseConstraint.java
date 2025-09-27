package com.metaobjects.constraint;

import com.metaobjects.MetaData;

/**
 * Base class for schema-friendly constraints.
 * These constraints use declarative data instead of functional interfaces,
 * making them easy to serialize to XSD, JSON Schema, and AI documentation.
 *
 * Constraints specify their target via string patterns instead of predicates:
 * - targetType: "field", "object", "attr", etc.
 * - targetSubType: "string", "int", "*" (for any subtype)
 * - targetName: "maxLength", "required", "*" (for any name)
 *
 * This allows schema generators to easily extract constraint information
 * without needing to execute functional interface logic.
 */
public abstract class BaseConstraint implements Constraint {

    protected final String constraintId;
    protected final String description;
    protected final String targetType;
    protected final String targetSubType;
    protected final String targetName;

    /**
     * Create a base constraint
     * @param constraintId Unique identifier for this constraint
     * @param description Human-readable description
     * @param targetType MetaData type this applies to ("field", "object", "attr", "*")
     * @param targetSubType MetaData subType this applies to ("string", "int", "*")
     * @param targetName MetaData name this applies to ("maxLength", "required", "*")
     */
    protected BaseConstraint(String constraintId, String description,
                           String targetType, String targetSubType, String targetName) {
        this.constraintId = constraintId;
        this.description = description;
        this.targetType = targetType != null ? targetType : "*";
        this.targetSubType = targetSubType != null ? targetSubType : "*";
        this.targetName = targetName != null ? targetName : "*";
    }

    /**
     * Check if this constraint applies to the given MetaData
     * @param metaData The MetaData to check
     * @return True if this constraint should be applied
     */
    public boolean appliesTo(MetaData metaData) {
        return matchesPattern(metaData.getType(), targetType) &&
               matchesPattern(metaData.getSubType(), targetSubType) &&
               matchesPattern(metaData.getName(), targetName);
    }

    /**
     * Simple pattern matching: exact match or "*" wildcard
     * @param actual The actual value to match
     * @param pattern The pattern ("*" matches anything)
     * @return True if matches
     */
    private boolean matchesPattern(String actual, String pattern) {
        if ("*".equals(pattern)) return true;
        if (actual == null || pattern == null) return false;
        return pattern.equals(actual);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isApplicableTo(String metaDataType) {
        return matchesPattern(metaDataType, targetType);
    }

    /**
     * Get the constraint ID
     * @return Unique constraint identifier
     */
    public String getConstraintId() {
        return constraintId;
    }

    /**
     * Get the target type pattern
     * @return Type pattern (e.g., "field", "object", "*")
     */
    public String getTargetType() {
        return targetType;
    }

    /**
     * Get the target subtype pattern
     * @return SubType pattern (e.g., "string", "int", "*")
     */
    public String getTargetSubType() {
        return targetSubType;
    }

    /**
     * Get the target name pattern
     * @return Name pattern (e.g., "maxLength", "required", "*")
     */
    public String getTargetName() {
        return targetName;
    }

    /**
     * Generate target description for documentation
     * @return Human-readable target description
     */
    public String getTargetDescription() {
        StringBuilder sb = new StringBuilder();
        if (!"*".equals(targetType)) {
            sb.append(targetType);
            if (!"*".equals(targetSubType)) {
                sb.append(".").append(targetSubType);
            }
            if (!"*".equals(targetName)) {
                sb.append("[").append(targetName).append("]");
            }
        } else {
            sb.append("any metadata");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
               "id='" + constraintId + '\'' +
               ", target=" + getTargetDescription() +
               ", description='" + description + '\'' +
               '}';
    }
}