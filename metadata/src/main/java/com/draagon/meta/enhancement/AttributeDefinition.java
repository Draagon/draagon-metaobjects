package com.draagon.meta.enhancement;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * v6.0.0: Represents an attribute definition that can be applied to MetaData instances.
 * This replaces the TypesConfig overlay system for adding attributes dynamically.
 */
public class AttributeDefinition {
    
    private final String name;
    private final String type;
    private final String subType;
    private final String description;
    private final Set<String> applicableTypes;
    private final boolean required;
    private final Object defaultValue;
    
    private AttributeDefinition(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.subType = builder.subType;
        this.description = builder.description;
        this.applicableTypes = Collections.unmodifiableSet(new HashSet<>(builder.applicableTypes));
        this.required = builder.required;
        this.defaultValue = builder.defaultValue;
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    public String getSubType() {
        return subType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Set<String> getApplicableTypes() {
        return applicableTypes;
    }
    
    public boolean isRequired() {
        return required;
    }
    
    public Object getDefaultValue() {
        return defaultValue;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String name;
        private String type;
        private String subType;
        private String description;
        private Set<String> applicableTypes = new HashSet<>();
        private boolean required = false;
        private Object defaultValue = null;
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder type(String type) {
            this.type = type;
            return this;
        }
        
        public Builder subType(String subType) {
            this.subType = subType;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder applicableTypes(String... types) {
            Collections.addAll(this.applicableTypes, types);
            return this;
        }
        
        public Builder applicableTypes(Collection<String> types) {
            this.applicableTypes.addAll(types);
            return this;
        }
        
        public Builder required(boolean required) {
            this.required = required;
            return this;
        }
        
        public Builder defaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
        
        public AttributeDefinition build() {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Attribute name cannot be null or empty");
            }
            if (type == null || type.trim().isEmpty()) {
                throw new IllegalArgumentException("Attribute type cannot be null or empty");
            }
            if (subType == null || subType.trim().isEmpty()) {
                throw new IllegalArgumentException("Attribute subType cannot be null or empty");
            }
            return new AttributeDefinition(this);
        }
    }
    
    @Override
    public String toString() {
        return "AttributeDefinition{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", subType='" + subType + '\'' +
                ", applicableTypes=" + applicableTypes +
                '}';
    }
}