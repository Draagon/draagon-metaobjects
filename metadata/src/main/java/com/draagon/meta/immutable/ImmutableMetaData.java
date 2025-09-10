package com.draagon.meta.immutable;

import com.draagon.meta.MetaData;
import com.draagon.meta.ValidationResult;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.type.MetaDataTypeDefinition;

import java.util.*;
import java.util.stream.Stream;

/**
 * Immutable variant of MetaData that provides thread-safety through immutability.
 * Once created, this object cannot be modified. All mutation operations return
 * new instances with the requested changes.
 * 
 * This is ideal for high-concurrency scenarios where thread safety is critical
 * and the overhead of creating new instances is acceptable.
 */
public final class ImmutableMetaData {
    
    private final String type;
    private final String subType;
    private final String name;
    private final String shortName;
    private final String pkg;
    private final List<ImmutableMetaData> children;
    private final ImmutableMetaData superData;
    private final Map<String, Object> attributes;
    private final MetaDataTypeDefinition typeDefinition;
    
    // Cached hash code for performance
    private final int hashCode;
    
    /**
     * Private constructor - use Builder to create instances
     */
    private ImmutableMetaData(Builder builder) {
        this.type = Objects.requireNonNull(builder.type, "Type cannot be null");
        this.subType = Objects.requireNonNull(builder.subType, "SubType cannot be null");
        this.name = Objects.requireNonNull(builder.name, "Name cannot be null");
        
        // Calculate package and short name
        int separatorIndex = name.lastIndexOf(MetaData.PKG_SEPARATOR);
        if (separatorIndex >= 0) {
            this.shortName = name.substring(separatorIndex + MetaData.PKG_SEPARATOR.length());
            this.pkg = name.substring(0, separatorIndex);
        } else {
            this.shortName = name;
            this.pkg = "";
        }
        
        // Create immutable collections
        this.children = List.copyOf(builder.children);
        this.attributes = Map.copyOf(builder.attributes);
        this.superData = builder.superData;
        this.typeDefinition = builder.typeDefinition;
        
        // Pre-calculate hash code
        this.hashCode = Objects.hash(type, subType, name, children, superData, attributes);
    }
    
    // ========== ACCESSOR METHODS ==========
    
    public String getType() { return type; }
    public String getSubType() { return subType; }
    public String getName() { return name; }
    public String getShortName() { return shortName; }
    public String getPackage() { return pkg; }
    
    public List<ImmutableMetaData> getChildren() { return children; }
    public Optional<ImmutableMetaData> getSuperData() { return Optional.ofNullable(superData); }
    public Map<String, Object> getAttributes() { return attributes; }
    public Optional<MetaDataTypeDefinition> getTypeDefinition() { return Optional.ofNullable(typeDefinition); }
    
    // ========== QUERY METHODS ==========
    
    /**
     * Check if this is of the specified type
     */
    public boolean isType(String type) {
        return this.type.equals(type);
    }
    
    /**
     * Find child by name
     */
    public Optional<ImmutableMetaData> findChild(String name) {
        return children.stream()
            .filter(child -> child.getName().equals(name))
            .findFirst();
    }
    
    /**
     * Find children by type
     */
    public Stream<ImmutableMetaData> findChildrenByType(String type) {
        return children.stream()
            .filter(child -> child.isType(type));
    }
    
    /**
     * Get all children as stream
     */
    public Stream<ImmutableMetaData> getChildrenStream() {
        return children.stream();
    }
    
    /**
     * Check if has children
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }
    
    /**
     * Get child count
     */
    public int getChildCount() {
        return children.size();
    }
    
    /**
     * Get attribute value
     */
    public Optional<Object> getAttribute(String name) {
        return Optional.ofNullable(attributes.get(name));
    }
    
