/*
 * Copyright 2002 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.object.value;

import com.metaobjects.attr.MetaAttribute;
import com.metaobjects.field.MetaField;
import com.metaobjects.identity.MetaIdentity;
import com.metaobjects.object.MetaObject;
import com.metaobjects.object.data.DataMetaObject;
import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.relationship.MetaRelationship;
import com.metaobjects.validator.MetaValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueMetaObject extends DataMetaObject
{
    private static final Logger log = LoggerFactory.getLogger(ValueMetaObject.class);
    public final static String SUBTYPE_VALUE = "value";
    
    public static void registerTypes(MetaDataRegistry registry) {
        try {
            registry.registerType(ValueMetaObject.class, def -> def
                .type(MetaObject.TYPE_OBJECT).subType(SUBTYPE_VALUE)
                .description("Value-based metadata object")
                .optionalChild(MetaField.TYPE_FIELD, "*")
                .optionalChild(MetaAttribute.TYPE_ATTR, "*")
                .optionalChild(MetaValidator.TYPE_VALIDATOR, "*")
                .optionalChild(MetaIdentity.TYPE_IDENTITY, "*", "*")  // Enable new MetaIdentity system for value objects
                .optionalChild(MetaRelationship.TYPE_RELATIONSHIP, "*", "*")  // Enable new MetaIdentity system for value objects
            );
            if (log != null) {
                log.debug("Registered ValueMetaObject type with unified registry");
            }
        } catch (Exception e) {
            if (log != null) {
                log.error("Failed to register ValueMetaObject type with unified registry", e);
            }
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
