package com.draagon.meta.object.service;

import com.draagon.meta.registry.MetaDataRegistry;

/**
 * Object Creation service class that extends MetaData types with object instantiation attributes.
 *
 * <p>This service adds attributes needed for ValueObject and DataObject creation to existing
 * MetaData types. All attribute names are defined as constants for type safety
 * and consistency across the codebase.</p>
 *
 * <h3>Object Creation Attributes:</h3>
 * <ul>
 * <li><strong>OBJECT_FACTORY:</strong> Factory class for object creation</li>
 * <li><strong>OBJECT_BUILDER:</strong> Builder class for complex object construction</li>
 * <li><strong>OBJECT_IMMUTABLE:</strong> Whether objects are immutable after creation</li>
 * <li><strong>OBJECT_CACHEABLE:</strong> Whether objects can be cached</li>
 * <li><strong>OBJECT_VALIDATION:</strong> Validation strategy for object creation</li>
 * <li><strong>OBJECT_SERIALIZATION:</strong> Serialization strategy</li>
 * </ul>
 *
 * @since 6.0.0
 */
public class ObjectCreationService {

    // Object Creation Attributes
    public static final String OBJECT_FACTORY = "objectFactory";
    public static final String OBJECT_BUILDER = "objectBuilder";
    public static final String OBJECT_CONSTRUCTOR = "objectConstructor";
    public static final String OBJECT_INITIALIZER = "objectInitializer";

    // Object Lifecycle Attributes
    public static final String OBJECT_IMMUTABLE = "objectImmutable";
    public static final String OBJECT_CACHEABLE = "objectCacheable";
    public static final String OBJECT_CLONEABLE = "objectCloneable";
    public static final String OBJECT_SINGLETON = "objectSingleton";

    // Object Validation Attributes
    public static final String OBJECT_VALIDATION = "objectValidation";
    public static final String OBJECT_VALIDATE_ON_CREATION = "objectValidateOnCreation";
    public static final String OBJECT_VALIDATE_ON_UPDATE = "objectValidateOnUpdate";
    public static final String OBJECT_AUTO_VALIDATION = "objectAutoValidation";

    // Object Serialization Attributes
    public static final String OBJECT_SERIALIZATION = "objectSerialization";
    public static final String OBJECT_JSON_SERIALIZABLE = "objectJsonSerializable";
    public static final String OBJECT_XML_SERIALIZABLE = "objectXmlSerializable";
    public static final String OBJECT_BINARY_SERIALIZABLE = "objectBinarySerializable";

    // Object Metadata Attributes
    public static final String OBJECT_VERSION = "objectVersion";
    public static final String OBJECT_CREATED_BY = "objectCreatedBy";
    public static final String OBJECT_CREATED_DATE = "objectCreatedDate";
    public static final String OBJECT_MODIFIED_DATE = "objectModifiedDate";

    // Object Access Attributes
    public static final String OBJECT_ACCESS_PATTERN = "objectAccessPattern";
    public static final String OBJECT_THREAD_SAFE = "objectThreadSafe";
    public static final String OBJECT_READ_ONLY = "objectReadOnly";
    public static final String OBJECT_LAZY_LOADING = "objectLazyLoading";

    // ValueObject Specific Attributes
    public static final String VALUE_OBJECT_EQUALS_BY = "valueObjectEqualsBy";
    public static final String VALUE_OBJECT_HASHCODE_BY = "valueObjectHashcodeBy";
    public static final String VALUE_OBJECT_COMPARABLE = "valueObjectComparable";
    public static final String VALUE_OBJECT_NATURAL_ORDER = "valueObjectNaturalOrder";

    // DataObject Specific Attributes
    public static final String DATA_OBJECT_WRAPPER = "dataObjectWrapper";
    public static final String DATA_OBJECT_PROXY_TYPE = "dataObjectProxyType";
    public static final String DATA_OBJECT_INTERFACE = "dataObjectInterface";
    public static final String DATA_OBJECT_IMPLEMENTATION = "dataObjectImplementation";

    /**
     * Register Object Creation-specific type extensions with the MetaData registry.
     *
     * <p>This method extends existing MetaData types with attributes needed
     * for ValueObject and DataObject creation and management. It follows the extension
     * pattern of finding existing types and adding optional attributes.</p>
     *
     * @param registry The MetaData registry to extend
     */
    public static void registerTypeExtensions(MetaDataRegistry registry) {
        try {
            // Extend object types for creation and lifecycle management
            registerObjectExtensions(registry);

            // Extend field types for object member attributes
            registerFieldExtensions(registry);

        } catch (Exception e) {
            // Log error but don't fail - service provider pattern should be resilient
            System.err.println("Warning: Failed to register Object Creation type extensions: " + e.getMessage());
        }
    }

