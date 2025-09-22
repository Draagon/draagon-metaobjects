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
    public static void registerType(Class<? extends MetaData> clazz, 
                                   Consumer<TypeDefinitionBuilder> configurator) {
        TypeDefinitionBuilder builder = TypeDefinitionBuilder.forClass(clazz);
        configurator.accept(builder);
        getInstance().register(builder.build());
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
        } catch (ClassNotFoundException e) {
            log.warn("Some core types could not be loaded: {}", e.getMessage());
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