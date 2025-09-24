package com.draagon.meta.registry;

import com.draagon.meta.attr.BooleanAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.key.MetaKey;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.mapped.MappedMetaObject;
import com.draagon.meta.object.pojo.PojoMetaObject;
import com.draagon.meta.object.proxy.ProxyMetaObject;
import com.draagon.meta.validator.MetaValidator;
import com.draagon.meta.view.MetaView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Provider for all object types in the MetaObjects framework.
 *
 * <p>This provider registers the complete object type hierarchy:</p>
 * <ul>
 *   <li><strong>object.base:</strong> Base object type with common object attributes and children</li>
 *   <li><strong>object.map:</strong> Map-based objects with key-value field access</li>
 *   <li><strong>object.pojo:</strong> POJO objects with reflection-based field access</li>
 *   <li><strong>object.proxy:</strong> Proxy objects with dynamic proxy field access</li>
 * </ul>
 *
 * <p>All concrete object types inherit from object.base, which inherits from metadata.base,
 * providing a clean inheritance hierarchy with shared attributes and child acceptance.</p>
 *
 * <h3>Object Type Hierarchy:</h3>
 * <pre>
 * metadata.base (MetaDataLoader)
 *     └── object.base (MetaObject) - common object attributes, accepts fields/keys/validators/views
 *         ├── object.map (MappedMetaObject) - Map-based field access
 *         ├── object.pojo (PojoMetaObject) - POJO reflection-based access + className/packageName
 *         └── object.proxy (ProxyMetaObject) - Dynamic proxy access + interfaceName
 * </pre>
 *
 * @since 6.3.0
 */
public class ObjectTypeProvider implements MetaDataTypeProvider {

    private static final Logger log = LoggerFactory.getLogger(ObjectTypeProvider.class);

    @Override
    public void registerTypes(MetaDataRegistry registry) throws Exception {
        log.info("Registering object types...");

        // Register object.base - the base type for all objects
        registerObjectBase(registry);

        // Register concrete object types
        registerConcreteObjectTypes(registry);

        log.info("Successfully registered {} object types", getObjectTypeCount());
    }

    /**
     * Register object.base - the foundation for all object types
     */
    private void registerObjectBase(MetaDataRegistry registry) {
        registry.registerType(MetaObject.class, def -> def
            .type(MetaObject.TYPE_OBJECT).subType(MetaObject.SUBTYPE_BASE)
            .description("Base object metadata with common object attributes")
            .inheritsFrom(MetaDataLoader.TYPE_METADATA, MetaDataLoader.SUBTYPE_BASE)

            // BIDIRECTIONAL CONSTRAINT: Objects accept metadata.base AND loader types as parents
            .acceptsParents(MetaDataLoader.TYPE_METADATA, MetaDataLoader.SUBTYPE_BASE)
            .acceptsParents(MetaDataLoader.TYPE_LOADER, "*")  // Accept any loader type (loader.simple, etc.)

            // OBJECT-SPECIFIC ATTRIBUTES (core object concepts)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, MetaObject.ATTR_EXTENDS)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, MetaObject.ATTR_IMPLEMENTS)
            .acceptsNamedAttributes(BooleanAttribute.SUBTYPE_BOOLEAN, MetaObject.ATTR_IS_INTERFACE)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, MetaObject.ATTR_DESCRIPTION)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, MetaObject.ATTR_OBJECT_REF)

            // OBJECTS CONTAIN STRUCTURE - accepts all main metadata types
            .acceptsChildren(MetaField.TYPE_FIELD, "*")           // Any field type
            .acceptsChildren(MetaObject.TYPE_OBJECT, "*")         // Composition - objects contain objects
            .acceptsChildren(MetaKey.TYPE_KEY, "*")               // Any key type
            .acceptsChildren(MetaValidator.TYPE_VALIDATOR, "*")   // Any validator type
            .acceptsChildren(MetaView.TYPE_VIEW, "*")             // Any view type
        );
    }

    /**
     * Register concrete object types that inherit from object.base
     */
    private void registerConcreteObjectTypes(MetaDataRegistry registry) {
        // Map-based objects (object.map)
        registry.registerType(MappedMetaObject.class, def -> def
            .type(MetaObject.TYPE_OBJECT).subType("map")
            .description("Map-based MetaObject with key-value field access")
            .inheritsFrom(MetaObject.TYPE_OBJECT, MetaObject.SUBTYPE_BASE)

            // EXPLICIT PARENT ACCEPTANCE (in case inheritance doesn't propagate acceptsParents)
            .acceptsParents(MetaDataLoader.TYPE_LOADER, "*")  // Accept any loader type
            // No map-specific attributes - inherits all from object.base
        );

        // POJO objects (object.pojo)
        registry.registerType(PojoMetaObject.class, def -> def
            .type(MetaObject.TYPE_OBJECT).subType("pojo")
            .description("POJO MetaObject with reflection-based field access")
            .inheritsFrom(MetaObject.TYPE_OBJECT, MetaObject.SUBTYPE_BASE)

            // EXPLICIT PARENT ACCEPTANCE (in case inheritance doesn't propagate acceptsParents)
            .acceptsParents(MetaDataLoader.TYPE_LOADER, "*")  // Accept any loader type

            // POJO-SPECIFIC ATTRIBUTES (plus inherited from object.base)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, PojoMetaObject.ATTR_CLASS_NAME)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, PojoMetaObject.ATTR_PACKAGE_NAME)
        );

        // Proxy objects (object.proxy)
        registry.registerType(ProxyMetaObject.class, def -> def
            .type(MetaObject.TYPE_OBJECT).subType("proxy")
            .description("Proxy MetaObject with dynamic proxy field access")
            .inheritsFrom(MetaObject.TYPE_OBJECT, MetaObject.SUBTYPE_BASE)

            // EXPLICIT PARENT ACCEPTANCE (in case inheritance doesn't propagate acceptsParents)
            .acceptsParents(MetaDataLoader.TYPE_LOADER, "*")  // Accept any loader type

            // PROXY-SPECIFIC ATTRIBUTES (plus inherited from object.base)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, MetaObject.ATTR_OBJECT)  // Required for proxy interface
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ProxyMetaObject.ATTR_PROXYOBJECT)
            .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ProxyMetaObject.ATTR_INTERFACE_NAME)
        );
    }

    @Override
    public String getProviderName() {
        return "object-types";
    }

    @Override
    public Set<String> getDependencies() {
        // Object types depend on core types and field types being loaded first
        return Set.of("core-types", "field-types");
    }

    @Override
    public int getPriority() {
        // High priority - fundamental types
        return 600;
    }

    @Override
    public boolean supportsOSGi() {
        return true;
    }

    @Override
    public String getDescription() {
        return "All MetaObject types (object.base + 3 concrete object types)";
    }

    /**
     * Get the total number of object types registered by this provider
     */
    private int getObjectTypeCount() {
        return 4; // object.base + object.map + object.pojo + object.proxy
    }
}