    /**
     * Get attribute value with type casting
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAttribute(String name, Class<T> type) {
        Object value = attributes.get(name);
        return value != null && type.isInstance(value) ? 
            Optional.of((T) value) : 
            Optional.empty();
    }
    
    /**
     * Check if has attribute
     */
    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }
    
    // ========== MUTATION METHODS (RETURN NEW INSTANCES) ==========
    
    /**
     * Add a child, returning a new instance
     */
    public ImmutableMetaData withChild(ImmutableMetaData child) {
        Objects.requireNonNull(child, "Child cannot be null");
        
        // Check if child already exists
        if (findChild(child.getName()).isPresent()) {
            throw new IllegalArgumentException("Child with name '" + child.getName() + "' already exists");
        }
        
        return new Builder(this)
            .addChild(child)
            .build();
    }
    
    /**
     * Remove a child by name, returning a new instance
     */
    public ImmutableMetaData withoutChild(String childName) {
        return new Builder(this)
            .removeChild(childName)
            .build();
    }
    
    /**
     * Replace a child, returning a new instance
     */
    public ImmutableMetaData withReplacedChild(ImmutableMetaData newChild) {
        Objects.requireNonNull(newChild, "New child cannot be null");
        
        return new Builder(this)
            .removeChild(newChild.getName())
            .addChild(newChild)
            .build();
    }
    
    /**
     * Set an attribute, returning a new instance
     */
    public ImmutableMetaData withAttribute(String name, Object value) {
        Objects.requireNonNull(name, "Attribute name cannot be null");
        
        return new Builder(this)
            .setAttribute(name, value)
            .build();
    }
    
    /**
     * Remove an attribute, returning a new instance
     */
    public ImmutableMetaData withoutAttribute(String name) {
        return new Builder(this)
            .removeAttribute(name)
            .build();
    }
    
    /**
     * Set super data, returning a new instance
     */
    public ImmutableMetaData withSuperData(ImmutableMetaData superData) {
        return new Builder(this)
            .superData(superData)
            .build();
    }
    
    // ========== CONVERSION METHODS ==========
    
    /**
     * Convert to mutable MetaData
     */
    public MetaData toMutable() {
        MetaData mutable = new MetaData(type, subType, name);
        
        // Copy attributes
        attributes.forEach((key, value) -> {
            if (value instanceof String) {
                // This is a simplified conversion - in reality you'd need proper attribute handling
                // mutable.addMetaAttr(new MetaAttribute<>(subType, key, DataTypes.STRING).setValue(value));
            }
        });
        
        // Copy children (recursively convert to mutable)
        children.forEach(child -> {
            MetaData mutableChild = child.toMutable();
            mutable.addChild(mutableChild);
        });
        
        // Set super data if present
        if (superData != null) {
            mutable.setSuperData(superData.toMutable());
        }
        
        return mutable;
    }
    
    // ========== VALIDATION ==========
    
    /**
     * Validate this immutable MetaData
     */
    public ValidationResult validate() {
        ValidationResult.Builder builder = ValidationResult.builder();
        
        // Basic validation
        if (type.trim().isEmpty()) {
            builder.addError("Type cannot be empty");
        }
        
        if (subType.trim().isEmpty()) {
            builder.addError("SubType cannot be empty");
        }
        
        if (name.trim().isEmpty()) {
            builder.addError("Name cannot be empty");
        }
        
        // Type system validation
        if (typeDefinition != null) {
            if (!typeDefinition.isSubTypeAllowed(subType)) {
                builder.addError("SubType '" + subType + "' not allowed for type '" + type + "'");
            }
            
            if (!typeDefinition.allowsChildren() && hasChildren()) {
                builder.addError("Type '" + type + "' does not allow children");
            }
        }
        
        // Validate children
        children.forEach(child -> {
            ValidationResult childResult = child.validate();
            if (!childResult.isValid()) {
                builder.addChildResult(child.getName(), childResult);
            }
        });
        
        return builder.build();
    }
    
    // ========== OBJECT METHODS ==========
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ImmutableMetaData that = (ImmutableMetaData) obj;
        return Objects.equals(type, that.type) &&
               Objects.equals(subType, that.subType) &&
               Objects.equals(name, that.name) &&
               Objects.equals(children, that.children) &&
               Objects.equals(superData, that.superData) &&
               Objects.equals(attributes, that.attributes);
    }
    
    @Override
    public int hashCode() {
        return hashCode; // Pre-calculated for performance
    }
    
    @Override
    public String toString() {
        return String.format("ImmutableMetaData[%s:%s]{%s}", type, subType, name);
    }
    
    // ========== STATIC FACTORY METHODS ==========
    
    /**
     * Create a new builder
     */
    public static Builder builder(String type, String subType, String name) {
        return new Builder().type(type).subType(subType).name(name);
    }
    
    /**
     * Convert from mutable MetaData
     */
    public static ImmutableMetaData fromMutable(MetaData mutable) {
        Builder builder = builder(mutable.getTypeName(), mutable.getSubTypeName(), mutable.getName());
        
        // Copy type definition
        mutable.getTypeDefinition().ifPresent(builder::typeDefinition);
        
        // Convert children
        mutable.getChildren().forEach(child -> {
            if (child != null) {
                builder.addChild(fromMutable(child));
            }
        });
        
        // Convert super data
        if (mutable.getSuperData() != null) {
            builder.superData(fromMutable(mutable.getSuperData()));
        }
        
        return builder.build();
    }
    
    // ========== BUILDER CLASS ==========
    
    public static class Builder {
        private String type;
        private String subType;
        private String name;
        private final List<ImmutableMetaData> children = new ArrayList<>();
        private final Map<String, Object> attributes = new HashMap<>();
        private ImmutableMetaData superData;
        private MetaDataTypeDefinition typeDefinition;
        
        public Builder() {}
        
        // Copy constructor
        public Builder(ImmutableMetaData source) {
            this.type = source.type;
            this.subType = source.subType;
            this.name = source.name;
            this.children.addAll(source.children);
            this.attributes.putAll(source.attributes);
            this.superData = source.superData;
            this.typeDefinition = source.typeDefinition;
        }
        
        public Builder type(String type) {
            this.type = type;
            return this;
        }
        
        public Builder subType(String subType) {
            this.subType = subType;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder addChild(ImmutableMetaData child) {
            this.children.add(Objects.requireNonNull(child));
            return this;
        }
        
        public Builder removeChild(String childName) {
            this.children.removeIf(child -> child.getName().equals(childName));
            return this;
        }
        
        public Builder setAttribute(String name, Object value) {
            this.attributes.put(name, value);
            return this;
        }
        
        public Builder removeAttribute(String name) {
            this.attributes.remove(name);
            return this;
        }
        
        public Builder superData(ImmutableMetaData superData) {
            this.superData = superData;
            return this;
        }
        
        public Builder typeDefinition(MetaDataTypeDefinition typeDefinition) {
            this.typeDefinition = typeDefinition;
            return this;
        }
        
        public ImmutableMetaData build() {
            return new ImmutableMetaData(this);
        }
    }
}