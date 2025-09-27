package com.metaobjects.object;

import com.metaobjects.MetaData;
import com.metaobjects.MetaDataBuilder;
import com.metaobjects.InvalidMetaDataException;

/**
 * Builder for MetaObject instances providing fluent API for construction and configuration.
 * Extends MetaDataBuilder to inherit base functionality while adding MetaObject-specific operations.
 * 
 * @param <SELF> The concrete builder type for method chaining
 * @param <TARGET> The target MetaObject type being built
 */
public class MetaObjectBuilder<SELF extends MetaObjectBuilder<SELF, TARGET>, TARGET extends MetaObject> 
        extends MetaDataBuilder<SELF, TARGET> {
    
    /**
     * Constructor taking the target MetaObject to build
     * @param target The MetaObject to configure
     */
    public MetaObjectBuilder(TARGET target) {
        super(target);
    }
    
    /**
     * Add a child MetaData object (MetaObject-specific version)
     * @param child The child MetaData to add
     * @return This builder for method chaining
     */
    @Override
    public SELF addChild(MetaData child) throws InvalidMetaDataException {
        target.addChild(child);
        return self();
    }
    
    /**
     * Set the super object for this MetaObject
     * @param superObject The super MetaObject
     * @return This builder for method chaining
     */
    public SELF setSuperObject(MetaObject superObject) {
        target.setSuperObject(superObject);
        return self();
    }
    
    /**
     * Create an overloaded copy and return a new builder for it
     * @return A new builder wrapping the overloaded MetaObject
     */
    @SuppressWarnings("unchecked")
    public SELF overload() {
        TARGET overloaded = (TARGET) target.overload();
        try {
            return (SELF) this.getClass()
                    .getConstructor(target.getClass())
                    .newInstance(overloaded);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create builder for overloaded object", e);
        }
    }
}