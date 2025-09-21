package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;
import com.draagon.meta.loader.MetaDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RelationshipConstraintEnforcer handles validation of relationship constraints
 * across the entire metadata graph. This enforcer integrates with the existing
 * constraint system while providing specialized handling for graph-level validation.
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>Graph Traversal:</strong> Validates relationships across the entire metadata graph</li>
 *   <li><strong>Performance Optimized:</strong> Leverages read-optimized MetaDataLoader patterns</li>
 *   <li><strong>Batch Validation:</strong> Validates multiple relationships efficiently</li>
 *   <li><strong>Detailed Reporting:</strong> Provides comprehensive violation reports</li>
 * </ul>
 *
 * <h3>Usage Patterns:</h3>
 * <pre>{@code
 * // Validate all relationships for a MetaDataLoader
 * RelationshipConstraintEnforcer enforcer = new RelationshipConstraintEnforcer(constraintRegistry);
 * List<ConstraintViolation> violations = enforcer.validateAllRelationships(loader);
 *
 * // Validate relationships for specific MetaData
 * List<ConstraintViolation> violations = enforcer.validateRelationships(metaData, loader);
 * }</pre>
 *
 * @since 6.1.0
 */
public class RelationshipConstraintEnforcer {

    private static final Logger log = LoggerFactory.getLogger(RelationshipConstraintEnforcer.class);

    private final ConstraintRegistry constraintRegistry;

    /**
     * Create RelationshipConstraintEnforcer with constraint registry
     * @param constraintRegistry The constraint registry containing relationship constraints
     */
    public RelationshipConstraintEnforcer(ConstraintRegistry constraintRegistry) {
        this.constraintRegistry = constraintRegistry;
    }

    /**
     * Validate all relationship constraints across the entire metadata graph
     * @param loader The MetaDataLoader containing the metadata graph
     * @return List of constraint violations found
     */
    public List<ConstraintViolation> validateAllRelationships(MetaDataLoader loader) {
        List<ConstraintViolation> violations = new ArrayList<>();

        log.debug("Starting validation of all relationship constraints for loader: {}", loader.getName());

        List<RelationshipConstraint> relationshipConstraints = getRelationshipConstraints();

        if (relationshipConstraints.isEmpty()) {
            log.debug("No relationship constraints found - skipping validation");
            return violations;
        }

        // Get all MetaData from the loader for validation
        List<MetaData> allMetaData = collectAllMetaData(loader);

        log.debug("Validating {} relationship constraints against {} metadata objects",
                 relationshipConstraints.size(), allMetaData.size());

        // Validate each MetaData against all applicable relationship constraints
        for (MetaData metaData : allMetaData) {
            violations.addAll(validateRelationships(metaData, loader, relationshipConstraints));
        }

        log.info("Relationship validation complete. Found {} violations across {} constraints",
                violations.size(), relationshipConstraints.size());

        return violations;
    }

    /**
     * Validate relationship constraints for specific MetaData
     * @param metaData The MetaData to validate
     * @param loader The MetaDataLoader for graph traversal
     * @return List of constraint violations found
     */
    public List<ConstraintViolation> validateRelationships(MetaData metaData, MetaDataLoader loader) {
        return validateRelationships(metaData, loader, getRelationshipConstraints());
    }

    /**
     * Validate specific relationship constraints for MetaData
     * @param metaData The MetaData to validate
     * @param loader The MetaDataLoader for graph traversal
     * @param constraints The relationship constraints to check
     * @return List of constraint violations found
     */
    private List<ConstraintViolation> validateRelationships(MetaData metaData,
                                                           MetaDataLoader loader,
                                                           List<RelationshipConstraint> constraints) {
        List<ConstraintViolation> violations = new ArrayList<>();

        for (RelationshipConstraint constraint : constraints) {
            try {
                if (constraint.appliesTo(metaData)) {
                    ValidationContext context = ValidationContext.forRelationshipValidation(metaData, loader);

                    boolean isValid = constraint.validateRelationship(metaData, loader, context);

                    if (!isValid) {
                        violations.add(new ConstraintViolation(
                            constraint.getId(),
                            constraint.getDescription(),
                            metaData,
                            null, // No specific value for relationship constraints
                            "Relationship validation failed",
                            context
                        ));

                        log.warn("Relationship constraint violation: {} for MetaData: {}",
                                constraint.getId(), metaData.getName());
                    } else {
                        log.trace("Relationship constraint passed: {} for MetaData: {}",
                                 constraint.getId(), metaData.getName());
                    }
                }
            } catch (Exception e) {
                log.error("Error validating relationship constraint {} for MetaData {}: {}",
                         constraint.getId(), metaData.getName(), e.getMessage(), e);

                violations.add(new ConstraintViolation(
                    constraint.getId(),
                    constraint.getDescription(),
                    metaData,
                    null,
                    "Validation error: " + e.getMessage(),
                    ValidationContext.forRelationshipValidation(metaData, loader)
                ));
            }
        }

        return violations;
    }

