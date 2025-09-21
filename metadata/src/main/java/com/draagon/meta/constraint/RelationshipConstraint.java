package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;
import com.draagon.meta.loader.MetaDataLoader;

import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * RelationshipConstraint validates relationships between different MetaData objects
 * across the entire metadata graph. This enables sophisticated cross-reference
 * validation that goes beyond simple parent-child placement rules.
 *
 * <p>Examples of relationship constraints:</p>
 * <ul>
 *   <li><strong>Foreign Key Validation:</strong> "Field with objectRef='User' must reference existing User object"</li>
 *   <li><strong>Inheritance Validation:</strong> "Object extending 'BaseEntity' must have 'id' field"</li>
 *   <li><strong>Cross-Module Consistency:</strong> "Database table name must match across objects and fields"</li>
 *   <li><strong>Dependency Validation:</strong> "Object A depends on Object B being properly configured"</li>
 * </ul>
 *
 * <h3>Architecture Benefits:</h3>
 * <ul>
 *   <li><strong>Graph-Level Integrity:</strong> Validates entire metadata graph consistency</li>
 *   <li><strong>Dynamic Resolution:</strong> Resolves references at validation time</li>
 *   <li><strong>Performance Optimized:</strong> Leverages read-optimized MetaDataLoader patterns</li>
 *   <li><strong>Extensible:</strong> Plugin-friendly for custom business rules</li>
 * </ul>
 *
 * @since 6.1.0
 */
public class RelationshipConstraint implements Constraint {

    /**
     * Functional interface for resolving related MetaData objects
     */
    @FunctionalInterface
    public interface MetaDataResolver {
        /**
         * Resolve related MetaData for the given source MetaData
         * @param source The source MetaData
         * @param loader The MetaDataLoader for graph traversal
         * @return The resolved related MetaData, or null if not found
         */
        MetaData resolve(MetaData source, MetaDataLoader loader);
    }

    /**
     * Functional interface for validating relationships between two MetaData objects
     */
    @FunctionalInterface
    public interface RelationshipValidator {
        /**
         * Validate the relationship between source and target MetaData
         * @param source The source MetaData
         * @param target The target MetaData (may be null if not found)
         * @param context Validation context
         * @return True if the relationship is valid
         */
        boolean test(MetaData source, MetaData target, ValidationContext context);
    }

    private final String id;
    private final String description;
    private final Function<MetaData, Boolean> applicableTo;
    private final MetaDataResolver resolver;
    private final RelationshipValidator validator;
    private final boolean requireTarget;

    /**
     * Create a relationship constraint
     *
     * @param id Unique identifier for this constraint
     * @param description Human-readable description of the relationship rule
     * @param applicableTo Function to test if this constraint applies to a MetaData
     * @param resolver Function to resolve the related MetaData object
     * @param validator Function to validate the relationship
     * @param requireTarget Whether the target MetaData must exist for validation to pass
     */
    public RelationshipConstraint(String id, String description,
                                 Function<MetaData, Boolean> applicableTo,
                                 MetaDataResolver resolver,
                                 RelationshipValidator validator,
                                 boolean requireTarget) {
        this.id = id;
        this.description = description;
        this.applicableTo = applicableTo;
        this.resolver = resolver;
        this.validator = validator;
        this.requireTarget = requireTarget;
    }

    /**
     * Check if this constraint applies to the given MetaData
     * @param metaData The MetaData to check
     * @return True if this relationship constraint should be applied
     */
    public boolean appliesTo(MetaData metaData) {
        return applicableTo.apply(metaData);
    }

    /**
     * Validate the relationship for the given MetaData
     * @param source The source MetaData
     * @param loader The MetaDataLoader for graph traversal
     * @param context Validation context
     * @return True if the relationship is valid
     */
    public boolean validateRelationship(MetaData source, MetaDataLoader loader, ValidationContext context) {
        if (!appliesTo(source)) {
            return true; // Constraint doesn't apply
        }

        // Resolve the target MetaData
        MetaData target = resolver.resolve(source, loader);

        // Check if target is required but not found
        if (requireTarget && target == null) {
            return false;
        }

        // Validate the relationship
        return validator.test(source, target, context);
    }

