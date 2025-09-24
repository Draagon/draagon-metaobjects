package com.draagon.meta.generator.service;

import com.draagon.meta.registry.MetaDataRegistry;

/**
 * JSON Schema Generator service class that extends MetaData types with JSON Schema-specific attributes.
 *
 * <p>This service adds attributes needed for JSON Schema generation to existing
 * MetaData types. All attribute names are defined as constants for type safety
 * and consistency across the codebase.</p>
 *
 * <h3>JSON Schema Attributes:</h3>
 * <ul>
 * <li><strong>JSON_TITLE:</strong> Schema title for documentation</li>
 * <li><strong>JSON_DESCRIPTION:</strong> Schema description</li>
 * <li><strong>JSON_FORMAT:</strong> String format constraint (email, date-time, etc.)</li>
 * <li><strong>JSON_ENUM:</strong> Enumeration values for fields</li>
 * <li><strong>JSON_MINIMUM:</strong> Minimum value for numeric fields</li>
 * <li><strong>JSON_MAXIMUM:</strong> Maximum value for numeric fields</li>
 * <li><strong>JSON_MIN_LENGTH:</strong> Minimum string length</li>
 * <li><strong>JSON_MAX_LENGTH:</strong> Maximum string length</li>
 * <li><strong>JSON_PATTERN:</strong> Regular expression pattern</li>
 * <li><strong>JSON_ADDITIONAL_PROPERTIES:</strong> Allow additional properties</li>
 * </ul>
 *
 * @since 6.0.0
 */
public class JsonSchemaGeneratorService {

    // JSON Schema Core Attributes
    public static final String JSON_TITLE = "jsonTitle";
    public static final String JSON_DESCRIPTION = "jsonDescription";
    public static final String JSON_DEFAULT = "jsonDefault";
    public static final String JSON_EXAMPLES = "jsonExamples";

    // JSON Schema String Constraints
    public static final String JSON_FORMAT = "jsonFormat";
    public static final String JSON_PATTERN = "jsonPattern";
    public static final String JSON_MIN_LENGTH = "jsonMinLength";
    public static final String JSON_MAX_LENGTH = "jsonMaxLength";

    // JSON Schema Numeric Constraints
    public static final String JSON_MINIMUM = "jsonMinimum";
    public static final String JSON_MAXIMUM = "jsonMaximum";
    public static final String JSON_EXCLUSIVE_MINIMUM = "jsonExclusiveMinimum";
    public static final String JSON_EXCLUSIVE_MAXIMUM = "jsonExclusiveMaximum";
    public static final String JSON_MULTIPLE_OF = "jsonMultipleOf";

    // JSON Schema Array Constraints
    public static final String JSON_MIN_ITEMS = "jsonMinItems";
    public static final String JSON_MAX_ITEMS = "jsonMaxItems";
    public static final String JSON_UNIQUE_ITEMS = "jsonUniqueItems";
    public static final String JSON_ITEMS_SCHEMA = "jsonItemsSchema";

    // JSON Schema Object Constraints
    public static final String JSON_ADDITIONAL_PROPERTIES = "jsonAdditionalProperties";
    public static final String JSON_MIN_PROPERTIES = "jsonMinProperties";
    public static final String JSON_MAX_PROPERTIES = "jsonMaxProperties";
    public static final String JSON_PROPERTY_NAMES = "jsonPropertyNames";

    // JSON Schema Enumeration
    public static final String JSON_ENUM = "jsonEnum";
    public static final String JSON_CONST = "jsonConst";

    // JSON Schema Conditional Logic
    public static final String JSON_IF = "jsonIf";
    public static final String JSON_THEN = "jsonThen";
    public static final String JSON_ELSE = "jsonElse";

    // JSON Schema Composition
    public static final String JSON_ALL_OF = "jsonAllOf";
    public static final String JSON_ANY_OF = "jsonAnyOf";
    public static final String JSON_ONE_OF = "jsonOneOf";
    public static final String JSON_NOT = "jsonNot";

