/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.field;

import com.draagon.meta.DataTypes;
import com.draagon.meta.InvalidValueException;
import com.draagon.meta.io.json.JsonSerializationHandler;
import com.draagon.meta.io.string.StringSerializationHandler;
import com.draagon.meta.util.DataConverter;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;

import static com.draagon.meta.field.MetaField.TYPE_FIELD;
import static com.draagon.meta.field.MetaField.SUBTYPE_BASE;

/**
 * A Class Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@MetaDataType(type = "field", subType = "class", description = "Class field for class type references")
@SuppressWarnings("serial")
public class ClassField extends MetaField<Class> implements StringSerializationHandler // XMLSerializationHandler, JsonSerializationHandler
{
    private static final Logger log = LoggerFactory.getLogger(ClassField.class);

    public final static String SUBTYPE_CLASS = "class";

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

            MetaDataRegistry.registerType(ClassField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_CLASS)
                .description("Class field for class type references")

                // INHERIT FROM BASE FIELD
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // NO CLASS-SPECIFIC ATTRIBUTES - inherits all from field.base

            );

            log.debug("Registered ClassField type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register ClassField type with unified registry", e);
        }
    }

    public ClassField(String name ) {
        super( SUBTYPE_CLASS, name, DataTypes.CUSTOM );
    }

    /**
     * Manually Create a ClassField
     * @param name Name of the field
     * @return New ClassField
     */
    public static ClassField create(String name ) {
        ClassField f = new ClassField( name );
        return f;
    }

    @Override
    public Class<?> getValueClass() {
        return Class.class;
    }

    /**
     * Sets the object attribute represented by this MetaField
     */
    @Override
    protected void setObjectAttribute(Object obj, Object val) {

        // Ensure the data types are accurate
        if (val != null ) {
            if (!getValueClass().isInstance(val)) {
                if ( val instanceof String ) {
                    val = convertToClass((String)val);
                } else {
                    throw new InvalidValueException("Invalid value [" + val + "], expected class [" + getValueClass().getName() + "]");
                }
            }
        }

        // Perform validation -- Disabled for performance reasons
        //performValidation( obj, val );

        // Set the value on the object
        getDeclaringObject().setValue(this, obj, val);
    }

    /**
     * Gets the object attribute represented by this MetaField
     */
    @Override
    protected Object getObjectAttribute(Object obj) {
        Object val = getDeclaringObject().getValue(this, obj);
        if ( val != null ) {
            if (!getValueClass().isInstance(val)) {
                if (val instanceof String) {
                    try {
                        val = loadClass((String) val);
                    } catch (ClassNotFoundException e) {
                        throw new InvalidValueException("Cannot find class for "
                                + "[" + val.getClass().getName() + "] on MetaField [" + this + "]: " + e.getMessage(), e);
                    }
                }
                else {
                    throw new InvalidValueException("Cannot set value of class "
                            + "[" + val.getClass().getName() + "] on MetaField: " + this);
                }
            }
        }

        return val;
    }

    public Class convertToClass( String val ) {
        try {
            return loadClass(val);
        } catch (ClassNotFoundException e) {
            throw new InvalidValueException("Cannot find class for "
                    + "[" + val + "] on MetaField [" + this + "]: " + e.getMessage(), e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // String SerializationHandler

    public String getValueAsString(Object o) {
        return ((Class)getObjectAttribute(o)).getName();
    }

    public void setValueAsString(Object o, String val) {
        setObjectAttribute(o, convertToClass( val ));
    }

    //////////////////////////////////////////////////////////////////////////
    // Custom Serialization Handlers

    /*@Override
    public void writeJsonValue(Object o, JsonWriter out) throws IOException {
        if ( o == null ) return null;
        o = getObjectValue(o);
        out.value(((Class)o).getName());
    }

    @Override
    public void readJsonValue(Object o, JsonReader in) throws IOException {
        setObjectAttribute(o, convertToClass( in.nextString() ));
    }

    @Override
    public String getXmlAttr(Object o) {
        if ( o == null ) return null;
        o = getObjectValue(o);
        return ((Class)o).getName();
    }

    @Override
    public void setXmlAttr(Object o, String s) {
        setObjectAttribute(o, convertToClass( s ));
    }

    @Override
    public void writeXmlValue(Object o, String xmlName, Document doc, Element e) {
        if ( o == null ) return;
        o = getObjectValue(o);
        e.appendChild( doc.createTextNode( ((Class)o).getName()));
    }

    @Override
    public void readXmlValue(Object o, String xmlName, Element e) {
        String val = e.getTextContent();
        setObjectAttribute( o, convertToClass(val));
    }*/
}
