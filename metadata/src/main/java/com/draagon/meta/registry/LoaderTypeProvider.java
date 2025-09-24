package com.draagon.meta.registry;

import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.simple.SimpleLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Provider for all loader types in the MetaObjects framework.
 *
 * <p>This provider registers the complete loader type hierarchy:</p>
 * <ul>
 *   <li><strong>loader.manual:</strong> Manual metadata loader (already in CoreTypeProvider)</li>
 *   <li><strong>loader.simple:</strong> Simple JSON-based metadata loader</li>
 *   <li><strong>Other loaders:</strong> File-based, XML-based loaders from other modules</li>
 * </ul>
 *
 * <p>All concrete loader types inherit from metadata.base, providing access to
 * universal metadata capabilities while adding loader-specific functionality.</p>
 *
 * <h3>Loader Type Hierarchy:</h3>
 * <pre>
 * metadata.base (MetaDataLoader)
 *     ├── loader.manual (MetaDataLoader) - basic manual loader
 *     └── loader.simple (SimpleLoader) - JSON-based loader with file discovery
 * </pre>
 *
 * @since 6.3.0
 */
public class LoaderTypeProvider implements MetaDataTypeProvider {

    private static final Logger log = LoggerFactory.getLogger(LoaderTypeProvider.class);

    @Override
    public void registerTypes(MetaDataRegistry registry) throws Exception {
        log.info("Registering loader types...");

        // Register loader.simple - JSON-based metadata loader
        registerSimpleLoader(registry);

        log.info("Successfully registered {} loader types", getLoaderTypeCount());
    }

    /**
     * Register loader.simple - JSON-based metadata loader that accepts all metadata types
     */
    private void registerSimpleLoader(MetaDataRegistry registry) {
        registry.registerType(SimpleLoader.class, def -> def
            .type(MetaDataLoader.TYPE_LOADER).subType(SimpleLoader.SUBTYPE_SIMPLE)
            .description("Simple JSON-based metadata loader")

            // INHERIT FROM BASE METADATA (metadata.base)
            .inheritsFrom(MetaDataLoader.TYPE_METADATA, MetaDataLoader.SUBTYPE_BASE)

            // LOADER ACCEPTS ALL METADATA TYPES (critical for Maven plugin)
            .acceptsChildren("field", "*")      // Accept any field type
            .acceptsChildren("attr", "*")       // Accept any attribute type
            .acceptsChildren("validator", "*")  // Accept any validator type
            .acceptsChildren("view", "*")       // Accept any view type
            .acceptsChildren("key", "*")        // Accept any key type
            .acceptsChildren("loader", "*")     // Accept nested loaders

            // BIDIRECTIONAL OBJECT TYPE ACCEPTANCE (matches ObjectTypeProvider pattern)
            // Note: Using wildcard to match ObjectTypeProvider's acceptsParents(TYPE_LOADER, "*")
            .acceptsChildren("object", "*")     // Accept any object type - bidirectional match
        );
    }

    @Override
    public String getProviderName() {
        return "loader-types";
    }

    @Override
    public Set<String> getDependencies() {
        // Loader types depend on core types and object types being loaded first
        return Set.of("core-types", "object-types");
    }

    @Override
    public int getPriority() {
        // Lower priority than object-types to ensure loader types load after object types
        return 400;
    }

    @Override
    public boolean supportsOSGi() {
        return true;
    }

    @Override
    public String getDescription() {
        return "All MetaDataLoader types (loader.simple + others)";
    }

    /**
     * Get the total number of loader types registered by this provider
     */
    private int getLoaderTypeCount() {
        return 1; // loader.simple (loader.manual is in CoreTypeProvider)
    }
}