package com.metaobjects.database;

import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.registry.MetaDataTypeProvider;

/**
 * Core database MetaData type provider that registers common database attributes
 * used by both JPA code generation (mustache templates) and ObjectManagerDB.
 *
 * This provider registers database-related attributes that are shared across
 * database functionality rather than being specific to one implementation.
 *
 * Priority: 150 (after core types, before code generation)
 */
public class CoreDBMetaDataProvider implements MetaDataTypeProvider {

    // Common database attribute constants
    public static final String DB_TABLE = "dbTable";
    public static final String DB_COLUMN = "dbColumn";
    public static final String DB_NULLABLE = "dbNullable";
    public static final String DB_PRIMARY_KEY = "dbPrimaryKey";
    public static final String DB_FOREIGN_KEY = "dbForeignKey";
    public static final String DB_INDEX = "dbIndex";
    public static final String DB_UNIQUE = "dbUnique";
    public static final String DB_LENGTH = "dbLength";
    public static final String DB_PRECISION = "dbPrecision";
    public static final String DB_SCALE = "dbScale";
    public static final String DB_AUTO_INCREMENT = "dbAutoIncrement";

    @Override
    public String getProviderId() {
        return "database-extensions";
    }

    @Override
    public String[] getDependencies() {
        // Depends on field types and object types for extending field.base and object.base
        return new String[]{"field-types", "object-types"};
    }

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        registerDatabaseAttributes(registry);
    }

    @Override
    public String getDescription() {
        return "Database MetaData Provider - Common database attributes for JPA and ObjectManager";
    }

    /**
     * Registers common database attributes used by both JPA code generation
     * and ObjectManagerDB implementations.
     */
    public static void registerDatabaseAttributes(MetaDataRegistry registry) {
        // Object-level database attributes
        registry.findType("object", "base")
            .optionalAttribute(DB_TABLE, "string")
            .optionalAttribute(DB_INDEX, "string")
            .optionalAttribute(DB_UNIQUE, "string");

        registry.findType("object", "pojo")
            .optionalAttribute(DB_TABLE, "string");

        // Field-level database attributes
        registry.findType("field", "base")
            .optionalAttribute(DB_COLUMN, "string")
            .optionalAttribute(DB_NULLABLE, "boolean")
            .optionalAttribute(DB_PRIMARY_KEY, "boolean")
            .optionalAttribute(DB_FOREIGN_KEY, "string")
            .optionalAttribute(DB_INDEX, "string")
            .optionalAttribute(DB_UNIQUE, "boolean")
            .optionalAttribute(DB_LENGTH, "int")
            .optionalAttribute(DB_PRECISION, "int")
            .optionalAttribute(DB_SCALE, "int");

        // String field specific
        registry.findType("field", "string")
            .optionalAttribute(DB_COLUMN, "string")
            .optionalAttribute(DB_LENGTH, "int");

        // Numeric field specific
        registry.findType("field", "long")
            .optionalAttribute(DB_COLUMN, "string");

        registry.findType("field", "int")
            .optionalAttribute(DB_COLUMN, "string");

        registry.findType("field", "double")
            .optionalAttribute(DB_COLUMN, "string")
            .optionalAttribute(DB_PRECISION, "int")
            .optionalAttribute(DB_SCALE, "int");

        // Key-level database attributes
        registry.findType("key", "primary")
            .optionalAttribute(DB_AUTO_INCREMENT, "string");
    }
}