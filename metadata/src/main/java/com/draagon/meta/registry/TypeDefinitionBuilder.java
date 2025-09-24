package com.draagon.meta.registry;

import com.draagon.meta.MetaData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fluent builder for creating TypeDefinition instances with bidirectional constraint declarations.
 *
 * <p>This builder provides a clean API for defining MetaData types along with their
 * bidirectional constraints, replacing the old ChildRequirement system with unified
 * acceptsChildren/acceptsParents declarations.</p>
 *
 * <h3>Usage Examples:</h3>
 *
 * <pre>{@code
 * // String field with specific attributes
 * TypeDefinitionBuilder.forClass(StringField.class)
 *     .type(TYPE_FIELD).subType(SUBTYPE_STRING)
 *     .description("String field with pattern validation")
 *     .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)
 *     .acceptsNamedChildren(TYPE_ATTR, SUBTYPE_STRING, ATTR_PATTERN)
 *     .acceptsNamedChildren(TYPE_ATTR, SUBTYPE_INT, ATTR_MAX_LENGTH)
 *     .build();
 *
 * // Object type accepting any fields
 * TypeDefinitionBuilder.forClass(MetaObject.class)
 *     .type(TYPE_OBJECT).subType(SUBTYPE_BASE)
 *     .acceptsChildren(TYPE_FIELD, "*")  // Any field type and subtype
 *     .acceptsChildren(TYPE_KEY, "*")
 *     .build();
 * }</pre>
 *
 * @since 6.2.0
 */
public class TypeDefinitionBuilder {

    private final Class<? extends MetaData> implementationClass;
    private String type;
    private String subType;
    private String description;
    private String parentType;
    private String parentSubType;
    private final List<AcceptsChildrenDeclaration> acceptsChildren = new ArrayList<>();
    private final List<AcceptsParentsDeclaration> acceptsParents = new ArrayList<>();
    
    /**
     * Create builder for the specified implementation class
     * 
     * @param implementationClass Java class that implements this MetaData type
     */
    public TypeDefinitionBuilder(Class<? extends MetaData> implementationClass) {
        this.implementationClass = Objects.requireNonNull(implementationClass, "Implementation class cannot be null");
    }
    
    /**
     * Create builder for the specified implementation class (static factory)
     * 
     * @param implementationClass Java class that implements this MetaData type
     * @return New TypeDefinitionBuilder
     */
    public static TypeDefinitionBuilder forClass(Class<? extends MetaData> implementationClass) {
        return new TypeDefinitionBuilder(implementationClass);
    }

    /**
     * Create builder from existing TypeDefinition for extension purposes
     *
     * @param existing Existing TypeDefinition to copy settings from
     * @return New TypeDefinitionBuilder with settings copied from existing definition
     */
    public static TypeDefinitionBuilder from(TypeDefinition existing) {
        TypeDefinitionBuilder builder = new TypeDefinitionBuilder(existing.getImplementationClass());

        // Copy basic properties
        builder.type = existing.getType();
        builder.subType = existing.getSubType();
        builder.description = existing.getDescription();
        builder.parentType = existing.getParentType();
        builder.parentSubType = existing.getParentSubType();

        // Copy accepts children declarations (direct ones only, not inherited)
        builder.acceptsChildren.addAll(existing.getDirectAcceptsChildren());

        // Copy accepts parents declarations (direct ones only, not inherited)
        builder.acceptsParents.addAll(existing.getDirectAcceptsParents());

        return builder;
    }

    /**
     * Set the primary type identifier
     * 
     * @param type Primary type like "field", "object", "attr"
     * @return This builder for method chaining
     */
    public TypeDefinitionBuilder type(String type) {
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        return this;
    }
    
    /**
     * Set the specific subtype identifier
     * 
     * @param subType Specific subtype like "string", "int", "base"
     * @return This builder for method chaining
     */
    public TypeDefinitionBuilder subType(String subType) {
        this.subType = Objects.requireNonNull(subType, "SubType cannot be null");
        return this;
    }
    
