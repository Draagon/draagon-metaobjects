package com.draagon.meta.generator.direct.plugins;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.generator.GeneratorIOWriter;
import com.draagon.meta.generator.direct.GenerationContext;
import com.draagon.meta.generator.direct.GenerationPlugin;
import com.draagon.meta.generator.direct.FileDirectWriter;

/**
 * Plugin that adds JSON serialization annotations (Jackson, Gson, etc.)
 */
public class JsonSerializationPlugin implements GenerationPlugin {
    
    public enum JsonLibrary {
        JACKSON, GSON, JSONB
    }
    
    private JsonLibrary library = JsonLibrary.JACKSON;
    private boolean addJsonPropertyAnnotations = true;
    private boolean addJsonIgnoreForSensitiveFields = true;
    private boolean addDateFormatting = true;
    
    public JsonSerializationPlugin() {}
    
    public JsonSerializationPlugin useLibrary(JsonLibrary library) {
        this.library = library;
        return this;
    }
    
    public JsonSerializationPlugin addJsonPropertyAnnotations(boolean add) {
        this.addJsonPropertyAnnotations = add;
        return this;
    }
    
    public JsonSerializationPlugin addJsonIgnoreForSensitiveFields(boolean add) {
        this.addJsonIgnoreForSensitiveFields = add;
        return this;
    }
    
    public JsonSerializationPlugin addDateFormatting(boolean add) {
        this.addDateFormatting = add;
        return this;
    }
    
    @Override
    public void initialize(GenerationContext context) {
        context.setProperty("json.library", library);
        context.setProperty("json.addPropertyAnnotations", addJsonPropertyAnnotations);
        context.setProperty("json.ignoresSensitive", addJsonIgnoreForSensitiveFields);
        context.setProperty("json.formatDates", addDateFormatting);
    }
    
    @Override
    public void contributeImports(MetaObject object, GenerationContext context) {
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
                
            case JSONB:
                if (addJsonPropertyAnnotations) {
                    context.addImport("jakarta.json.bind.annotation.JsonbProperty");
                }
                if (addJsonIgnoreForSensitiveFields) {
                    context.addImport("jakarta.json.bind.annotation.JsonbTransient");
                }
                if (addDateFormatting) {
                    context.addImport("jakarta.json.bind.annotation.JsonbDateFormat");
                }
                break;
        }
    }
    
    @Override
    public void beforeFieldGeneration(MetaField field, GenerationContext context, GeneratorIOWriter<?> writer) {
        if (!(writer instanceof FileDirectWriter)) return;
        
        FileDirectWriter<?> fileWriter = (FileDirectWriter<?>) writer;
        
        addJsonAnnotations(field, fileWriter, context);
    }
    
    private void addJsonAnnotations(MetaField field, FileDirectWriter<?> writer, GenerationContext context) {
        try {
            java.lang.reflect.Method println = writer.getClass().getDeclaredMethod("println", boolean.class, String.class);
            println.setAccessible(true);
            
            // Check for sensitive fields (password, secret, etc.)
            boolean isSensitive = isSensitiveField(field);
            
            if (isSensitive && addJsonIgnoreForSensitiveFields) {
                addIgnoreAnnotation(println, writer, context);
            } else {
                if (addJsonPropertyAnnotations) {
                    addPropertyAnnotation(field, println, writer, context);
                }
                
                if (addDateFormatting && isDateField(field)) {
                    addDateFormatAnnotation(field, println, writer, context);
                }
            }
            
        } catch (Exception e) {
            // Fallback - store annotation info in context
            context.setProperty("json.field." + field.getName() + ".annotations", getAnnotationsForField(field));
        }
    }
    
    private boolean isSensitiveField(MetaField field) {
        String name = field.getName().toLowerCase();
        return name.contains("password") || 
               name.contains("secret") || 
               name.contains("token") ||
               name.contains("key") ||
               field.hasMetaAttr("sensitive") ||
               field.hasMetaAttr("pii");
    }
    
    private boolean isDateField(MetaField field) {
        return field.getDataType().toString().toLowerCase().contains("date") ||
               field.getDataType().toString().toLowerCase().contains("time");
    }
    
    private void addIgnoreAnnotation(java.lang.reflect.Method println, FileDirectWriter<?> writer, GenerationContext context) throws Exception {
        switch (library) {
            case JACKSON:
                println.invoke(writer, true, "@JsonIgnore");
                break;
            case GSON:
                println.invoke(writer, true, "@Expose(serialize = false, deserialize = false)");
                break;
            case JSONB:
                println.invoke(writer, true, "@JsonbTransient");
                break;
        }
    }
    
    private void addPropertyAnnotation(MetaField field, java.lang.reflect.Method println, FileDirectWriter<?> writer, GenerationContext context) throws Exception {
        String jsonName = field.hasMetaAttr("jsonName") ? 
            field.getMetaAttr("jsonName").getValueAsString() : 
            field.getName();
            
        switch (library) {
            case JACKSON:
                println.invoke(writer, true, "@JsonProperty(\"" + jsonName + "\")");
                break;
            case GSON:
                println.invoke(writer, true, "@SerializedName(\"" + jsonName + "\")");
                break;
            case JSONB:
                println.invoke(writer, true, "@JsonbProperty(\"" + jsonName + "\")");
                break;
        }
    }
    
    private void addDateFormatAnnotation(MetaField field, java.lang.reflect.Method println, FileDirectWriter<?> writer, GenerationContext context) throws Exception {
        String dateFormat = field.hasMetaAttr("dateFormat") ? 
            field.getMetaAttr("dateFormat").getValueAsString() : 
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
            
        switch (library) {
            case JACKSON:
                println.invoke(writer, true, "@JsonFormat(pattern = \"" + dateFormat + "\")");
                break;
            case JSONB:
                println.invoke(writer, true, "@JsonbDateFormat(\"" + dateFormat + "\")");
                break;
            // GSON doesn't have a standard date format annotation
        }
    }
    
    private String getAnnotationsForField(MetaField field) {
        StringBuilder annotations = new StringBuilder();
        
        if (isSensitiveField(field) && addJsonIgnoreForSensitiveFields) {
            switch (library) {
                case JACKSON: annotations.append("@JsonIgnore\n"); break;
                case GSON: annotations.append("@Expose(serialize = false, deserialize = false)\n"); break;
                case JSONB: annotations.append("@JsonbTransient\n"); break;
            }
        } else {
            if (addJsonPropertyAnnotations) {
                String jsonName = field.hasMetaAttr("jsonName") ? 
                    field.getMetaAttr("jsonName").getValueAsString() : 
                    field.getName();
                    
                switch (library) {
                    case JACKSON: annotations.append("@JsonProperty(\"").append(jsonName).append("\")\n"); break;
                    case GSON: annotations.append("@SerializedName(\"").append(jsonName).append("\")\n"); break;
                    case JSONB: annotations.append("@JsonbProperty(\"").append(jsonName).append("\")\n"); break;
                }
            }
        }
        
        return annotations.toString();
    }
    
    @Override
    public String getName() {
        return "JsonSerializationPlugin";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
}