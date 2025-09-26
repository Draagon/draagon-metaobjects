package com.draagon.meta.registry;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataTypeId;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.BooleanAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.constraint.*;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
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
                    // Load service providers to ensure they're available for parsing
                    instance.ensureInitialized();
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
        builder.withRegistry(this);  // Provide registry reference for auto-constraint generation
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
     * @deprecated Legacy method for backward compatibility during constraint system migration.
     * Use addConstraint() with concrete constraint classes instead.
     */
    @Deprecated
    public void registerPlacementConstraint(String constraintId, String description,
                                           Object parentMatcher,
                                           Object childMatcher) {
        log.warn("Using deprecated registerPlacementConstraint() method. Consider migrating to addConstraint() with concrete constraint classes.");
        // No-op for backward compatibility during migration
    }

    /**
     * @deprecated Legacy method for backward compatibility during constraint system migration.
     * Use addConstraint() with concrete constraint classes instead.
     */
    @Deprecated
    public void registerValidationConstraint(String constraintId, String description,
                                            Object applicabilityTest,
                                            Object valueValidator) {
        log.warn("Using deprecated registerValidationConstraint() method. Consider migrating to addConstraint() with concrete constraint classes.");
        // No-op for backward compatibility during migration
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
            getValidationConstraintTypeSummary()
        );
    }
    
    /**
     * Ensure service-based extensions are loaded
     */
    private void ensureInitialized() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    loadServiceProviders();
                    initialized = true;
                }
            }
        }
    }
    
    
    
    // Legacy provider loading removed - using unified plugin approach
    

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
     * <p>Providers are loaded in dependency order using topological sorting to ensure proper
     * dependency resolution. This replaces the fragile priority-based system with explicit dependencies.</p>
     */
    private void loadServiceProviders() {
        try {
            // Get all MetaDataTypeProvider implementations via ServiceLoader
            Collection<MetaDataTypeProvider> providers = serviceRegistry.getServices(MetaDataTypeProvider.class);

            if (providers.isEmpty()) {
                log.debug("No MetaDataTypeProvider services found");
                return;
            }

            // Resolve dependencies using topological sort
            List<MetaDataTypeProvider> resolvedProviders = resolveDependencies(providers);

            log.info("Loading {} MetaDataTypeProvider services in dependency order", resolvedProviders.size());

            // Register type extensions from each provider in dependency order
            for (MetaDataTypeProvider provider : resolvedProviders) {
                try {
                    long startTime = System.currentTimeMillis();
                    provider.registerTypes(this);

                    // Resolve any deferred inheritance after each provider completes
                    if (!deferredInheritanceTypes.isEmpty()) {
                        resolveDeferredInheritance();
                    }

                    long duration = System.currentTimeMillis() - startTime;

                    String depsStr = provider.getDependencies().length > 0 ?
                        String.join(",", provider.getDependencies()) : "none";

                    log.debug("Loaded provider: {} (id: {}, deps: {}) in {}ms - {}",
                             provider.getClass().getSimpleName(),
                             provider.getProviderId(),
                             depsStr,
                             duration,
                             provider.getDescription());

                } catch (Exception e) {
                    log.error("Failed to load MetaDataTypeProvider: {} - {}",
                             provider.getClass().getName(), e.getMessage(), e);
                    // Continue with other providers - don't fail completely
                }
            }

            log.info("Successfully loaded {} MetaDataTypeProvider services", resolvedProviders.size());

        } catch (Exception e) {
            log.error("Error during service provider loading: {}", e.getMessage(), e);
        }
    }

    /**
     * Resolve provider dependencies using topological sorting.
     *
     * <p>This algorithm ensures that providers are loaded in the correct order
     * by analyzing their dependency graph. It detects circular dependencies and
     * missing dependencies to prevent runtime errors.</p>
     *
     * @param providers Collection of providers to sort
     * @return List of providers in dependency order
     * @throws IllegalStateException if circular dependencies are detected
     */
    private List<MetaDataTypeProvider> resolveDependencies(Collection<MetaDataTypeProvider> providers) {
        // Build provider map by ID for fast lookup
        Map<String, MetaDataTypeProvider> providerMap = new HashMap<>();
        for (MetaDataTypeProvider provider : providers) {
            String id = provider.getProviderId();
            if (providerMap.containsKey(id)) {
                log.warn("Duplicate provider ID '{}': {} and {}. Using first occurrence.",
                        id, providerMap.get(id).getClass().getName(), provider.getClass().getName());
            } else {
                providerMap.put(id, provider);
            }
        }

        // Validate dependencies and detect missing ones
        Set<String> missingDeps = new HashSet<>();
        for (MetaDataTypeProvider provider : providers) {
            for (String dep : provider.getDependencies()) {
                if (!providerMap.containsKey(dep)) {
                    missingDeps.add(dep + " (required by " + provider.getProviderId() + ")");
                }
            }
        }

        if (!missingDeps.isEmpty()) {
            log.warn("Missing provider dependencies: {}. These will be ignored.", String.join(", ", missingDeps));
        }

        // Perform topological sort using Kahn's algorithm
        List<MetaDataTypeProvider> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();

        // Try to visit each provider
        for (MetaDataTypeProvider provider : providers) {
            if (!visited.contains(provider.getProviderId())) {
                topologicalSort(provider, providerMap, visited, visiting, result);
            }
        }

        return result;
    }

    /**
     * Recursive topological sort implementation with cycle detection.
     *
     * @param provider Current provider being processed
     * @param providerMap Map of provider ID to provider instance
     * @param visited Set of completely processed providers
     * @param visiting Set of providers currently being processed (for cycle detection)
     * @param result Result list in topological order
     * @throws IllegalStateException if a circular dependency is detected
     */
    private void topologicalSort(MetaDataTypeProvider provider,
                                Map<String, MetaDataTypeProvider> providerMap,
                                Set<String> visited,
                                Set<String> visiting,
                                List<MetaDataTypeProvider> result) {

        String providerId = provider.getProviderId();

        // Check for circular dependency
        if (visiting.contains(providerId)) {
            throw new IllegalStateException("Circular dependency detected involving provider: " + providerId);
        }

        // Skip if already processed
        if (visited.contains(providerId)) {
            return;
        }

        // Mark as currently being processed
        visiting.add(providerId);

        // Process dependencies first
        for (String depId : provider.getDependencies()) {
            MetaDataTypeProvider dependency = providerMap.get(depId);
            if (dependency != null) {
                topologicalSort(dependency, providerMap, visited, visiting, result);
            }
            // Note: Missing dependencies are already logged in resolveDependencies()
        }

        // Mark as completely processed
        visiting.remove(providerId);
        visited.add(providerId);

        // Add to result
        result.add(provider);
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
            "String fields can have maxLength attribute",
            "field.string",         // Parent pattern
            "attr.int[maxLength]",  // Child pattern
            true                    // Allowed
        ));

        // MetaField can optionally have required attribute
        addConstraint(new PlacementConstraint(
            "field.required.placement",
            "Fields can have required attribute",
            MetaField.TYPE_FIELD, "*",                          // Parent: field.*
            MetaAttribute.TYPE_ATTR, BooleanAttribute.SUBTYPE_BOOLEAN, "required", // Child: attr.boolean[required]
            true                                                // Allowed
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
            MetaObject.TYPE_OBJECT, "*",            // Parent: object.*
            MetaAttribute.TYPE_ATTR, "*", "skipJpa", // Child: attr.*[skipJpa]
            true                                    // Allowed
        ));

        // VALIDATION CONSTRAINT: skipJpa must be boolean
        addConstraint(new EnumConstraint(
            "codegen.skipJpa.validation",
            "skipJpa must be a boolean value (true/false)",
            "attr",                 // Target type
            "*",                    // Any subtype
            "skipJpa",              // Target name
            Set.of("true", "false"), // Allowed values
            false,                  // Case insensitive
            true                    // Allow null (optional)
        ));

        // PLACEMENT CONSTRAINT: skipJpa attribute can be placed on MetaFields
        addConstraint(new PlacementConstraint(
            "codegen.skipJpa.field.placement",
            "skipJpa attribute can be placed on MetaFields to skip JPA generation",
            MetaField.TYPE_FIELD, "*",              // Parent: field.*
            MetaAttribute.TYPE_ATTR, "*", "skipJpa", // Child: attr.*[skipJpa]
            true                                    // Allowed
        ));
    }

    private void addFieldBehaviorConstraints() {
        // PLACEMENT CONSTRAINT: collection attribute can be placed on MetaFields
        addConstraint(new PlacementConstraint(
            "codegen.collection.placement",
            "collection attribute can be placed on MetaFields to indicate collection type",
            MetaField.TYPE_FIELD, "*",                  // Parent: field.*
            MetaAttribute.TYPE_ATTR, "*", "collection", // Child: attr.*[collection]
            true                                        // Allowed
        ));

        // VALIDATION CONSTRAINT: collection must be boolean
        addConstraint(new EnumConstraint(
            "codegen.collection.validation",
            "collection must be a boolean value (true/false)",
            "attr",                     // Target type
            "*",                        // Any subtype
            "collection",               // Target name
            Set.of("true", "false"),    // Allowed values
            false,                      // Case insensitive
            true                        // Allow null (optional)
        ));

        // PLACEMENT CONSTRAINT: isSearchable attribute can be placed on MetaFields
        addConstraint(new PlacementConstraint(
            "codegen.isSearchable.placement",
            "isSearchable attribute can be placed on MetaFields for search functionality",
            MetaField.TYPE_FIELD, "*",                      // Parent: field.*
            MetaAttribute.TYPE_ATTR, "*", "isSearchable",   // Child: attr.*[isSearchable]
            true                                            // Allowed
        ));

        // VALIDATION CONSTRAINT: isSearchable must be boolean
        addConstraint(new EnumConstraint(
            "codegen.isSearchable.validation",
            "isSearchable must be a boolean value (true/false)",
            "attr",                     // Target type
            "*",                        // Any subtype
            "isSearchable",             // Target name
            Set.of("true", "false"),    // Allowed values
            false,                      // Case insensitive
            true                        // Allow null (optional)
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
            "*", "*",                               // Parent: *.* (any metadata)
            MetaAttribute.TYPE_ATTR, "*", "xmlName", // Child: attr.*[xmlName]
            true                                    // Allowed
        ));

        // VALIDATION CONSTRAINT: xmlName must be valid XML identifier
        addConstraint(new RegexConstraint(
            "coreio.xmlName.validation",
            "xmlName must be a valid XML element name",
            "attr",                     // Target type
            "*",                        // Any subtype
            "xmlName",                  // Target name
            "^[a-zA-Z_][a-zA-Z0-9_.-]{0,99}$", // XML name pattern with length limit
            true                        // Allow null (optional)
        ));
    }

    private void addXmlBehaviorConstraints() {
        // PLACEMENT CONSTRAINT: xmlTyped attribute can be placed on MetaObjects
        addConstraint(new PlacementConstraint(
            "coreio.xmlTyped.placement",
            "xmlTyped attribute can be placed on MetaObjects for type information in XML",
            MetaObject.TYPE_OBJECT, "*",                // Parent: object.*
            MetaAttribute.TYPE_ATTR, "*", "xmlTyped",   // Child: attr.*[xmlTyped]
            true                                        // Allowed
        ));

        // VALIDATION CONSTRAINT: xmlTyped must be boolean
        addConstraint(new EnumConstraint(
            "coreio.xmlTyped.validation",
            "xmlTyped must be a boolean value (true/false)",
            "attr",                     // Target type
            "*",                        // Any subtype
            "xmlTyped",                 // Target name
            Set.of("true", "false"),    // Allowed values
            false,                      // Case insensitive
            true                        // Allow null (optional)
        ));

        // PLACEMENT CONSTRAINT: xmlWrap attribute can be placed on MetaFields
        addConstraint(new PlacementConstraint(
            "coreio.xmlWrap.placement",
            "xmlWrap attribute can be placed on MetaFields for XML wrapping behavior",
            MetaField.TYPE_FIELD, "*",              // Parent: field.*
            MetaAttribute.TYPE_ATTR, "*", "xmlWrap", // Child: attr.*[xmlWrap]
            true                                    // Allowed
        ));

        // VALIDATION CONSTRAINT: xmlWrap must be boolean
        addConstraint(new EnumConstraint(
            "coreio.xmlWrap.validation",
            "xmlWrap must be a boolean value (true/false)",
            "attr",                     // Target type
            "*",                        // Any subtype
            "xmlWrap",                  // Target name
            Set.of("true", "false"),    // Allowed values
            false,                      // Case insensitive
            true                        // Allow null (optional)
        ));

        // PLACEMENT CONSTRAINT: xmlIgnore attribute can be placed on MetaFields
        addConstraint(new PlacementConstraint(
            "coreio.xmlIgnore.placement",
            "xmlIgnore attribute can be placed on MetaFields to exclude from XML serialization",
            MetaField.TYPE_FIELD, "*",                  // Parent: field.*
            MetaAttribute.TYPE_ATTR, "*", "xmlIgnore",  // Child: attr.*[xmlIgnore]
            true                                        // Allowed
        ));

        // VALIDATION CONSTRAINT: xmlIgnore must be boolean
        addConstraint(new EnumConstraint(
            "coreio.xmlIgnore.validation",
            "xmlIgnore must be a boolean value (true/false)",
            "attr",                     // Target type
            "*",                        // Any subtype
            "xmlIgnore",                // Target name
            Set.of("true", "false"),    // Allowed values
            false,                      // Case insensitive
            true                        // Allow null (optional)
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
            "field.string",             // Parent pattern (string fields only)
            "attr.*[htmlInputType]",    // Child pattern
            true                        // Allowed
        ));

        // VALIDATION CONSTRAINT: htmlInputType must be valid HTML input type
        addConstraint(new EnumConstraint(
            "web.htmlInputType.validation",
            "htmlInputType must be a valid HTML input type",
            "attr",                     // Target type
            "*",                        // Any subtype
            "htmlInputType",            // Target name
            Set.of("text", "password", "email", "url", "tel", "search", // Standard HTML input types
                   "number", "range", "date", "datetime-local", "time", "month", "week",
                   "color", "file", "image", "hidden", "checkbox", "radio",
                   "submit", "button", "reset"),
            false,                      // Case insensitive
            true                        // Allow null (optional)
        ));
    }

    private void addCssAndHtmlConstraints() {
        // PLACEMENT CONSTRAINT: CSS class attributes
        addConstraint(new PlacementConstraint(
            "web.cssClass.placement",
            "cssClass attribute can be placed on any MetaData for styling",
            "*", "*",                                   // Parent: *.* (any metadata)
            MetaAttribute.TYPE_ATTR, "*", "cssClass",   // Child: attr.*[cssClass]
            true                                        // Allowed
        ));

        // VALIDATION CONSTRAINT: CSS class names must follow valid pattern with length limit
        addConstraint(new RegexConstraint(
            "web.cssClass.validation",
            "CSS class names must follow valid CSS identifier pattern and be <= 50 chars",
            "attr",                     // Target type
            "*",                        // Any subtype
            "cssClass",                 // Target name
            "^[a-zA-Z][a-zA-Z0-9_-]{0,49}$", // CSS class pattern with 50 char limit
            true                        // Allow null (optional)
        ));

        // PLACEMENT CONSTRAINT: HTML ID attributes
        addConstraint(new PlacementConstraint(
            "web.htmlId.placement",
            "htmlId attribute can be placed on any MetaData for DOM identification",
            "*", "*",                               // Parent: *.* (any metadata)
            MetaAttribute.TYPE_ATTR, "*", "htmlId", // Child: attr.*[htmlId]
            true                                    // Allowed
        ));

        // VALIDATION CONSTRAINT: HTML ID must follow valid pattern
        addConstraint(new RegexConstraint(
            "web.htmlId.validation",
            "HTML ID must follow valid HTML identifier pattern",
            "attr",                     // Target type
            "*",                        // Any subtype
            "htmlId",                   // Target name
            "^[a-zA-Z][a-zA-Z0-9_-]*$", // HTML ID pattern
            true                        // Allow null (optional)
        ));
    }

    private void addFormTextConstraints() {
        // PLACEMENT CONSTRAINT: Form label attributes
        addConstraint(new PlacementConstraint(
            "web.formLabel.placement",
            "formLabel attribute can be placed on fields for form generation",
            MetaField.TYPE_FIELD, "*",                      // Parent: field.*
            MetaAttribute.TYPE_ATTR, "*", "formLabel",      // Child: attr.*[formLabel]
            true                                            // Allowed
        ));

        // VALIDATION CONSTRAINT: Form labels must be non-empty and within length limits
        addConstraint(new LengthConstraint(
            "web.formLabel.validation",
            "Form labels must be non-empty and within 1-100 characters",
            "attr",                     // Target type
            "*",                        // Any subtype
            "formLabel",                // Target name
            1,                          // Min length
            100,                        // Max length
            false                       // Don't allow null (required)
        ));

        // PLACEMENT CONSTRAINT: Placeholder text attributes
        addConstraint(new PlacementConstraint(
            "web.placeholder.placement",
            "placeholder attribute can be placed on string fields for input hints",
            "field.string",             // Parent pattern (string fields only)
            "attr.*[placeholder]",      // Child pattern
            true                        // Allowed
        ));

        // VALIDATION CONSTRAINT: Placeholder text length limits
        addConstraint(new LengthConstraint(
            "web.placeholder.validation",
            "Placeholder text must be within 200 character limit",
            "attr",                     // Target type
            "*",                        // Any subtype
            "placeholder",              // Target name
            null,                       // No min length
            200,                        // Max length
            true                        // Allow null (optional)
        ));

        // PLACEMENT CONSTRAINT: Validation message attributes
        addConstraint(new PlacementConstraint(
            "web.validationMessage.placement",
            "validationMessage attribute can be placed on any MetaData for error display",
            "*", "*",                                           // Parent: *.* (any metadata)
            MetaAttribute.TYPE_ATTR, "*", "validationMessage", // Child: attr.*[validationMessage]
            true                                                // Allowed
        ));

        // VALIDATION CONSTRAINT: Validation message length limits
        addConstraint(new LengthConstraint(
            "web.validationMessage.validation",
            "Validation messages must be within 500 character limit",
            "attr",                     // Target type
            "*",                        // Any subtype
            "validationMessage",        // Target name
            null,                       // No min length
            500,                        // Max length
            true                        // Allow null (optional)
        ));

        // PLACEMENT CONSTRAINT: Help text attributes
        addConstraint(new PlacementConstraint(
            "web.helpText.placement",
            "helpText attribute can be placed on any MetaData for user guidance",
            "*", "*",                               // Parent: *.* (any metadata)
            MetaAttribute.TYPE_ATTR, "*", "helpText", // Child: attr.*[helpText]
            true                                    // Allowed
        ));

        // VALIDATION CONSTRAINT: Help text length limits
        addConstraint(new LengthConstraint(
            "web.helpText.validation",
            "Help text must be within 1000 character limit",
            "attr",                     // Target type
            "*",                        // Any subtype
            "helpText",                 // Target name
            null,                       // No min length
            1000,                       // Max length
            true                        // Allow null (optional)
        ));
    }

    private void addSecurityConstraints() {
        // VALIDATION CONSTRAINT: String fields should not contain script tags (XSS prevention)
        addConstraint(new RegexConstraint(
            "web.xss.validation",
            "String fields should not contain script tags for security",
            "field",                    // Target type
            "string",                   // String subtype only
            "*",                        // Any field name
            "^(?!.*<script).*$",        // Regex pattern: no <script tags (case insensitive)
            true                        // Allow null (optional)
        ));
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
    public List<CustomConstraint> getFieldValidationConstraints() {
        return getAllValidationConstraints().stream()
            .filter(c -> c instanceof CustomConstraint)
            .map(c -> (CustomConstraint) c)
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