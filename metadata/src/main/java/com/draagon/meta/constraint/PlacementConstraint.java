package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;
import java.util.function.Predicate;

/**
 * PlacementConstraint defines WHERE a MetaData type can be placed in the metadata hierarchy.
 * Supports both simple string patterns (for easy generator integration) and predicate-based
 * matching (for existing code compatibility).
 *
 * Pattern Examples:
 * - "object.*" - any object subtype
 * - "field.string" - specific field.string subtype
 * - "metadata.root" - specific metadata root type
 *
 * Abstract Requirements:
 * - MUST_BE_ABSTRACT: Child must have isAbstract=true (default for metadata.root)
 * - MUST_BE_CONCRETE: Child must have isAbstract=false
 * - ANY: Can be abstract or concrete (default for most cases)
 */
public class PlacementConstraint implements Constraint {

    // Pattern-based fields (for simple generator integration)
    private final String parentPattern;
    private final String childPattern;
    private final PlacementPolicy policy;
    private final AbstractRequirement abstractRequirement;

    // Predicate-based fields (for existing code compatibility)
    private final String id;
    private final String description;
    private final Predicate<MetaData> parentMatcher;
    private final Predicate<MetaData> childMatcher;

    // Track which constructor was used
    private final boolean usePatterns;

    /**
     * Create a placement constraint with simple pattern matching (NEW APPROACH)
     * @param parentPattern Pattern for parent type (e.g., "object.*", "metadata.root")
     * @param childPattern Pattern for child type (e.g., "field.*", "attr.string")
     * @param policy Whether this placement is ALLOWED or FORBIDDEN
     */
    public PlacementConstraint(String parentPattern, String childPattern, PlacementPolicy policy) {
        this(parentPattern, childPattern, policy, AbstractRequirement.ANY);
    }

    /**
     * Create a placement constraint with abstract requirements (NEW APPROACH)
     * @param parentPattern Pattern for parent type (e.g., "object.*", "metadata.root")
     * @param childPattern Pattern for child type (e.g., "field.*", "attr.string")
     * @param policy Whether this placement is ALLOWED or FORBIDDEN
     * @param abstractRequirement Whether child must be abstract, concrete, or either
     */
    public PlacementConstraint(String parentPattern, String childPattern,
                              PlacementPolicy policy, AbstractRequirement abstractRequirement) {
        this.parentPattern = parentPattern;
        this.childPattern = childPattern;
        this.policy = policy;
        this.abstractRequirement = abstractRequirement;

        // Pattern-based approach
        this.usePatterns = true;
        this.id = null;
        this.description = null;
        this.parentMatcher = null;
        this.childMatcher = null;
    }

    /**
     * Create a placement constraint with predicate matching (EXISTING CODE COMPATIBILITY)
     * @param id Unique constraint identifier
     * @param description Human-readable description
     * @param parentMatcher Predicate to match parent MetaData
     * @param childMatcher Predicate to match child MetaData
     */
    public PlacementConstraint(String id, String description,
                              Predicate<MetaData> parentMatcher,
                              Predicate<MetaData> childMatcher) {
        // Predicate-based approach
        this.id = id;
        this.description = description;
        this.parentMatcher = parentMatcher;
        this.childMatcher = childMatcher;
        this.usePatterns = false;

        // Default values for pattern-based fields
        this.parentPattern = null;
        this.childPattern = null;
        this.policy = PlacementPolicy.ALLOWED; // Default to ALLOWED
        this.abstractRequirement = AbstractRequirement.ANY; // Default to ANY
    }

    /**
     * Check if a parent matches this constraint
     * @param parent The parent MetaData
     * @return True if parent matches this constraint
     */
    public boolean matchesParentPattern(MetaData parent) {
        if (usePatterns) {
            return matchesPattern(parent, parentPattern);
        } else {
            return parentMatcher != null && parentMatcher.test(parent);
        }
    }

