package com.draagon.meta.registry;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataTypeId;
import com.draagon.meta.constraint.Constraint;
import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.constraint.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.Comparator;

/**
 * Unified registry for MetaData type definitions with integrated child requirements.
 * 
 * <p>This class replaces the dual registry pattern by providing:</p>
 * 
 * <ul>
 *   <li><strong>Unified Registration:</strong> Single API for type + child requirement registration</li>
 *   <li><strong>Child Validation:</strong> Built-in validation of parent-child relationships</li>
 *   <li><strong>Service Extensions:</strong> Global child requirements from service providers</li>
 *   <li><strong>OSGI Compatible:</strong> Works in both OSGI and non-OSGI environments</li>
 *   <li><strong>Thread-Safe:</strong> Optimized for read-heavy workloads with concurrent access</li>
 * </ul>
 * 
 * <h3>Registration Examples:</h3>
 * 
 * <pre>{@code
 * // Register a field type with attributes
 * MetaDataRegistry.getInstance().registerType(StringField.class, def -> def
 *     .type("field").subType("string")
 *     .description("String field with pattern validation")
 *     .optionalAttribute("pattern", "string")
 *     .optionalAttribute("required", "boolean")
 * );
 * 
 * // Register an object type that accepts fields
 * MetaDataRegistry.getInstance().registerType(MetaObject.class, def -> def
 *     .type("object").subType("base")
 *     .optionalChild("field", "*", "*")  // Any field type, any name
 * );
 * }</pre>
 * 
 * @since 6.0.0
 */
