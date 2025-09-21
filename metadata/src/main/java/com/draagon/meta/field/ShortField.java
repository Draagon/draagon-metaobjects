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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Short Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@MetaDataTypeHandler(type = "field", subType = "short", description = "Short field type with numeric validation")
@SuppressWarnings("serial")
public class ShortField extends PrimitiveField<Short>
{
    private static final Logger log = LoggerFactory.getLogger(ShortField.class);

    public final static String SUBTYPE_SHORT = "short";
    public final static String ATTR_MIN_VALUE = "minValue";
    public final static String ATTR_MAX_VALUE = "maxValue";

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.registerType(ShortField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_SHORT)
                .description("Short field with numeric validation")
                
                // SHORT-SPECIFIC ATTRIBUTES
                .optionalAttribute(ATTR_MIN_VALUE, "short")
                .optionalAttribute(ATTR_MAX_VALUE, "short")
                
                // COMMON FIELD ATTRIBUTES
                .optionalAttribute("isAbstract", "string")
                .optionalAttribute("validation", "string")
                .optionalAttribute("required", "string")
                .optionalAttribute("defaultValue", "string")
                .optionalAttribute("defaultView", "string")
                
                // DATABASE ATTRIBUTES (for database mapping)
                .optionalAttribute("isId", "boolean")
                .optionalAttribute("dbColumn", "string")
                .optionalAttribute("isSearchable", "boolean")
                .optionalAttribute("isOptional", "boolean")
                
                // ACCEPTS VALIDATORS
                .optionalChild("validator", "*")
                
                // ACCEPTS VIEWS
                .optionalChild("view", "*")
                
                // ACCEPTS COMMON ATTRIBUTES
                .optionalChild("attr", "string")
                .optionalChild("attr", "int")
                .optionalChild("attr", "boolean")
            );
            
            log.debug("Registered ShortField type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register ShortField type with unified registry", e);
        }
    }

    public ShortField( String name ) {
        super( SUBTYPE_SHORT, name, DataTypes.SHORT );
    }

    /**
     * Manually Create a ByteField
     * @param name Name of the field
     * @param defaultValue Default value for the field
     * @return New ByteField
     */
    public static ShortField create( String name, Integer defaultValue ) {
        ShortField f = new ShortField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(StringAttribute.create( ATTR_DEFAULT_VALUE, defaultValue.toString() ));
        }
        return f;
    }
}
