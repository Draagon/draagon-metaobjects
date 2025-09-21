package com.draagon.meta.manager.db.constraint;

import com.draagon.meta.constraint.ConstraintProvider;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;

import java.util.Set;

/**
 * Database constraint provider for Object Manager Database (OMDB) module.
 * 
 * This provider defines constraints for database-specific attributes used in ORM mapping,
 * SQL generation, and database schema management. These constraints are cross-cutting concerns
 * that apply to multiple MetaData types for database persistence purposes.
 */
public class DatabaseConstraintProvider implements ConstraintProvider {
    
    @Override
    public void registerConstraints(ConstraintRegistry registry) {
        // Database table constraints
        addTableConstraints(registry);
        
        // Database column constraints
        addColumnConstraints(registry);
        
        // Database type and schema constraints
        addTypeAndSchemaConstraints(registry);
        
        // Database metadata constraints (nullable, primary key, etc.)
        addDatabaseMetadataConstraints(registry);
        
        // OMDB-specific object manager attributes
        addOMDBSpecificConstraints(registry);
    }
    
    private void addTableConstraints(ConstraintRegistry registry) {
        // PLACEMENT CONSTRAINT: dbTable attribute can be placed on MetaObjects
        PlacementConstraint dbTablePlacement = new PlacementConstraint(
            "db.dbTable.placement",
            "dbTable attribute can be placed on MetaObjects for table mapping",
            (parent) -> parent instanceof MetaObject,
            (child) -> child instanceof MetaAttribute && "dbTable".equals(child.getName())
        );
        registry.addConstraint(dbTablePlacement);
        
        // VALIDATION CONSTRAINT: dbTable must be a valid SQL identifier and required
        ValidationConstraint dbTableValidation = new ValidationConstraint(
            "db.dbTable.validation",
            "dbTable must be a required, valid SQL identifier (1-64 chars, SQL-safe)",
            (metadata) -> metadata instanceof MetaAttribute && "dbTable".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return false; // Required
                String tableName = value.toString().trim();
                return isValidSqlIdentifier(tableName) && tableName.length() >= 1 && tableName.length() <= 64;
            }
        );
        registry.addConstraint(dbTableValidation);
        
        // PLACEMENT CONSTRAINT: dbSchema attribute can be placed on MetaObjects
        PlacementConstraint dbSchemaPlacement = new PlacementConstraint(
            "db.dbSchema.placement",
            "dbSchema attribute can be placed on MetaObjects for schema specification",
            (parent) -> parent instanceof MetaObject,
            (child) -> child instanceof MetaAttribute && "dbSchema".equals(child.getName())
        );
        registry.addConstraint(dbSchemaPlacement);
        
