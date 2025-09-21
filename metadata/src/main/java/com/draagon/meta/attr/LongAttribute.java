/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.attr;

import com.draagon.meta.DataTypes;
import com.draagon.meta.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Long Attribute
 */
@SuppressWarnings("serial")
public class LongAttribute extends MetaAttribute<Long> {
    
    private static final Logger log = LoggerFactory.getLogger(LongAttribute.class);
    
    public final static String TYPE_ATTR = "attr";
    public final static String SUBTYPE_LONG = "long";
    
    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.registerType(LongAttribute.class, def -> def
                .type(TYPE_ATTR).subType(SUBTYPE_LONG)
                .description("Long attribute for numeric metadata")
            );
            
            log.debug("Registered LongAttribute type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register LongAttribute type with unified registry", e);
        }
    }

    /**
     * Constructs the Integer MetaAttribute
     */
    public LongAttribute(String name ) {
        super( SUBTYPE_LONG, name, DataTypes.LONG);
    }

    /**
     * Manually create an Integer MetaAttribute with a value
     */
    public static LongAttribute create(String name, Long value ) {
        LongAttribute a = new LongAttribute( name );
        a.setValue( value );
        return a;
    }
}
