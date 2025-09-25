package com.draagon.meta.object;

import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataTypeProvider;

/**
 * Core objects MetaData type provider that registers DataObject and ValueObject
 * related attributes and type extensions.
 *
 * This provider handles registration for DataObject and ValueObject classes
 * by delegating to their respective registration methods.
 *
 * Priority: 50 (after base types, before other extensions)
 */
public class CoreObjectsMetaDataProvider implements MetaDataTypeProvider {

    @Override
    public String getProviderId() {
        return "core-object-extensions";
    }

    @Override
    public String[] getDependencies() {
        // Depends on object-types since it extends object.base and object.pojo
        return new String[]{"object-types"};
    }

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // Delegate to DataObject and ValueObject registration methods
        DataObjectExtensions.registerDataObjectAttributes(registry);
        ValueObjectExtensions.registerValueObjectAttributes(registry);
    }

    /**
     * DataObject extensions and attributes
     */
    public static class DataObjectExtensions {

        // DataObject attribute constants
        public static final String DATA_BUILDER_CLASS = "dataBuilderClass";
        public static final String DATA_IMMUTABLE = "dataImmutable";
        public static final String DATA_VALIDATION_MODE = "dataValidationMode";
        public static final String DATA_DEFAULT_VALUES = "dataDefaultValues";

        public static void registerDataObjectAttributes(MetaDataRegistry registry) {
            // Add DataObject-specific attributes to objects
            registry.findType("object", "base")
                .optionalAttribute(DATA_BUILDER_CLASS, "string")
                .optionalAttribute(DATA_IMMUTABLE, "boolean")
                .optionalAttribute(DATA_VALIDATION_MODE, "string")
                .optionalAttribute(DATA_DEFAULT_VALUES, "string");

            registry.findType("object", "pojo")
                .optionalAttribute(DATA_BUILDER_CLASS, "string")
                .optionalAttribute(DATA_IMMUTABLE, "boolean");
        }
    }

    /**
     * ValueObject extensions and attributes
     */
    public static class ValueObjectExtensions {

        // ValueObject attribute constants
        public static final String VALUE_OBJECT_TYPE = "valueObjectType";
        public static final String VALUE_EQUALS_BY = "valueEqualsBy";
        public static final String VALUE_HASHCODE_BY = "valueHashCodeBy";
        public static final String VALUE_TOSTRING_FORMAT = "valueToStringFormat";
        public static final String VALUE_EXTENSIONS_ENABLED = "valueExtensionsEnabled";

        public static void registerValueObjectAttributes(MetaDataRegistry registry) {
            // Add ValueObject-specific attributes to objects
            registry.findType("object", "base")
                .optionalAttribute(VALUE_OBJECT_TYPE, "string")
                .optionalAttribute(VALUE_EQUALS_BY, "string")
                .optionalAttribute(VALUE_HASHCODE_BY, "string")
                .optionalAttribute(VALUE_TOSTRING_FORMAT, "string")
                .optionalAttribute(VALUE_EXTENSIONS_ENABLED, "boolean");

            registry.findType("object", "pojo")
                .optionalAttribute(VALUE_OBJECT_TYPE, "string")
                .optionalAttribute(VALUE_EQUALS_BY, "string");
        }
    }

    @Override
    public String getDescription() {
        return "Core Objects MetaData Provider - DataObject and ValueObject attribute extensions";
    }
}