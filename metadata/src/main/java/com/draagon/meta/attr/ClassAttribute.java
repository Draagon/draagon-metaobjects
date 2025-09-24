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
 * A Java Class Attribute
 */
@MetaDataType(type = "attr", subType = "class", description = "Class attribute for Java class metadata")
@SuppressWarnings("serial")
public class ClassAttribute extends MetaAttribute<Class<?>> {

    private static final Logger log = LoggerFactory.getLogger(ClassAttribute.class);

    public final static String SUBTYPE_CLASS = "class";

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.getInstance().registerType(ClassAttribute.class, def -> def
                .type(TYPE_ATTR).subType(SUBTYPE_CLASS)
                .inheritsFrom(TYPE_ATTR, SUBTYPE_BASE)
                .description("Class attribute for Java class metadata")
            );
            log.debug("Registered ClassAttribute type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register ClassAttribute type with unified registry", e);
        }
    }

    /**
     * Constructs the MetaClass
     */
    public ClassAttribute(String name ) {
        super( SUBTYPE_CLASS, name, DataTypes.CUSTOM);
    }

    /**
     * Manually create a Class MetaAttribute with a Class<?> value
     */
    public static ClassAttribute create(String name, Class<?> value ) {
        ClassAttribute a = new ClassAttribute( name );
        a.setValue( value );
        return a;
    }

    /**
     * Manually create a Class MetaAttribute with a String classname value
     */
    public static ClassAttribute create(String name, String value ) {
        ClassAttribute a = new ClassAttribute( name );
        a.setValueAsString( value );
        return a;
    }

    @Override
    public void setValue(Class<?> value) {
        super.setValue(value);
    }

    @Override
    public void setValueAsObject(Object value) {
        if ( value == null ) {
            setValue( null );
        } else if ( value instanceof String ) {
            setValueAsString( (String) value );
        }
        else if ( value instanceof Class ) {
            setValue( (Class<?>) value );
        }
        else {
            throw new InvalidAttributeValueException( "Can not set value with class [" + value.getClass() + "] for object: " + value );
        }
    }

    @Override
    public void setValueAsString(String value) {
        try {
            if ( value == null ) {
                setValue( null );
            } else {
                setValue( (Class<?>) loadClass(value));
            }
        } catch (ClassNotFoundException e) {
            throw new InvalidAttributeValueException("Invalid Class Name [" + value + "] for ClassAttribute");
        }
    }

    @Override
    public String getValueAsString() {
        if ( getValue() == null ) return null;
        return getValue().getName();
    }
}
