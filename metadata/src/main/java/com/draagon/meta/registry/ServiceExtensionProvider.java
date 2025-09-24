package com.draagon.meta.registry;

import java.util.Set;
import java.util.Collections;

/**
 * ServiceLoader-based provider interface for extending existing MetaData types with service-specific capabilities.
 *
 * <p>This interface enables the <strong>Service Extension Pattern</strong> where external services can add
 * attributes, constraints, and capabilities to core MetaData types without polluting the core types themselves.
 * This maintains clean architectural separation while enabling powerful extensibility.</p>
 *
 * <h3>Architectural Benefits:</h3>
 * <ul>
 *   <li><strong>Service Separation:</strong> Database logic stays in database modules</li>
 *   <li><strong>Core Purity:</strong> Core types remain focused on universal concepts</li>
 *   <li><strong>Plugin Extensibility:</strong> Third parties can extend without core changes</li>
 *   <li><strong>Enterprise Adoption:</strong> Companies customize without forking core code</li>
 * </ul>
 *
 * <h3>Usage Example - Database Extensions:</h3>
 * <pre>{@code
 * public class DatabaseExtensionProvider implements ServiceExtensionProvider {
 *
 *     @Override
 *     public void extendTypes(MetaDataRegistry registry) {
 *         // Extend ALL field types with database attributes
 *         registry.extendType(MetaField.class, def -> def
 *             .acceptsNamedAttributes("string", "dbColumn")
 *             .acceptsNamedAttributes("string", "dbType")
 *             .acceptsNamedAttributes("boolean", "dbNullable")
 *         );
 *
 *         // Extend ALL object types with database attributes
 *         registry.extendType(MetaObject.class, def -> def
 *             .acceptsNamedAttributes("string", "dbTable")
 *             .acceptsNamedAttributes("string", "dbSchema")
 *         );
 *
 *         // Add database-specific constraints
 *         addDatabaseValidationConstraints();
 *     }
 *
 *     @Override
 *     public String getProviderName() {
 *         return "database-extensions";
 *     }
 *
 *     @Override
 *     public Set<String> getDependencies() {
 *         return Set.of("core-types", "field-types", "object-types");
 *     }
 * }
 * }</pre>
 *
 * <h3>Usage Example - JPA Extensions:</h3>
 * <pre>{@code
 * public class JpaExtensionProvider implements ServiceExtensionProvider {
 *
 *     @Override
 *     public void extendTypes(MetaDataRegistry registry) {
 *         // Add JPA-specific attributes to field types
 *         registry.extendType(MetaField.class, def -> def
 *             .acceptsNamedAttributes("boolean", "jpaId")
 *             .acceptsNamedAttributes("string", "jpaColumn")
 *             .acceptsNamedAttributes("boolean", "jpaGeneratedValue")
 *         );
 *
 *         // Add JPA-specific constraints
 *         addJpaValidationConstraints();
 *     }
 *
 *     @Override
 *     public String getProviderName() {
 *         return "jpa-extensions";
 *     }
 * }
 * }</pre>
 *
 * <h3>Service Registration:</h3>
 * <p>Create {@code META-INF/services/com.draagon.meta.registry.ServiceExtensionProvider}:</p>
 * <pre>
 * com.draagon.meta.database.DatabaseExtensionProvider
 * com.draagon.meta.jpa.JpaExtensionProvider
 * com.mycompany.CustomExtensionProvider
 * </pre>
 *
 * <h3>Load Order:</h3>
 * <ol>
 *   <li><strong>Core Types:</strong> MetaDataTypeProviders register base types</li>
 *   <li><strong>Service Extensions:</strong> ServiceExtensionProviders extend types with service capabilities</li>
 *   <li><strong>Constraint Resolution:</strong> All constraints are resolved and flattened</li>
 * </ol>
 *
 * @since 6.2.0 (Phase 3)
 */
public interface ServiceExtensionProvider {

    /**
     * Extend existing MetaData types with service-specific capabilities.
     *
     * <p>This method is called after core types are registered but before constraint
     * flattening occurs. Extensions should use {@link MetaDataRegistry#extendType(Class, java.util.function.Consumer)}
     * to add attributes and constraints to existing types.</p>
     *
     * <p><strong>Important:</strong> Extensions should be <em>additive only</em> - they should not
     * modify or remove existing type capabilities, only add new ones.</p>
     *
     * @param registry The MetaDataRegistry to extend types in
     * @throws Exception if type extension fails
     */
    void extendTypes(MetaDataRegistry registry) throws Exception;

    /**
     * Get the unique name of this service extension provider.
     *
     * <p>Provider names are used for dependency resolution and error reporting.
     * Names should clearly indicate the service domain (e.g., "database-extensions", "jpa-extensions").</p>
     *
     * @return Unique provider name
     */
    String getProviderName();

    /**
     * Get the set of provider dependencies that must be loaded before this extension.
     *
     * <p>Extensions typically depend on core type providers being loaded first.
     * Dependencies are resolved using topological sorting.</p>
     *
     * <p><strong>Common Dependencies:</strong></p>
     * <ul>
     *   <li>{@code "core-types"} - Core MetaData types</li>
     *   <li>{@code "field-types"} - All field implementations</li>
     *   <li>{@code "object-types"} - All object implementations</li>
     *   <li>{@code "attribute-types"} - All attribute implementations</li>
     * </ul>
     *
     * @return Set of provider names this extension depends on
     */
    default Set<String> getDependencies() {
        return Set.of("core-types", "field-types", "object-types", "attribute-types");
    }

    /**
     * Get the priority of this extension provider.
     *
     * <p>Higher priority extensions are applied first. This allows core service
     * extensions to establish base patterns before plugin extensions.</p>
     *
     * <p><strong>Priority Levels:</strong></p>
     * <ul>
     *   <li>{@code 1000+} - Core service extensions (database, JPA)</li>
     *   <li>{@code 500+} - Framework extensions (Spring, validation)</li>
     *   <li>{@code 100+} - Plugin extensions</li>
     *   <li>{@code 0} - Default priority</li>
     * </ul>
     *
     * @return Priority level (higher = earlier execution)
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Check if this extension supports the current runtime environment.
     *
     * <p>This allows extensions to be conditionally enabled based on classpath
     * availability, system properties, or other environmental factors.</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * @Override
     * public boolean supportsCurrentEnvironment() {
     *     try {
     *         // Only enable if JPA is on the classpath
     *         Class.forName("javax.persistence.Entity");
     *         return true;
     *     } catch (ClassNotFoundException e) {
     *         return false;
     *     }
     * }
     * }</pre>
     *
     * @return true if this extension should be loaded in the current environment
     */
    default boolean supportsCurrentEnvironment() {
        return true;
    }

    /**
     * Get a human-readable description of this extension provider.
     *
     * <p>Used for logging and debugging purposes to understand what
     * capabilities this extension adds to the system.</p>
     *
     * @return Description of this extension provider
     */
    default String getDescription() {
        return getClass().getSimpleName();
    }
}