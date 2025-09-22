/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.database.common;

import com.draagon.meta.constraint.ConstraintProvider;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;

import java.util.Set;

import static com.draagon.meta.database.common.DatabaseAttributeConstants.*;

/**
 * Shared database constraint provider for database-related attributes.
 *
 * <p>This provider consolidates database constraints that were previously duplicated between
 * OMDB and code generation modules. It defines constraints for database-specific attributes
 * used in:</p>
 * <ul>
 *   <li><strong>ORM Mapping</strong> - Object-relational mapping for persistence</li>
 *   <li><strong>SQL Generation</strong> - Database schema and query generation</li>
 *   <li><strong>JPA Code Generation</strong> - JPA entity and annotation generation</li>
 *   <li><strong>Database Schema Management</strong> - DDL and database administration</li>
 * </ul>
 *
 * <p><strong>Architectural Principle:</strong> Database attributes are cross-cutting concerns
 * that apply to multiple modules. This provider ensures consistent validation and placement
 * rules across all database-aware components.</p>
 */
public class DatabaseConstraintProvider implements ConstraintProvider {

    @Override
    public void registerConstraints(ConstraintRegistry registry) {
        // Table-level constraints (MetaObject attributes)
        addTableLevelConstraints(registry);

        // Field-level constraints (MetaField attributes)
        addFieldLevelConstraints(registry);

        // Database type and schema constraints
        addTypeAndSchemaConstraints(registry);

        // Database constraint and indexing attributes
        addConstraintAndIndexConstraints(registry);

        // OMDB-specific operational attributes
        addOMDBOperationalConstraints(registry);
    }

    private void addTableLevelConstraints(ConstraintRegistry registry) {
        // === DATABASE TABLE CONSTRAINTS ===

        // PLACEMENT: dbTable attribute on MetaObjects
        registry.addConstraint(new PlacementConstraint(
            "database.dbTable.placement",
            "dbTable attribute can be placed on MetaObjects for table mapping",
            (parent) -> parent instanceof MetaObject,
            (child) -> child instanceof MetaAttribute && ATTR_DB_TABLE.equals(child.getName())
        ));

        // VALIDATION: dbTable must be valid SQL identifier
        registry.addConstraint(new ValidationConstraint(
            "database.dbTable.validation",
            "dbTable must be a valid SQL identifier (1-64 chars, SQL-safe)",
            (metadata) -> metadata instanceof MetaAttribute && ATTR_DB_TABLE.equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String tableName = value.toString().trim();
                return isValidSqlIdentifier(tableName) && tableName.length() >= 1 && tableName.length() <= 64;
            }
        ));

        // === DATABASE SCHEMA CONSTRAINTS ===

        // PLACEMENT: dbSchema attribute on MetaObjects
        registry.addConstraint(new PlacementConstraint(
            "database.dbSchema.placement",
            "dbSchema attribute can be placed on MetaObjects for schema specification",
            (parent) -> parent instanceof MetaObject,
            (child) -> child instanceof MetaAttribute && ATTR_DB_SCHEMA.equals(child.getName())
        ));

