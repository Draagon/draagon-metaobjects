package com.draagon.meta.registry;

import com.draagon.meta.attr.BooleanAttribute;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.field.*;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.validator.MetaValidator;
import com.draagon.meta.view.MetaView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Provider for all field types in the MetaObjects framework.
 *
 * <p>This provider registers the complete field type hierarchy:</p>
 * <ul>
 *   <li><strong>field.base:</strong> Base field type with common field attributes</li>
 *   <li><strong>Primitive Fields:</strong> string, int, long, double, float, boolean, byte, short</li>
 *   <li><strong>Complex Fields:</strong> date, class, object, objectArray, stringArray</li>
 * </ul>
 *
 * <p>All concrete field types inherit from field.base, which inherits from metadata.base,
 * providing a clean inheritance hierarchy with shared attributes and capabilities.</p>
 *
 * <h3>Field Type Hierarchy:</h3>
 * <pre>
 * metadata.base (MetaDataLoader)
 *     └── field.base (MetaField) - common field attributes (required, defaultValue, defaultView)
 *         ├── field.string (StringField) - pattern, maxLength, minLength
 *         ├── field.int (IntegerField) - minValue, maxValue
 *         ├── field.long (LongField) - minValue, maxValue
 *         ├── field.double (DoubleField) - minValue, maxValue, precision
 *         ├── field.float (FloatField) - minValue, maxValue, precision
 *         ├── field.boolean (BooleanField) - no additional attributes
 *         ├── field.byte (ByteField) - minValue, maxValue
 *         ├── field.short (ShortField) - minValue, maxValue
 *         ├── field.date (DateField) - format, timezone
 *         ├── field.class (ClassField) - baseClass, interfaces
 *         ├── field.object (ObjectField) - objectRef, lazy loading
 *         ├── field.objectArray (ObjectArrayField) - objectRef, maxSize
 *         └── field.stringArray (StringArrayField) - maxSize, itemPattern
 * </pre>
 *
 * @since 6.3.0
 */
public class FieldTypeProvider implements MetaDataTypeProvider {

    private static final Logger log = LoggerFactory.getLogger(FieldTypeProvider.class);

    @Override
    public void registerTypes(MetaDataRegistry registry) throws Exception {
        log.info("Registering field types...");

        // Register field.base - the base type for all fields
        registerFieldBase(registry);

        // Register primitive field types
        registerPrimitiveFields(registry);

        // Register complex field types
        registerComplexFields(registry);

        log.info("Successfully registered {} field types", getFieldTypeCount());
    }

    /**
     * Register field.base - the foundation for all field types
     */
    private void registerFieldBase(MetaDataRegistry registry) {
        registry.registerType(MetaField.class, def -> def
            .type(MetaField.TYPE_FIELD).subType(MetaField.SUBTYPE_BASE)
            .description("Base field metadata with common field attributes")
            .inheritsFrom(MetaDataLoader.TYPE_METADATA, MetaDataLoader.SUBTYPE_BASE)

            // BIDIRECTIONAL CONSTRAINT: Fields accept metadata.base, loader types, and object types as parents
            .acceptsParents(MetaDataLoader.TYPE_METADATA, MetaDataLoader.SUBTYPE_BASE)
            .acceptsParents(MetaDataLoader.TYPE_LOADER, "*")  // Fields can be placed under any loader type
            .acceptsParents("object", "*")                   // Fields can be placed under any object type

            // FIELD-SPECIFIC ATTRIBUTES (core field concepts)
            .acceptsNamedAttributes(BooleanAttribute.SUBTYPE_BOOLEAN, MetaField.ATTR_REQUIRED)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, MetaField.ATTR_DEFAULT_VALUE)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, MetaField.ATTR_DEFAULT_VIEW)

            // COMMON FIELD ATTRIBUTES (used across many field types)
            // NOTE: "isKey" is now calculated based on PrimaryKey metadata, not an explicit attribute
            .acceptsNamedAttributes(BooleanAttribute.SUBTYPE_BOOLEAN, "isOptional") // Mark optional fields
            .acceptsNamedAttributes(BooleanAttribute.SUBTYPE_BOOLEAN, "isReadOnly") // Mark read-only fields

            // UNIVERSAL ATTRIBUTE SUPPORT (for extensibility like other base types)
            .acceptsChildren("attr", "*")                        // Any attribute type - allows for plugin extensibility
            .acceptsChildren("attr", "string")                   // Specifically accept string attributes with any name

