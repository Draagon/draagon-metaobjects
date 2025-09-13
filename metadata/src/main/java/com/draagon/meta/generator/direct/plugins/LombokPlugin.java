package com.draagon.meta.generator.direct.plugins;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.generator.GeneratorIOWriter;
import com.draagon.meta.generator.direct.GenerationContext;
import com.draagon.meta.generator.direct.GenerationPlugin;
import com.draagon.meta.generator.direct.FileDirectWriter;

import java.util.Set;
import java.util.HashSet;

/**
 * Plugin that adds Lombok annotations to reduce boilerplate code
 */
public class LombokPlugin implements GenerationPlugin {
    
    private final Set<String> enabledFeatures = new HashSet<>();
    
    public LombokPlugin() {
        // Default features
        enabledFeatures.add("getter");
        enabledFeatures.add("setter");
        enabledFeatures.add("toString");
        enabledFeatures.add("equalsAndHashCode");
    }
    
    public LombokPlugin enableFeature(String feature) {
        enabledFeatures.add(feature);
        return this;
    }
    
    public LombokPlugin disableFeature(String feature) {
        enabledFeatures.remove(feature);
        return this;
    }
    
    public LombokPlugin withBuilder() {
        enabledFeatures.add("builder");
        return this;
    }
    
    public LombokPlugin withAllArgsConstructor() {
        enabledFeatures.add("allArgsConstructor");
        return this;
    }
    
    public LombokPlugin withNoArgsConstructor() {
        enabledFeatures.add("noArgsConstructor");
        return this;
    }
    
    @Override
    public void initialize(GenerationContext context) {
        context.setProperty("lombok.enabled", true);
        context.setProperty("lombok.features", enabledFeatures);
        
        // When Lombok is enabled, we don't want to generate getters/setters manually
        context.setProperty("generate.getters", !enabledFeatures.contains("getter"));
        context.setProperty("generate.setters", !enabledFeatures.contains("setter"));
        context.setProperty("generate.toString", !enabledFeatures.contains("toString"));
        context.setProperty("generate.equalsHashCode", !enabledFeatures.contains("equalsAndHashCode"));
    }
    
    @Override
    public void contributeImports(MetaObject object, GenerationContext context) {
        if (enabledFeatures.contains("getter") || enabledFeatures.contains("setter")) {
            context.addImport("lombok.Getter");
            context.addImport("lombok.Setter");
        }
        
        if (enabledFeatures.contains("toString")) {
            context.addImport("lombok.ToString");
        }
        
        if (enabledFeatures.contains("equalsAndHashCode")) {
            context.addImport("lombok.EqualsAndHashCode");
        }
        
        if (enabledFeatures.contains("builder")) {
            context.addImport("lombok.Builder");
        }
        
        if (enabledFeatures.contains("allArgsConstructor")) {
            context.addImport("lombok.AllArgsConstructor");
        }
        
        if (enabledFeatures.contains("noArgsConstructor")) {
            context.addImport("lombok.NoArgsConstructor");
        }
    }
    
    @Override
    public void beforeObjectGeneration(MetaObject object, GenerationContext context, GeneratorIOWriter<?> writer) {
        if (!(writer instanceof FileDirectWriter)) return;
        
        FileDirectWriter<?> fileWriter = (FileDirectWriter<?>) writer;
        
        // Add class-level Lombok annotations
        addClassAnnotations(fileWriter, context);
    }
    
    private void addClassAnnotations(FileDirectWriter<?> writer, GenerationContext context) {
        try {
            java.lang.reflect.Method println = writer.getClass().getDeclaredMethod("println", boolean.class, String.class);
            println.setAccessible(true);
            
            if (enabledFeatures.contains("getter")) {
                println.invoke(writer, true, "@Getter");
            }
            
            if (enabledFeatures.contains("setter")) {
                println.invoke(writer, true, "@Setter");
            }
            
            if (enabledFeatures.contains("toString")) {
                println.invoke(writer, true, "@ToString");
            }
            
            if (enabledFeatures.contains("equalsAndHashCode")) {
                println.invoke(writer, true, "@EqualsAndHashCode");
            }
            
            if (enabledFeatures.contains("builder")) {
                println.invoke(writer, true, "@Builder");
            }
            
            if (enabledFeatures.contains("allArgsConstructor")) {
                println.invoke(writer, true, "@AllArgsConstructor");
            }
            
            if (enabledFeatures.contains("noArgsConstructor")) {
                println.invoke(writer, true, "@NoArgsConstructor");
            }
            
        } catch (Exception e) {
            // Fallback - store in context for later use
            context.setProperty("lombok.annotations.added", enabledFeatures);
        }
    }
    
    @Override
    public String customizeMethodName(MetaField field, String methodType, String defaultName, GenerationContext context) {
        // If Lombok is generating methods, we might want to skip them entirely
        // This would be handled by the generator checking the context properties
        return defaultName;
    }
    
    @Override
    public String getName() {
        return "LombokPlugin";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
}