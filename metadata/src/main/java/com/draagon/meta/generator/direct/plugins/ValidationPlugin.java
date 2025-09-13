package com.draagon.meta.generator.direct.plugins;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.generator.GeneratorIOWriter;
import com.draagon.meta.generator.direct.GenerationContext;
import com.draagon.meta.generator.direct.GenerationPlugin;
import com.draagon.meta.generator.direct.FileDirectWriter;
import com.draagon.meta.validator.MetaValidator;

/**
 * Plugin that adds validation annotations based on MetaField properties
 */
public class ValidationPlugin implements GenerationPlugin {
    
    private boolean useJakartaValidation = true;
    private boolean addNotNullForRequired = true;
    private boolean addSizeForStringFields = true;
    
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
    public void initialize(GenerationContext context) {
        // Configure context properties for validation
        context.setProperty("validation.useJakarta", useJakartaValidation);
        context.setProperty("validation.addNotNull", addNotNullForRequired);
        context.setProperty("validation.addSize", addSizeForStringFields);
    }
    
    @Override
    public void contributeImports(MetaObject object, GenerationContext context) {
        String validationPackage = useJakartaValidation ? "jakarta.validation" : "javax.validation";
        
        // Check if we need validation imports
        boolean needsValidation = object.getMetaFields(false).stream()
            .anyMatch(this::shouldAddValidation);
            
        if (needsValidation) {
            context.addImport(validationPackage + ".constraints.*");
            context.addImport(validationPackage + ".Valid");
        }
    }
    
    @Override
    public void beforeFieldGeneration(MetaField field, GenerationContext context, GeneratorIOWriter<?> writer) {
        if (!(writer instanceof FileDirectWriter)) return;
        
        FileDirectWriter<?> fileWriter = (FileDirectWriter<?>) writer;
        
        if (shouldAddValidation(field)) {
            addValidationAnnotations(field, fileWriter, context);
        }
    }
    
    private boolean shouldAddValidation(MetaField field) {
        // Add validation if field is required or has length constraints
        return isFieldRequired(field) || 
               (field.hasMetaAttr("length") && addSizeForStringFields) ||
               field.hasMetaAttr("min") || 
               field.hasMetaAttr("max");
    }
    
    private void addValidationAnnotations(MetaField field, FileDirectWriter<?> writer, GenerationContext context) {
        StringBuilder annotations = new StringBuilder();
        
        // @NotNull for required fields
        if (isFieldRequired(field) && addNotNullForRequired) {
            annotations.append("@NotNull\n");
        }
        
        // @Size for string fields with length
        if (field.hasMetaAttr("length") && addSizeForStringFields) {
            try {
                int length = Integer.parseInt(field.getMetaAttr("length").getValueAsString());
                annotations.append(String.format("@Size(max = %d)\n", length));
            } catch (NumberFormatException e) {
                // Ignore if length is not a valid number
            }
        }
        
        // @Min/@Max for numeric fields
        if (field.hasMetaAttr("min")) {
            try {
                int min = Integer.parseInt(field.getMetaAttr("min").getValueAsString());
                annotations.append(String.format("@Min(%d)\n", min));
            } catch (NumberFormatException e) {
                // Ignore if min is not a valid number
            }
        }
        
        if (field.hasMetaAttr("max")) {
            try {
                int max = Integer.parseInt(field.getMetaAttr("max").getValueAsString());
                annotations.append(String.format("@Max(%d)\n", max));
            } catch (NumberFormatException e) {
                // Ignore if max is not a valid number
            }
        }
        
        // Write annotations if any were added
        if (annotations.length() > 0) {
            // Use reflection to access the println method - this is a simplification
            // In a real implementation, you'd want a better way to add annotations
            try {
                java.lang.reflect.Method println = writer.getClass().getDeclaredMethod("println", boolean.class, String.class);
                println.setAccessible(true);
                
                // Add each annotation line
                for (String line : annotations.toString().split("\n")) {
                    if (!line.trim().isEmpty()) {
                        println.invoke(writer, true, line.trim());
                    }
                }
            } catch (Exception e) {
                // Fallback - just log that we wanted to add validation
                context.setProperty("validation.wanted." + field.getName(), annotations.toString());
            }
        }
    }
    
    @Override
    public String getName() {
        return "ValidationPlugin";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    /**
     * Check if a field is required by looking for a 'required' validator
     */
    private boolean isFieldRequired(MetaField field) {
        try {
            // Check if field has a 'required' validator
            for (Object obj : field.getValidators()) {
                if (obj instanceof MetaValidator) {
                    MetaValidator validator = (MetaValidator) obj;
                    if ("required".equals(validator.getSubTypeName())) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}