        // VALIDATION: dbSchema must be valid SQL identifier
        registry.addConstraint(new ValidationConstraint(
            "database.dbSchema.validation",
            "dbSchema must be a valid SQL identifier (1-64 chars, SQL-safe)",
            (metadata) -> metadata instanceof MetaAttribute && ATTR_DB_SCHEMA.equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String schemaName = value.toString().trim();
                return isValidSqlIdentifier(schemaName) && schemaName.length() >= 1 && schemaName.length() <= 64;
            }
        ));

        // === DATABASE VIEW CONSTRAINTS ===

        // PLACEMENT: dbView attribute on MetaObjects
        registry.addConstraint(new PlacementConstraint(
            "database.dbView.placement",
            "dbView attribute can be placed on MetaObjects for view mapping",
            (parent) -> parent instanceof MetaObject,
            (child) -> child instanceof MetaAttribute && ATTR_DB_VIEW.equals(child.getName())
        ));

        // PLACEMENT: dbViewSQL attribute on MetaObjects
        registry.addConstraint(new PlacementConstraint(
            "database.dbViewSQL.placement",
            "dbViewSQL attribute can be placed on MetaObjects for view SQL definition",
            (parent) -> parent instanceof MetaObject,
            (child) -> child instanceof MetaAttribute && ATTR_DB_VIEW_SQL.equals(child.getName())
        ));

        // VALIDATION: dbViewSQL length limit
        registry.addConstraint(new ValidationConstraint(
            "database.dbViewSQL.validation",
            "dbViewSQL must be within reasonable length (max 8000 chars)",
            (metadata) -> metadata instanceof MetaAttribute && ATTR_DB_VIEW_SQL.equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                return value.toString().length() <= 8000; // Reasonable SQL length limit
            }
        ));
    }

    private void addFieldLevelConstraints(ConstraintRegistry registry) {
        // === DATABASE COLUMN CONSTRAINTS ===

        // PLACEMENT: dbColumn attribute on MetaFields
        registry.addConstraint(new PlacementConstraint(
            "database.dbColumn.placement",
            "dbColumn attribute can be placed on MetaFields for column mapping",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && ATTR_DB_COLUMN.equals(child.getName())
        ));

        // VALIDATION: dbColumn must be valid SQL identifier
        registry.addConstraint(new ValidationConstraint(
            "database.dbColumn.validation",
            "dbColumn must be a valid SQL identifier (1-64 chars, SQL-safe)",
            (metadata) -> metadata instanceof MetaAttribute && ATTR_DB_COLUMN.equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String columnName = value.toString().trim();
                return isValidSqlIdentifier(columnName) && columnName.length() >= 1 && columnName.length() <= 64;
            }
        ));

        // === DATABASE NULLABLE CONSTRAINTS ===

        // PLACEMENT: dbNullable attribute on MetaFields
        registry.addConstraint(new PlacementConstraint(
            "database.dbNullable.placement",
            "dbNullable attribute can be placed on MetaFields for null specification",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && ATTR_DB_NULLABLE.equals(child.getName())
        ));

        // VALIDATION: dbNullable must be boolean
        registry.addConstraint(new ValidationConstraint(
            "database.dbNullable.validation",
            "dbNullable must be a boolean value (true/false)",
            (metadata) -> metadata instanceof MetaAttribute && ATTR_DB_NULLABLE.equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        ));

        // === DATABASE DEFAULT VALUE CONSTRAINTS ===

        // PLACEMENT: dbDefault attribute on MetaFields
        registry.addConstraint(new PlacementConstraint(
            "database.dbDefault.placement",
            "dbDefault attribute can be placed on MetaFields for default value specification",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && ATTR_DB_DEFAULT.equals(child.getName())
        ));

        // VALIDATION: dbDefault length limit
        registry.addConstraint(new ValidationConstraint(
            "database.dbDefault.validation",
            "dbDefault value must be within 255 character limit",
            (metadata) -> metadata instanceof MetaAttribute && ATTR_DB_DEFAULT.equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                return value.toString().length() <= 255;
            }
        ));
    }

    private void addTypeAndSchemaConstraints(ConstraintRegistry registry) {
        // === DATABASE TYPE CONSTRAINTS ===

        // PLACEMENT: dbType attribute on MetaFields
        registry.addConstraint(new PlacementConstraint(
            "database.dbType.placement",
            "dbType attribute can be placed on MetaFields for SQL type specification",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && ATTR_DB_TYPE.equals(child.getName())
        ));

        // VALIDATION: dbType must be valid SQL data type
        registry.addConstraint(new ValidationConstraint(
            "database.dbType.validation",
            "dbType must be a valid SQL data type",
            (metadata) -> metadata instanceof MetaAttribute && ATTR_DB_TYPE.equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String dbType = value.toString().toUpperCase().trim();
                return isValidSqlDataType(dbType);
            }
        ));

        // === DATABASE LENGTH CONSTRAINTS ===

        // PLACEMENT: dbLength attribute on MetaFields
        registry.addConstraint(new PlacementConstraint(
            "database.dbLength.placement",
            "dbLength attribute can be placed on MetaFields for field length specification",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && ATTR_DB_LENGTH.equals(child.getName())
        ));

        // VALIDATION: dbLength must be within valid range
        registry.addConstraint(new ValidationConstraint(
            "database.dbLength.validation",
            "dbLength must be within valid range (1-65535)",
            (metadata) -> metadata instanceof MetaAttribute && ATTR_DB_LENGTH.equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                try {
                    int length = Integer.parseInt(value.toString());
                    return length >= 1 && length <= 65535;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        ));

        // === DATABASE PRECISION/SCALE CONSTRAINTS ===

        // PLACEMENT: dbPrecision attribute on MetaFields
        registry.addConstraint(new PlacementConstraint(
            "database.dbPrecision.placement",
            "dbPrecision attribute can be placed on MetaFields for decimal precision",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && ATTR_DB_PRECISION.equals(child.getName())
        ));

        // VALIDATION: dbPrecision must be within valid range
        registry.addConstraint(new ValidationConstraint(
            "database.dbPrecision.validation",
            "dbPrecision must be within valid range (1-65)",
            (metadata) -> metadata instanceof MetaAttribute && ATTR_DB_PRECISION.equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                try {
                    int precision = Integer.parseInt(value.toString());
                    return precision >= 1 && precision <= 65;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        ));

        // PLACEMENT: dbScale attribute on MetaFields
        registry.addConstraint(new PlacementConstraint(
            "database.dbScale.placement",
            "dbScale attribute can be placed on MetaFields for decimal scale",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && ATTR_DB_SCALE.equals(child.getName())
        ));

        // VALIDATION: dbScale must be within valid range
        registry.addConstraint(new ValidationConstraint(
            "database.dbScale.validation",
            "dbScale must be within valid range (0-30)",
            (metadata) -> metadata instanceof MetaAttribute && ATTR_DB_SCALE.equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                try {
                    int scale = Integer.parseInt(value.toString());
                    return scale >= 0 && scale <= 30;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        ));
    }

    private void addConstraintAndIndexConstraints(ConstraintRegistry registry) {
        // === PRIMARY KEY CONSTRAINTS ===

        // PLACEMENT: dbPrimaryKey attribute on MetaFields
        registry.addConstraint(new PlacementConstraint(
            "database.dbPrimaryKey.placement",
            "dbPrimaryKey attribute can be placed on MetaFields for primary key specification",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && ATTR_DB_PRIMARY_KEY.equals(child.getName())
        ));

        // VALIDATION: dbPrimaryKey must be boolean
        registry.addConstraint(new ValidationConstraint(
            "database.dbPrimaryKey.validation",
            "dbPrimaryKey must be a boolean value (true/false)",
            (metadata) -> metadata instanceof MetaAttribute && ATTR_DB_PRIMARY_KEY.equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        ));

        // === INDEX CONSTRAINTS ===

        // PLACEMENT: isIndex attribute on MetaFields
        registry.addConstraint(new PlacementConstraint(
            "database.isIndex.placement",
            "isIndex attribute can be placed on MetaFields for database index creation",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && ATTR_IS_INDEX.equals(child.getName())
        ));

        // VALIDATION: isIndex must be boolean
        registry.addConstraint(new ValidationConstraint(
            "database.isIndex.validation",
            "isIndex must be a boolean value (true/false)",
            (metadata) -> metadata instanceof MetaAttribute && ATTR_IS_INDEX.equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        ));

        // === UNIQUE CONSTRAINTS ===

        // PLACEMENT: isUnique attribute on MetaFields
        registry.addConstraint(new PlacementConstraint(
            "database.isUnique.placement",
            "isUnique attribute can be placed on MetaFields for unique constraint",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && ATTR_IS_UNIQUE.equals(child.getName())
        ));

        // VALIDATION: isUnique must be boolean
        registry.addConstraint(new ValidationConstraint(
            "database.isUnique.validation",
            "isUnique must be a boolean value (true/false)",
            (metadata) -> metadata instanceof MetaAttribute && ATTR_IS_UNIQUE.equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        ));
    }

    private void addOMDBOperationalConstraints(ConstraintRegistry registry) {
        // === VIEW-ONLY CONSTRAINTS ===

        // PLACEMENT: isViewOnly attribute on MetaFields
        registry.addConstraint(new PlacementConstraint(
            "database.isViewOnly.placement",
            "isViewOnly attribute can be placed on MetaFields for read-only fields",
            (parent) -> parent instanceof MetaField,
            (child) -> child instanceof MetaAttribute && ATTR_IS_VIEW_ONLY.equals(child.getName())
        ));

        // VALIDATION: isViewOnly must be boolean
        registry.addConstraint(new ValidationConstraint(
            "database.isViewOnly.validation",
            "isViewOnly must be a boolean value (true/false)",
            (metadata) -> metadata instanceof MetaAttribute && ATTR_IS_VIEW_ONLY.equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        ));

        // === DIRTY WRITE CONSTRAINTS ===

        // PLACEMENT: allowDirtyWrite attribute on MetaObjects
        registry.addConstraint(new PlacementConstraint(
            "database.allowDirtyWrite.placement",
            "allowDirtyWrite attribute can be placed on MetaObjects for dirty write control",
            (parent) -> parent instanceof MetaObject,
            (child) -> child instanceof MetaAttribute && ATTR_ALLOW_DIRTY_WRITE.equals(child.getName())
        ));

        // VALIDATION: allowDirtyWrite must be boolean
        registry.addConstraint(new ValidationConstraint(
            "database.allowDirtyWrite.validation",
            "allowDirtyWrite must be a boolean value (true/false)",
            (metadata) -> metadata instanceof MetaAttribute && ATTR_ALLOW_DIRTY_WRITE.equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                String boolValue = value.toString().toLowerCase().trim();
                return "true".equals(boolValue) || "false".equals(boolValue);
            }
        ));

        // === DATABASE INHERITANCE CONSTRAINTS ===

        // PLACEMENT: dbInheritance attribute on MetaObjects
        registry.addConstraint(new PlacementConstraint(
            "database.dbInheritance.placement",
            "dbInheritance attribute can be placed on MetaObjects for inheritance mapping",
            (parent) -> parent instanceof MetaObject,
            (child) -> child instanceof MetaAttribute && ATTR_DB_INHERITANCE.equals(child.getName())
        ));

        // VALIDATION: dbInheritance value length limit
        registry.addConstraint(new ValidationConstraint(
            "database.dbInheritance.validation",
            "dbInheritance value must be within reasonable length (max 500 chars)",
            (metadata) -> metadata instanceof MetaAttribute && ATTR_DB_INHERITANCE.equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return true; // Optional
                return value.toString().length() <= 500; // Reasonable limit for inheritance config
            }
        ));
    }

    // === VALIDATION HELPER METHODS ===

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
        return 1000; // Standard priority for shared database constraints
    }

    @Override
    public String getDescription() {
        return "Shared database constraints for ORM mapping, SQL generation, and JPA code generation";
    }
}