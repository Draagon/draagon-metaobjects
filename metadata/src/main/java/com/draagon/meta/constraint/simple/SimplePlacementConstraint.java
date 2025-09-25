package com.draagon.meta.constraint.simple;

import com.draagon.meta.MetaData;
import com.draagon.meta.constraint.Constraint;
import com.draagon.meta.constraint.ConstraintViolationException;

/**
 * Simple placement constraint that's easy to serialize to schemas.
 * Uses string patterns instead of functional predicates to define where
 * metadata can be placed in the hierarchy.
 *
 * Pattern syntax:
 * - "field.*" - any field subtype
 * - "object.pojo" - specific object.pojo type
 * - "attr.string[maxLength]" - specific attr.string with name "maxLength"
 * - "*" - matches anything
 *
 * Example usage:
 * - Allow maxLength on string fields:
 *   new SimplePlacementConstraint("field.string.maxLength", "String fields can have maxLength",
 *                               "field.string", "attr.int[maxLength]", true)
 * - Forbid script tags on string fields:
 *   new SimplePlacementConstraint("field.string.script", "String fields cannot contain script",
 *                               "field.string", "attr.*[script]", false)
 */
public class SimplePlacementConstraint implements Constraint {

    private final String constraintId;
    private final String description;
    private final String parentPattern;
    private final String childPattern;
    private final boolean allowed;

    /**
     * Create a placement constraint
     * @param constraintId Unique identifier
     * @param description Human-readable description
     * @param parentPattern Pattern for parent type (e.g., "field.string", "object.*")
     * @param childPattern Pattern for child type (e.g., "attr.int[maxLength]", "field.*")
     * @param allowed Whether this placement is allowed (true) or forbidden (false)
     */
    public SimplePlacementConstraint(String constraintId, String description,
                                   String parentPattern, String childPattern, boolean allowed) {
        this.constraintId = constraintId;
        this.description = description;
        this.parentPattern = parentPattern != null ? parentPattern : "*";
        this.childPattern = childPattern != null ? childPattern : "*";
        this.allowed = allowed;
    }

    /**
     * Check if this constraint applies to a parent-child relationship
     * @param parent The parent MetaData
     * @param child The child MetaData
     * @return True if this constraint should be checked
     */
    public boolean appliesTo(MetaData parent, MetaData child) {
        return matchesPattern(parent, parentPattern) && matchesPattern(child, childPattern);
    }

    /**
     * Check if a MetaData matches a pattern
     * Supported patterns:
     * - "type.*" - any subtype of type
     * - "type.subtype" - specific type.subtype
     * - "type.subtype[name]" - specific type.subtype with specific name
     * - "*" - matches anything
     *
     * @param metaData MetaData to check
     * @param pattern Pattern to match
     * @return True if matches
     */
    private boolean matchesPattern(MetaData metaData, String pattern) {
        if ("*".equals(pattern)) return true;
        if (metaData == null || pattern == null) return false;

        // Extract name constraint if present: "type.subtype[name]"
        String nameConstraint = null;
        String typePattern = pattern;

        if (pattern.contains("[") && pattern.endsWith("]")) {
            int bracketIndex = pattern.indexOf("[");
            typePattern = pattern.substring(0, bracketIndex);
            nameConstraint = pattern.substring(bracketIndex + 1, pattern.length() - 1);
        }

        // Parse type.subtype pattern
        String[] parts = typePattern.split("\\.");
        if (parts.length != 2) {
            return false; // Invalid pattern
        }

        String patternType = parts[0];
        String patternSubType = parts[1];

        // Check type match
        if (!"*".equals(patternType) && !patternType.equals(metaData.getType())) {
            return false;
        }

        // Check subtype match
        if (!"*".equals(patternSubType) && !patternSubType.equals(metaData.getSubType())) {
            return false;
        }

        // Check name constraint if present
        if (nameConstraint != null) {
            if (!"*".equals(nameConstraint) && !nameConstraint.equals(metaData.getName())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void validate(MetaData metaData, Object value) throws ConstraintViolationException {
        // PlacementConstraints are validated during addChild operations, not during value validation
        throw new UnsupportedOperationException(
            "SimplePlacementConstraint validation should be called via appliesTo(), not validate()");
    }

    @Override
    public String getType() {
        return "simple-placement";
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Get the constraint ID
     * @return Unique constraint identifier
     */
    public String getConstraintId() {
        return constraintId;
    }

    /**
     * Get the parent pattern
     * @return Pattern for parent types
     */
    public String getParentPattern() {
        return parentPattern;
    }

    /**
     * Get the child pattern
     * @return Pattern for child types
     */
    public String getChildPattern() {
        return childPattern;
    }

    /**
     * Check if this placement is allowed
     * @return True if allowed, false if forbidden
     */
    public boolean isAllowed() {
        return allowed;
    }

    /**
     * Check if this placement is forbidden
     * @return True if forbidden, false if allowed
     */
    public boolean isForbidden() {
        return !allowed;
    }

    @Override
    public String toString() {
        return "SimplePlacementConstraint{" +
               "id='" + constraintId + '\'' +
               ", parent='" + parentPattern + '\'' +
               ", child='" + childPattern + '\'' +
               ", allowed=" + allowed +
               ", description='" + description + '\'' +
               '}';
    }
}