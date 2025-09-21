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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Byte Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
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
            MetaDataRegistry.registerType(ByteField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_BYTE)
                .description("Byte field with numeric validation")
                
                // BYTE-SPECIFIC ATTRIBUTES
                .optionalAttribute(ATTR_MIN_VALUE, "byte")
                .optionalAttribute(ATTR_MAX_VALUE, "byte")
                // Inherits: required, defaultValue, validation, defaultView from MetaField
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
    public static ByteField create( String name, Integer defaultValue ) {
        ByteField f = new ByteField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(StringAttribute.create( ATTR_DEFAULT_VALUE, defaultValue.toString() ));
        }
        return f;
    }
}
