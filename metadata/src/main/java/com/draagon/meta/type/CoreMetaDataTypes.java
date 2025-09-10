package com.draagon.meta.type;

import com.draagon.meta.MetaData;
import java.util.Set;

/**
 * Enum defining core MetaData types that ship with the framework.
 * This provides compile-time type safety for built-in types while
 * allowing runtime extension through the registry system.
 */
public enum CoreMetaDataTypes {
    
    ATTRIBUTE("attr", "Metadata attribute", "com.draagon.meta.attr.MetaAttribute"),
    FIELD("field", "Metadata field", "com.draagon.meta.field.MetaField"),
    OBJECT("object", "Metadata object", "com.draagon.meta.object.MetaObject"),
    LOADER("loader", "Metadata loader", "com.draagon.meta.loader.MetaDataLoader"),
    VIEW("view", "Metadata view", "com.draagon.meta.view.MetaView"),
    VALIDATOR("validator", "Metadata validator", "com.draagon.meta.validator.MetaValidator");
    
    private final String typeName;
    private final String description;
    private final String implementationClassName;
    private volatile Class<? extends MetaData> implementationClass;
    
    CoreMetaDataTypes(String typeName, String description, String implementationClassName) {
        this.typeName = typeName;
        this.description = description;
        this.implementationClassName = implementationClassName;
    }
    
    public String getTypeName() {
        return typeName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get the implementation class, loading it lazily to avoid circular dependencies
     */
    @SuppressWarnings("unchecked")
    public Class<? extends MetaData> getImplementationClass() {
        if (implementationClass == null) {
            synchronized (this) {
                if (implementationClass == null) {
                    try {
                        Class<?> clazz = Class.forName(implementationClassName);
                        if (MetaData.class.isAssignableFrom(clazz)) {
                            implementationClass = (Class<? extends MetaData>) clazz;
                        } else {
                            throw new IllegalStateException(
                                "Class " + implementationClassName + " is not a MetaData subclass"
                            );
                        }
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException(
                            "Core MetaData implementation class not found: " + implementationClassName, e
                        );
                    }
                }
            }
        }
        return implementationClass;
    }
    
    /**
     * Create a MetaDataTypeDefinition for this core type
     */
    public MetaDataTypeDefinition createTypeDefinition() {
        return MetaDataTypeDefinition.builder(typeName, getImplementationClass())
            .description(description)
            .allowedSubTypes(getDefaultAllowedSubTypes())
            .allowsChildren(getAllowsChildren())
            .build();
    }
    
    /**
     * Get default allowed subtypes for each core type
     */
    private Set<String> getDefaultAllowedSubTypes() {
        return switch (this) {
            case ATTRIBUTE -> Set.of("string", "int", "long", "double", "boolean", "date", "enum");
            case FIELD -> Set.of("string", "int", "long", "double", "boolean", "date", "ref");
            case OBJECT -> Set.of("entity", "value", "proxy", "mapped");
            case LOADER -> Set.of("xml", "json", "yaml", "manual", "simple");
            case VIEW -> Set.of("list", "form", "detail", "summary");
            case VALIDATOR -> Set.of("required", "length", "range", "pattern", "custom");
        };
    }
    
    /**
     * Determine if this type allows children
     */
    private boolean getAllowsChildren() {
        return switch (this) {
            case ATTRIBUTE, VALIDATOR -> false;  // Leaf nodes
            case FIELD, OBJECT, LOADER, VIEW -> true;  // Can have children
        };
    }
    
    /**
     * Find core type by type name
     */
    public static CoreMetaDataTypes fromTypeName(String typeName) {
        for (CoreMetaDataTypes type : values()) {
            if (type.typeName.equals(typeName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown core MetaData type: " + typeName);
    }
    
    /**
     * Check if the given type name is a core type
     */
    public static boolean isCoreType(String typeName) {
        for (CoreMetaDataTypes type : values()) {
            if (type.typeName.equals(typeName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Register all core types with the registry
     */
    public static void registerAllCoreTypes(MetaDataTypeRegistry registry) {
        for (CoreMetaDataTypes coreType : values()) {
            try {
                MetaDataTypeDefinition definition = coreType.createTypeDefinition();
                registry.registerType(definition);
            } catch (Exception e) {
                // Log but continue - some classes might not be loaded yet
                System.err.println("Failed to register core type: " + coreType.typeName + " - " + e.getMessage());
            }
        }
    }
    
    @Override
    public String toString() {
        return typeName + " (" + description + ")";
    }
}