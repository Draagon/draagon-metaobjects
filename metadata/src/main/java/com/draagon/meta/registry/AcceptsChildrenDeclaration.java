package com.draagon.meta.registry;

import java.util.Objects;

/**
 * Declaration that a MetaData type accepts certain children.
 *
 * This class represents a parent type declaring: "I accept children of type X, subtype Y, optionally named Z"
 *
 * Part of the bidirectional constraint system where both parent and child must agree on placement.
 *
 * @since 6.2.0
 */
public class AcceptsChildrenDeclaration {

    private final String childType;
    private final String childSubType;
    private final String childName; // null means any name

    /**
     * Create a declaration that this type accepts children
     *
     * @param childType Expected child type (e.g., "field", "attr", "validator")
     * @param childSubType Expected child subType (e.g., "string", "int") or "*" for any
     * @param childName Expected child name or null for any name
     */
    public AcceptsChildrenDeclaration(String childType, String childSubType, String childName) {
        this.childType = Objects.requireNonNull(childType, "Child type cannot be null");
        this.childSubType = Objects.requireNonNull(childSubType, "Child subType cannot be null");
        this.childName = childName; // Can be null for "any name"
    }

    /**
     * Get the expected child type
     *
     * @return Child type like "field", "attr", "validator"
     */
    public String getChildType() {
        return childType;
    }

    /**
     * Get the expected child subType
     *
     * @return Child subType like "string", "int", or "*" for any
     */
    public String getChildSubType() {
        return childSubType;
    }

    /**
     * Get the expected child name
     *
     * @return Child name or null if any name is accepted
     */
    public String getChildName() {
        return childName;
    }

    /**
     * Check if this declaration accepts any name
     *
     * @return true if childName is null (any name accepted)
     */
    public boolean acceptsAnyName() {
        return childName == null;
    }

    /**
     * Check if this declaration accepts any subType
     *
     * @return true if childSubType is "*"
     */
    public boolean acceptsAnySubType() {
        return "*".equals(childSubType);
    }

    /**
     * Check if this declaration matches a child with given type, subType, and name
     *
     * @param actualChildType Actual child type
     * @param actualChildSubType Actual child subType
     * @param actualChildName Actual child name
     * @return true if this declaration would accept the child
     */
    public boolean matches(String actualChildType, String actualChildSubType, String actualChildName) {
        // Type must match exactly
        if (!childType.equals(actualChildType)) {
            return false;
        }

        // SubType must match or be wildcard
        if (!"*".equals(childSubType) && !childSubType.equals(actualChildSubType)) {
            return false;
        }

        // Name must match or be unspecified
        if (childName != null && !childName.equals(actualChildName)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AcceptsChildrenDeclaration that = (AcceptsChildrenDeclaration) o;
        return Objects.equals(childType, that.childType) &&
               Objects.equals(childSubType, that.childSubType) &&
               Objects.equals(childName, that.childName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(childType, childSubType, childName);
    }

    @Override
    public String toString() {
        String nameStr = childName != null ? ":" + childName : "";
        return String.format("accepts[%s.%s%s]", childType, childSubType, nameStr);
    }
}