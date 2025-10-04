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

import static com.metaobjects.field.MetaField.TYPE_FIELD;
import static com.metaobjects.field.MetaField.SUBTYPE_BASE;

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

    /**
     * Register ObjectField type using the standardized registerTypes() pattern.
     * This method registers the object field type that inherits from field.base.
     *
     * @param registry The MetaDataRegistry to register with
     */
    public static void registerTypes(MetaDataRegistry registry) {
        try {
            registry.registerType(ObjectField.class, def -> {
                def.type(TYPE_FIELD).subType(SUBTYPE_OBJECT)
                   .description("Object field with object reference support")

                   // INHERIT FROM BASE FIELD
                   .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE);

                // OBJECT-SPECIFIC ATTRIBUTES WITH FLUENT CONSTRAINTS
                def.optionalAttributeWithConstraints(ATTR_OBJECTREF)
                   .ofType(StringAttribute.SUBTYPE_STRING)
                   .asSingle();
            });

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
