/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.attr;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaDataTypeId;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A String Array Attribute with unified registry registration.
 */
@MetaDataType(type = "attr", subType = "stringarray", description = "String array attribute for multiple text values")
@SuppressWarnings("serial")
public class StringArrayAttribute extends MetaAttribute<List<String>>
{
    private static final Logger log = LoggerFactory.getLogger(StringArrayAttribute.class);

    public final static String SUBTYPE_STRING_ARRAY = "stringarray";

    /**
     * Constructs the String Array MetaAttribute
     */
    public StringArrayAttribute(String name ) {
        super( SUBTYPE_STRING_ARRAY, name, DataTypes.STRING_ARRAY);
    }

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.registerType(StringArrayAttribute.class, def -> def
                .type(TYPE_ATTR).subType(SUBTYPE_STRING_ARRAY)
                .description("String array attribute value")
                // NO CHILD REQUIREMENTS - just registers identity
                // Placement rules are defined by parent types that accept string array attributes
            );
            
            log.debug("Registered StringArrayAttribute type with unified registry");
            
            // Register StringArrayAttribute-specific constraints
            setupStringArrayAttributeConstraints();
            
        } catch (Exception e) {
            log.error("Failed to register StringArrayAttribute type with unified registry", e);
        }
    }
    
    /**
     * Setup StringArrayAttribute-specific constraints in the constraint registry
     */
    private static void setupStringArrayAttributeConstraints() {
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