    /**
     * Check if a child matches this constraint
     * @param child The child MetaData
     * @return True if child matches this constraint
     */
    public boolean matchesChildPattern(MetaData child) {
        if (usePatterns) {
            return matchesPattern(child, childPattern);
        } else {
            return childMatcher != null && childMatcher.test(child);
        }
    }

    /**
     * Check if this constraint applies to the given parent-child relationship
     * @param parent The parent MetaData
     * @param child The child MetaData
     * @return True if this constraint should be checked for this relationship
     */
    public boolean appliesTo(MetaData parent, MetaData child) {
        return matchesParentPattern(parent) && matchesChildPattern(child);
    }

    /**
     * Check if a MetaData matches a pattern like "object.*" or "field.string"
     * @param metaData The MetaData to check
     * @param pattern The pattern to match (type.subtype or type.*)
     * @return True if the MetaData matches the pattern
     */
    private boolean matchesPattern(MetaData metaData, String pattern) {
        if (pattern == null) return false;

        String[] parts = pattern.split("\\.");
        if (parts.length != 2) {
            return false; // Invalid pattern
        }

        String patternType = parts[0];
        String patternSubType = parts[1];

        // Check type match
        if (!patternType.equals(metaData.getType())) {
            return false;
        }

        // Check subtype match (* means any subtype)
        if ("*".equals(patternSubType)) {
            return true;
        }

        return patternSubType.equals(metaData.getSubType());
    }

    @Override
    public void validate(MetaData metaData, Object value)
            throws ConstraintViolationException {
        // PlacementConstraints are validated during addChild operations, not during value validation
        throw new UnsupportedOperationException(
            "PlacementConstraint validation should be called via appliesTo(), not validate()");
    }

    @Override
    public String getType() {
        return "placement";
    }

    @Override
    public String getDescription() {
        if (usePatterns) {
            return String.format("%s %s can %s %s",
                parentPattern,
                policy == PlacementPolicy.ALLOWED ? "can contain" : "cannot contain",
                abstractRequirement == AbstractRequirement.MUST_BE_ABSTRACT ? "abstract" :
                abstractRequirement == AbstractRequirement.MUST_BE_CONCRETE ? "concrete" : "any",
                childPattern);
        } else {
            return description != null ? description : "Predicate-based placement constraint";
        }
    }

    /**
     * Get the constraint ID (for predicate-based constraints)
     * @return The constraint ID or a generated one for pattern-based constraints
     */
    public String getId() {
        if (usePatterns) {
            return parentPattern + "->" + childPattern;
        } else {
            return id;
        }
    }

    /**
     * Get the parent pattern (for pattern-based constraints)
     * @return The parent pattern string (e.g., "object.*")
     */
    public String getParentPattern() {
        return parentPattern;
    }

    /**
     * Get the child pattern (for pattern-based constraints)
     * @return The child pattern string (e.g., "field.*")
     */
    public String getChildPattern() {
        return childPattern;
    }

    /**
     * Get the placement policy
     * @return Whether this placement is ALLOWED or FORBIDDEN
     */
    public PlacementPolicy getPolicy() {
        return policy;
    }

    /**
     * Check if this placement is allowed
     * @return True if policy is ALLOWED
     */
    public boolean isAllowed() {
        return policy == PlacementPolicy.ALLOWED;
    }

    /**
     * Get the abstract requirement
     * @return The abstract requirement for this placement
     */
    public AbstractRequirement getAbstractRequirement() {
        return abstractRequirement;
    }

    /**
     * Check if this constraint uses pattern-based matching
     * @return True if pattern-based, false if predicate-based
     */
    public boolean usePatternMatching() {
        return usePatterns;
    }

    @Override
    public String toString() {
        if (usePatterns) {
            return "PlacementConstraint{" +
                   "parent='" + parentPattern + '\'' +
                   ", child='" + childPattern + '\'' +
                   ", policy=" + policy +
                   ", abstract=" + abstractRequirement +
                   '}';
        } else {
            return "PlacementConstraint{" +
                   "id='" + id + '\'' +
                   ", description='" + description + '\'' +
                   ", predicate-based=true" +
                   '}';
        }
    }
}