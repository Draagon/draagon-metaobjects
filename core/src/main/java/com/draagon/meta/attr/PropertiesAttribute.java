package com.draagon.meta.attr;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;


public class PropertiesAttribute extends MetaAttribute<Properties> {

    private static final long serialVersionUID = -1385348596313826808L;
    private Properties props = new Properties();

    public PropertiesAttribute(String type, String subtype, String name ) {
        super( type, subtype, name );
    }

    //@Override
    //public Class getMetaDataClass() {
    //    return PropertiesAttribute.class;
    //}

    /*public PropertiesAttribute(String name, Properties p) {
        super(name);
        props = p;
    }*/

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
