/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.database.common;

import com.draagon.meta.constraint.ConstraintProvider;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;

import java.util.Set;

import static com.draagon.meta.database.common.DatabaseAttributeConstants.*;

/**
 * Shared database constraint provider for database-related VALUE validation.
 *
 * <p>v6.2.0 UPDATE: This provider now focuses ONLY on VALUE validation constraints.
 * STRUCTURAL placement constraints have been moved to the bidirectional constraint
 * system in core attribute types (StringAttribute, BooleanAttribute, IntAttribute).</p>
 *
 * <p>This provider consolidates database VALUE validation that applies across:</p>
 * <ul>
 *   <li><strong>ORM Mapping</strong> - Object-relational mapping for persistence</li>
 *   <li><strong>SQL Generation</strong> - Database schema and query generation</li>
 *   <li><strong>JPA Code Generation</strong> - JPA entity and annotation generation</li>
 *   <li><strong>Database Schema Management</strong> - DDL and database administration</li>
 * </ul>
 *
 * <p><strong>Architectural Principle:</strong> Database attributes are cross-cutting concerns
 * that apply to multiple modules. This provider ensures consistent VALUE validation
 * across all database-aware components.</p>
 */
public class DatabaseConstraintProvider implements ConstraintProvider {

    @Override
    public void registerConstraints(ConstraintRegistry registry) {
        // STRUCTURAL PLACEMENT CONSTRAINTS - Database module declares what it can contribute
        addDatabaseAttributePlacementConstraints(registry);

        // VALUE VALIDATION CONSTRAINTS - Validate database attribute values
        addTableLevelValidationConstraints(registry);
        addFieldLevelValidationConstraints(registry);
        addTypeAndSchemaValidationConstraints(registry);
        addConstraintAndIndexValidationConstraints(registry);
        addOMDBOperationalValidationConstraints(registry);
    }

