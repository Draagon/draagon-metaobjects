package com.draagon.meta.registry;

import java.util.Set;
import java.util.Collections;

/**
 * ServiceLoader-based provider interface for MetaData type registration.
 *
 * <p>This interface replaces annotation-based type discovery with explicit
 * provider registration to fix timing and classloader isolation issues.</p>
 *
 * <h3>Key Benefits:</h3>
 * <ul>
 *   <li><strong>Explicit Registration:</strong> No classpath scanning required</li>
 *   <li><strong>Timing Control:</strong> Providers register when we call them</li>
 *   <li><strong>OSGi Compatible:</strong> Works across bundle boundaries</li>
 *   <li><strong>Dependency Resolution:</strong> Topological sorting for load order</li>
 *   <li><strong>Performance:</strong> O(1) discovery vs O(n) annotation scanning</li>
 * </ul>
 *
 * <h3>Implementation Pattern:</h3>
 * <pre>{@code
 * public class FieldTypeProvider implements MetaDataTypeProvider {
 *     @Override
 *     public void registerTypes(MetaDataRegistry registry) {
 *         registry.registerType(StringField.class, StringField::buildTypeDefinition);
 *         registry.registerType(IntegerField.class, IntegerField::buildTypeDefinition);
 *         registry.registerType(LongField.class, LongField::buildTypeDefinition);
 *     }
 *
 *     @Override
 *     public String getProviderName() {
 *         return "field-types";
 *     }
 *
 *     @Override
 *     public Set<String> getDependencies() {
 *         return Set.of("core-types"); // Depends on core types being loaded first
 *     }
 * }
 * }</pre>
 *
 * <h3>Service Registration:</h3>
 * <p>Create {@code META-INF/services/com.draagon.meta.registry.MetaDataTypeProvider}:</p>
 * <pre>
 * com.draagon.meta.field.FieldTypeProvider
 * com.draagon.meta.attr.AttributeTypeProvider
 * com.draagon.meta.validator.ValidatorTypeProvider
 * </pre>
 *
 * @since 6.3.0
 */
public interface MetaDataTypeProvider {

    /**
     * Register all types provided by this provider.
     *
     * <p>This method is called during provider discovery and should register
     * all MetaData types that this provider contributes to the system.</p>
     *
     * @param registry The MetaDataRegistry to register types into
     * @throws Exception if type registration fails
     */
    void registerTypes(MetaDataRegistry registry) throws Exception;

    /**
     * Get the unique name of this provider.
     *
     * <p>Provider names are used for dependency resolution and error reporting.
     * Names should be descriptive and unique across the entire system.</p>
     *
     * @return Unique provider name (e.g., "field-types", "core-types", "database-extensions")
     */
    String getProviderName();

    /**
     * Get the set of provider dependencies.
     *
     * <p>Dependencies are resolved using topological sorting to ensure providers
     * are loaded in the correct order. Use provider names from {@link #getProviderName()}.</p>
     *
     * @return Set of provider names this provider depends on, empty set if no dependencies
     */
    default Set<String> getDependencies() {
        return Collections.emptySet();
    }

    /**
     * Get the priority for this provider.
     *
     * <p>Higher priority providers are loaded first when dependencies are equal.
     * Use this to fine-tune loading order within dependency groups.</p>
     *
     * @return Priority value (higher = loaded first), default 500
     */
    default int getPriority() {
        return 500;
    }

    /**
     * Check if this provider supports OSGi environments.
     *
     * <p>OSGi-aware providers handle bundle lifecycle events and dynamic loading.
     * Non-OSGi providers are only loaded during static initialization.</p>
     *
     * @return true if this provider supports OSGi, false otherwise
     */
    default boolean supportsOSGi() {
        return true; // Default to OSGi support for forward compatibility
    }

    /**
     * Get provider description for debugging and health reports.
     *
     * @return Human-readable description of what this provider contributes
     */
    default String getDescription() {
        return getProviderName() + " provider";
    }
}