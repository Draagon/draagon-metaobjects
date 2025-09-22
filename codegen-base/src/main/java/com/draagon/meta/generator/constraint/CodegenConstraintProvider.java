package com.draagon.meta.generator.constraint;

import com.draagon.meta.constraint.ConstraintProvider;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;

import static com.draagon.meta.generator.constraint.CodegenAttributeConstants.*;

/**
 * Code generation constraint provider for code generation specific attributes.
 *
 * <p>This provider defines constraints for attributes used exclusively by code generators,
 * template engines, and build-time code generation tools. Database-related attributes
 * (dbTable, dbColumn, etc.) are handled by the shared DatabaseConstraintProvider to avoid
 * duplication between OMDB and codegen modules.</p>
 *
 * <p><strong>Scope:</strong> JPA generation control, field behavior markers, and template
 * processing attributes only.</p>
 */
public class CodegenConstraintProvider implements ConstraintProvider {
    
    @Override
    public void registerConstraints(ConstraintRegistry registry) {
        // JPA generation control attributes
        addJpaGenerationConstraints(registry);

        // Field behavior attributes (codegen-specific)
        addFieldBehaviorConstraints(registry);
    }
    
    private void addJpaGenerationConstraints(ConstraintRegistry registry) {
        // PLACEMENT CONSTRAINT: skipJpa attribute can be placed on MetaObjects
        PlacementConstraint skipJpaObjectPlacement = new PlacementConstraint(
            "codegen.skipJpa.object.placement",
            "skipJpa attribute can be placed on MetaObjects to skip JPA generation",
            (parent) -> parent instanceof MetaObject,
            (child) -> child instanceof MetaAttribute && ATTR_SKIP_JPA.equals(child.getName())
        );
        registry.addConstraint(skipJpaObjectPlacement);
        
        // VALIDATION CONSTRAINT: skipJpa must be boolean
        ValidationConstraint skipJpaObjectValidation = new ValidationConstraint(
            "codegen.skipJpa.object.validation",
            "skipJpa must be a boolean value (true/false)",
            (metadata) -> metadata instanceof MetaAttribute && ATTR_SKIP_JPA.equals(metadata.getName()),
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
            (child) -> child instanceof MetaAttribute && ATTR_SKIP_JPA.equals(child.getName())
        );
        registry.addConstraint(skipJpaFieldPlacement);
        
        // VALIDATION CONSTRAINT: skipJpa must be boolean for fields too
        ValidationConstraint skipJpaFieldValidation = new ValidationConstraint(
            "codegen.skipJpa.field.validation",
            "skipJpa must be a boolean value (true/false)",
            (metadata) -> metadata instanceof MetaAttribute && ATTR_SKIP_JPA.equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        );
        registry.addConstraint(skipJpaFieldValidation);
    }
    
    
    private void addFieldBehaviorConstraints(ConstraintRegistry registry) {
        // PLACEMENT CONSTRAINT: collection attribute can be placed on MetaFields
        PlacementConstraint collectionPlacement = new PlacementConstraint(
            "codegen.collection.placement",
            "collection attribute can be placed on MetaFields to indicate collection type",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && ATTR_COLLECTION.equals(child.getName())
        );
        registry.addConstraint(collectionPlacement);
        
        // VALIDATION CONSTRAINT: collection must be boolean
        ValidationConstraint collectionValidation = new ValidationConstraint(
            "codegen.collection.validation",
            "collection must be a boolean value (true/false)",
            (metadata) -> metadata instanceof MetaAttribute && ATTR_COLLECTION.equals(metadata.getName()),
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
            (child) -> child instanceof MetaAttribute && ATTR_IS_SEARCHABLE.equals(child.getName())
        );
        registry.addConstraint(isSearchablePlacement);
        
        // VALIDATION CONSTRAINT: isSearchable must be boolean
        ValidationConstraint isSearchableValidation = new ValidationConstraint(
            "codegen.isSearchable.validation",
            "isSearchable must be a boolean value (true/false)",
            (metadata) -> metadata instanceof MetaAttribute && ATTR_IS_SEARCHABLE.equals(metadata.getName()),
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