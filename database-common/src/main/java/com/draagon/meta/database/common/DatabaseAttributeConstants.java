/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.database.common;

/**
 * Database attribute constants shared across OMDB and code generation modules.
 *
 * <p>This class consolidates all database-related attribute names used by:
 * <ul>
 *   <li><strong>OMDB Module</strong> - Object Manager Database for ORM mapping and SQL generation</li>
 *   <li><strong>Codegen Modules</strong> - JPA entity generation and database-aware code generation</li>
 *   <li><strong>Other Database Plugins</strong> - Custom database integrations and extensions</li>
 * </ul>
 *
 * <p><strong>Architectural Principle:</strong> These constants provide the single source of truth
 * for database attribute names, preventing duplication and ensuring consistency across modules
 * that share database concerns.</p>
 *
 * @since 5.2.0
 */
public final class DatabaseAttributeConstants {

    private DatabaseAttributeConstants() {
        // Utility class - no instantiation
    }

    // === TABLE-LEVEL DATABASE ATTRIBUTES ===

    /** Database table name attribute for MetaObjects */
    public static final String ATTR_DB_TABLE = "dbTable";

    /** Database schema name attribute for MetaObjects */
    public static final String ATTR_DB_SCHEMA = "dbSchema";

    /** Database view name attribute for MetaObjects */
    public static final String ATTR_DB_VIEW = "dbView";

    /** Database view SQL attribute for MetaObjects */
    public static final String ATTR_DB_VIEW_SQL = "dbViewSQL";

    /** Database inheritance strategy attribute for MetaObjects */
    public static final String ATTR_DB_INHERITANCE = "dbInheritance";

    // === FIELD-LEVEL DATABASE ATTRIBUTES ===

    /** Database column name attribute for MetaFields */
    public static final String ATTR_DB_COLUMN = "dbColumn";

    /** Database column type attribute for MetaFields */
    public static final String ATTR_DB_TYPE = "dbType";

    /** Database nullable specification attribute for MetaFields */
    public static final String ATTR_DB_NULLABLE = "dbNullable";

    /** Database column length attribute for MetaFields */
    public static final String ATTR_DB_LENGTH = "dbLength";

    /** Database precision attribute for numeric MetaFields */
    public static final String ATTR_DB_PRECISION = "dbPrecision";

    /** Database scale attribute for numeric MetaFields */
    public static final String ATTR_DB_SCALE = "dbScale";

    /** Database default value attribute for MetaFields */
    public static final String ATTR_DB_DEFAULT = "dbDefault";

    // === DATABASE CONSTRAINT ATTRIBUTES ===

    /** Database primary key marker attribute for MetaFields */
    public static final String ATTR_DB_PRIMARY_KEY = "dbPrimaryKey";

    /** Database foreign key reference attribute for MetaFields */
    public static final String ATTR_DB_FOREIGN_KEY = "dbForeignKey";

    /** Database foreign table reference attribute for MetaFields */
    public static final String ATTR_DB_FOREIGN_TABLE = "dbForeignTable";

    /** Database foreign column reference attribute for MetaFields */
    public static final String ATTR_DB_FOREIGN_COLUMN = "dbForeignColumn";

    /** Database index marker attribute for MetaFields */
    public static final String ATTR_IS_INDEX = "isIndex";

    /** Database unique constraint marker attribute for MetaFields */
    public static final String ATTR_IS_UNIQUE = "isUnique";

    // === OMDB-SPECIFIC ATTRIBUTES ===

    /** Read-only field marker for OMDB views */
    public static final String ATTR_IS_VIEW_ONLY = "isViewOnly";

    /** Allow dirty write marker for OMDB objects */
    public static final String ATTR_ALLOW_DIRTY_WRITE = "allowDirtyWrite";

    /** Database sequence name for auto-generated fields */
    public static final String ATTR_DB_SEQUENCE = "dbSequence";

    /** Database trigger name for field processing */
    public static final String ATTR_DB_TRIGGER = "dbTrigger";

    // === HELPER METHODS FOR COMMON OPERATIONS ===

    /**
     * Check if an attribute name is a database-related attribute.
     *
     * @param attributeName the attribute name to check
     * @return true if the attribute is database-related
     */
    public static boolean isDatabaseAttribute(String attributeName) {
        if (attributeName == null) {
            return false;
        }

        return attributeName.startsWith("db") ||
               ATTR_IS_INDEX.equals(attributeName) ||
               ATTR_IS_UNIQUE.equals(attributeName) ||
               ATTR_IS_VIEW_ONLY.equals(attributeName) ||
               ATTR_ALLOW_DIRTY_WRITE.equals(attributeName);
    }

    /**
     * Check if an attribute name is a table-level database attribute.
     *
     * @param attributeName the attribute name to check
     * @return true if the attribute applies to table/object level
     */
    public static boolean isTableLevelAttribute(String attributeName) {
        return ATTR_DB_TABLE.equals(attributeName) ||
               ATTR_DB_SCHEMA.equals(attributeName) ||
               ATTR_DB_VIEW.equals(attributeName) ||
               ATTR_DB_VIEW_SQL.equals(attributeName) ||
               ATTR_DB_INHERITANCE.equals(attributeName) ||
               ATTR_ALLOW_DIRTY_WRITE.equals(attributeName);
    }

    /**
     * Check if an attribute name is a field-level database attribute.
     *
     * @param attributeName the attribute name to check
     * @return true if the attribute applies to field/column level
     */
    public static boolean isFieldLevelAttribute(String attributeName) {
        return ATTR_DB_COLUMN.equals(attributeName) ||
               ATTR_DB_TYPE.equals(attributeName) ||
               ATTR_DB_NULLABLE.equals(attributeName) ||
               ATTR_DB_LENGTH.equals(attributeName) ||
               ATTR_DB_PRECISION.equals(attributeName) ||
               ATTR_DB_SCALE.equals(attributeName) ||
               ATTR_DB_DEFAULT.equals(attributeName) ||
               ATTR_DB_PRIMARY_KEY.equals(attributeName) ||
               ATTR_DB_FOREIGN_KEY.equals(attributeName) ||
               ATTR_DB_FOREIGN_TABLE.equals(attributeName) ||
               ATTR_DB_FOREIGN_COLUMN.equals(attributeName) ||
               ATTR_IS_INDEX.equals(attributeName) ||
               ATTR_IS_UNIQUE.equals(attributeName) ||
               ATTR_IS_VIEW_ONLY.equals(attributeName) ||
               ATTR_DB_SEQUENCE.equals(attributeName) ||
               ATTR_DB_TRIGGER.equals(attributeName);
    }
}