    /**
     * Extend object types with creation and lifecycle attributes.
     */
    private static void registerObjectExtensions(MetaDataRegistry registry) {
        // POJO objects get comprehensive creation attributes
        registry.findType("object", "pojo")
            .optionalAttribute(OBJECT_FACTORY, "string")
            .optionalAttribute(OBJECT_BUILDER, "string")
            .optionalAttribute(OBJECT_CONSTRUCTOR, "string")
            .optionalAttribute(OBJECT_IMMUTABLE, "boolean")
            .optionalAttribute(OBJECT_CACHEABLE, "boolean")
            .optionalAttribute(OBJECT_CLONEABLE, "boolean")
            .optionalAttribute(OBJECT_SINGLETON, "boolean")
            .optionalAttribute(OBJECT_VALIDATION, "string")
            .optionalAttribute(OBJECT_VALIDATE_ON_CREATION, "boolean")
            .optionalAttribute(OBJECT_VALIDATE_ON_UPDATE, "boolean")
            .optionalAttribute(OBJECT_JSON_SERIALIZABLE, "boolean")
            .optionalAttribute(OBJECT_XML_SERIALIZABLE, "boolean")
            .optionalAttribute(OBJECT_THREAD_SAFE, "boolean")
            .optionalAttribute(OBJECT_READ_ONLY, "boolean")
            .optionalAttribute(VALUE_OBJECT_EQUALS_BY, "stringarray")
            .optionalAttribute(VALUE_OBJECT_COMPARABLE, "boolean")
            .optionalAttribute(VALUE_OBJECT_NATURAL_ORDER, "string");

        // Proxy objects get proxy-specific attributes
        registry.findType("object", "proxy")
            .optionalAttribute(OBJECT_FACTORY, "string")
            .optionalAttribute(OBJECT_IMMUTABLE, "boolean")
            .optionalAttribute(OBJECT_CACHEABLE, "boolean")
            .optionalAttribute(OBJECT_VALIDATION, "string")
            .optionalAttribute(OBJECT_JSON_SERIALIZABLE, "boolean")
            .optionalAttribute(OBJECT_THREAD_SAFE, "boolean")
            .optionalAttribute(DATA_OBJECT_WRAPPER, "string")
            .optionalAttribute(DATA_OBJECT_PROXY_TYPE, "string")
            .optionalAttribute(DATA_OBJECT_INTERFACE, "string")
            .optionalAttribute(DATA_OBJECT_IMPLEMENTATION, "string");

        // Map objects get map-specific attributes
        registry.findType("object", "map")
            .optionalAttribute(OBJECT_FACTORY, "string")
            .optionalAttribute(OBJECT_IMMUTABLE, "boolean")
            .optionalAttribute(OBJECT_CACHEABLE, "boolean")
            .optionalAttribute(OBJECT_VALIDATION, "string")
            .optionalAttribute(OBJECT_JSON_SERIALIZABLE, "boolean")
            .optionalAttribute(OBJECT_THREAD_SAFE, "boolean");
    }

    /**
     * Extend field types with object member attributes.
     */
    private static void registerFieldExtensions(MetaDataRegistry registry) {
        // String fields in objects
        registry.findType("field", "string")
            .optionalAttribute(OBJECT_LAZY_LOADING, "boolean")
            .optionalAttribute(VALUE_OBJECT_EQUALS_BY, "boolean")
            .optionalAttribute(VALUE_OBJECT_HASHCODE_BY, "boolean");

        // Numeric fields in objects
        registry.findType("field", "int")
            .optionalAttribute(OBJECT_LAZY_LOADING, "boolean")
            .optionalAttribute(VALUE_OBJECT_EQUALS_BY, "boolean")
            .optionalAttribute(VALUE_OBJECT_HASHCODE_BY, "boolean");

        registry.findType("field", "long")
            .optionalAttribute(OBJECT_LAZY_LOADING, "boolean")
            .optionalAttribute(VALUE_OBJECT_EQUALS_BY, "boolean")
            .optionalAttribute(VALUE_OBJECT_HASHCODE_BY, "boolean");

        // Date fields in objects
        registry.findType("field", "date")
            .optionalAttribute(OBJECT_LAZY_LOADING, "boolean")
            .optionalAttribute(VALUE_OBJECT_EQUALS_BY, "boolean")
            .optionalAttribute(VALUE_OBJECT_HASHCODE_BY, "boolean");

        // Boolean fields in objects
        registry.findType("field", "boolean")
            .optionalAttribute(VALUE_OBJECT_EQUALS_BY, "boolean")
            .optionalAttribute(VALUE_OBJECT_HASHCODE_BY, "boolean");
    }

    /**
     * Check if an attribute name is object creation-related.
     *
     * @param attributeName The attribute name to check
     * @return True if the attribute is object creation-related
     */
    public static boolean isObjectCreationAttribute(String attributeName) {
        return attributeName != null &&
               (attributeName.startsWith("object") ||
                attributeName.startsWith("valueObject") ||
                attributeName.startsWith("dataObject"));
    }

    /**
     * Get standard object access patterns.
     *
     * @return Array of standard access pattern values
     */
    public static String[] getStandardAccessPatterns() {
        return new String[]{"read-write", "read-only", "write-only", "immutable", "builder"};
    }

    /**
     * Get standard validation strategies.
     *
     * @return Array of standard validation strategy values
     */
    public static String[] getStandardValidationStrategies() {
        return new String[]{"none", "basic", "strict", "custom", "constraint-based"};
    }
}