    /**
     * Register JSON Schema-specific type extensions with the MetaData registry.
     *
     * <p>This method extends existing MetaData types with attributes needed
     * for JSON Schema generation. It follows the extension pattern of finding
     * existing types and adding optional attributes.</p>
     *
     * @param registry The MetaData registry to extend
     */
    public static void registerTypeExtensions(MetaDataRegistry registry) {
        try {
            // Extend field types for JSON Schema property generation
            registerFieldExtensions(registry);

            // Extend object types for JSON Schema object generation
            registerObjectExtensions(registry);

            // Extend attribute types for JSON Schema metadata
            registerAttributeExtensions(registry);

        } catch (Exception e) {
            // Log error but don't fail - service provider pattern should be resilient
            System.err.println("Warning: Failed to register JSON Schema type extensions: " + e.getMessage());
        }
    }

    /**
     * Extend field types with JSON Schema-specific attributes.
     */
    private static void registerFieldExtensions(MetaDataRegistry registry) {
        // String fields get JSON Schema string constraints
        registry.findType("field", "string")
            .optionalAttribute(JSON_TITLE, "string")
            .optionalAttribute(JSON_DESCRIPTION, "string")
            .optionalAttribute(JSON_FORMAT, "string")
            .optionalAttribute(JSON_PATTERN, "string")
            .optionalAttribute(JSON_MIN_LENGTH, "int")
            .optionalAttribute(JSON_MAX_LENGTH, "int")
            .optionalAttribute(JSON_ENUM, "stringarray")
            .optionalAttribute(JSON_DEFAULT, "string")
            .optionalAttribute(JSON_EXAMPLES, "stringarray");

        // Numeric fields get JSON Schema numeric constraints
        registry.findType("field", "int")
            .optionalAttribute(JSON_TITLE, "string")
            .optionalAttribute(JSON_DESCRIPTION, "string")
            .optionalAttribute(JSON_MINIMUM, "int")
            .optionalAttribute(JSON_MAXIMUM, "int")
            .optionalAttribute(JSON_EXCLUSIVE_MINIMUM, "boolean")
            .optionalAttribute(JSON_EXCLUSIVE_MAXIMUM, "boolean")
            .optionalAttribute(JSON_MULTIPLE_OF, "int")
            .optionalAttribute(JSON_DEFAULT, "int")
            .optionalAttribute(JSON_EXAMPLES, "stringarray");

        registry.findType("field", "long")
            .optionalAttribute(JSON_TITLE, "string")
            .optionalAttribute(JSON_DESCRIPTION, "string")
            .optionalAttribute(JSON_MINIMUM, "long")
            .optionalAttribute(JSON_MAXIMUM, "long")
            .optionalAttribute(JSON_EXCLUSIVE_MINIMUM, "boolean")
            .optionalAttribute(JSON_EXCLUSIVE_MAXIMUM, "boolean")
            .optionalAttribute(JSON_MULTIPLE_OF, "long")
            .optionalAttribute(JSON_DEFAULT, "long")
            .optionalAttribute(JSON_EXAMPLES, "stringarray");

        registry.findType("field", "double")
            .optionalAttribute(JSON_TITLE, "string")
            .optionalAttribute(JSON_DESCRIPTION, "string")
            .optionalAttribute(JSON_MINIMUM, "double")
            .optionalAttribute(JSON_MAXIMUM, "double")
            .optionalAttribute(JSON_EXCLUSIVE_MINIMUM, "boolean")
            .optionalAttribute(JSON_EXCLUSIVE_MAXIMUM, "boolean")
            .optionalAttribute(JSON_MULTIPLE_OF, "double")
            .optionalAttribute(JSON_DEFAULT, "double")
            .optionalAttribute(JSON_EXAMPLES, "stringarray");

        // Date fields get JSON Schema date/time constraints
        registry.findType("field", "date")
            .optionalAttribute(JSON_TITLE, "string")
            .optionalAttribute(JSON_DESCRIPTION, "string")
            .optionalAttribute(JSON_FORMAT, "string") // date-time, date, time
            .optionalAttribute(JSON_DEFAULT, "string")
            .optionalAttribute(JSON_EXAMPLES, "stringarray");

        // Boolean fields get JSON Schema boolean constraints
        registry.findType("field", "boolean")
            .optionalAttribute(JSON_TITLE, "string")
            .optionalAttribute(JSON_DESCRIPTION, "string")
            .optionalAttribute(JSON_DEFAULT, "boolean")
            .optionalAttribute(JSON_EXAMPLES, "stringarray");

        // Array fields get JSON Schema array constraints
        registry.findType("field", "stringarray")
            .optionalAttribute(JSON_TITLE, "string")
            .optionalAttribute(JSON_DESCRIPTION, "string")
            .optionalAttribute(JSON_MIN_ITEMS, "int")
            .optionalAttribute(JSON_MAX_ITEMS, "int")
            .optionalAttribute(JSON_UNIQUE_ITEMS, "boolean")
            .optionalAttribute(JSON_ITEMS_SCHEMA, "string")
            .optionalAttribute(JSON_DEFAULT, "stringarray")
            .optionalAttribute(JSON_EXAMPLES, "stringarray");
    }

