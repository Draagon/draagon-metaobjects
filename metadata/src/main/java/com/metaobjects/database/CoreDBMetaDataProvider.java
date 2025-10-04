package com.metaobjects.database;

import com.metaobjects.attr.BooleanAttribute;
import com.metaobjects.attr.IntAttribute;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.field.DoubleField;
import com.metaobjects.field.IntegerField;
import com.metaobjects.field.LongField;
import com.metaobjects.field.MetaField;
import com.metaobjects.identity.MetaIdentity;
import com.metaobjects.identity.PrimaryIdentity;
import com.metaobjects.identity.SecondaryIdentity;
import com.metaobjects.object.MetaObject;
import com.metaobjects.object.pojo.PojoMetaObject;
import com.metaobjects.object.pojo.PojoObject;
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

    // Identity-specific database attributes
    public static final String DB_SEQUENCE_NAME = "dbSequenceName";
    public static final String DB_INDEX_NAME = "dbIndexName";
    public static final String DB_TABLESPACE = "dbTablespace";


    @Override
    public String getProviderId() {
        return "database-extensions";
    }

    @Override
    public String[] getDependencies() {
        // Depends on field types, object types, and identity types for extending them
        return new String[]{"field-types", "object-types", "identity-types"};
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
        registry.findType(MetaObject.TYPE_OBJECT, MetaObject.SUBTYPE_BASE)
            .optionalAttribute(DB_TABLE, StringAttribute.SUBTYPE_STRING)
            .optionalAttribute(DB_INDEX, StringAttribute.SUBTYPE_STRING)
            .optionalAttribute(DB_UNIQUE, StringAttribute.SUBTYPE_STRING);

        registry.findType(MetaObject.TYPE_OBJECT, PojoMetaObject.SUBTYPE_POJO)
            .optionalAttribute(DB_TABLE, StringAttribute.SUBTYPE_STRING);

        // Field-level database attributes
        registry.findType(MetaField.TYPE_FIELD, MetaField.SUBTYPE_BASE)
            .optionalAttribute(DB_COLUMN, StringAttribute.SUBTYPE_STRING)
            .optionalAttribute(DB_NULLABLE, BooleanAttribute.SUBTYPE_BOOLEAN)
            .optionalAttribute(DB_FOREIGN_KEY, StringAttribute.SUBTYPE_STRING)
            .optionalAttribute(DB_INDEX, StringAttribute.SUBTYPE_STRING)
            .optionalAttribute(DB_UNIQUE, BooleanAttribute.SUBTYPE_BOOLEAN)
            .optionalAttribute(DB_LENGTH, IntAttribute.SUBTYPE_INT)
            .optionalAttribute(DB_PRECISION, IntAttribute.SUBTYPE_INT)
            .optionalAttribute(DB_SCALE, IntAttribute.SUBTYPE_INT);

        // String field specific
        registry.findType(MetaField.TYPE_FIELD, StringAttribute.SUBTYPE_STRING)
            .optionalAttribute(DB_COLUMN, StringAttribute.SUBTYPE_STRING)
            .optionalAttribute(DB_LENGTH, IntAttribute.SUBTYPE_INT);

        // Numeric field specific
        registry.findType(MetaField.TYPE_FIELD, LongField.SUBTYPE_LONG)
            .optionalAttribute(DB_COLUMN, StringAttribute.SUBTYPE_STRING);

        registry.findType(MetaField.TYPE_FIELD, IntegerField.SUBTYPE_INT)
            .optionalAttribute(DB_COLUMN, StringAttribute.SUBTYPE_STRING);

        registry.findType(MetaField.TYPE_FIELD, DoubleField.SUBTYPE_DOUBLE)
            .optionalAttribute(DB_COLUMN, StringAttribute.SUBTYPE_STRING)
            .optionalAttribute(DB_PRECISION, IntAttribute.SUBTYPE_INT)
            .optionalAttribute(DB_SCALE, IntAttribute.SUBTYPE_INT);

        // Identity-level database attributes (replaces deprecated key attributes)
        registry.findType(MetaIdentity.TYPE_IDENTITY, PrimaryIdentity.SUBTYPE_PRIMARY)
            .optionalAttribute(DB_SEQUENCE_NAME, StringAttribute.SUBTYPE_STRING)
            .optionalAttribute(DB_INDEX_NAME, StringAttribute.SUBTYPE_STRING)
            .optionalAttribute(DB_TABLESPACE, StringAttribute.SUBTYPE_STRING);

        registry.findType(MetaIdentity.TYPE_IDENTITY, SecondaryIdentity.SUBTYPE_SECONDARY)
            .optionalAttribute(DB_INDEX_NAME, StringAttribute.SUBTYPE_STRING)
            .optionalAttribute(DB_TABLESPACE, StringAttribute.SUBTYPE_STRING);
    }
}