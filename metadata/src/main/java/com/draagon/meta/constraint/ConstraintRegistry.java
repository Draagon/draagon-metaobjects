package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Simplified registry for concrete constraint instances.
 *
 * <p>This registry manages a collection of concrete constraint implementations
 * that all implement the {@link Constraint} interface. It provides a unified
 * way to store, retrieve, and apply constraints across the MetaObjects framework.</p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>Simple Storage:</strong> Just manages a List of Constraint objects</li>
 *   <li><strong>Type-Specific Filtering:</strong> Get constraints by concrete type for schema generators</li>
 *   <li><strong>MetaData Filtering:</strong> Get constraints that apply to specific MetaData</li>
 *   <li><strong>Thread-Safe:</strong> Supports concurrent access and modification</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // Add constraints
 * ConstraintRegistry registry = ConstraintRegistry.getInstance();
 * registry.addConstraint(new RegexValidationConstraint(...));
 * registry.addConstraint(new LengthConstraint(...));
 *
 * // Get constraints for validation
 * List<Constraint> applicable = registry.getConstraintsForMetaData(metaData);
 * for (Constraint constraint : applicable) {
 *     constraint.validate(metaData, context);
 * }
 * }</pre>
 *
 * @since 6.2.0
 */
public class ConstraintRegistry {

    private static final Logger log = LoggerFactory.getLogger(ConstraintRegistry.class);

    private static volatile ConstraintRegistry instance;
    private static final Object INIT_LOCK = new Object();

    private final List<Constraint> constraints;

    /**
     * Create ConstraintRegistry with thread-safe constraint storage
     */
    private ConstraintRegistry() {
        this.constraints = new CopyOnWriteArrayList<>();
        log.info("ConstraintRegistry initialized with simple constraint collection");
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
     * Add a constraint to the registry
     *
     * @param constraint The constraint to add
     */
    public void addConstraint(Constraint constraint) {
        if (constraint == null) {
            log.warn("Attempted to add null constraint - ignoring");
            return;
        }

        constraints.add(constraint);
        log.debug("Added constraint: {} [{}]", constraint.getName(), constraint.getDescription());
    }

    /**
     * Get all constraints
     *
     * @return List of all registered constraints
     */
    public List<Constraint> getAllConstraints() {
        return new ArrayList<>(constraints);
    }

    /**
     * Get constraints that apply to specific MetaData
     *
     * @param metaData The MetaData to check
     * @return List of constraints that apply to the MetaData
     */
    public List<Constraint> getConstraintsForMetaData(MetaData metaData) {
        return constraints.stream()
            .filter(constraint -> constraint.appliesTo(metaData))
            .collect(Collectors.toList());
    }

    /**
     * Get constraints by type (using instanceof)
     *
     * @param constraintType The constraint class type
     * @param <T> The constraint type
     * @return List of constraints matching the type
     */
    public <T extends Constraint> List<T> getConstraintsByType(Class<T> constraintType) {
        return constraints.stream()
            .filter(constraintType::isInstance)
            .map(constraintType::cast)
            .collect(Collectors.toList());
    }

    /**
     * Get total number of registered constraints
     *
     * @return Count of all constraints
     */
    public int getConstraintCount() {
        return constraints.size();
    }

    /**
     * Clear all constraints (primarily for testing)
     */
    public void clear() {
        constraints.clear();
        log.debug("Cleared all constraints from registry");
    }

    /**
     * Check if a constraint with the given name is registered
     *
     * @param constraintName The name to check
     * @return true if a constraint with this name exists
     */
    public boolean hasConstraint(String constraintName) {
        return constraints.stream()
            .anyMatch(constraint -> Objects.equals(constraintName, constraint.getName()));
    }

    /**
     * Remove a constraint by name
     *
     * @param constraintName The name of the constraint to remove
     * @return true if a constraint was removed
     */
    public boolean removeConstraint(String constraintName) {
        boolean removed = constraints.removeIf(constraint ->
            Objects.equals(constraintName, constraint.getName()));

        if (removed) {
            log.debug("Removed constraint: {}", constraintName);
        } else {
            log.debug("No constraint found with name: {}", constraintName);
        }

        return removed;
    }
}