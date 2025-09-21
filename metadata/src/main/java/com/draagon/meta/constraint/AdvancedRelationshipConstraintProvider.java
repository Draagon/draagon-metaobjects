package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.loader.MetaDataLoader;

/**
 * AdvancedRelationshipConstraintProvider demonstrates sophisticated cross-reference
 * validation patterns using the new RelationshipConstraint system. This showcases
 * advanced metadata integrity validation across the entire metadata graph.
 *
 * <h3>Advanced Constraint Patterns Demonstrated:</h3>
 * <ul>
 *   <li><strong>Foreign Key Validation:</strong> Object references must point to existing objects</li>
 *   <li><strong>Inheritance Validation:</strong> Base class requirements must be satisfied</li>
 *   <li><strong>Cross-Module Consistency:</strong> Related objects must have consistent attributes</li>
 *   <li><strong>Dependency Validation:</strong> Required dependencies must be properly configured</li>
 *   <li><strong>Circular Reference Detection:</strong> Prevent circular object references</li>
 * </ul>
 *
 * <h3>Business Rule Examples:</h3>
 * <pre>{@code
 * // Foreign Key: User.departmentId must reference existing Department object
 * RelationshipConstraint foreignKey = RelationshipConstraint.foreignKey("departmentId", "objectRef");
 *
 * // Inheritance: All BaseEntity subclasses must have an 'id' field
 * RelationshipConstraint inheritance = RelationshipConstraint.inheritance("id", "BaseEntity");
 *
 * // Consistency: All objects with same dbTable must have consistent schema
 * RelationshipConstraint consistency = RelationshipConstraint.consistency("dbTable");
 * }</pre>
 *
 * @since 6.1.0
 */
public class AdvancedRelationshipConstraintProvider implements ConstraintProvider {

    @Override
    public void registerConstraints(ConstraintRegistry registry) {
        // Register sophisticated relationship validation patterns
        addForeignKeyConstraints(registry);
        addInheritanceConstraints(registry);
        addConsistencyConstraints(registry);
        addDependencyConstraints(registry);
        addCircularReferenceConstraints(registry);
    }

    /**
     * Add foreign key relationship constraints
     */
    private void addForeignKeyConstraints(ConstraintRegistry registry) {
        // FOREIGN KEY: Fields with objectRef must reference existing objects
        RelationshipConstraint objectRefConstraint = new RelationshipConstraint(
            "relationship.foreignkey.objectRef",
            "Fields with objectRef attribute must reference existing MetaObject",
            (metadata) -> metadata instanceof MetaField && metadata.hasMetaAttr("objectRef"),
            (source, loader) -> {
                String objectRefName = source.getMetaAttr("objectRef").getValueAsString();
                try {
                    return loader.getMetaObjectByName(objectRefName);
                } catch (Exception e) {
                    return null; // Object not found
                }
            },
            (source, target, context) -> target != null, // Target must exist
            true // Require target
        );
        registry.addConstraint(objectRefConstraint);

        // FOREIGN KEY: Department references in User objects
        RelationshipConstraint departmentRefConstraint = new RelationshipConstraint(
            "relationship.foreignkey.department",
            "User.departmentId must reference existing Department object",
            (metadata) -> metadata instanceof MetaField &&
                         "departmentId".equals(metadata.getName()) &&
                         metadata.getParent() != null &&
                         "User".equals(metadata.getParent().getName()),
            (source, loader) -> {
                // Look for Department object
                try {
                    return loader.getMetaObjectByName("Department");
                } catch (Exception e) {
                    return null;
                }
            },
            (source, target, context) -> target != null, // Department must exist
            true // Require target
        );
        registry.addConstraint(departmentRefConstraint);
    }

