package com.draagon.meta.constraint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * v6.0.0: Registry for constraint definitions with abstract reference resolution.
 * Provides constraint discovery, loading, and enforcement capabilities with graceful
 * degradation for unknown constraint types in enterprise scenarios.
 */
public class ConstraintRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(ConstraintRegistry.class);
    
    private static volatile ConstraintRegistry instance;
    private static final Object INIT_LOCK = new Object();
    
    private final ConstraintDefinitionParser parser;
    private final Map<ConstraintTarget, List<ConstraintDefinition>> constraintsByTarget;
    private final Map<String, ConstraintFactory> constraintFactories;
    private final Set<String> knownConstraintTypes;
    private final Set<String> unknownConstraintTypes;
    
    private ConstraintRegistry() {
        this.parser = new ConstraintDefinitionParser();
        this.constraintsByTarget = new ConcurrentHashMap<>();
        this.constraintFactories = new ConcurrentHashMap<>();
        this.knownConstraintTypes = ConcurrentHashMap.newKeySet();
        this.unknownConstraintTypes = ConcurrentHashMap.newKeySet();
        
        // Register built-in constraint factories
        registerBuiltInFactories();
        
        // Load constraints from discovered constraint files
        loadConstraintsFromClasspath();
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
     * Register a constraint factory for a specific constraint type
     * @param type The constraint type name
     * @param factory Factory for creating constraint instances
     */
    public void registerConstraintFactory(String type, ConstraintFactory factory) {
        constraintFactories.put(type, factory);
        knownConstraintTypes.add(type);
        unknownConstraintTypes.remove(type); // Remove from unknown if previously unknown
        
        log.debug("Registered constraint factory for type: {}", type);
    }
    
    /**
     * Load constraints from a specific resource file
     * @param resourcePath Path to constraint definition file
     * @throws ConstraintParseException If loading fails
     */
    public void loadConstraintsFromResource(String resourcePath) throws ConstraintParseException {
        log.debug("Loading constraints from resource: {}", resourcePath);
        
        ConstraintDefinitionParser.ConstraintDefinitions definitions = parser.parseFromResource(resourcePath);
        
        // Process references first
        for (String reference : definitions.getReferences()) {
            loadConstraintsFromResource(reference);
        }
        
        // Process constraint instances
        for (ConstraintDefinitionParser.ConstraintInstance instance : definitions.getInstances()) {
            processConstraintInstance(instance, resourcePath);
        }
        
        log.info("Loaded {} constraint instances from {}", definitions.getInstances().size(), resourcePath);
    }
    
    /**
     * Get all constraints applicable to a specific target
     * @param targetType Target type (e.g., "object", "field", "attr")
     * @param targetSubType Target subtype (can be null for any subtype)
     * @param targetName Target name (can be null for any name)
     * @return List of applicable constraints
     */
    public List<ConstraintDefinition> getConstraintsForTarget(String targetType, String targetSubType, String targetName) {
        List<ConstraintDefinition> result = new ArrayList<>();
        
        // Find constraints with exact matches and wildcards
        for (Map.Entry<ConstraintTarget, List<ConstraintDefinition>> entry : constraintsByTarget.entrySet()) {
            ConstraintTarget target = entry.getKey();
            if (target.matches(targetType, targetSubType, targetName)) {
                result.addAll(entry.getValue());
            }
        }
        
        return result;
    }
    
    /**
     * Get all registered constraint types (both known and unknown)
     * @return Set of constraint type names
     */
    public Set<String> getAllConstraintTypes() {
        Set<String> allTypes = new HashSet<>(knownConstraintTypes);
        allTypes.addAll(unknownConstraintTypes);
        return allTypes;
    }
    
    /**
     * Get constraint types that are known (have registered factories)
     * @return Set of known constraint type names
     */
    public Set<String> getKnownConstraintTypes() {
        return Collections.unmodifiableSet(knownConstraintTypes);
    }
    
    /**
     * Get constraint types that are unknown (no registered factories)
     * @return Set of unknown constraint type names
     */
    public Set<String> getUnknownConstraintTypes() {
        return Collections.unmodifiableSet(unknownConstraintTypes);
    }
    
    /**
     * Check if a constraint type is known (has a registered factory)
     * @param constraintType The constraint type to check
     * @return True if the constraint type is known
     */
    public boolean isConstraintTypeKnown(String constraintType) {
        return knownConstraintTypes.contains(constraintType);
    }
    
    /**
     * Clear all loaded constraints (for testing)
     */
    public void clearAllConstraints() {
        constraintsByTarget.clear();
        parser.clearLoaded();
    }
    
    private void registerBuiltInFactories() {
        // Register built-in constraint types
        registerConstraintFactory("required", new RequiredConstraintFactory());
        registerConstraintFactory("pattern", new PatternConstraintFactory());
        registerConstraintFactory("length", new LengthConstraintFactory());
        registerConstraintFactory("range", new RangeConstraintFactory());
        registerConstraintFactory("enum", new EnumConstraintFactory());
    }
    
    private void loadConstraintsFromClasspath() {
        // Discover constraint files from META-INF/constraints/ directory
        String[] standardConstraintFiles = {
            "META-INF/constraints/core-constraints.json",
            "META-INF/constraints/database-constraints.json", 
            "META-INF/constraints/web-constraints.json"
        };
        
        for (String constraintFile : standardConstraintFiles) {
            try {
                loadConstraintsFromResource(constraintFile);
            } catch (ConstraintParseException e) {
                // Not finding optional constraint files is okay
                log.debug("Optional constraint file not found: {}", constraintFile);
            }
        }
        
        // TODO: Add ServiceLoader-based discovery for additional constraint files from libraries
    }
    
    private void processConstraintInstance(ConstraintDefinitionParser.ConstraintInstance instance, String sourceName) {
        try {
            ConstraintDefinition constraintDef = resolveConstraintDefinition(instance, sourceName);
            if (constraintDef != null) {
                ConstraintTarget target = new ConstraintTarget(
                    instance.getTargetType(),
                    instance.getTargetSubType(),
                    instance.getTargetName()
                );
                
                constraintsByTarget.computeIfAbsent(target, k -> new ArrayList<>()).add(constraintDef);
                
                log.debug("Registered constraint [{}] for target [{}]", 
                    constraintDef.getType(), target);
            }
        } catch (Exception e) {
            log.error("Error processing constraint instance from {}: {}", sourceName, e.getMessage(), e);
        }
    }
    
    private ConstraintDefinition resolveConstraintDefinition(ConstraintDefinitionParser.ConstraintInstance instance, String sourceName) {
        String constraintType;
        Map<String, Object> parameters;
        
        if (instance.isAbstractReference()) {
            // Resolve abstract reference
            ConstraintDefinitionParser.AbstractConstraintDefinition abstractDef = 
                parser.getAbstractDefinitions().get(instance.getAbstractRef());
                
            if (abstractDef == null) {
                log.warn("Abstract constraint definition not found: {} (source: {})", 
                    instance.getAbstractRef(), sourceName);
                return null;
            }
            
            constraintType = abstractDef.getType();
            parameters = new HashMap<>(abstractDef.getParameters());
            
            // Apply parameter overrides
            parameters.putAll(instance.getParameters());
            
        } else if (instance.isInlineDefinition()) {
            // Use inline definition
            constraintType = instance.getInlineType();
            parameters = instance.getParameters();
            
        } else {
            log.warn("Constraint instance has neither abstract reference nor inline definition (source: {})", sourceName);
            return null;
        }
        
        // Check if constraint type is known
        if (!isConstraintTypeKnown(constraintType)) {
            unknownConstraintTypes.add(constraintType);
            log.warn("Unknown constraint type '{}' will be ignored (source: {}). " +
                "Register a ConstraintFactory to handle this type.", constraintType, sourceName);
            return null; // Graceful degradation - ignore unknown constraints
        }
        
        return new ConstraintDefinition(constraintType, parameters, sourceName);
    }
    
    /**
     * Target specification for constraint matching
     */
    private static class ConstraintTarget {
        private final String type;
        private final String subType;
        private final String name;
        
        public ConstraintTarget(String type, String subType, String name) {
            this.type = type;
            this.subType = subType;
            this.name = name;
        }
        
        public boolean matches(String targetType, String targetSubType, String targetName) {
            // Type must match exactly (required)
            if (!type.equals(targetType)) {
                return false;
            }
            
            // SubType matching: null means "any", "*" means "any", otherwise exact match
            if (subType != null && !"*".equals(subType)) {
                if (targetSubType == null || !subType.equals(targetSubType)) {
                    return false;
                }
            }
            
            // Name matching: null means "any", "*" means "any", otherwise exact match
            if (name != null && !"*".equals(name)) {
                if (targetName == null || !name.equals(targetName)) {
                    return false;
                }
            }
            
            return true;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConstraintTarget that = (ConstraintTarget) o;
            return Objects.equals(type, that.type) &&
                   Objects.equals(subType, that.subType) &&
                   Objects.equals(name, that.name);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(type, subType, name);
        }
        
        @Override
        public String toString() {
            return "ConstraintTarget{" +
                   "type='" + type + '\'' +
                   ", subType='" + subType + '\'' +
                   ", name='" + name + '\'' +
                   '}';
        }
    }
    
    /**
     * Resolved constraint definition ready for enforcement
     */
    public static class ConstraintDefinition {
        private final String type;
        private final Map<String, Object> parameters;
        private final String sourceName;
        
        public ConstraintDefinition(String type, Map<String, Object> parameters, String sourceName) {
            this.type = type;
            this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
            this.sourceName = sourceName;
        }
        
        public String getType() { return type; }
        public Map<String, Object> getParameters() { return parameters; }
        public String getSourceName() { return sourceName; }
        
        /**
         * Create a constraint instance using the registered factory
         * @return Constraint instance or null if factory not available
         */
        public Constraint createConstraint() {
            ConstraintFactory factory = ConstraintRegistry.getInstance().constraintFactories.get(type);
            if (factory == null) {
                return null;
            }
            return factory.createConstraint(parameters);
        }
    }
}