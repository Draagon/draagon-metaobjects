package com.draagon.meta.generator.service;

import com.draagon.meta.registry.MetaDataRegistry;

/**
 * XSD Generator service class that extends MetaData types with XSD-specific attributes.
 *
 * <p>This service adds attributes needed for XSD schema generation to existing
 * MetaData types. All attribute names are defined as constants for type safety
 * and consistency across the codebase.</p>
 *
 * <h3>XSD Generation Attributes:</h3>
 * <ul>
 * <li><strong>XSD_ELEMENT_NAME:</strong> Custom XML element name override</li>
 * <li><strong>XSD_NAMESPACE:</strong> XML namespace for the element</li>
 * <li><strong>XSD_MIN_OCCURS:</strong> Minimum occurrence constraint</li>
 * <li><strong>XSD_MAX_OCCURS:</strong> Maximum occurrence constraint</li>
 * <li><strong>XSD_NILLABLE:</strong> Whether element can be nil</li>
 * <li><strong>XSD_ABSTRACT:</strong> Whether element is abstract</li>
 * <li><strong>XSD_TYPE_NAME:</strong> Custom XSD type name</li>
 * <li><strong>XSD_DOCUMENTATION:</strong> XSD documentation annotation</li>
 * </ul>
 *
 * @since 6.0.0
 */
public class XSDGeneratorService {

    // XSD Element Attributes
    public static final String XSD_ELEMENT_NAME = "xsdElementName";
    public static final String XSD_NAMESPACE = "xsdNamespace";
    public static final String XSD_MIN_OCCURS = "xsdMinOccurs";
    public static final String XSD_MAX_OCCURS = "xsdMaxOccurs";
    public static final String XSD_NILLABLE = "xsdNillable";
    public static final String XSD_ABSTRACT = "xsdAbstract";

    // XSD Type Attributes
    public static final String XSD_TYPE_NAME = "xsdTypeName";
    public static final String XSD_BASE_TYPE = "xsdBaseType";
    public static final String XSD_RESTRICTION = "xsdRestriction";
    public static final String XSD_EXTENSION = "xsdExtension";

    // XSD Documentation
    public static final String XSD_DOCUMENTATION = "xsdDocumentation";
    public static final String XSD_APPINFO = "xsdAppInfo";

    // XSD Complex Type Attributes
    public static final String XSD_MIXED_CONTENT = "xsdMixedContent";
    public static final String XSD_SEQUENCE = "xsdSequence";
    public static final String XSD_CHOICE = "xsdChoice";
    public static final String XSD_ALL = "xsdAll";

    /**
     * Register XSD-specific type extensions with the MetaData registry.
     *
     * <p>This method extends existing MetaData types with attributes needed
     * for XSD schema generation. It follows the extension pattern of finding
     * existing types and adding optional attributes.</p>
     *
     * @param registry The MetaData registry to extend
     */
    public static void registerTypeExtensions(MetaDataRegistry registry) {
        try {
            // Extend field types for XSD element generation
            registerFieldExtensions(registry);

            // Extend object types for XSD complex type generation
            registerObjectExtensions(registry);

            // Extend attribute types for XSD attribute generation
            registerAttributeExtensions(registry);

        } catch (Exception e) {
            // Log error but don't fail - service provider pattern should be resilient
            System.err.println("Warning: Failed to register XSD type extensions: " + e.getMessage());
        }
    }

