package com.draagon.meta.registry;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataTypeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
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
 * MetaDataRegistry.registerType(StringField.class, def -> def
 *     .type("field").subType("string")
 *     .description("String field with pattern validation")
 *     .optionalAttribute("pattern", "string")
 *     .optionalAttribute("required", "boolean")
 * );
 * 
 * // Register an object type that accepts fields
 * MetaDataRegistry.registerType(MetaObject.class, def -> def
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
    private volatile boolean initialized = false;
    private volatile boolean currentlyReloading = false;
    
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

        // CRITICAL FIX: Validate registry state and reload if corrupted
        // Only validate if we're not currently in the middle of a reload to prevent infinite recursion
        if (!instance.currentlyReloading && instance.initialized && !instance.isRegistryStateValid()) {
            synchronized (INSTANCE_LOCK) {
                // Double-check to avoid race conditions
                if (!instance.currentlyReloading && !instance.isRegistryStateValid()) {
                    log.warn("Registry state validation failed - reloading providers to fix corruption");
                    instance.reloadCoreTypes();
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
    public static void registerType(Class<? extends MetaData> clazz,
                                   Consumer<TypeDefinitionBuilder> configurator) {
        TypeDefinitionBuilder builder = TypeDefinitionBuilder.forClass(clazz);
        configurator.accept(builder);
        getInstance().register(builder.build());
    }

    /**
     * Centralized registration method for all MetaData types using the standardized pattern.
     * This replaces scattered static blocks with a controlled, testable registration process.
     *
     * <p>This method follows the Phase 2 enhancement pattern where each MetaData class
     * implements a standard {@code registerTypes(MetaDataRegistry)} method that contains
     * all type and constraint registration logic.</p>
     *
     * <h3>Usage Pattern:</h3>
     * <pre>{@code
     * // In MetaData classes:
     * public static void registerTypes(MetaDataRegistry registry) {
     *     registry.registerType(StringField.class, def -> def
     *         .type(TYPE_FIELD).subType(SUBTYPE_STRING)
     *         .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)
     *         .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_PATTERN)
     *     );
     *
     *     // Register constraints
     *     ConstraintRegistry.getInstance().addConstraint(new ValidationConstraint(...));
     * }
     * }</pre>
     *
     * @param typeClasses Classes that implement the registerTypes(MetaDataRegistry) pattern
     */
    public static void registerTypes(Class<? extends MetaData>... typeClasses) {
        MetaDataRegistry registry = getInstance();

        for (Class<? extends MetaData> clazz : typeClasses) {
            try {
                // Use reflection to call the standardized registerTypes(MetaDataRegistry) method
                var registerMethod = clazz.getDeclaredMethod("registerTypes", MetaDataRegistry.class);
                registerMethod.setAccessible(true);
                registerMethod.invoke(null, registry);

                log.debug("Successfully registered types from class: {}", clazz.getSimpleName());

            } catch (NoSuchMethodException e) {
                log.warn("Class {} does not implement registerTypes(MetaDataRegistry) method - skipping",
                        clazz.getSimpleName());
            } catch (Exception e) {
                log.error("Failed to register types from class {}: {}",
                         clazz.getSimpleName(), e.getMessage(), e);
            }
        }
    }

    /**
     * Register all core MetaData types using the standardized registerTypes() pattern.
     * This is called during framework initialization to establish all base types.
     *
     * <p><strong>Phase 2 Pattern:</strong> This method calls the standardized {@code registerTypes()}
     * method on all core MetaData classes, replacing the previous static block approach.</p>
     *
     * <h3>Registration Order:</h3>
     * <ol>
     *   <li><strong>Base Types:</strong> MetaData, MetaField, MetaObject, MetaAttribute</li>
     *   <li><strong>Field Types:</strong> StringField, IntegerField, LongField, etc.</li>
     *   <li><strong>Object Types:</strong> PojoMetaObject, ProxyMetaObject, etc.</li>
     *   <li><strong>Support Types:</strong> Validators, Keys, Views, Loaders</li>
     * </ol>
     */
    @SuppressWarnings("unchecked")
    public static void registerAllCoreTypes() {
        log.info("Starting Phase 2 core type registration using standardized registerTypes() pattern");

        try {
            // PHASE 1: Base Types (these must be registered first for inheritance)
            log.debug("Registering base types...");
            registerTypes(
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.loader.MetaDataLoader"), // metadata.base
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.field.MetaField"),        // field.base
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.object.MetaObject"),      // object.base
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.attr.MetaAttribute"),     // attr.base
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.validator.MetaValidator"), // validator.base
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.key.MetaKey"),            // key.base
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.view.MetaView")           // view.base
            );

            // PHASE 2: Field Types (inherit from field.base)
            log.debug("Registering field types...");
            registerTypes(
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.field.StringField"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.field.IntegerField"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.field.LongField"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.field.DoubleField"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.field.FloatField"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.field.BooleanField"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.field.ByteField"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.field.ShortField"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.field.DateField"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.field.ClassField"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.field.ObjectField"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.field.ObjectArrayField"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.field.StringArrayField"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.field.TimestampField")
            );

            // PHASE 3: Attribute Types (inherit from attr.base)
            log.debug("Registering attribute types...");
            registerTypes(
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.attr.StringAttribute"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.attr.IntAttribute"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.attr.BooleanAttribute"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.attr.ClassAttribute"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.attr.DoubleAttribute"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.attr.LongAttribute"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.attr.PropertiesAttribute"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.attr.StringArrayAttribute")
            );

            // PHASE 4: Object Types (inherit from object.base)
            log.debug("Registering object types...");
            registerTypes(
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.object.pojo.PojoMetaObject"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.object.mapped.MappedMetaObject"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.object.proxy.ProxyMetaObject")
            );

            // PHASE 5: Support Types (validators, keys, views, loaders)
            log.debug("Registering support types...");
            registerTypes(
                // Validators
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.validator.RequiredValidator"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.validator.LengthValidator"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.validator.NumericValidator"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.validator.RegexValidator"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.validator.ArrayValidator"),

                // Keys
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.key.PrimaryKey"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.key.ForeignKey"),
                (Class<? extends MetaData>) Class.forName("com.draagon.meta.key.SecondaryKey")
            );

            log.info("Phase 2 core type registration completed successfully - {} types registered",
                    getInstance().getRegisteredTypes().size());

            // Resolve any deferred inheritance now that all base types are registered
            int resolvedCount = getInstance().resolveDeferredInheritance();
            if (resolvedCount > 0) {
                log.info("Resolved {} deferred inheritance relationships", resolvedCount);
            }

        } catch (ClassNotFoundException e) {
            log.error("Failed to load MetaData class during core type registration: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to register core types using standardized pattern: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Register a type definition with inheritance resolution
     *
     * @param definition Complete type definition
     */
    public void register(TypeDefinition definition) {
        MetaDataTypeId typeId = new MetaDataTypeId(definition.getType(), definition.getSubType());

        // CRITICAL: Ensure MetaDataLoader base types are registered first
        // This prevents deferred inheritance issues when field/object types
        // try to inherit from metadata.base before it's registered
        ensureMetaDataLoaderTypesRegistered();

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
     * Extend an existing registered type with additional named attributes/children.
     * Used by service providers to add service-specific capabilities to existing types.
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

        // Use updated bidirectional inheritance resolution
        resolveBidirectionalInheritance(definition, parentDefinition);
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
        if (currentlyReloading) {
            log.debug("Registry clear skipped - reload already in progress");
            return;
        }

        try {
            currentlyReloading = true;
            typeDefinitions.clear();
            globalRequirements.clear();
            deferredInheritanceTypes.clear();
            initialized = false;
            log.debug("Cleared all type registrations");

            // CRITICAL FIX: Automatically reload providers to prevent test interference
            // This ensures ServiceLoader provider discovery is re-triggered after clearing
            try {
                log.info("Registry cleared - automatically reloading providers to prevent corruption");
                loadCoreTypes();
                log.info("Provider reload completed successfully after registry clear");
            } catch (Exception e) {
                log.error("Failed to reload providers after registry clear - this may cause test failures", e);
            }
        } finally {
            currentlyReloading = false;
        }
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
            serviceRegistry.getDescription()
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
        try {
            log.info("Loading service extension providers...");

            // Discover ServiceExtensionProvider implementations via ServiceLoader
            ServiceLoader<ServiceExtensionProvider> extensionLoader = ServiceLoader.load(ServiceExtensionProvider.class);
            List<ServiceExtensionProvider> providers = new ArrayList<>();

            // Collect all providers
            for (ServiceExtensionProvider provider : extensionLoader) {
                if (provider.supportsCurrentEnvironment()) {
                    providers.add(provider);
                    log.debug("Discovered service extension provider: {}", provider.getProviderName());
                } else {
                    log.debug("Skipping service extension provider {} (unsupported environment)",
                             provider.getProviderName());
                }
            }

            if (providers.isEmpty()) {
                log.debug("No ServiceExtensionProvider implementations found - using self-registration pattern only");
                return;
            }

            // Sort providers by dependencies and priority
            List<ServiceExtensionProvider> orderedProviders = sortExtensionProvidersByDependencies(providers);

            // Apply each provider's extensions to existing types
            for (ServiceExtensionProvider provider : orderedProviders) {
                try {
                    log.debug("Applying extensions from provider: {} (priority: {})",
                             provider.getProviderName(), provider.getPriority());

                    provider.extendTypes(this);

                    log.info("Successfully applied extensions from provider: {} - {}",
                            provider.getProviderName(), provider.getDescription());

                } catch (Exception e) {
                    log.error("Failed to apply extensions from provider: {} - {}",
                             provider.getProviderName(), e.getMessage(), e);
                    // Continue with other providers rather than failing completely
                }
            }

            log.info("Service extension loading completed - {} providers processed", orderedProviders.size());

        } catch (Exception e) {
            log.error("Failed to load service extension providers: {}", e.getMessage(), e);
            // Non-fatal - registry can still function without extensions
        }
    }
    
    
    // Legacy provider loading removed - using unified plugin approach
    
    /**
     * Load core types using ServiceLoader provider discovery
     */
    private void loadCoreTypes() {
        try {
            log.info("Loading MetaData types via ServiceLoader provider discovery...");

            // Use the new provider discovery system instead of Class.forName()
            MetaDataProviderDiscovery.discoverAllProviders(this);

            log.info("Successfully loaded {} registered types via provider discovery",
                    getRegisteredTypes().size());

            // CRITICAL: Refresh constraint system to pick up all newly registered types
            log.info("Refreshing constraint system with {} registered types", getRegisteredTypes().size());
            com.draagon.meta.constraint.ConstraintEnforcer.getInstance().refreshConstraintFlattener();
            log.info("Constraint system refresh completed");

            // Mark registry as fully initialized
            initialized = true;

        } catch (Exception e) {
            log.error("Provider discovery failed, falling back to manual class loading", e);

            // Fallback to manual class loading for backward compatibility
            fallbackToManualClassLoading();

            // Mark as initialized even after fallback
            initialized = true;
        }
    }

    /**
     * Fallback method for manual class loading if provider discovery fails
     */
    private void fallbackToManualClassLoading() {
        try {
            log.warn("Using fallback manual class loading - this may not work in all environments");

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

            log.debug("Fallback class loading completed");
        } catch (ClassNotFoundException e) {
            log.warn("Some core types could not be loaded via fallback: {}", e.getMessage());
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
                    resolveBidirectionalInheritance(definition, parentDefinition);
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
     * Check if the registry is in a valid state with all expected providers loaded.
     * This validates that ServiceLoader provider discovery completed successfully
     * and that core types are properly registered.
     *
     * @return true if registry state is valid, false if corruption detected
     */
    private boolean isRegistryStateValid() {
        try {
            // Check if core object types are registered (these are loaded by ObjectTypeProvider)
            boolean hasObjectMap = isRegistered("object", "map");
            boolean hasObjectPojo = isRegistered("object", "pojo");
            boolean hasObjectProxy = isRegistered("object", "proxy");
            boolean hasObjectBase = isRegistered("object", "base");

            // Check if core field types are registered
            boolean hasFieldString = isRegistered("field", "string");
            boolean hasFieldInt = isRegistered("field", "int");
            boolean hasFieldLong = isRegistered("field", "long");

            // Check if core attribute types are registered
            boolean hasAttrString = isRegistered("attr", "string");
            boolean hasAttrInt = isRegistered("attr", "int");
            boolean hasAttrBoolean = isRegistered("attr", "boolean");

            // Registry is valid if we have core types from all major providers
            boolean isValid = hasObjectMap && hasObjectPojo && hasObjectProxy && hasObjectBase &&
                             hasFieldString && hasFieldInt && hasFieldLong &&
                             hasAttrString && hasAttrInt && hasAttrBoolean;

            if (!isValid) {
                log.warn("Registry state validation failed - missing core types. " +
                        "Object types: map={}, pojo={}, proxy={}, base={}. " +
                        "Field types: string={}, int={}, long={}. " +
                        "Attr types: string={}, int={}, boolean={}",
                        hasObjectMap, hasObjectPojo, hasObjectProxy, hasObjectBase,
                        hasFieldString, hasFieldInt, hasFieldLong,
                        hasAttrString, hasAttrInt, hasAttrBoolean);
            } else {
                log.debug("Registry state validation passed - all core types present");
            }

            return isValid;

        } catch (Exception e) {
            log.warn("Registry state validation failed with exception: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Safely reload core types without corrupting existing state.
     * This method re-triggers ServiceLoader provider discovery to restore
     * missing types after test interference or other corruption.
     */
    private void reloadCoreTypes() {
        if (currentlyReloading) {
            log.debug("Registry reload already in progress, skipping duplicate reload");
            return;
        }

        try {
            currentlyReloading = true;
            log.info("Reloading core types to fix registry corruption...");

            // Store current registration count for comparison
            int initialTypeCount = typeDefinitions.size();

            // Re-trigger provider discovery without clearing existing registrations
            // This allows missing providers to be loaded while preserving valid types
            MetaDataProviderDiscovery.discoverAllProviders(this);

            int finalTypeCount = typeDefinitions.size();
            log.info("Core types reload completed. Type count: {} -> {}",
                    initialTypeCount, finalTypeCount);

            // Refresh constraint system with updated type registry
            com.draagon.meta.constraint.ConstraintEnforcer.getInstance().refreshConstraintFlattener();
            log.info("Constraint system refreshed after core types reload");

        } catch (Exception e) {
            log.error("Failed to reload core types - registry may remain in corrupted state", e);
        } finally {
            currentlyReloading = false;
        }
    }

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

    /**
     * Bidirectional inheritance resolution for the new AcceptsChildrenDeclaration system
     */
    private void resolveBidirectionalInheritance(TypeDefinition definition, TypeDefinition parentDefinition) {
        // Copy AcceptsChildren declarations from parent to child (NEW BIDIRECTIONAL SYSTEM)
        List<AcceptsChildrenDeclaration> inheritedChildrenDeclarations = new ArrayList<>();

        // Add parent's direct accepts children declarations
        inheritedChildrenDeclarations.addAll(parentDefinition.getDirectAcceptsChildren());

        // Add parent's inherited accepts children declarations (recursive inheritance)
        inheritedChildrenDeclarations.addAll(parentDefinition.getInheritedAcceptsChildren());

        // Populate inherited accepts children in the child definition
        definition.populateInheritedAcceptsChildren(inheritedChildrenDeclarations);

        // Copy AcceptsParents declarations from parent to child (CRITICAL FOR BIDIRECTIONAL CONSTRAINTS)
        List<AcceptsParentsDeclaration> inheritedParentsDeclarations = new ArrayList<>();

        // Add parent's direct accepts parents declarations
        inheritedParentsDeclarations.addAll(parentDefinition.getDirectAcceptsParents());

        // Add parent's inherited accepts parents declarations (recursive inheritance)
        inheritedParentsDeclarations.addAll(parentDefinition.getInheritedAcceptsParents());

        // Populate inherited accepts parents in the child definition
        definition.populateInheritedAcceptsParents(inheritedParentsDeclarations);

        log.debug("Bidirectional inheritance resolved for {}: {} accepts children + {} accepts parents declarations inherited from parent {}",
                 definition.getQualifiedName(), inheritedChildrenDeclarations.size(), inheritedParentsDeclarations.size(), parentDefinition.getQualifiedName());
    }

    private static volatile boolean metaDataLoaderTypesRegistered = false;

    /**
     * Ensures MetaDataLoader base types are registered before any inheritance attempts.
     * This method triggers MetaDataLoader class loading to register metadata.base and loader.manual
     * types, preventing deferred inheritance issues.
     */
    private void ensureMetaDataLoaderTypesRegistered() {
        if (!metaDataLoaderTypesRegistered) {
            synchronized (INSTANCE_LOCK) {
                if (!metaDataLoaderTypesRegistered) {
                    try {
                        // Force MetaDataLoader class loading to trigger its static block
                        // which registers metadata.base and loader.manual types
                        Class.forName("com.draagon.meta.loader.MetaDataLoader");
                        log.debug("MetaDataLoader types ensured via class loading");
                        metaDataLoaderTypesRegistered = true;
                    } catch (ClassNotFoundException e) {
                        log.warn("Could not load MetaDataLoader class: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Sort service extension providers by dependencies using topological sorting.
     *
     * @param providers List of service extension providers to sort
     * @return Ordered list of providers with dependencies resolved
     * @throws IllegalArgumentException if circular dependencies are detected
     */
    private List<ServiceExtensionProvider> sortExtensionProvidersByDependencies(List<ServiceExtensionProvider> providers) {
        // Build provider name to provider map
        Map<String, ServiceExtensionProvider> providerMap = providers.stream()
            .collect(Collectors.toMap(ServiceExtensionProvider::getProviderName, p -> p));

        // Track visited and being processed states for cycle detection
        Set<String> visited = new HashSet<>();
        Set<String> processing = new HashSet<>();
        List<ServiceExtensionProvider> result = new ArrayList<>();

        // Perform topological sort using DFS
        for (ServiceExtensionProvider provider : providers) {
            if (!visited.contains(provider.getProviderName())) {
                topologicalSortDFS(provider.getProviderName(), providerMap, visited, processing, result);
            }
        }

        // Sort by priority within dependency order (higher priority first)
        result.sort(Comparator.comparing(ServiceExtensionProvider::getPriority).reversed());

        return result;
    }

    /**
     * Depth-first search for topological sorting with cycle detection.
     */
    private void topologicalSortDFS(String providerName,
                                   Map<String, ServiceExtensionProvider> providerMap,
                                   Set<String> visited,
                                   Set<String> processing,
                                   List<ServiceExtensionProvider> result) {

        if (processing.contains(providerName)) {
            throw new IllegalArgumentException(
                "Circular dependency detected in ServiceExtensionProvider dependencies: " + providerName);
        }

        if (visited.contains(providerName)) {
            return;
        }

        ServiceExtensionProvider provider = providerMap.get(providerName);
        if (provider == null) {
            log.warn("Missing dependency provider: {} - continuing with available providers", providerName);
            return;
        }

        processing.add(providerName);

        // Process dependencies first
        for (String dependency : provider.getDependencies()) {
            topologicalSortDFS(dependency, providerMap, visited, processing, result);
        }

        processing.remove(providerName);
        visited.add(providerName);
        result.add(provider);
    }

    /**
     * Registry statistics record
     */
    public record RegistryStats(
        int totalTypes,
        Map<String, Integer> typesByPrimary,
        int globalRequirements,
        String serviceRegistryDescription
    ) {}
}