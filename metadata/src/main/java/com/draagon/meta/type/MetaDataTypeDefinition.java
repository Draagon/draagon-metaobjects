package com.draagon.meta.type;

import com.draagon.meta.MetaData;

import java.util.Map;
import java.util.Set;
import java.util.Objects;

/**
 * Immutable definition of a MetaData type that provides type-safe metadata about
 * metadata types, including validation rules and factory information.
 * 
 * This record replaces the string-based type system with a more robust,
 * extensible approach that supports plugin architectures.
 */
public record MetaDataTypeDefinition(
    String typeName,
    String description,
    Class<? extends MetaData> implementationClass,
    Set<String> allowedSubTypes,
    Map<String, Object> metadata,
    boolean allowsChildren,
    boolean isAbstract
) {
    
    /**
     * Compact constructor with validation
     */
    public MetaDataTypeDefinition {
        Objects.requireNonNull(typeName, "Type name cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
        Objects.requireNonNull(implementationClass, "Implementation class cannot be null");
        Objects.requireNonNull(allowedSubTypes, "Allowed sub types cannot be null");
        Objects.requireNonNull(metadata, "Metadata map cannot be null");
        
        if (typeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Type name cannot be empty");
        }
        
        // Defensive copy to ensure immutability
        allowedSubTypes = Set.copyOf(allowedSubTypes);
        metadata = Map.copyOf(metadata);
    }
    
    /**
     * Builder for convenient construction
     */
    public static Builder builder(String typeName, Class<? extends MetaData> implementationClass) {
        return new Builder(typeName, implementationClass);
    }
    
    /**
     * Validates if the given subtype is allowed for this type definition
     */
    public boolean isSubTypeAllowed(String subType) {
        return allowedSubTypes.isEmpty() || allowedSubTypes.contains(subType);
    }
    
    /**
     * Gets metadata value by key with type casting
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> type) {
        Object value = metadata.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Gets metadata value by key with default
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, T defaultValue) {
        Object value = metadata.get(key);
        if (value != null) {
            try {
                return (T) value;
            } catch (ClassCastException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Fluent builder for MetaDataTypeDefinition
     */
    public static class Builder {
        private final String typeName;
        private final Class<? extends MetaData> implementationClass;
        private String description = "";
        private Set<String> allowedSubTypes = Set.of();
        private Map<String, Object> metadata = Map.of();
        private boolean allowsChildren = true;
        private boolean isAbstract = false;
        
        private Builder(String typeName, Class<? extends MetaData> implementationClass) {
            this.typeName = typeName;
            this.implementationClass = implementationClass;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder allowedSubTypes(Set<String> allowedSubTypes) {
            this.allowedSubTypes = allowedSubTypes;
            return this;
        }
        
        public Builder allowedSubTypes(String... allowedSubTypes) {
            this.allowedSubTypes = Set.of(allowedSubTypes);
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder allowsChildren(boolean allowsChildren) {
            this.allowsChildren = allowsChildren;
            return this;
        }
        
        public Builder isAbstract(boolean isAbstract) {
            this.isAbstract = isAbstract;
            return this;
        }
        
        public MetaDataTypeDefinition build() {
            return new MetaDataTypeDefinition(
                typeName, 
                description, 
                implementationClass, 
                allowedSubTypes, 
                metadata, 
                allowsChildren, 
                isAbstract
            );
        }
    }
}