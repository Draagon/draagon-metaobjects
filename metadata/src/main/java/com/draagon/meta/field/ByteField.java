/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataTypeHandler;
import com.draagon.meta.util.MetaDataConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.draagon.meta.util.MetaDataConstants.TYPE_FIELD;
import static com.draagon.meta.field.MetaField.SUBTYPE_BASE;

/**
 * A Byte Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@MetaDataTypeHandler(type = "field", subType = "byte", description = "Byte field with numeric validation")
@SuppressWarnings("serial")
public class ByteField extends PrimitiveField<Byte>
{
    private static final Logger log = LoggerFactory.getLogger(ByteField.class);

    public final static String SUBTYPE_BYTE = "byte";
    public final static String ATTR_MIN_VALUE = "minValue";
    public final static String ATTR_MAX_VALUE = "maxValue";

    // Unified registry self-registration
    static {
        try {
            // Explicitly trigger MetaField static initialization first
            try {
                Class.forName(MetaField.class.getName());
                // Add a small delay to ensure MetaField registration completes
                Thread.sleep(1);
            } catch (ClassNotFoundException | InterruptedException e) {
                log.warn("Could not force MetaField class loading", e);
            }

            MetaDataRegistry.registerType(ByteField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_BYTE)
                .description("Byte field with numeric validation")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // BYTE-SPECIFIC ATTRIBUTES ONLY
                .optionalAttribute(ATTR_MIN_VALUE, "byte")
                .optionalAttribute(ATTR_MAX_VALUE, "byte")

                // SERVICE-SPECIFIC ATTRIBUTES (for cross-module compatibility)
                .optionalAttribute(MetaDataConstants.ATTR_IS_ID, "boolean")
                .optionalAttribute(MetaDataConstants.ATTR_DB_COLUMN, "string")
                .optionalAttribute(MetaDataConstants.ATTR_IS_SEARCHABLE, "boolean")
                .optionalAttribute(MetaDataConstants.ATTR_IS_OPTIONAL, "boolean")
            );

            log.debug("Registered ByteField type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register ByteField type with unified registry", e);
        }
    }

    public ByteField(String name) {
        super(SUBTYPE_BYTE, name, DataTypes.BYTE);
    }

    /**
     * Manually Create a ByteField
     * @param name Name of the field
     * @param defaultValue Default value for the field
     * @return New ByteField
     */
    public static ByteField create( String name, Byte defaultValue ) {
        ByteField f = new ByteField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(StringAttribute.create( MetaDataConstants.ATTR_DEFAULT_VALUE, defaultValue.toString() ));
        }
        return f;
    }
}
