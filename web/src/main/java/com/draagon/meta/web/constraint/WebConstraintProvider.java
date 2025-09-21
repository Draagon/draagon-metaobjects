package com.draagon.meta.web.constraint;

import com.draagon.meta.constraint.ConstraintProvider;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;

import java.util.List;

/**
 * Web module constraint provider for HTML form generation and web-specific attributes.
 * 
 * This provider defines constraints for web-specific attributes used in HTML form generation,
 * CSS styling, and user interface validation. These constraints are cross-cutting concerns
 * that apply to multiple MetaData types for web presentation purposes.
 */
public class WebConstraintProvider implements ConstraintProvider {
    
    @Override
    public void registerConstraints(ConstraintRegistry registry) {
        // HTML input type validation for form generation
        addHtmlInputTypeConstraints(registry);
        
        // CSS class and HTML ID validation
        addCssAndHtmlConstraints(registry);
        
        // Form label and UI text constraints
        addFormTextConstraints(registry);
        
        // Security constraints for web content
        addSecurityConstraints(registry);
    }
    
    private void addHtmlInputTypeConstraints(ConstraintRegistry registry) {
        // PLACEMENT CONSTRAINT: htmlInputType attribute can be placed on string fields
        PlacementConstraint htmlInputTypePlacement = new PlacementConstraint(
            "web.htmlInputType.placement",
            "htmlInputType attribute can be placed on string fields for form generation",
            (parent) -> parent instanceof MetaField && "string".equals(((MetaField) parent).getSubTypeName()),
            (child) -> child instanceof MetaAttribute && "htmlInputType".equals(child.getName())
        );
        registry.addConstraint(htmlInputTypePlacement);
        
        // VALIDATION CONSTRAINT: htmlInputType must be valid HTML input type
        ValidationConstraint htmlInputTypeValidation = new ValidationConstraint(
            "web.htmlInputType.validation",
            "htmlInputType must be a valid HTML input type",
            (metadata) -> metadata instanceof MetaAttribute && "htmlInputType".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true;
                String inputType = value.toString().toLowerCase();
                return isValidHtmlInputType(inputType);
            }
        );
        registry.addConstraint(htmlInputTypeValidation);
    }
    
    private void addCssAndHtmlConstraints(ConstraintRegistry registry) {
        // PLACEMENT CONSTRAINT: CSS class attributes
        PlacementConstraint cssClassPlacement = new PlacementConstraint(
            "web.cssClass.placement",
            "cssClass attribute can be placed on any MetaData for styling",
            (parent) -> parent instanceof MetaData,
            (child) -> child instanceof MetaAttribute && "cssClass".equals(child.getName())
        );
        registry.addConstraint(cssClassPlacement);
        
        // VALIDATION CONSTRAINT: CSS class names must follow valid pattern
        ValidationConstraint cssClassValidation = new ValidationConstraint(
            "web.cssClass.validation",
            "CSS class names must follow valid CSS identifier pattern",
            (metadata) -> metadata instanceof MetaAttribute && "cssClass".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true;
                String cssClass = value.toString();
                return cssClass.matches("^[a-zA-Z]([a-zA-Z0-9_-])*$") && cssClass.length() <= 50;
            }
        );
        registry.addConstraint(cssClassValidation);
        
        // PLACEMENT CONSTRAINT: HTML ID attributes
        PlacementConstraint htmlIdPlacement = new PlacementConstraint(
            "web.htmlId.placement",
            "htmlId attribute can be placed on any MetaData for DOM identification",
            (parent) -> parent instanceof MetaData,
            (child) -> child instanceof MetaAttribute && "htmlId".equals(child.getName())
        );
        registry.addConstraint(htmlIdPlacement);
        
        // VALIDATION CONSTRAINT: HTML ID must follow valid pattern
        ValidationConstraint htmlIdValidation = new ValidationConstraint(
            "web.htmlId.validation",
            "HTML ID must follow valid HTML identifier pattern",
            (metadata) -> metadata instanceof MetaAttribute && "htmlId".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true;
                String htmlId = value.toString();
                return htmlId.matches("^[a-zA-Z]([a-zA-Z0-9_-])*$");
            }
        );
        registry.addConstraint(htmlIdValidation);
    }
    
    private void addFormTextConstraints(ConstraintRegistry registry) {
        // PLACEMENT CONSTRAINT: Form label attributes
        PlacementConstraint formLabelPlacement = new PlacementConstraint(
            "web.formLabel.placement",
            "formLabel attribute can be placed on fields for form generation",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && "formLabel".equals(child.getName())
        );
        registry.addConstraint(formLabelPlacement);
        
        // VALIDATION CONSTRAINT: Form labels must be non-empty and within length limits
        ValidationConstraint formLabelValidation = new ValidationConstraint(
            "web.formLabel.validation",
            "Form labels must be non-empty and within 1-100 characters",
            (metadata) -> metadata instanceof MetaAttribute && "formLabel".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return false; // Required
                String label = value.toString().trim();
                return !label.isEmpty() && label.length() >= 1 && label.length() <= 100;
            }
        );
        registry.addConstraint(formLabelValidation);
        
        // PLACEMENT CONSTRAINT: Placeholder text attributes
        PlacementConstraint placeholderPlacement = new PlacementConstraint(
            "web.placeholder.placement",
            "placeholder attribute can be placed on string fields for input hints",
            (parent) -> parent instanceof MetaField && "string".equals(((MetaField) parent).getSubTypeName()),
            (child) -> child instanceof MetaAttribute && "placeholder".equals(child.getName())
        );
        registry.addConstraint(placeholderPlacement);
        
        // VALIDATION CONSTRAINT: Placeholder text length limits
        ValidationConstraint placeholderValidation = new ValidationConstraint(
            "web.placeholder.validation",
            "Placeholder text must be within 200 character limit",
            (metadata) -> metadata instanceof MetaAttribute && "placeholder".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true;
                return value.toString().length() <= 200;
            }
        );
        registry.addConstraint(placeholderValidation);
        
        // PLACEMENT CONSTRAINT: Validation message attributes
        PlacementConstraint validationMessagePlacement = new PlacementConstraint(
            "web.validationMessage.placement",
            "validationMessage attribute can be placed on any MetaData for error display",
            (parent) -> parent instanceof MetaData,
            (child) -> child instanceof MetaAttribute && "validationMessage".equals(child.getName())
        );
        registry.addConstraint(validationMessagePlacement);
        
        // VALIDATION CONSTRAINT: Validation message length limits
        ValidationConstraint validationMessageValidation = new ValidationConstraint(
            "web.validationMessage.validation",
            "Validation messages must be within 500 character limit",
            (metadata) -> metadata instanceof MetaAttribute && "validationMessage".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true;
                return value.toString().length() <= 500;
            }
        );
        registry.addConstraint(validationMessageValidation);
        
        // PLACEMENT CONSTRAINT: Help text attributes
        PlacementConstraint helpTextPlacement = new PlacementConstraint(
            "web.helpText.placement",
            "helpText attribute can be placed on any MetaData for user guidance",
            (parent) -> parent instanceof MetaData,
            (child) -> child instanceof MetaAttribute && "helpText".equals(child.getName())
        );
        registry.addConstraint(helpTextPlacement);
        
        // VALIDATION CONSTRAINT: Help text length limits
        ValidationConstraint helpTextValidation = new ValidationConstraint(
            "web.helpText.validation",
            "Help text must be within 1000 character limit",
            (metadata) -> metadata instanceof MetaAttribute && "helpText".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true;
                return value.toString().length() <= 1000;
            }
        );
        registry.addConstraint(helpTextValidation);
    }
    
    private void addSecurityConstraints(ConstraintRegistry registry) {
        // VALIDATION CONSTRAINT: String fields should not contain script tags (XSS prevention)
        ValidationConstraint xssValidation = new ValidationConstraint(
            "web.xss.validation",
            "String fields should not contain script tags for security",
            (metadata) -> metadata instanceof MetaField && "string".equals(((MetaField) metadata).getSubTypeName()),
            (metadata, value) -> {
                if (value == null) return true;
                String stringValue = value.toString().toLowerCase();
                return !stringValue.contains("<script");
            }
        );
        registry.addConstraint(xssValidation);
    }
    
    private boolean isValidHtmlInputType(String inputType) {
        return List.of(
            "text", "password", "email", "url", "tel", "search",
            "number", "range", "date", "datetime-local", "time", "month", "week",
            "color", "file", "image", "hidden", "checkbox", "radio", "submit", "button", "reset"
        ).contains(inputType);
    }
    
    @Override
    public int getPriority() {
        return 1000; // Lower priority than core constraints
    }
    
    @Override
    public String getDescription() {
        return "Web module constraints for HTML form generation and web-specific attributes";
    }
}