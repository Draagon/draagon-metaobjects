package com.draagon.meta.enhancement.providers;

import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.attr.BooleanAttribute;
import com.draagon.meta.enhancement.AttributeDefinition;
import com.draagon.meta.enhancement.MetaDataAttributeProvider;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.field.MetaField;

import java.util.Arrays;
import java.util.Collection;

/**
 * v6.0.0: Provides IO/serialization-related attributes for JSON, XML, and other serialization formats.
 * This replaces the TypesConfig overlay system with service-based attribute provision.
 * 
 * These attributes control serialization behaviors across different IO formats.
 */
public class IOAttributeProvider implements MetaDataAttributeProvider {
    
    public static final String PROVIDER_ID = "IOAttributes";
    
    // JSON attribute names
    public static final String ATTR_JSON_IGNORE = "jsonIgnore";
    public static final String ATTR_JSON_PROPERTY = "jsonProperty";
    public static final String ATTR_JSON_TYPE_INFO = "jsonTypeInfo";
    public static final String ATTR_JSON_FORMAT = "jsonFormat";
    
    // XML attribute names
    public static final String ATTR_XML_ELEMENT = "xmlElement";
    public static final String ATTR_XML_ATTRIBUTE = "xmlAttribute";
    public static final String ATTR_XML_ROOT = "xmlRoot";
    public static final String ATTR_XML_NAMESPACE = "xmlNamespace";
    public static final String ATTR_XML_IGNORE = "xmlIgnore";
    
    // CSV attribute names
    public static final String ATTR_CSV_COLUMN = "csvColumn";
    public static final String ATTR_CSV_ORDER = "csvOrder";
    public static final String ATTR_CSV_IGNORE = "csvIgnore";
    
    // General serialization attributes
    public static final String ATTR_SERIALIZE_AS = "serializeAs";
    public static final String ATTR_DESERIALIZE_AS = "deserializeAs";
    
    @Override
    public Collection<AttributeDefinition> getAttributeDefinitions() {
        return Arrays.asList(
            // JSON attributes
            AttributeDefinition.builder()
                .name(ATTR_JSON_IGNORE)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(BooleanAttribute.SUBTYPE_BOOLEAN)
                .description("Skip field in JSON serialization")
                .applicableTypes(MetaField.TYPE_FIELD)
                .defaultValue(false)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_JSON_PROPERTY)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("JSON property name override")
                .applicableTypes(MetaField.TYPE_FIELD)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_JSON_TYPE_INFO)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("JSON type information for polymorphic serialization")
                .applicableTypes(MetaObject.TYPE_OBJECT)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_JSON_FORMAT)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("JSON formatting pattern (e.g., date format)")
                .applicableTypes(MetaField.TYPE_FIELD)
                .build(),
                
            // XML attributes
            AttributeDefinition.builder()
                .name(ATTR_XML_ELEMENT)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("XML element name override")
                .applicableTypes(MetaField.TYPE_FIELD)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_XML_ATTRIBUTE)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(BooleanAttribute.SUBTYPE_BOOLEAN)
                .description("Serialize field as XML attribute instead of element")
                .applicableTypes(MetaField.TYPE_FIELD)
                .defaultValue(false)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_XML_ROOT)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("XML root element name")
                .applicableTypes(MetaObject.TYPE_OBJECT)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_XML_NAMESPACE)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("XML namespace URI")
                .applicableTypes(MetaObject.TYPE_OBJECT, MetaField.TYPE_FIELD)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_XML_IGNORE)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(BooleanAttribute.SUBTYPE_BOOLEAN)
                .description("Skip field in XML serialization")
                .applicableTypes(MetaField.TYPE_FIELD)
                .defaultValue(false)
                .build(),
                
            // CSV attributes
            AttributeDefinition.builder()
                .name(ATTR_CSV_COLUMN)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("CSV column header name")
                .applicableTypes(MetaField.TYPE_FIELD)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_CSV_IGNORE)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(BooleanAttribute.SUBTYPE_BOOLEAN)
                .description("Skip field in CSV serialization")
                .applicableTypes(MetaField.TYPE_FIELD)
                .defaultValue(false)
                .build(),
                
            // General serialization attributes
            AttributeDefinition.builder()
                .name(ATTR_SERIALIZE_AS)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("Override serialization type/format")
                .applicableTypes(MetaField.TYPE_FIELD)
                .build(),
                
            AttributeDefinition.builder()
                .name(ATTR_DESERIALIZE_AS)
                .type(MetaAttribute.TYPE_ATTR)
                .subType(StringAttribute.SUBTYPE_STRING)
                .description("Override deserialization type/format")
                .applicableTypes(MetaField.TYPE_FIELD)
                .build()
        );
    }
    
    @Override
    public String getProviderId() {
        return PROVIDER_ID;
    }
    
    @Override
    public int getPriority() {
        return 50; // IO attributes loaded after database attributes
    }
    
    @Override
    public String getDescription() {
        return "Provides serialization attributes for JSON, XML, CSV and other IO formats";
    }
}