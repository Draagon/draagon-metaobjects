package com.draagon.meta.type;

import com.draagon.meta.MetaData;
import com.draagon.meta.ValidationResult;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for migrating between legacy string-based type system
 * and the new type registry system. Provides backward compatibility
 * during the transition period.
 */
public class MetaDataMigrationUtils {
    
    private static final Logger log = LoggerFactory.getLogger(MetaDataMigrationUtils.class);
    
    // Mapping of legacy type strings to modern implementations
    private static final Map<String, String> LEGACY_TYPE_MAPPINGS = Map.of(
        "attr", "com.draagon.meta.attr.MetaAttribute",
        "field", "com.draagon.meta.field.MetaField", 
        "object", "com.draagon.meta.object.MetaObject",
        "loader", "com.draagon.meta.loader.MetaDataLoader",
        "view", "com.draagon.meta.view.MetaView",
        "validator", "com.draagon.meta.validator.MetaValidator"
    );
    
    /**
     * Initialize the type registry with legacy types that may not be
     * automatically registered due to circular dependencies
     */
    public static void initializeLegacyTypes() {
        MetaDataTypeRegistry registry = MetaDataTypeRegistry.getInstance();
        
        LEGACY_TYPE_MAPPINGS.forEach((typeName, className) -> {
            if (!registry.hasType(typeName)) {
                try {
                    registerLegacyType(registry, typeName, className);
                } catch (Exception e) {
                    log.debug("Could not register legacy type {}: {}", typeName, e.getMessage());
                }
            }
        });
        
        log.debug("Initialized legacy type mappings");
    }
    
    /**
     * Register a single legacy type
     */
    @SuppressWarnings("unchecked")
    private static void registerLegacyType(MetaDataTypeRegistry registry, String typeName, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (MetaData.class.isAssignableFrom(clazz)) {
                
                MetaDataTypeDefinition definition = MetaDataTypeDefinition.builder(
                    typeName, (Class<? extends MetaData>) clazz)
                    .description("Legacy " + typeName + " type")
                    .allowedSubTypes(getLegacyAllowedSubTypes(typeName))
                    .allowsChildren(getLegacyAllowsChildren(typeName))
                    .build();
                    
                registry.registerType(definition);
                log.debug("Registered legacy type: {} -> {}", typeName, clazz.getSimpleName());
            }
        } catch (ClassNotFoundException e) {
            log.debug("Legacy type class not found: {}", className);
        }
    }
    
    /**
     * Get legacy allowed subtypes for backward compatibility
     */
    private static java.util.Set<String> getLegacyAllowedSubTypes(String typeName) {
        return switch (typeName) {
            case "attr" -> java.util.Set.of("string", "int", "long", "double", "boolean", "date");
            case "field" -> java.util.Set.of("string", "int", "long", "double", "boolean", "date", "ref");
            case "object" -> java.util.Set.of("entity", "value", "proxy", "mapped");
            case "loader" -> java.util.Set.of("xml", "json", "manual", "simple");
            case "view" -> java.util.Set.of("list", "form", "detail");
            case "validator" -> java.util.Set.of("required", "length", "range", "pattern");
            default -> java.util.Set.of(); // Allow all subtypes for unknown types
        };
    }
    
    /**
     * Determine if legacy type allows children
     */
    private static boolean getLegacyAllowsChildren(String typeName) {
        return switch (typeName) {
            case "attr", "validator" -> false;  // Leaf nodes
            case "field", "object", "loader", "view" -> true;  // Can have children
            default -> true; // Default to allowing children
        };
    }
    
    /**
     * Convert legacy MetaData instances to use the modern type system
     */
    public static void migrateToModernTypes(MetaData metaData) {
        // Ensure type is registered
        String typeName = metaData.getTypeName();
        MetaDataTypeRegistry registry = MetaDataTypeRegistry.getInstance();
        
        if (!registry.hasType(typeName)) {
            // Try to register this type based on the actual class
            registerTypeFromInstance(registry, metaData);
        }
        
        // Recursively migrate children
        metaData.getChildren().forEach(MetaDataMigrationUtils::migrateToModernTypes);
        
        log.debug("Migrated MetaData to modern type system: {}", metaData.getName());
    }
    
    /**
     * Register a type definition based on an actual MetaData instance
     */
    private static void registerTypeFromInstance(MetaDataTypeRegistry registry, MetaData instance) {
        try {
            String typeName = instance.getTypeName();
            Class<? extends MetaData> clazz = instance.getClass();
            
            MetaDataTypeDefinition definition = MetaDataTypeDefinition.builder(typeName, clazz)
                .description("Auto-registered from instance: " + clazz.getSimpleName())
                .allowedSubTypes(java.util.Set.of()) // Allow all subtypes
                .allowsChildren(true) // Default to allowing children
                .build();
                
            registry.registerType(definition);
            log.debug("Auto-registered type from instance: {} -> {}", typeName, clazz.getSimpleName());
            
        } catch (Exception e) {
            log.warn("Failed to auto-register type from instance: {}", instance.getName(), e);
        }
    }
    
    /**
     * Validate that a MetaData instance is compatible with the modern type system
     */
    public static ValidationResult validateModernCompatibility(MetaData metaData) {
        ValidationResult.Builder builder = ValidationResult.builder();
        
        // Check if type is registered
        MetaDataTypeRegistry registry = MetaDataTypeRegistry.getInstance();
        if (!registry.hasType(metaData.getTypeName())) {
            builder.addError("Type '" + metaData.getTypeName() + "' is not registered in the type system");
        }
        
        // Check if the instance class matches the registered type
        registry.getType(metaData.getTypeName()).ifPresent(definition -> {
            if (!definition.implementationClass().isInstance(metaData)) {
                builder.addError("Instance class " + metaData.getClass().getName() + 
                    " does not match registered implementation class " + definition.implementationClass().getName());
            }
            
            // Check subtype validity
            if (!definition.isSubTypeAllowed(metaData.getSubTypeName())) {
                builder.addError("SubType '" + metaData.getSubTypeName() + "' is not allowed for type '" + 
                    metaData.getTypeName() + "'");
            }
        });
        
        // Recursively validate children
        metaData.getChildren().forEach(child -> {
            ValidationResult childResult = validateModernCompatibility(child);
            if (!childResult.isValid()) {
                builder.addChildResult(child.getName(), childResult);
            }
        });
        
        return builder.build();
    }
    
    /**
     * Get a mapping of all legacy types to their modern equivalents
     */
    public static Map<String, String> getLegacyTypeMappings() {
        return new HashMap<>(LEGACY_TYPE_MAPPINGS);
    }
    
    /**
     * Check if a type name is a known legacy type
     */
    public static boolean isLegacyType(String typeName) {
        return LEGACY_TYPE_MAPPINGS.containsKey(typeName);
    }
    
    /**
     * Get the modern implementation class name for a legacy type
     */
    public static String getModernImplementationClass(String legacyTypeName) {
        return LEGACY_TYPE_MAPPINGS.get(legacyTypeName);
    }
}