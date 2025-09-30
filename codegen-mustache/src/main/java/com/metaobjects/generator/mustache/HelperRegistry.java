package com.metaobjects.generator.mustache;

import com.metaobjects.object.MetaObject;
import com.metaobjects.field.MetaField;
import com.metaobjects.key.PrimaryKey;
import com.metaobjects.key.ForeignKey;
import com.metaobjects.key.SecondaryKey;
import com.metaobjects.validator.MetaValidator;
import com.metaobjects.validator.RequiredValidator;
import com.metaobjects.validator.LengthValidator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
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

    private static final Logger log = LoggerFactory.getLogger(HelperRegistry.class);

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

        // Template-specific helpers for code generation
        register("constantFieldName", this::getConstantFieldName);
        register("isBoolean", this::isBoolean);

        // Database helper aliases and additional helpers
        register("dbColumn", this::getDbColumnName); // Alias for dbColumnName
        register("hasDbColumn", this::hasDbColumn);
        register("capitalizedFieldName", this::capitalize); // Alias for capitalize
        register("hasNext", this::hasNext);
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
            // Use inlined database naming logic for intelligent defaults with snake_case conversion
            return getColumnName(field);
        }
        return null;
    }
    
    private Object getDbTableName(Object input) {
        if (input instanceof MetaObject) {
            MetaObject metaObject = (MetaObject) input;
            // Use inlined database naming logic for intelligent defaults with snake_case conversion
            return getTableName(metaObject);
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
            (field.getSubType().equals("long") || field.getSubType().equals("int"))) {
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
            // Use inlined database naming logic for intelligent nullable detection based on validators
            return isFieldNullable(field);
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
            // Use inlined database naming logic to intelligently determine column length from validators
            return getFieldColumnLength(field).orElse(null);
        }
        return null;
    }
    
    private Object hasColumnLength(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            // Check if a column length constraint exists
            return getFieldColumnLength(field).isPresent();
        }
        return false;
    }
    
    private Object getPrecision(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            // Use inlined database naming logic to get precision for numeric fields
            return getFieldPrecision(field).orElse(null);
        }
        return null;
    }
    
    private Object getScale(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            // Use inlined database naming logic to get scale for numeric fields
            return getFieldScale(field).orElse(null);
        }
        return null;
    }

    // ========================================
    // Inlined Database Naming Utility Methods
    // ========================================

    /**
     * Get the database column name for a MetaField.
     * Returns the explicit dbColumn attribute if present, otherwise converts
     * the field name from camelCase to snake_case.
     */
    private String getColumnName(MetaField metaField) {
        // Check for explicit dbColumn attribute first
        if (metaField.hasMetaAttr("dbColumn")) {
            String explicitColumn = metaField.getMetaAttr("dbColumn").getValueAsString();
            if (explicitColumn != null && !explicitColumn.trim().isEmpty()) {
                log.debug("Using explicit dbColumn: {} for field: {}", explicitColumn, metaField.getName());
                return explicitColumn.trim();
            }
        }

        // Default: Convert field name from camelCase to snake_case
        String fieldName = metaField.getName();
        String columnName = camelCaseToSnakeCase(fieldName);
        log.debug("Generated column name: {} from field: {}", columnName, fieldName);
        return columnName;
    }

    /**
     * Get the database table name for a MetaObject.
     * Returns the explicit dbTable attribute if present, otherwise converts
     * the object name from camelCase to snake_case.
     */
    private String getTableName(MetaObject metaObject) {
        // Check for explicit dbTable attribute first
        if (metaObject.hasMetaAttr("dbTable")) {
            String explicitTable = metaObject.getMetaAttr("dbTable").getValueAsString();
            if (explicitTable != null && !explicitTable.trim().isEmpty()) {
                log.debug("Using explicit dbTable: {} for object: {}", explicitTable, metaObject.getName());
                return explicitTable.trim();
            }
        }

        // Default: Convert object name from camelCase to snake_case
        String objectName = metaObject.getName();
        String tableName = camelCaseToSnakeCase(objectName);
        log.debug("Generated table name: {} from object: {}", tableName, objectName);
        return tableName;
    }

    /**
     * Determine if a field should be nullable based on validators.
     * Returns the explicit dbNullable attribute if present, otherwise analyzes
     * the field's validators to determine if it's required (not nullable).
     */
    private boolean isFieldNullable(MetaField metaField) {
        // Check for explicit dbNullable attribute first
        if (metaField.hasMetaAttr("dbNullable")) {
            try {
                boolean explicitNullable = Boolean.parseBoolean(
                    metaField.getMetaAttr("dbNullable").getValueAsString());
                log.debug("Using explicit dbNullable: {} for field: {}", explicitNullable, metaField.getName());
                return explicitNullable;
            } catch (Exception e) {
                log.warn("Invalid dbNullable value for field {}, falling back to validator analysis",
                    metaField.getName());
            }
        }

        // Default: Analyze validators to determine if field is required
        boolean hasRequiredValidator = hasValidator(metaField, RequiredValidator.class);
        boolean nullable = !hasRequiredValidator;

        log.debug("Inferred nullable: {} for field: {} (hasRequiredValidator: {})",
            nullable, metaField.getName(), hasRequiredValidator);
        return nullable;
    }

    /**
     * Get the database column length for a field.
     * Returns the explicit dbLength attribute if present, otherwise analyzes
     * validators to determine appropriate length constraints.
     */
    private Optional<Integer> getFieldColumnLength(MetaField metaField) {
        // Check for explicit dbLength attribute first
        if (metaField.hasMetaAttr("dbLength")) {
            try {
                int explicitLength = Integer.parseInt(
                    metaField.getMetaAttr("dbLength").getValueAsString());
                log.debug("Using explicit dbLength: {} for field: {}", explicitLength, metaField.getName());
                return Optional.of(explicitLength);
            } catch (NumberFormatException e) {
                log.warn("Invalid dbLength value for field {}, falling back to validator analysis",
                    metaField.getName());
            }
        }

        // Default: Analyze LengthValidator to determine maximum length
        Optional<Integer> validatorLength = getMaxLengthFromValidators(metaField);
        if (validatorLength.isPresent()) {
            log.debug("Inferred length: {} from validators for field: {}",
                validatorLength.get(), metaField.getName());
            return validatorLength;
        }

        // Check for maxLength attribute (common pattern)
        if (metaField.hasMetaAttr("maxLength")) {
            try {
                int maxLength = Integer.parseInt(
                    metaField.getMetaAttr("maxLength").getValueAsString());
                log.debug("Using maxLength attribute: {} for field: {}", maxLength, metaField.getName());
                return Optional.of(maxLength);
            } catch (NumberFormatException e) {
                log.warn("Invalid maxLength value for field {}", metaField.getName());
            }
        }

        log.debug("No length constraint found for field: {}", metaField.getName());
        return Optional.empty();
    }

    /**
     * Get the database precision for numeric fields.
     * Returns the explicit dbPrecision attribute if present, otherwise returns
     * empty Optional as precision is typically database-specific.
     */
    private Optional<Integer> getFieldPrecision(MetaField metaField) {
        if (metaField.hasMetaAttr("dbPrecision")) {
            try {
                int precision = Integer.parseInt(
                    metaField.getMetaAttr("dbPrecision").getValueAsString());
                log.debug("Using explicit dbPrecision: {} for field: {}", precision, metaField.getName());
                return Optional.of(precision);
            } catch (NumberFormatException e) {
                log.warn("Invalid dbPrecision value for field {}", metaField.getName());
            }
        }

        return Optional.empty();
    }

    /**
     * Get the database scale for numeric fields.
     * Returns the explicit dbScale attribute if present, otherwise returns
     * empty Optional as scale is typically database-specific.
     */
    private Optional<Integer> getFieldScale(MetaField metaField) {
        if (metaField.hasMetaAttr("dbScale")) {
            try {
                int scale = Integer.parseInt(
                    metaField.getMetaAttr("dbScale").getValueAsString());
                log.debug("Using explicit dbScale: {} for field: {}", scale, metaField.getName());
                return Optional.of(scale);
            } catch (NumberFormatException e) {
                log.warn("Invalid dbScale value for field {}", metaField.getName());
            }
        }

        return Optional.empty();
    }

    /**
     * Convert camelCase or PascalCase to snake_case.
     * Handles various edge cases:
     * - UserAccount → user_account
     * - firstName → first_name
     * - XMLParser → xml_parser
     * - accountID → account_id
     * - IOUtils → io_utils
     */
    private static String camelCaseToSnakeCase(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        char[] chars = input.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char current = chars[i];

            // Add underscore before uppercase letters (except first character)
            if (i > 0 && Character.isUpperCase(current)) {
                // Handle consecutive uppercase letters (like "XMLParser" or "IOUtils")
                if (i < chars.length - 1 && Character.isLowerCase(chars[i + 1])) {
                    // Current is start of a new word (XML|Parser, IO|Utils)
                    result.append('_');
                } else if (i > 0 && Character.isLowerCase(chars[i - 1])) {
                    // Transition from lowercase to uppercase (first|Name, account|ID)
                    result.append('_');
                }
            }

            result.append(Character.toLowerCase(current));
        }

        return result.toString();
    }

    /**
     * Check if a MetaField has a validator of the specified type.
     */
    private static boolean hasValidator(MetaField metaField, Class<? extends MetaValidator> validatorClass) {
        List<MetaValidator> validators = metaField.getChildren(MetaValidator.class);
        return validators.stream()
            .anyMatch(validator -> validatorClass.isInstance(validator));
    }

    /**
     * Extract maximum length from LengthValidator or similar validators.
     */
    private static Optional<Integer> getMaxLengthFromValidators(MetaField metaField) {
        List<MetaValidator> validators = metaField.getChildren(MetaValidator.class);

        for (MetaValidator validator : validators) {
            if (validator instanceof LengthValidator) {
                LengthValidator lengthValidator = (LengthValidator) validator;
                // LengthValidator typically has getMaxLength() method
                try {
                    // Use reflection to call getMaxLength() if it exists
                    java.lang.reflect.Method getMaxLength = lengthValidator.getClass().getMethod("getMaxLength");
                    Object maxLength = getMaxLength.invoke(lengthValidator);
                    if (maxLength instanceof Integer) {
                        return Optional.of((Integer) maxLength);
                    }
                } catch (Exception e) {
                    log.debug("Could not extract max length from LengthValidator for field: {}",
                        metaField.getName());
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Convert a field name to constant format (UPPER_SNAKE_CASE).
     * Examples: "maxTanks" → "MAX_TANKS", "firstName" → "FIRST_NAME", "id" → "ID"
     */
    private Object getConstantFieldName(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            return fieldNameToConstant(field.getName());
        }
        if (input instanceof String) {
            return fieldNameToConstant((String) input);
        }
        return null;
    }

    /**
     * Check if a field has a boolean data type.
     */
    private Object isBoolean(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            String dataType = field.getDataType().toString().toLowerCase();
            return "boolean".equals(dataType);
        }
        return false;
    }

    /**
     * Convert a field name from camelCase to CONSTANT_CASE.
     */
    private String fieldNameToConstant(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return "";
        }

        // Convert to snake_case first, then to uppercase
        return camelCaseToSnakeCase(fieldName).toUpperCase();
    }

    /**
     * Check if a field has a database column mapping.
     */
    private Object hasDbColumn(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            return field.hasMetaAttr("dbColumn");
        }
        return false;
    }

    /**
     * Helper for template iteration - determines if current item has a next item.
     * Note: This is typically handled by the template engine context, but provided
     * as a fallback helper for compatibility.
     */
    private Object hasNext(Object input) {
        // This helper is typically provided by the template context during field iteration
        // Return false as default - the actual hasNext logic is handled in MustacheTemplateEngine
        return false;
    }
}