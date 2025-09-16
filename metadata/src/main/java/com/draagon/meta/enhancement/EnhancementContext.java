package com.draagon.meta.enhancement;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * v6.0.0: Context information for MetaData enhancement operations.
 * Contains service name, template information, and other context properties.
 */
public class EnhancementContext {
    
    private final String serviceName;
    private final String templateName;
    private final Map<String, Object> properties;
    
    private EnhancementContext(Builder builder) {
        this.serviceName = builder.serviceName;
        this.templateName = builder.templateName;
        this.properties = Collections.unmodifiableMap(new HashMap<>(builder.properties));
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String getTemplateName() {
        return templateName;
    }
    
    public Map<String, Object> getProperties() {
        return properties;
    }
    
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, Class<T> type) {
        Object value = properties.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    public String getStringProperty(String key) {
        return getProperty(key, String.class);
    }
    
    public Boolean getBooleanProperty(String key) {
        return getProperty(key, Boolean.class);
    }
    
    public Integer getIntProperty(String key) {
        return getProperty(key, Integer.class);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String serviceName;
        private String templateName;
        private Map<String, Object> properties = new HashMap<>();
        
        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }
        
        public Builder templateName(String templateName) {
            this.templateName = templateName;
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
        
        public EnhancementContext build() {
            return new EnhancementContext(this);
        }
    }
    
    @Override
    public String toString() {
        return "EnhancementContext{" +
                "serviceName='" + serviceName + '\'' +
                ", templateName='" + templateName + '\'' +
                ", properties=" + properties.keySet() +
                '}';
    }
}