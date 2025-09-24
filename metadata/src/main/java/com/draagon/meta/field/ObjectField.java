/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.DataTypes;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.util.MetaDataUtil;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.draagon.meta.field.MetaField.TYPE_FIELD;
import static com.draagon.meta.field.MetaField.SUBTYPE_BASE;

/**
 * An Object Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@MetaDataType(type = "field", subType = "object", description = "Object field with object reference support")
@SuppressWarnings("serial")
public class ObjectField extends MetaField<Object>
{
    private static final Logger log = LoggerFactory.getLogger(ObjectField.class);

    public final static String SUBTYPE_OBJECT = "object";
    public final static String ATTR_OBJECTREF = MetaObject.ATTR_OBJECT_REF;

    // Unified registry self-registration
    static {
        try {
            // Explicitly trigger MetaField static initialization first
            try {
                Class.forName(MetaField.class.getName());
                // Add a small delay to ensure MetaField registration completes
                Thread.sleep(1);
            } catch (ClassNotFoundException | InterruptedException e) {
                log.warn("Could not force MetaField class loading", e);
            }

            MetaDataRegistry.registerType(ObjectField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_OBJECT)
                .description("Object field with object reference support")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // OBJECT-SPECIFIC ATTRIBUTES (using new API)
                .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_OBJECTREF)

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
