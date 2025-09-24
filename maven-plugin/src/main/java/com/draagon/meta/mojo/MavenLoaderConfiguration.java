/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.mojo;

import com.draagon.meta.loader.LoaderConfigurable;
import com.draagon.meta.loader.LoaderConfigurationBuilder;

import java.util.List;
import java.util.Map;

/**
 * Maven-specific implementation of LoaderConfiguration.
 * This bridges Maven Mojo configuration to the generic LoaderConfigurable interface.
 */
public class MavenLoaderConfiguration {
    
    /**
     * Configure a LoaderConfigurable instance using Maven-specific configuration
     *
     * @param configurable The loader to configure
     * @param sourceDir The Maven source directory
     * @param classLoader The Maven project class loader
     * @param sources The list of source files
     * @param globals The global arguments map
     */
    public static void configure(LoaderConfigurable configurable,
                               String sourceDir,
                               ClassLoader classLoader,
                               List<String> sources,
                               Map<String, String> globals) {


        // CRITICAL: Ensure MetaDataRegistry is fully initialized before any constraint operations
        if (configurable instanceof com.draagon.meta.loader.MetaDataLoader) {
            com.draagon.meta.loader.MetaDataLoader loader = (com.draagon.meta.loader.MetaDataLoader) configurable;

            // Force complete initialization of MetaDataRegistry singleton
            // This ensures loadCoreTypes() completes before any constraint enforcement
            com.draagon.meta.registry.MetaDataRegistry registry = com.draagon.meta.registry.MetaDataRegistry.getInstance();

            // CRITICAL: Explicitly trigger provider discovery to ensure all types are registered
            // This fixes timing issues where static blocks run before provider discovery
            try {
                System.out.println("Maven plugin: Explicitly triggering provider discovery...");
                com.draagon.meta.registry.MetaDataProviderDiscovery.discoverAllProviders(registry);
                System.out.println("Maven plugin: Provider discovery completed with " + registry.getRegisteredTypes().size() + " types");

                // CRITICAL: Refresh constraint system to pick up all newly registered types
                System.out.println("Maven plugin: Refreshing constraint system with " + registry.getRegisteredTypes().size() + " registered types");
                com.draagon.meta.constraint.ConstraintEnforcer.getInstance().refreshConstraintFlattener();
                System.out.println("Maven plugin: Constraint system refresh completed");
            } catch (Exception e) {
                System.err.println("Maven plugin: Provider discovery failed: " + e.getMessage());
                e.printStackTrace();
            }

            // Wait for all deferred inheritance resolution to complete
            int resolvedCount = registry.resolveDeferredInheritance();
            if (resolvedCount > 0) {
                System.out.println("Maven plugin: Resolved " + resolvedCount + " deferred inheritance relationships");
            }

            // Now it's safe to access the registry - all types should be loaded
            int registeredTypeCount = loader.getTypeRegistry().getRegisteredTypes().size();
            System.out.println("Maven plugin: Found " + registeredTypeCount + " registered types after full initialization");

            // CRITICAL FIX: Force refresh of constraint flattener in Maven environment
            // Maven's classloader isolation can cause stale constraint flatteners
            try {
                com.draagon.meta.constraint.ConstraintEnforcer constraintEnforcer =
                    com.draagon.meta.constraint.ConstraintEnforcer.getInstance();
                constraintEnforcer.refreshConstraintFlattener();
                System.out.println("Maven plugin: Successfully refreshed constraint flattener for Maven environment");
            } catch (Exception e) {
                System.err.println("Maven plugin: Warning - failed to refresh constraint flattener: " + e.getMessage());
            }
        }
        
        LoaderConfigurable.LoaderConfiguration config = new LoaderConfigurationBuilder()
                .sourceDir(sourceDir)
                .classLoader(classLoader)
                .sources(sources)
                .arguments(globals)
                .build();
                
        configurable.configure(config);
    }
}