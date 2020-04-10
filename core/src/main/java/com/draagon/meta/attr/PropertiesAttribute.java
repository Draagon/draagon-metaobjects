package com.draagon.meta.attr;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;


public class PropertiesAttribute extends MetaAttribute<Properties> {

    private Properties props = new Properties();

    public final static String SUBTYPE_PROPERTIES = "properties";

    public PropertiesAttribute(String name ) {
        super( SUBTYPE_PROPERTIES, name );
    }

    @Override
    public Properties getValue() {
        return props;
    }

    public void setValue(Properties value) throws InvalidAttributeValueException {
        props = value;
    }

    @Override
    public void setValue(Object value) {
        props = (Properties) value;
    }


    @Override
    public Object clone() {
        PropertiesAttribute pa = (PropertiesAttribute) super.clone();
        pa.props = props;
        return pa;
    }

    @Override
    public void setValueAsString(String value) {
        try {
            props.load(new StringReader(value.toString()));
        } catch (IOException e) {
            throw new InvalidAttributeValueException("Could not load properties [" + value + "]: " + e.getMessage(), e);
        }
    }

    @Override
    public String getValueAsString() {
        return props.toString();
    }
}
