/*
 * Copyright 2004 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.field;

import com.metaobjects.DataTypes;
import com.metaobjects.object.MetaObject;
import com.metaobjects.util.MetaDataUtil;
import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.attr.StringAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.metaobjects.field.MetaField.TYPE_FIELD;
import static com.metaobjects.field.MetaField.SUBTYPE_BASE;

/**
 * An Object Array Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
public class ObjectArrayField extends ArrayField<Object,List<Object>>
{
    private static final Logger log = LoggerFactory.getLogger(ObjectArrayField.class);

    public final static String SUBTYPE_OBJECT_ARRAY = "objectArray";
    public final static String ATTR_OBJECTREF = "objectRef";

    /**
     * Register ObjectArrayField type using the standardized registerTypes() pattern.
     * This method registers the object array field type that inherits from field.base.
     *
     * @param registry The MetaDataRegistry to register with
     */
    public static void registerTypes(MetaDataRegistry registry) {
        try {
            registry.registerType(ObjectArrayField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_OBJECT_ARRAY)
                .description("Object array field for lists of object references")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // OBJECT ARRAY-SPECIFIC ATTRIBUTES ONLY
                .optionalAttribute(ATTR_OBJECTREF, StringAttribute.SUBTYPE_STRING)
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