    /**
     * Set the human-readable description
     *
     * @param description Description of this type
     * @return This builder for method chaining
     */
    public TypeDefinitionBuilder description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Specify parent type for inheritance
     *
     * @param parentType Parent type (e.g., "field", "object")
     * @param parentSubType Parent subType (e.g., "base", "string")
     * @return This builder for method chaining
     */
    public TypeDefinitionBuilder inheritsFrom(String parentType, String parentSubType) {
        this.parentType = Objects.requireNonNull(parentType, "Parent type cannot be null");
        this.parentSubType = Objects.requireNonNull(parentSubType, "Parent subType cannot be null");
        return this;
    }

    /**
     * Convenience method to inherit from base field type
     *
     * @return This builder for method chaining
     */
    public TypeDefinitionBuilder inheritsFromBaseField() {
        return inheritsFrom("field", "base");
    }

    /**
     * Convenience method to inherit from base object type
     *
     * @return This builder for method chaining
     */
    public TypeDefinitionBuilder inheritsFromBaseObject() {
        return inheritsFrom("object", "base");
    }

    /**
     * Declare that this type accepts children of the specified type and subtype (any name)
     *
     * @param childType Expected child type (e.g., TYPE_FIELD, TYPE_ATTR)
     * @param childSubType Expected child subType (e.g., SUBTYPE_STRING, SUBTYPE_INT) or "*" for any
     * @return This builder for method chaining
     */
    public TypeDefinitionBuilder acceptsChildren(String childType, String childSubType) {
        acceptsChildren.add(new AcceptsChildrenDeclaration(childType, childSubType, null));
        return this;
    }

    /**
     * Declare that this type accepts children of the specified type, subtype, and specific name
     *
     * @param childType Expected child type (e.g., TYPE_FIELD, TYPE_ATTR)
     * @param childSubType Expected child subType (e.g., SUBTYPE_STRING, SUBTYPE_INT)
     * @param childName Expected specific child name (e.g., ATTR_MAX_LENGTH)
     * @return This builder for method chaining
     */
    public TypeDefinitionBuilder acceptsNamedChildren(String childType, String childSubType, String childName) {
        acceptsChildren.add(new AcceptsChildrenDeclaration(childType, childSubType, childName));
        return this;
    }

    /**
     * Declare that this type can be placed under parents of the specified type and subtype (any name)
     *
     * @param parentType Expected parent type (e.g., TYPE_FIELD, TYPE_OBJECT)
     * @param parentSubType Expected parent subType (e.g., SUBTYPE_STRING, SUBTYPE_BASE) or "*" for any
     * @return This builder for method chaining
     */
    public TypeDefinitionBuilder acceptsParents(String parentType, String parentSubType) {
        acceptsParents.add(new AcceptsParentsDeclaration(parentType, parentSubType, null));
        return this;
    }

    /**
     * Declare that this type can be placed under parents of the specified type and subtype when named specifically
     *
     * @param parentType Expected parent type (e.g., TYPE_FIELD, TYPE_OBJECT)
     * @param parentSubType Expected parent subType (e.g., SUBTYPE_STRING, SUBTYPE_BASE)
     * @param expectedChildName The name this child expects to have when placed under this parent
     * @return This builder for method chaining
     */
    public TypeDefinitionBuilder acceptsNamedParents(String parentType, String parentSubType, String expectedChildName) {
        acceptsParents.add(new AcceptsParentsDeclaration(parentType, parentSubType, expectedChildName));
        return this;
    }

    /**
     * Convenience method for attribute children (any name)
     *
     * @param attrSubType Attribute subType (e.g., SUBTYPE_STRING, SUBTYPE_BOOLEAN, SUBTYPE_INT)
     * @return This builder for method chaining
     */
    public TypeDefinitionBuilder acceptsAttributes(String attrSubType) {
        return acceptsChildren("attr", attrSubType);
    }