public class MetaDataRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(MetaDataRegistry.class);
    
    private static volatile MetaDataRegistry instance;
    private static final Object INSTANCE_LOCK = new Object();
    
    private final ServiceRegistry serviceRegistry;
    private final Map<MetaDataTypeId, TypeDefinition> typeDefinitions = new ConcurrentHashMap<>();
    private final Map<String, List<ChildRequirement>> globalRequirements = new ConcurrentHashMap<>();
    private final Set<TypeDefinition> deferredInheritanceTypes = ConcurrentHashMap.newKeySet();
    // Unified constraint storage using enhanced ChildRequirement
    private final List<ChildRequirement> allConstraints = Collections.synchronizedList(new ArrayList<>());

    // Integrated constraint system (merged from ConstraintRegistry)
    private final List<Constraint> constraints = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean constraintsInitialized = false;

    private volatile boolean initialized = false;
    
    /**
     * Get the singleton instance
     * 
     * @return Global MetaDataRegistry instance
     */
    public static MetaDataRegistry getInstance() {
        if (instance == null) {
            synchronized (INSTANCE_LOCK) {
                if (instance == null) {
                    instance = new MetaDataRegistry();
                    // Load core types to ensure they're available for parsing
                    instance.loadCoreTypes();
                }
            }
        }
        return instance;
    }
    
    /**
     * Create registry with custom service registry
     * 
     * @param serviceRegistry Service registry for extensions
     */
    public MetaDataRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = Objects.requireNonNull(serviceRegistry, "ServiceRegistry cannot be null");
        log.debug("Created MetaDataRegistry with {}", serviceRegistry.getDescription());
    }
    
    /**
     * Create registry with default service registry
     */
    public MetaDataRegistry() {
        this(ServiceRegistryFactory.getDefault());
    }
    
    /**
     * Register a MetaData type with fluent configuration
     * 
     * @param clazz Implementation class
     * @param configurator Configuration function for the type definition
     */
    public void registerType(Class<? extends MetaData> clazz,
                                   Consumer<TypeDefinitionBuilder> configurator) {
        TypeDefinitionBuilder builder = TypeDefinitionBuilder.forClass(clazz);
        configurator.accept(builder);
        this.register(builder.build());
    }

    /**
     * Extend an existing registered type with additional attributes/children.
     * This allows service providers to add their own attributes to core types
     * without modifying the core type definitions.
     *
     * @param metaDataClass The implementation class of the type to extend
     * @param extension Configuration function for additional attributes/children
     * @return This registry instance for chaining
     * @throws IllegalArgumentException if the type is not already registered
     */
    public MetaDataRegistry extendType(Class<? extends MetaData> metaDataClass, Consumer<TypeDefinitionBuilder> extension) {
        Objects.requireNonNull(metaDataClass, "MetaData class cannot be null");
        Objects.requireNonNull(extension, "Extension function cannot be null");

        // Find the registered type definition by implementation class
        TypeDefinition existing = null;
        MetaDataTypeId typeIdToExtend = null;

        for (Map.Entry<MetaDataTypeId, TypeDefinition> entry : typeDefinitions.entrySet()) {
            if (entry.getValue().getImplementationClass().equals(metaDataClass)) {
                existing = entry.getValue();
                typeIdToExtend = entry.getKey();
                break;
            }
        }

        if (existing == null) {
            throw new IllegalArgumentException(
                "Type must be registered before extension: " + metaDataClass.getName() +
                ". Available types: " + getRegisteredTypeNames()
            );
        }

        // Create a builder from the existing definition
        TypeDefinitionBuilder builder = TypeDefinitionBuilder.from(existing);

        // Apply the extension
        extension.accept(builder);

        // Update the registered type with extended definition
        TypeDefinition extendedDefinition = builder.build();
        typeDefinitions.put(typeIdToExtend, extendedDefinition);

        log.debug("Extended type: {} with additional attributes/children",
                 typeIdToExtend.toQualifiedName());

        return this;
    }

    /**
     * Register multiple types that use standardized registerTypes() pattern.
     * This method calls the static registerTypes() method on each class if it exists.
     *
     * @param typeClasses Classes that implement the registerTypes() pattern
     */
    @SafeVarargs
    public static void registerTypes(Class<? extends MetaData>... typeClasses) {
        for (Class<? extends MetaData> typeClass : typeClasses) {
            if (typeClass == null) {
                continue; // Skip null classes from getClassSafely()
            }

            try {
                // Look for a static registerTypes(MetaDataRegistry) method
                var registerMethod = typeClass.getMethod("registerTypes", MetaDataRegistry.class);
                registerMethod.invoke(null, getInstance());
            } catch (NoSuchMethodException e) {
                // Fallback: Try to find static registerTypes() method with no parameters
                try {
                    var singleRegisterMethod = typeClass.getMethod("registerTypes");
                    singleRegisterMethod.invoke(null);
                } catch (NoSuchMethodException e2) {
                    log.warn("Class {} does not have registerTypes() method - skipping", typeClass.getName());
                } catch (Exception e2) {
                    log.error("Failed to call registerTypes() on {}: {}", typeClass.getName(), e2.getMessage(), e2);
                }
            } catch (Exception e) {
                log.error("Failed to register types from {}: {}", typeClass.getName(), e.getMessage(), e);
            }
        }
    }

    /**
     * Register all core MetaData types using the standardized registerTypes() pattern.
     * This is a centralized method for registering all core types in proper order.
     */
    public static void registerAllCoreTypes() {
        try {
            log.info("Registering all core MetaData types using standardized registerTypes() pattern...");

            // PHASE 1: Base Types (foundation types that others inherit from)
            log.debug("Registering base types...");
            // Note: Base types would be registered here if they had registerTypes() methods

            // PHASE 2: Field Types
            log.debug("Registering field types...");
            registerTypes(
                getClassSafely("com.draagon.meta.field.MetaField"),
                getClassSafely("com.draagon.meta.field.StringField"),
                getClassSafely("com.draagon.meta.field.IntegerField"),
                getClassSafely("com.draagon.meta.field.LongField"),
                getClassSafely("com.draagon.meta.field.DoubleField"),
                getClassSafely("com.draagon.meta.field.FloatField"),
                getClassSafely("com.draagon.meta.field.BooleanField"),
                getClassSafely("com.draagon.meta.field.ByteField"),
                getClassSafely("com.draagon.meta.field.ShortField"),
                getClassSafely("com.draagon.meta.field.DateField"),
                getClassSafely("com.draagon.meta.field.ClassField"),
                getClassSafely("com.draagon.meta.field.ObjectField"),
                getClassSafely("com.draagon.meta.field.ObjectArrayField"),
                getClassSafely("com.draagon.meta.field.StringArrayField"),
                getClassSafely("com.draagon.meta.field.TimestampField")
            );

            // PHASE 3: Attribute Types
            log.debug("Registering attribute types...");
            registerTypes(
                getClassSafely("com.draagon.meta.attr.MetaAttribute"),
                getClassSafely("com.draagon.meta.attr.StringAttribute"),
                getClassSafely("com.draagon.meta.attr.IntAttribute"),
                getClassSafely("com.draagon.meta.attr.BooleanAttribute"),
                getClassSafely("com.draagon.meta.attr.ClassAttribute"),
                getClassSafely("com.draagon.meta.attr.DoubleAttribute"),
                getClassSafely("com.draagon.meta.attr.LongAttribute"),
                getClassSafely("com.draagon.meta.attr.PropertiesAttribute"),
                getClassSafely("com.draagon.meta.attr.StringArrayAttribute")
            );

            // PHASE 4: Object Types
            log.debug("Registering object types...");
            registerTypes(
                getClassSafely("com.draagon.meta.object.MetaObject"),
                getClassSafely("com.draagon.meta.object.pojo.PojoMetaObject"),
                getClassSafely("com.draagon.meta.object.mapped.MappedMetaObject"),
                getClassSafely("com.draagon.meta.object.proxy.ProxyMetaObject")
            );

            log.info("Core type registration completed successfully - {} types registered",
                    getInstance().getRegisteredTypes().size());

        } catch (Exception e) {
            log.error("Failed to register core types using standardized pattern: {}", e.getMessage(), e);
        }
    }

    /**
     * Safely get a class by name, filtering out missing classes.
     *
     * @param className Full class name
     * @return Class instance, or null if not found
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends MetaData> getClassSafely(String className) {
        try {
            return (Class<? extends MetaData>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.debug("Class {} not available - skipping", className);
            return null;
        }
    }

    /**
     * Register a type definition with inheritance resolution
     *
     * @param definition Complete type definition
     */
    public void register(TypeDefinition definition) {
        MetaDataTypeId typeId = new MetaDataTypeId(definition.getType(), definition.getSubType());

        TypeDefinition existing = typeDefinitions.get(typeId);
        if (existing != null && !existing.getImplementationClass().equals(definition.getImplementationClass())) {
            throw new MetaDataException(
                "Type already registered with different implementation: " + typeId.toQualifiedName() +
                ". Existing: " + existing.getImplementationClass().getName() +
                ", New: " + definition.getImplementationClass().getName()
            );
        }

        // Resolve inheritance if this type has a parent
        resolveInheritance(definition);

        typeDefinitions.put(typeId, definition);
        log.debug("Registered type: {} -> {} (parent: {})", typeId.toQualifiedName(),
                 definition.getImplementationClass().getSimpleName(),
                 definition.hasParent() ? definition.getParentQualifiedName() : "none");

        // Try to resolve any deferred inheritance that might now be possible
        if (!deferredInheritanceTypes.isEmpty()) {
            resolveDeferredInheritance();
        }
    }

    /**
     * Resolve inheritance for a type definition by populating inherited requirements from parent
     *
     * @param definition Type definition to resolve inheritance for
     */
    private void resolveInheritance(TypeDefinition definition) {
        if (!definition.hasParent()) {
            return; // No inheritance to resolve
        }

        MetaDataTypeId parentTypeId = new MetaDataTypeId(definition.getParentType(), definition.getParentSubType());
        TypeDefinition parentDefinition = typeDefinitions.get(parentTypeId);

        if (parentDefinition == null) {
            // Defer inheritance resolution for later when parent might be available
            deferredInheritanceTypes.add(definition);
            log.debug("Deferring inheritance for {} - parent {} not yet registered",
                    definition.getQualifiedName(), parentTypeId.toQualifiedName());
            return;
        }

        // Delegate to extracted method for consistency
        resolveInheritanceForDefinition(definition, parentDefinition);
    }
    
    /**
     * Create a new MetaData instance
     * 
     * @param <T> Expected MetaData type
     * @param type Primary type (e.g., "field", "object")
     * @param subType Specific subtype (e.g., "string", "int")
     * @param name Instance name
     * @return New MetaData instance
     */
    @SuppressWarnings("unchecked")
    public <T extends MetaData> T createInstance(String type, String subType, String name) {
        Objects.requireNonNull(type, "Type cannot be null");
        Objects.requireNonNull(subType, "SubType cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        
        MetaDataTypeId typeId = new MetaDataTypeId(type, subType);
        TypeDefinition definition = typeDefinitions.get(typeId);
        
        if (definition == null) {
            throw new MetaDataException(
                "No type registered for: " + typeId.toQualifiedName() + 
                ". Available types: " + getRegisteredTypeNames()
            );
        }
        
        try {
            Class<? extends MetaData> implClass = definition.getImplementationClass();
            
            // Try the standard 3-parameter constructor first
            try {
                Constructor<? extends MetaData> constructor = implClass.getConstructor(
                    String.class, String.class, String.class);
                return (T) constructor.newInstance(type, subType, name);
            } catch (NoSuchMethodException e) {
                // Fall back to other constructor patterns
                try {
                    Constructor<? extends MetaData> constructor = implClass.getConstructor(
                        String.class, String.class);
                    return (T) constructor.newInstance(subType, name);
                } catch (NoSuchMethodException e2) {
                    Constructor<? extends MetaData> constructor = implClass.getConstructor(String.class);
                    return (T) constructor.newInstance(name);
                }
            }
            
        } catch (Exception e) {
            throw new MetaDataException(
                "Failed to create instance of type: " + typeId.toQualifiedName() + 
                " with class: " + definition.getImplementationClass().getName(), e);
        }
    }
    
    /**
     * Get type definition by type and subtype
     *
     * @param type Primary type
     * @param subType Specific subtype
     * @return TypeDefinition if found, null otherwise
     */
    public TypeDefinition getTypeDefinition(String type, String subType) {
        return typeDefinitions.get(new MetaDataTypeId(type, subType));
    }

    /**
     * Get type definition by MetaDataTypeId
     *
     * @param typeId Type identifier
     * @return TypeDefinition if found, null otherwise
     */
    public TypeDefinition getTypeDefinition(MetaDataTypeId typeId) {
        return typeDefinitions.get(typeId);
    }
    
    /**
     * Check if a parent type accepts a specific child
     * 
     * @param parentType Parent type (e.g., "field", "object")
     * @param parentSubType Parent subType (e.g., "string", "base")
     * @param childType Child type (e.g., "attr", "field")
     * @param childSubType Child subType (e.g., "string", "boolean")
     * @param childName Child name (e.g., "pattern", "required")
     * @return true if the parent accepts this child
     */
    public boolean acceptsChild(String parentType, String parentSubType,
                              String childType, String childSubType, String childName) {
        TypeDefinition parentDef = getTypeDefinition(parentType, parentSubType);
        if (parentDef == null) {
            return false;
        }
        
        // Check type-specific requirements
        if (parentDef.acceptsChild(childType, childSubType, childName)) {
            return true;
        }
        
        // Check global requirements (from service providers)
        String parentKey = parentType + "." + parentSubType;
        List<ChildRequirement> globalReqs = globalRequirements.get(parentKey);
        if (globalReqs != null) {
            for (ChildRequirement req : globalReqs) {
                if (req.matches(childType, childSubType, childName)) {
                    return true;
                }
            }
        }
        
        // Check wildcard global requirements
        String wildcardKey = parentType + ".*";
        List<ChildRequirement> wildcardReqs = globalRequirements.get(wildcardKey);
        if (wildcardReqs != null) {
            for (ChildRequirement req : wildcardReqs) {
                if (req.matches(childType, childSubType, childName)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Get all child requirements for a parent type
     * 
     * @param parentType Parent type
     * @param parentSubType Parent subType
     * @return List of all child requirements (type-specific + global)
     */
    public List<ChildRequirement> getChildRequirements(String parentType, String parentSubType) {
        List<ChildRequirement> requirements = new ArrayList<>();
        
        // Add type-specific requirements
        TypeDefinition parentDef = getTypeDefinition(parentType, parentSubType);
        if (parentDef != null) {
            requirements.addAll(parentDef.getChildRequirements());
        }
        
        // Add global requirements
        String parentKey = parentType + "." + parentSubType;
        List<ChildRequirement> globalReqs = globalRequirements.get(parentKey);
        if (globalReqs != null) {
            requirements.addAll(globalReqs);
        }
        
        // Add wildcard global requirements
        String wildcardKey = parentType + ".*";
        List<ChildRequirement> wildcardReqs = globalRequirements.get(wildcardKey);
        if (wildcardReqs != null) {
            requirements.addAll(wildcardReqs);
        }
        
        return requirements;
    }
    
    /**
     * Get a specific child requirement by name
     * 
     * @param parentType Parent type
     * @param parentSubType Parent subType
     * @param childName Child name to look up
     * @return ChildRequirement if found, null otherwise
     */
    public ChildRequirement getChildRequirement(String parentType, String parentSubType, String childName) {
        // Check type-specific requirements first
        TypeDefinition parentDef = getTypeDefinition(parentType, parentSubType);
        if (parentDef != null) {
            ChildRequirement req = parentDef.getChildRequirement(childName);
            if (req != null) {
                return req;
            }
        }
        
        // Check global requirements
        List<ChildRequirement> allReqs = getChildRequirements(parentType, parentSubType);
        for (ChildRequirement req : allReqs) {
            if (childName.equals(req.getName())) {
                return req;
            }
        }
        
        return null;
    }
    
    /**
     * Add a global child requirement (used by service providers)
     *
     * @param parentType Parent type pattern ("field", "object", "*")
     * @param parentSubType Parent subType pattern ("string", "base", "*")
     * @param requirement Child requirement to add
     */
    public void addGlobalChildRequirement(String parentType, String parentSubType, ChildRequirement requirement) {
        String key = parentType + "." + parentSubType;
        globalRequirements.computeIfAbsent(key, k -> new ArrayList<>()).add(requirement);

        log.debug("Added global child requirement: {} accepts {}", key, requirement.getDescription());
    }

    // ========== UNIFIED CONSTRAINT SUPPORT ==========

    /**
     * Register a placement constraint using the unified constraint system
     *
     * @param constraintId Unique constraint identifier
     * @param description Human-readable description of the placement rule
     * @param parentMatcher Predicate to test if a parent MetaData can contain the child
     * @param childMatcher Predicate to test if a child MetaData can be placed under the parent
     */
    public void registerPlacementConstraint(String constraintId, String description,
                                           Predicate<MetaData> parentMatcher,
                                           Predicate<MetaData> childMatcher) {
        ChildRequirement constraint = ChildRequirement.placementConstraint(
            constraintId, description, parentMatcher, childMatcher);
        allConstraints.add(constraint);
        log.debug("Registered placement constraint: {} - {}", constraintId, description);
    }

    /**
     * Register a validation constraint using the unified constraint system
     *
     * @param constraintId Unique constraint identifier
     * @param description Human-readable description of the validation rule
     * @param applicabilityTest Predicate to determine which MetaData this constraint applies to
     * @param valueValidator Custom value validation logic
     */
    public void registerValidationConstraint(String constraintId, String description,
                                            Predicate<MetaData> applicabilityTest,
                                            BiPredicate<MetaData, Object> valueValidator) {
        ChildRequirement constraint = ChildRequirement.validationConstraint(
            constraintId, description, applicabilityTest, valueValidator);
        allConstraints.add(constraint);
        log.debug("Registered validation constraint: {} - {}", constraintId, description);
    }

    /**
     * Register a constraint directly using ChildRequirement
     *
     * @param constraint The constraint to register
     */
    public void registerConstraint(ChildRequirement constraint) {
        if (constraint == null) {
            log.warn("Attempted to register null constraint - ignoring");
            return;
        }
        allConstraints.add(constraint);
        log.debug("Registered constraint: {} - {}", constraint.getConstraintId(), constraint.getDescription());
    }

    /**
     * Get all registered constraints
     *
     * @return List of all constraints (read-only view)
     */
    public List<ChildRequirement> getAllConstraints() {
        return Collections.unmodifiableList(allConstraints);
    }

    /**
     * Get all placement constraints
     *
     * @return List of placement constraints
     */
    public List<ChildRequirement> getPlacementConstraints() {
        return allConstraints.stream()
            .filter(ChildRequirement::isPlacementConstraint)
            .collect(Collectors.toList());
    }

    /**
     * Get all validation constraints
     *
     * @return List of validation constraints
     */
    public List<ChildRequirement> getValidationConstraints() {
        return allConstraints.stream()
            .filter(ChildRequirement::isValidationConstraint)
            .collect(Collectors.toList());
    }

    /**
     * Validate a parent-child placement using unified constraint system
     *
     * @param parent The parent MetaData
     * @param child The child MetaData to be added
     * @throws ConstraintViolationException If placement is not allowed
     */
    public void validatePlacement(MetaData parent, MetaData child) throws ConstraintViolationException {
        // First check traditional child requirements
        boolean allowedByChildRequirements = acceptsChild(
            parent.getType(), parent.getSubType(),
            child.getType(), child.getSubType(), child.getName());

        // Then check placement constraints
        List<ChildRequirement> placementConstraints = getPlacementConstraints();
        boolean allowedByPlacementConstraints = placementConstraints.isEmpty(); // Default allow if no constraints

        for (ChildRequirement constraint : placementConstraints) {
            if (constraint.isPlacementAllowed(parent, child)) {
                allowedByPlacementConstraints = true;
                break; // At least one constraint allows it
            }
        }

        if (!allowedByChildRequirements && !allowedByPlacementConstraints) {
            throw new ConstraintViolationException(
                String.format("Child %s[%s] cannot be placed under parent %s[%s]",
                    child.getClass().getSimpleName(), child.getName(),
                    parent.getClass().getSimpleName(), parent.getName()),
                "placement.validation",
                parent
            );
        }
    }

    /**
     * Validate a value using unified constraint system
     *
     * @param metaData The metadata object being validated
     * @param value The value being validated
     * @throws ConstraintViolationException If validation fails
     */
    public void validateValue(MetaData metaData, Object value)
            throws ConstraintViolationException {
        List<ChildRequirement> validationConstraints = getValidationConstraints();

        for (ChildRequirement constraint : validationConstraints) {
            try {
                constraint.validateValue(metaData, value);
            } catch (ConstraintViolationException e) {
                // Re-throw with additional context
                throw new ConstraintViolationException(
                    e.getMessage() + " (constraint: " + constraint.getConstraintId() + ")",
                    e.getConstraintType(),
                    e.getViolatingValue(),
                    e.getMetaData()
                );
            }
        }
    }

    /**
     * Get constraint statistics
     *
     * @return Map of constraint types to counts
     */
    public Map<String, Integer> getConstraintStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", allConstraints.size());
        stats.put("placement", (int) allConstraints.stream().filter(ChildRequirement::isPlacementConstraint).count());
        stats.put("validation", (int) allConstraints.stream().filter(ChildRequirement::isValidationConstraint).count());
        return stats;
    }
    
    /**
     * Get human-readable description of supported children for error messages
     * 
     * @param parentType Parent type
     * @param parentSubType Parent subType
     * @return Description of all supported children
     */
    public String getSupportedChildrenDescription(String parentType, String parentSubType) {
        TypeDefinition typeDef = getTypeDefinition(parentType, parentSubType);
        StringBuilder result = new StringBuilder();
        
        // Include the type's own description
        if (typeDef != null && typeDef.getDescription() != null) {
            result.append(typeDef.getDescription());
        }
        
        List<ChildRequirement> requirements = getChildRequirements(parentType, parentSubType);
        
        if (!requirements.isEmpty()) {
            List<String> descriptions = requirements.stream()
                .map(ChildRequirement::getDescription)
                .collect(Collectors.toList());
            
            if (result.length() > 0) {
                result.append(". ");
            }
            result.append("Supports: ").append(String.join(", ", descriptions));
        } else if (result.length() == 0) {
            return "No children supported";
        }
        
        return result.toString();
    }
    
    /**
     * Check if a type is registered
     * 
     * @param type Primary type
     * @param subType Specific subtype
     * @return true if registered
     */
    public boolean isRegistered(String type, String subType) {
        return typeDefinitions.containsKey(new MetaDataTypeId(type, subType));
    }
    
    /**
     * Check if any type is registered with the given primary type name
     * 
     * @param type Primary type name (e.g., "field", "view", "validator")
     * @return true if any subtype is registered for this primary type
     */
    public boolean hasType(String type) {
        Objects.requireNonNull(type, "Type cannot be null");
        
        return typeDefinitions.keySet().stream()
            .anyMatch(typeId -> type.equals(typeId.type()));
    }
    
    // Deprecated loader registration methods removed
    
    /**
     * Get all registered type identifiers
     * 
     * @return Set of registered MetaDataTypeId instances
     */
    public Set<MetaDataTypeId> getRegisteredTypes() {
        return Set.copyOf(typeDefinitions.keySet());
    }
    
    // Deprecated getDefaultSubType method removed
    
    /**
     * Get all registered type names for display
     * 
     * @return Set of qualified type names like "field.string", "object.base"
     */
    public Set<String> getRegisteredTypeNames() {
        return typeDefinitions.keySet().stream()
            .map(MetaDataTypeId::toQualifiedName)
            .collect(Collectors.toSet());
    }
    
    /**
     * Get all type definitions
     * 
     * @return Collection of all registered type definitions
     */
    public Collection<TypeDefinition> getAllTypeDefinitions() {
        return Collections.unmodifiableCollection(typeDefinitions.values());
    }
    
    /**
     * Clear all registrations (primarily for testing)
     */
    public void clear() {
        typeDefinitions.clear();
        globalRequirements.clear();
        initialized = false;
        log.debug("Cleared all type registrations");
    }
    
    /**
     * Get registry statistics
     * 
     * @return RegistryStats with type counts and service information
     */
    public RegistryStats getStats() {
        Map<String, Integer> typesByPrimary = new HashMap<>();
        for (MetaDataTypeId typeId : typeDefinitions.keySet()) {
            typesByPrimary.merge(typeId.type(), 1, Integer::sum);
        }
        
        int globalRequirementCount = globalRequirements.values().stream()
            .mapToInt(List::size)
            .sum();
        
        return new RegistryStats(
            typeDefinitions.size(),
            typesByPrimary,
            globalRequirementCount,
            serviceRegistry.getDescription(),
            getConstraintStats()
        );
    }
    
    /**
     * Ensure service-based extensions are loaded
     */
    private void ensureInitialized() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    loadServiceBasedExtensions();
                    initialized = true;
                }
            }
        }
    }
    
    /**
     * Load service-based extensions that add global child requirements
     */
    private void loadServiceBasedExtensions() {
        // Plugin system removed - all types now self-register via static blocks
        // Database attributes are registered via DatabaseAttributeRegistration
        log.debug("Service-based extension loading completed - using self-registration pattern");
    }
    
    
    // Legacy provider loading removed - using unified plugin approach
    
    /**
     * Force load core types by triggering their static blocks
     */
    private void loadCoreTypes() {
        try {
            // Force loading of core field types
            Class.forName("com.draagon.meta.field.StringField");
            Class.forName("com.draagon.meta.field.IntegerField");
            Class.forName("com.draagon.meta.field.LongField");
            Class.forName("com.draagon.meta.field.DoubleField");
            Class.forName("com.draagon.meta.field.FloatField");
            Class.forName("com.draagon.meta.field.BooleanField");
            Class.forName("com.draagon.meta.field.ByteField");
            Class.forName("com.draagon.meta.field.ShortField");
            Class.forName("com.draagon.meta.field.DateField");
            Class.forName("com.draagon.meta.field.ClassField");
            Class.forName("com.draagon.meta.field.ObjectField");
            Class.forName("com.draagon.meta.field.ObjectArrayField");
            Class.forName("com.draagon.meta.field.StringArrayField");
            
            // Force loading of core object types
            Class.forName("com.draagon.meta.object.MetaObject");
            Class.forName("com.draagon.meta.object.pojo.PojoMetaObject");
            Class.forName("com.draagon.meta.object.mapped.MappedMetaObject");
            Class.forName("com.draagon.meta.object.proxy.ProxyMetaObject");
            
            // Force loading of attribute types
            Class.forName("com.draagon.meta.attr.StringAttribute");
            Class.forName("com.draagon.meta.attr.IntAttribute");
            Class.forName("com.draagon.meta.attr.BooleanAttribute");
            
            // Force loading of validator types
            Class.forName("com.draagon.meta.validator.RequiredValidator");
            Class.forName("com.draagon.meta.validator.LengthValidator");
            
            // Force loading of key types
            Class.forName("com.draagon.meta.key.PrimaryKey");
            Class.forName("com.draagon.meta.key.ForeignKey");
            Class.forName("com.draagon.meta.key.SecondaryKey");
            
            log.debug("Core types loaded successfully");

            // Load service providers after core types are available
            loadServiceProviders();

        } catch (ClassNotFoundException e) {
            log.warn("Some core types could not be loaded: {}", e.getMessage());
        }
    }

    /**
     * Find a type for extension by type and subtype.
     *
     * <p>This method returns a TypeExtensionBuilder that allows service providers
     * to add optional attributes and child requirements to existing types.</p>
     *
     * @param type The primary type (e.g., "field", "object")
     * @param subType The subtype (e.g., "string", "pojo")
     * @return TypeExtensionBuilder for extending the type
     * @throws IllegalArgumentException if the type is not found
     */
    public TypeExtensionBuilder findType(String type, String subType) {
        MetaDataTypeId typeId = new MetaDataTypeId(type, subType);
        TypeDefinition existing = typeDefinitions.get(typeId);

        if (existing == null) {
            // Try to be helpful with error message
            String availableTypes = typeDefinitions.keySet().stream()
                .filter(id -> id.type().equals(type))
                .map(id -> id.type() + "." + id.subType())
                .collect(Collectors.joining(", "));

            if (availableTypes.isEmpty()) {
                availableTypes = typeDefinitions.keySet().stream()
                    .limit(10)
                    .map(id -> id.type() + "." + id.subType())
                    .collect(Collectors.joining(", "));
                throw new IllegalArgumentException(
                    "Type '" + type + "." + subType + "' not found. No types with primary type '" + type + "' are registered. " +
                    "Available types include: " + availableTypes);
            } else {
                throw new IllegalArgumentException(
                    "Type '" + type + "." + subType + "' not found. Available " + type + " types: " + availableTypes);
            }
        }

        return new TypeExtensionBuilder(this, existing, typeId);
    }

    /**
     * Load service providers via ServiceLoader to extend MetaData types.
     *
     * <p>This method discovers MetaDataTypeProvider implementations from META-INF/services files
     * and delegates to them to extend existing MetaData types with service-specific attributes.</p>
     *
     * <p>Providers are loaded in priority order (lower = higher priority) to ensure proper
     * dependency ordering for type extensions.</p>
     */
    private void loadServiceProviders() {
        try {
            // Get all MetaDataTypeProvider implementations via ServiceLoader
            Collection<MetaDataTypeProvider> providers = serviceRegistry.getServices(MetaDataTypeProvider.class);

            if (providers.isEmpty()) {
                log.debug("No MetaDataTypeProvider services found");
                return;
            }

            // Sort providers by priority (lower = higher priority)
            List<MetaDataTypeProvider> sortedProviders = providers.stream()
                .sorted(Comparator.comparing(MetaDataTypeProvider::getPriority))
                .collect(Collectors.toList());

            log.info("Loading {} MetaDataTypeProvider services in priority order", sortedProviders.size());

            // Register type extensions from each provider
            for (MetaDataTypeProvider provider : sortedProviders) {
                try {
                    long startTime = System.currentTimeMillis();
                    provider.registerTypes(this);
                    long duration = System.currentTimeMillis() - startTime;

                    log.debug("Loaded provider: {} (priority {}) in {}ms - {}",
                             provider.getClass().getSimpleName(),
                             provider.getPriority(),
                             duration,
                             provider.getDescription());

                } catch (Exception e) {
                    log.error("Failed to load MetaDataTypeProvider: {} - {}",
                             provider.getClass().getName(), e.getMessage(), e);
                    // Continue with other providers - don't fail completely
                }
            }

            log.info("Successfully loaded {} MetaDataTypeProvider services", sortedProviders.size());

        } catch (Exception e) {
            log.error("Error during service provider loading: {}", e.getMessage(), e);
        }
    }

    /**
     * Resolve deferred inheritance for types whose parents weren't available during initial registration.
     * This method should be called after all static type registrations have completed.
     *
     * @return Number of deferred types that were successfully resolved
     */
    public int resolveDeferredInheritance() {
        if (deferredInheritanceTypes.isEmpty()) {
            return 0;
        }

        Set<TypeDefinition> resolved = new HashSet<>();
        Set<TypeDefinition> stillDeferred = new HashSet<>();

        for (TypeDefinition definition : deferredInheritanceTypes) {
            MetaDataTypeId parentTypeId = new MetaDataTypeId(definition.getParentType(), definition.getParentSubType());
            TypeDefinition parentDefinition = typeDefinitions.get(parentTypeId);

            if (parentDefinition != null) {
                try {
                    // Resolve inheritance now that parent is available
                    resolveInheritanceForDefinition(definition, parentDefinition);
                    resolved.add(definition);
                    log.debug("Resolved deferred inheritance for {} from parent {}",
                            definition.getQualifiedName(), parentTypeId.toQualifiedName());
                } catch (Exception e) {
                    log.warn("Failed to resolve deferred inheritance for {}: {}",
                            definition.getQualifiedName(), e.getMessage());
                    stillDeferred.add(definition);
                }
            } else {
                stillDeferred.add(definition);
                log.warn("Parent type {} still not found for {} during deferred resolution",
                        parentTypeId.toQualifiedName(), definition.getQualifiedName());
            }
        }

        // Update deferred set with remaining unresolved types
        deferredInheritanceTypes.clear();
        deferredInheritanceTypes.addAll(stillDeferred);

        int resolvedCount = resolved.size();
        if (resolvedCount > 0) {
            log.info("Resolved deferred inheritance for {} types, {} still deferred",
                    resolvedCount, stillDeferred.size());
        }

        return resolvedCount;
    }

    /**
     * Extract the inheritance resolution logic so it can be reused for deferred resolution
     */
    private void resolveInheritanceForDefinition(TypeDefinition definition, TypeDefinition parentDefinition) {
        // Get all requirements from parent (direct + inherited)
        Map<String, ChildRequirement> parentRequirements = new HashMap<>();

        // Add parent's direct requirements using proper key generation logic
        for (ChildRequirement req : parentDefinition.getDirectChildRequirements()) {
            String key = req.getName();
            if ("*".equals(key)) {
                // For wildcard requirements, create unique keys to avoid overwrites
                key = "*:" + req.getExpectedType() + ":" + req.getExpectedSubType();
            }
            parentRequirements.put(key, req);
        }

        // Add parent's inherited requirements (recursive inheritance) - these already use unique keys
        parentRequirements.putAll(parentDefinition.getInheritedChildRequirements());

        // Populate inherited requirements in the child definition
        definition.populateInheritedRequirements(parentRequirements);

        log.debug("Inheritance resolved for {}: {} requirements inherited from parent {}",
                definition.getQualifiedName(), parentRequirements.size(),
                parentDefinition.getQualifiedName());
    }

    // ========== REGISTRY HEALTH VALIDATION ==========

    /**
     * Validate registry consistency and architectural compliance.
     * This method performs deferred validation after all registrations complete
     * to avoid order dependency issues during static initialization.
     *
     * @return RegistryHealthReport with validation results and recommendations
     */
    public RegistryHealthReport validateConsistency() {
        RegistryHealthReport report = new RegistryHealthReport();

        // Ensure all types are loaded
        ensureInitialized();

        // Collect statistics for the report
        populateRegistryStatistics(report);

        // Validate base type consistency
        validateBaseTypeConsistency(report);

        // Validate inheritance patterns
        validateInheritancePatterns(report);

        // Validate structural integrity
        validateStructuralIntegrity(report);

        return report;
    }

    /**
     * Populate registry statistics in the health report
     */
    private void populateRegistryStatistics(RegistryHealthReport report) {
        Map<String, Set<String>> typeToSubTypes = new HashMap<>();
        Set<String> allTypes = new HashSet<>();

        for (MetaDataTypeId typeId : typeDefinitions.keySet()) {
            String type = typeId.type();
            String subType = typeId.subType();

            allTypes.add(type);
            typeToSubTypes.computeIfAbsent(type, k -> new HashSet<>()).add(subType);
        }

        report.addMetadata("totalTypes", typeDefinitions.size());
        report.addMetadata("primaryTypes", allTypes.size());
        report.addMetadata("typeToSubTypes", typeToSubTypes);
    }

    /**
     * Validate that all type families have base subtypes
     */
    private void validateBaseTypeConsistency(RegistryHealthReport report) {
        Map<String, Set<String>> typeToSubTypes = getTypeToSubTypesMap();
        Set<String> typesWithBase = new HashSet<>();
        Set<String> typesWithoutBase = new HashSet<>();

        for (String type : typeToSubTypes.keySet()) {
            if (typeToSubTypes.get(type).contains("base")) {
                typesWithBase.add(type);
            } else {
                typesWithoutBase.add(type);
            }
        }

        report.addMetadata("typesWithBase", typesWithBase);
        report.addMetadata("typesWithoutBase", typesWithoutBase);
        report.addMetadata("missingBaseTypes", typesWithoutBase);

        // Add warnings for missing base types
        for (String type : typesWithoutBase) {
            report.addWarning("Type family '" + type + "' missing recommended base subtype");
            report.addRecommendation("Consider adding " + type + ".base for inheritance support");
        }

        // Log success for types with bases
        if (!typesWithBase.isEmpty()) {
            report.addMetadata("baseTypeCompliance",
                String.format("%d/%d type families have base subtypes",
                    typesWithBase.size(), typeToSubTypes.size()));
        }
    }

    /**
     * Validate inheritance patterns are working correctly
     */
    private void validateInheritancePatterns(RegistryHealthReport report) {
        int typesWithInheritance = 0;
        int typesInheritingFromBase = 0;
        List<String> inheritanceChain = new ArrayList<>();

        for (TypeDefinition definition : typeDefinitions.values()) {
            if (definition.hasParent()) {
                typesWithInheritance++;
                inheritanceChain.add(definition.getQualifiedName() + " → " + definition.getParentQualifiedName());

                if ("base".equals(definition.getParentSubType())) {
                    typesInheritingFromBase++;
                }
            }
        }

        report.addMetadata("typesWithInheritance", typesWithInheritance);
        report.addMetadata("typesInheritingFromBase", typesInheritingFromBase);
        report.addMetadata("inheritanceChains", inheritanceChain);

        // Check if inheritance is being utilized effectively
        if (typesWithInheritance == 0) {
            report.addWarning("No types use inheritance - consider using base types for shared attributes");
        } else if (typesInheritingFromBase == 0) {
            report.addWarning("Types have inheritance but none inherit from base types");
        }

        // Check for deferred inheritance issues
        if (!deferredInheritanceTypes.isEmpty()) {
            report.addError("Unresolved inheritance dependencies: " +
                deferredInheritanceTypes.size() + " types have missing parent types");

            for (TypeDefinition deferred : deferredInheritanceTypes) {
                report.addError("Type " + deferred.getQualifiedName() +
                    " cannot find parent " + deferred.getParentQualifiedName());
            }
        }
    }

    /**
     * Validate structural integrity of the registry
     */
    private void validateStructuralIntegrity(RegistryHealthReport report) {
        // Check for duplicate implementations
        Map<Class<?>, List<String>> implementationToTypes = new HashMap<>();

        for (Map.Entry<MetaDataTypeId, TypeDefinition> entry : typeDefinitions.entrySet()) {
            Class<?> implClass = entry.getValue().getImplementationClass();
            String typeName = entry.getKey().toQualifiedName();

            implementationToTypes.computeIfAbsent(implClass, k -> new ArrayList<>()).add(typeName);
        }

        // Report duplicate implementations (usually indicates problems)
        for (Map.Entry<Class<?>, List<String>> entry : implementationToTypes.entrySet()) {
            if (entry.getValue().size() > 1) {
                report.addWarning("Class " + entry.getKey().getSimpleName() +
                    " implements multiple types: " + entry.getValue());
            }
        }

        // Validate core types are present
        validateCoreTypesPresent(report);
    }

    /**
     * Validate that expected core types are registered
     */
    private void validateCoreTypesPresent(RegistryHealthReport report) {
        String[] expectedCoreTypes = {
            "field.base", "object.base", "attr.base", "validator.base", "key.base"
        };

        List<String> missingCoreTypes = new ArrayList<>();
        for (String coreType : expectedCoreTypes) {
            String[] parts = coreType.split("\\.");
            if (!isRegistered(parts[0], parts[1])) {
                missingCoreTypes.add(coreType);
            }
        }

        if (!missingCoreTypes.isEmpty()) {
            report.addError("Missing core base types: " + missingCoreTypes);
            report.addRecommendation("Ensure all base types are registered during static initialization");
        } else {
            report.addMetadata("coreTypesComplete", "All expected core base types present");
        }
    }

    /**
     * Get type to subtypes mapping for analysis
     */
    private Map<String, Set<String>> getTypeToSubTypesMap() {
        Map<String, Set<String>> typeToSubTypes = new HashMap<>();

        for (MetaDataTypeId typeId : typeDefinitions.keySet()) {
            typeToSubTypes.computeIfAbsent(typeId.type(), k -> new HashSet<>()).add(typeId.subType());
        }

        return typeToSubTypes;
    }

    /**
     * Check if registry has any missing base types
     *
     * @return true if any type families are missing base subtypes
     */
    public boolean hasMissingBaseTypes() {
        return !getMissingBaseTypes().isEmpty();
    }

    /**
     * Get set of type names that are missing base subtypes
     *
     * @return Set of type names missing base subtypes
     */
    public Set<String> getMissingBaseTypes() {
        Map<String, Set<String>> typeToSubTypes = getTypeToSubTypesMap();
        Set<String> missingBases = new HashSet<>();

        for (String type : typeToSubTypes.keySet()) {
            if (!typeToSubTypes.get(type).contains("base")) {
                missingBases.add(type);
            }
        }

        return missingBases;
    }

    // ====================== INTEGRATED CONSTRAINT SYSTEM ======================

    /**
     * Load core constraints into the registry (migrated from ConstraintRegistry)
     */
    private void loadCoreConstraints() {
        if (constraintsInitialized) {
            return;
        }

        synchronized (this) {
            if (constraintsInitialized) {
                return;
            }

            try {
                // Load essential constraints using concrete classes
                loadPlacementConstraints();

                // Load all constraints previously provided by ConstraintProviders
                loadCodegenConstraints();
                loadCoreIOConstraints();
                loadWebConstraints();

                log.info("Loaded {} core constraints using concrete constraint classes",
                         constraints.size());
                constraintsInitialized = true;

            } catch (Exception e) {
                log.error("Error loading core constraints: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Load naming pattern constraints
     */

    /**
     * Load placement constraints
     */
    private void loadPlacementConstraints() {
        // StringField can optionally have maxLength attribute
        addConstraint(new PlacementConstraint(
            "stringfield.maxlength.placement",
            "String fields can optionally have maxLength attribute",
            (parent) -> parent.getClass().getSimpleName().equals("StringField"),
            (child) -> child instanceof com.draagon.meta.attr.IntAttribute &&
                      "maxLength".equals(child.getName())
        ));

        // MetaField can optionally have required attribute
        addConstraint(new PlacementConstraint(
            "field.required.placement",
            "Fields can optionally have required attribute",
            (parent) -> parent instanceof com.draagon.meta.field.MetaField,
            (child) -> child instanceof com.draagon.meta.attr.BooleanAttribute &&
                      "required".equals(child.getName())
        ));
    }

    /**
     * Load code generation constraints (from former CodegenConstraintProvider)
     */
    private void loadCodegenConstraints() {
        // JPA generation control attributes
        addJpaGenerationConstraints();

        // Field behavior attributes (codegen-specific)
        addFieldBehaviorConstraints();
    }

    private void addJpaGenerationConstraints() {
        // PLACEMENT CONSTRAINT: skipJpa attribute can be placed on MetaObjects
        addConstraint(new PlacementConstraint(
            "codegen.skipJpa.object.placement",
            "skipJpa attribute can be placed on MetaObjects to skip JPA generation",
            (parent) -> parent instanceof com.draagon.meta.object.MetaObject,
            (child) -> child instanceof com.draagon.meta.attr.MetaAttribute && "skipJpa".equals(child.getName())
        ));

        // VALIDATION CONSTRAINT: skipJpa must be boolean
        addConstraint(new ValidationConstraint(
            "codegen.skipJpa.object.validation",
            "skipJpa must be a boolean value (true/false)",
            (metadata) -> metadata instanceof com.draagon.meta.attr.MetaAttribute && "skipJpa".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        ));

        // PLACEMENT CONSTRAINT: skipJpa attribute can be placed on MetaFields
        addConstraint(new PlacementConstraint(
            "codegen.skipJpa.field.placement",
            "skipJpa attribute can be placed on MetaFields to skip JPA generation",
            (parent) -> parent instanceof com.draagon.meta.field.MetaField,
            (child) -> child instanceof com.draagon.meta.attr.MetaAttribute && "skipJpa".equals(child.getName())
        ));

        // VALIDATION CONSTRAINT: skipJpa must be boolean for fields too
        addConstraint(new ValidationConstraint(
            "codegen.skipJpa.field.validation",
            "skipJpa must be a boolean value (true/false)",
            (metadata) -> metadata instanceof com.draagon.meta.attr.MetaAttribute && "skipJpa".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        ));
    }

    private void addFieldBehaviorConstraints() {
        // PLACEMENT CONSTRAINT: collection attribute can be placed on MetaFields
        addConstraint(new PlacementConstraint(
            "codegen.collection.placement",
            "collection attribute can be placed on MetaFields to indicate collection type",
            (parent) -> parent instanceof com.draagon.meta.field.MetaField,
            (child) -> child instanceof com.draagon.meta.attr.MetaAttribute && "collection".equals(child.getName())
        ));

        // VALIDATION CONSTRAINT: collection must be boolean
        addConstraint(new ValidationConstraint(
            "codegen.collection.validation",
            "collection must be a boolean value (true/false)",
            (metadata) -> metadata instanceof com.draagon.meta.attr.MetaAttribute && "collection".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        ));

        // PLACEMENT CONSTRAINT: isSearchable attribute can be placed on MetaFields
        addConstraint(new PlacementConstraint(
            "codegen.isSearchable.placement",
            "isSearchable attribute can be placed on MetaFields for search functionality",
            (parent) -> parent instanceof com.draagon.meta.field.MetaField,
            (child) -> child instanceof com.draagon.meta.attr.MetaAttribute && "isSearchable".equals(child.getName())
        ));

        // VALIDATION CONSTRAINT: isSearchable must be boolean
        addConstraint(new ValidationConstraint(
            "codegen.isSearchable.validation",
            "isSearchable must be a boolean value (true/false)",
            (metadata) -> metadata instanceof com.draagon.meta.attr.MetaAttribute && "isSearchable".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        ));
    }

    /**
     * Load core I/O constraints (from former CoreIOConstraintProvider)
     */
    private void loadCoreIOConstraints() {
        // XML name mapping attributes
        addXmlNamingConstraints();

        // XML behavior control attributes
        addXmlBehaviorConstraints();
    }

    private void addXmlNamingConstraints() {
        // PLACEMENT CONSTRAINT: xmlName attribute can be placed on any MetaData
        addConstraint(new PlacementConstraint(
            "coreio.xmlName.placement",
            "xmlName attribute can be placed on any MetaData for XML element naming",
            (parent) -> parent instanceof com.draagon.meta.MetaData,
            (child) -> child instanceof com.draagon.meta.attr.MetaAttribute && "xmlName".equals(child.getName())
        ));

        // VALIDATION CONSTRAINT: xmlName must be valid XML identifier
        addConstraint(new ValidationConstraint(
            "coreio.xmlName.validation",
            "xmlName must be a valid XML element name",
            (metadata) -> metadata instanceof com.draagon.meta.attr.MetaAttribute && "xmlName".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String xmlName = value.toString().trim();
                // XML name pattern: must start with letter/underscore, followed by letters/digits/hyphens/underscores/periods
                return xmlName.matches("^[a-zA-Z_][a-zA-Z0-9_.-]*$") && xmlName.length() >= 1 && xmlName.length() <= 100;
            }
        ));
    }

    private void addXmlBehaviorConstraints() {
        // PLACEMENT CONSTRAINT: xmlTyped attribute can be placed on MetaObjects
        addConstraint(new PlacementConstraint(
            "coreio.xmlTyped.placement",
            "xmlTyped attribute can be placed on MetaObjects for type information in XML",
            (parent) -> parent instanceof com.draagon.meta.object.MetaObject,
            (child) -> child instanceof com.draagon.meta.attr.MetaAttribute && "xmlTyped".equals(child.getName())
        ));

        // VALIDATION CONSTRAINT: xmlTyped must be boolean
        addConstraint(new ValidationConstraint(
            "coreio.xmlTyped.validation",
            "xmlTyped must be a boolean value (true/false)",
            (metadata) -> metadata instanceof com.draagon.meta.attr.MetaAttribute && "xmlTyped".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        ));

        // PLACEMENT CONSTRAINT: xmlWrap attribute can be placed on MetaFields
        addConstraint(new PlacementConstraint(
            "coreio.xmlWrap.placement",
            "xmlWrap attribute can be placed on MetaFields for XML wrapping behavior",
            (parent) -> parent instanceof com.draagon.meta.field.MetaField,
            (child) -> child instanceof com.draagon.meta.attr.MetaAttribute && "xmlWrap".equals(child.getName())
        ));

        // VALIDATION CONSTRAINT: xmlWrap must be boolean
        addConstraint(new ValidationConstraint(
            "coreio.xmlWrap.validation",
            "xmlWrap must be a boolean value (true/false)",
            (metadata) -> metadata instanceof com.draagon.meta.attr.MetaAttribute && "xmlWrap".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        ));

        // PLACEMENT CONSTRAINT: xmlIgnore attribute can be placed on MetaFields
        addConstraint(new PlacementConstraint(
            "coreio.xmlIgnore.placement",
            "xmlIgnore attribute can be placed on MetaFields to exclude from XML serialization",
            (parent) -> parent instanceof com.draagon.meta.field.MetaField,
            (child) -> child instanceof com.draagon.meta.attr.MetaAttribute && "xmlIgnore".equals(child.getName())
        ));

        // VALIDATION CONSTRAINT: xmlIgnore must be boolean
        addConstraint(new ValidationConstraint(
            "coreio.xmlIgnore.validation",
            "xmlIgnore must be a boolean value (true/false)",
            (metadata) -> metadata instanceof com.draagon.meta.attr.MetaAttribute && "xmlIgnore".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        ));
    }

    /**
     * Load web constraints (from former WebConstraintProvider)
     */
    private void loadWebConstraints() {
        // HTML input type validation for form generation
        addHtmlInputTypeConstraints();

        // CSS class and HTML ID validation
        addCssAndHtmlConstraints();

        // Form label and UI text constraints
        addFormTextConstraints();

        // Security constraints for web content
        addSecurityConstraints();
    }

    private void addHtmlInputTypeConstraints() {
        // PLACEMENT CONSTRAINT: htmlInputType attribute can be placed on string fields
        addConstraint(new PlacementConstraint(
            "web.htmlInputType.placement",
            "htmlInputType attribute can be placed on string fields for form generation",
            (parent) -> parent instanceof com.draagon.meta.field.MetaField && "string".equals(((com.draagon.meta.field.MetaField) parent).getSubType()),
            (child) -> child instanceof com.draagon.meta.attr.MetaAttribute && "htmlInputType".equals(child.getName())
        ));

        // VALIDATION CONSTRAINT: htmlInputType must be valid HTML input type
        addConstraint(new ValidationConstraint(
            "web.htmlInputType.validation",
            "htmlInputType must be a valid HTML input type",
            (metadata) -> metadata instanceof com.draagon.meta.attr.MetaAttribute && "htmlInputType".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true;
                String inputType = value.toString().toLowerCase();
                return isValidHtmlInputType(inputType);
            }
        ));
    }

    private void addCssAndHtmlConstraints() {
        // PLACEMENT CONSTRAINT: CSS class attributes
        addConstraint(new PlacementConstraint(
            "web.cssClass.placement",
            "cssClass attribute can be placed on any MetaData for styling",
            (parent) -> parent instanceof com.draagon.meta.MetaData,
            (child) -> child instanceof com.draagon.meta.attr.MetaAttribute && "cssClass".equals(child.getName())
        ));

        // VALIDATION CONSTRAINT: CSS class names must follow valid pattern
        addConstraint(new ValidationConstraint(
            "web.cssClass.validation",
            "CSS class names must follow valid CSS identifier pattern",
            (metadata) -> metadata instanceof com.draagon.meta.attr.MetaAttribute && "cssClass".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true;
                String cssClass = value.toString();
                return cssClass.matches("^[a-zA-Z]([a-zA-Z0-9_-])*$") && cssClass.length() <= 50;
            }
        ));

        // PLACEMENT CONSTRAINT: HTML ID attributes
        addConstraint(new PlacementConstraint(
            "web.htmlId.placement",
            "htmlId attribute can be placed on any MetaData for DOM identification",
            (parent) -> parent instanceof com.draagon.meta.MetaData,
            (child) -> child instanceof com.draagon.meta.attr.MetaAttribute && "htmlId".equals(child.getName())
        ));

        // VALIDATION CONSTRAINT: HTML ID must follow valid pattern
        addConstraint(new ValidationConstraint(
            "web.htmlId.validation",
            "HTML ID must follow valid HTML identifier pattern",
            (metadata) -> metadata instanceof com.draagon.meta.attr.MetaAttribute && "htmlId".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true;
                String htmlId = value.toString();
                return htmlId.matches("^[a-zA-Z]([a-zA-Z0-9_-])*$");
            }
        ));
    }

    private void addFormTextConstraints() {
        // PLACEMENT CONSTRAINT: Form label attributes
        addConstraint(new PlacementConstraint(
            "web.formLabel.placement",
            "formLabel attribute can be placed on fields for form generation",
            (parent) -> parent instanceof com.draagon.meta.field.MetaField,
            (child) -> child instanceof com.draagon.meta.attr.MetaAttribute && "formLabel".equals(child.getName())
        ));

        // VALIDATION CONSTRAINT: Form labels must be non-empty and within length limits
        addConstraint(new ValidationConstraint(
            "web.formLabel.validation",
            "Form labels must be non-empty and within 1-100 characters",
            (metadata) -> metadata instanceof com.draagon.meta.attr.MetaAttribute && "formLabel".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return false; // Required
                String label = value.toString().trim();
                return !label.isEmpty() && label.length() >= 1 && label.length() <= 100;
            }
        ));

        // PLACEMENT CONSTRAINT: Placeholder text attributes
        addConstraint(new PlacementConstraint(
            "web.placeholder.placement",
            "placeholder attribute can be placed on string fields for input hints",
            (parent) -> parent instanceof com.draagon.meta.field.MetaField && "string".equals(((com.draagon.meta.field.MetaField) parent).getSubType()),
            (child) -> child instanceof com.draagon.meta.attr.MetaAttribute && "placeholder".equals(child.getName())
        ));

        // VALIDATION CONSTRAINT: Placeholder text length limits
        addConstraint(new ValidationConstraint(
            "web.placeholder.validation",
            "Placeholder text must be within 200 character limit",
            (metadata) -> metadata instanceof com.draagon.meta.attr.MetaAttribute && "placeholder".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true;
                return value.toString().length() <= 200;
            }
        ));

        // PLACEMENT CONSTRAINT: Validation message attributes
        addConstraint(new PlacementConstraint(
            "web.validationMessage.placement",
            "validationMessage attribute can be placed on any MetaData for error display",
            (parent) -> parent instanceof com.draagon.meta.MetaData,
            (child) -> child instanceof com.draagon.meta.attr.MetaAttribute && "validationMessage".equals(child.getName())
        ));

        // VALIDATION CONSTRAINT: Validation message length limits
        addConstraint(new ValidationConstraint(
            "web.validationMessage.validation",
            "Validation messages must be within 500 character limit",
            (metadata) -> metadata instanceof com.draagon.meta.attr.MetaAttribute && "validationMessage".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true;
                return value.toString().length() <= 500;
            }
        ));

        // PLACEMENT CONSTRAINT: Help text attributes
        addConstraint(new PlacementConstraint(
            "web.helpText.placement",
            "helpText attribute can be placed on any MetaData for user guidance",
            (parent) -> parent instanceof com.draagon.meta.MetaData,
            (child) -> child instanceof com.draagon.meta.attr.MetaAttribute && "helpText".equals(child.getName())
        ));

        // VALIDATION CONSTRAINT: Help text length limits
        addConstraint(new ValidationConstraint(
            "web.helpText.validation",
            "Help text must be within 1000 character limit",
            (metadata) -> metadata instanceof com.draagon.meta.attr.MetaAttribute && "helpText".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true;
                return value.toString().length() <= 1000;
            }
        ));
    }

    private void addSecurityConstraints() {
        // VALIDATION CONSTRAINT: String fields should not contain script tags (XSS prevention)
        addConstraint(new ValidationConstraint(
            "web.xss.validation",
            "String fields should not contain script tags for security",
            (metadata) -> metadata instanceof com.draagon.meta.field.MetaField && "string".equals(((com.draagon.meta.field.MetaField) metadata).getSubType()),
            (metadata, value) -> {
                if (value == null) return true;
                String stringValue = value.toString().toLowerCase();
                return !stringValue.contains("<script");
            }
        ));
    }

    private boolean isValidHtmlInputType(String inputType) {
        java.util.List<String> validTypes = java.util.Arrays.asList(
            "text", "password", "email", "url", "tel", "search",
            "number", "range", "date", "datetime-local", "time", "month", "week",
            "color", "file", "image", "hidden", "checkbox", "radio", "submit", "button", "reset"
        );
        return validTypes.contains(inputType);
    }

    /**
     * Add a constraint to the registry
     * @param constraint The constraint to add
     */
    public void addConstraint(Constraint constraint) {
        if (constraint == null) {
            log.warn("Attempted to add null constraint");
            return;
        }

        constraints.add(constraint);
        log.debug("Added constraint: {} [{}]", constraint.getType(), constraint.getDescription());
    }

    /**
     * Get all validation constraints (unified constraint system)
     * @return List of all registered validation constraints
     */
    public List<Constraint> getAllValidationConstraints() {
        if (!constraintsInitialized) {
            loadCoreConstraints();
        }
        return new ArrayList<>(constraints);
    }

    /**
     * Get placement validation constraints (unified constraint system)
     * @return List of placement constraints
     */
    public List<PlacementConstraint> getPlacementValidationConstraints() {
        return getAllValidationConstraints().stream()
            .filter(c -> c instanceof PlacementConstraint)
            .map(c -> (PlacementConstraint) c)
            .collect(Collectors.toList());
    }

    /**
     * Get field validation constraints (unified constraint system)
     * @return List of validation constraints
     */
    public List<ValidationConstraint> getFieldValidationConstraints() {
        return getAllValidationConstraints().stream()
            .filter(c -> c instanceof ValidationConstraint)
            .map(c -> (ValidationConstraint) c)
            .collect(Collectors.toList());
    }

    /**
     * Get validation constraints by type
     * @param constraintType The constraint type to filter by
     * @return List of constraints matching the type
     */
    public List<Constraint> getValidationConstraintsByType(String constraintType) {
        return getAllValidationConstraints().stream()
            .filter(c -> constraintType.equals(c.getType()))
            .collect(Collectors.toList());
    }

    /**
     * Get total number of registered validation constraints
     * @return Count of all validation constraints
     */
    public int getValidationConstraintCount() {
        if (!constraintsInitialized) {
            loadCoreConstraints();
        }
        return constraints.size();
    }

    /**
     * Get summary of validation constraint types and counts
     * @return Map of constraint type to count
     */
    public Map<String, Integer> getValidationConstraintTypeSummary() {
        return getAllValidationConstraints().stream()
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
    public boolean isConstraintsInitialized() {
        return constraintsInitialized;
    }

    /**
     * Force reload of constraint providers (primarily for testing)
     */
    public void reloadConstraints() {
        synchronized (this) {
            constraints.clear();
            constraintsInitialized = false;
            loadCoreConstraints();
        }
    }

    /**
     * Registry statistics record with constraint information
     */
    public record RegistryStats(
        int totalTypes,
        Map<String, Integer> typesByPrimary,
        int globalRequirements,
        String serviceRegistryDescription,
        Map<String, Integer> constraintStats
    ) {}
}