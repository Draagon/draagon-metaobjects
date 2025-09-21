package com.draagon.meta.registry;

/**
 * Represents a requirement for a child MetaData element within a parent type.
 * 
 * <p>This class supports flexible matching patterns using wildcards to allow
 * generic definitions while maintaining type safety and validation:</p>
 * 
 * <ul>
 *   <li><strong>Specific matching:</strong> {@code name="pattern", type="attr", subType="string"}</li>
 *   <li><strong>Any name matching:</strong> {@code name="*", type="field", subType="string"}</li>
 *   <li><strong>Any subType matching:</strong> {@code name="*", type="attr", subType="*"}</li>
 * </ul>
 * 
 * @since 6.0.0
 */
public class ChildRequirement {
    
    private final String name;           // "pattern", "email", "*" for any
    private final String expectedType;   // "field", "attr", "validator", "*" for any
    private final String expectedSubType; // "string", "int", "*" for any  
    private final boolean required;
    
    /**
     * Create a child requirement with specific matching criteria
     * 
     * @param name Expected child name or "*" for any name
     * @param expectedType Expected child type or "*" for any type
     * @param expectedSubType Expected child subType or "*" for any subType
     * @param required Whether this child is required (true) or optional (false)
     */
    public ChildRequirement(String name, String expectedType, String expectedSubType, boolean required) {
        this.name = name != null ? name : "*";
        // Normalize types to lowercase for consistency with MetaDataTypeId
        this.expectedType = expectedType != null ? expectedType.toLowerCase() : "*";
        this.expectedSubType = expectedSubType != null ? expectedSubType.toLowerCase() : "*";
        this.required = required;
    }
    
    /**
     * Create an optional child requirement
     * 
     * @param name Expected child name or "*" for any name
     * @param expectedType Expected child type or "*" for any type
     * @param expectedSubType Expected child subType or "*" for any subType
     * @return New optional ChildRequirement
     */
    public static ChildRequirement optional(String name, String expectedType, String expectedSubType) {
        return new ChildRequirement(name, expectedType, expectedSubType, false);
    }
    
    /**
     * Create a required child requirement
     * 
     * @param name Expected child name or "*" for any name
     * @param expectedType Expected child type or "*" for any type
     * @param expectedSubType Expected child subType or "*" for any subType
     * @return New required ChildRequirement
     */
    public static ChildRequirement required(String name, String expectedType, String expectedSubType) {
        return new ChildRequirement(name, expectedType, expectedSubType, true);
    }
    
    /**
     * Test if this requirement matches a specific child
     * 
     * @param childType Actual child type
     * @param childSubType Actual child subType
     * @param childName Actual child name
     * @return true if the child matches this requirement
     */
    public boolean matches(String childType, String childSubType, String childName) {
        return matchesPattern(this.expectedType, childType) &&
               matchesPattern(this.expectedSubType, childSubType) &&
               matchesPattern(this.name, childName);
    }
    
    /**
     * Wildcard pattern matching - "*" matches anything
     * 
     * @param pattern Pattern string, "*" for wildcard
     * @param value Actual value to test
     * @return true if pattern matches value
     */
    private boolean matchesPattern(String pattern, String value) {
        if (pattern == null || "*".equals(pattern)) {
            return true;
        }
        return pattern.equals(value);
    }
    
    /**
     * Get the expected name pattern
     * 
     * @return Expected name or "*" for any name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the expected type pattern
     * 
     * @return Expected type or "*" for any type
     */
    public String getExpectedType() {
        return expectedType;
    }
    
    /**
     * Get the expected subType pattern
     * 
     * @return Expected subType or "*" for any subType
     */
    public String getExpectedSubType() {
        return expectedSubType;
    }
    
    /**
     * Check if this child is required
     * 
     * @return true if required, false if optional
     */
    public boolean isRequired() {
        return required;
    }
    
    /**
     * Check if this requirement uses wildcard matching
     * 
     * @return true if any field uses "*" pattern
     */
    public boolean isWildcard() {
        return "*".equals(name) || "*".equals(expectedType) || "*".equals(expectedSubType);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ChildRequirement that = (ChildRequirement) obj;
        return required == that.required &&
               name.equals(that.name) &&
               expectedType.equals(that.expectedType) &&
               expectedSubType.equals(that.expectedSubType);
    }
    
    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + expectedType.hashCode();
        result = 31 * result + expectedSubType.hashCode();
        result = 31 * result + Boolean.hashCode(required);
        return result;
    }
    
    @Override
    public String toString() {
        String qualifier = required ? "required" : "optional";
        return String.format("%s child[name=%s, type=%s.%s]", 
            qualifier, name, expectedType, expectedSubType);
    }
    
    /**
     * Create a human-readable description for error messages
     * 
     * @return Description like "required attribute 'pattern' of type string"
     */
    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(required ? "required" : "optional");
        
        if ("attr".equals(expectedType)) {
            desc.append(" attribute");
        } else if ("field".equals(expectedType)) {
            desc.append(" field");
        } else {
            desc.append(" child");
        }
        
        if (!"*".equals(name)) {
            desc.append(" '").append(name).append("'");
        }
        
        if (!"*".equals(expectedSubType)) {
            desc.append(" of type ").append(expectedSubType);
        }
        
        return desc.toString();
    }
}