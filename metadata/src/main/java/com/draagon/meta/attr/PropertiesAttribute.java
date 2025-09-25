package com.draagon.meta.attr;

import com.draagon.meta.DataTypes;
import com.draagon.meta.registry.MetaDataRegistry;

import static com.draagon.meta.attr.MetaAttribute.TYPE_ATTR;
import static com.draagon.meta.attr.MetaAttribute.SUBTYPE_BASE;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * A Properties Attribute with provider-based registration.
 */
@SuppressWarnings("serial")
public class PropertiesAttribute extends MetaAttribute<Properties> {

    public final static String SUBTYPE_PROPERTIES = "properties";

    /**
     * Register this type with the MetaDataRegistry (called by provider)
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(PropertiesAttribute.class, def -> def
            .type(TYPE_ATTR).subType(SUBTYPE_PROPERTIES)
            .description("Properties attribute for key-value configuration data")
            .inheritsFrom(TYPE_ATTR, SUBTYPE_BASE)
        );
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
