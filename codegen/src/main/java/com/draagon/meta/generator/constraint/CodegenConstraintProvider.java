package com.draagon.meta.generator.constraint;

import com.draagon.meta.constraint.ConstraintProvider;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;

/**
 * Code generation constraint provider for attributes used in template-based code generation.
 * 
 * This provider defines constraints for attributes used by code generators, template engines,
 * and build-time code generation tools. These constraints are cross-cutting concerns that
 * apply to multiple MetaData types for code generation purposes.
 */
public class CodegenConstraintProvider implements ConstraintProvider {
    
    @Override
    public void registerConstraints(ConstraintRegistry registry) {
        // JPA generation control attributes
        addJpaGenerationConstraints(registry);
        
        // Database column mapping attributes  
        addDatabaseMappingConstraints(registry);
        
        // Field behavior attributes
        addFieldBehaviorConstraints(registry);
    }
    
    private void addJpaGenerationConstraints(ConstraintRegistry registry) {
        // PLACEMENT CONSTRAINT: skipJpa attribute can be placed on MetaObjects
        PlacementConstraint skipJpaObjectPlacement = new PlacementConstraint(
            "codegen.skipJpa.object.placement",
            "skipJpa attribute can be placed on MetaObjects to skip JPA generation",
            (parent) -> parent instanceof MetaObject,
            (child) -> child instanceof MetaAttribute && "skipJpa".equals(child.getName())
        );
        registry.addConstraint(skipJpaObjectPlacement);
        
        // VALIDATION CONSTRAINT: skipJpa must be boolean
        ValidationConstraint skipJpaObjectValidation = new ValidationConstraint(
            "codegen.skipJpa.object.validation",
            "skipJpa must be a boolean value (true/false)",
            (metadata) -> metadata instanceof MetaAttribute && "skipJpa".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        );
        registry.addConstraint(skipJpaObjectValidation);
        
        // PLACEMENT CONSTRAINT: skipJpa attribute can be placed on MetaFields
        PlacementConstraint skipJpaFieldPlacement = new PlacementConstraint(
            "codegen.skipJpa.field.placement",
            "skipJpa attribute can be placed on MetaFields to skip JPA generation",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && "skipJpa".equals(child.getName())
        );
        registry.addConstraint(skipJpaFieldPlacement);
        
        // VALIDATION CONSTRAINT: skipJpa must be boolean for fields too
        ValidationConstraint skipJpaFieldValidation = new ValidationConstraint(
            "codegen.skipJpa.field.validation",
            "skipJpa must be a boolean value (true/false)",
            (metadata) -> metadata instanceof MetaAttribute && "skipJpa".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        );
        registry.addConstraint(skipJpaFieldValidation);
    }
    
    private void addDatabaseMappingConstraints(ConstraintRegistry registry) {
        // PLACEMENT CONSTRAINT: dbTable attribute can be placed on MetaObjects
        PlacementConstraint dbTablePlacement = new PlacementConstraint(
            "codegen.dbTable.placement",
            "dbTable attribute can be placed on MetaObjects for JPA @Table annotation",
            (parent) -> parent instanceof MetaObject,
            (child) -> child instanceof MetaAttribute && "dbTable".equals(child.getName())
        );
        registry.addConstraint(dbTablePlacement);
        
        // VALIDATION CONSTRAINT: dbTable must be valid identifier
        ValidationConstraint dbTableValidation = new ValidationConstraint(
            "codegen.dbTable.validation",
            "dbTable must be a valid SQL table identifier",
            (metadata) -> metadata instanceof MetaAttribute && "dbTable".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String tableName = value.toString().trim();
                return tableName.matches("^[a-zA-Z][a-zA-Z0-9_]*$") && tableName.length() >= 1 && tableName.length() <= 64;
            }
        );
        registry.addConstraint(dbTableValidation);
        
        // PLACEMENT CONSTRAINT: dbColumn attribute can be placed on MetaFields
        PlacementConstraint dbColumnPlacement = new PlacementConstraint(
            "codegen.dbColumn.placement",
            "dbColumn attribute can be placed on MetaFields for JPA @Column annotation",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && "dbColumn".equals(child.getName())
        );
        registry.addConstraint(dbColumnPlacement);
        
        // VALIDATION CONSTRAINT: dbColumn must be valid identifier
        ValidationConstraint dbColumnValidation = new ValidationConstraint(
            "codegen.dbColumn.validation",
            "dbColumn must be a valid SQL column identifier",
            (metadata) -> metadata instanceof MetaAttribute && "dbColumn".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String columnName = value.toString().trim();
                return columnName.matches("^[a-zA-Z][a-zA-Z0-9_]*$") && columnName.length() >= 1 && columnName.length() <= 64;
            }
        );
        registry.addConstraint(dbColumnValidation);
    }
    
    private void addFieldBehaviorConstraints(ConstraintRegistry registry) {
        // PLACEMENT CONSTRAINT: collection attribute can be placed on MetaFields
        PlacementConstraint collectionPlacement = new PlacementConstraint(
            "codegen.collection.placement",
            "collection attribute can be placed on MetaFields to indicate collection type",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && "collection".equals(child.getName())
        );
        registry.addConstraint(collectionPlacement);
        
        // VALIDATION CONSTRAINT: collection must be boolean
        ValidationConstraint collectionValidation = new ValidationConstraint(
            "codegen.collection.validation",
            "collection must be a boolean value (true/false)",
            (metadata) -> metadata instanceof MetaAttribute && "collection".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        );
        registry.addConstraint(collectionValidation);
        
        // PLACEMENT CONSTRAINT: isSearchable attribute can be placed on MetaFields
        PlacementConstraint isSearchablePlacement = new PlacementConstraint(
            "codegen.isSearchable.placement",
            "isSearchable attribute can be placed on MetaFields for search functionality",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && "isSearchable".equals(child.getName())
        );
        registry.addConstraint(isSearchablePlacement);
        
        // VALIDATION CONSTRAINT: isSearchable must be boolean
        ValidationConstraint isSearchableValidation = new ValidationConstraint(
            "codegen.isSearchable.validation",
            "isSearchable must be a boolean value (true/false)",
            (metadata) -> metadata instanceof MetaAttribute && "isSearchable".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        );
        registry.addConstraint(isSearchableValidation);
    }
    
    @Override
    public int getPriority() {
        return 1200; // Lower priority than core, web, and database constraints
    }
    
    @Override
    public String getDescription() {
        return "Code generation constraints for template-based code generation attributes";
    }
}