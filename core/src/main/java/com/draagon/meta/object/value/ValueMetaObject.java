/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.object.value;

import com.draagon.meta.object.data.DataMetaObject;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MetaDataType(type = "object", subType = "value", description = "Value-based metadata object with dynamic attribute access")
public class ValueMetaObject extends DataMetaObject
{
    private static final Logger log = LoggerFactory.getLogger(ValueMetaObject.class);
    public final static String SUBTYPE_VALUE = "value";
    
    // Self-registration with unified registry
    static {
        try {
            MetaDataRegistry.registerType(ValueMetaObject.class, def -> def
                .type("object").subType(SUBTYPE_VALUE)
                .description("Value-based metadata object")
                .optionalChild("field", "*")
                .optionalChild("attr", "*")
                .optionalChild("validator", "*")
            );
            log.debug("Registered ValueMetaObject type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register ValueMetaObject type with unified registry", e);
        }
    }

    /**
     * Constructs the MetaClassObject for MetaObjects
     */
    public ValueMetaObject( String name ) {
        super( SUBTYPE_VALUE, name);
    }

    /**
     * Manually create a ValueMetaObject with the specified name
     * @param name Name for the ValueMetaObject
     * @return Created ValueObject
     */
    public static ValueMetaObject create( String name ) {
        return new ValueMetaObject( name );
    }

    @Override
    public boolean allowExtensions() {
        if ( hasMetaAttr(ATTR_ALLOWEXTENSIONS)) {
            return super.allowExtensions();
        }
        return false;
    }

    @Override
    public boolean isStrict() {
        if ( hasMetaAttr(ATTR_ISSTRICT)) {
            return super.isStrict();
        }
        return false;
    }


    /**
     * Whether the MetaClass handles the object specified
     */
    @Override
    public boolean produces(Object obj) {

        if (obj != null && obj instanceof ValueObject) {
            return super.produces( obj );
        }

        return false;
    }

    @Override
    protected Class<?> getDefaultObjectClass() {
        return ValueObject.class;
    }
}
