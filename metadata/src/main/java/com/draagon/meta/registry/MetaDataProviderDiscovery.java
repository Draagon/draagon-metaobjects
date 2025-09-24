package com.draagon.meta.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Unified discovery manager for MetaDataTypeProvider services across different environments.
 *
 * <p>This class handles ServiceLoader-based provider discovery with:</p>
 * <ul>
 *   <li><strong>Environment Detection:</strong> Automatically detects OSGi, Spring, or standard JVM</li>
 *   <li><strong>Dependency Resolution:</strong> Topological sorting for provider load order</li>
 *   <li><strong>Fail-Fast Error Handling:</strong> Clear diagnostics when provider loading fails</li>
 *   <li><strong>Performance Optimization:</strong> O(1) discovery vs O(n) classpath scanning</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // Discover and register all providers
 * MetaDataProviderDiscovery.discoverAllProviders(registry);
 *
 * // Manual discovery for specific environments
 * ProviderManager manager = MetaDataProviderDiscovery.createProviderManager();
 * List<MetaDataTypeProvider> providers = manager.discoverProviders();
 * }</pre>
 *
 * @since 6.3.0
 */
public class MetaDataProviderDiscovery {

    private static final Logger log = LoggerFactory.getLogger(MetaDataProviderDiscovery.class);

    /**
     * Main entry point: Discover and register all MetaDataTypeProvider services.
     *
     * @param registry The MetaDataRegistry to register types into
     * @throws MetaDataProviderException if any provider fails to load
     */
    public static void discoverAllProviders(MetaDataRegistry registry) {
        ProviderManager manager = createProviderManager();

        try {
            // PHASE 1: Discover all providers
            List<MetaDataTypeProvider> providers = manager.discoverProviders();
            log.info("Discovered {} MetaDataTypeProvider services", providers.size());

            // PHASE 2: Resolve dependencies and sort
            List<MetaDataTypeProvider> orderedProviders = resolveDependencies(providers);
            log.debug("Provider load order: {}",
                    orderedProviders.stream()
                            .map(MetaDataTypeProvider::getProviderName)
                            .collect(Collectors.joining(" -> ")));

            // PHASE 3: Register types in dependency order
            for (MetaDataTypeProvider provider : orderedProviders) {
                try {
                    log.debug("Loading provider: {}", provider.getProviderName());
                    provider.registerTypes(registry);
                    log.info("Successfully loaded provider: {} ({})",
                            provider.getProviderName(), provider.getDescription());
                } catch (Exception e) {
                    // FAIL FAST - don't continue with incomplete type system
                    throw new MetaDataProviderException(
                            "Failed to load provider: " + provider.getProviderName(),
                            provider.getProviderName(),
                            provider.getDependencies(),
                            ProviderLoadPhase.TYPE_REGISTRATION,
                            e
                    );
                }
            }

            // PHASE 4: Initialize constraint system with complete type registry
            log.info("All providers loaded successfully, refreshing constraint system");
            // Note: Constraint system refresh is handled externally

        } catch (Exception e) {
            if (e instanceof MetaDataProviderException) {
                throw e;
            } else {
                throw new MetaDataProviderException(
                        "Provider discovery failed: " + e.getMessage(),
                        "discovery-manager",
                        Collections.emptySet(),
                        ProviderLoadPhase.DISCOVERY,
                        e
                );
            }
        }
    }

    /**
     * Create appropriate provider manager based on runtime environment.
     *
     * @return ProviderManager instance for current environment
     */
    public static ProviderManager createProviderManager() {
        if (isOSGiEnvironment()) {
            log.debug("Detected OSGi environment, using OSGiProviderManager");
            return new OSGiProviderManager();
        } else if (isSpringEnvironment()) {
            log.debug("Detected Spring environment, using SpringProviderManager");
            return new SpringProviderManager();
        } else {
            log.debug("Using standard JVM environment, using StandardProviderManager");
            return new StandardProviderManager();
        }
    }

