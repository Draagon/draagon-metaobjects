package com.metaobjects.object.pojo;

import com.metaobjects.object.MetaObjectBuilder;

/**
 * Builder for PojoMetaObject instances providing fluent API for construction and configuration.
 * 
 * This builder provides a type-safe way to construct and configure PojoMetaObject instances
 * using the builder pattern instead of direct method chaining on the object itself.
 */
public class PojoMetaObjectBuilder extends MetaObjectBuilder<PojoMetaObjectBuilder, PojoMetaObject> {
    
    /**
     * Constructor taking the target PojoMetaObject to build
     * @param target The PojoMetaObject to configure
     */
    public PojoMetaObjectBuilder(PojoMetaObject target) {
        super(target);
    }
    
    /**
     * Create a new builder for a PojoMetaObject with the given name
     * @param name The name for the PojoMetaObject
     * @return A new builder instance
     */
    public static PojoMetaObjectBuilder create(String name) {
        return new PojoMetaObjectBuilder(PojoMetaObject.create(name));
    }
    
    /**
     * Create a new builder for an existing PojoMetaObject
     * @param pojoMetaObject The existing PojoMetaObject to wrap in a builder
     * @return A new builder instance
     */
    public static PojoMetaObjectBuilder forObject(PojoMetaObject pojoMetaObject) {
        return new PojoMetaObjectBuilder(pojoMetaObject);
    }
}