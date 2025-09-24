package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;

import java.util.function.Predicate;

/**
 * PlacementConstraint defines WHERE a MetaData type can be placed in the metadata hierarchy.
 * This implements constraint-based placement rules ("X CAN be placed under Y") rather than
 * rigid "allow" or "disallow" patterns that violate extensibility.
 * 
 * Example: "StringField can optionally have maxLength attribute"
 * - parentMatcher: checks if parent is StringField
 * - childMatcher: checks if child is IntAttribute with name "maxLength"
 */
public class PlacementConstraint implements Constraint {
    
    private final String id;
    private final String description;
    private final Predicate<MetaData> parentMatcher;
    private final Predicate<MetaData> childMatcher;
    
    /**
     * Create a placement constraint
     * @param id Unique identifier for this constraint
     * @param description Human-readable description of the placement rule
     * @param parentMatcher Predicate to test if a parent MetaData can contain the child
     * @param childMatcher Predicate to test if a child MetaData can be placed under the parent
     */
    public PlacementConstraint(String id, String description, 
                              Predicate<MetaData> parentMatcher, 
                              Predicate<MetaData> childMatcher) {
        this.id = id;
        this.description = description;
        this.parentMatcher = parentMatcher;
        this.childMatcher = childMatcher;
    }
    
    /**
     * Check if a child can be placed under a parent
     * @param parent The parent MetaData
     * @param child The child MetaData to be added
     * @return True if this placement is allowed by this constraint
     */
    public boolean isPlacementAllowed(MetaData parent, MetaData child) {
        return parentMatcher.test(parent) && childMatcher.test(child);
    }
    
    /**
     * Check if this constraint applies to the given parent-child relationship
     * @param parent The parent MetaData
     * @param child The child MetaData
     * @return True if this constraint should be checked for this relationship
     */
    public boolean appliesTo(MetaData parent, MetaData child) {
        // This constraint applies only if BOTH parent and child match our predicates
        // A constraint like "MetaObject CAN contain MetaField" should only apply when 
        // we're actually adding a MetaField to a MetaObject, not in other cases
        return parentMatcher.test(parent) && childMatcher.test(child);
    }
    
    @Override
    public void validate(MetaData metaData, Object value)
            throws ConstraintViolationException {
        // PlacementConstraints are validated during addChild operations, not during value validation
        // This method is here to satisfy the Constraint interface but shouldn't be called for placement
        throw new UnsupportedOperationException(
            "PlacementConstraint validation should be called via isPlacementAllowed(), not validate()");
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
     * Get the unique identifier for this constraint
     * @return The constraint ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Get the parent matcher predicate
     * @return Predicate for testing parent MetaData
     */
    public Predicate<MetaData> getParentMatcher() {
        return parentMatcher;
    }
    
    /**
     * Get the child matcher predicate  
     * @return Predicate for testing child MetaData
     */
    public Predicate<MetaData> getChildMatcher() {
        return childMatcher;
    }
    
    @Override
    public String toString() {
        return "PlacementConstraint{" +
               "id='" + id + '\'' +
               ", description='" + description + '\'' +
               '}';
    }
}