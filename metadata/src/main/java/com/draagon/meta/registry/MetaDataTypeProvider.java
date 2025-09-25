package com.draagon.meta.registry;

/**
 * Service provider interface for registering MetaData types and their extensions.
 *
 * <p>Implementations of this interface are discovered automatically via ServiceLoader
 * from META-INF/services files. This enables dynamic type registration and extension
 * without configuration files or static dependencies.</p>
 *
 * <h3>Dependency-Based Loading:</h3>
 * <p>Providers specify explicit dependencies instead of fragile priority numbers.
 * The system automatically resolves the dependency graph using topological sorting
 * to ensure proper load order.</p>
 *
 * <h3>Example Implementation:</h3>
 * <pre>{@code
 * public class DatabaseMetaDataProvider implements MetaDataTypeProvider {
 *
 *     @Override
 *     public String getProviderId() {
 *         return "database-extensions";
 *     }
 *
 *     @Override
 *     public String[] getDependencies() {
 *         return new String[]{"core-types", "field-types"};  // Explicit dependencies
 *     }
 *
 *     @Override
 *     public void registerTypes(MetaDataRegistry registry) {
 *         // Delegate to service classes
 *         DatabaseService.registerTypeExtensions(registry);
 *         PostgreSQLService.registerTypeExtensions(registry);
 *     }
 * }
 * }</pre>
 *
 * <h3>Dependency Benefits:</h3>
 * <ul>
 * <li><strong>Explicit Dependencies:</strong> Clear what each provider needs</li>
 * <li><strong>Automatic Resolution:</strong> System calculates correct load order</li>
 * <li><strong>Circular Detection:</strong> Prevents dependency cycles</li>
 * <li><strong>Missing Detection:</strong> Warns about unresolved dependencies</li>
 * <li><strong>Maintainable:</strong> Easy to add new providers without priority conflicts</li>
 * </ul>
 *
 * <h3>Common Provider IDs:</h3>
 * <ul>
 * <li><strong>core-types:</strong> Basic metadata.base type</li>
 * <li><strong>field-types:</strong> All concrete field types</li>
 * <li><strong>object-types:</strong> All concrete object types</li>
 * <li><strong>attribute-types:</strong> All attribute types</li>
 * <li><strong>validator-types:</strong> All validator types</li>
 * <li><strong>key-types:</strong> All key types</li>
 * <li><strong>web-types:</strong> All web view types</li>
 * </ul>
 *
 * @since 6.0.0
 */
public interface MetaDataTypeProvider {

    /**
     * Register MetaData type extensions with the registry.
     *
     * <p>This method is called during registry initialization to extend
     * existing types with new attributes and constraints. Providers should
     * delegate to service classes that contain constants and extension logic.</p>
     *
     * <p><strong>Important:</strong> This method may be called multiple times,
     * so implementations should be idempotent (safe to call repeatedly).
     * Use try-catch blocks around extensions to handle conflicts.</p>
     *
     * @param registry The registry to extend types in
     */
    void registerTypes(MetaDataRegistry registry);

    /**
     * Get the unique identifier for this provider.
     *
     * <p>Used for dependency resolution. Should be a short, descriptive name
     * that other providers can reference in their getDependencies() method.</p>
     *
     * <h3>Naming Convention:</h3>
     * <ul>
     * <li>Use kebab-case: "field-types", "database-extensions"</li>
     * <li>Be descriptive: "web-view-types" not "web"</li>
     * <li>Include scope: "om-managed-types" not "managed"</li>
     * </ul>
     *
     * @return Unique provider identifier
     */
    default String getProviderId() {
        return getClass().getSimpleName().toLowerCase()
               .replaceAll("metadataprovider$", "")
               .replaceAll("provider$", "")
               .replaceAll("([a-z])([A-Z])", "$1-$2")
               .toLowerCase();
    }

    /**
     * Get the providers that this provider depends on.
     *
     * <p>The dependency resolution system will ensure these providers
     * are loaded before this provider. Returns empty array if no dependencies.</p>
     *
     * <h3>Dependency Examples:</h3>
     * <ul>
     * <li><strong>Field types</strong> depend on: ["core-types"] (for field.base)</li>
     * <li><strong>Object types</strong> depend on: ["core-types"] (for object.base)</li>
     * <li><strong>OM types</strong> depend on: ["object-types"] (for object.base)</li>
     * <li><strong>Web types</strong> depend on: ["view-types"] (for view.base)</li>
     * </ul>
     *
     * @return Array of provider IDs this provider depends on
     */
    default String[] getDependencies() {
        return new String[0];  // No dependencies by default
    }


    /**
     * Get a description of this provider (optional).
     *
     * <p>Used for logging and debugging purposes.</p>
     *
     * @return Human-readable description
     */
    default String getDescription() {
        return getClass().getSimpleName();
    }
}