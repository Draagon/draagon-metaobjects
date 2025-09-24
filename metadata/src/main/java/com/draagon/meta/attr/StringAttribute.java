/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.attr;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaDataTypeId;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import com.draagon.meta.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.draagon.meta.attr.MetaAttribute.TYPE_ATTR;
import static com.draagon.meta.attr.MetaAttribute.SUBTYPE_BASE;

/**
 * A String Attribute with unified registry registration.
 */
@MetaDataType(type = "attr", subType = "string", description = "String attribute value")
public class StringAttribute extends MetaAttribute<String> {

    private static final Logger log = LoggerFactory.getLogger(StringAttribute.class);

    public final static String SUBTYPE_STRING = "string";

    /**
     * Constructs the String MetaAttribute
     */
    public StringAttribute(String name ) {
        super( SUBTYPE_STRING, name, DataTypes.STRING);
    }

    /**
     * Register attr.string type using Phase 2 standardized pattern.
     *
     * @param registry MetaDataRegistry to register with
     */
    public static void registerTypes(MetaDataRegistry registry) {
        try {
            registry.registerType(StringAttribute.class, def -> def
                .type(TYPE_ATTR).subType(SUBTYPE_STRING)
                .description("String attribute value")

                // INHERIT FROM BASE ATTRIBUTE
                .inheritsFrom(TYPE_ATTR, SUBTYPE_BASE)

                // === CORE STRING ATTRIBUTE PARENT ACCEPTANCES ===
                // String attributes can be used for core field attributes
                .acceptsNamedParents("field", "string", "pattern")         // String field pattern attribute
                .acceptsNamedParents("field", "*", "defaultValue")         // Default value for any field
                .acceptsNamedParents("field", "*", "defaultView")          // Default view for any field

                // String attributes can be used for core object attributes
                .acceptsNamedParents("object", "*", "extends")             // Object inheritance
                .acceptsNamedParents("object", "*", "implements")          // Object interfaces

                // === DATABASE STRING ATTRIBUTE PARENT ACCEPTANCES ===
                // TABLE-LEVEL DATABASE ATTRIBUTES (for MetaObjects)
                .acceptsNamedParents("object", "*", "dbTable")          // Database table name
                .acceptsNamedParents("object", "*", "dbSchema")         // Database schema name
                .acceptsNamedParents("object", "*", "dbView")           // Database view name
                .acceptsNamedParents("object", "*", "dbViewSQL")        // Database view SQL
                .acceptsNamedParents("object", "*", "dbInheritance")    // Database inheritance strategy

                // FIELD-LEVEL DATABASE ATTRIBUTES (for MetaFields)
                .acceptsNamedParents("field", "*", "dbColumn")          // Database column name
                .acceptsNamedParents("field", "*", "dbType")            // Database column type
                .acceptsNamedParents("field", "*", "dbDefault")         // Database default value
                .acceptsNamedParents("field", "*", "dbForeignKey")      // Database foreign key reference
                .acceptsNamedParents("field", "*", "dbSequence")        // Database sequence name
                .acceptsNamedParents("field", "*", "dbTrigger")         // Database trigger name
            );

            log.debug("Registered StringAttribute type using Phase 2 pattern");
        } catch (Exception e) {
            log.error("Failed to register StringAttribute type using Phase 2 pattern", e);
            throw new RuntimeException("StringAttribute type registration failed", e);
        }
    }

    /**
     * Manually create a String MetaAttribute with a value
     */
    public static StringAttribute create(String name, String value ) {
        StringAttribute a = new StringAttribute( name );
        a.setValue( value );
        return a;
    }
}
