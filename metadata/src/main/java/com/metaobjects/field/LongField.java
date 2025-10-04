/*
 * Copyright 2004 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.field;

import com.metaobjects.*;
import com.metaobjects.attr.LongAttribute;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Long Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
public class LongField extends PrimitiveField<Long> {

    private static final Logger log = LoggerFactory.getLogger(LongField.class);

    public final static String SUBTYPE_LONG = "long";
    public final static String ATTR_MIN_VALUE = "minValue";
    public final static String ATTR_MAX_VALUE = "maxValue";

    public LongField( String name ) {
        super( SUBTYPE_LONG, name, DataTypes.LONG );
    }

    /**
     * Register LongField type and constraints with the registry
     *
     * @param registry The MetaDataRegistry to register with
     */
    public static void registerTypes(MetaDataRegistry registry) {
        try {
            // Register the type definition
            registry.registerType(LongField.class, def -> {
                def.type(TYPE_FIELD).subType(SUBTYPE_LONG)
                   .description("Long field with numeric validation")

                   // INHERIT FROM BASE FIELD
                   .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE);

                // LONG-SPECIFIC ATTRIBUTES WITH FLUENT CONSTRAINTS
                def.optionalAttributeWithConstraints(ATTR_MIN_VALUE)
                   .ofType(LongAttribute.SUBTYPE_LONG)
                   .asSingle();

                def.optionalAttributeWithConstraints(ATTR_MAX_VALUE)
                   .ofType(LongAttribute.SUBTYPE_LONG)
                   .asSingle();
            });

            if (log != null) {
                log.debug("Registered LongField type with unified registry (auto-generated constraints)");
            }

        } catch (Exception e) {
            if (log != null) {
                log.error("Failed to register LongField type with unified registry", e);
            }
        }
    }

    /**
     * Manually Create a LongField
     * @param name Name of the field
     * @param defaultValue Default value for the field
     * @return New LongField
     */
    public static LongField create( String name, Long defaultValue ) {
        LongField f = new LongField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(StringAttribute.create( ATTR_DEFAULT_VALUE, defaultValue.toString() ));
        }
        return f;
    }
}