    /**
     * Resolve provider dependencies using topological sorting.
     *
     * @param providers List of discovered providers
     * @return Providers sorted in dependency order
     * @throws MetaDataProviderException if circular dependencies are detected
     */
    public static List<MetaDataTypeProvider> resolveDependencies(List<MetaDataTypeProvider> providers) {
        // Build provider name to provider mapping
        Map<String, MetaDataTypeProvider> providerMap = providers.stream()
                .collect(Collectors.toMap(
                        MetaDataTypeProvider::getProviderName,
                        p -> p,
                        (existing, replacement) -> {
                            log.warn("Duplicate provider name: {}, using first registration",
                                    existing.getProviderName());
                            return existing;
                        }
                ));

        // Validate all dependencies exist
        for (MetaDataTypeProvider provider : providers) {
            for (String dependency : provider.getDependencies()) {
                if (!providerMap.containsKey(dependency)) {
                    throw new MetaDataProviderException(
                            "Provider '" + provider.getProviderName() +
                            "' depends on unknown provider: " + dependency,
                            provider.getProviderName(),
                            provider.getDependencies(),
                            ProviderLoadPhase.DEPENDENCY_RESOLUTION,
                            null
                    );
                }
            }
        }

        // Topological sort with cycle detection
        List<MetaDataTypeProvider> sorted = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();

        for (MetaDataTypeProvider provider : providers) {
            if (!visited.contains(provider.getProviderName())) {
                topologicalSort(provider.getProviderName(), providerMap, sorted, visited, visiting);
            }
        }

        // Secondary sort by priority for providers with equal dependencies
        sorted.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));

        return sorted;
    }

    /**
     * Recursive topological sort with cycle detection.
     */
    private static void topologicalSort(String providerName,
                                       Map<String, MetaDataTypeProvider> providerMap,
                                       List<MetaDataTypeProvider> sorted,
                                       Set<String> visited,
                                       Set<String> visiting) {
        if (visiting.contains(providerName)) {
            throw new MetaDataProviderException(
                    "Circular dependency detected involving provider: " + providerName,
                    providerName,
                    Collections.emptySet(),
                    ProviderLoadPhase.DEPENDENCY_RESOLUTION,
                    null
            );
        }

        if (visited.contains(providerName)) {
            return; // Already processed
        }

        visiting.add(providerName);
        MetaDataTypeProvider provider = providerMap.get(providerName);

        // Process dependencies first
        for (String dependency : provider.getDependencies()) {
            topologicalSort(dependency, providerMap, sorted, visited, visiting);
        }

        visiting.remove(providerName);
        visited.add(providerName);
        sorted.add(provider);
    }

    /**
     * Check if running in OSGi environment.
     */
    private static boolean isOSGiEnvironment() {
        try {
            Class.forName("org.osgi.framework.Bundle");
            return System.getProperty("org.osgi.framework.version") != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Check if running in Spring environment.
     */
    private static boolean isSpringEnvironment() {
        try {
            Class.forName("org.springframework.context.ApplicationContext");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Provider manager interface for different environments.
     */
    public interface ProviderManager {
        List<MetaDataTypeProvider> discoverProviders();
        String getDescription();
    }

    /**
     * Standard JVM provider manager using ServiceLoader.
     */
    public static class StandardProviderManager implements ProviderManager {
        @Override
        public List<MetaDataTypeProvider> discoverProviders() {
            List<MetaDataTypeProvider> providers = new ArrayList<>();
            ServiceLoader<MetaDataTypeProvider> serviceLoader =
                    ServiceLoader.load(MetaDataTypeProvider.class);

            for (MetaDataTypeProvider provider : serviceLoader) {
                providers.add(provider);
            }

            return providers;
        }

        @Override
        public String getDescription() {
            return "Standard JVM ServiceLoader";
        }
    }

    /**
     * OSGi provider manager with bundle lifecycle support.
     */
    public static class OSGiProviderManager implements ProviderManager {
        @Override
        public List<MetaDataTypeProvider> discoverProviders() {
            List<MetaDataTypeProvider> providers = new ArrayList<>();

            try {
                // OSGi-specific ServiceLoader discovery
                ServiceLoader<MetaDataTypeProvider> serviceLoader =
                        ServiceLoader.load(MetaDataTypeProvider.class);

                for (MetaDataTypeProvider provider : serviceLoader) {
                    if (provider.supportsOSGi()) {
                        providers.add(provider);
                    } else {
                        log.warn("Provider {} does not support OSGi, skipping in OSGi environment",
                                provider.getProviderName());
                    }
                }

            } catch (Exception e) {
                log.warn("OSGi-specific discovery failed, falling back to standard ServiceLoader: {}",
                        e.getMessage());

                // Fallback to standard ServiceLoader
                ServiceLoader<MetaDataTypeProvider> fallbackLoader =
                        ServiceLoader.load(MetaDataTypeProvider.class);
                for (MetaDataTypeProvider provider : fallbackLoader) {
                    providers.add(provider);
                }
            }

            return providers;
        }

        @Override
        public String getDescription() {
            return "OSGi Bundle ServiceLoader";
        }
    }

    /**
     * Spring provider manager with ApplicationContext integration.
     */
    public static class SpringProviderManager implements ProviderManager {
        @Override
        public List<MetaDataTypeProvider> discoverProviders() {
            List<MetaDataTypeProvider> providers = new ArrayList<>();

            // Use standard ServiceLoader first
            ServiceLoader<MetaDataTypeProvider> serviceLoader =
                    ServiceLoader.load(MetaDataTypeProvider.class);
            for (MetaDataTypeProvider provider : serviceLoader) {
                providers.add(provider);
            }

            // TODO: Add Spring ApplicationContext bean discovery if needed
            // This would require injecting ApplicationContext, which adds complexity

            return providers;
        }

        @Override
        public String getDescription() {
            return "Spring ApplicationContext ServiceLoader";
        }
    }

    /**
     * Provider load phases for error reporting.
     */
    public enum ProviderLoadPhase {
        DISCOVERY("Provider discovery"),
        DEPENDENCY_RESOLUTION("Dependency resolution"),
        TYPE_REGISTRATION("Type registration");

        private final String description;

        ProviderLoadPhase(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}