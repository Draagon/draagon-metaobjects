/*
 * Copyright 2004 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.field;

import com.metaobjects.*;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.metaobjects.field.MetaField.TYPE_FIELD;
import static com.metaobjects.field.MetaField.SUBTYPE_BASE;

/**
 * A Short Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
public class ShortField extends PrimitiveField<Short>
{
    private static final Logger log = LoggerFactory.getLogger(ShortField.class);

    public final static String SUBTYPE_SHORT = "short";
    public final static String ATTR_MIN_VALUE = "minValue";
    public final static String ATTR_MAX_VALUE = "maxValue";

    /**
     * Register ShortField type with the registry (called by provider)
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(ShortField.class, def -> def
            .type(TYPE_FIELD).subType(SUBTYPE_SHORT)
            .description("Short field with numeric validation")
            .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)
            .optionalAttribute(ATTR_MIN_VALUE, SUBTYPE_SHORT)
            .optionalAttribute(ATTR_MAX_VALUE, SUBTYPE_SHORT)
        );
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
