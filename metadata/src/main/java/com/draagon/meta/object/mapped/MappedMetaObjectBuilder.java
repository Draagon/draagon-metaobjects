package com.draagon.meta.object.mapped;

import com.draagon.meta.object.MetaObjectBuilder;

/**
 * Builder for MappedMetaObject instances providing fluent API for construction and configuration.
 * 
 * This builder provides a type-safe way to construct and configure MappedMetaObject instances
 * using the builder pattern instead of direct method chaining on the object itself.
 */
public class MappedMetaObjectBuilder extends MetaObjectBuilder<MappedMetaObjectBuilder, MappedMetaObject> {
    
    /**
     * Constructor taking the target MappedMetaObject to build
     * @param target The MappedMetaObject to configure
     */
    public MappedMetaObjectBuilder(MappedMetaObject target) {
        super(target);
    }
    
    /**
     * Create a new builder for a MappedMetaObject with the given name
     * @param name The name for the MappedMetaObject
     * @return A new builder instance
     */
    public static MappedMetaObjectBuilder create(String name) {
        return new MappedMetaObjectBuilder(MappedMetaObject.create(name));
    }
    
    /**
     * Create a new builder for an existing MappedMetaObject
     * @param mappedMetaObject The existing MappedMetaObject to wrap in a builder
     * @return A new builder instance
     */
    public static MappedMetaObjectBuilder forObject(MappedMetaObject mappedMetaObject) {
        return new MappedMetaObjectBuilder(mappedMetaObject);
    }
}