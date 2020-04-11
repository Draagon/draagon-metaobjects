package com.draagon.meta.attr;

import com.draagon.meta.DataTypes;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;


public class PropertiesAttribute extends MetaAttribute<Properties> {

    public final static String SUBTYPE_PROPERTIES = "properties";

    public PropertiesAttribute(String name ) {
        super( SUBTYPE_PROPERTIES, name, DataTypes.CUSTOM);
    }

    @Override
    public void setValueAsObject(Object value) {
        if ( value == null ) {
            setValue( null );
        } else if ( value instanceof String ) {
            setValueAsString( (String) value );
        }
        else if ( value instanceof Properties ) {
            super.setValue((Properties) value);
        }
        throw new InvalidAttributeValueException( "Can not set value with class [" + value.getClass() + "] for object: " + value );
    }

    @Override
    public void setValueAsString(String value) {
        try {
            Properties p = new Properties();
            p.load(new StringReader(value.toString()));
            setValue( p );
        } catch (IOException e) {
            throw new InvalidAttributeValueException("Could not load properties [" + value + "]: " + e.getMessage(), e);
        }
    }

    @Override
    public String getValueAsString() {
        return getValue().toString();
    }
}
