package com.draagon.meta.registry;

import java.util.Objects;

/**
 * Declaration that a MetaData type can be placed under certain parents.
 *
 * This class represents a child type declaring: "I can be placed under parent type X, subtype Y, when I'm named Z"
 *
 * Part of the bidirectional constraint system where both parent and child must agree on placement.
 *
 * @since 6.2.0
 */
public class AcceptsParentsDeclaration {

    private final String parentType;
    private final String parentSubType;
    private final String expectedChildName; // null means any name

    /**
     * Create a declaration that this type can be placed under certain parents
     *
     * @param parentType Expected parent type (e.g., "field", "object", "loader")
     * @param parentSubType Expected parent subType (e.g., "string", "base") or "*" for any
     * @param expectedChildName The name this child expects to have, or null for any name
     */
    public AcceptsParentsDeclaration(String parentType, String parentSubType, String expectedChildName) {
        this.parentType = Objects.requireNonNull(parentType, "Parent type cannot be null");
        this.parentSubType = Objects.requireNonNull(parentSubType, "Parent subType cannot be null");
        this.expectedChildName = expectedChildName; // Can be null for "any name"
    }

    /**
     * Get the expected parent type
     *
     * @return Parent type like "field", "object", "loader"
     */
    public String getParentType() {
        return parentType;
    }

    /**
     * Get the expected parent subType
     *
     * @return Parent subType like "string", "base", or "*" for any
     */
    public String getParentSubType() {
        return parentSubType;
    }

    /**
     * Get the expected child name when placed under this parent
     *
     * @return Expected child name or null if any name is acceptable
     */
    public String getExpectedChildName() {
        return expectedChildName;
    }

    /**
     * Check if this declaration accepts any child name
     *
     * @return true if expectedChildName is null (any name accepted)
     */
    public boolean acceptsAnyChildName() {
        return expectedChildName == null;
    }

    /**
     * Check if this declaration accepts any parent subType
     *
     * @return true if parentSubType is "*"
     */
    public boolean acceptsAnyParentSubType() {
        return "*".equals(parentSubType);
    }

    /**
     * Check if this declaration matches a parent with given type, subType, and proposed child name
     *
     * @param actualParentType Actual parent type
     * @param actualParentSubType Actual parent subType
     * @param proposedChildName Proposed name for this child
     * @return true if this declaration would accept placement under this parent
     */
    public boolean matches(String actualParentType, String actualParentSubType, String proposedChildName) {
        // Parent type must match exactly
        if (!parentType.equals(actualParentType)) {
            return false;
        }

        // Parent subType must match or be wildcard
        if (!"*".equals(parentSubType) && !parentSubType.equals(actualParentSubType)) {
            return false;
        }

        // Child name must match expected name if specified
        if (expectedChildName != null && !expectedChildName.equals(proposedChildName)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AcceptsParentsDeclaration that = (AcceptsParentsDeclaration) o;
        return Objects.equals(parentType, that.parentType) &&
               Objects.equals(parentSubType, that.parentSubType) &&
               Objects.equals(expectedChildName, that.expectedChildName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentType, parentSubType, expectedChildName);
    }

    @Override
    public String toString() {
        String nameStr = expectedChildName != null ? " as:" + expectedChildName : "";
        return String.format("acceptsParent[%s.%s%s]", parentType, parentSubType, nameStr);
    }
}