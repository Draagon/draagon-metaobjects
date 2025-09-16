package com.draagon.meta.enhancement.providers;

import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.attr.BooleanAttribute;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.enhancement.AttributeDefinition;
import com.draagon.meta.enhancement.MetaDataAttributeProvider;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.field.MetaField;

import java.util.Arrays;
import java.util.Collection;

/**
 * v6.0.0: Provides validation-related attributes for ValidationChain and validation rules.
 * This replaces the TypesConfig overlay system with service-based attribute provision.
 * 
 * These attributes control validation behaviors and rules across MetaData instances.
 */
public class ValidationAttributeProvider implements MetaDataAttributeProvider {
    
    public static final String PROVIDER_ID = "ValidationAttributes";
    
    // Validation attribute names
    public static final String ATTR_REQUIRED = "required";
    public static final String ATTR_MIN_LENGTH = "minLength";
    public static final String ATTR_MAX_LENGTH = "maxLength";
    public static final String ATTR_MIN_VALUE = "minValue";
    public static final String ATTR_MAX_VALUE = "maxValue";
    public static final String ATTR_PATTERN = "pattern";
    public static final String ATTR_CUSTOM_VALIDATOR = "customValidator";
    public static final String ATTR_ERROR_MESSAGE = "errorMessage";
    public static final String ATTR_VALIDATION_GROUP = "validationGroup";
    public static final String ATTR_NULLABLE = "nullable";
    
    // UI-related validation attributes
    public static final String ATTR_DISPLAY_NAME = "displayName";
    public static final String ATTR_HELP_TEXT = "helpText";
    public static final String ATTR_PLACEHOLDER = "placeholder";
    public static final String ATTR_INPUT_TYPE = "inputType";
    
    @Override
    public Collection<AttributeDefinition> getAttributeDefinitions() {
        return Arrays.asList(
            // Core validation attributes
            AttributeDefinition.builder()
                .name(ATTR_REQUIRED)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(BooleanAttribute.SUBTYPE_BOOLEAN)
                .description("Whether field value is required")
                .applicableTypes(MetaField.TYPE_FIELD)
                .defaultValue(false)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_NULLABLE)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(BooleanAttribute.SUBTYPE_BOOLEAN)
                .description("Whether field can have null values")
                .applicableTypes(MetaField.TYPE_FIELD)
                .defaultValue(true)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_MIN_LENGTH)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(IntAttribute.SUBTYPE_INT)
                .description("Minimum string length")
                .applicableTypes(MetaField.TYPE_FIELD)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_MAX_LENGTH)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(IntAttribute.SUBTYPE_INT)
                .description("Maximum string length")
                .applicableTypes(MetaField.TYPE_FIELD)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_MIN_VALUE)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("Minimum numeric value")
                .applicableTypes(MetaField.TYPE_FIELD)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_MAX_VALUE)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("Maximum numeric value")
                .applicableTypes(MetaField.TYPE_FIELD)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_PATTERN)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("Regular expression pattern for validation")
                .applicableTypes(MetaField.TYPE_FIELD)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_CUSTOM_VALIDATOR)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("Custom validator class name")
                .applicableTypes(MetaField.TYPE_FIELD, MetaObject.TYPE_OBJECT)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_ERROR_MESSAGE)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("Custom validation error message")
                .applicableTypes(MetaField.TYPE_FIELD, MetaObject.TYPE_OBJECT)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_VALIDATION_GROUP)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("Validation group for conditional validation")
                .applicableTypes(MetaField.TYPE_FIELD, MetaObject.TYPE_OBJECT)
                .build(),
                
            // UI-related validation attributes
            AttributeDefinition.builder()
                .name(ATTR_DISPLAY_NAME)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("Human-readable display name")
                .applicableTypes(MetaField.TYPE_FIELD, MetaObject.TYPE_OBJECT)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_HELP_TEXT)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("Help text for user interface")
                .applicableTypes(MetaField.TYPE_FIELD, MetaObject.TYPE_OBJECT)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_PLACEHOLDER)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("Placeholder text for input fields")
                .applicableTypes(MetaField.TYPE_FIELD)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_INPUT_TYPE)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("HTML input type (text, email, password, etc.)")
                .applicableTypes(MetaField.TYPE_FIELD)
                .defaultValue("text")
                .build()
        );
    }
    
    @Override
    public String getProviderId() {
        return PROVIDER_ID;
    }
    
    @Override
    public int getPriority() {
        return 75; // Validation attributes loaded after database but before UI
    }
    
    @Override
    public String getDescription() {
        return "Provides validation and UI-related attributes for ValidationChain and form generation";
    }
}