package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;
import com.draagon.meta.registry.ServiceRegistry;
import com.draagon.meta.registry.ServiceRegistryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * v6.0.0: Unified registry for programmatic constraint definitions using ServiceRegistry pattern.
 * 
 * <p>Provides constraint storage and discovery capabilities with service-based provider loading
 * that works in both OSGi and non-OSGi environments:</p>
 * 
 * <ul>
 *   <li><strong>OSGi:</strong> ConstraintProvider services discovered via ServiceTracker</li>
 *   <li><strong>Non-OSGi:</strong> ConstraintProvider services discovered via ServiceLoader</li>
 *   <li><strong>Manual:</strong> Direct constraint registration for testing</li>
 * </ul>
 * 
 * <p>This unified approach eliminates the need for static blocks and provides proper
 * service lifecycle management with automatic cleanup in OSGi environments.</p>
 * 
 * @since 6.0.0
 */
public class ConstraintRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(ConstraintRegistry.class);
    
    private static volatile ConstraintRegistry instance;
    private static final Object INIT_LOCK = new Object();
    
    private final ServiceRegistry serviceRegistry;
    private final List<Constraint> allConstraints;
    private volatile boolean initialized = false;
    
    /**
     * Create ConstraintRegistry with default ServiceRegistry (auto-detects environment)
     */
    private ConstraintRegistry() {
        this(ServiceRegistryFactory.getDefault());
    }
    
    /**
     * Create ConstraintRegistry with specific ServiceRegistry
     * 
     * @param serviceRegistry Service registry for provider discovery
     */
    public ConstraintRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = Objects.requireNonNull(serviceRegistry, "ServiceRegistry cannot be null");
        this.allConstraints = Collections.synchronizedList(new ArrayList<>());
        
        log.info("ConstraintRegistry initialized with ServiceRegistry: {}", serviceRegistry.getDescription());
        loadConstraintProviders();
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
     * Load constraints from base class static blocks (v6.1.0+ pattern) 
     */
    private void loadConstraintProviders() {
        if (initialized) {
            return;
        }
        
        synchronized (this) {
            if (initialized) {
                return;
            }
            
            try {
                // Force class loading of base classes with constraint registration
                loadBaseClassConstraints();
                
                // Also load type-specific constraints from individual MetaData classes 
                loadTypeSpecificConstraints();
                
                log.info("Loaded {} total constraints from base classes and type-specific registrations", 
                         allConstraints.size());
                initialized = true;
                
            } catch (Exception e) {
                log.error("Error loading constraints from base classes: {}", e.getMessage(), e);
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
     * Get the ServiceRegistry used by this ConstraintRegistry
     * 
     * @return The ServiceRegistry instance
     */
    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
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
            loadConstraintProviders();
        }
    }
    
    /**
     * Get statistics about loaded constraints and providers
     * 
     * @return ConstraintRegistryStats with detailed information
     */
    public ConstraintRegistryStats getStats() {
        Map<String, Integer> constraintsByType = getConstraintTypeSummary();
        
        // Count providers if possible
        int providerCount = 0;
        try {
            Collection<ConstraintProvider> providers = serviceRegistry.getServices(ConstraintProvider.class);
            providerCount = providers.size();
        } catch (Exception e) {
            log.debug("Could not get provider count: {}", e.getMessage());
        }
        
        return new ConstraintRegistryStats(
            allConstraints.size(),
            constraintsByType,
            providerCount,
            serviceRegistry.getDescription(),
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
        String serviceRegistryDescription,
        boolean initialized
    ) {}
    
    /**
     * Register base class constraints directly to this instance
     */
    private void loadBaseClassConstraints() {
        try {
            log.debug("Loading base class constraints");
            int constraintsBefore = allConstraints.size();
            
            // Register MetaField cross-cutting constraints directly
            registerMetaFieldConstraints();
            
            // Register MetaAttribute cross-cutting constraints directly
            registerMetaAttributeConstraints();
            
            // Register MetaObject cross-cutting constraints directly
            registerMetaObjectConstraints();
            
            int constraintsAfter = allConstraints.size();
            int baseClassConstraints = constraintsAfter - constraintsBefore;
            
            if (baseClassConstraints > 0) {
                log.info("Loaded {} constraints from base class registrations", baseClassConstraints);
            } else {
                log.debug("No constraints loaded from base class registrations");
            }
            
        } catch (Exception e) {
            log.error("Error loading base class constraints: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Force class loading of type-specific MetaData classes with constraint registration
     */
    private void loadTypeSpecificConstraints() {
        try {
            log.debug("Loading type-specific constraints");
            int constraintsBefore = allConstraints.size();
            
            // Register StringField-specific constraints directly to this instance
            registerStringFieldConstraints();
            
            // Register other field type constraints
            registerIntegerFieldConstraints();
            registerLongFieldConstraints();
            registerDoubleFieldConstraints();
            registerDateFieldConstraints();
            registerTimestampFieldConstraints();
            
            // Register attribute type constraints
            registerBooleanAttributeConstraints();
            registerIntAttributeConstraints();
            registerStringArrayAttributeConstraints();
            
            // Register object type constraints
            registerPojoMetaObjectConstraints();
            registerProxyMetaObjectConstraints();
            
            int constraintsAfter = allConstraints.size();
            int typeSpecificConstraints = constraintsAfter - constraintsBefore;
            
            if (typeSpecificConstraints > 0) {
                log.info("Loaded {} type-specific constraints", typeSpecificConstraints);
            } else {
                log.debug("No type-specific constraints loaded");
            }
            
        } catch (Exception e) {
            log.error("Error loading type-specific constraints: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Register StringField-specific constraints
     */
    private void registerStringFieldConstraints() {
        try {
            // Import the constants from StringField
            final String ATTR_PATTERN = "pattern";
            final String ATTR_MAX_LENGTH = "maxLength";
            final String ATTR_MIN_LENGTH = "minLength";
            
            // PLACEMENT CONSTRAINT: StringField CAN have maxLength attribute
            PlacementConstraint maxLengthPlacement = new PlacementConstraint(
                "stringfield.maxlength.placement",
                "StringField can optionally have maxLength attribute",
                (metadata) -> "com.draagon.meta.field.StringField".equals(metadata.getClass().getName()),
                (child) -> "com.draagon.meta.attr.IntAttribute".equals(child.getClass().getName()) && 
                          child.getName().equals(ATTR_MAX_LENGTH)
            );
            addConstraint(maxLengthPlacement);
            
            // PLACEMENT CONSTRAINT: StringField CAN have minLength attribute
            PlacementConstraint minLengthPlacement = new PlacementConstraint(
                "stringfield.minlength.placement",
                "StringField can optionally have minLength attribute",
                (metadata) -> "com.draagon.meta.field.StringField".equals(metadata.getClass().getName()),
                (child) -> "com.draagon.meta.attr.IntAttribute".equals(child.getClass().getName()) && 
                          child.getName().equals(ATTR_MIN_LENGTH)
            );
            addConstraint(minLengthPlacement);
            
            // PLACEMENT CONSTRAINT: StringField CAN have pattern attribute
            PlacementConstraint patternPlacement = new PlacementConstraint(
                "stringfield.pattern.placement",
                "StringField can optionally have pattern attribute",
                (metadata) -> "com.draagon.meta.field.StringField".equals(metadata.getClass().getName()),
                (child) -> "com.draagon.meta.attr.StringAttribute".equals(child.getClass().getName()) && 
                          child.getName().equals(ATTR_PATTERN)
            );
            addConstraint(patternPlacement);
            
            // VALIDATION CONSTRAINT: Pattern validation for string fields
            ValidationConstraint patternValidation = new ValidationConstraint(
                "stringfield.pattern.validation",
                "StringField pattern attribute must be valid regex",
                (metadata) -> "com.draagon.meta.field.StringField".equals(metadata.getClass().getName()) && 
                             metadata.hasMetaAttr(ATTR_PATTERN),
                (metadata, value) -> {
                    try {
                        String pattern = metadata.getMetaAttr(ATTR_PATTERN).getValueAsString();
                        if (pattern != null && !pattern.isEmpty()) {
                            // Test if pattern is valid regex
                            java.util.regex.Pattern.compile(pattern);
                        }
                        return true;
                    } catch (java.util.regex.PatternSyntaxException e) {
                        return false;
                    }
                }
            );
            addConstraint(patternValidation);
            
            log.debug("Registered StringField-specific constraints");
            
        } catch (Exception e) {
            log.error("Failed to register StringField constraints", e);
        }
    }
    
    /**
     * Register placeholder methods for other constraint types
     * TODO: Implement these methods with the actual constraints from the respective classes
     */
    private void registerIntegerFieldConstraints() {
        // TODO: Copy constraints from IntegerField static block
    }
    
    private void registerLongFieldConstraints() {
        // TODO: Copy constraints from LongField static block
    }
    
    private void registerDoubleFieldConstraints() {
        // TODO: Copy constraints from DoubleField static block
    }
    
    private void registerDateFieldConstraints() {
        // TODO: Copy constraints from DateField static block
    }
    
    private void registerTimestampFieldConstraints() {
        // TODO: Copy constraints from TimestampField static block
    }
    
    private void registerBooleanAttributeConstraints() {
        // TODO: Copy constraints from BooleanAttribute static block
    }
    
    private void registerIntAttributeConstraints() {
        // TODO: Copy constraints from IntAttribute static block
    }
    
    private void registerStringArrayAttributeConstraints() {
        // TODO: Copy constraints from StringArrayAttribute static block
    }
    
    private void registerPojoMetaObjectConstraints() {
        // TODO: Copy constraints from PojoMetaObject static block
    }
    
    private void registerProxyMetaObjectConstraints() {
        // TODO: Copy constraints from ProxyMetaObject static block
    }
    
    /**
     * Register MetaField cross-cutting constraints directly to this instance
     */
    private void registerMetaFieldConstraints() {
        try {
            // VALIDATION CONSTRAINT: Field naming patterns (allow package-qualified names)
            ValidationConstraint fieldNamingPattern = new ValidationConstraint(
                "field.naming.pattern",
                "Field names must follow identifier pattern or be package-qualified",
                (metadata) -> metadata instanceof com.draagon.meta.field.MetaField,
                (metadata, value) -> {
                    String name = metadata.getName();
                    if (name == null) return false;
                    
                    // Allow package-qualified names (with ::)
                    if (name.contains("::")) {
                        String[] parts = name.split("::");
                        for (String part : parts) {
                            if (!part.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
                                return false;
                            }
                        }
                        return true;
                    } else {
                        // Simple names must follow identifier pattern
                        return name.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
                    }
                }
            );
            addConstraint(fieldNamingPattern);
            
            // PLACEMENT CONSTRAINT: All fields CAN have required attribute
            PlacementConstraint requiredPlacement = new PlacementConstraint(
                "field.required.placement",
                "Fields can optionally have required attribute",
                (metadata) -> metadata instanceof com.draagon.meta.field.MetaField,
                (child) -> child instanceof com.draagon.meta.attr.BooleanAttribute && 
                          child.getName().equals("required")
            );
            addConstraint(requiredPlacement);
            
            log.debug("Registered MetaField cross-cutting constraints");
            
        } catch (Exception e) {
            log.error("Failed to register MetaField constraints", e);
        }
    }
    
    /**
     * Register MetaAttribute cross-cutting constraints directly to this instance
     */
    private void registerMetaAttributeConstraints() {
        try {
            // PLACEMENT CONSTRAINT: Attributes can be placed on any MetaData
            PlacementConstraint universalAttributePlacement = new PlacementConstraint(
                "attribute.universal.placement",
                "Attributes can be placed on any MetaData",
                (metadata) -> metadata instanceof MetaData,
                (child) -> child instanceof com.draagon.meta.attr.MetaAttribute
            );
            addConstraint(universalAttributePlacement);
            
            // VALIDATION CONSTRAINT: Attribute naming patterns
            ValidationConstraint attributeNamingPattern = new ValidationConstraint(
                "attribute.naming.pattern",
                "Attribute names must follow identifier pattern",
                (metadata) -> metadata instanceof com.draagon.meta.attr.MetaAttribute,
                (metadata, value) -> {
                    String name = metadata.getName();
                    return name != null && name.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
                }
            );
            addConstraint(attributeNamingPattern);
            
            log.debug("Registered MetaAttribute cross-cutting constraints");
            
        } catch (Exception e) {
            log.error("Failed to register MetaAttribute constraints", e);
        }
    }
    
    /**
     * Register MetaObject cross-cutting constraints directly to this instance
     */
    private void registerMetaObjectConstraints() {
        try {
            // VALIDATION CONSTRAINT: Object naming patterns (allow package-qualified names)
            ValidationConstraint objectNamingPattern = new ValidationConstraint(
                "object.naming.pattern",
                "Object names must follow identifier pattern or be package-qualified",
                (metadata) -> metadata instanceof com.draagon.meta.object.MetaObject,
                (metadata, value) -> {
                    String name = metadata.getName();
                    if (name == null) return false;
                    
                    // Allow package-qualified names (with ::)
                    if (name.contains("::")) {
                        String[] parts = name.split("::");
                        for (String part : parts) {
                            if (!part.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
                                return false;
                            }
                        }
                        return true;
                    } else {
                        // Simple names must follow identifier pattern
                        return name.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
                    }
                }
            );
            addConstraint(objectNamingPattern);
            
            // PLACEMENT CONSTRAINT: Objects CAN contain fields
            PlacementConstraint fieldsPlacement = new PlacementConstraint(
                "object.fields.placement",
                "Objects can contain fields",
                (metadata) -> metadata instanceof com.draagon.meta.object.MetaObject,
                (child) -> child instanceof com.draagon.meta.field.MetaField
            );
            addConstraint(fieldsPlacement);
            
            // PLACEMENT CONSTRAINT: Objects CAN contain attributes
            PlacementConstraint attributesPlacement = new PlacementConstraint(
                "object.attributes.placement",
                "Objects can contain attributes",
                (metadata) -> metadata instanceof com.draagon.meta.object.MetaObject,
                (child) -> child instanceof com.draagon.meta.attr.MetaAttribute
            );
            addConstraint(attributesPlacement);
            
            // PLACEMENT CONSTRAINT: Objects CAN contain keys
            PlacementConstraint keysPlacement = new PlacementConstraint(
                "object.keys.placement",
                "Objects can contain keys (primary, foreign, secondary)",
                (metadata) -> metadata instanceof com.draagon.meta.object.MetaObject,
                (child) -> child instanceof com.draagon.meta.key.MetaKey
            );
            addConstraint(keysPlacement);
            
            // PLACEMENT CONSTRAINT: Objects CAN contain validators
            PlacementConstraint validatorsPlacement = new PlacementConstraint(
                "object.validators.placement",
                "Objects can contain validators",
                (metadata) -> metadata instanceof com.draagon.meta.object.MetaObject,
                (child) -> child instanceof com.draagon.meta.validator.MetaValidator
            );
            addConstraint(validatorsPlacement);
            
            // PLACEMENT CONSTRAINT: Objects CAN contain views
            PlacementConstraint viewsPlacement = new PlacementConstraint(
                "object.views.placement",
                "Objects can contain views",
                (metadata) -> metadata instanceof com.draagon.meta.object.MetaObject,
                (child) -> child instanceof com.draagon.meta.view.MetaView
            );
            addConstraint(viewsPlacement);
            
            // PLACEMENT CONSTRAINT: Objects CAN contain nested objects
            PlacementConstraint nestedObjectsPlacement = new PlacementConstraint(
                "object.nested.placement",
                "Objects can contain nested objects",
                (metadata) -> metadata instanceof com.draagon.meta.object.MetaObject,
                (child) -> child instanceof com.draagon.meta.object.MetaObject
            );
            addConstraint(nestedObjectsPlacement);
            
            // VALIDATION CONSTRAINT: Unique field names within object (applies to all objects)
            ValidationConstraint uniqueFieldNames = new ValidationConstraint(
                "object.field.uniqueness",
                "Field names must be unique within an object",
                (metadata) -> metadata instanceof com.draagon.meta.object.MetaObject,
                (metadata, value) -> {
                    if (metadata instanceof com.draagon.meta.object.MetaObject) {
                        com.draagon.meta.object.MetaObject obj = (com.draagon.meta.object.MetaObject) metadata;
                        var fieldNames = obj.getChildren(com.draagon.meta.field.MetaField.class).stream()
                            .map(field -> field.getName())
                            .collect(java.util.stream.Collectors.toSet());
                        var fieldList = obj.getChildren(com.draagon.meta.field.MetaField.class);
                        return fieldNames.size() == fieldList.size(); // No duplicates
                    }
                    return true;
                }
            );
            addConstraint(uniqueFieldNames);
            
            log.debug("Registered MetaObject cross-cutting constraints");
            
        } catch (Exception e) {
            log.error("Failed to register MetaObject constraints", e);
        }
    }
    
    // ====================
    // BACKWARD COMPATIBILITY METHODS  
    // ====================
    
}