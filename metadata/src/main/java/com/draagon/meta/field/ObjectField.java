/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.DataTypes;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.util.MetaDataUtil;
import com.draagon.meta.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Object Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
public class ObjectField extends MetaField<Object>
{
    private static final Logger log = LoggerFactory.getLogger(ObjectField.class);

    public final static String SUBTYPE_OBJECT = "object";
    public final static String ATTR_OBJECTREF = MetaObject.ATTR_OBJECT_REF;

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.getInstance().registerType(ObjectField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_OBJECT)
                .description("Object field with object reference support")
                
                // OBJECT-SPECIFIC ATTRIBUTES
                .optionalAttribute(ATTR_OBJECTREF, "string")
                // Inherits: required, defaultValue, validation, defaultView from MetaField
            );
            
            log.debug("Registered ObjectField type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register ObjectField type with unified registry", e);
        }
    }

    public ObjectField( String name ) {
        super( SUBTYPE_OBJECT, name, DataTypes.OBJECT );
    }

    /**
     * Manually Create an Object Filed
     * @param name Name of the field
     * @return New ObjectField
     */
    public static ObjectField create( String name ) {
        ObjectField f = new ObjectField( name );
        return f;
    }

    /**
     * Return the referenced MetaObject
     */
    public MetaObject getObjectRef() {
        return MetaDataUtil.getObjectRef(this);
    }
}
