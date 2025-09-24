/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.attr;

import com.draagon.meta.DataTypes;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.draagon.meta.attr.MetaAttribute.TYPE_ATTR;
import static com.draagon.meta.attr.MetaAttribute.SUBTYPE_BASE;

/**
 * A String Array Attribute with unified registry registration and parent acceptance.
 *
 * @version 6.2
 */
@MetaDataType(type = "attr", subType = "stringarray", description = "String array attribute for multiple text values")
public class StringArrayAttribute extends MetaAttribute<List<String>>
{
    private static final Logger log = LoggerFactory.getLogger(StringArrayAttribute.class);

    public final static String SUBTYPE_STRING_ARRAY = "stringarray";

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

            MetaDataRegistry.registerType(StringArrayAttribute.class, def -> def
                .type(TYPE_ATTR).subType(SUBTYPE_STRING_ARRAY)
                .description("String array attribute for multiple text values")

                // INHERIT FROM BASE ATTRIBUTE
                .inheritsFrom(TYPE_ATTR, SUBTYPE_BASE)
            );

            log.debug("Registered StringArrayAttribute type with unified registry");

            // Register StringArrayAttribute-specific validation constraints only
            setupStringArrayAttributeValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register StringArrayAttribute type with unified registry", e);
        }
    }

    /**
     * Constructs the String Array MetaAttribute
     */
    public StringArrayAttribute(String name ) {
        super( SUBTYPE_STRING_ARRAY, name, DataTypes.STRING_ARRAY);
    }
    
    /**
     * Setup StringArrayAttribute-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupStringArrayAttributeValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();
            
            // VALIDATION CONSTRAINT: String array attribute format
            ValidationConstraint stringArrayAttributeValidation = new ValidationConstraint(
                "stringarrayattribute.format.validation",
                "StringArrayAttribute values must be properly formatted",
                (metadata) -> metadata instanceof StringArrayAttribute,
                (metadata, value) -> {
                    if (metadata instanceof StringArrayAttribute) {
                        StringArrayAttribute arrayAttr = (StringArrayAttribute) metadata;
                        try {
                            // Test if the value can be parsed as a string array
                            arrayAttr.getValue();
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    }
                    return true;
                }
            );
            constraintRegistry.addConstraint(stringArrayAttributeValidation);
            
            log.debug("Registered StringArrayAttribute-specific constraints");
            
        } catch (Exception e) {
            log.error("Failed to register StringArrayAttribute constraints", e);
        }
    }

    /**
     * Manually create a StringArray MetaAttribute with a value
     */
    public static StringArrayAttribute create(String name, String value ) {
        StringArrayAttribute a = new StringArrayAttribute( name );
        a.setValueAsString( value );
        return a;
    }
}
