package com.draagon.meta.enhancement.providers;

import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.attr.BooleanAttribute;
import com.draagon.meta.enhancement.AttributeDefinition;
import com.draagon.meta.enhancement.MetaDataAttributeProvider;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.field.MetaField;

import java.util.Arrays;
import java.util.Collection;

/**
 * v6.0.0: Provides database-related attributes for ObjectManagerDB and ORM code generators.
 * This replaces the TypesConfig overlay system with service-based attribute provision.
 * 
 * These attributes are shared across ObjectManagerDB and ORM code generators to ensure consistency.
 */
public class DatabaseAttributeProvider implements MetaDataAttributeProvider {
    
    public static final String PROVIDER_ID = "DatabaseAttributes";
    
    // Database attribute names
    public static final String ATTR_DB_TABLE = "dbTable";
    public static final String ATTR_DB_SCHEMA = "dbSchema";  
    public static final String ATTR_DB_VIEW = "dbView";
    public static final String ATTR_DB_COL = "dbCol";
    public static final String ATTR_DB_TYPE = "dbType";
    public static final String ATTR_DB_LENGTH = "dbLength";
    public static final String ATTR_DB_PRECISION = "dbPrecision";
    public static final String ATTR_DB_SCALE = "dbScale";
    public static final String ATTR_DB_NULLABLE = "dbNullable";
    public static final String ATTR_DB_PRIMARY_KEY = "dbPrimaryKey";
    public static final String ATTR_DB_FOREIGN_KEY = "dbForeignKey";
    public static final String ATTR_DB_UNIQUE = "dbUnique";
    public static final String ATTR_DB_INDEX = "dbIndex";
    public static final String ATTR_DB_SEQUENCE = "dbSequence";
    
    @Override
    public Collection<AttributeDefinition> getAttributeDefinitions() {
        return Arrays.asList(
            // MetaObject database attributes
            AttributeDefinition.builder()
                .name(ATTR_DB_TABLE)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("Database table name")
                .applicableTypes(MetaObject.TYPE_OBJECT)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_DB_SCHEMA)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("Database schema name")
                .applicableTypes(MetaObject.TYPE_OBJECT)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_DB_VIEW)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("Database view name")
                .applicableTypes(MetaObject.TYPE_OBJECT)
                .build(),
                
            // MetaField database attributes
            AttributeDefinition.builder()
                .name(ATTR_DB_COL)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("Database column name")
                .applicableTypes(MetaField.TYPE_FIELD)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_DB_TYPE)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("Database column type (VARCHAR, INTEGER, etc.)")
                .applicableTypes(MetaField.TYPE_FIELD)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_DB_NULLABLE)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(BooleanAttribute.SUBTYPE_BOOLEAN)
                .description("Whether database column allows NULL values")
                .applicableTypes(MetaField.TYPE_FIELD)
                .defaultValue(true)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_DB_PRIMARY_KEY)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(BooleanAttribute.SUBTYPE_BOOLEAN)
                .description("Whether field is part of primary key")
                .applicableTypes(MetaField.TYPE_FIELD)
                .defaultValue(false)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_DB_FOREIGN_KEY)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("Foreign key reference (table.column)")
                .applicableTypes(MetaField.TYPE_FIELD)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_DB_UNIQUE)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(BooleanAttribute.SUBTYPE_BOOLEAN)
                .description("Whether field has unique constraint")
                .applicableTypes(MetaField.TYPE_FIELD)
                .defaultValue(false)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_DB_INDEX)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("Database index name for this field")
                .applicableTypes(MetaField.TYPE_FIELD)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_DB_SEQUENCE)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("Database sequence for auto-increment fields")
                .applicableTypes(MetaField.TYPE_FIELD)
                .build()
        );
    }
    
    @Override
    public String getProviderId() {
        return PROVIDER_ID;
    }
    
    @Override
    public int getPriority() {
        return 100; // Database attributes should be loaded early
    }
    
    @Override
    public String getDescription() {
        return "Provides database mapping attributes for ObjectManagerDB and ORM code generators";
    }
}