    /**
     * Extend field types with XSD-specific attributes.
     */
    private static void registerFieldExtensions(MetaDataRegistry registry) {
        // String fields get XSD string constraints
        registry.findType("field", "string")
            .optionalAttribute(XSD_ELEMENT_NAME, "string")
            .optionalAttribute(XSD_MIN_OCCURS, "int")
            .optionalAttribute(XSD_MAX_OCCURS, "int")
            .optionalAttribute(XSD_NILLABLE, "boolean")
            .optionalAttribute(XSD_DOCUMENTATION, "string");

        // Numeric fields get XSD numeric constraints
        registry.findType("field", "int")
            .optionalAttribute(XSD_ELEMENT_NAME, "string")
            .optionalAttribute(XSD_MIN_OCCURS, "int")
            .optionalAttribute(XSD_MAX_OCCURS, "int")
            .optionalAttribute(XSD_RESTRICTION, "string")
            .optionalAttribute(XSD_DOCUMENTATION, "string");

        registry.findType("field", "long")
            .optionalAttribute(XSD_ELEMENT_NAME, "string")
            .optionalAttribute(XSD_MIN_OCCURS, "int")
            .optionalAttribute(XSD_MAX_OCCURS, "int")
            .optionalAttribute(XSD_RESTRICTION, "string")
            .optionalAttribute(XSD_DOCUMENTATION, "string");

        registry.findType("field", "double")
            .optionalAttribute(XSD_ELEMENT_NAME, "string")
            .optionalAttribute(XSD_MIN_OCCURS, "int")
            .optionalAttribute(XSD_MAX_OCCURS, "int")
            .optionalAttribute(XSD_RESTRICTION, "string")
            .optionalAttribute(XSD_DOCUMENTATION, "string");

        // Date fields get XSD date constraints
        registry.findType("field", "date")
            .optionalAttribute(XSD_ELEMENT_NAME, "string")
            .optionalAttribute(XSD_MIN_OCCURS, "int")
            .optionalAttribute(XSD_MAX_OCCURS, "int")
            .optionalAttribute(XSD_NILLABLE, "boolean")
            .optionalAttribute(XSD_DOCUMENTATION, "string");

        // Boolean fields get XSD boolean constraints
        registry.findType("field", "boolean")
            .optionalAttribute(XSD_ELEMENT_NAME, "string")
            .optionalAttribute(XSD_MIN_OCCURS, "int")
            .optionalAttribute(XSD_MAX_OCCURS, "int")
            .optionalAttribute(XSD_DOCUMENTATION, "string");
    }

    /**
     * Extend object types with XSD complex type attributes.
     */
    private static void registerObjectExtensions(MetaDataRegistry registry) {
        registry.findType("object", "pojo")
            .optionalAttribute(XSD_TYPE_NAME, "string")
            .optionalAttribute(XSD_NAMESPACE, "string")
            .optionalAttribute(XSD_ABSTRACT, "boolean")
            .optionalAttribute(XSD_MIXED_CONTENT, "boolean")
            .optionalAttribute(XSD_SEQUENCE, "boolean")
            .optionalAttribute(XSD_CHOICE, "boolean")
            .optionalAttribute(XSD_DOCUMENTATION, "string")
            .optionalAttribute(XSD_APPINFO, "string");

        registry.findType("object", "proxy")
            .optionalAttribute(XSD_TYPE_NAME, "string")
            .optionalAttribute(XSD_NAMESPACE, "string")
            .optionalAttribute(XSD_ABSTRACT, "boolean")
            .optionalAttribute(XSD_DOCUMENTATION, "string");

        registry.findType("object", "map")
            .optionalAttribute(XSD_TYPE_NAME, "string")
            .optionalAttribute(XSD_NAMESPACE, "string")
            .optionalAttribute(XSD_MIXED_CONTENT, "boolean")
            .optionalAttribute(XSD_DOCUMENTATION, "string");
    }

    /**
     * Extend attribute types with XSD attribute generation support.
     */
    private static void registerAttributeExtensions(MetaDataRegistry registry) {
        registry.findType("attr", "string")
            .optionalAttribute(XSD_ELEMENT_NAME, "string")
            .optionalAttribute(XSD_NAMESPACE, "string")
            .optionalAttribute(XSD_DOCUMENTATION, "string");

        registry.findType("attr", "int")
            .optionalAttribute(XSD_ELEMENT_NAME, "string")
            .optionalAttribute(XSD_RESTRICTION, "string")
            .optionalAttribute(XSD_DOCUMENTATION, "string");

        registry.findType("attr", "boolean")
            .optionalAttribute(XSD_ELEMENT_NAME, "string")
            .optionalAttribute(XSD_DOCUMENTATION, "string");
    }

    /**
     * Check if an attribute name is XSD-related.
     *
     * @param attributeName The attribute name to check
     * @return True if the attribute is XSD-related
     */
    public static boolean isXSDAttribute(String attributeName) {
        return attributeName != null && attributeName.startsWith("xsd");
    }
}