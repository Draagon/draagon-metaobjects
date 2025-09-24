package com.draagon.meta.constraint;

import com.draagon.meta.registry.AcceptsChildrenDeclaration;
import com.draagon.meta.registry.AcceptsParentsDeclaration;
import com.draagon.meta.registry.TypeDefinition;
import com.draagon.meta.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;

/**
 * Flattens bidirectional constraints into efficient runtime lookup structures.
 *
 * This class takes the bidirectional constraint declarations (acceptsChildren/acceptsParents)
 * from TypeDefinition objects and creates optimized lookup tables for O(1) constraint checking.
 *
 * Key Algorithm: For a placement to be valid, BOTH parent and child must agree:
 * - Parent must declare it accepts this child (via acceptsChildren)
 * - Child must declare it accepts this parent (via acceptsParents)
 *
 * Features:
 * - Inheritance chain resolution
 * - Wildcard matching support ("*" patterns)
 * - Three-part constraint matching (type, subType, name)
 * - Efficient O(1) runtime lookups
 * - Thread-safe concurrent access
 */
public class ConstraintFlattener {

    private static final Logger log = LoggerFactory.getLogger(ConstraintFlattener.class);

    // Flattened constraint lookup structures (thread-safe)
    private final Map<String, PlacementRule> flattenedRules = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> validParentTypes = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> validChildTypes = new ConcurrentHashMap<>();

    // Source registry for inheritance resolution
    private final MetaDataRegistry typeRegistry;

    // Cache for inheritance chains (WeakHashMap for OSGI compatibility)
    private final Map<String, List<String>> inheritanceChainCache = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Represents a flattened placement rule after bidirectional resolution
     */
    public static class PlacementRule {
        private final String parentQualifiedType;
        private final String childQualifiedType;
        private final String childName;
        private final boolean nameSpecific;
        private final boolean allowed;
        private final String reason;

        public PlacementRule(String parentQualifiedType, String childQualifiedType, String childName,
                           boolean nameSpecific, boolean allowed, String reason) {
            this.parentQualifiedType = parentQualifiedType;
            this.childQualifiedType = childQualifiedType;
            this.childName = childName;
            this.nameSpecific = nameSpecific;
            this.allowed = allowed;
            this.reason = reason;
        }

        public String getParentQualifiedType() { return parentQualifiedType; }
        public String getChildQualifiedType() { return childQualifiedType; }
        public String getChildName() { return childName; }
        public boolean isNameSpecific() { return nameSpecific; }
        public boolean isAllowed() { return allowed; }
        public String getReason() { return reason; }

        @Override
        public String toString() {
            return String.format("PlacementRule{parent=%s, child=%s, name=%s, allowed=%s, reason=%s}",
                parentQualifiedType, childQualifiedType, childName, allowed, reason);
        }
    }

    public ConstraintFlattener(MetaDataRegistry typeRegistry) {
        this.typeRegistry = Objects.requireNonNull(typeRegistry, "TypeRegistry cannot be null");
        log.debug("Initialized ConstraintFlattener with registry containing {} types",
                typeRegistry.getRegisteredTypes().size());
    }