            // FIELD-SPECIFIC CHILDREN
            .acceptsChildren(MetaValidator.TYPE_VALIDATOR, "*")  // Fields can have validators
            .acceptsChildren(MetaView.TYPE_VIEW, "*")            // Fields can have views
        );
    }

    /**
     * Register primitive field types
     */
    private void registerPrimitiveFields(MetaDataRegistry registry) {
        // String field with pattern and length validation
        registry.registerType(StringField.class, def -> def
            .type(MetaField.TYPE_FIELD).subType(StringField.SUBTYPE_STRING)
            .description("String field with length and pattern validation")
            .inheritsFrom(MetaField.TYPE_FIELD, MetaField.SUBTYPE_BASE)

            // STRING-SPECIFIC ATTRIBUTES (core only)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, StringField.ATTR_PATTERN)
            .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, StringField.ATTR_MAX_LENGTH)
            .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, StringField.ATTR_MIN_LENGTH)

            // EXTENSIBILITY: Accept arbitrary string attributes for schema generation
            .acceptsChildren("attr", "string")
        );

        // Integer field with numeric range validation
        registry.registerType(IntegerField.class, def -> def
            .type(MetaField.TYPE_FIELD).subType(IntegerField.SUBTYPE_INT)
            .description("Integer field with range validation")
            .inheritsFrom(MetaField.TYPE_FIELD, MetaField.SUBTYPE_BASE)

            // INTEGER-SPECIFIC ATTRIBUTES (core only)
            .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, IntegerField.ATTR_MIN_VALUE)
            .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, IntegerField.ATTR_MAX_VALUE)

            // EXTENSIBILITY: Accept arbitrary string attributes for schema generation
            .acceptsChildren("attr", "string")
        );

        // Long field with numeric range validation
        registry.registerType(LongField.class, def -> def
            .type(MetaField.TYPE_FIELD).subType(LongField.SUBTYPE_LONG)
            .description("Long field with range validation")
            .inheritsFrom(MetaField.TYPE_FIELD, MetaField.SUBTYPE_BASE)

            // LONG-SPECIFIC ATTRIBUTES (core only)
            .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, LongField.ATTR_MIN_VALUE)
            .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, LongField.ATTR_MAX_VALUE)

            // EXTENSIBILITY: Accept arbitrary string attributes for schema generation
            .acceptsChildren("attr", "string")
        );

        // Double field with numeric range and precision
        registry.registerType(DoubleField.class, def -> def
            .type(MetaField.TYPE_FIELD).subType(DoubleField.SUBTYPE_DOUBLE)
            .description("Double field with range and precision validation")
            .inheritsFrom(MetaField.TYPE_FIELD, MetaField.SUBTYPE_BASE)

            // DOUBLE-SPECIFIC ATTRIBUTES (core only)
            .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, DoubleField.ATTR_MIN_VALUE)
            .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, DoubleField.ATTR_MAX_VALUE)
            .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, DoubleField.ATTR_PRECISION)

            // EXTENSIBILITY: Accept arbitrary string attributes for schema generation
            .acceptsChildren("attr", "string")
        );

        // Float field with numeric range and precision
        registry.registerType(FloatField.class, def -> def
            .type(MetaField.TYPE_FIELD).subType(FloatField.SUBTYPE_FLOAT)
            .description("Float field with range and precision validation")
            .inheritsFrom(MetaField.TYPE_FIELD, MetaField.SUBTYPE_BASE)

            // FLOAT-SPECIFIC ATTRIBUTES (core only)
            .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, FloatField.ATTR_MIN_VALUE)
            .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, FloatField.ATTR_MAX_VALUE)
            .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, FloatField.ATTR_PRECISION)

            // EXTENSIBILITY: Accept arbitrary string attributes for schema generation
            .acceptsChildren("attr", "string")
        );

        // Boolean field (no additional attributes)
        registry.registerType(BooleanField.class, def -> def
            .type(MetaField.TYPE_FIELD).subType(BooleanField.SUBTYPE_BOOLEAN)
            .description("Boolean field for true/false values")
            .inheritsFrom(MetaField.TYPE_FIELD, MetaField.SUBTYPE_BASE)
            // No boolean-specific attributes

            // EXTENSIBILITY: Accept arbitrary string attributes for schema generation
            .acceptsChildren("attr", "string")
        );

        // Byte field with numeric range
        registry.registerType(ByteField.class, def -> def
            .type(MetaField.TYPE_FIELD).subType(ByteField.SUBTYPE_BYTE)
            .description("Byte field with range validation")
            .inheritsFrom(MetaField.TYPE_FIELD, MetaField.SUBTYPE_BASE)

            // BYTE-SPECIFIC ATTRIBUTES (core only)
            .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, ByteField.ATTR_MIN_VALUE)
            .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, ByteField.ATTR_MAX_VALUE)

            // EXTENSIBILITY: Accept arbitrary string attributes for schema generation
            .acceptsChildren("attr", "string")
        );

        // Short field with numeric range
        registry.registerType(ShortField.class, def -> def
            .type(MetaField.TYPE_FIELD).subType(ShortField.SUBTYPE_SHORT)
            .description("Short field with range validation")
            .inheritsFrom(MetaField.TYPE_FIELD, MetaField.SUBTYPE_BASE)

            // SHORT-SPECIFIC ATTRIBUTES (core only)
            .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, ShortField.ATTR_MIN_VALUE)
            .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, ShortField.ATTR_MAX_VALUE)

            // EXTENSIBILITY: Accept arbitrary string attributes for schema generation
            .acceptsChildren("attr", "string")
        );
    }

    /**
     * Register complex field types
     */
    private void registerComplexFields(MetaDataRegistry registry) {
        // Date field with formatting options
        registry.registerType(DateField.class, def -> def
            .type(MetaField.TYPE_FIELD).subType(DateField.SUBTYPE_DATE)
            .description("Date field with format support")
            .inheritsFrom(MetaField.TYPE_FIELD, MetaField.SUBTYPE_BASE)

            // DATE-SPECIFIC ATTRIBUTES (core only)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, DateField.ATTR_FORMAT)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, DateField.ATTR_DATE_FORMAT)

            // EXTENSIBILITY: Accept arbitrary string attributes for schema generation
            .acceptsChildren("attr", "string")
        );

        // Class field for Java class references
        registry.registerType(ClassField.class, def -> def
            .type(MetaField.TYPE_FIELD).subType(ClassField.SUBTYPE_CLASS)
            .description("Class field for Java class references")
            .inheritsFrom(MetaField.TYPE_FIELD, MetaField.SUBTYPE_BASE)
            // No additional class-specific attributes currently defined

            // EXTENSIBILITY: Accept arbitrary string attributes for schema generation
            .acceptsChildren("attr", "string")
        );

        // Object field for complex object references
        registry.registerType(ObjectField.class, def -> def
            .type(MetaField.TYPE_FIELD).subType(ObjectField.SUBTYPE_OBJECT)
            .description("Object field for complex object references")
            .inheritsFrom(MetaField.TYPE_FIELD, MetaField.SUBTYPE_BASE)

            // OBJECT-SPECIFIC ATTRIBUTES (core only)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ObjectField.ATTR_OBJECTREF)

            // EXTENSIBILITY: Accept arbitrary string attributes for schema generation
            .acceptsChildren("attr", "string")
        );

        // Object array field
        registry.registerType(ObjectArrayField.class, def -> def
            .type(MetaField.TYPE_FIELD).subType(ObjectArrayField.SUBTYPE_OBJECT_ARRAY)
            .description("Object array field for collections of complex objects")
            .inheritsFrom(MetaField.TYPE_FIELD, MetaField.SUBTYPE_BASE)

            // OBJECT ARRAY-SPECIFIC ATTRIBUTES (core only)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ObjectArrayField.ATTR_OBJECT_REF)

            // EXTENSIBILITY: Accept arbitrary string attributes for schema generation
            .acceptsChildren("attr", "string")
        );

        // String array field
        registry.registerType(StringArrayField.class, def -> def
            .type(MetaField.TYPE_FIELD).subType(StringArrayField.SUBTYPE_STRING_ARRAY)
            .description("String array field for collections of strings")
            .inheritsFrom(MetaField.TYPE_FIELD, MetaField.SUBTYPE_BASE)
            // No additional string array-specific attributes currently defined

            // EXTENSIBILITY: Accept arbitrary string attributes for schema generation
            .acceptsChildren("attr", "string")
        );
    }

    @Override
    public String getProviderName() {
        return "field-types";
    }

    @Override
    public Set<String> getDependencies() {
        // Field types depend on core types being loaded first
        return Set.of("core-types");
    }

    @Override
    public int getPriority() {
        // High priority - fundamental types
        return 800;
    }

    @Override
    public boolean supportsOSGi() {
        return true;
    }

    @Override
    public String getDescription() {
        return "All MetaField types (field.base + 12 concrete field types)";
    }

    /**
     * Get the total number of field types registered by this provider
     */
    private int getFieldTypeCount() {
        return 13; // field.base + 12 concrete field types
    }
}