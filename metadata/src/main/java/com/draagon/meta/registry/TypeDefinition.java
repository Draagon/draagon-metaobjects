package com.draagon.meta.registry;

import com.draagon.meta.MetaData;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Immutable definition of a MetaData type including its implementation class,
 * identity information, and bidirectional constraint declarations.
 *
 * <p>This replaces the old ChildRequirement system with bidirectional constraints
 * where types declare what children they accept and what parents they accept.</p>
 *
 * @since 6.2.0
 */
public class TypeDefinition {

    private final Class<? extends MetaData> implementationClass;
    private final String type;
    private final String subType;
    private final String description;
    private final String parentType;
    private final String parentSubType;
    private final List<AcceptsChildrenDeclaration> acceptsChildren;
    private final List<AcceptsParentsDeclaration> acceptsParents;
    private final List<AcceptsChildrenDeclaration> inheritedAcceptsChildren;
    private final List<AcceptsParentsDeclaration> inheritedAcceptsParents;

    /**
     * Create a type definition with bidirectional constraints and optional inheritance
     *
     * @param implementationClass Java class that implements this type
     * @param type Primary type identifier (e.g., "field", "object")
     * @param subType Specific subtype identifier (e.g., "string", "base")
     * @param description Human-readable description
     * @param acceptsChildren List of children this type accepts
     * @param acceptsParents List of parents this type can be placed under
     * @param parentType Parent type for inheritance (can be null)
     * @param parentSubType Parent subType for inheritance (can be null)
     */
    public TypeDefinition(Class<? extends MetaData> implementationClass,
                         String type,
                         String subType,
                         String description,
                         List<AcceptsChildrenDeclaration> acceptsChildren,
                         List<AcceptsParentsDeclaration> acceptsParents,
                         String parentType,
                         String parentSubType) {
        this.implementationClass = Objects.requireNonNull(implementationClass, "Implementation class cannot be null");
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.subType = Objects.requireNonNull(subType, "SubType cannot be null");
        this.description = description != null ? description : "";
        this.parentType = parentType;
        this.parentSubType = parentSubType;
        this.acceptsChildren = acceptsChildren != null ? List.copyOf(acceptsChildren) : List.of();
        this.acceptsParents = acceptsParents != null ? List.copyOf(acceptsParents) : List.of();

        // Inherited constraints (will be populated later during registry resolution)
        this.inheritedAcceptsChildren = Collections.synchronizedList(new ArrayList<>());
        this.inheritedAcceptsParents = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Create a type definition without inheritance (convenience constructor)
     *
     * @param implementationClass Java class that implements this type
     * @param type Primary type identifier (e.g., "field", "object")
     * @param subType Specific subtype identifier (e.g., "string", "base")
     * @param description Human-readable description
     * @param acceptsChildren List of children this type accepts
     * @param acceptsParents List of parents this type can be placed under
     */
    public TypeDefinition(Class<? extends MetaData> implementationClass,
                         String type,
                         String subType,
                         String description,
                         List<AcceptsChildrenDeclaration> acceptsChildren,
                         List<AcceptsParentsDeclaration> acceptsParents) {
        this(implementationClass, type, subType, description, acceptsChildren, acceptsParents, null, null);
    }

    /**
     * Get the implementation class for this type
     *
     * @return Java class that implements this MetaData type
     */
    public Class<? extends MetaData> getImplementationClass() {
        return implementationClass;
    }

    /**
     * Get the primary type identifier
     *
     * @return Type identifier like "field", "object", "attr"
     */
    public String getType() {
        return type;
    }

    /**
     * Get the specific subtype identifier
     *
     * @return SubType identifier like "string", "int", "base"
     */
    public String getSubType() {
        return subType;
    }

    /**
     * Get the human-readable description
     *
     * @return Description of this type
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get qualified type name for display and logging
     *
     * @return Qualified name like "field.string" or "object.base"
     */
    public String getQualifiedName() {
        return type + "." + subType;
    }

    /**
     * Check if this type has a parent type for inheritance
     *
     * @return true if this type inherits from a parent type
     */
    public boolean hasParent() {
        return parentType != null && parentSubType != null;
    }

    /**
     * Get the parent type for inheritance
     *
     * @return Parent type identifier or null if no inheritance
     */
    public String getParentType() {
        return parentType;
    }

    /**
     * Get the parent subType for inheritance
     *
     * @return Parent subType identifier or null if no inheritance
     */
    public String getParentSubType() {
        return parentSubType;
    }

    /**
     * Get the qualified parent type name
     *
     * @return Qualified parent name like "field.base" or null if no inheritance
     */
    public String getParentQualifiedName() {
        return hasParent() ? parentType + "." + parentSubType : null;
    }

    /**
     * Get all children declarations for this type (including inherited)
     *
     * @return List of all accepts children declarations (direct + inherited)
     */
    public List<AcceptsChildrenDeclaration> getAcceptsChildren() {
        List<AcceptsChildrenDeclaration> all = new ArrayList<>(acceptsChildren);
        all.addAll(inheritedAcceptsChildren);
        return Collections.unmodifiableList(all);
    }

    /**
     * Get direct children declarations (excluding inherited)
     *
     * @return List of direct accepts children declarations only
     */
    public List<AcceptsChildrenDeclaration> getDirectAcceptsChildren() {
        return Collections.unmodifiableList(acceptsChildren);
    }

    /**
     * Get inherited children declarations
     *
     * @return List of inherited accepts children declarations
     */
    public List<AcceptsChildrenDeclaration> getInheritedAcceptsChildren() {
        return Collections.unmodifiableList(inheritedAcceptsChildren);
    }

    /**
     * Get direct parents declarations (excluding inherited)
     *
     * @return List of direct accepts parents declarations only
     */
    public List<AcceptsParentsDeclaration> getDirectAcceptsParents() {
        return Collections.unmodifiableList(acceptsParents);
    }

    /**
     * Get inherited parents declarations
     *
     * @return List of inherited accepts parents declarations
     */
    public List<AcceptsParentsDeclaration> getInheritedAcceptsParents() {
        return Collections.unmodifiableList(inheritedAcceptsParents);
    }

    /**
     * Get all parent declarations for this type (including inherited)
     *
     * @return List of all accepts parents declarations (direct + inherited)
     */
    public List<AcceptsParentsDeclaration> getAcceptsParents() {
        List<AcceptsParentsDeclaration> all = new ArrayList<>(acceptsParents);
        all.addAll(inheritedAcceptsParents);
        return Collections.unmodifiableList(all);
    }

    /**
     * Check if a child is acceptable for this type based on bidirectional constraints
     *
     * @param childType Actual child type
     * @param childSubType Actual child subType
     * @param childName Actual child name
     * @return true if this type accepts the specified child
     */
    public boolean acceptsChild(String childType, String childSubType, String childName) {
        // Check direct declarations first
        for (AcceptsChildrenDeclaration declaration : acceptsChildren) {
            if (declaration.matches(childType, childSubType, childName)) {
                return true;
            }
        }

        // Check inherited declarations
        for (AcceptsChildrenDeclaration declaration : inheritedAcceptsChildren) {
            if (declaration.matches(childType, childSubType, childName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if this type can be placed under a specific parent
     *
     * @param parentType Actual parent type
     * @param parentSubType Actual parent subType
     * @param proposedChildName Proposed name for this child
     * @return true if this type can be placed under the specified parent
     */
    public boolean acceptsParent(String parentType, String parentSubType, String proposedChildName) {
        // Check direct declarations first
        for (AcceptsParentsDeclaration declaration : acceptsParents) {
            if (declaration.matches(parentType, parentSubType, proposedChildName)) {
                return true;
            }
        }

        // Check inherited declarations
        for (AcceptsParentsDeclaration declaration : inheritedAcceptsParents) {
            if (declaration.matches(parentType, parentSubType, proposedChildName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get human-readable description of supported children for error messages
     *
     * @return Description like "Accepts: attr.string (any name), attr.int:maxLength"
     */
    public String getSupportedChildrenDescription() {
        List<AcceptsChildrenDeclaration> allChildren = getAcceptsChildren();
        if (allChildren.isEmpty()) {
            return "No children accepted";
        }

        StringBuilder desc = new StringBuilder("Accepts: ");
        List<String> parts = new ArrayList<>();

        for (AcceptsChildrenDeclaration declaration : allChildren) {
            String typeInfo = declaration.getChildType() + "." + declaration.getChildSubType();
            if (declaration.getChildName() != null) {
                typeInfo += ":" + declaration.getChildName();
            } else {
                typeInfo += " (any name)";
            }
            parts.add(typeInfo);
        }

        desc.append(String.join(", ", parts));
        return desc.toString();
    }

    /**
     * Get human-readable description of acceptable parents for error messages
     *
     * @return Description like "Can be placed under: field.string, object.*"
     */
    public String getAcceptableParentsDescription() {
        if (acceptsParents.isEmpty()) {
            return "No parent restrictions";
        }

        StringBuilder desc = new StringBuilder("Can be placed under: ");
        List<String> parts = new ArrayList<>();

        for (AcceptsParentsDeclaration declaration : acceptsParents) {
            String typeInfo = declaration.getParentType() + "." + declaration.getParentSubType();
            if (declaration.getExpectedChildName() != null) {
                typeInfo += " as:" + declaration.getExpectedChildName();
            }
            parts.add(typeInfo);
        }

        desc.append(String.join(", ", parts));
        return desc.toString();
    }

    /**
     * Internal method to populate inherited children declarations.
     * This is called by MetaDataRegistry during type registration to resolve inheritance chains.
     *
     * @param parentAcceptsChildren Children declarations from the parent type
     */
    void populateInheritedConstraints(List<AcceptsChildrenDeclaration> parentAcceptsChildren) {
        if (parentAcceptsChildren != null) {
            // Clear any existing inherited constraints
            inheritedAcceptsChildren.clear();

            // Add all parent constraints that are not overridden by direct constraints
            for (AcceptsChildrenDeclaration parentDeclaration : parentAcceptsChildren) {
                if (!isOverriddenByDirectDeclaration(parentDeclaration)) {
                    inheritedAcceptsChildren.add(parentDeclaration);
                }
            }
        }
    }

    /**
     * Check if a parent declaration is overridden by any direct declaration
     */
    private boolean isOverriddenByDirectDeclaration(AcceptsChildrenDeclaration parentDeclaration) {
        for (AcceptsChildrenDeclaration directDeclaration : acceptsChildren) {
            // Simple override logic: same type and subtype means override
            if (directDeclaration.getChildType().equals(parentDeclaration.getChildType()) &&
                directDeclaration.getChildSubType().equals(parentDeclaration.getChildSubType())) {

                // If names are both specific, they must match to be an override
                if (directDeclaration.getChildName() != null && parentDeclaration.getChildName() != null) {
                    return directDeclaration.getChildName().equals(parentDeclaration.getChildName());
                }

                // If either is unnamed, consider it an override
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        TypeDefinition that = (TypeDefinition) obj;
        return implementationClass.equals(that.implementationClass) &&
               type.equals(that.type) &&
               subType.equals(that.subType) &&
               description.equals(that.description) &&
               acceptsChildren.equals(that.acceptsChildren) &&
               acceptsParents.equals(that.acceptsParents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(implementationClass, type, subType, description,
                          acceptsChildren, acceptsParents);
    }

    // ======================================
    // BACKWARD COMPATIBILITY BRIDGE METHODS
    // ======================================

    /**
     * Bridge method for backward compatibility.
     * Converts new AcceptsChildrenDeclaration to old ChildRequirement format.
     *
     * @deprecated Use getAcceptsChildren() instead
     */
    @Deprecated
    public List<ChildRequirement> getChildRequirements() {
        List<ChildRequirement> bridgeRequirements = new ArrayList<>();

        // Convert direct accepts children
        for (AcceptsChildrenDeclaration declaration : acceptsChildren) {
            ChildRequirement bridgeReq = convertToChildRequirement(declaration);
            bridgeRequirements.add(bridgeReq);
        }

        // Convert inherited accepts children
        for (AcceptsChildrenDeclaration declaration : inheritedAcceptsChildren) {
            ChildRequirement bridgeReq = convertToChildRequirement(declaration);
            bridgeRequirements.add(bridgeReq);
        }

        return bridgeRequirements;
    }

    /**
     * Bridge method for backward compatibility.
     *
     * @deprecated Use getDirectAcceptsChildren() instead
     */
    @Deprecated
    public List<ChildRequirement> getDirectChildRequirements() {
        return acceptsChildren.stream()
                .map(this::convertToChildRequirement)
                .collect(Collectors.toList());
    }

    /**
     * Bridge method for backward compatibility.
     *
     * @deprecated Use getInheritedAcceptsChildren() instead
     */
    @Deprecated
    public Map<String, ChildRequirement> getInheritedChildRequirements() {
        Map<String, ChildRequirement> inherited = new HashMap<>();
        for (AcceptsChildrenDeclaration declaration : inheritedAcceptsChildren) {
            ChildRequirement bridgeReq = convertToChildRequirement(declaration);
            String key = bridgeReq.getName() != null ? bridgeReq.getName() : "*:" + bridgeReq.getExpectedType() + ":" + bridgeReq.getExpectedSubType();
            inherited.put(key, bridgeReq);
        }
        return inherited;
    }

    /**
     * Bridge method for backward compatibility.
     *
     * @deprecated Use acceptsChild() instead
     */
    @Deprecated
    public ChildRequirement getChildRequirement(String childName) {
        // Look for a named child acceptance
        for (AcceptsChildrenDeclaration declaration : acceptsChildren) {
            if (childName.equals(declaration.getChildName())) {
                return convertToChildRequirement(declaration);
            }
        }

        // Look for a wildcard acceptance
        for (AcceptsChildrenDeclaration declaration : acceptsChildren) {
            if (declaration.getChildName() == null) {
                return convertToChildRequirement(declaration);
            }
        }

        // Check inherited
        for (AcceptsChildrenDeclaration declaration : inheritedAcceptsChildren) {
            if (childName.equals(declaration.getChildName()) || declaration.getChildName() == null) {
                return convertToChildRequirement(declaration);
            }
        }

        return null;
    }

    /**
     * Bridge method for backward compatibility.
     *
     * @deprecated Use populateInheritedConstraints() instead
     */
    @Deprecated
    void populateInheritedRequirements(Map<String, ChildRequirement> parentRequirements) {
        // Convert ChildRequirements back to AcceptsChildrenDeclarations
        List<AcceptsChildrenDeclaration> parentDeclarations = new ArrayList<>();
        if (parentRequirements != null) {
            for (ChildRequirement req : parentRequirements.values()) {
                AcceptsChildrenDeclaration declaration = convertFromChildRequirement(req);
                parentDeclarations.add(declaration);
            }
        }
        populateInheritedConstraints(parentDeclarations);
    }

    /**
     * Convert AcceptsChildrenDeclaration to ChildRequirement for backward compatibility
     */
    private ChildRequirement convertToChildRequirement(AcceptsChildrenDeclaration declaration) {
        String name = declaration.getChildName() != null ? declaration.getChildName() : "*";
        return ChildRequirement.optional(name, declaration.getChildType(), declaration.getChildSubType());
    }

    /**
     * Convert ChildRequirement to AcceptsChildrenDeclaration
     */
    private AcceptsChildrenDeclaration convertFromChildRequirement(ChildRequirement requirement) {
        String name = "*".equals(requirement.getName()) ? null : requirement.getName();
        return new AcceptsChildrenDeclaration(requirement.getExpectedType(), requirement.getExpectedSubType(), name);
    }

    /**
     * Populate inherited accepts children declarations from parent type
     * Used by inheritance resolution system
     *
     * @param inheritedDeclarations List of accepts children declarations to inherit
     */
    public void populateInheritedAcceptsChildren(List<AcceptsChildrenDeclaration> inheritedDeclarations) {
        this.inheritedAcceptsChildren.clear();
        this.inheritedAcceptsChildren.addAll(inheritedDeclarations);
    }

    /**
     * Populate inherited accepts parents declarations from parent type
     * Used by inheritance resolution system
     *
     * @param inheritedDeclarations List of accepts parents declarations to inherit
     */
    public void populateInheritedAcceptsParents(List<AcceptsParentsDeclaration> inheritedDeclarations) {
        this.inheritedAcceptsParents.clear();
        this.inheritedAcceptsParents.addAll(inheritedDeclarations);
    }

    @Override
    public String toString() {
        return String.format("TypeDefinition[%s -> %s, accepts=%d children, %d parents]",
            getQualifiedName(), implementationClass.getSimpleName(),
            acceptsChildren.size(), acceptsParents.size());
    }
}