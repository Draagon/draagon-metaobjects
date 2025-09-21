/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataTypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A String Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@MetaDataTypeHandler(type = "field", subType = "string", description = "String field type with length and pattern validation")
@SuppressWarnings("serial")
public class StringField extends PrimitiveField<String> {

    private static final Logger log = LoggerFactory.getLogger(StringField.class);

    public final static String SUBTYPE_STRING = "string";
    public final static String ATTR_PATTERN = "pattern";
    public final static String ATTR_MAX_LENGTH = "maxLength";
    public final static String ATTR_MIN_LENGTH = "minLength";

    public StringField( String name ) {
        super( SUBTYPE_STRING, name, DataTypes.STRING );
    }

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.registerType(StringField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_STRING)
                .description("String field with pattern validation")
                
                // STRING-SPECIFIC ATTRIBUTES
                .optionalAttribute(ATTR_PATTERN, "string")
                .optionalAttribute(ATTR_MAX_LENGTH, "int")
                .optionalAttribute(ATTR_MIN_LENGTH, "int")
                
                // COMMON FIELD ATTRIBUTES
                .optionalAttribute("isAbstract", "string")
                .optionalAttribute("validation", "string")
                .optionalAttribute("required", "string")
                .optionalAttribute("defaultValue", "string")
                .optionalAttribute("defaultView", "string")
                
                // TEST-SPECIFIC ATTRIBUTES (for codegen tests)
                .optionalAttribute("isId", "boolean")
                .optionalAttribute("dbColumn", "string")
                .optionalAttribute("isSearchable", "boolean")
                .optionalAttribute("isOptional", "boolean")
                
                // ACCEPTS VALIDATORS
                .optionalChild("validator", "*")
                
                // ACCEPTS VIEWS
                .optionalChild("view", "*")
                
                // ACCEPTS COMMON ATTRIBUTES
                .optionalChild("attr", "string")
                .optionalChild("attr", "int")
                .optionalChild("attr", "boolean")
            );
            
            log.debug("Registered StringField type with unified registry");
            
            // Register StringField-specific constraints
            setupStringFieldConstraints();
            
        } catch (Exception e) {
            log.error("Failed to register StringField type with unified registry", e);
        }
    }
    
    /**
     * Setup StringField-specific constraints in the constraint registry
     */
    private static void setupStringFieldConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();
            
            // PLACEMENT CONSTRAINT: StringField CAN have maxLength attribute
            PlacementConstraint maxLengthPlacement = new PlacementConstraint(
                "stringfield.maxlength.placement",
                "StringField can optionally have maxLength attribute",
                (metadata) -> metadata instanceof StringField,
                (child) -> child instanceof IntAttribute && 
                          child.getName().equals(ATTR_MAX_LENGTH)
            );
            constraintRegistry.addConstraint(maxLengthPlacement);
            
            // PLACEMENT CONSTRAINT: StringField CAN have minLength attribute
            PlacementConstraint minLengthPlacement = new PlacementConstraint(
                "stringfield.minlength.placement",
                "StringField can optionally have minLength attribute",
                (metadata) -> metadata instanceof StringField,
                (child) -> child instanceof IntAttribute && 
                          child.getName().equals(ATTR_MIN_LENGTH)
            );
            constraintRegistry.addConstraint(minLengthPlacement);
            
            // PLACEMENT CONSTRAINT: StringField CAN have pattern attribute
            PlacementConstraint patternPlacement = new PlacementConstraint(
                "stringfield.pattern.placement",
                "StringField can optionally have pattern attribute",
                (metadata) -> metadata instanceof StringField,
                (child) -> child instanceof StringAttribute && 
                          child.getName().equals(ATTR_PATTERN)
            );
            constraintRegistry.addConstraint(patternPlacement);
            
            // VALIDATION CONSTRAINT: Pattern validation for string fields
            ValidationConstraint patternValidation = new ValidationConstraint(
                "stringfield.pattern.validation",
                "StringField pattern attribute must be valid regex",
                (metadata) -> metadata instanceof StringField && metadata.hasMetaAttr(ATTR_PATTERN),
                (metadata, value) -> {
                    try {
                        String pattern = metadata.getMetaAttr(ATTR_PATTERN).getValueAsString();
                        if (pattern != null && !pattern.isEmpty()) {
                            // Test if pattern is valid regex
                            java.util.regex.Pattern.compile(pattern);
                        }
                        return true;
                    } catch (java.util.regex.PatternSyntaxException e) {
                        return false;
                    }
                }
            );
            constraintRegistry.addConstraint(patternValidation);
            
            log.debug("Registered StringField-specific constraints");
            
        } catch (Exception e) {
            log.error("Failed to register StringField constraints", e);
        }
    }


    /**
     * Manually Create a StringField
     * @param name Name of the field
     * @param defaultValue Default value for the field
     * @return New StringField
     */
    public static StringField create( String name, String defaultValue ) {
        StringField f = new StringField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(StringAttribute.create( ATTR_DEFAULT_VALUE, defaultValue ));
        }
        return f;
    }
}
