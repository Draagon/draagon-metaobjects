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
 * A Boolean Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class BooleanField extends PrimitiveField<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(BooleanField.class);

    public final static String SUBTYPE_BOOLEAN = "boolean";

    public BooleanField(String name ) {
        super( SUBTYPE_BOOLEAN, name, DataTypes.BOOLEAN );
    }

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.registerType(BooleanField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_BOOLEAN)
                .description("Boolean field for true/false values")
                
                // COMMON FIELD ATTRIBUTES
                .optionalAttribute("isAbstract", "string")
                .optionalAttribute("validation", "string")
                .optionalAttribute("required", "string")
                .optionalAttribute("defaultValue", "string")
                .optionalAttribute("defaultView", "string")
                
                // ACCEPTS VALIDATORS
                .optionalChild("validator", "*")
                
                // ACCEPTS COMMON ATTRIBUTES
                .optionalChild("attr", "string")
                .optionalChild("attr", "int")
                .optionalChild("attr", "boolean")
            );
            
            log.debug("Registered BooleanField type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register BooleanField type with unified registry", e);
        }
    }

    /**
     * Manually Create a Boolean Filed
     * @param name Name of the field
     * @param defaultValue Default value for the field
     * @return New BooleanField
     */
    public static BooleanField create( String name, Boolean defaultValue ) {
        BooleanField f = new BooleanField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(StringAttribute.create( ATTR_DEFAULT_VALUE, defaultValue.toString() ));
        }
        return f;
    }
}
