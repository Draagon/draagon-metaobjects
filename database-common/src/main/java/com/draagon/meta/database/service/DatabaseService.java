package com.draagon.meta.database.service;

import com.draagon.meta.registry.MetaDataRegistry;

/**
 * Database service class that extends MetaData types with database-specific attributes.
 *
 * <p>This service adds attributes needed for database ORM mapping and SQL generation to existing
 * MetaData types. All attribute names are defined as constants for type safety
 * and consistency across the codebase.</p>
 *
 * <h3>Database Attributes:</h3>
 * <ul>
 * <li><strong>DB_TABLE:</strong> Database table name for objects</li>
 * <li><strong>DB_COLUMN:</strong> Database column name for fields</li>
 * <li><strong>DB_NULLABLE:</strong> Whether column allows NULL values</li>
 * <li><strong>DB_PRIMARY_KEY:</strong> Whether field is primary key</li>
 * <li><strong>DB_FOREIGN_KEY:</strong> Foreign key relationship</li>
 * <li><strong>DB_INDEX:</strong> Database index information</li>
 * <li><strong>DB_UNIQUE:</strong> Unique constraint</li>
 * <li><strong>DB_DATA_TYPE:</strong> Specific database data type</li>
 * </ul>
 *
 * @since 6.0.0
 */
public class DatabaseService {

    // Table-Level Database Attributes
    public static final String DB_TABLE = "dbTable";
    public static final String DB_SCHEMA = "dbSchema";
    public static final String DB_CATALOG = "dbCatalog";
    public static final String DB_TABLE_SPACE = "dbTableSpace";

    // Column-Level Database Attributes
    public static final String DB_COLUMN = "dbColumn";
    public static final String DB_DATA_TYPE = "dbDataType";
    public static final String DB_NULLABLE = "dbNullable";
    public static final String DB_DEFAULT_VALUE = "dbDefaultValue";

    // Constraint Attributes
    public static final String DB_PRIMARY_KEY = "dbPrimaryKey";
    public static final String DB_FOREIGN_KEY = "dbForeignKey";
    public static final String DB_UNIQUE = "dbUnique";
    public static final String DB_CHECK_CONSTRAINT = "dbCheckConstraint";

    // Index Attributes
    public static final String DB_INDEX = "dbIndex";
    public static final String DB_INDEX_NAME = "dbIndexName";
    public static final String DB_INDEX_TYPE = "dbIndexType";
    public static final String DB_INDEX_UNIQUE = "dbIndexUnique";

    // Size and Precision Attributes
    public static final String DB_LENGTH = "dbLength";
    public static final String DB_PRECISION = "dbPrecision";
    public static final String DB_SCALE = "dbScale";
    public static final String DB_MAX_LENGTH = "dbMaxLength";

    // Performance Attributes
    public static final String DB_SEARCHABLE = "dbSearchable";
    public static final String DB_SORTABLE = "dbSortable";
    public static final String DB_CACHEABLE = "dbCacheable";
    public static final String DB_PARTITION_KEY = "dbPartitionKey";

    // Audit and Tracking Attributes
    public static final String DB_AUDIT_TRAIL = "dbAuditTrail";
    public static final String DB_VERSION_CONTROL = "dbVersionControl";
    public static final String DB_CREATED_DATE = "dbCreatedDate";
    public static final String DB_MODIFIED_DATE = "dbModifiedDate";

    // Legacy and Migration Attributes
    public static final String DB_LEGACY_COLUMN = "dbLegacyColumn";
    public static final String DB_MIGRATION_STATUS = "dbMigrationStatus";
    public static final String DB_DEPRECATED = "dbDeprecated";

    // Security Attributes
    public static final String DB_ENCRYPTED = "dbEncrypted";
    public static final String DB_SENSITIVE_DATA = "dbSensitiveData";
    public static final String DB_ACCESS_LEVEL = "dbAccessLevel";

