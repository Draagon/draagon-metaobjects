package com.draagon.meta.constraint.simple;

import com.draagon.meta.MetaData;
import com.draagon.meta.constraint.ConstraintViolationException;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Simple regex validation constraint that's easy to serialize to schemas.
 *
 * Example usage:
 * - Field naming: new SimpleRegexConstraint("field.naming", "Field names must be identifiers",
 *                                          "field", "*", "*", "^[a-zA-Z][a-zA-Z0-9_]*$")
 * - Email validation: new SimpleRegexConstraint("email.format", "Must be valid email",
 *                                               "field", "string", "email", "^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$")
 */
public class SimpleRegexConstraint extends SimpleConstraint {

    private final String regexPattern;
    private final Pattern compiledPattern;
    private final boolean allowNull;

    /**
     * Create a regex constraint that doesn't allow null values
     * @param constraintId Unique identifier
     * @param description Human-readable description
     * @param targetType Target type pattern
     * @param targetSubType Target subtype pattern
     * @param targetName Target name pattern
     * @param regexPattern Regular expression pattern to validate
     */
    public SimpleRegexConstraint(String constraintId, String description,
                               String targetType, String targetSubType, String targetName,
                               String regexPattern) {
        this(constraintId, description, targetType, targetSubType, targetName, regexPattern, false);
    }

    /**
     * Create a regex constraint with null handling option
     * @param constraintId Unique identifier
     * @param description Human-readable description
     * @param targetType Target type pattern
     * @param targetSubType Target subtype pattern
     * @param targetName Target name pattern
     * @param regexPattern Regular expression pattern to validate
     * @param allowNull Whether null values are allowed
     */
    public SimpleRegexConstraint(String constraintId, String description,
                               String targetType, String targetSubType, String targetName,
                               String regexPattern, boolean allowNull) {
        super(constraintId, description, targetType, targetSubType, targetName);
        this.regexPattern = regexPattern;
        this.allowNull = allowNull;

        try {
            this.compiledPattern = Pattern.compile(regexPattern);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid regex pattern: " + regexPattern, e);
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
        if (!compiledPattern.matcher(stringValue).matches()) {
            throw new ConstraintViolationException(
                String.format("Value '%s' for %s '%s' does not match required pattern: %s",
                    stringValue, metaData.getType(), metaData.getName(), regexPattern),
                constraintId,
                metaData
            );
        }
    }

    @Override
    public String getType() {
        return "simple-regex";
    }

    /**
     * Get the regex pattern
     * @return Regular expression pattern
     */
    public String getRegexPattern() {
        return regexPattern;
    }

    /**
     * Check if null values are allowed
     * @return True if null values are allowed
     */
    public boolean isAllowNull() {
        return allowNull;
    }

    /**
     * Get the compiled Pattern object
     * @return Compiled regex pattern
     */
    public Pattern getCompiledPattern() {
        return compiledPattern;
    }

    @Override
    public String toString() {
        return "SimpleRegexConstraint{" +
               "id='" + constraintId + '\'' +
               ", pattern='" + regexPattern + '\'' +
               ", target=" + getTargetDescription() +
               ", allowNull=" + allowNull +
               '}';
    }
}