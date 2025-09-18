package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * v6.0.0: Service that enforces constraints during metadata construction.
 * Integrates with MetaData.addChild() and attribute setting to validate
 * metadata structure in real-time.
 */
public class ConstraintEnforcer {
    
    private static final Logger log = LoggerFactory.getLogger(ConstraintEnforcer.class);
    
    private static volatile ConstraintEnforcer instance;
    private static final Object INIT_LOCK = new Object();
    
    private final ConstraintRegistry constraintRegistry;
    private final ConcurrentMap<String, Boolean> constraintCheckingEnabled;
    private boolean globalConstraintCheckingEnabled;
    
    private ConstraintEnforcer() {
        this.constraintRegistry = ConstraintRegistry.getInstance();
        this.constraintCheckingEnabled = new ConcurrentHashMap<>();
        this.globalConstraintCheckingEnabled = true;
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
     * Enforce constraints when adding a child to metadata
     * @param parent The parent metadata object
     * @param child The child being added
     * @throws ConstraintViolationException If constraints are violated
     */
    public void enforceConstraintsOnAddChild(MetaData parent, MetaData child) throws ConstraintViolationException {
        if (!isConstraintCheckingEnabled(parent)) {
            return;
        }
        
        ValidationContext context = ValidationContext.forAddChild(parent, child);
        
        // Check constraints on the child metadata object itself
        enforceConstraintsOnMetaData(child, context);
        
        // If child is a MetaAttribute, check attribute-specific constraints
        if (child instanceof MetaAttribute) {
            enforceConstraintsOnAttribute(parent, (MetaAttribute) child, context);
        }
    }
    
    /**
     * Enforce constraints on a metadata object
     * @param metaData The metadata object to validate
     * @param context Validation context
     * @throws ConstraintViolationException If constraints are violated
     */
    public void enforceConstraintsOnMetaData(MetaData metaData, ValidationContext context) throws ConstraintViolationException {
        if (!isConstraintCheckingEnabled(metaData)) {
            return;
        }
        
        String type = metaData.getType();
        String subType = metaData.getSubType();
        String fullName = metaData.getName();
        String name = metaData.getShortName(); // Use short name for constraint validation
        
        // Special case: If this is a direct field creation (not from loader) and contains ::, 
        // validate full name to reject inappropriate use of :: in direct field names
        if (fullName != null && fullName.contains("::") && !fullName.equals(name)) {
            // Check if this metadata is being created directly (not through loader qualified naming)
            // If the parent is not a loader and the name contains ::, it's likely a direct creation
            if (context != null && context.getOperation().isPresent() && context.getOperation().get().equals("addChild")) {
                Optional<MetaData> parentOpt = context.getParentMetaData();
                if (parentOpt.isPresent() && !(parentOpt.get() instanceof com.draagon.meta.loader.MetaDataLoader)) {
                    // Use full name validation for direct field creation with ::
                    name = fullName;
                }
            }
        }
        
        // Get applicable constraints
        List<ConstraintRegistry.ConstraintDefinition> constraints = constraintRegistry.getConstraintsForTarget(type, subType, name);
        
        if (constraints.isEmpty()) {
            log.trace("No constraints found for metadata [{}] type={}, subType={}, name={}", 
                metaData.toString(), type, subType, name);
            return;
        }
        
        log.debug("Enforcing {} constraints for metadata [{}]", constraints.size(), metaData.toString());
        
        // Enforce each constraint
        for (ConstraintRegistry.ConstraintDefinition constraintDef : constraints) {
            try {
                Constraint constraint = constraintDef.createConstraint();
                if (constraint != null) {
                    // Validate the metadata object itself (name validation, structure validation, etc.)
                    constraint.validate(metaData, name, context);
                } else {
                    log.warn("Could not create constraint instance for type [{}] - constraint will be skipped", 
                        constraintDef.getType());
                }
            } catch (ConstraintViolationException e) {
                log.debug("Constraint violation in metadata [{}]: {}", metaData.toString(), e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("Error enforcing constraint [{}] on metadata [{}]: {}", 
                    constraintDef.getType(), metaData.toString(), e.getMessage(), e);
                // Don't propagate unexpected errors - constraint system should be non-blocking for unknown errors
            }
        }
    }
    
    /**
     * Enforce constraints on a metadata attribute
     * @param parent The parent metadata object
     * @param attribute The attribute being added
     * @param context Validation context
     * @throws ConstraintViolationException If constraints are violated
     */
    public void enforceConstraintsOnAttribute(MetaData parent, MetaAttribute attribute, ValidationContext context) throws ConstraintViolationException {
        if (!isConstraintCheckingEnabled(parent)) {
            return;
        }
        
        String attributeName = attribute.getName();
        Object attributeValue = attribute.getValue();
        
        // Get constraints for this specific attribute - look for attr-type constraints, not parent-type constraints
        List<ConstraintRegistry.ConstraintDefinition> constraints = constraintRegistry.getConstraintsForTarget(
            "attr", attribute.getSubType(), attributeName);
        
        // Filter out pattern constraints from attributes - they should never apply to attribute values
        constraints = constraints.stream()
            .filter(c -> !"pattern".equals(c.getType()))
            .collect(Collectors.toList());
        
        if (constraints.isEmpty()) {
            log.trace("No constraints found for attribute [{}] on metadata [{}]", attributeName, parent.toString());
            return;
        }
        
        log.debug("Enforcing {} constraints for attribute [{}] on metadata [{}]", 
            constraints.size(), attributeName, parent.toString());
        
        ValidationContext attrContext = ValidationContext.builder()
            .operation("setAttribute")
            .parentMetaData(parent)
            .fieldName(attributeName)
            .property("attributeValue", attributeValue)
            .property("attribute", attribute)
            .build();
        
        // Enforce each constraint
        for (ConstraintRegistry.ConstraintDefinition constraintDef : constraints) {
            try {
                Constraint constraint = constraintDef.createConstraint();
                if (constraint != null) {
                    constraint.validate(parent, attributeValue, attrContext);
                } else {
                    log.warn("Could not create constraint instance for type [{}] - constraint will be skipped", 
                        constraintDef.getType());
                }
            } catch (ConstraintViolationException e) {
                log.debug("Constraint violation in attribute [{}] on metadata [{}]: {}", 
                    attributeName, parent.toString(), e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("Error enforcing constraint [{}] on attribute [{}] of metadata [{}]: {}", 
                    constraintDef.getType(), attributeName, parent.toString(), e.getMessage(), e);
                // Don't propagate unexpected errors - constraint system should be non-blocking for unknown errors
            }
        }
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