    /**
     * Extend object types with JSON Schema object constraints.
     */
    private static void registerObjectExtensions(MetaDataRegistry registry) {
        registry.findType("object", "pojo")
            .optionalAttribute(JSON_TITLE, "string")
            .optionalAttribute(JSON_DESCRIPTION, "string")
            .optionalAttribute(JSON_ADDITIONAL_PROPERTIES, "boolean")
            .optionalAttribute(JSON_MIN_PROPERTIES, "int")
            .optionalAttribute(JSON_MAX_PROPERTIES, "int")
            .optionalAttribute(JSON_PROPERTY_NAMES, "string")
            .optionalAttribute(JSON_ALL_OF, "string")
            .optionalAttribute(JSON_ANY_OF, "string")
            .optionalAttribute(JSON_ONE_OF, "string");

        registry.findType("object", "proxy")
            .optionalAttribute(JSON_TITLE, "string")
            .optionalAttribute(JSON_DESCRIPTION, "string")
            .optionalAttribute(JSON_ADDITIONAL_PROPERTIES, "boolean");

        registry.findType("object", "map")
            .optionalAttribute(JSON_TITLE, "string")
            .optionalAttribute(JSON_DESCRIPTION, "string")
            .optionalAttribute(JSON_ADDITIONAL_PROPERTIES, "boolean")
            .optionalAttribute(JSON_MIN_PROPERTIES, "int")
            .optionalAttribute(JSON_MAX_PROPERTIES, "int");
    }

    /**
     * Extend attribute types with JSON Schema metadata support.
     */
    private static void registerAttributeExtensions(MetaDataRegistry registry) {
        registry.findType("attr", "string")
            .optionalAttribute(JSON_TITLE, "string")
            .optionalAttribute(JSON_DESCRIPTION, "string")
            .optionalAttribute(JSON_FORMAT, "string")
            .optionalAttribute(JSON_PATTERN, "string");

        registry.findType("attr", "int")
            .optionalAttribute(JSON_TITLE, "string")
            .optionalAttribute(JSON_DESCRIPTION, "string")
            .optionalAttribute(JSON_MINIMUM, "int")
            .optionalAttribute(JSON_MAXIMUM, "int");

        registry.findType("attr", "boolean")
            .optionalAttribute(JSON_TITLE, "string")
            .optionalAttribute(JSON_DESCRIPTION, "string");
    }

    /**
     * Check if an attribute name is JSON Schema-related.
     *
     * @param attributeName The attribute name to check
     * @return True if the attribute is JSON Schema-related
     */
    public static boolean isJsonSchemaAttribute(String attributeName) {
        return attributeName != null && attributeName.startsWith("json");
    }

    /**
     * Get standard JSON Schema format values for different field types.
     *
     * @param fieldType The MetaData field type
     * @return Suggested JSON Schema format, or null if none applies
     */
    public static String getStandardJsonFormat(String fieldType) {
        switch (fieldType) {
            case "date":
                return "date-time";
            case "string":
                return null; // Can be email, uri, etc. but no default
            default:
                return null;
        }
    }
}