    /**
     * Flatten all bidirectional constraints from the registry into efficient lookup structures
     */
    public void flattenAllConstraints() {
        log.info("Starting constraint flattening for {} registered types",
                typeRegistry.getRegisteredTypes().size());

        // CRITICAL: Ensure all inheritance resolution is complete before flattening
        int resolvedCount = typeRegistry.resolveDeferredInheritance();
        if (resolvedCount > 0) {
            log.info("Resolved {} deferred inheritance relationships before constraint flattening", resolvedCount);
        }

        long startTime = System.currentTimeMillis();
        int rulesGenerated = 0;
        int conflictsResolved = 0;

        // Clear previous flattening results
        flattenedRules.clear();
        validParentTypes.clear();
        validChildTypes.clear();
        inheritanceChainCache.clear();

        // Get all type definitions
        Collection<TypeDefinition> allTypes = typeRegistry.getAllTypeDefinitions();

        // Generate flattened rules for all possible parent-child combinations
        for (TypeDefinition parentType : allTypes) {
            for (TypeDefinition childType : allTypes) {
                List<PlacementRule> rules = generatePlacementRules(parentType, childType);
                for (PlacementRule rule : rules) {
                    String ruleKey = createRuleKey(rule);
                    flattenedRules.put(ruleKey, rule);
                    rulesGenerated++;

                    if (rule.isAllowed()) {
                        // Update quick lookup tables
                        validParentTypes.computeIfAbsent(rule.getChildQualifiedType(), k -> ConcurrentHashMap.newKeySet())
                                       .add(rule.getParentQualifiedType());
                        validChildTypes.computeIfAbsent(rule.getParentQualifiedType(), k -> ConcurrentHashMap.newKeySet())
                                      .add(rule.getChildQualifiedType());
                    }
                }
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("Constraint flattening completed: {} rules generated, {} conflicts resolved in {}ms",
                rulesGenerated, conflictsResolved, (endTime - startTime));
        log.debug("Generated {} parent type mappings, {} child type mappings",
                validParentTypes.size(), validChildTypes.size());
    }

    /**
     * Generate placement rules for a specific parent-child type combination
     */
    private List<PlacementRule> generatePlacementRules(TypeDefinition parentType, TypeDefinition childType) {
        List<PlacementRule> rules = new ArrayList<>();

        String parentQualified = parentType.getQualifiedName();
        String childQualified = childType.getQualifiedName();

        // Check all possible name combinations (named + unnamed)
        Set<String> childNames = extractPossibleChildNames(parentType, childType);

        for (String childName : childNames) {
            PlacementRule rule = evaluateBidirectionalConstraint(parentType, childType, childName);
            if (rule != null) {
                rules.add(rule);
            }
        }

        return rules;
    }

    /**
     * Core bidirectional constraint evaluation: BOTH parent and child must agree
     */
    private PlacementRule evaluateBidirectionalConstraint(TypeDefinition parentType, TypeDefinition childType, String childName) {
        String parentQualified = parentType.getQualifiedName();
        String childQualified = childType.getQualifiedName();

        // Check if parent accepts this child
        boolean parentAccepts = evaluateParentAcceptance(parentType, childType, childName);

        // Check if child accepts this parent
        boolean childAccepts = evaluateChildAcceptance(childType, parentType, childName);


        // BIDIRECTIONAL RULE: Both must agree for placement to be allowed
        boolean allowed = parentAccepts && childAccepts;

        String reason;
        if (allowed) {
            reason = "Both parent and child accept this placement";
        } else if (!parentAccepts && !childAccepts) {
            reason = "Neither parent nor child accepts this placement";
        } else if (!parentAccepts) {
            reason = "Parent does not accept this child";
        } else {
            reason = "Child does not accept this parent";
        }

        boolean nameSpecific = childName != null && !"*".equals(childName);

        return new PlacementRule(parentQualified, childQualified, childName, nameSpecific, allowed, reason);
    }

    /**
     * Evaluate if parent type accepts the given child
     */
    private boolean evaluateParentAcceptance(TypeDefinition parentType, TypeDefinition childType, String childName) {
        // Check all acceptsChildren declarations (including inherited)
        for (AcceptsChildrenDeclaration declaration : parentType.getAcceptsChildren()) {
            if (matchesChildDeclaration(declaration, childType, childName)) {
                log.trace("Parent {} accepts child {} via declaration: {}",
                        parentType.getQualifiedName(), childType.getQualifiedName(), declaration);
                return true;
            }
        }

        return false;
    }

    /**
     * Evaluate if child type accepts the given parent
     */
    private boolean evaluateChildAcceptance(TypeDefinition childType, TypeDefinition parentType, String childName) {
        // Check all acceptsParents declarations
        for (AcceptsParentsDeclaration declaration : childType.getAcceptsParents()) {
            if (matchesParentDeclaration(declaration, parentType, childName)) {
                log.trace("Child {} accepts parent {} via declaration: {}",
                        childType.getQualifiedName(), parentType.getQualifiedName(), declaration);
                return true;
            }
        }

        return false;
    }

    /**
     * Check if an AcceptsChildrenDeclaration matches the actual child
     */
    private boolean matchesChildDeclaration(AcceptsChildrenDeclaration declaration, TypeDefinition childType, String childName) {
        // Match child type
        if (!matchesTypePattern(declaration.getChildType(), childType.getType())) {
            return false;
        }

        // Match child subType
        if (!matchesTypePattern(declaration.getChildSubType(), childType.getSubType())) {
            return false;
        }

        // Match child name (if specified)
        String declaredName = declaration.getChildName();
        if (declaredName != null && !"*".equals(declaredName)) {
            if (childName == null || !declaredName.equals(childName)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if an AcceptsParentsDeclaration matches the actual parent
     */
    private boolean matchesParentDeclaration(AcceptsParentsDeclaration declaration, TypeDefinition parentType, String childName) {
        // Match parent type
        if (!matchesTypePattern(declaration.getParentType(), parentType.getType())) {
            return false;
        }

        // Match parent subType
        if (!matchesTypePattern(declaration.getParentSubType(), parentType.getSubType())) {
            return false;
        }

        // Match expected child name condition (if specified)
        String expectedChildName = declaration.getExpectedChildName();
        if (expectedChildName != null && !"*".equals(expectedChildName)) {
            if (childName == null || !expectedChildName.equals(childName)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Wildcard pattern matching for types
     */
    private boolean matchesTypePattern(String pattern, String actual) {
        if (pattern == null || actual == null) {
            return false;
        }

        // Wildcard matches everything
        if ("*".equals(pattern)) {
            return true;
        }

        // Exact match
        if (pattern.equals(actual)) {
            return true;
        }

        // TODO: Add support for more complex patterns if needed (regex, etc.)

        return false;
    }

    /**
     * Extract all possible child names that could be relevant for this parent-child combination
     */
    private Set<String> extractPossibleChildNames(TypeDefinition parentType, TypeDefinition childType) {
        Set<String> names = new HashSet<>();

        // Always include unnamed case
        names.add(null);
        names.add("*");

        // Extract names from parent's acceptsChildren declarations
        for (AcceptsChildrenDeclaration declaration : parentType.getAcceptsChildren()) {
            if (declaration.getChildName() != null) {
                names.add(declaration.getChildName());
            }
        }

        // Extract names from child's acceptsParents declarations
        for (AcceptsParentsDeclaration declaration : childType.getAcceptsParents()) {
            if (declaration.getExpectedChildName() != null) {
                names.add(declaration.getExpectedChildName());
            }
        }

        return names;
    }

    /**
     * Create unique rule key for storage
     */
    private String createRuleKey(PlacementRule rule) {
        return String.format("%s -> %s [%s]",
                rule.getParentQualifiedType(),
                rule.getChildQualifiedType(),
                rule.getChildName() != null ? rule.getChildName() : "*");
    }

    /**
     * Public API: Check if a placement is allowed (O(1) lookup)
     */
    public boolean isPlacementAllowed(String parentType, String parentSubType, String childType, String childSubType, String childName) {
        String parentQualified = parentType + "." + parentSubType;
        String childQualified = childType + "." + childSubType;

        // DEBUG: Log the lookup attempt
        log.info("DEBUG: Looking up placement: parent={}, child={}, name={}", parentQualified, childQualified, childName);

        // Try exact match first
        String exactKey = String.format("%s -> %s [%s]", parentQualified, childQualified, childName != null ? childName : "*");
        PlacementRule exactRule = flattenedRules.get(exactKey);
        log.info("DEBUG: Exact key lookup: '{}' -> {}", exactKey, exactRule != null ? "FOUND" : "NOT FOUND");
        if (exactRule != null) {
            log.info("DEBUG: Found rule - allowed={}, reason={}", exactRule.isAllowed(), exactRule.getReason());
            log.trace("Found exact placement rule: {}", exactRule);
            return exactRule.isAllowed();
        }

        // Try wildcard fallbacks
        String wildcardKey = String.format("%s -> %s [*]", parentQualified, childQualified);
        PlacementRule wildcardRule = flattenedRules.get(wildcardKey);
        log.info("DEBUG: Wildcard key lookup: '{}' -> {}", wildcardKey, wildcardRule != null ? "FOUND" : "NOT FOUND");
        if (wildcardRule != null) {
            log.trace("Found wildcard placement rule: {}", wildcardRule);
            return wildcardRule.isAllowed();
        }

        // CASE-INSENSITIVE FALLBACK: Try case-insensitive lookup for both exact and wildcard
        // This handles field.objectarray vs field.objectArray mismatches
        log.info("DEBUG: Trying case-insensitive fallback for failed lookup");
        for (Map.Entry<String, PlacementRule> entry : flattenedRules.entrySet()) {
            String ruleKey = entry.getKey();

            // Check case-insensitive match for exact key
            if (ruleKey.equalsIgnoreCase(exactKey)) {
                log.info("DEBUG: Case-insensitive exact match found: '{}' matches '{}'", exactKey, ruleKey);
                return entry.getValue().isAllowed();
            }

            // Check case-insensitive match for wildcard key
            if (ruleKey.equalsIgnoreCase(wildcardKey)) {
                log.info("DEBUG: Case-insensitive wildcard match found: '{}' matches '{}'", wildcardKey, ruleKey);
                return entry.getValue().isAllowed();
            }
        }

        // DEBUG: Show what keys ARE available for this parent
        log.info("DEBUG: Available keys starting with '{}':", parentQualified);
        flattenedRules.keySet().stream()
            .filter(key -> key.startsWith(parentQualified))
            .limit(10)
            .forEach(key -> log.info("  Available key: '{}'", key));

        // Default to false if no rule found
        log.trace("No placement rule found for {} -> {} [{}], defaulting to false",
                parentQualified, childQualified, childName);
        return false;
    }

    /**
     * Get all valid child types for a parent type (for quick lookups)
     */
    public Set<String> getValidChildTypes(String parentType, String parentSubType) {
        String parentQualified = parentType + "." + parentSubType;
        return validChildTypes.getOrDefault(parentQualified, Collections.emptySet());
    }

    /**
     * Get all valid parent types for a child type (for quick lookups)
     */
    public Set<String> getValidParentTypes(String childType, String childSubType) {
        String childQualified = childType + "." + childSubType;
        return validParentTypes.getOrDefault(childQualified, Collections.emptySet());
    }

    /**
     * Get flattening statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRules", flattenedRules.size());
        stats.put("allowedRules", flattenedRules.values().stream().mapToLong(r -> r.isAllowed() ? 1 : 0).sum());
        stats.put("parentTypeMappings", validParentTypes.size());
        stats.put("childTypeMappings", validChildTypes.size());
        stats.put("inheritanceChainsCached", inheritanceChainCache.size());
        return stats;
    }

    /**
     * Debug method to get all flattened rules
     */
    public Map<String, PlacementRule> getAllRules() {
        return Collections.unmodifiableMap(flattenedRules);
    }
}