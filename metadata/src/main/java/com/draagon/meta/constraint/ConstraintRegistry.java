package com.draagon.meta.constraint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * v6.0.0: Unified registry for programmatic constraint definitions.
 * Provides constraint storage and discovery capabilities using a simplified
 * single-pattern approach for self-registered constraints.
 */
public class ConstraintRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(ConstraintRegistry.class);
    
    private static volatile ConstraintRegistry instance;
    private static final Object INIT_LOCK = new Object();
    
    // UNIFIED: Single storage for all constraints
    private final List<Constraint> allConstraints;
    
    private ConstraintRegistry() {
        this.allConstraints = Collections.synchronizedList(new ArrayList<>());
        
        log.info("ConstraintRegistry initialized with unified programmatic constraint storage");
    }
    
    /**
     * Get the singleton instance of ConstraintRegistry
     * @return The constraint registry instance
     */
    public static ConstraintRegistry getInstance() {
        if (instance == null) {
            synchronized (INIT_LOCK) {
                if (instance == null) {
                    instance = new ConstraintRegistry();
                }
            }
        }
        return instance;
    }
    
    /**
     * Add a constraint programmatically (unified approach for self-registration pattern)
     * @param constraint The constraint instance to add
     */
    public void addConstraint(Constraint constraint) {
        if (constraint == null) {
            log.warn("Attempted to add null constraint - ignoring");
            return;
        }
        
        allConstraints.add(constraint);
        log.debug("Added constraint: {} [{}]", constraint.getType(), constraint.getDescription());
    }
    
    /**
     * Get all constraints
     * @return List of all registered constraints
     */
    public List<Constraint> getAllConstraints() {
        return new ArrayList<>(allConstraints);
    }
    
    /**
     * Get all placement constraints
     * @return List of placement constraints
     */
    public List<PlacementConstraint> getPlacementConstraints() {
        return allConstraints.stream()
            .filter(c -> c instanceof PlacementConstraint)
            .map(c -> (PlacementConstraint) c)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all validation constraints  
     * @return List of validation constraints
     */
    public List<ValidationConstraint> getValidationConstraints() {
        return allConstraints.stream()
            .filter(c -> c instanceof ValidationConstraint)
            .map(c -> (ValidationConstraint) c)
            .collect(Collectors.toList());
    }
    
    /**
     * Get constraints by type
     * @param constraintType The constraint type to filter by
     * @return List of constraints matching the type
     */
    public List<Constraint> getConstraintsByType(String constraintType) {
        return allConstraints.stream()
            .filter(c -> constraintType.equals(c.getType()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get total number of registered constraints
     * @return Count of all constraints
     */
    public int getConstraintCount() {
        return allConstraints.size();
    }
    
    /**
     * Get summary of constraint types and counts
     * @return Map of constraint type to count
     */
    public Map<String, Integer> getConstraintTypeSummary() {
        return allConstraints.stream()
            .collect(Collectors.groupingBy(
                Constraint::getType,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
    }
    
    // ====================
    // BACKWARD COMPATIBILITY METHODS
    // ====================
    
    /**
     * @deprecated Use getPlacementConstraints() instead
     * Get all programmatic placement constraints (backward compatibility)
     * @return List of placement constraints
     */
    @Deprecated
    public List<PlacementConstraint> getProgrammaticPlacementConstraints() {
        return getPlacementConstraints();
    }
    
    /**
     * @deprecated Use getValidationConstraints() instead  
     * Get all programmatic validation constraints (backward compatibility)
     * @return List of validation constraints
     */
    @Deprecated
    public List<ValidationConstraint> getProgrammaticValidationConstraints() {
        return getValidationConstraints();
    }
    
    /**
     * @deprecated Use getConstraintsByType() instead
     * Get all programmatic constraints of a specific type (backward compatibility)
     * @param constraintType The constraint type to filter by
     * @return List of constraints matching the type
     */
    @Deprecated
    public List<Constraint> getProgrammaticConstraints(String constraintType) {
        return getConstraintsByType(constraintType);
    }
    
    /**
     * @deprecated No longer needed - constraints are stored unified
     * Clear all loaded constraints (for testing)
     */
    @Deprecated
    public void clearAllConstraints() {
        allConstraints.clear();
        log.debug("Cleared all constraints");
    }
    
    /**
     * @deprecated Legacy method - always returns empty list (JSON loading disabled)
     * Get all constraints applicable to a specific target (legacy compatibility)
     * @param targetType Target type (ignored)
     * @param targetSubType Target subtype (ignored) 
     * @param targetName Target name (ignored)
     * @return Empty list (legacy JSON-based constraints no longer supported)
     */
    @Deprecated
    public List<Object> getConstraintsForTarget(String targetType, String targetSubType, String targetName) {
        // Legacy method - JSON-based constraints are no longer supported
        // All constraints are now programmatic and accessed via unified methods
        log.trace("Legacy getConstraintsForTarget called - returning empty list (JSON constraints disabled)");
        return Collections.emptyList();
    }
}