package com.metaobjects.spring;

import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.registry.MetaDataLoaderRegistry;
import com.metaobjects.registry.ServiceRegistryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * Spring Auto-Configuration for MetaObjects OSGi-compatible registry.
 * 
 * <p>This configuration automatically:</p>
 * <ul>
 *   <li>Creates OSGi-compatible MetaDataLoaderRegistry using ServiceRegistryFactory</li>
 *   <li>Auto-discovers MetaDataLoader beans from Spring context</li>
 *   <li>Provides backward-compatible @Autowired injection</li>
 *   <li>Works in both OSGi and non-OSGi environments</li>
 * </ul>
 * 
 * <p><strong>Usage in Spring Controllers:</strong></p>
 * <pre>{@code
 * @RestController
 * public class MyController {
 *     
 *     // OPTION 1: Same as before - auto-wires primary loader
 *     @Autowired
 *     private MetaDataLoader metaDataLoader;
 *     
 *     // OPTION 2: Full registry access
 *     @Autowired 
 *     private MetaDataLoaderRegistry metaDataLoaderRegistry;
 *     
 *     // OPTION 3: Convenient service wrapper
 *     @Autowired
 *     private MetaDataService metaDataService;
 * }
 * }</pre>
 * 
 * @since 6.0.0
 */
@Configuration
public class MetaDataAutoConfiguration {
    
    /**
     * Creates OSGi-compatible MetaDataLoaderRegistry with auto-discovery
     * of MetaDataLoader beans from Spring context.
     */
    @Bean
    @Primary
    public MetaDataLoaderRegistry metaDataLoaderRegistry() {
        
        // Create OSGi-compatible registry (auto-detects environment)
        MetaDataLoaderRegistry registry = new MetaDataLoaderRegistry(
            ServiceRegistryFactory.getDefault()
        );
        
        return registry;
    }
    
    /**
     * Provides the primary MetaDataLoader for backward compatibility.
     * 
     * <p>This allows existing Spring code using {@code @Autowired MetaDataLoader} 
     * to continue working without changes.</p>
     */
    @Bean
    @Primary
    public MetaDataLoader primaryMetaDataLoader(
            @Autowired(required = false) List<MetaDataLoader> allLoaders,
            MetaDataLoaderRegistry registry) {
        
        // Auto-register any MetaDataLoader beans found in Spring context
        if (allLoaders != null) {
            for (MetaDataLoader loader : allLoaders) {
                // Avoid circular registration 
                if (!registry.getDataLoaders().contains(loader)) {
                    registry.registerLoader(loader);
                }
            }
        }
        
        // Return the first available loader, or create a default if none found
        return registry.getDataLoaders().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "No MetaDataLoader found. Please ensure at least one MetaDataLoader bean is configured."
            ));
    }
    
    /**
     * Convenient service wrapper for common MetaData operations.
     * 
     * <p>Provides a clean API layer over the registry for most common use cases.</p>
     */
    @Bean
    public MetaDataService metaDataService(MetaDataLoaderRegistry registry) {
        return new MetaDataService(registry);
    }
}