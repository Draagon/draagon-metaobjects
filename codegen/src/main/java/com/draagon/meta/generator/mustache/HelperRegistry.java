package com.draagon.meta.generator.mustache;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.field.MetaField;
import org.apache.commons.lang3.StringUtils;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.function.Function;

/**
 * Registry for helper functions used in Mustache templates.
 * Provides Java-specific helper functions for string manipulation,
 * type conversion, and MetaObjects integration.
 * 
 * Based on the cross-language template system architecture documented in
 * TEMPLATE_IMPLEMENTATION_GUIDE.md
 */
public class HelperRegistry {
    
    private final Map<String, Function<Object, Object>> helpers = new HashMap<>();
    
    public HelperRegistry() {
        registerDefaultHelpers();
    }
    
    private void registerDefaultHelpers() {
        // String manipulation helpers
        register("capitalize", this::capitalize);
        register("camelCase", this::camelCase);
        register("pascalCase", this::pascalCase);
        register("upperCase", this::upperCase);
        register("lowerCase", this::lowerCase);
        
        // Java type helpers
        register("javaType", this::getJavaType);
        register("isPrimitive", this::isPrimitive);
        register("isCollection", this::isCollection);
        register("javaImport", this::getJavaImport);
        register("javaDefault", this::getJavaDefault);
        
        // Database helpers
        register("dbColumnName", this::getDbColumnName);
        register("dbTableName", this::getDbTableName);
        register("isIdField", this::isIdField);
        register("isNullable", this::isNullable);
        
        // MetaObjects helpers
        register("hasAttribute", this::hasAttribute);
        register("getAttributeValue", this::getAttributeValue);
        register("getFieldsByType", this::getFieldsByType);
        register("fullName", this::getFullName);
        
        // JPA specific helpers
        register("jpaColumnType", this::getJpaColumnType);
        register("isSearchable", this::isSearchable);
    }
    
    /**
     * Register a helper function with the given name.
     */
    public void register(String name, Function<Object, Object> helper) {
        helpers.put(name, helper);
    }
    
    /**
     * Get a helper function by name.
     */
    public Function<Object, Object> get(String name) {
        return helpers.get(name);
    }
    
    /**
     * Check if a helper function exists.
     */
    public boolean contains(String name) {
        return helpers.containsKey(name);
    }
    
    // Helper function implementations
    
    private Object capitalize(Object input) {
        return input != null ? StringUtils.capitalize(input.toString()) : null;
    }
    
    private Object camelCase(Object input) {
        if (input == null) return null;
        String str = input.toString();
        return StringUtils.uncapitalize(pascalCase(str).toString());
    }
    
