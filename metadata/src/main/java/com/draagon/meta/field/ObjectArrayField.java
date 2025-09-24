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

import java.util.List;

import static com.draagon.meta.field.MetaField.TYPE_FIELD;
import static com.draagon.meta.field.MetaField.SUBTYPE_BASE;

/**
 * An Object Array Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@MetaDataType(type = "field", subType = "objectArray", description = "Object array field for lists of object references")
public class ObjectArrayField extends ArrayField<Object,List<Object>>
{
    private static final Logger log = LoggerFactory.getLogger(ObjectArrayField.class);

    public final static String SUBTYPE_OBJECT_ARRAY = "objectarray";
    public final static String ATTR_OBJECT_REF = "objectRef";

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

            MetaDataRegistry.registerType(ObjectArrayField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_OBJECT_ARRAY)
                .description("Object array field for lists of object references")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // OBJECT ARRAY-SPECIFIC ATTRIBUTES (using new API)
                .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_OBJECT_REF)

            );

            log.debug("Registered ObjectArrayField type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register ObjectArrayField type with unified registry", e);
        }
    }

    public ObjectArrayField(String name ) {
        super( SUBTYPE_OBJECT_ARRAY, name, DataTypes.OBJECT_ARRAY );
    }

    /**
     * Manually Create an Object Filed
     * @param name Name of the field
     * @return New ObjectField
     */
    public static ObjectArrayField create( String name ) {
        return new ObjectArrayField( name );

    }

    public static ObjectArrayField create( String name, boolean emptyDefault ) {
        return new ObjectArrayField( name );
    }

    /**
     * Return the specified MetaObject
     */
    public MetaObject getObjectRef() {
        return MetaDataUtil.getObjectRef(this);
    }

    /** Return the array item type */
    public Class getArrayItemClass() {

        if ( getObjectRef() != null ) {
            MetaObject mo = getObjectRef();
            try {
                return mo.getObjectClass();
            } catch (ClassNotFoundException e) {
            }
        }

        return Object.class;
    }
}
