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

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.registerType(StringAttribute.class, def -> def
                .type(TYPE_ATTR).subType(SUBTYPE_STRING)
                .description("String attribute value")

                // INHERIT FROM BASE ATTRIBUTE
                .inheritsFrom(TYPE_ATTR, SUBTYPE_BASE)

                // NO STRING-SPECIFIC ATTRIBUTES (only uses inherited base attributes)
                // Attributes are typically leaf nodes with no children
            );
            
            log.debug("Registered StringAttribute type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register StringAttribute type with unified registry", e);
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
