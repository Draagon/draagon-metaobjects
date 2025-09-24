package com.draagon.meta.registry;

/**
 * Service provider interface for registering MetaData types and their extensions.
 *
 * <p>Implementations of this interface are discovered automatically via ServiceLoader
 * from META-INF/services files. This enables dynamic type registration and extension
 * without configuration files or static dependencies.</p>
 *
 * <h3>Architecture Pattern:</h3>
 * <p>Provider classes should delegate to service classes that contain the actual
 * extension logic and constants. This follows the pattern:</p>
 * <pre>
 * Provider → Service Classes → registry.findType().optionalAttribute()
 * </pre>
 *
 * <h3>Implementation Guidelines:</h3>
 *
 * <p><strong>For Java ServiceLoader:</strong> Add your implementation to
 * {@code META-INF/services/com.draagon.meta.registry.MetaDataTypeProvider}</p>
 *
 * <p><strong>For OSGI:</strong> Register as a service in your bundle activator or
 * use declarative services with {@code @Component}</p>
 *
 * <h3>Example Implementation:</h3>
 * <pre>{@code
 * public class DatabaseMetaDataProvider implements MetaDataTypeProvider {
 *
 *     @Override
 *     public void registerTypes(MetaDataRegistry registry) {
 *         // Delegate to service classes
 *         DatabaseService.registerTypeExtensions(registry);
 *         PostgreSQLService.registerTypeExtensions(registry);
 *     }
 *
 *     @Override
 *     public int getPriority() {
 *         return 200; // After core types (100), before plugins (300+)
 *     }
 * }
 * }</pre>
 *
 * <h3>Service Class Pattern:</h3>
 * <pre>{@code
 * public class DatabaseService {
 *     // Constants for attribute names
 *     public static final String DB_TABLE = "dbTable";
 *     public static final String DB_COLUMN = "dbColumn";
 *
 *     public static void registerTypeExtensions(MetaDataRegistry registry) {
 *         // Extend existing field types
 *         registry.findType("field", "string")
 *             .optionalAttribute(DB_COLUMN, "string");
 *
 *         registry.findType("object", "pojo")
 *             .optionalAttribute(DB_TABLE, "string");
 *     }
 * }
 * }</pre>
 *
 * <h3>Extension Priority Levels:</h3>
 * <ul>
 * <li><strong>Core Types (0-99):</strong> Basic field, object, attribute, validator, view types</li>
 * <li><strong>Database Services (100-199):</strong> Database attributes and constraints</li>
 * <li><strong>Codegen Services (200-299):</strong> Code generation attributes</li>
 * <li><strong>Web Services (300-399):</strong> Web framework attributes</li>
 * <li><strong>Business Plugins (400+):</strong> Custom business logic extensions</li>
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
     * Get the priority of this provider (optional).
     *
     * <p>Lower numbers = higher priority. Providers with higher priority
     * are processed first during registration. This ensures proper dependency
     * order for type extensions.</p>
     *
     * <h3>Recommended Priority Ranges:</h3>
     * <ul>
     * <li><strong>0-99:</strong> Core types and base implementations</li>
     * <li><strong>100-199:</strong> Database and persistence services</li>
     * <li><strong>200-299:</strong> Code generation and schema services</li>
     * <li><strong>300-399:</strong> Web and UI framework services</li>
     * <li><strong>400+:</strong> Business plugins and custom extensions</li>
     * </ul>
     *
     * @return Priority value (lower = higher priority)
     */
    default int getPriority() {
        return 100;
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