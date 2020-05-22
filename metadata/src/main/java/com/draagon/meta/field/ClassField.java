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
import com.draagon.meta.io.xml.XMLSerializationHandler;
import com.draagon.meta.util.DataConverter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;

/**
 * A Double Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class ClassField extends MetaField<Class> implements StringSerializationHandler // XMLSerializationHandler, JsonSerializationHandler
{
    public final static String SUBTYPE_DOUBLE   = "double";

    public ClassField(String name ) {
        super( SUBTYPE_DOUBLE, name, DataTypes.CUSTOM );
    }

    /**
     * Manually Create a DoubleField
     * @param name Name of the field
     * @return New DoubleField
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
                        val = Class.forName((String) val);
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
            return Class.forName(val);
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
