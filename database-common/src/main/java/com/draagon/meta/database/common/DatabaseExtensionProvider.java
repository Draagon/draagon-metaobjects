/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.database.common;

import com.draagon.meta.registry.ServiceExtensionProvider;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.attr.BooleanAttribute;

import java.util.Set;

import static com.draagon.meta.database.common.DatabaseAttributeConstants.*;

/**
 * Database service extension provider that extends core MetaData types with database-specific capabilities.
 *
 * <p>This provider demonstrates the <strong>Service Extension Pattern</strong> where the database service
 * adds its own attributes to core MetaData types without polluting the core types themselves.</p>
 *
 * <h3>Architectural Benefits:</h3>
 * <ul>
 *   <li><strong>Service Separation:</strong> Database logic stays in database modules</li>
 *   <li><strong>Core Purity:</strong> Core types remain focused on universal concepts</li>
 *   <li><strong>Plugin Extensibility:</strong> Third parties can extend without core changes</li>
 *   <li><strong>Enterprise Adoption:</strong> Companies customize without forking core code</li>
 * </ul>
 *
 * <h3>Database Extensions Added:</h3>
 * <ul>
 *   <li><strong>ALL field types</strong> get database column attributes (dbColumn, dbType, dbNullable, etc.)</li>
 *   <li><strong>ALL object types</strong> get database table attributes (dbTable, dbSchema, dbView, etc.)</li>
 *   <li><strong>Numeric fields</strong> get precision/scale attributes (dbPrecision, dbScale)</li>
 *   <li><strong>String fields</strong> get foreign key attributes (dbForeignTable, dbForeignColumn)</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // After this extension is loaded, core field types can accept database attributes:
 * StringField userNameField = new StringField("userName");
 * userNameField.addAttribute(new StringAttribute("dbColumn", "user_name"));
 * userNameField.addAttribute(new BooleanAttribute("dbNullable", "false"));
 * userNameField.addAttribute(new IntAttribute("dbLength", "100"));
 *
 * // Core object types can accept database table attributes:
 * MetaObject userObject = new MetaObject("User");
 * userObject.addAttribute(new StringAttribute("dbTable", "users"));
 * userObject.addAttribute(new StringAttribute("dbSchema", "public"));
 * }</pre>
 *
 * @since 6.2.0 (Phase 3)
 */
public class DatabaseExtensionProvider implements ServiceExtensionProvider {

    @Override
    public void extendTypes(MetaDataRegistry registry) throws Exception {
        // Extend ALL field types with database attributes
        extendFieldTypesWithDatabaseAttributes(registry);

        // Extend ALL object types with database attributes
        extendObjectTypesWithDatabaseAttributes(registry);

        // Add database-specific validation constraints
        addDatabaseValidationConstraints(registry);
    }

    private void extendFieldTypesWithDatabaseAttributes(MetaDataRegistry registry) {
        // Extend ALL field types with core database column attributes
        registry.extendType(MetaField.class, def -> def
            // STRING DATABASE ATTRIBUTES for columns
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_DB_COLUMN)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_DB_TYPE)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_DB_DEFAULT)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_DB_FOREIGN_KEY)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_DB_FOREIGN_TABLE)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_DB_FOREIGN_COLUMN)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_DB_SEQUENCE)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_DB_TRIGGER)

            // BOOLEAN DATABASE ATTRIBUTES for flags
            .acceptsNamedAttributes(BooleanAttribute.SUBTYPE_BOOLEAN, ATTR_DB_NULLABLE)
            .acceptsNamedAttributes(BooleanAttribute.SUBTYPE_BOOLEAN, ATTR_DB_PRIMARY_KEY)
            .acceptsNamedAttributes(BooleanAttribute.SUBTYPE_BOOLEAN, ATTR_IS_INDEX)
            .acceptsNamedAttributes(BooleanAttribute.SUBTYPE_BOOLEAN, ATTR_IS_UNIQUE)
            .acceptsNamedAttributes(BooleanAttribute.SUBTYPE_BOOLEAN, ATTR_IS_VIEW_ONLY)

            // INTEGER DATABASE ATTRIBUTES for numeric properties
            .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, ATTR_DB_LENGTH)
            .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, ATTR_DB_PRECISION)
            .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, ATTR_DB_SCALE)
        );
    }

    private void extendObjectTypesWithDatabaseAttributes(MetaDataRegistry registry) {
        // Extend ALL object types with database table/schema attributes
        registry.extendType(MetaObject.class, def -> def
            // STRING DATABASE ATTRIBUTES for tables/schemas
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_DB_TABLE)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_DB_SCHEMA)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_DB_VIEW)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_DB_VIEW_SQL)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_DB_INHERITANCE)

            // BOOLEAN DATABASE ATTRIBUTES for operational flags
            .acceptsNamedAttributes(BooleanAttribute.SUBTYPE_BOOLEAN, ATTR_ALLOW_DIRTY_WRITE)
        );
    }

    private void addDatabaseValidationConstraints(MetaDataRegistry registry) {
        // Note: This is where we could add database-specific validation constraints
        // that are different from the ones in DatabaseConstraintProvider.
        // For now, we rely on the existing DatabaseConstraintProvider for validation.

        // Future enhancement: Add service-specific validation rules here
        // Example: Cross-table foreign key validation, schema existence checks, etc.
    }

    @Override
    public String getProviderName() {
        return "database-extensions";
    }

    @Override
    public Set<String> getDependencies() {
        // We depend on core types being loaded first
        return Set.of("core-types", "field-types", "object-types", "attribute-types");
    }

    @Override
    public int getPriority() {
        return 1000; // High priority - core service extension
    }

    @Override
    public boolean supportsCurrentEnvironment() {
        try {
            // Only enable if database classes are available
            Class.forName("com.draagon.meta.database.common.DatabaseAttributeConstants");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Database service extension - adds database attributes to all field and object types " +
               "for ORM mapping, SQL generation, and JPA code generation";
    }
}