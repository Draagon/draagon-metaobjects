/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.generator.mustache;

import com.draagon.meta.registry.ServiceExtensionProvider;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.attr.BooleanAttribute;

import java.util.Set;

import static com.draagon.meta.generator.mustache.JpaConstants.*;

/**
 * JPA service extension provider that extends core MetaData types with JPA-specific code generation capabilities.
 *
 * <p>This provider demonstrates advanced service extension patterns where code generation services
 * add their own attributes to core MetaData types for template-driven code generation.</p>
 *
 * <h3>Architectural Benefits:</h3>
 * <ul>
 *   <li><strong>Code Generation Separation:</strong> JPA logic stays in codegen modules</li>
 *   <li><strong>Template Independence:</strong> Core types don't know about specific code generation</li>
 *   <li><strong>Multiple Target Support:</strong> Enables JPA, Hibernate, Spring Data extensions</li>
 *   <li><strong>Framework Evolution:</strong> New annotation frameworks can be added without core changes</li>
 * </ul>
 *
 * <h3>JPA Extensions Added:</h3>
 * <ul>
 *   <li><strong>ALL field types</strong> get JPA field attributes (jpaColumn, jpaId, jpaLength, etc.)</li>
 *   <li><strong>ALL object types</strong> get JPA entity attributes (jpaTable, hasJpa, jpaCascade, etc.)</li>
 *   <li><strong>Relationship fields</strong> get JPA relationship attributes (jpaFetch, jpaCascade)</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // After this extension is loaded, core field types can accept JPA attributes:
 * StringField userNameField = new StringField("userName");
 * userNameField.addAttribute(new StringAttribute("jpaColumn", "user_name"));
 * userNameField.addAttribute(new BooleanAttribute("jpaId", "false"));
 * userNameField.addAttribute(new IntAttribute("jpaLength", "50"));
 *
 * // Core object types can accept JPA entity attributes:
 * MetaObject userObject = new MetaObject("User");
 * userObject.addAttribute(new StringAttribute("jpaTable", "users"));
 * userObject.addAttribute(new BooleanAttribute("hasJpa", "true"));
 * }</pre>
 *
 * @since 6.2.0 (Phase 3)
 */
public class JpaExtensionProvider implements ServiceExtensionProvider {

    @Override
    public void extendTypes(MetaDataRegistry registry) throws Exception {
        // Extend ALL field types with JPA field attributes
        extendFieldTypesWithJpaAttributes(registry);

        // Extend ALL object types with JPA entity attributes
        extendObjectTypesWithJpaAttributes(registry);

        // Add JPA-specific validation constraints if needed
        addJpaValidationConstraints(registry);
    }

    private void extendFieldTypesWithJpaAttributes(MetaDataRegistry registry) {
        // Extend ALL field types with JPA field-level attributes
        registry.extendType(MetaField.class, def -> def
            // STRING JPA ATTRIBUTES for column mapping
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_JPA_COLUMN)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_JPA_CASCADE)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_JPA_FETCH)

            // BOOLEAN JPA ATTRIBUTES for flags
            .acceptsNamedAttributes(BooleanAttribute.SUBTYPE_BOOLEAN, ATTR_JPA_ID)
            .acceptsNamedAttributes(BooleanAttribute.SUBTYPE_BOOLEAN, ATTR_JPA_NULLABLE)
            .acceptsNamedAttributes(BooleanAttribute.SUBTYPE_BOOLEAN, ATTR_JPA_UNIQUE)

            // INTEGER JPA ATTRIBUTES for numeric properties
            .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, ATTR_JPA_LENGTH)
        );
    }

    private void extendObjectTypesWithJpaAttributes(MetaDataRegistry registry) {
        // Extend ALL object types with JPA entity-level attributes
        registry.extendType(MetaObject.class, def -> def
            // STRING JPA ATTRIBUTES for entity mapping
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_JPA_TABLE)

            // BOOLEAN JPA ATTRIBUTES for generation control
            .acceptsNamedAttributes(BooleanAttribute.SUBTYPE_BOOLEAN, ATTR_HAS_JPA)
        );
    }

    private void addJpaValidationConstraints(MetaDataRegistry registry) {
        // Note: JPA-specific validation constraints could be added here
        // For example: validate JPA table names, column names, cascade types, etc.
        // Currently relying on template validation during code generation

        // Future enhancement: Add JPA-specific validation rules
        // Example: Validate cascade types (ALL, PERSIST, MERGE, etc.)
        // Example: Validate fetch types (EAGER, LAZY)
        // Example: Cross-field validation for JPA relationships
    }

    @Override
    public String getProviderName() {
        return "jpa-extensions";
    }

    @Override
    public Set<String> getDependencies() {
        // JPA extensions depend on core types and database extensions being loaded first
        return Set.of("core-types", "field-types", "object-types", "attribute-types", "database-extensions");
    }

    @Override
    public int getPriority() {
        return 800; // High priority but after database extensions (1000)
    }

    @Override
    public boolean supportsCurrentEnvironment() {
        try {
            // Only enable if JPA constants are available (they should be in this module)
            Class.forName("com.draagon.meta.generator.mustache.JpaConstants");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "JPA service extension - adds JPA code generation attributes to all field and object types " +
               "for template-driven JPA entity and annotation generation";
    }
}