package com.draagon.meta.generator.hybrid;

import com.draagon.meta.generator.direct.GenerationContext;
import com.draagon.meta.generator.util.GeneratorUtil;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.loader.MetaDataLoader;

import java.util.*;

/**
 * Extended context for hybrid generators that includes script-specific functionality
 */
public class ScriptContext extends GenerationContext {
    
    private final Map<String, Object> scriptVariables = new HashMap<>();
    private final Map<String, ScriptFunction> scriptFunctions = new HashMap<>();
    private final Map<String, Object> scriptBindings = new HashMap<>();
    
    public ScriptContext(MetaDataLoader loader) {
        super(loader);
        initializeScriptUtilities();
    }
    
    public ScriptContext(GenerationContext baseContext) {
        super(baseContext.getLoader());
        
        // Copy all properties and state from base context
        if (baseContext.getCurrentObject() != null) {
            setCurrentObject(baseContext.getCurrentObject());
        }
        if (baseContext.getCurrentField() != null) {
            setCurrentField(baseContext.getCurrentField());
        }
        if (baseContext.getCurrentPackage() != null) {
            setCurrentPackage(baseContext.getCurrentPackage());
        }
        if (baseContext.getCurrentClassName() != null) {
            setCurrentClassName(baseContext.getCurrentClassName());
        }
        
        baseContext.getImports().forEach(this::addImport);
        baseContext.getPlugins().forEach(this::addPlugin);
        
        initializeScriptUtilities();
    }
    
    // Script variable management
    public ScriptContext setScriptVariable(String name, Object value) {
        scriptVariables.put(name, value);
        return this;
    }
    
    public <T> T getScriptVariable(String name) {
        return (T) scriptVariables.get(name);
    }
    
    public <T> T getScriptVariable(String name, T defaultValue) {
        return (T) scriptVariables.getOrDefault(name, defaultValue);
    }
    
    public boolean hasScriptVariable(String name) {
        return scriptVariables.containsKey(name);
    }
    
    // Script function management
    public ScriptContext addScriptFunction(String name, ScriptFunction function) {
        scriptFunctions.put(name, function);
        return this;
    }
    
    public ScriptFunction getScriptFunction(String name) {
        return scriptFunctions.get(name);
    }
    
    public boolean hasScriptFunction(String name) {
        return scriptFunctions.containsKey(name);
    }
    
    // Script binding management for Groovy
    public ScriptContext setBinding(String name, Object value) {
        scriptBindings.put(name, value);
        return this;
    }
    
    public Object getBinding(String name) {
        return scriptBindings.get(name);
    }
    
    public Map<String, Object> getAllBindings() {
        return new HashMap<>(scriptBindings);
    }
    
    // Enhanced variable resolution with script variables
    @Override
    public String resolveVariables(String template) {
        if (template == null) return null;
        
        // First apply base resolution
        String result = super.resolveVariables(template);
        
        // Then apply script variables
        for (Map.Entry<String, Object> entry : scriptVariables.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, String.valueOf(entry.getValue()));
            }
        }
        
        // Apply script bindings
        for (Map.Entry<String, Object> entry : scriptBindings.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, String.valueOf(entry.getValue()));
            }
        }
        
        return result;
    }
    
    // Utility methods for scripts
    public String toCamelCase(String input, boolean capitalize) {
        return GeneratorUtil.toCamelCase(input, capitalize);
    }
    
    public String toSnakeCase(String input) {
        return input.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
    
    public String toKebabCase(String input) {
        return input.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
    }
    
    public String capitalizeFirst(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
    
    public String pluralize(String input) {
        if (input == null || input.isEmpty()) return input;
        // Simple pluralization rules - could be enhanced
        if (input.endsWith("y")) {
            return input.substring(0, input.length() - 1) + "ies";
        } else if (input.endsWith("s") || input.endsWith("x") || input.endsWith("z") ||
                   input.endsWith("ch") || input.endsWith("sh")) {
            return input + "es";
        } else {
            return input + "s";
        }
    }
    
    // Field utility methods for scripts
    public boolean isStringField(MetaField field) {
        return field.getDataType().toString().toLowerCase().contains("string");
    }
    
    public boolean isNumericField(MetaField field) {
        String type = field.getDataType().toString().toLowerCase();
        return type.contains("int") || type.contains("long") || 
               type.contains("float") || type.contains("double") ||
               type.contains("decimal") || type.contains("number");
    }
    
    public boolean isBooleanField(MetaField field) {
        return field.getDataType().toString().toLowerCase().contains("bool");
    }
    
    public boolean isDateField(MetaField field) {
        String type = field.getDataType().toString().toLowerCase();
        return type.contains("date") || type.contains("time");
    }
    
    public boolean isCollectionField(MetaField field) {
        return field.getDataType().isArray() || 
               field.getDataType().toString().toLowerCase().contains("list") ||
               field.getDataType().toString().toLowerCase().contains("set");
    }
    
    public String getJavaType(MetaField field) {
        switch(field.getDataType()) {
            case BOOLEAN: return "Boolean";
            case BYTE: return "Byte";
            case SHORT: return "Short";
            case INT: return "Integer";
            case LONG: return "Long";
            case FLOAT: return "Float";
            case DOUBLE: return "Double";
            case DATE: return "java.util.Date";
            case STRING: return "String";
            case STRING_ARRAY: return "java.util.List<String>";
            case OBJECT: return "Object";
            case OBJECT_ARRAY: return "java.util.List<Object>";
            default: return "Object";
        }
    }
    
    private void initializeScriptUtilities() {
        // Add common script utilities
        setScriptVariable("utils", this);
        setScriptVariable("StringUtils", new StringUtilities());
        setScriptVariable("CollectionUtils", new CollectionUtilities());
        
        // Set up common bindings
        setBinding("context", this);
        setBinding("logger", org.slf4j.LoggerFactory.getLogger(ScriptContext.class));
    }
    
    // Inner utility classes for scripts
    public static class StringUtilities {
        public boolean isEmpty(String str) {
            return str == null || str.trim().isEmpty();
        }
        
        public boolean isNotEmpty(String str) {
            return !isEmpty(str);
        }
        
        public String defaultIfEmpty(String str, String defaultValue) {
            return isEmpty(str) ? defaultValue : str;
        }
        
        public String join(Collection<String> items, String separator) {
            return String.join(separator, items);
        }
    }
    
    public static class CollectionUtilities {
        public boolean isEmpty(Collection<?> collection) {
            return collection == null || collection.isEmpty();
        }
        
        public boolean isNotEmpty(Collection<?> collection) {
            return !isEmpty(collection);
        }
        
        public int size(Collection<?> collection) {
            return collection == null ? 0 : collection.size();
        }
        
        public <T> List<T> filter(Collection<T> items, java.util.function.Predicate<T> predicate) {
            return items.stream().filter(predicate).collect(java.util.stream.Collectors.toList());
        }
    }
}

/**
 * Functional interface for script functions
 */
@FunctionalInterface
interface ScriptFunction {
    Object apply(Object... args);
}