    /**
     * Get all relationship constraints from the registry
     * @return List of RelationshipConstraint objects
     */
    private List<RelationshipConstraint> getRelationshipConstraints() {
        return constraintRegistry.getAllConstraints().stream()
                .filter(constraint -> constraint instanceof RelationshipConstraint)
                .map(constraint -> (RelationshipConstraint) constraint)
                .collect(Collectors.toList());
    }

    /**
     * Collect all MetaData objects from the loader for validation
     * @param loader The MetaDataLoader
     * @return List of all MetaData objects
     */
    private List<MetaData> collectAllMetaData(MetaDataLoader loader) {
        List<MetaData> allMetaData = new ArrayList<>();

        // Add direct children
        allMetaData.addAll(loader.getChildren());

        // Recursively add children of children
        for (MetaData child : loader.getChildren()) {
            allMetaData.addAll(collectMetaDataRecursively(child));
        }

        return allMetaData;
    }

    /**
     * Recursively collect all MetaData objects from a parent
     * @param parent The parent MetaData
     * @return List of all nested MetaData objects
     */
    private List<MetaData> collectMetaDataRecursively(MetaData parent) {
        List<MetaData> result = new ArrayList<>();

        for (MetaData child : parent.getChildren()) {
            result.add(child);
            result.addAll(collectMetaDataRecursively(child));
        }

        return result;
    }

    /**
     * Check if relationship constraints are enabled
     * @return True if relationship constraints should be validated
     */
    public boolean isRelationshipValidationEnabled() {
        return !getRelationshipConstraints().isEmpty();
    }

    /**
     * Get count of available relationship constraints
     * @return Number of relationship constraints in registry
     */
    public int getRelationshipConstraintCount() {
        return getRelationshipConstraints().size();
    }

    /**
     * Get statistics about relationship constraint validation
     * @param loader The MetaDataLoader to analyze
     * @return RelationshipValidationStats with validation metrics
     */
    public RelationshipValidationStats getValidationStats(MetaDataLoader loader) {
        List<RelationshipConstraint> constraints = getRelationshipConstraints();
        List<MetaData> allMetaData = collectAllMetaData(loader);

        int applicableConstraints = 0;
        for (RelationshipConstraint constraint : constraints) {
            for (MetaData metaData : allMetaData) {
                if (constraint.appliesTo(metaData)) {
                    applicableConstraints++;
                }
            }
        }

        return new RelationshipValidationStats(
            constraints.size(),
            allMetaData.size(),
            applicableConstraints
        );
    }

    /**
     * Statistics class for relationship validation metrics
     */
    public static class RelationshipValidationStats {
        private final int totalConstraints;
        private final int totalMetaData;
        private final int applicableConstraints;

        public RelationshipValidationStats(int totalConstraints, int totalMetaData, int applicableConstraints) {
            this.totalConstraints = totalConstraints;
            this.totalMetaData = totalMetaData;
            this.applicableConstraints = applicableConstraints;
        }

        public int getTotalConstraints() { return totalConstraints; }
        public int getTotalMetaData() { return totalMetaData; }
        public int getApplicableConstraints() { return applicableConstraints; }

        @Override
        public String toString() {
            return String.format("RelationshipValidationStats{constraints=%d, metaData=%d, applicable=%d}",
                    totalConstraints, totalMetaData, applicableConstraints);
        }
    }
}