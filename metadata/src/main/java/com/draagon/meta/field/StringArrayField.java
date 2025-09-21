/*
 * Copyright 2016 Doug Mealing LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.DataTypes;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.io.string.StringSerializationHandler;
import com.draagon.meta.util.DataConverter;
import com.draagon.meta.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A String Array Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class StringArrayField extends ArrayField<String,List<String>> implements StringSerializationHandler
{
    private static final Logger log = LoggerFactory.getLogger(StringArrayField.class);

    public final static String SUBTYPE_STRING_ARRAY = "stringArray";

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.registerType(StringArrayField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_STRING_ARRAY)
                .description("String array field for lists of string values")
                
                // Inherits: required, defaultValue, validation, defaultView from MetaField
                // Array fields inherit array-specific attributes from ArrayField
            );
            
            log.debug("Registered StringArrayField type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register StringArrayField type with unified registry", e);
        }
    }

    public StringArrayField( String name ) {
        super( SUBTYPE_STRING_ARRAY, name, DataTypes.STRING_ARRAY );
    }

    /**
     * Manually Create a StringArrayField
     * @param name Name of the field
     * @param defaultValue Default value for the field
     * @return New StringArrayField
     */
    public static StringArrayField create( String name, String defaultValue ) {
        StringArrayField f = new StringArrayField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(StringAttribute.create( ATTR_DEFAULT_VALUE, defaultValue ));
        }
        return f;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // String SerializationHandler

    public String getValueAsString(Object o) {
        return DataConverter.toString(getObjectAttribute(o));
    }

    public void setValueAsString(Object o, String val) {
        setObjectAttribute(o, DataConverter.toStringArray( val ));
    }
}
