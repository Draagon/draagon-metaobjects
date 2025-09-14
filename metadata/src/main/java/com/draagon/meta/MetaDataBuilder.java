package com.draagon.meta;

import com.draagon.meta.attr.MetaAttribute;

/**
 * Builder for MetaData objects providing fluent API for construction and configuration.
 * Uses the self-typing pattern to ensure proper return types in inheritance hierarchies.
 * 
 * @param <SELF> The concrete builder type for method chaining
 * @param <TARGET> The target MetaData type being built
 */
public class MetaDataBuilder<SELF extends MetaDataBuilder<SELF, TARGET>, TARGET extends MetaData> {
    
    protected final TARGET target;
    
    /**
     * Constructor taking the target MetaData object to build
     * @param target The MetaData object to configure
     */
    public MetaDataBuilder(TARGET target) {
        this.target = target;
    }
    
    /**
     * Self-typing pattern method for fluent API
     * @return This builder instance with correct type
     */
    @SuppressWarnings("unchecked")
    protected final SELF self() {
        return (SELF) this;
    }
    
    /**
     * Add a MetaAttribute to the target (fluent version of addMetaAttrSafe)
     * @param attr The attribute to add
     * @return This builder for method chaining
     */
    public SELF addMetaAttr(MetaAttribute attr) {
        target.addMetaAttrSafe(attr);
        return self();
    }
    
    /**
     * Add a child MetaData object (fluent version of addChildSafe)
     * @param child The child MetaData to add
     * @return This builder for method chaining
     */
    public SELF addChild(MetaData child) throws InvalidMetaDataException {
        target.addChildSafe(child);
        return self();
    }
    
    
    /**
     * Build and return the configured MetaData object
     * @return The configured MetaData instance
     */
    public TARGET build() {
        return target;
    }
}