    /**
     * Register Database-specific type extensions with the MetaData registry.
     *
     * <p>This method extends existing MetaData types with attributes needed
     * for database operations and ORM mapping. It follows the extension pattern of finding
     * existing types and adding optional attributes.</p>
     *
     * @param registry The MetaData registry to extend
     */
    public static void registerTypeExtensions(MetaDataRegistry registry) {
        try {
            // Extend field types for database column mapping
            registerFieldExtensions(registry);

            // Extend object types for database table mapping
            registerObjectExtensions(registry);

            // Extend key types for database constraint mapping
            registerKeyExtensions(registry);

        } catch (Exception e) {
            // Log error but don't fail - service provider pattern should be resilient
            System.err.println("Warning: Failed to register Database type extensions: " + e.getMessage());
        }
    }

    /**
     * Extend field types with database column attributes.
     */
    private static void registerFieldExtensions(MetaDataRegistry registry) {
        // String fields get database string column attributes
        registry.findType("field", "string")
            .optionalAttribute(DB_COLUMN, "string")
            .optionalAttribute(DB_DATA_TYPE, "string")
            .optionalAttribute(DB_NULLABLE, "boolean")
            .optionalAttribute(DB_LENGTH, "int")
            .optionalAttribute(DB_MAX_LENGTH, "int")
            .optionalAttribute(DB_DEFAULT_VALUE, "string")
            .optionalAttribute(DB_UNIQUE, "boolean")
            .optionalAttribute(DB_INDEX, "boolean")
            .optionalAttribute(DB_SEARCHABLE, "boolean")
            .optionalAttribute(DB_ENCRYPTED, "boolean")
            .optionalAttribute(DB_SENSITIVE_DATA, "boolean");

        // Numeric fields get database numeric column attributes
        registry.findType("field", "int")
            .optionalAttribute(DB_COLUMN, "string")
            .optionalAttribute(DB_DATA_TYPE, "string")
            .optionalAttribute(DB_NULLABLE, "boolean")
            .optionalAttribute(DB_DEFAULT_VALUE, "string")
            .optionalAttribute(DB_PRIMARY_KEY, "boolean")
            .optionalAttribute(DB_FOREIGN_KEY, "string")
            .optionalAttribute(DB_UNIQUE, "boolean")
            .optionalAttribute(DB_INDEX, "boolean")
            .optionalAttribute(DB_CHECK_CONSTRAINT, "string");

        registry.findType("field", "long")
            .optionalAttribute(DB_COLUMN, "string")
            .optionalAttribute(DB_DATA_TYPE, "string")
            .optionalAttribute(DB_NULLABLE, "boolean")
            .optionalAttribute(DB_DEFAULT_VALUE, "string")
            .optionalAttribute(DB_PRIMARY_KEY, "boolean")
            .optionalAttribute(DB_FOREIGN_KEY, "string")
            .optionalAttribute(DB_UNIQUE, "boolean")
            .optionalAttribute(DB_INDEX, "boolean")
            .optionalAttribute(DB_CHECK_CONSTRAINT, "string");

        registry.findType("field", "double")
            .optionalAttribute(DB_COLUMN, "string")
            .optionalAttribute(DB_DATA_TYPE, "string")
            .optionalAttribute(DB_NULLABLE, "boolean")
            .optionalAttribute(DB_PRECISION, "int")
            .optionalAttribute(DB_SCALE, "int")
            .optionalAttribute(DB_DEFAULT_VALUE, "string")
            .optionalAttribute(DB_CHECK_CONSTRAINT, "string")
            .optionalAttribute(DB_INDEX, "boolean");

        // Date fields get database date/time column attributes
        registry.findType("field", "date")
            .optionalAttribute(DB_COLUMN, "string")
            .optionalAttribute(DB_DATA_TYPE, "string")
            .optionalAttribute(DB_NULLABLE, "boolean")
            .optionalAttribute(DB_DEFAULT_VALUE, "string")
            .optionalAttribute(DB_INDEX, "boolean")
            .optionalAttribute(DB_AUDIT_TRAIL, "boolean")
            .optionalAttribute(DB_CREATED_DATE, "boolean")
            .optionalAttribute(DB_MODIFIED_DATE, "boolean");

        // Boolean fields get database boolean column attributes
        registry.findType("field", "boolean")
            .optionalAttribute(DB_COLUMN, "string")
            .optionalAttribute(DB_DATA_TYPE, "string")
            .optionalAttribute(DB_NULLABLE, "boolean")
            .optionalAttribute(DB_DEFAULT_VALUE, "string")
            .optionalAttribute(DB_INDEX, "boolean");
    }

