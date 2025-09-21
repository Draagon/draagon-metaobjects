package com.draagon.meta.registry;

import com.draagon.meta.MetaData;

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
    private final Map<String, ChildRequirement> childRequirements;
    private final List<ChildRequirement> wildcardRequirements;
    
    /**
     * Create a type definition with child requirements
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
        this.implementationClass = Objects.requireNonNull(implementationClass, "Implementation class cannot be null");
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.subType = Objects.requireNonNull(subType, "SubType cannot be null");
        this.description = description != null ? description : "";
        
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
     * Get all child requirements for this type
     * 
     * @return List of all child requirements (named + wildcards)
     */
    public List<ChildRequirement> getChildRequirements() {
        List<ChildRequirement> all = new ArrayList<>(childRequirements.values());
        all.addAll(wildcardRequirements);
        return Collections.unmodifiableList(all);
    }
    
    /**
     * Get a specific child requirement by name
     * 
     * @param childName Name of the child to look up
     * @return ChildRequirement if found, null otherwise
     */
    public ChildRequirement getChildRequirement(String childName) {
        return childRequirements.get(childName);
    }
    
    /**
     * Check if a child is acceptable for this type
     * 
     * @param childType Actual child type
     * @param childSubType Actual child subType
     * @param childName Actual child name
     * @return true if this type accepts the specified child
     */
    public boolean acceptsChild(String childType, String childSubType, String childName) {
        // Check specific named requirement first
        ChildRequirement namedReq = childRequirements.get(childName);
        if (namedReq != null) {
            return namedReq.matches(childType, childSubType, childName);
        }
        
        // Check wildcard requirements
        for (ChildRequirement wildcardReq : wildcardRequirements) {
            if (wildcardReq.matches(childType, childSubType, childName)) {
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