    @Override
    public void validate(MetaData metaData, Object value, ValidationContext context)
            throws ConstraintViolationException {
        // RelationshipConstraints require a MetaDataLoader for graph traversal
        // This method should not be called directly - use validateRelationship() instead
        throw new UnsupportedOperationException(
            "RelationshipConstraint validation requires MetaDataLoader - use validateRelationship() method");
    }

    @Override
    public String getType() {
        return "relationship";
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isApplicableTo(String metaDataType) {
        // RelationshipConstraints determine applicability dynamically
        return true;
    }

    /**
     * Get the unique identifier for this constraint
     * @return The constraint ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the MetaData resolver
     * @return The resolver function
     */
    public MetaDataResolver getResolver() {
        return resolver;
    }

    /**
     * Get the relationship validator
     * @return The validator function
     */
    public RelationshipValidator getValidator() {
        return validator;
    }

    /**
     * Check if target MetaData is required
     * @return True if target must exist for validation to pass
     */
    public boolean isTargetRequired() {
        return requireTarget;
    }

    @Override
    public String toString() {
        return "RelationshipConstraint{" +
               "id='" + id + '\'' +
               ", description='" + description + '\'' +
               ", requireTarget=" + requireTarget +
               '}';
    }

    // ============= FACTORY METHODS FOR COMMON RELATIONSHIP PATTERNS =============

    /**
     * Create a foreign key relationship constraint
     * @param fieldName The field name that contains the object reference
     * @param attributeName The attribute name containing the target object name
     * @return RelationshipConstraint for foreign key validation
     */
    public static RelationshipConstraint foreignKey(String fieldName, String attributeName) {
        return new RelationshipConstraint(
            "relationship.foreignkey." + fieldName,
            "Field '" + fieldName + "' with " + attributeName + " must reference existing object",
            (metadata) -> metadata.getName().equals(fieldName) && metadata.hasMetaAttr(attributeName),
            (source, loader) -> {
                String targetName = source.getMetaAttr(attributeName).getValueAsString();
                return loader.getMetaObjectByName(targetName);
            },
            (source, target, context) -> target != null, // Target must exist
            true // Require target
        );
    }

    /**
     * Create an inheritance relationship constraint
     * @param requiredFieldName The field that must exist in objects extending a base
     * @param baseObjectName The name of the base object
     * @return RelationshipConstraint for inheritance validation
     */
    public static RelationshipConstraint inheritance(String requiredFieldName, String baseObjectName) {
        return new RelationshipConstraint(
            "relationship.inheritance." + baseObjectName,
            "Objects extending '" + baseObjectName + "' must have field '" + requiredFieldName + "'",
            (metadata) -> metadata.hasMetaAttr("extends") &&
                         baseObjectName.equals(metadata.getMetaAttr("extends").getValueAsString()),
            (source, loader) -> {
                if (source instanceof com.draagon.meta.object.MetaObject) {
                    return ((com.draagon.meta.object.MetaObject) source).getMetaField(requiredFieldName);
                }
                return null;
            },
            (source, target, context) -> target != null, // Field must exist
            true // Require target
        );
    }

    /**
     * Create a consistency relationship constraint
     * @param attributeName The attribute that must be consistent
     * @return RelationshipConstraint for cross-object attribute consistency
     */
    public static RelationshipConstraint consistency(String attributeName) {
        return new RelationshipConstraint(
            "relationship.consistency." + attributeName,
            "Attribute '" + attributeName + "' must be consistent across related objects",
            (metadata) -> metadata.hasMetaAttr(attributeName),
            (source, loader) -> {
                // Find related objects with same attribute
                return loader.getChildren().stream()
                    .filter(child -> child.hasMetaAttr(attributeName))
                    .filter(child -> !child.equals(source))
                    .findFirst()
                    .orElse(null);
            },
            (source, target, context) -> {
                if (target == null) return true; // No related objects
                String sourceValue = source.getMetaAttr(attributeName).getValueAsString();
                String targetValue = target.getMetaAttr(attributeName).getValueAsString();
                return sourceValue.equals(targetValue);
            },
            false // Don't require target
        );
    }
}