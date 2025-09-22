/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.attr;

import com.draagon.meta.DataTypes;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Double Attribute
 */
@MetaDataType(type = "attr", subType = "double", description = "Double attribute for floating-point numeric metadata")
@SuppressWarnings("serial")
public class DoubleAttribute extends MetaAttribute<Double> {
    
    private static final Logger log = LoggerFactory.getLogger(DoubleAttribute.class);
    
    public final static String TYPE_ATTR = "attr";
    public final static String SUBTYPE_DOUBLE = "double";
    
    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.registerType(DoubleAttribute.class, def -> def
                .type(TYPE_ATTR).subType(SUBTYPE_DOUBLE)
                .description("Double attribute for floating-point numeric metadata")
            );
            
            log.debug("Registered DoubleAttribute type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register DoubleAttribute type with unified registry", e);
        }
    }

    /**
     * Constructs the Double MetaAttribute
     */
    public DoubleAttribute(String name) {
        super(SUBTYPE_DOUBLE, name, DataTypes.DOUBLE);
    }

    /**
     * Manually create a Double MetaAttribute with a value
     */
    public static DoubleAttribute create(String name, Double value) {
        DoubleAttribute a = new DoubleAttribute(name);
        a.setValue(value);
        return a;
    }
}