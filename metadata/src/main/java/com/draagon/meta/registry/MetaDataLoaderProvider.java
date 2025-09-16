package com.draagon.meta.registry;

/**
 * Service provider interface for registering MetaDataLoaders.
 * 
 * <p>Implementations of this interface are discovered automatically by the
 * {@link MetaDataLoaderRegistry} via the {@link ServiceRegistry}. This enables
 * dynamic loader registration without configuration files or static dependencies.</p>
 * 
 * <h3>Implementation Guidelines:</h3>
 * 
 * <p><strong>For Java ServiceLoader:</strong> Add your implementation to 
 * {@code META-INF/services/com.draagon.meta.registry.MetaDataLoaderProvider}</p>
 * 
 * <p><strong>For OSGI:</strong> Register as a service in your bundle activator or 
 * use declarative services with {@code @Component}</p>
 * 
 * <h3>Example Implementation:</h3>
 * <pre>{@code
 * public class CoreLoaderProvider implements MetaDataLoaderProvider {
 *     
 *     @Override
 *     public void registerLoaders(MetaDataLoaderRegistry registry) {
 *         // Register file-based loader
 *         FileMetaDataLoader fileLoader = new FileMetaDataLoader();
 *         fileLoader.setName("file-loader");
 *         registry.registerLoader(fileLoader);
 *         
 *         // Register XML loader
 *         XMLMetaDataLoader xmlLoader = new XMLMetaDataLoader();
 *         xmlLoader.setName("xml-loader");
 *         registry.registerLoader(xmlLoader);
 *         
 *         // Register JSON loader
 *         JsonMetaDataLoader jsonLoader = new JsonMetaDataLoader();
 *         jsonLoader.setName("json-loader");
 *         registry.registerLoader(jsonLoader);
 *     }
 * }
 * }</pre>
 * 
 * <h3>Plugin Extension Example:</h3>
 * <pre>{@code
 * public class DatabaseLoaderProvider implements MetaDataLoaderProvider {
 *     
 *     @Override
 *     public void registerLoaders(MetaDataLoaderRegistry registry) {
 *         // Register database-based loader for enterprise package
 *         DatabaseMetaDataLoader dbLoader = new DatabaseMetaDataLoader();
 *         dbLoader.setConnectionString("jdbc:postgresql://localhost/metadata");
 *         dbLoader.setName("database-loader");
 *         registry.registerLoader(dbLoader);
 *         
 *         // Register caching loader wrapper
 *         CachingMetaDataLoader cacheLoader = new CachingMetaDataLoader(dbLoader);
 *         cacheLoader.setName("cached-database-loader");
 *         registry.registerLoader(cacheLoader);
 *     }
 * }
 * }</pre>
 * 
 * @since 6.0.0
 */
public interface MetaDataLoaderProvider {
    
    /**
     * Register MetaDataLoaders with the registry.
     * 
     * <p>This method is called during registry initialization to register
     * all loaders that this provider supports. Each loader should have a
     * unique name and be properly configured.</p>
     * 
     * <p><strong>Important:</strong> This method may be called multiple times,
     * so implementations should be idempotent (safe to call repeatedly).
     * Use try-catch blocks around registration to handle duplicate registrations.</p>
     * 
     * @param registry The registry to register loaders with
     */
    void registerLoaders(MetaDataLoaderRegistry registry);
    
    /**
     * Get the priority of this provider (optional).
     * 
     * <p>Lower numbers = higher priority. Providers with higher priority
     * are processed first during registration. Default priority is 100.</p>
     * 
     * <p>Use this to ensure certain loaders are registered before others,
     * or to override default loaders with custom implementations.</p>
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