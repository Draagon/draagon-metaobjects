package com.draagon.meta.registry;

import com.draagon.meta.loader.MetaDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;

/**
 * Provider for core MetaData base types that all other types depend on.
 *
 * <p>This provider registers the fundamental types that form the foundation
 * of the MetaObjects type system:</p>
 * <ul>
 *   <li><strong>metadata.base:</strong> Root type that all metadata inherits from</li>
 *   <li><strong>loader.manual:</strong> Manual metadata loader type</li>
 * </ul>
 *
 * <p>These types MUST be registered before any other types attempt inheritance,
 * as most other types inherit from metadata.base either directly or indirectly.</p>
 *
 * <h3>Type Registration:</h3>
 * <pre>{@code
 * // metadata.base - universal parent
 * MetaDataRegistry.registerType(MetaDataLoader.class, def -> def
 *     .type("metadata").subType("base")
 *     .description("Base metadata type - root of all metadata inheritance")
 *     .acceptsChildren("attr", "*")      // All metadata can have attributes
 *     .acceptsChildren("object", "*")    // Can contain any object type
 *     .acceptsChildren("field", "*")     // Can contain any field type
 *     // ... etc
 * );
 * }</pre>
 *
 * @since 6.3.0
 */
public class CoreTypeProvider implements MetaDataTypeProvider {

    private static final Logger log = LoggerFactory.getLogger(CoreTypeProvider.class);

    @Override
    public void registerTypes(MetaDataRegistry registry) throws Exception {
        log.info("Registering core base types...");

        // Register metadata.base - the root type that all metadata inherits from
        registry.registerType(MetaDataLoader.class, def -> def
            .type(MetaDataLoader.TYPE_METADATA).subType(MetaDataLoader.SUBTYPE_BASE)
            .description("Base metadata type - root of all metadata inheritance")

            // BASE TYPE ACCEPTS STRUCTURAL CHILDREN - specific metadata types declare their own attributes
            // NOTE: Removed "attr", "*" to maintain service separation - specific types declare their attributes
            .acceptsChildren("object", "*")     // Can contain any object type
            .acceptsChildren("field", "*")      // Can contain any field type
            .acceptsChildren("view", "*")       // Can contain any view type
            .acceptsChildren("validator", "*")  // Can contain any validator type
            .acceptsChildren("key", "*")        // Can contain any key type
            .acceptsChildren("loader", "*")     // Can contain nested loaders

            // UNIVERSAL CORE ATTRIBUTES ONLY (truly universal concepts)
            .acceptsNamedAttributes("string", "description")  // All metadata can have description
            .acceptsNamedAttributes("boolean", "isAbstract")   // All metadata can be abstract
        );

        // Register loader.manual for backward compatibility
        registry.registerType(MetaDataLoader.class, def -> def
            .type(MetaDataLoader.TYPE_LOADER).subType(MetaDataLoader.SUBTYPE_MANUAL)
            .description("Manual metadata loader")
            .inheritsFrom(MetaDataLoader.TYPE_METADATA, MetaDataLoader.SUBTYPE_BASE)  // Inherits all base capabilities
        );

        log.info("Successfully registered core base types: metadata.base, loader.manual");
    }

    @Override
    public String getProviderName() {
        return "core-types";
    }

    @Override
    public Set<String> getDependencies() {
        // Core types have no dependencies - they are the foundation
        return Collections.emptySet();
    }

    @Override
    public int getPriority() {
        // Highest priority - must be loaded first
        return 1000;
    }

    @Override
    public boolean supportsOSGi() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Core MetaData base types (metadata.base, loader.manual)";
    }
}