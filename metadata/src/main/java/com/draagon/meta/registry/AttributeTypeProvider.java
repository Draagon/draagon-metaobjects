package com.draagon.meta.registry;

import com.draagon.meta.attr.*;
import com.draagon.meta.loader.MetaDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Provider for all attribute types in the MetaObjects framework.
 *
 * <p>This provider registers the complete attribute type hierarchy:</p>
 * <ul>
 *   <li><strong>attr.base:</strong> Base attribute type with common attribute functionality</li>
 *   <li><strong>Primitive Attributes:</strong> string, int, long, double, boolean, class</li>
 *   <li><strong>Complex Attributes:</strong> properties, stringArray</li>
 * </ul>
 *
 * <p>All concrete attribute types inherit from attr.base, which inherits from metadata.base,
 * providing a clean inheritance hierarchy with shared capabilities.</p>
 *
 * <h3>Attribute Type Hierarchy:</h3>
 * <pre>
 * metadata.base (MetaDataLoader)
 *     └── attr.base (MetaAttribute) - common attribute functionality
 *         ├── attr.string (StringAttribute) - string value storage
 *         ├── attr.int (IntAttribute) - integer value storage
 *         ├── attr.long (LongAttribute) - long value storage
 *         ├── attr.double (DoubleAttribute) - double value storage
 *         ├── attr.boolean (BooleanAttribute) - boolean value storage
 *         ├── attr.class (ClassAttribute) - class reference storage
 *         ├── attr.properties (PropertiesAttribute) - key-value pair storage
 *         └── attr.stringArray (StringArrayAttribute) - string array storage
 * </pre>
 *
 * @since 6.3.0
 */
public class AttributeTypeProvider implements MetaDataTypeProvider {

    private static final Logger log = LoggerFactory.getLogger(AttributeTypeProvider.class);

    @Override
    public void registerTypes(MetaDataRegistry registry) throws Exception {
        log.info("Registering attribute types...");

        // Register attr.base - the base type for all attributes
        registerAttributeBase(registry);

        // Register primitive attribute types
        registerPrimitiveAttributes(registry);

        // Register complex attribute types
        registerComplexAttributes(registry);

        log.info("Successfully registered {} attribute types", getAttributeTypeCount());
    }

    /**
     * Register attr.base - the foundation for all attribute types
     */
    private void registerAttributeBase(MetaDataRegistry registry) {
        registry.registerType(MetaAttribute.class, def -> def
            .type(MetaAttribute.TYPE_ATTR).subType(MetaAttribute.SUBTYPE_BASE)
            .description("Base attribute metadata with common attribute functionality")
            .inheritsFrom(MetaDataLoader.TYPE_METADATA, MetaDataLoader.SUBTYPE_BASE)

            // BIDIRECTIONAL CONSTRAINT: Attributes can be placed under any metadata type
            .acceptsParents(MetaDataLoader.TYPE_METADATA, "*")
            .acceptsParents("field", "*")    // Attributes commonly placed under fields
            .acceptsParents("object", "*")   // Attributes commonly placed under objects
            .acceptsParents("key", "*")      // Attributes can be placed under keys
            .acceptsParents("validator", "*") // Attributes can be placed under validators
            .acceptsParents("view", "*")     // Attributes can be placed under views

            // No attribute-specific children - attributes are leaf nodes in most cases
        );
    }

    /**
     * Register primitive attribute types
     */
    private void registerPrimitiveAttributes(MetaDataRegistry registry) {
        // String attribute for text values
        registry.registerType(StringAttribute.class, def -> def
            .type(MetaAttribute.TYPE_ATTR).subType(StringAttribute.SUBTYPE_STRING)
            .description("String attribute for text value storage")
            .inheritsFrom(MetaAttribute.TYPE_ATTR, MetaAttribute.SUBTYPE_BASE)
            // No string-specific children - pure value storage
        );

        // Integer attribute for numeric values
        registry.registerType(IntAttribute.class, def -> def
            .type(MetaAttribute.TYPE_ATTR).subType(IntAttribute.SUBTYPE_INT)
            .description("Integer attribute for numeric value storage")
            .inheritsFrom(MetaAttribute.TYPE_ATTR, MetaAttribute.SUBTYPE_BASE)
            // No int-specific children - pure value storage
        );

        // Long attribute for large numeric values
        registry.registerType(LongAttribute.class, def -> def
            .type(MetaAttribute.TYPE_ATTR).subType(LongAttribute.SUBTYPE_LONG)
            .description("Long attribute for large numeric value storage")
            .inheritsFrom(MetaAttribute.TYPE_ATTR, MetaAttribute.SUBTYPE_BASE)
            // No long-specific children - pure value storage
        );

        // Double attribute for decimal values
        registry.registerType(DoubleAttribute.class, def -> def
            .type(MetaAttribute.TYPE_ATTR).subType(DoubleAttribute.SUBTYPE_DOUBLE)
            .description("Double attribute for decimal value storage")
            .inheritsFrom(MetaAttribute.TYPE_ATTR, MetaAttribute.SUBTYPE_BASE)
            // No double-specific children - pure value storage
        );

        // Boolean attribute for true/false values
        registry.registerType(BooleanAttribute.class, def -> def
            .type(MetaAttribute.TYPE_ATTR).subType(BooleanAttribute.SUBTYPE_BOOLEAN)
            .description("Boolean attribute for true/false value storage")
            .inheritsFrom(MetaAttribute.TYPE_ATTR, MetaAttribute.SUBTYPE_BASE)
            // No boolean-specific children - pure value storage
        );

        // Class attribute for Java class references
        registry.registerType(ClassAttribute.class, def -> def
            .type(MetaAttribute.TYPE_ATTR).subType(ClassAttribute.SUBTYPE_CLASS)
            .description("Class attribute for Java class reference storage")
            .inheritsFrom(MetaAttribute.TYPE_ATTR, MetaAttribute.SUBTYPE_BASE)
            // No class-specific children - pure value storage
        );
    }

    /**
     * Register complex attribute types
     */
    private void registerComplexAttributes(MetaDataRegistry registry) {
        // Properties attribute for key-value pairs
        registry.registerType(PropertiesAttribute.class, def -> def
            .type(MetaAttribute.TYPE_ATTR).subType(PropertiesAttribute.SUBTYPE_PROPERTIES)
            .description("Properties attribute for key-value pair storage")
            .inheritsFrom(MetaAttribute.TYPE_ATTR, MetaAttribute.SUBTYPE_BASE)
            // Properties could potentially have child attributes, but typically don't
        );

        // String array attribute for collections of strings
        registry.registerType(StringArrayAttribute.class, def -> def
            .type(MetaAttribute.TYPE_ATTR).subType(StringArrayAttribute.SUBTYPE_STRING_ARRAY)
            .description("String array attribute for string collection storage")
            .inheritsFrom(MetaAttribute.TYPE_ATTR, MetaAttribute.SUBTYPE_BASE)
            // No string array-specific children - collection value storage
        );
    }

    @Override
    public String getProviderName() {
        return "attribute-types";
    }

    @Override
    public Set<String> getDependencies() {
        // Attribute types depend on core types being loaded first
        return Set.of("core-types");
    }

    @Override
    public int getPriority() {
        // High priority - fundamental types used by fields
        return 750;
    }

    @Override
    public boolean supportsOSGi() {
        return true;
    }

    @Override
    public String getDescription() {
        return "All MetaAttribute types (attr.base + 7 concrete attribute types)";
    }

    /**
     * Get the total number of attribute types registered by this provider
     */
    private int getAttributeTypeCount() {
        return 8; // attr.base + 7 concrete attribute types
    }
}