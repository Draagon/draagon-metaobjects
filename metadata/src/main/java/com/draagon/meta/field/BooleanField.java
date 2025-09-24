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
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.draagon.meta.field.MetaField.TYPE_FIELD;
import static com.draagon.meta.field.MetaField.SUBTYPE_BASE;

/**
 * A Boolean Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@MetaDataType(type = "field", subType = "boolean", description = "Boolean field for true/false values")
@SuppressWarnings("serial")
public class BooleanField extends PrimitiveField<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(BooleanField.class);

    public final static String SUBTYPE_BOOLEAN = "boolean";

    // Static registration block - automatically registers when class is loaded
    static {
        try {
            registerTypes(MetaDataRegistry.getInstance());
        } catch (Exception e) {
            log.error("Failed to register BooleanField type during class loading", e);
        }
    }

    public BooleanField(String name ) {
        super( SUBTYPE_BOOLEAN, name, DataTypes.BOOLEAN );
    }

    /**
     * Register BooleanField type with the registry
     *
     * @param registry The MetaDataRegistry to register with
     */
    public static void registerTypes(MetaDataRegistry registry) {
        try {
            // Register the type definition
            MetaDataRegistry.getInstance().registerType(BooleanField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_BOOLEAN)
                .description("Boolean field for true/false values")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // NO BOOLEAN-SPECIFIC ATTRIBUTES - inherits all from MetaField base
            );

            log.debug("Registered BooleanField type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register BooleanField type with unified registry", e);
        }
    }

    /**
     * Manually Create a Boolean Field
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