    private Object pascalCase(Object input) {
        if (input == null) return null;
        String str = input.toString();
        return Arrays.stream(str.split("[_\\s]+"))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining());
    }
    
    private Object upperCase(Object input) {
        return input != null ? input.toString().toUpperCase() : null;
    }
    
    private Object lowerCase(Object input) {
        return input != null ? input.toString().toLowerCase() : null;
    }
    
    private Object getJavaType(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            String dataType = field.getDataType().toString();
            String fieldName = field.getName();
            return mapDataTypeToJava(dataType, fieldName);
        }
        if (input instanceof String) {
            return mapDataTypeToJava((String) input, null);
        }
        return "Object";
    }
    
    private String mapDataTypeToJava(String dataType, String fieldName) {
        switch (dataType.toLowerCase()) {
            case "string": return "String";
            case "int": case "integer": return "Integer";
            case "long": return "Long";
            case "boolean": return "Boolean";
            case "double": 
                // Special handling for monetary fields
                if (fieldName != null && (fieldName.toLowerCase().contains("price") || 
                    fieldName.toLowerCase().contains("amount") || 
                    fieldName.toLowerCase().contains("cost"))) {
                    return "java.math.BigDecimal";
                }
                return "Double";
            case "float": return "Float";
            case "date": return "java.time.LocalDate";
            case "datetime": return "java.time.LocalDateTime";
            case "decimal": return "java.math.BigDecimal";
            case "uuid": return "java.util.UUID";
            default: return "Object";
        }
    }
    
    private String mapDataTypeToJava(String dataType) {
        return mapDataTypeToJava(dataType, null);
    }
    
    private Object getJavaImport(Object input) {
        String javaType = getJavaType(input).toString();
        if (javaType.contains(".")) {
            return javaType;
        }
        return null; // No import needed for primitives
    }
    
    private Object getJavaDefault(Object input) {
        String javaType = getJavaType(input).toString();
        switch (javaType) {
            case "String": return "\"\"";
            case "Integer": return "0";
            case "Long": return "0L";
            case "Boolean": return "false";
            case "Double": return "0.0";
            case "Float": return "0.0f";
            case "java.time.LocalDate": return "java.time.LocalDate.now()";
            case "java.time.LocalDateTime": return "java.time.LocalDateTime.now()";
            case "java.math.BigDecimal": return "java.math.BigDecimal.ZERO";
            case "java.util.UUID": return "java.util.UUID.randomUUID()";
            default: return "null";
        }
    }
    
    private Object isPrimitive(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            String dataType = field.getDataType().toString();
            return Arrays.asList("int", "long", "boolean", "double", "float").contains(dataType.toLowerCase());
        }
        return false;
    }
    
    private Object isCollection(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            return field.hasMetaAttr("collection") && Boolean.parseBoolean(field.getMetaAttr("collection").getValueAsString());
        }
        return false;
    }
    
    private Object getDbColumnName(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            return field.hasMetaAttr("dbColumn") ? field.getMetaAttr("dbColumn").getValueAsString() : field.getName();
        }
        return null;
    }
    
    private Object getDbTableName(Object input) {
        if (input instanceof MetaObject) {
            MetaObject metaObject = (MetaObject) input;
            return metaObject.hasMetaAttr("dbTable") ? metaObject.getMetaAttr("dbTable").getValueAsString() : metaObject.getName();
        }
        return null;
    }
    
    private Object isIdField(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            return field.hasMetaAttr("isId") && Boolean.parseBoolean(field.getMetaAttr("isId").getValueAsString());
        }
        return false;
    }
    
    private Object isNullable(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            return !field.hasMetaAttr("required") || !Boolean.parseBoolean(field.getMetaAttr("required").getValueAsString());
        }
        return true;
    }
    
    private Object isSearchable(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            return field.hasMetaAttr("isSearchable") && Boolean.parseBoolean(field.getMetaAttr("isSearchable").getValueAsString());
        }
        return false;
    }
    
    private Object getJpaColumnType(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            String dataType = field.getDataType().toString();
            switch (dataType.toLowerCase()) {
                case "string": return "VARCHAR(255)";
                case "int": case "integer": return "INTEGER";
                case "long": return "BIGINT";
                case "boolean": return "BOOLEAN";
                case "double": return "DOUBLE";
                case "float": return "FLOAT";
                case "date": return "DATE";
                case "datetime": return "TIMESTAMP";
                case "decimal": return "DECIMAL(19,2)";
                case "uuid": return "VARCHAR(36)";
                default: return "VARCHAR(255)";
            }
        }
        return "VARCHAR(255)";
    }
    
    private Object hasAttribute(Object input) {
        // This would need additional context about which attribute to check
        // For now, return a placeholder implementation
        return false;
    }
    
    private Object getAttributeValue(Object input) {
        // This would need additional context about which attribute to get
        // For now, return a placeholder implementation
        return null;
    }
    
    private Object getFieldsByType(Object input) {
        // This would need additional context about which type to filter by
        // For now, return a placeholder implementation
        return null;
    }
    
    private Object getFullName(Object input) {
        if (input instanceof MetaObject) {
            MetaObject metaObject = (MetaObject) input;
            return metaObject.getPackage() + "::" + metaObject.getName();
        }
        return null;
    }
}