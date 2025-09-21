package com.draagon.meta.generator.mustache;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.key.PrimaryKey;
import com.draagon.meta.key.ForeignKey;
import com.draagon.meta.key.SecondaryKey;
import com.draagon.meta.validator.MetaValidator;
import com.draagon.meta.registry.DatabaseNamingUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.function.Function;

/**
 * Registry for helper functions used in Mustache templates.
 * Provides Java-specific helper functions for string manipulation,
 * type conversion, and MetaObjects integration.
 * 
 * Based on the cross-language template system architecture documented in
 * .claude/archive/template-system/TEMPLATE_IMPLEMENTATION_GUIDE.md
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
        register("isForeignKeyField", this::isForeignKeyField);
        register("isSecondaryKeyField", this::isSecondaryKeyField);
        register("isNullable", this::isNullable);
        register("getColumnLength", this::getColumnLength);
        register("hasColumnLength", this::hasColumnLength);
        register("getPrecision", this::getPrecision);
        register("getScale", this::getScale);
        
        // MetaObjects helpers
        register("hasAttribute", this::hasAttribute);
        register("getAttributeValue", this::getAttributeValue);
        register("getFieldsByType", this::getFieldsByType);
        register("fullName", this::getFullName);
        
        // JPA specific helpers
        register("jpaColumnType", this::getJpaColumnType);
        register("isSearchable", this::isSearchable);
        register("shouldGenerateJpa", this::shouldGenerateJpa);
        
        // Validation helpers
        register("hasValidation", this::hasValidation);
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
            // Use DatabaseNamingUtils for intelligent defaults with snake_case conversion
            return DatabaseNamingUtils.getColumnName(field);
        }
        return null;
    }
    
    private Object getDbTableName(Object input) {
        if (input instanceof MetaObject) {
            MetaObject metaObject = (MetaObject) input;
            // Use DatabaseNamingUtils for intelligent defaults with snake_case conversion
            return DatabaseNamingUtils.getTableName(metaObject);
        }
        return null;
    }
    
    private Object isIdField(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            
            // FIRST: Check if this field is part of a PrimaryKey metadata (preferred approach)
            MetaObject metaObject = (MetaObject) field.getParent();
            if (metaObject != null) {
                // Look for PrimaryKey children in the MetaObject
                List<PrimaryKey> primaryKeys = metaObject.getChildren(PrimaryKey.class);
                
                for (PrimaryKey primaryKey : primaryKeys) {
                    List<MetaField> keyFields = primaryKey.getKeyFields();
                    if (keyFields.contains(field)) {
                        return true;
                    }
                }
            }
            
            // INFERENCE: Use intelligent naming and pattern inference for clean implementation
            return inferIdFieldFromPatterns(field);
        }
        return false;
    }
    
    private boolean inferIdFieldFromPatterns(MetaField field) {
        String fieldName = field.getName();
        String dbColumn = field.hasMetaAttr("dbColumn") ? field.getMetaAttr("dbColumn").getValueAsString() : "";
        
        // Common ID field naming patterns
        if ("id".equals(fieldName)) return true;
        if (fieldName != null && fieldName.endsWith("Id")) return true;
        if (fieldName != null && fieldName.endsWith("ID")) return true;
        
        // Database column naming patterns
        if (dbColumn.endsWith("_id")) return true;
        if (dbColumn.endsWith("_ID")) return true;
        if ("id".equals(dbColumn)) return true;
        
        // Type-based inference for numeric ID fields
        if (("id".equals(fieldName) || fieldName.endsWith("Id")) && 
            (field.getSubTypeName().equals("long") || field.getSubTypeName().equals("int"))) {
            return true;
        }
        
        return false;
    }
    
    private Object isForeignKeyField(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            
            // Check if this field is part of a ForeignKey metadata
            MetaObject metaObject = (MetaObject) field.getParent();
            if (metaObject != null) {
                // Look for ForeignKey children in the MetaObject
                List<ForeignKey> foreignKeys = metaObject.getChildren(ForeignKey.class);
                
                for (ForeignKey foreignKey : foreignKeys) {
                    List<MetaField> keyFields = foreignKey.getKeyFields();
                    if (keyFields.contains(field)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private Object isSecondaryKeyField(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            
            // Check if this field is part of a SecondaryKey metadata
            MetaObject metaObject = (MetaObject) field.getParent();
            if (metaObject != null) {
                // Look for SecondaryKey children in the MetaObject
                List<SecondaryKey> secondaryKeys = metaObject.getChildren(SecondaryKey.class);
                
                for (SecondaryKey secondaryKey : secondaryKeys) {
                    List<MetaField> keyFields = secondaryKey.getKeyFields();
                    if (keyFields.contains(field)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private Object isNullable(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            // Use DatabaseNamingUtils for intelligent nullable detection based on validators
            return DatabaseNamingUtils.isNullable(field);
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
    
    private Object shouldGenerateJpa(Object input) {
        if (input instanceof MetaObject) {
            MetaObject metaObject = (MetaObject) input;
            
            // If skipJpa is explicitly set to true, don't generate JPA
            if (metaObject.hasMetaAttr("skipJpa") && 
                Boolean.parseBoolean(metaObject.getMetaAttr("skipJpa").getValueAsString())) {
                return false;
            }
            
            // Inference: Generate JPA if object has database-related attributes or keys
            return metaObject.hasMetaAttr("dbTable") || 
                   hasAnyFieldWithDbColumn(metaObject) ||
                   hasAnyDatabaseKeys(metaObject);
        }
        
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            
            // If skipJpa is explicitly set to true, don't generate JPA
            if (field.hasMetaAttr("skipJpa") && 
                Boolean.parseBoolean(field.getMetaAttr("skipJpa").getValueAsString())) {
                return false;
            }
            
            // Inference: Generate JPA if field has database-related attributes or is part of keys
            return field.hasMetaAttr("dbColumn") || 
                   isPartOfAnyKey(field);
        }
        
        return false;
    }
    
    private boolean hasAnyFieldWithDbColumn(MetaObject metaObject) {
        List<MetaField> fields = metaObject.getChildren(MetaField.class);
        return fields.stream().anyMatch(field -> field.hasMetaAttr("dbColumn"));
    }
    
    private boolean hasAnyDatabaseKeys(MetaObject metaObject) {
        // Check for any database-related keys (Primary, Foreign, Secondary)
        return !metaObject.getChildren(PrimaryKey.class).isEmpty() ||
               !metaObject.getChildren(ForeignKey.class).isEmpty() ||
               !metaObject.getChildren(SecondaryKey.class).isEmpty();
    }
    
    private boolean isPartOfAnyKey(MetaField field) {
        // Check if field is part of any key type
        return ((Boolean) isIdField(field)) ||
               ((Boolean) isForeignKeyField(field)) ||
               ((Boolean) isSecondaryKeyField(field));
    }
    
    private Object hasValidation(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            
            // Check if this field has any MetaValidator children
            List<MetaValidator> validators = field.getChildren(MetaValidator.class);
            return !validators.isEmpty();
        }
        
        if (input instanceof MetaObject) {
            MetaObject metaObject = (MetaObject) input;
            
            // Check if the object itself has validators or if any of its fields have validators
            List<MetaValidator> objectValidators = metaObject.getChildren(MetaValidator.class);
            if (!objectValidators.isEmpty()) {
                return true;
            }
            
            // Check if any field has validators
            List<MetaField> fields = metaObject.getChildren(MetaField.class);
            return fields.stream().anyMatch(field -> !field.getChildren(MetaValidator.class).isEmpty());
        }
        
        return false;
    }
    
    private Object getColumnLength(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            // Use DatabaseNamingUtils to intelligently determine column length from validators
            return DatabaseNamingUtils.getColumnLength(field).orElse(null);
        }
        return null;
    }
    
    private Object hasColumnLength(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            // Check if a column length constraint exists
            return DatabaseNamingUtils.getColumnLength(field).isPresent();
        }
        return false;
    }
    
    private Object getPrecision(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            // Use DatabaseNamingUtils to get precision for numeric fields
            return DatabaseNamingUtils.getPrecision(field).orElse(null);
        }
        return null;
    }
    
    private Object getScale(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            // Use DatabaseNamingUtils to get scale for numeric fields
            return DatabaseNamingUtils.getScale(field).orElse(null);
        }
        return null;
    }
}