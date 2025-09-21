package com.draagon.meta.transform;

import com.draagon.meta.MetaData;

import java.util.List;

/**
 * TransformationRule defines a specific transformation that can be applied to metadata.
 * Rules are the core building blocks of the metadata transformation system, allowing
 * sophisticated business logic to be applied automatically.
 *
 * <p>Transformation rules can:</p>
 * <ul>
 *   <li><strong>Fix Constraint Violations:</strong> Automatically resolve validation failures</li>
 *   <li><strong>Enhance Metadata:</strong> Add missing attributes, fields, or relationships</li>
 *   <li><strong>Normalize Structure:</strong> Apply consistent patterns and conventions</li>
 *   <li><strong>Infer Relationships:</strong> Automatically detect and create connections</li>
 * </ul>
 *
 * <h3>Implementation Example:</h3>
 * <pre>{@code
 * public class JpaEnhancementRule implements TransformationRule {
 *     @Override
 *     public RuleResult apply(TransformationContext context) {
 *         // Find objects that need JPA attributes
 *         // Add missing @Entity, @Table, @Id annotations
 *         // Return result with transformations applied
 *     }
 * }
 * }</pre>
 *
 * @since 6.1.0
 */
public interface TransformationRule {

    /**
     * Get the unique name of this transformation rule
     * @return The rule name
     */
    String getName();

    /**
     * Get the description of what this rule does
     * @return A human-readable description
     */
    String getDescription();

    /**
     * Get the priority of this rule (higher priority rules are applied first)
     * @return The rule priority (0-1000, with 1000 being highest priority)
     */
    int getPriority();

    /**
     * Check if this rule is applicable to the given metadata list
     * @param metaDataList The metadata to check
     * @return True if this rule could transform any of the metadata
     */
    boolean isApplicableTo(List<MetaData> metaDataList);

    /**
     * Apply this transformation rule to the metadata in the given context
     * @param context The transformation context containing metadata and configuration
     * @return RuleResult indicating what was transformed and any issues encountered
     */
    RuleResult apply(TransformationContext context);

    /**
     * Get the category of this transformation rule
     * @return The rule category
     */
    default TransformationCategory getCategory() {
        return TransformationCategory.ENHANCEMENT;
    }

    /**
     * Check if this rule should run in preview mode
     * @return True if the rule supports preview mode
     */
    default boolean supportsPreview() {
        return true;
    }

    /**
     * Get the estimated impact of applying this rule
     * @param context The transformation context
     * @return The estimated number of transformations this rule would apply
     */
    default int estimateImpact(TransformationContext context) {
        return 0; // Override in implementations for better previews
    }

    /**
     * Categories of transformation rules for organization and prioritization
     */
    enum TransformationCategory {
        /** Rules that fix constraint violations */
        CONSTRAINT_RESOLUTION("Constraint Resolution", 900),

        /** Rules that enhance metadata with missing information */
        ENHANCEMENT("Enhancement", 700),

        /** Rules that normalize and standardize metadata structure */
        NORMALIZATION("Normalization", 500),

        /** Rules that optimize metadata for performance */
        OPTIMIZATION("Optimization", 300),

        /** Rules that clean up or reorganize metadata */
        CLEANUP("Cleanup", 100);

        private final String displayName;
        private final int defaultPriority;

        TransformationCategory(String displayName, int defaultPriority) {
            this.displayName = displayName;
            this.defaultPriority = defaultPriority;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getDefaultPriority() {
            return defaultPriority;
        }
    }
}