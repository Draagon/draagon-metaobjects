package com.draagon.meta.generator.direct;

import java.util.Map;
import java.util.HashMap;

/**
 * Represents a configurable code fragment that can contain variables
 */
public class CodeFragment {
    
    private final String template;
    private final Map<String, Object> defaultValues;
    
    public CodeFragment(String template) {
        this(template, new HashMap<>());
    }
    
    public CodeFragment(String template, Map<String, Object> defaultValues) {
        this.template = template;
        this.defaultValues = new HashMap<>(defaultValues);
    }
    
    /**
     * Generate code using the provided context
     */
    public String generate(GenerationContext context) {
        return generate(context, new HashMap<>());
    }
    
    /**
     * Generate code using the provided context and additional variables
     */
    public String generate(GenerationContext context, Map<String, Object> additionalVars) {
        String result = template;
        
        // Apply context resolution first
        result = context.resolveVariables(result);
        
        // Apply default values
        for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            result = result.replace(placeholder, String.valueOf(entry.getValue()));
        }
        
        // Apply additional variables
        for (Map.Entry<String, Object> entry : additionalVars.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            result = result.replace(placeholder, String.valueOf(entry.getValue()));
        }
        
        return result;
    }
    
    /**
     * Create a new fragment with additional default values
     */
    public CodeFragment withDefaults(Map<String, Object> additionalDefaults) {
        Map<String, Object> merged = new HashMap<>(this.defaultValues);
        merged.putAll(additionalDefaults);
        return new CodeFragment(this.template, merged);
    }
    
    /**
     * Create a new fragment with a single default value
     */
    public CodeFragment withDefault(String key, Object value) {
        Map<String, Object> newDefaults = new HashMap<>(this.defaultValues);
        newDefaults.put(key, value);
        return new CodeFragment(this.template, newDefaults);
    }
    
    public String getTemplate() {
        return template;
    }
    
    public Map<String, Object> getDefaultValues() {
        return new HashMap<>(defaultValues);
    }
    
    @Override
    public String toString() {
        return "CodeFragment{template='" + template + "', defaults=" + defaultValues + "}";
    }
}