    private void addDatabaseAttributePlacementConstraints(ConstraintRegistry registry) {
        // === DATABASE STRUCTURAL PLACEMENT CONSTRAINTS ===
        // Database module declares what database attributes can be placed on core types
        // This maintains separation of concerns - core types don't know about database

        // STRING DATABASE ATTRIBUTES on ALL FIELD TYPES
        registry.addConstraint(new PlacementConstraint(
            "database.field.dbColumn.placement",
            "All field types can have dbColumn string attribute",
            (metadata) -> metadata instanceof MetaField,
            (child) -> child instanceof MetaAttribute &&
                      "attr".equals(child.getType()) &&
                      "string".equals(child.getSubType()) &&
                      ATTR_DB_COLUMN.equals(child.getName())
        ));

        registry.addConstraint(new PlacementConstraint(
            "database.field.dbType.placement",
            "All field types can have dbType string attribute",
            (metadata) -> metadata instanceof MetaField,
            (child) -> child instanceof MetaAttribute &&
                      "attr".equals(child.getType()) &&
                      "string".equals(child.getSubType()) &&
                      ATTR_DB_TYPE.equals(child.getName())
        ));

        registry.addConstraint(new PlacementConstraint(
            "database.field.dbDefault.placement",
            "All field types can have dbDefault string attribute",
            (metadata) -> metadata instanceof MetaField,
            (child) -> child instanceof MetaAttribute &&
                      "attr".equals(child.getType()) &&
                      "string".equals(child.getSubType()) &&
                      ATTR_DB_DEFAULT.equals(child.getName())
        ));

        // BOOLEAN DATABASE ATTRIBUTES on ALL FIELD TYPES
        registry.addConstraint(new PlacementConstraint(
            "database.field.dbNullable.placement",
            "All field types can have dbNullable boolean attribute",
            (metadata) -> metadata instanceof MetaField,
            (child) -> child instanceof MetaAttribute &&
                      "attr".equals(child.getType()) &&
                      "boolean".equals(child.getSubType()) &&
                      ATTR_DB_NULLABLE.equals(child.getName())
        ));

        registry.addConstraint(new PlacementConstraint(
            "database.field.dbPrimaryKey.placement",
            "All field types can have dbPrimaryKey boolean attribute",
            (metadata) -> metadata instanceof MetaField,
            (child) -> child instanceof MetaAttribute &&
                      "attr".equals(child.getType()) &&
                      "boolean".equals(child.getSubType()) &&
                      ATTR_DB_PRIMARY_KEY.equals(child.getName())
        ));

        // INTEGER DATABASE ATTRIBUTES on ALL FIELD TYPES (numeric precision/scale)
        registry.addConstraint(new PlacementConstraint(
            "database.field.dbLength.placement",
            "All field types can have dbLength int attribute",
            (metadata) -> metadata instanceof MetaField,
            (child) -> child instanceof MetaAttribute &&
                      "attr".equals(child.getType()) &&
                      "int".equals(child.getSubType()) &&
                      ATTR_DB_LENGTH.equals(child.getName())
        ));

        registry.addConstraint(new PlacementConstraint(
            "database.field.dbPrecision.placement",
            "All field types can have dbPrecision int attribute",
            (metadata) -> metadata instanceof MetaField,
            (child) -> child instanceof MetaAttribute &&
                      "attr".equals(child.getType()) &&
                      "int".equals(child.getSubType()) &&
                      ATTR_DB_PRECISION.equals(child.getName())
        ));

        registry.addConstraint(new PlacementConstraint(
            "database.field.dbScale.placement",
            "All field types can have dbScale int attribute",
            (metadata) -> metadata instanceof MetaField,
            (child) -> child instanceof MetaAttribute &&
                      "attr".equals(child.getType()) &&
                      "int".equals(child.getSubType()) &&
                      ATTR_DB_SCALE.equals(child.getName())
        ));

        // STRING DATABASE ATTRIBUTES on ALL OBJECT TYPES
        registry.addConstraint(new PlacementConstraint(
            "database.object.dbTable.placement",
            "All object types can have dbTable string attribute",
            (metadata) -> metadata instanceof MetaObject,
            (child) -> child instanceof MetaAttribute &&
                      "attr".equals(child.getType()) &&
                      "string".equals(child.getSubType()) &&
                      ATTR_DB_TABLE.equals(child.getName())
        ));

        registry.addConstraint(new PlacementConstraint(
            "database.object.dbSchema.placement",
            "All object types can have dbSchema string attribute",
            (metadata) -> metadata instanceof MetaObject,
            (child) -> child instanceof MetaAttribute &&
                      "attr".equals(child.getType()) &&
                      "string".equals(child.getSubType()) &&
                      ATTR_DB_SCHEMA.equals(child.getName())
        ));

        // FOREIGN KEY DATABASE ATTRIBUTES on STRING FIELDS
        registry.addConstraint(new PlacementConstraint(
            "database.field.dbForeignTable.placement",
            "String fields can have dbForeignTable string attribute",
            (metadata) -> metadata instanceof MetaField && "string".equals(((MetaField) metadata).getSubType()),
            (child) -> child instanceof MetaAttribute &&
                      "attr".equals(child.getType()) &&
                      "string".equals(child.getSubType()) &&
                      ATTR_DB_FOREIGN_TABLE.equals(child.getName())
        ));

        registry.addConstraint(new PlacementConstraint(
            "database.field.dbForeignColumn.placement",
            "String fields can have dbForeignColumn string attribute",
            (metadata) -> metadata instanceof MetaField && "string".equals(((MetaField) metadata).getSubType()),
            (child) -> child instanceof MetaAttribute &&
                      "attr".equals(child.getType()) &&
                      "string".equals(child.getSubType()) &&
                      ATTR_DB_FOREIGN_COLUMN.equals(child.getName())
        ));
    }

    private void addTableLevelValidationConstraints(ConstraintRegistry registry) {
        // === DATABASE TABLE VALUE VALIDATION CONSTRAINTS ===
        // Note: Placement constraints moved to bidirectional constraint system in core attribute types

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

    private void addFieldLevelValidationConstraints(ConstraintRegistry registry) {
        // === DATABASE COLUMN VALUE VALIDATION CONSTRAINTS ===

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

    private void addTypeAndSchemaValidationConstraints(ConstraintRegistry registry) {
        // === DATABASE TYPE VALUE VALIDATION CONSTRAINTS ===

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

    private void addConstraintAndIndexValidationConstraints(ConstraintRegistry registry) {
        // === PRIMARY KEY VALUE VALIDATION CONSTRAINTS ===

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

    private void addOMDBOperationalValidationConstraints(ConstraintRegistry registry) {
        // === VIEW-ONLY VALUE VALIDATION CONSTRAINTS ===

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
        return "Shared database VALUE validation constraints for ORM mapping, SQL generation, and JPA code generation (v6.2.0: structural constraints moved to bidirectional system)";
    }
}