    /**
     * Add inheritance relationship constraints
     */
    private void addInheritanceConstraints(ConstraintRegistry registry) {
        // INHERITANCE: BaseEntity subclasses must have 'id' field
        RelationshipConstraint baseEntityIdConstraint = new RelationshipConstraint(
            "relationship.inheritance.baseentity.id",
            "Objects extending BaseEntity must have an 'id' field",
            (metadata) -> metadata instanceof MetaObject &&
                         metadata.hasMetaAttr("extends") &&
                         "BaseEntity".equals(metadata.getMetaAttr("extends").getValueAsString()),
            (source, loader) -> ((MetaObject) source).getMetaField("id"),
            (source, target, context) -> target != null, // id field must exist
            true // Require target
        );
        registry.addConstraint(baseEntityIdConstraint);

        // INHERITANCE: AuditableEntity subclasses must have audit fields
        RelationshipConstraint auditableConstraint = new RelationshipConstraint(
            "relationship.inheritance.auditable.fields",
            "Objects extending AuditableEntity must have createdDate and modifiedDate fields",
            (metadata) -> metadata instanceof MetaObject &&
                         metadata.hasMetaAttr("extends") &&
                         "AuditableEntity".equals(metadata.getMetaAttr("extends").getValueAsString()),
            (source, loader) -> {
                // Check for both audit fields
                MetaObject metaObject = (MetaObject) source;
                MetaField createdDate = metaObject.getMetaField("createdDate");
                MetaField modifiedDate = metaObject.getMetaField("modifiedDate");
                return (createdDate != null && modifiedDate != null) ? createdDate : null;
            },
            (source, target, context) -> {
                // Both audit fields must exist
                MetaObject metaObject = (MetaObject) source;
                return metaObject.getMetaField("createdDate") != null &&
                       metaObject.getMetaField("modifiedDate") != null;
            },
            false // Don't require specific target, use custom validation
        );
        registry.addConstraint(auditableConstraint);
    }

    /**
     * Add cross-object consistency constraints
     */
    private void addConsistencyConstraints(ConstraintRegistry registry) {
        // CONSISTENCY: Objects with same dbTable must have consistent schema version
        RelationshipConstraint dbTableConsistencyConstraint = new RelationshipConstraint(
            "relationship.consistency.dbTable.schema",
            "Objects sharing dbTable must have consistent schema version",
            (metadata) -> metadata instanceof MetaObject && metadata.hasMetaAttr("dbTable"),
            (source, loader) -> {
                String sourceTable = source.getMetaAttr("dbTable").getValueAsString();
                String sourceSchema = source.hasMetaAttr("schemaVersion") ?
                    source.getMetaAttr("schemaVersion").getValueAsString() : "1.0";

                // Find other objects with same dbTable
                return loader.getChildren().stream()
                    .filter(child -> child instanceof MetaObject)
                    .filter(child -> child.hasMetaAttr("dbTable"))
                    .filter(child -> !child.equals(source))
                    .filter(child -> sourceTable.equals(child.getMetaAttr("dbTable").getValueAsString()))
                    .findFirst()
                    .orElse(null);
            },
            (source, target, context) -> {
                if (target == null) return true; // No conflicting objects

                String sourceSchema = source.hasMetaAttr("schemaVersion") ?
                    source.getMetaAttr("schemaVersion").getValueAsString() : "1.0";
                String targetSchema = target.hasMetaAttr("schemaVersion") ?
                    target.getMetaAttr("schemaVersion").getValueAsString() : "1.0";

                return sourceSchema.equals(targetSchema);
            },
            false // Don't require target
        );
        registry.addConstraint(dbTableConsistencyConstraint);

        // CONSISTENCY: JPA entity names must be unique
        RelationshipConstraint jpaEntityNameConstraint = new RelationshipConstraint(
            "relationship.consistency.jpa.entityName",
            "JPA entity names must be unique across all objects",
            (metadata) -> metadata instanceof MetaObject && metadata.hasMetaAttr("entityName"),
            (source, loader) -> {
                String sourceEntityName = source.getMetaAttr("entityName").getValueAsString();

                // Find other objects with same entityName
                return loader.getChildren().stream()
                    .filter(child -> child instanceof MetaObject)
                    .filter(child -> child.hasMetaAttr("entityName"))
                    .filter(child -> !child.equals(source))
                    .filter(child -> sourceEntityName.equals(child.getMetaAttr("entityName").getValueAsString()))
                    .findFirst()
                    .orElse(null);
            },
            (source, target, context) -> target == null, // No duplicate entity names
            false // Don't require target (we want null for uniqueness)
        );
        registry.addConstraint(jpaEntityNameConstraint);
    }

