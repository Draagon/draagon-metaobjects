package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * v6.0.0: Unified constraint enforcement service for metadata validation.
 * 
 * Uses a single-pattern approach where all constraints (placement and validation)
 * are processed through a unified enforcement loop. This provides:
 * 
 * - Single source of truth for constraint enforcement
 * - Better performance (3x fewer constraint checking calls)
 * - Simplified architecture with one clear enforcement path
 * - Support for programmatic self-registered constraints
 * 
 * Integrates with MetaData.addChild() to validate metadata structure in real-time
 * using constraints registered via the self-registration pattern.
 */
public class ConstraintEnforcer {
    
    private static final Logger log = LoggerFactory.getLogger(ConstraintEnforcer.class);
    
    private static volatile ConstraintEnforcer instance;
    private static final Object INIT_LOCK = new Object();
    
    private final ConstraintRegistry constraintRegistry;
    private final ConstraintFlattener constraintFlattener;
    private final ConcurrentMap<String, Boolean> constraintCheckingEnabled;
    private boolean globalConstraintCheckingEnabled;

    private ConstraintEnforcer() {
        this.constraintRegistry = ConstraintRegistry.getInstance();
        this.constraintFlattener = new ConstraintFlattener(MetaDataRegistry.getInstance());
        this.constraintCheckingEnabled = new ConcurrentHashMap<>();
        this.globalConstraintCheckingEnabled = true;

        // Initialize the constraint flattener with current registry state
        initializeConstraintFlattener();
    }

    /**
     * Initialize the constraint flattener with current bidirectional constraints
     */
    private void initializeConstraintFlattener() {
        try {
            constraintFlattener.flattenAllConstraints();
            log.info("ConstraintFlattener initialized with flattened bidirectional constraints");
        } catch (Exception e) {
            log.warn("Failed to initialize ConstraintFlattener: " + e.getMessage(), e);
        }
    }

    
    /**
     * Get the singleton instance of ConstraintEnforcer
     * @return The constraint enforcer instance
     */
    public static ConstraintEnforcer getInstance() {
        if (instance == null) {
            synchronized (INIT_LOCK) {
                if (instance == null) {
                    instance = new ConstraintEnforcer();
                }
            }
        }
        return instance;
    }
    
    /**
     * Enforce constraints when adding a child to metadata (v6.2.0: bidirectional approach)
     * @param parent The parent metadata object
     * @param child The child being added
     * @throws ConstraintViolationException If constraints are violated
     */
    public void enforceConstraintsOnAddChild(MetaData parent, MetaData child) throws ConstraintViolationException {
        if (!isConstraintCheckingEnabled(parent)) {
            return;
        }

        ValidationContext context = ValidationContext.forAddChild(parent, child);

        log.debug("Enforcing bidirectional constraints for adding [{}] to [{}]",
            child.toString(), parent.toString());

        // STEP 1: BIDIRECTIONAL CONSTRAINT CHECKING
        // Both parent and child must agree on the placement
        boolean placementAllowed = constraintFlattener.isPlacementAllowed(
            parent.getType(), parent.getSubType(),
            child.getType(), child.getSubType(),
            child.getName()
        );

        if (!placementAllowed) {
            // Generate helpful error message with constraint information
            String supportedChildren = getSupportedChildrenDescription(parent);
            String message = String.format(
                "Bidirectional constraint violation: %s.%s does not accept child '%s' of type %s.%s. %s",
                parent.getType(), parent.getSubType(), child.getName(),
                child.getType(), child.getSubType(), supportedChildren);
            throw new ConstraintViolationException(message, "bidirectional", child.getName(), context);
        }

        log.trace("Bidirectional constraint check passed for [{}] -> [{}]", parent, child);

        // STEP 2: VALUE VALIDATION CONSTRAINTS
        // Process validation constraints on the child (unchanged)
        List<Constraint> allConstraints = constraintRegistry.getAllConstraints();
        for (Constraint constraint : allConstraints) {
            if (constraint instanceof ValidationConstraint) {
                ValidationConstraint vc = (ValidationConstraint) constraint;
                if (vc.appliesTo(child)) {
                    vc.validate(child, child.getName(), context);
                }
            }
        }

        log.trace("All constraints satisfied for adding [{}] to [{}]", child.getName(), parent.getName());
    }

    /**
     * Generate a description of supported children for error messages
     */
    private String getSupportedChildrenDescription(MetaData parent) {
        String parentQualified = parent.getType() + "." + parent.getSubType();
        return String.format("Supported children: %s",
            constraintFlattener.getValidChildTypes(parent.getType(), parent.getSubType()));
    }
    
    
    
    /**
     * Enable or disable constraint checking globally
     * @param enabled True to enable constraint checking, false to disable
     */
    public void setConstraintCheckingEnabled(boolean enabled) {
        this.globalConstraintCheckingEnabled = enabled;
        log.info("Global constraint checking {}", enabled ? "enabled" : "disabled");
    }
    
    /**
     * Enable or disable constraint checking for a specific metadata type
     * @param metaDataType The metadata type (e.g., "object", "field", "attr")
     * @param enabled True to enable constraint checking, false to disable
     */
    public void setConstraintCheckingEnabled(String metaDataType, boolean enabled) {
        constraintCheckingEnabled.put(metaDataType, enabled);
        log.info("Constraint checking {} for metadata type [{}]", enabled ? "enabled" : "disabled", metaDataType);
    }
    
    /**
     * Check if constraint checking is enabled for a metadata object
     * @param metaData The metadata object to check
     * @return True if constraint checking is enabled
     */
    public boolean isConstraintCheckingEnabled(MetaData metaData) {
        if (!globalConstraintCheckingEnabled) {
            return false;
        }
        
        String type = metaData.getType();
        return constraintCheckingEnabled.getOrDefault(type, true);
    }
    
    /**
     * Get the constraint registry used by this enforcer
     * @return The constraint registry
     */
    public ConstraintRegistry getConstraintRegistry() {
        return constraintRegistry;
    }

    /**
     * Get the constraint flattener used by this enforcer
     * @return The constraint flattener
     */
    public ConstraintFlattener getConstraintFlattener() {
        return constraintFlattener;
    }

    /**
     * Refresh the constraint flattener when new types are registered
     * Should be called when the type registry is updated
     */
    public void refreshConstraintFlattener() {
        log.info("Refreshing constraint flattener due to type registry changes");
        initializeConstraintFlattener();
    }
    
    /**
     * Check if constraint checking is enabled globally
     * @return True if global constraint checking is enabled
     */
    public boolean isGlobalConstraintCheckingEnabled() {
        return globalConstraintCheckingEnabled;
    }
    
    /**
     * Get constraint checking status for all metadata types
     * @return Map of metadata type to enabled status
     */
    public ConcurrentMap<String, Boolean> getConstraintCheckingStatus() {
        return new ConcurrentHashMap<>(constraintCheckingEnabled);
    }
    
    
}