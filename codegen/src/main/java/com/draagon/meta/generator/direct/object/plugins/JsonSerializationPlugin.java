package com.draagon.meta.generator.direct.object.plugins;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.generator.GeneratorIOWriter;
import com.draagon.meta.generator.direct.BaseGenerationContext;
import com.draagon.meta.generator.direct.GenerationContext;
import com.draagon.meta.generator.direct.GenerationPlugin;

/**
 * Plugin that adds JSON serialization annotations based on the selected library
 * Object-specific implementation for MetaObject code generation
 */
public class JsonSerializationPlugin implements GenerationPlugin {
    
    public enum JsonLibrary {
        JACKSON, GSON, JAKARTA_JSONB
    }
    
    private JsonLibrary library = JsonLibrary.JACKSON;
    private boolean addJsonPropertyAnnotations = true;
    private boolean addJsonIgnoreForSensitiveFields = true;
    private boolean addDateFormatting = true;
    private boolean applied = false;
    
    public JsonSerializationPlugin() {}
    
    public JsonSerializationPlugin useLibrary(JsonLibrary library) {
        this.library = library;
        return this;
    }
    
    public JsonSerializationPlugin addJsonPropertyAnnotations(boolean addAnnotations) {
        this.addJsonPropertyAnnotations = addAnnotations;
        return this;
    }
    
    public JsonSerializationPlugin addJsonIgnoreForSensitiveFields(boolean addIgnore) {
        this.addJsonIgnoreForSensitiveFields = addIgnore;
        return this;
    }
    
    public JsonSerializationPlugin addDateFormatting(boolean addFormatting) {
        this.addDateFormatting = addFormatting;
        return this;
    }
    
    @Override
    public void initialize(BaseGenerationContext<MetaObject> context) {
        context.setProperty("json.library", library);
        context.setProperty("json.addPropertyAnnotations", addJsonPropertyAnnotations);
        context.setProperty("json.ignoresSensitive", addJsonIgnoreForSensitiveFields);
        context.setProperty("json.formatDates", addDateFormatting);
        this.applied = true;
    }
    
    @Override
    public void beforeObjectGeneration(MetaObject object, GenerationContext context, GeneratorIOWriter<?> writer) {
        // Add imports based on selected library
        addJsonImports(context);
        
        // Add class-level annotations if needed
        addClassLevelAnnotations(object, context, writer);
    }
    
    @Override
    public void beforeFieldGeneration(MetaField field, GenerationContext context, GeneratorIOWriter<?> writer) {
        // Add field-level JSON annotations
        addFieldAnnotations(field, context, writer);
    }
    
    private void addJsonImports(GenerationContext context) {
        switch (library) {
            case JACKSON:
                if (addJsonPropertyAnnotations) {
                    context.addImport("com.fasterxml.jackson.annotation.JsonProperty");
                }
                if (addJsonIgnoreForSensitiveFields) {
                    context.addImport("com.fasterxml.jackson.annotation.JsonIgnore");
                }
                if (addDateFormatting) {
                    context.addImport("com.fasterxml.jackson.annotation.JsonFormat");
                }
                break;
                
            case GSON:
                if (addJsonPropertyAnnotations) {
                    context.addImport("com.google.gson.annotations.SerializedName");
                }
                if (addJsonIgnoreForSensitiveFields) {
                    context.addImport("com.google.gson.annotations.Expose");
                }
                break;
                
            case JAKARTA_JSONB:
                if (addJsonPropertyAnnotations) {
                    context.addImport("jakarta.json.bind.annotation.JsonbProperty");
                }
                if (addDateFormatting) {
                    context.addImport("jakarta.json.bind.annotation.JsonbDateFormat");
                }
                break;
        }
    }
    
    private void addClassLevelAnnotations(MetaObject object, GenerationContext context, GeneratorIOWriter<?> writer) {
        // Add class-level JSON annotations if needed
        // Most JSON libraries don't require class-level annotations for basic serialization
        // but some configurations might benefit from them
        
        if (library == JsonLibrary.GSON && addJsonIgnoreForSensitiveFields) {
            // GSON might use @Expose at class level for certain configurations
            // This would be implementation-specific
        }
    }
    
    private void addFieldAnnotations(MetaField field, GenerationContext context, GeneratorIOWriter<?> writer) {
        // Add field-specific annotations based on field characteristics
        
        if (addJsonPropertyAnnotations) {
            addPropertyAnnotation(field, context);
        }
        
        if (addJsonIgnoreForSensitiveFields && isSensitiveField(field)) {
            addIgnoreAnnotation(field, context);
        }
        
        if (addDateFormatting && isDateField(field)) {
            addDateFormatAnnotation(field, context);
        }
    }
    
    private void addPropertyAnnotation(MetaField field, GenerationContext context) {
        String fieldName = field.getName();
        // Convert camelCase to snake_case for JSON property names
        String jsonName = camelToSnake(fieldName);
        
        switch (library) {
            case JACKSON:
                // TODO: Add annotation through writer when API is available
                // writer.writeAnnotation("@JsonProperty(\"" + jsonName + "\")");
                break;
            case GSON:
                // TODO: Add annotation through writer when API is available
                // writer.writeAnnotation("@SerializedName(\"" + jsonName + "\")");
                break;
            case JAKARTA_JSONB:
                // TODO: Add annotation through writer when API is available
                // writer.writeAnnotation("@JsonbProperty(\"" + jsonName + "\")");
                break;
        }
    }
    
    private void addIgnoreAnnotation(MetaField field, GenerationContext context) {
        switch (library) {
            case JACKSON:
                // TODO: Add annotation through writer when API is available
                // writer.writeAnnotation("@JsonIgnore");
                break;
            case GSON:
                // TODO: Add annotation through writer when API is available
                // writer.writeAnnotation("@Expose(serialize = false, deserialize = false)");
                break;
            case JAKARTA_JSONB:
                // Jakarta JSON-B uses @JsonbTransient for ignoring
                context.addImport("jakarta.json.bind.annotation.JsonbTransient");
                // TODO: Add annotation through writer when API is available
                // writer.writeAnnotation("@JsonbTransient");
                break;
        }
    }
    
    private void addDateFormatAnnotation(MetaField field, GenerationContext context) {
        switch (library) {
            case JACKSON:
                // TODO: Add annotation through writer when API is available
                // writer.writeAnnotation("@JsonFormat(pattern = \"yyyy-MM-dd'T'HH:mm:ss.SSSZ\")");
                break;
            case JAKARTA_JSONB:
                // TODO: Add annotation through writer when API is available
                // writer.writeAnnotation("@JsonbDateFormat(\"yyyy-MM-dd'T'HH:mm:ss.SSSZ\")");
                break;
            // GSON handles dates differently, typically through custom deserializers
        }
    }
    
    private boolean isSensitiveField(MetaField field) {
        String fieldName = field.getName().toLowerCase();
        return fieldName.contains("password") || 
               fieldName.contains("secret") || 
               fieldName.contains("token") ||
               fieldName.contains("key");
    }
    
    private boolean isDateField(MetaField field) {
        String fieldType = field.getDataType().toString();
        return fieldType.contains("Date") || 
               fieldType.contains("LocalDate") || 
               fieldType.contains("LocalDateTime") ||
               fieldType.contains("Instant") ||
               fieldType.contains("ZonedDateTime");
    }
    
    private String camelToSnake(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
    
    @Override
    public String getName() {
        return "JsonSerializationPlugin";
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