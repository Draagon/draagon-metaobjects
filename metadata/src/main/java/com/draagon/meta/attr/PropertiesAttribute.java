package com.draagon.meta.attr;

import com.draagon.meta.DataTypes;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * A Properties Attribute
 */
@MetaDataType(type = "attr", subType = "properties", description = "Properties attribute for key-value configuration data")
@SuppressWarnings("serial")
public class PropertiesAttribute extends MetaAttribute<Properties> {

    private static final Logger log = LoggerFactory.getLogger(PropertiesAttribute.class);
    public final static String SUBTYPE_PROPERTIES = "properties";

    // Self-registration with unified registry
    static {
        try {
            MetaDataRegistry.registerType(PropertiesAttribute.class, def -> def
                .type("attr").subType(SUBTYPE_PROPERTIES)
                .description("Properties attribute for key-value configuration data")
            );
            log.debug("Registered PropertiesAttribute type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register PropertiesAttribute type with unified registry", e);
        }
    }

    public PropertiesAttribute(String name ) {
        super( SUBTYPE_PROPERTIES, name, DataTypes.CUSTOM);
    }

    /**
     * Manually create a Properties MetaAttribute with a value
     */
    public static PropertiesAttribute create(String name, Properties value ) {
        PropertiesAttribute a = new PropertiesAttribute( name );
        a.setValue( value );
        return a;
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
