package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;
import com.draagon.meta.attr.*;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;

/**
 * Placement constraint that's easy to serialize to schemas.
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
 *   new PlacementConstraint("field.string.maxLength", "String fields can have maxLength",
 *                          "field", "string", "attr", "int", "maxLength", true)
 * - Forbid script tags on string fields:
 *   new PlacementConstraint("field.string.script", "String fields cannot contain script",
 *                          "field", "string", "attr", "*", "script", false)
 */
public class PlacementConstraint implements Constraint {

    private final String constraintId;
    private final String description;
    private final String parentPattern;
    private final String childPattern;
    private final boolean allowed;

    /**
     * Create a placement constraint using string patterns (legacy constructor)
     * @param constraintId Unique identifier
     * @param description Human-readable description
     * @param parentPattern Pattern for parent type (e.g., "field.string", "object.*")
     * @param childPattern Pattern for child type (e.g., "attr.int[maxLength]", "field.*")
     * @param allowed Whether this placement is allowed (true) or forbidden (false)
     * @deprecated Use the constructor with separate type/subtype/name parameters and proper constants instead
     */
    @Deprecated
    public PlacementConstraint(String constraintId, String description,
                              String parentPattern, String childPattern, boolean allowed) {
        this.constraintId = constraintId;
        this.description = description;
        this.parentPattern = parentPattern != null ? parentPattern : "*";
        this.childPattern = childPattern != null ? childPattern : "*";
        this.allowed = allowed;
    }

    /**
     * Create a placement constraint using separated type/subtype/name parameters (recommended)
     * @param constraintId Unique identifier
     * @param description Human-readable description
     * @param parentType Parent type (use constants like MetaField.TYPE_FIELD)
     * @param parentSubType Parent subtype (use constants like StringField.SUBTYPE_STRING)
     * @param childType Child type (use constants like MetaAttribute.TYPE_ATTR)
     * @param childSubType Child subtype (use constants like StringAttribute.SUBTYPE_STRING)
     * @param childName Child name (use constants like StringField.ATTR_MAX_LENGTH)
     * @param allowed Whether this placement is allowed (true) or forbidden (false)
     */
    public PlacementConstraint(String constraintId, String description,
                              String parentType, String parentSubType,
                              String childType, String childSubType, String childName,
                              boolean allowed) {
        this.constraintId = constraintId;
        this.description = description;
        this.parentPattern = buildPattern(parentType, parentSubType, null);
        this.childPattern = buildPattern(childType, childSubType, childName);
        this.allowed = allowed;
    }

    /**
     * Build a pattern string from separated components
     * @param type Type component (or "*" for wildcard)
     * @param subType SubType component (or "*" for wildcard)
     * @param name Name component (or null for no name constraint)
     * @return Pattern string like "type.subtype[name]"
     */
    private static String buildPattern(String type, String subType, String name) {
        StringBuilder pattern = new StringBuilder();
        pattern.append(type != null ? type : "*");
        pattern.append(".");
        pattern.append(subType != null ? subType : "*");
        if (name != null && !"*".equals(name)) {
            pattern.append("[").append(name).append("]");
        }
        return pattern.toString();
    }

    // === STATIC FACTORY METHODS FOR COMMON PATTERNS ===

    /**
     * Allow an attribute on a specific field type
     * @param constraintId Unique constraint identifier
     * @param description Human-readable description
     * @param parentType Parent field type (use MetaField.TYPE_FIELD)
     * @param parentSubType Parent field subtype (use constants like StringField.SUBTYPE_STRING)
     * @param attributeSubType Attribute subtype (use constants like StringAttribute.SUBTYPE_STRING)
     * @param attributeName Attribute name (use constants like StringField.ATTR_MAX_LENGTH)
     * @return New PlacementConstraint allowing the attribute
     */
    public static PlacementConstraint allowAttribute(String constraintId, String description,
                                                   String parentType, String parentSubType,
                                                   String attributeSubType, String attributeName) {
        return new PlacementConstraint(constraintId, description,
            parentType, parentSubType, MetaAttribute.TYPE_ATTR, attributeSubType, attributeName, true);
    }

    /**
     * Allow an attribute on any field type
     * @param constraintId Unique constraint identifier
     * @param description Human-readable description
     * @param attributeSubType Attribute subtype (use constants like BooleanAttribute.SUBTYPE_BOOLEAN)
     * @param attributeName Attribute name (use constants like MetaField.ATTR_REQUIRED)
     * @return New PlacementConstraint allowing the attribute on any field
     */
    public static PlacementConstraint allowAttributeOnAnyField(String constraintId, String description,
                                                             String attributeSubType, String attributeName) {
        return allowAttribute(constraintId, description, MetaField.TYPE_FIELD, "*", attributeSubType, attributeName);
    }

    /**
     * Allow an attribute on any object type
     * @param constraintId Unique constraint identifier
     * @param description Human-readable description
     * @param attributeSubType Attribute subtype (use constants like StringAttribute.SUBTYPE_STRING)
     * @param attributeName Attribute name (use constants for object attributes)
     * @return New PlacementConstraint allowing the attribute on any object
     */
    public static PlacementConstraint allowAttributeOnAnyObject(String constraintId, String description,
                                                              String attributeSubType, String attributeName) {
        return allowAttribute(constraintId, description, MetaObject.TYPE_OBJECT, "*", attributeSubType, attributeName);
    }

    /**
     * Allow a child type under a parent type (no name constraint)
     * @param constraintId Unique constraint identifier
     * @param description Human-readable description
     * @param parentType Parent type (use constants like MetaObject.TYPE_OBJECT)
     * @param parentSubType Parent subtype (use constants like "*" for any)
     * @param childType Child type (use constants like MetaField.TYPE_FIELD)
     * @param childSubType Child subtype (use constants like "*" for any)
     * @return New PlacementConstraint allowing the child type
     */
    public static PlacementConstraint allowChildType(String constraintId, String description,
                                                   String parentType, String parentSubType,
                                                   String childType, String childSubType) {
        return new PlacementConstraint(constraintId, description,
            parentType, parentSubType, childType, childSubType, null, true);
    }

    /**
     * Forbid an attribute on a specific field type
     * @param constraintId Unique constraint identifier
     * @param description Human-readable description
     * @param parentType Parent field type (use MetaField.TYPE_FIELD)
     * @param parentSubType Parent field subtype (use constants like StringField.SUBTYPE_STRING)
     * @param attributeSubType Attribute subtype (use constants like StringAttribute.SUBTYPE_STRING)
     * @param attributeName Attribute name (use constants like StringField.ATTR_MAX_LENGTH)
     * @return New PlacementConstraint forbidding the attribute
     */
    public static PlacementConstraint forbidAttribute(String constraintId, String description,
                                                    String parentType, String parentSubType,
                                                    String attributeSubType, String attributeName) {
        return new PlacementConstraint(constraintId, description,
            parentType, parentSubType, MetaAttribute.TYPE_ATTR, attributeSubType, attributeName, false);
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
            "PlacementConstraint validation should be called via appliesTo(), not validate()");
    }

    @Override
    public String getType() {
        return "placement";
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
        return "PlacementConstraint{" +
               "id='" + constraintId + '\'' +
               ", parent='" + parentPattern + '\'' +
               ", child='" + childPattern + '\'' +
               ", allowed=" + allowed +
               ", description='" + description + '\'' +
               '}';
    }
}