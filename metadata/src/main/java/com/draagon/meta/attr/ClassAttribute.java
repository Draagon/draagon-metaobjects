/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.attr;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Java Class Attribute with unified registry registration and parent acceptance.
 *
 * @version 6.2
 */
@MetaDataType(type = "attr", subType = "class", description = "Class attribute for Java class metadata")
@SuppressWarnings("serial")
public class ClassAttribute extends MetaAttribute<Class<?>> {

    private static final Logger log = LoggerFactory.getLogger(ClassAttribute.class);

    public final static String SUBTYPE_CLASS = "class";

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

            MetaDataRegistry.registerType(ClassAttribute.class, def -> def
                .type(TYPE_ATTR).subType(SUBTYPE_CLASS)
                .description("Class attribute for Java class metadata")

                // INHERIT FROM BASE ATTRIBUTE
                .inheritsFrom(TYPE_ATTR, SUBTYPE_BASE)
            );

            log.debug("Registered ClassAttribute type with unified registry");

            // Register ClassAttribute-specific validation constraints only
            setupClassAttributeValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register ClassAttribute type with unified registry", e);
        }
    }

    /**
     * Setup ClassAttribute-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupClassAttributeValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALIDATION CONSTRAINT: Class attribute values
            ValidationConstraint classAttributeValidation = new ValidationConstraint(
                "classattribute.value.validation",
                "ClassAttribute values must be valid Java class names or null",
                (metadata) -> metadata instanceof ClassAttribute,
                (metadata, value) -> {
                    if (metadata instanceof ClassAttribute) {
                        ClassAttribute classAttr = (ClassAttribute) metadata;
                        String valueStr = classAttr.getValueAsString();
                        if (valueStr == null || valueStr.isEmpty()) {
                            return true;
                        }
                        try {
                            // Try to load the class to validate it exists
                            ClassAttribute.loadClassFromName(valueStr);
                            return true;
                        } catch (ClassNotFoundException e) {
                            return false;
                        }
                    }
                    return true;
                }
            );
            constraintRegistry.addConstraint(classAttributeValidation);

            log.debug("Registered ClassAttribute-specific constraints");

        } catch (Exception e) {
            log.error("Failed to register ClassAttribute constraints", e);
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
            throw new MetaDataException( "Can not set value with class [" + value.getClass() + "] for object: " + value );
        }
    }

    @Override
    public void setValueAsString(String value) {
        try {
            if ( value == null ) {
                setValue( null );
            } else {
                setValue( (Class<?>) loadClassFromName(value));
            }
        } catch (ClassNotFoundException e) {
            throw new MetaDataException("Invalid Class Name [" + value + "] for ClassAttribute");
        }
    }

    /**
     * Load a class by name using the current thread's context classloader
     */
    public static Class<?> loadClassFromName(String className) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(className);
    }

    @Override
    public String getValueAsString() {
        if ( getValue() == null ) return null;
        return getValue().getName();
    }
}
