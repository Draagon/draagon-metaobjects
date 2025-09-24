package com.draagon.meta.attr;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * A Properties Attribute with unified registry registration and parent acceptance.
 *
 * @version 6.2
 */
@MetaDataType(type = "attr", subType = "properties", description = "Properties attribute for key-value configuration data")
public class PropertiesAttribute extends MetaAttribute<Properties> {

    private static final Logger log = LoggerFactory.getLogger(PropertiesAttribute.class);

    public final static String SUBTYPE_PROPERTIES = "properties";

    // Unified registry self-registration
    static {
        try {
            // Explicitly trigger MetaAttribute static initialization first
            try {
                Class.forName(MetaAttribute.class.getName());
                // Add a small delay to ensure MetaAttribute registration completes
                Thread.sleep(1);
            } catch (ClassNotFoundException | InterruptedException e) {
                log.warn("Could not force MetaAttribute class loading", e);
            }

            MetaDataRegistry.registerType(PropertiesAttribute.class, def -> def
                .type(TYPE_ATTR).subType(SUBTYPE_PROPERTIES)
                .description("Properties attribute for key-value configuration data")

                // INHERIT FROM BASE ATTRIBUTE
                .inheritsFrom(TYPE_ATTR, SUBTYPE_BASE)

                // Universal fallback for any properties-based attribute
                .acceptsParents("*", "*")  // PropertiesAttribute can be used for any properties-based attribute

            );

            log.debug("Registered PropertiesAttribute type with unified registry");

            // Register PropertiesAttribute-specific validation constraints only
            setupPropertiesAttributeValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register PropertiesAttribute type with unified registry", e);
        }
    }

    /**
     * Setup PropertiesAttribute-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupPropertiesAttributeValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALIDATION CONSTRAINT: Properties attribute values
            ValidationConstraint propertiesAttributeValidation = new ValidationConstraint(
                "propertiesattribute.value.validation",
                "PropertiesAttribute values must be valid Java properties format",
                (metadata) -> metadata instanceof PropertiesAttribute,
                (metadata, value) -> {
                    if (metadata instanceof PropertiesAttribute) {
                        PropertiesAttribute propAttr = (PropertiesAttribute) metadata;
                        String valueStr = propAttr.getValueAsString();
                        if (valueStr == null || valueStr.isEmpty()) {
                            return true;
                        }
                        try {
                            // Try to parse as properties to validate format
                            Properties testProps = new Properties();
                            testProps.load(new StringReader(valueStr));
                            return true;
                        } catch (IOException e) {
                            return false;
                        }
                    }
                    return true;
                }
            );
            constraintRegistry.addConstraint(propertiesAttributeValidation);

            log.debug("Registered PropertiesAttribute-specific constraints");

        } catch (Exception e) {
            log.error("Failed to register PropertiesAttribute constraints", e);
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
        else {
            throw new MetaDataException( "Can not set value with class [" + value.getClass() + "] for object: " + value );
        }
    }

    @Override
    public void setValueAsString(String value) {
        try {
            if ( value == null ) {
                setValue( null );
            } else {
                Properties p = new Properties();
                p.load(new StringReader(value.toString()));
                setValue( p );
            }
        } catch (IOException e) {
            throw new MetaDataException("Could not load properties [" + value + "]: " + e.getMessage());
        }
    }

    @Override
    public String getValueAsString() {
        return getValue().toString();
    }
}
