package com.draagon.meta.generator.direct.object.plugins;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.generator.GeneratorIOWriter;
import com.draagon.meta.generator.direct.BaseGenerationContext;
import com.draagon.meta.generator.direct.GenerationContext;
import com.draagon.meta.generator.direct.GenerationPlugin;
import com.draagon.meta.validator.MetaValidator;

/**
 * Plugin that adds validation annotations based on MetaField properties
 * Object-specific implementation for MetaObject code generation
 */
public class ValidationPlugin implements GenerationPlugin {
    
    private boolean useJakartaValidation = true;
    private boolean addNotNullForRequired = true;
    private boolean addSizeForStringFields = true;
    private boolean applied = false;
    
    public ValidationPlugin() {}
    
    public ValidationPlugin useJakartaValidation(boolean useJakarta) {
        this.useJakartaValidation = useJakarta;
        return this;
    }
    
    public ValidationPlugin addNotNullForRequired(boolean addNotNull) {
        this.addNotNullForRequired = addNotNull;
        return this;
    }
    
    public ValidationPlugin addSizeForStringFields(boolean addSize) {
        this.addSizeForStringFields = addSize;
        return this;
    }
    
    @Override
    public void initialize(BaseGenerationContext<MetaObject> context) {
        // Configure context properties for validation
        context.setProperty("validation.useJakarta", useJakartaValidation);
        context.setProperty("validation.addNotNull", addNotNullForRequired);
        context.setProperty("validation.addSize", addSizeForStringFields);
        this.applied = true;
    }
    
    @Override
    public void beforeObjectGeneration(MetaObject object, GenerationContext context, GeneratorIOWriter<?> writer) {
        // Add validation imports
        if (useJakartaValidation) {
            context.addImport("jakarta.validation.Valid");
            context.addImport("jakarta.validation.constraints.*");
        } else {
            context.addImport("javax.validation.Valid");
            context.addImport("javax.validation.constraints.*");
        }
        
        // Check for field validators and add appropriate imports
        for (MetaField field : object.getMetaFields()) {
            addValidationImportsForField(field, context);
        }
    }
    
    @Override
    public void beforeFieldGeneration(MetaField field, GenerationContext context, GeneratorIOWriter<?> writer) {
        // Add field-level validation annotations
        addValidationAnnotationsForField(field, context, writer);
    }
    
    @Override
    public String customizeFieldType(MetaField field, String defaultType, GenerationContext context) {
        // No field type customization for validation
        return defaultType;
    }
    
    private void addValidationImportsForField(MetaField field, GenerationContext context) {
        // Add imports based on field characteristics
        if (isRequired(field) && addNotNullForRequired) {
            // @NotNull import already added in beforeObjectGeneration
        }
        
        if (isStringField(field) && addSizeForStringFields) {
            // @Size import already added in beforeObjectGeneration
        }
        
        // Check for MetaValidator instances on field
        // TODO: Implement field validator checking when API is available
    }
    
    private void addValidationAnnotationsForField(MetaField field, GenerationContext context, GeneratorIOWriter<?> writer) {
        // Add @NotNull for required fields
        if (isRequired(field) && addNotNullForRequired) {
            // TODO: Add annotation through writer when API is available
            // writer.writeAnnotation("@NotNull");
        }
        
        // Add @Size for string fields
        if (isStringField(field) && addSizeForStringFields) {
            int maxLength = getFieldMaxLength(field);
            if (maxLength > 0) {
                // TODO: Add annotation through writer when API is available
                // writer.writeAnnotation("@Size(max = " + maxLength + ")");
            }
        }
        
        // Add custom validation annotations based on MetaValidators
        // TODO: Implement when field validator API is available
    }
    
    private boolean isRequired(MetaField field) {
        // TODO: Implement proper required field detection
        // This would check field attributes or validators
        return false;
    }
    
    private boolean isStringField(MetaField field) {
        return "String".equals(field.getDataType().toString()) || 
               "java.lang.String".equals(field.getDataType().toString());
    }
    
    private int getFieldMaxLength(MetaField field) {
        // TODO: Implement proper max length detection from field attributes
        // This would check for size constraints in field definition
        return -1;
    }
    
    @Override
    public String getName() {
        return "ValidationPlugin";
    }
    
    @Override
    public String getVersion() {
        return "2.0.0";
    }
    
    /**
     * Check if this plugin has been applied (for testing)
     */
    public boolean wasApplied() {
        return applied;
    }
}