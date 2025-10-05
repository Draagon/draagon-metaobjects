package com.metaobjects.registry;

import com.metaobjects.MetaData;

import java.util.*;

/**
 * Immutable definition of a MetaData type including its implementation class,
 * identity information, and child requirements.
 * 
 * <p>This replaces the dual registry pattern by integrating type registration
 * and child requirement definitions into a single cohesive structure.</p>
 * 
 * @since 6.0.0
 */
public class TypeDefinition {

    private final Class<? extends MetaData> implementationClass;
    private final String type;
    private final String subType;
    private final String description;
    private final String parentType;
    private final String parentSubType;
    private final Map<String, ChildRequirement> childRequirements;
    private final List<ChildRequirement> wildcardRequirements;
    private final Map<String, ChildRequirement> inheritedChildRequirements;

    /**
     * Create a type definition with child requirements and optional inheritance
     *
     * @param implementationClass Java class that implements this type
     * @param type Primary type identifier (e.g., "field", "object")
     * @param subType Specific subtype identifier (e.g., "string", "base")
     * @param description Human-readable description
     * @param childRequirements Map of child name to requirement (direct requirements)
     * @param parentType Parent type for inheritance (can be null)
     * @param parentSubType Parent subType for inheritance (can be null)
     */
    public TypeDefinition(Class<? extends MetaData> implementationClass,
                         String type,
                         String subType,
                         String description,
                         Map<String, ChildRequirement> childRequirements,
                         String parentType,
                         String parentSubType) {
        this.implementationClass = Objects.requireNonNull(implementationClass, "Implementation class cannot be null");
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.subType = Objects.requireNonNull(subType, "SubType cannot be null");
        this.description = description != null ? description : "";
        this.parentType = parentType;
        this.parentSubType = parentSubType;

        // Separate named requirements from wildcard requirements for efficient lookup
        Map<String, ChildRequirement> named = new HashMap<>();
        List<ChildRequirement> wildcards = new ArrayList<>();

        if (childRequirements != null) {
            for (ChildRequirement req : childRequirements.values()) {
                if ("*".equals(req.getName())) {
                    wildcards.add(req);
                } else {
                    named.put(req.getName(), req);
                }
            }
        }

        this.childRequirements = Map.copyOf(named);
        this.wildcardRequirements = List.copyOf(wildcards);

        // Resolve inherited requirements (will be populated later during registry resolution)
        this.inheritedChildRequirements = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * Create a type definition without inheritance (backward compatibility)
     *
     * @param implementationClass Java class that implements this type
     * @param type Primary type identifier (e.g., "field", "object")
     * @param subType Specific subtype identifier (e.g., "string", "base")
     * @param description Human-readable description
     * @param childRequirements Map of child name to requirement
     */
    public TypeDefinition(Class<? extends MetaData> implementationClass,
                         String type,
                         String subType,
                         String description,
                         Map<String, ChildRequirement> childRequirements) {
        this(implementationClass, type, subType, description, childRequirements, null, null);
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
     * Get all child requirements for this type (including inherited)
     *
     * @return List of all child requirements (direct + inherited + wildcards)
     */
    public List<ChildRequirement> getChildRequirements() {
        List<ChildRequirement> all = new ArrayList<>(childRequirements.values());
        all.addAll(wildcardRequirements);
        all.addAll(inheritedChildRequirements.values());
        return Collections.unmodifiableList(all);
    }

    /**
     * Get all direct child requirements (excluding inherited)
     *
     * @return List of direct child requirements only
     */
    public List<ChildRequirement> getDirectChildRequirements() {
        List<ChildRequirement> all = new ArrayList<>(childRequirements.values());
        all.addAll(wildcardRequirements);
        return Collections.unmodifiableList(all);
    }

    /**
     * Get all inherited child requirements
     *
     * @return Map of inherited child requirements
     */
    public Map<String, ChildRequirement> getInheritedChildRequirements() {
        return Collections.unmodifiableMap(inheritedChildRequirements);
    }

    /**
     * Get a specific child requirement by name (checks direct first, then inherited)
     *
     * @param childName Name of the child to look up
     * @return ChildRequirement if found, null otherwise
     */
    public ChildRequirement getChildRequirement(String childName) {
        // Check direct requirements first (they override inherited)
        ChildRequirement direct = childRequirements.get(childName);
        if (direct != null) {
            return direct;
        }

        // Check inherited requirements
        return inheritedChildRequirements.get(childName);
    }
    
    /**
     * Check if a child is acceptable for this type (checks direct and inherited requirements)
     *
     * @param childType Actual child type
     * @param childSubType Actual child subType
     * @param childName Actual child name
     * @return true if this type accepts the specified child
     */
    public boolean acceptsChild(String childType, String childSubType, String childName) {
        // NAMESPACE SEPARATION SUPPORT: Check direct named requirement for EXACT type match
        ChildRequirement namedReq = childRequirements.get(childName);
        if (namedReq != null && namedReq.matches(childType, childSubType, childName)) {
            return true;
        }

        // NAMESPACE SEPARATION SUPPORT: Check direct wildcard requirements
        // (allows different types with same name via wildcards)
        for (ChildRequirement wildcardReq : wildcardRequirements) {
            if (wildcardReq.matches(childType, childSubType, childName)) {
                return true;
            }
        }

        // NAMESPACE SEPARATION SUPPORT: Check inherited named requirement for EXACT type match
        ChildRequirement inheritedNamedReq = inheritedChildRequirements.get(childName);
        if (inheritedNamedReq != null && inheritedNamedReq.matches(childType, childSubType, childName)) {
            return true;
        }

        // Check all inherited requirements (both named and wildcard)
        for (ChildRequirement inheritedReq : inheritedChildRequirements.values()) {
            if (inheritedReq.matches(childType, childSubType, childName)) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * Get all required children that are missing
     * 
     * @param existingChildren Names of children that already exist
     * @return List of required ChildRequirements that are not satisfied
     */
    public List<ChildRequirement> getMissingRequiredChildren(Set<String> existingChildren) {
        List<ChildRequirement> missing = new ArrayList<>();
        
        // Check named required children
        for (ChildRequirement req : childRequirements.values()) {
            if (req.isRequired() && !existingChildren.contains(req.getName())) {
                missing.add(req);
            }
        }
        
        // Wildcard requirements cannot be "missing" since they don't specify names
        
        return missing;
    }
    
    /**
     * Get human-readable description of supported children for error messages
     * 
     * @return Description like "Supports: pattern (optional string attribute), required (optional boolean attribute)"
     */
    public String getSupportedChildrenDescription() {
        if (childRequirements.isEmpty() && wildcardRequirements.isEmpty()) {
            return "No children supported";
        }
        
        StringBuilder desc = new StringBuilder("Supports: ");
        List<String> parts = new ArrayList<>();
        
        // Named requirements
        for (ChildRequirement req : childRequirements.values()) {
            parts.add(req.getDescription());
        }
        
        // Wildcard requirements
        for (ChildRequirement req : wildcardRequirements) {
            parts.add(req.getDescription());
        }
        
        desc.append(String.join(", ", parts));
        return desc.toString();
    }

    /**
     * Internal method to populate inherited child requirements.
     * This is called by MetaDataRegistry during type registration to resolve inheritance chains.
     *
     * @param parentRequirements Child requirements from the parent type
     */
    void populateInheritedRequirements(Map<String, ChildRequirement> parentRequirements) {
        if (parentRequirements != null) {
            // Clear any existing inherited requirements
            inheritedChildRequirements.clear();

            // Add all parent requirements that are not overridden by direct requirements
            for (Map.Entry<String, ChildRequirement> entry : parentRequirements.entrySet()) {
                String parentKey = entry.getKey();
                ChildRequirement parentReq = entry.getValue();

                // Only inherit if not overridden by direct requirement with same key
                if (!childRequirements.containsKey(parentKey)) {
                    inheritedChildRequirements.put(parentKey, parentReq);
                }
            }
        }
    }

    /**
     * Create a unique key for a ChildRequirement that includes all identifying fields
     * to avoid conflicts between wildcards with same name but different types
     */
    private String createUniqueKey(ChildRequirement req) {
        return req.getName() + ":" + req.getExpectedType() + ":" + req.getExpectedSubType();
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
               childRequirements.equals(that.childRequirements) &&
               wildcardRequirements.equals(that.wildcardRequirements);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(implementationClass, type, subType, description, 
                          childRequirements, wildcardRequirements);
    }
    
    @Override
    public String toString() {
        return String.format("TypeDefinition[%s -> %s, children=%d]",
            getQualifiedName(), implementationClass.getSimpleName(),
            childRequirements.size() + wildcardRequirements.size());
    }
}