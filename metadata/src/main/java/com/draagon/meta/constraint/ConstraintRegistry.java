package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * v6.2.0: Simplified constraint registry using concrete constraint classes.
 *
 * <p>Provides constraint storage and management using concrete constraint implementations.
 * This simplified approach supports direct constraint registration from MetaData classes
 * using their registerTypes() methods.</p>
 *
 * <p>Core constraint types supported:</p>
 * <ul>
 *   <li><strong>RegexValidationConstraint:</strong> Pattern-based validation</li>
 *   <li><strong>LengthConstraint:</strong> String length validation</li>
 *   <li><strong>EnumConstraint:</strong> Enumerated value validation</li>
 *   <li><strong>PlacementConstraint:</strong> Metadata hierarchy constraints</li>
 *   <li><strong>ValidationConstraint:</strong> Generic validation constraints</li>
 * </ul>
 *
 * @since 6.2.0
 */
public class ConstraintRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(ConstraintRegistry.class);

    private static volatile ConstraintRegistry instance;
    private static final Object INIT_LOCK = new Object();

    private final List<Constraint> allConstraints;
    private volatile boolean initialized = false;
    
    /**
     * Create ConstraintRegistry with simplified constraint management
     */
    private ConstraintRegistry() {
        this.allConstraints = Collections.synchronizedList(new ArrayList<>());

        log.info("ConstraintRegistry initialized with concrete constraint class support");
        loadCoreConstraints();
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
     * Load core constraints using concrete constraint classes
     */
    private void loadCoreConstraints() {
        if (initialized) {
            return;
        }

        synchronized (this) {
            if (initialized) {
                return;
            }

            try {
                // Load essential constraints using concrete classes
                loadNamingConstraints();
                loadPlacementConstraints();

                log.info("Loaded {} core constraints using concrete constraint classes",
                         allConstraints.size());
                initialized = true;

            } catch (Exception e) {
                log.error("Error loading core constraints: {}", e.getMessage(), e);
                // Continue with empty constraint set - don't fail initialization
                initialized = true;
            }
        }
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
    
    /**
     * Register a constraint using concrete constraint classes
     *
     * @param constraint The constraint to register
     */
    public void registerConstraint(Constraint constraint) {
        addConstraint(constraint);
    }
    
    /**
     * Check if constraint providers have been loaded
     * 
     * @return true if providers have been loaded
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Force reload of constraint providers (primarily for testing)
     */
    public void reload() {
        synchronized (this) {
            allConstraints.clear();
            initialized = false;
            loadCoreConstraints();
        }
    }
    
    /**
     * Get statistics about loaded constraints
     *
     * @return ConstraintRegistryStats with detailed information
     */
    public ConstraintRegistryStats getStats() {
        Map<String, Integer> constraintsByType = getConstraintTypeSummary();

        return new ConstraintRegistryStats(
            allConstraints.size(),
            constraintsByType,
            0, // No providers in simplified model
            "Concrete Constraint Classes",
            initialized
        );
    }
    
    /**
     * Statistics record for ConstraintRegistry
     */
    public record ConstraintRegistryStats(
        int totalConstraints,
        Map<String, Integer> constraintsByType,
        int providerCount,
        String registryDescription,
        boolean initialized
    ) {}
    
    /**
     * Load essential naming constraints using concrete constraint classes
     */
    private void loadNamingConstraints() {
        try {
            log.debug("Loading naming constraints");
            int constraintsBefore = allConstraints.size();

            // Field naming pattern using RegexValidationConstraint
            RegexValidationConstraint fieldNamingPattern = new RegexValidationConstraint(
                "field.naming.pattern",
                "Field names must follow identifier pattern or be package-qualified",
                (metadata) -> metadata instanceof com.draagon.meta.field.MetaField,
                "^[a-zA-Z][a-zA-Z0-9_]*$"
            );
            addConstraint(fieldNamingPattern);

            // Attribute naming pattern using RegexValidationConstraint
            RegexValidationConstraint attributeNamingPattern = new RegexValidationConstraint(
                "attribute.naming.pattern",
                "Attribute names must follow identifier pattern",
                (metadata) -> metadata instanceof com.draagon.meta.attr.MetaAttribute,
                "^[a-zA-Z][a-zA-Z0-9_]*$"
            );
            addConstraint(attributeNamingPattern);

            // Object naming pattern using RegexValidationConstraint
            RegexValidationConstraint objectNamingPattern = new RegexValidationConstraint(
                "object.naming.pattern",
                "Object names must follow identifier pattern or be package-qualified",
                (metadata) -> metadata instanceof com.draagon.meta.object.MetaObject,
                "^[a-zA-Z][a-zA-Z0-9_]*$"
            );
            addConstraint(objectNamingPattern);

            int constraintsAfter = allConstraints.size();
            int namingConstraints = constraintsAfter - constraintsBefore;

            if (namingConstraints > 0) {
                log.info("Loaded {} naming constraints using concrete constraint classes", namingConstraints);
            }

        } catch (Exception e) {
            log.error("Error loading naming constraints: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Load essential placement constraints using concrete constraint classes
     */
    private void loadPlacementConstraints() {
        try {
            log.debug("Loading placement constraints");
            int constraintsBefore = allConstraints.size();

            // Universal attribute placement constraint
            PlacementConstraint universalAttributePlacement = new PlacementConstraint(
                "attribute.universal.placement",
                "Attributes can be placed on any MetaData",
                (metadata) -> metadata instanceof MetaData,
                (child) -> child instanceof com.draagon.meta.attr.MetaAttribute
            );
            addConstraint(universalAttributePlacement);

            // Object contains fields placement constraint
            PlacementConstraint fieldsPlacement = new PlacementConstraint(
                "object.fields.placement",
                "Objects can contain fields",
                (metadata) -> metadata instanceof com.draagon.meta.object.MetaObject,
                (child) -> child instanceof com.draagon.meta.field.MetaField
            );
            addConstraint(fieldsPlacement);

            int constraintsAfter = allConstraints.size();
            int placementConstraints = constraintsAfter - constraintsBefore;

            if (placementConstraints > 0) {
                log.info("Loaded {} placement constraints using concrete constraint classes", placementConstraints);
            }

        } catch (Exception e) {
            log.error("Error loading placement constraints: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Example of how to register StringField constraints using concrete constraint classes.
     * This method will be called from StringField.registerTypes() instead of being hardcoded here.
     */
    public void registerStringFieldConstraints() {
        try {
            // These will be registered by StringField.registerTypes() method
            // Example: StringField pattern validation using RegexValidationConstraint
            log.debug("StringField constraints should be registered via StringField.registerTypes()");

        } catch (Exception e) {
            log.error("Failed to register StringField constraints", e);
        }
    }
    
    // ====================
    // BACKWARD COMPATIBILITY METHODS  
    // ====================
    
}