    /**
     * Convenience method for named attribute children
     *
     * @param attrSubType Attribute subType (e.g., SUBTYPE_STRING, SUBTYPE_BOOLEAN, SUBTYPE_INT)
     * @param attrName Specific attribute name (e.g., ATTR_MAX_LENGTH, ATTR_PATTERN)
     * @return This builder for method chaining
     */
    public TypeDefinitionBuilder acceptsNamedAttributes(String attrSubType, String attrName) {
        return acceptsNamedChildren("attr", attrSubType, attrName);
    }

    // ======================================
    // BACKWARD COMPATIBILITY BRIDGE METHODS
    // ======================================

    /**
     * @deprecated Use acceptsChildren(childType, childSubType) instead
     */
    @Deprecated
    public TypeDefinitionBuilder optionalChild(String childType, String childSubType) {
        return acceptsChildren(childType, childSubType);
    }

    /**
     * @deprecated Use acceptsNamedChildren(childType, childSubType, childName) instead
     */
    @Deprecated
    public TypeDefinitionBuilder optionalChild(String childType, String childSubType, String childName) {
        if ("*".equals(childName)) {
            return acceptsChildren(childType, childSubType);
        } else {
            return acceptsNamedChildren(childType, childSubType, childName);
        }
    }

    /**
     * @deprecated Use acceptsNamedChildren(childType, childSubType, childName) instead
     */
    @Deprecated
    public TypeDefinitionBuilder requiredChild(String childType, String childSubType, String childName) {
        // Note: New system doesn't distinguish required vs optional at registration time
        return optionalChild(childType, childSubType, childName);
    }

    // Deprecated methods removed - use acceptsNamedAttributes(attrSubType, attrName) instead

    /**
     * @deprecated Not needed in new bidirectional constraint system
     */
    @Deprecated
    public TypeDefinitionBuilder childRequirement(ChildRequirement requirement) {
        // Convert ChildRequirement to new API
        String name = "*".equals(requirement.getName()) ? null : requirement.getName();
        if (name != null) {
            return acceptsNamedChildren(requirement.getExpectedType(), requirement.getExpectedSubType(), name);
        } else {
            return acceptsChildren(requirement.getExpectedType(), requirement.getExpectedSubType());
        }
    }
    
    /**
     * Build the immutable TypeDefinition
     *
     * @return New TypeDefinition instance
     * @throws IllegalStateException if required fields are not set
     */
    public TypeDefinition build() {
        if (type == null) {
            throw new IllegalStateException("Type must be set");
        }
        if (subType == null) {
            throw new IllegalStateException("SubType must be set");
        }

        return new TypeDefinition(implementationClass, type, subType, description,
                                 acceptsChildren, acceptsParents, parentType, parentSubType);
    }
    
    /**
     * Get the current type being built (for debugging)
     * 
     * @return Current type or null if not set
     */
    public String getType() {
        return type;
    }
    
    /**
     * Get the current subType being built (for debugging)
     * 
     * @return Current subType or null if not set
     */
    public String getSubType() {
        return subType;
    }
    
    /**
     * Get the number of child declarations currently defined
     *
     * @return Count of accepts children declarations
     */
    public int getAcceptsChildrenCount() {
        return acceptsChildren.size();
    }

    /**
     * Get the number of parent declarations currently defined
     *
     * @return Count of accepts parents declarations
     */
    public int getAcceptsParentsCount() {
        return acceptsParents.size();
    }
    
    @Override
    public String toString() {
        String typeName = (type != null && subType != null) ? type + "." + subType : "undefined";
        return String.format("TypeDefinitionBuilder[%s -> %s, acceptsChildren=%d, acceptsParents=%d]",
            typeName, implementationClass.getSimpleName(), acceptsChildren.size(), acceptsParents.size());
    }
}