        // VALIDATION CONSTRAINT: dbSchema must be a valid SQL identifier
        ValidationConstraint dbSchemaValidation = new ValidationConstraint(
            "db.dbSchema.validation",
            "dbSchema must be a valid SQL identifier (1-64 chars, SQL-safe)",
            (metadata) -> metadata instanceof MetaAttribute && "dbSchema".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String schemaName = value.toString().trim();
                return isValidSqlIdentifier(schemaName) && schemaName.length() >= 1 && schemaName.length() <= 64;
            }
        );
        registry.addConstraint(dbSchemaValidation);
    }
    
    private void addColumnConstraints(ConstraintRegistry registry) {
        // PLACEMENT CONSTRAINT: dbColumn attribute can be placed on MetaFields
        PlacementConstraint dbColumnPlacement = new PlacementConstraint(
            "db.dbColumn.placement",
            "dbColumn attribute can be placed on MetaFields for column mapping",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && "dbColumn".equals(child.getName())
        );
        registry.addConstraint(dbColumnPlacement);
        
        // VALIDATION CONSTRAINT: dbColumn must be a valid SQL identifier and required
        ValidationConstraint dbColumnValidation = new ValidationConstraint(
            "db.dbColumn.validation",
            "dbColumn must be a required, valid SQL identifier (1-64 chars, SQL-safe)",
            (metadata) -> metadata instanceof MetaAttribute && "dbColumn".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return false; // Required
                String columnName = value.toString().trim();
                return isValidSqlIdentifier(columnName) && columnName.length() >= 1 && columnName.length() <= 64;
            }
        );
        registry.addConstraint(dbColumnValidation);
    }
    
    private void addTypeAndSchemaConstraints(ConstraintRegistry registry) {
        // PLACEMENT CONSTRAINT: dbType attribute can be placed on MetaFields
        PlacementConstraint dbTypePlacement = new PlacementConstraint(
            "db.dbType.placement",
            "dbType attribute can be placed on MetaFields for SQL type specification",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && "dbType".equals(child.getName())
        );
        registry.addConstraint(dbTypePlacement);
        
        // VALIDATION CONSTRAINT: dbType must be a valid SQL data type and required
        ValidationConstraint dbTypeValidation = new ValidationConstraint(
            "db.dbType.validation",
            "dbType must be a required, valid SQL data type",
            (metadata) -> metadata instanceof MetaAttribute && "dbType".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return false; // Required
                String dbType = value.toString().toUpperCase().trim();
                return isValidSqlDataType(dbType);
            }
        );
        registry.addConstraint(dbTypeValidation);
        
        // PLACEMENT CONSTRAINT: dbLength attribute can be placed on MetaFields
        PlacementConstraint dbLengthPlacement = new PlacementConstraint(
            "db.dbLength.placement",
            "dbLength attribute can be placed on MetaFields for field length specification",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && "dbLength".equals(child.getName())
        );
        registry.addConstraint(dbLengthPlacement);
        
        // VALIDATION CONSTRAINT: dbLength must be within valid range
        ValidationConstraint dbLengthValidation = new ValidationConstraint(
            "db.dbLength.validation",
            "dbLength must be within valid range (1-65535)",
            (metadata) -> metadata instanceof MetaAttribute && "dbLength".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                try {
                    int length = Integer.parseInt(value.toString());
                    return length >= 1 && length <= 65535;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        );
        registry.addConstraint(dbLengthValidation);
        
        // PLACEMENT CONSTRAINT: dbPrecision attribute can be placed on MetaFields
        PlacementConstraint dbPrecisionPlacement = new PlacementConstraint(
            "db.dbPrecision.placement",
            "dbPrecision attribute can be placed on MetaFields for decimal precision",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && "dbPrecision".equals(child.getName())
        );
        registry.addConstraint(dbPrecisionPlacement);
        
        // VALIDATION CONSTRAINT: dbPrecision must be within valid range
        ValidationConstraint dbPrecisionValidation = new ValidationConstraint(
            "db.dbPrecision.validation",
            "dbPrecision must be within valid range (1-65)",
            (metadata) -> metadata instanceof MetaAttribute && "dbPrecision".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                try {
                    int precision = Integer.parseInt(value.toString());
                    return precision >= 1 && precision <= 65;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        );
        registry.addConstraint(dbPrecisionValidation);
        
        // PLACEMENT CONSTRAINT: dbScale attribute can be placed on MetaFields
        PlacementConstraint dbScalePlacement = new PlacementConstraint(
            "db.dbScale.placement",
            "dbScale attribute can be placed on MetaFields for decimal scale",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && "dbScale".equals(child.getName())
        );
        registry.addConstraint(dbScalePlacement);
        
        // VALIDATION CONSTRAINT: dbScale must be within valid range
        ValidationConstraint dbScaleValidation = new ValidationConstraint(
            "db.dbScale.validation",
            "dbScale must be within valid range (0-30)",
            (metadata) -> metadata instanceof MetaAttribute && "dbScale".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                try {
                    int scale = Integer.parseInt(value.toString());
                    return scale >= 0 && scale <= 30;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        );
        registry.addConstraint(dbScaleValidation);
    }
    
    private void addDatabaseMetadataConstraints(ConstraintRegistry registry) {
        // PLACEMENT CONSTRAINT: dbNullable attribute can be placed on MetaFields
        PlacementConstraint dbNullablePlacement = new PlacementConstraint(
            "db.dbNullable.placement",
            "dbNullable attribute can be placed on MetaFields for null specification",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && "dbNullable".equals(child.getName())
        );
        registry.addConstraint(dbNullablePlacement);
        
        // VALIDATION CONSTRAINT: dbNullable is required boolean
        ValidationConstraint dbNullableValidation = new ValidationConstraint(
            "db.dbNullable.validation",
            "dbNullable must be a required boolean value",
            (metadata) -> metadata instanceof MetaAttribute && "dbNullable".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return false; // Required
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        );
        registry.addConstraint(dbNullableValidation);
        
        // PLACEMENT CONSTRAINT: dbPrimaryKey attribute can be placed on MetaFields
        PlacementConstraint dbPrimaryKeyPlacement = new PlacementConstraint(
            "db.dbPrimaryKey.placement",
            "dbPrimaryKey attribute can be placed on MetaFields for primary key specification",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && "dbPrimaryKey".equals(child.getName())
        );
        registry.addConstraint(dbPrimaryKeyPlacement);
        
        // VALIDATION CONSTRAINT: dbPrimaryKey is required boolean
        ValidationConstraint dbPrimaryKeyValidation = new ValidationConstraint(
            "db.dbPrimaryKey.validation",
            "dbPrimaryKey must be a required boolean value",
            (metadata) -> metadata instanceof MetaAttribute && "dbPrimaryKey".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return false; // Required
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        );
        registry.addConstraint(dbPrimaryKeyValidation);
        
        // PLACEMENT CONSTRAINT: dbDefault attribute can be placed on MetaFields
        PlacementConstraint dbDefaultPlacement = new PlacementConstraint(
            "db.dbDefault.placement",
            "dbDefault attribute can be placed on MetaFields for default value specification",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && "dbDefault".equals(child.getName())
        );
        registry.addConstraint(dbDefaultPlacement);
        
        // VALIDATION CONSTRAINT: dbDefault length limit
        ValidationConstraint dbDefaultValidation = new ValidationConstraint(
            "db.dbDefault.validation",
            "dbDefault value must be within 255 character limit",
            (metadata) -> metadata instanceof MetaAttribute && "dbDefault".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                return value.toString().length() <= 255;
            }
        );
        registry.addConstraint(dbDefaultValidation);
    }
    
    private void addOMDBSpecificConstraints(ConstraintRegistry registry) {
        // PLACEMENT CONSTRAINT: isIndex attribute can be placed on MetaFields
        PlacementConstraint isIndexPlacement = new PlacementConstraint(
            "db.isIndex.placement",
            "isIndex attribute can be placed on MetaFields for database index creation",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && "isIndex".equals(child.getName())
        );
        registry.addConstraint(isIndexPlacement);
        
        // VALIDATION CONSTRAINT: isIndex must be boolean
        ValidationConstraint isIndexValidation = new ValidationConstraint(
            "db.isIndex.validation",
            "isIndex must be a boolean value",
            (metadata) -> metadata instanceof MetaAttribute && "isIndex".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        );
        registry.addConstraint(isIndexValidation);
        
        // PLACEMENT CONSTRAINT: isUnique attribute can be placed on MetaFields
        PlacementConstraint isUniquePlacement = new PlacementConstraint(
            "db.isUnique.placement",
            "isUnique attribute can be placed on MetaFields for unique constraint",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && "isUnique".equals(child.getName())
        );
        registry.addConstraint(isUniquePlacement);
        
        // VALIDATION CONSTRAINT: isUnique must be boolean
        ValidationConstraint isUniqueValidation = new ValidationConstraint(
            "db.isUnique.validation",
            "isUnique must be a boolean value",
            (metadata) -> metadata instanceof MetaAttribute && "isUnique".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        );
        registry.addConstraint(isUniqueValidation);
        
        // PLACEMENT CONSTRAINT: isViewOnly attribute can be placed on MetaFields
        PlacementConstraint isViewOnlyPlacement = new PlacementConstraint(
            "db.isViewOnly.placement",
            "isViewOnly attribute can be placed on MetaFields for read-only fields",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && "isViewOnly".equals(child.getName())
        );
        registry.addConstraint(isViewOnlyPlacement);
        
        // VALIDATION CONSTRAINT: isViewOnly must be boolean
        ValidationConstraint isViewOnlyValidation = new ValidationConstraint(
            "db.isViewOnly.validation",
            "isViewOnly must be a boolean value",
            (metadata) -> metadata instanceof MetaAttribute && "isViewOnly".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        );
        registry.addConstraint(isViewOnlyValidation);
        
        // PLACEMENT CONSTRAINT: dbInheritance attribute can be placed on MetaObjects
        PlacementConstraint dbInheritancePlacement = new PlacementConstraint(
            "db.dbInheritance.placement",
            "dbInheritance attribute can be placed on MetaObjects for inheritance mapping",
            (parent) -> parent instanceof MetaObject,
            (child) -> child instanceof MetaAttribute && "dbInheritance".equals(child.getName())
        );
        registry.addConstraint(dbInheritancePlacement);
        
        // VALIDATION CONSTRAINT: dbInheritance value length limit
        ValidationConstraint dbInheritanceValidation = new ValidationConstraint(
            "db.dbInheritance.validation",
            "dbInheritance value must be within reasonable length",
            (metadata) -> metadata instanceof MetaAttribute && "dbInheritance".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                return value.toString().length() <= 500; // Reasonable limit for inheritance config
            }
        );
        registry.addConstraint(dbInheritanceValidation);
        
        // PLACEMENT CONSTRAINT: allowDirtyWrite attribute can be placed on MetaObjects
        PlacementConstraint allowDirtyWritePlacement = new PlacementConstraint(
            "db.allowDirtyWrite.placement",
            "allowDirtyWrite attribute can be placed on MetaObjects for dirty write control",
            (parent) -> parent instanceof MetaObject,
            (child) -> child instanceof MetaAttribute && "allowDirtyWrite".equals(child.getName())
        );
        registry.addConstraint(allowDirtyWritePlacement);
        
        // VALIDATION CONSTRAINT: allowDirtyWrite must be boolean
        ValidationConstraint allowDirtyWriteValidation = new ValidationConstraint(
            "db.allowDirtyWrite.validation",
            "allowDirtyWrite must be a boolean value",
            (metadata) -> metadata instanceof MetaAttribute && "allowDirtyWrite".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        );
        registry.addConstraint(allowDirtyWriteValidation);
    }
    
    private boolean isValidSqlIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = identifier.trim();
        
        // Check against SQL keywords (case-insensitive)
        Set<String> sqlKeywords = Set.of(
            "SELECT", "INSERT", "UPDATE", "DELETE", "FROM", "WHERE", "JOIN", "INNER", "OUTER", 
            "LEFT", "RIGHT", "FULL", "ORDER", "GROUP", "HAVING", "CREATE", "DROP", "ALTER", 
            "TABLE", "INDEX", "VIEW", "TRIGGER", "PROCEDURE", "FUNCTION", "DATABASE", "SCHEMA", 
            "USER", "ROLE", "GRANT", "REVOKE", "COMMIT", "ROLLBACK", "TRANSACTION", "BEGIN", 
            "END", "IF", "ELSE", "WHILE", "FOR", "LOOP", "DECLARE", "SET", "EXEC", "EXECUTE", 
            "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "CONSTRAINT", "UNIQUE", "NOT", "NULL", 
            "DEFAULT", "AUTO_INCREMENT", "TIMESTAMP", "CURRENT_TIMESTAMP"
        );
        
        if (sqlKeywords.contains(trimmed.toUpperCase())) {
            return false;
        }
        
        // Must match SQL identifier pattern: starts with letter, then letters/numbers/underscores
        return trimmed.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
    }
    
    private boolean isValidSqlDataType(String dataType) {
        if (dataType == null || dataType.trim().isEmpty()) {
            return false;
        }
        
        Set<String> validTypes = Set.of(
            "VARCHAR", "CHAR", "TEXT", "CLOB", "BLOB",
            "INTEGER", "INT", "BIGINT", "SMALLINT", "TINYINT",
            "DECIMAL", "NUMERIC", "FLOAT", "REAL", "DOUBLE",
            "DATE", "TIME", "DATETIME", "TIMESTAMP",
            "BOOLEAN", "BIT"
        );
        
        return validTypes.contains(dataType.toUpperCase().trim());
    }
    
    @Override
    public int getPriority() {
        return 1100; // Lower priority than core and web constraints
    }
    
    @Override
    public String getDescription() {
        return "Database constraints for ORM mapping and SQL schema generation";
    }
}