/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.registry;

import com.draagon.meta.MetaData;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.validator.MetaValidator;
import com.draagon.meta.validator.RequiredValidator;
import com.draagon.meta.validator.LengthValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Utility class for intelligent database naming and attribute inference.
 * 
 * <p>This class provides smart defaults for database-related attributes when they are not
 * explicitly specified in metadata. It follows the principle that all database attributes
 * should be optional, with intelligent defaults derived from:</p>
 * 
 * <ul>
 *   <li><strong>Object/Field Names</strong> - Converted from camelCase to snake_case</li>
 *   <li><strong>Validator Analysis</strong> - Nullable and length constraints inferred from validators</li>
 *   <li><strong>Type Analysis</strong> - Precision and scale from field data types</li>
 * </ul>
 * 
 * <p><strong>Naming Convention:</strong></p>
 * <ul>
 *   <li>{@code UserAccount} → {@code user_account} (table)</li>
 *   <li>{@code firstName} → {@code first_name} (column)</li>
 *   <li>{@code accountID} → {@code account_id} (column)</li>
 *   <li>{@code XMLParser} → {@code xml_parser} (table)</li>
 * </ul>
 * 
 * @since 6.0.0
 */
public final class DatabaseNamingUtils {
    
    private static final Logger log = LoggerFactory.getLogger(DatabaseNamingUtils.class);
    
    // Private constructor - utility class
    private DatabaseNamingUtils() {}
    
    /**
     * Get the database table name for a MetaObject.
     * 
     * <p>Returns the explicit dbTable attribute if present, otherwise converts
     * the object name from camelCase to snake_case.</p>
     * 
     * @param metaObject The MetaObject to get table name for
     * @return Database table name (never null)
     */
    public static String getTableName(MetaObject metaObject) {
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
     * Get the database column name for a MetaField.
     * 
     * <p>Returns the explicit dbColumn attribute if present, otherwise converts
     * the field name from camelCase to snake_case.</p>
     * 
     * @param metaField The MetaField to get column name for
     * @return Database column name (never null)
     */
    public static String getColumnName(MetaField metaField) {
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
     * Determine if a field should be nullable based on validators.
     * 
     * <p>Returns the explicit dbNullable attribute if present, otherwise analyzes
     * the field's validators to determine if it's required (not nullable).</p>
     * 
     * @param metaField The MetaField to analyze
     * @return true if the column should allow NULL values, false otherwise
     */
    public static boolean isNullable(MetaField metaField) {
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
     * 
     * <p>Returns the explicit dbLength attribute if present, otherwise analyzes
     * validators to determine appropriate length constraints.</p>
     * 
     * @param metaField The MetaField to analyze
     * @return Optional column length, empty if no length constraint should be applied
     */
    public static Optional<Integer> getColumnLength(MetaField metaField) {
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
     * 
     * <p>Returns the explicit dbPrecision attribute if present, otherwise returns
     * empty Optional as precision is typically database-specific.</p>
     * 
     * @param metaField The MetaField to analyze
     * @return Optional precision value
     */
    public static Optional<Integer> getPrecision(MetaField metaField) {
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
     * 
     * <p>Returns the explicit dbScale attribute if present, otherwise returns
     * empty Optional as scale is typically database-specific.</p>
     * 
     * @param metaField The MetaField to analyze
     * @return Optional scale value
     */
    public static Optional<Integer> getScale(MetaField metaField) {
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
     * 
     * <p>Handles various edge cases:</p>
     * <ul>
     *   <li>{@code UserAccount} → {@code user_account}</li>
     *   <li>{@code firstName} → {@code first_name}</li>
     *   <li>{@code XMLParser} → {@code xml_parser}</li>
     *   <li>{@code accountID} → {@code account_id}</li>
     *   <li>{@code IOUtils} → {@code io_utils}</li>
     * </ul>
     * 
     * @param input The camelCase or PascalCase string
     * @return snake_case equivalent (never null)
     */
    public static String camelCaseToSnakeCase(String input) {
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
     * 
     * @param metaField The MetaField to check
     * @param validatorClass The validator class to look for
     * @return true if the field has the specified validator type
     */
    private static boolean hasValidator(MetaField metaField, Class<? extends MetaValidator> validatorClass) {
        List<MetaValidator> validators = metaField.getChildren(MetaValidator.class);
        return validators.stream()
            .anyMatch(validator -> validatorClass.isInstance(validator));
    }
    
    /**
     * Extract maximum length from LengthValidator or similar validators.
     * 
     * @param metaField The MetaField to analyze
     * @return Optional maximum length from validators
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
}