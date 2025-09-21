package com.draagon.meta.io.constraint;

import com.draagon.meta.constraint.ConstraintProvider;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;

/**
 * Core IO constraint provider for XML serialization and data exchange attributes.
 * 
 * This provider defines constraints for attributes used by XML readers/writers,
 * JSON serialization, and other IO operations. These constraints are cross-cutting concerns
 * that apply to multiple MetaData types for data serialization purposes.
 */
public class CoreIOConstraintProvider implements ConstraintProvider {
    
    @Override
    public void registerConstraints(ConstraintRegistry registry) {
        // XML name mapping attributes
        addXmlNamingConstraints(registry);
        
        // XML behavior control attributes
        addXmlBehaviorConstraints(registry);
    }
    
    private void addXmlNamingConstraints(ConstraintRegistry registry) {
        // PLACEMENT CONSTRAINT: xmlName attribute can be placed on any MetaData
        PlacementConstraint xmlNamePlacement = new PlacementConstraint(
            "coreio.xmlName.placement",
            "xmlName attribute can be placed on any MetaData for XML element naming",
            (parent) -> parent instanceof MetaData,
            (child) -> child instanceof MetaAttribute && "xmlName".equals(child.getName())
        );
        registry.addConstraint(xmlNamePlacement);
        
        // VALIDATION CONSTRAINT: xmlName must be valid XML identifier
        ValidationConstraint xmlNameValidation = new ValidationConstraint(
            "coreio.xmlName.validation",
            "xmlName must be a valid XML element name",
            (metadata) -> metadata instanceof MetaAttribute && "xmlName".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String xmlName = value.toString().trim();
                // XML name pattern: must start with letter/underscore, followed by letters/digits/hyphens/underscores/periods
                return xmlName.matches("^[a-zA-Z_][a-zA-Z0-9_.-]*$") && xmlName.length() >= 1 && xmlName.length() <= 100;
            }
        );
        registry.addConstraint(xmlNameValidation);
    }
    
    private void addXmlBehaviorConstraints(ConstraintRegistry registry) {
        // PLACEMENT CONSTRAINT: xmlTyped attribute can be placed on MetaObjects
        PlacementConstraint xmlTypedPlacement = new PlacementConstraint(
            "coreio.xmlTyped.placement",
            "xmlTyped attribute can be placed on MetaObjects for type information in XML",
            (parent) -> parent instanceof MetaObject,
            (child) -> child instanceof MetaAttribute && "xmlTyped".equals(child.getName())
        );
        registry.addConstraint(xmlTypedPlacement);
        
        // VALIDATION CONSTRAINT: xmlTyped must be boolean
        ValidationConstraint xmlTypedValidation = new ValidationConstraint(
            "coreio.xmlTyped.validation",
            "xmlTyped must be a boolean value (true/false)",
            (metadata) -> metadata instanceof MetaAttribute && "xmlTyped".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        );
        registry.addConstraint(xmlTypedValidation);
        
        // PLACEMENT CONSTRAINT: xmlWrap attribute can be placed on MetaFields
        PlacementConstraint xmlWrapPlacement = new PlacementConstraint(
            "coreio.xmlWrap.placement",
            "xmlWrap attribute can be placed on MetaFields for XML wrapping behavior",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && "xmlWrap".equals(child.getName())
        );
        registry.addConstraint(xmlWrapPlacement);
        
        // VALIDATION CONSTRAINT: xmlWrap must be boolean
        ValidationConstraint xmlWrapValidation = new ValidationConstraint(
            "coreio.xmlWrap.validation",
            "xmlWrap must be a boolean value (true/false)",
            (metadata) -> metadata instanceof MetaAttribute && "xmlWrap".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        );
        registry.addConstraint(xmlWrapValidation);
        
        // PLACEMENT CONSTRAINT: xmlIgnore attribute can be placed on MetaFields
        PlacementConstraint xmlIgnorePlacement = new PlacementConstraint(
            "coreio.xmlIgnore.placement",
            "xmlIgnore attribute can be placed on MetaFields to exclude from XML serialization",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && "xmlIgnore".equals(child.getName())
        );
        registry.addConstraint(xmlIgnorePlacement);
        
        // VALIDATION CONSTRAINT: xmlIgnore must be boolean
        ValidationConstraint xmlIgnoreValidation = new ValidationConstraint(
            "coreio.xmlIgnore.validation",
            "xmlIgnore must be a boolean value (true/false)",
            (metadata) -> metadata instanceof MetaAttribute && "xmlIgnore".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        );
        registry.addConstraint(xmlIgnoreValidation);
    }
    
    @Override
    public int getPriority() {
        return 1300; // Lower priority than other constraint providers
    }
    
    @Override
    public String getDescription() {
        return "Core IO constraints for XML serialization and data exchange attributes";
    }
}