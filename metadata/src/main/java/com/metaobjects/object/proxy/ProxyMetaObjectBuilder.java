package com.metaobjects.object.proxy;

import com.metaobjects.object.MetaObject;
import com.metaobjects.object.pojo.PojoMetaObjectBuilder;

/**
 * Builder for ProxyMetaObject instances providing fluent API for construction and configuration.
 * 
 * This builder provides a type-safe way to construct and configure ProxyMetaObject instances
 * using the builder pattern instead of direct method chaining on the object itself.
 */
public class ProxyMetaObjectBuilder extends PojoMetaObjectBuilder {
    
    /**
     * Constructor taking the target ProxyMetaObject to build
     * @param target The ProxyMetaObject to configure
     */
    public ProxyMetaObjectBuilder(ProxyMetaObject target) {
        super(target);
    }
    
    /**
     * Create a new builder for a ProxyMetaObject with the given name and object class
     * @param name The name for the ProxyMetaObject
     * @param objectClass The object class
     * @return A new builder instance
     */
    public static ProxyMetaObjectBuilder create(String name, Class<?> objectClass) {
        MetaObject metaObject = ProxyMetaObject.create(name, objectClass);
        return new ProxyMetaObjectBuilder((ProxyMetaObject) metaObject);
    }
    
    /**
     * Create a new builder for a ProxyMetaObject with the given name, object class, and proxy class
     * @param name The name for the ProxyMetaObject
     * @param objectClass The object class
     * @param proxyObjectClass The proxy object class
     * @return A new builder instance
     */
    public static ProxyMetaObjectBuilder create(String name, Class<?> objectClass, Class<?> proxyObjectClass) {
        MetaObject metaObject = ProxyMetaObject.create(name, objectClass, proxyObjectClass);
        return new ProxyMetaObjectBuilder((ProxyMetaObject) metaObject);
    }
    
    /**
     * Create a new builder for an existing ProxyMetaObject
     * @param proxyMetaObject The existing ProxyMetaObject to wrap in a builder
     * @return A new builder instance
     */
    public static ProxyMetaObjectBuilder forObject(ProxyMetaObject proxyMetaObject) {
        return new ProxyMetaObjectBuilder(proxyMetaObject);
    }
}