    /**
     * Extend object types with database table attributes.
     */
    private static void registerObjectExtensions(MetaDataRegistry registry) {
        registry.findType("object", "pojo")
            .optionalAttribute(DB_TABLE, "string")
            .optionalAttribute(DB_SCHEMA, "string")
            .optionalAttribute(DB_CATALOG, "string")
            .optionalAttribute(DB_TABLE_SPACE, "string")
            .optionalAttribute(DB_AUDIT_TRAIL, "boolean")
            .optionalAttribute(DB_VERSION_CONTROL, "boolean")
            .optionalAttribute(DB_PARTITION_KEY, "string")
            .optionalAttribute(DB_CACHEABLE, "boolean")
            .optionalAttribute(DB_ACCESS_LEVEL, "string")
            .optionalAttribute(DB_MIGRATION_STATUS, "string");

        registry.findType("object", "proxy")
            .optionalAttribute(DB_TABLE, "string")
            .optionalAttribute(DB_SCHEMA, "string")
            .optionalAttribute(DB_ACCESS_LEVEL, "string");

        registry.findType("object", "map")
            .optionalAttribute(DB_TABLE, "string")
            .optionalAttribute(DB_SCHEMA, "string")
            .optionalAttribute(DB_CACHEABLE, "boolean");
    }

    /**
     * Extend key types with database constraint attributes.
     */
    private static void registerKeyExtensions(MetaDataRegistry registry) {
        registry.findType("key", "primary")
            .optionalAttribute(DB_INDEX_NAME, "string")
            .optionalAttribute(DB_INDEX_TYPE, "string")
            .optionalAttribute(DB_TABLE_SPACE, "string");

        registry.findType("key", "foreign")
            .optionalAttribute(DB_INDEX_NAME, "string")
            .optionalAttribute(DB_INDEX_TYPE, "string")
            .optionalAttribute(DB_CHECK_CONSTRAINT, "string");

        registry.findType("key", "secondary")
            .optionalAttribute(DB_INDEX_NAME, "string")
            .optionalAttribute(DB_INDEX_TYPE, "string")
            .optionalAttribute(DB_INDEX_UNIQUE, "boolean")
            .optionalAttribute(DB_TABLE_SPACE, "string");
    }

    /**
     * Check if an attribute name is database-related.
     *
     * @param attributeName The attribute name to check
     * @return True if the attribute is database-related
     */
    public static boolean isDatabaseAttribute(String attributeName) {
        return attributeName != null && attributeName.startsWith("db");
    }

    /**
     * Get standard database data types for different field types.
     *
     * @param fieldType The MetaData field type
     * @return Suggested database data type, or null if none applies
     */
    public static String getStandardDatabaseType(String fieldType) {
        switch (fieldType) {
            case "string":
                return "VARCHAR";
            case "int":
                return "INTEGER";
            case "long":
                return "BIGINT";
            case "double":
                return "DECIMAL";
            case "date":
                return "TIMESTAMP";
            case "boolean":
                return "BOOLEAN";
            default:
                return null;
        }
    }

    /**
     * Get standard database access levels.
     *
     * @return Array of standard access levels
     */
    public static String[] getStandardAccessLevels() {
        return new String[]{"public", "protected", "private", "restricted", "classified"};
    }
}