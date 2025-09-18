package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * v6.0.0: Context information for constraint validation operations.
 * Provides additional metadata and context about the validation being performed.
 */
public class ValidationContext {
    
    private final String operation;
    private final MetaData parentMetaData;
    private final String fieldName;
    private final Map<String, Object> properties;
    
    private ValidationContext(Builder builder) {
        this.operation = builder.operation;
        this.parentMetaData = builder.parentMetaData;
        this.fieldName = builder.fieldName;
        this.properties = Collections.unmodifiableMap(new HashMap<>(builder.properties));
    }
    
    /**
     * Get the operation being performed (e.g., "addChild", "setAttribute")
     * @return The operation name
     */
    public Optional<String> getOperation() {
        return Optional.ofNullable(operation);
    }
    
    /**
     * Get the parent metadata object
     * @return The parent metadata
     */
    public Optional<MetaData> getParentMetaData() {
        return Optional.ofNullable(parentMetaData);
    }
    
    /**
     * Get the field name being validated
     * @return The field name
     */
    public Optional<String> getFieldName() {
        return Optional.ofNullable(fieldName);
    }
    
    /**
     * Get a context property
     * @param key The property key
     * @return The property value
     */
    public Optional<Object> getProperty(String key) {
        return Optional.ofNullable(properties.get(key));
    }
    
    /**
     * Get a context property as a specific type
     * @param key The property key
     * @param type The expected type
     * @param <T> The type parameter
     * @return The property value cast to the specified type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getProperty(String key, Class<T> type) {
        Object value = properties.get(key);
        if (value != null && type.isInstance(value)) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }
    
    /**
     * Get all context properties
     * @return Unmodifiable map of properties
     */
    public Map<String, Object> getAllProperties() {
        return properties;
    }
    
    /**
     * Create a new ValidationContext builder
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Create a basic validation context for an operation
     * @param operation The operation being performed
     * @return A validation context
     */
    public static ValidationContext forOperation(String operation) {
        return builder().operation(operation).build();
    }
    
    /**
     * Create a validation context for adding a child
     * @param parent The parent metadata
     * @param child The child being added
     * @return A validation context
     */
    public static ValidationContext forAddChild(MetaData parent, MetaData child) {
        return builder()
            .operation("addChild")
            .parentMetaData(parent)
            .property("child", child)
            .build();
    }
    
    /**
     * Create a validation context for setting an attribute
     * @param metaData The metadata object
     * @param attributeName The attribute name
     * @param value The attribute value
     * @return A validation context
     */
    public static ValidationContext forSetAttribute(MetaData metaData, String attributeName, Object value) {
        return builder()
            .operation("setAttribute")
            .parentMetaData(metaData)
            .fieldName(attributeName)
            .property("attributeValue", value)
            .build();
    }
    
    /**
     * Builder for creating ValidationContext instances
     */
    public static class Builder {
        private String operation;
        private MetaData parentMetaData;
        private String fieldName;
        private Map<String, Object> properties = new HashMap<>();
        
        public Builder operation(String operation) {
            this.operation = operation;
            return this;
        }
        
        public Builder parentMetaData(MetaData parentMetaData) {
            this.parentMetaData = parentMetaData;
            return this;
        }
        
        public Builder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }
        
        public Builder property(String key, Object value) {
            this.properties.put(key, value);
            return this;
        }
        
        public Builder properties(Map<String, Object> properties) {
            this.properties.putAll(properties);
            return this;
        }
        
        public ValidationContext build() {
            return new ValidationContext(this);
        }
    }
}