    /**
     * Add dependency validation constraints
     */
    private void addDependencyConstraints(ConstraintRegistry registry) {
        // DEPENDENCY: Objects with JPA generation must have required JPA attributes
        RelationshipConstraint jpaRequirementsConstraint = new RelationshipConstraint(
            "relationship.dependency.jpa.requirements",
            "Objects with JPA generation enabled must have dbTable attribute",
            (metadata) -> metadata instanceof MetaObject &&
                         (!metadata.hasMetaAttr("skipJpa") ||
                          !"true".equals(metadata.getMetaAttr("skipJpa").getValueAsString())),
            (source, loader) -> {
                // Check if object has required JPA attributes
                return source.hasMetaAttr("dbTable") ? source : null;
            },
            (source, target, context) -> target != null, // Must have dbTable
            true // Require target
        );
        registry.addConstraint(jpaRequirementsConstraint);

        // DEPENDENCY: Searchable fields must have search index configuration
        RelationshipConstraint searchableIndexConstraint = new RelationshipConstraint(
            "relationship.dependency.searchable.index",
            "Fields marked as searchable should have search index configuration",
            (metadata) -> metadata instanceof MetaField &&
                         metadata.hasMetaAttr("isSearchable") &&
                         "true".equals(metadata.getMetaAttr("isSearchable").getValueAsString()),
            (source, loader) -> {
                // Check parent object for search configuration
                MetaObject parent = (MetaObject) source.getParent();
                return parent != null && parent.hasMetaAttr("searchIndexed") ? parent : null;
            },
            (source, target, context) -> {
                // This is a warning-level constraint - searchable fields should have index config
                return true; // Don't fail validation, but log warning
            },
            false // Don't require target (this is advisory)
        );
        registry.addConstraint(searchableIndexConstraint);
    }

    /**
     * Add circular reference detection constraints
     */
    private void addCircularReferenceConstraints(ConstraintRegistry registry) {
        // CIRCULAR REFERENCE: Detect circular object references
        RelationshipConstraint circularReferenceConstraint = new RelationshipConstraint(
            "relationship.circular.objectRef",
            "Object references must not create circular dependencies",
            (metadata) -> metadata instanceof MetaField && metadata.hasMetaAttr("objectRef"),
            (source, loader) -> {
                // Traverse reference chain to detect circles
                String objectRefName = source.getMetaAttr("objectRef").getValueAsString();
                MetaData parent = source.getParent();
                if (parent instanceof MetaObject) {
                    return detectCircularReference((MetaObject) parent, objectRefName, loader, 10); // Max depth 10
                }
                return null; // Parent is not a MetaObject
            },
            (source, target, context) -> target == null, // No circular reference found
            false // Don't require target (null means no circle)
        );
        registry.addConstraint(circularReferenceConstraint);
    }

    /**
     * Helper method to detect circular references
     */
    private MetaObject detectCircularReference(MetaObject startObject, String targetName,
                                             MetaDataLoader loader, int maxDepth) {
        if (maxDepth <= 0 || startObject == null) {
            return null; // No circle found within depth limit
        }

        if (startObject.getName().equals(targetName)) {
            return startObject; // Circle detected!
        }

        try {
            MetaObject targetObject = loader.getMetaObjectByName(targetName);
            if (targetObject == null) {
                return null; // Target doesn't exist
            }

            // Check if target object has references back to start
            for (MetaField field : targetObject.getChildren(MetaField.class)) {
                if (field.hasMetaAttr("objectRef")) {
                    String nextRef = field.getMetaAttr("objectRef").getValueAsString();
                    MetaObject circularRef = detectCircularReference(startObject, nextRef, loader, maxDepth - 1);
                    if (circularRef != null) {
                        return circularRef; // Circular reference found
                    }
                }
            }
        } catch (Exception e) {
            // Ignore - target object doesn't exist
        }

        return null; // No circular reference
    }

    @Override
    public int getPriority() {
        return 800; // High priority for relationship validation
    }

    @Override
    public String getDescription() {
        return "Advanced relationship constraints for cross-reference validation and metadata graph integrity";
    }
}