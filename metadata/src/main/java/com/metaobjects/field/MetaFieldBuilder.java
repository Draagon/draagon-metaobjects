package com.metaobjects.field;

import com.metaobjects.MetaData;
import com.metaobjects.MetaDataBuilder;
import com.metaobjects.InvalidMetaDataException;
import com.metaobjects.attr.MetaAttribute;

/**
 * Builder for MetaField instances providing fluent API for construction and configuration.
 * Extends MetaDataBuilder to inherit base functionality while adding MetaField-specific operations.
 * 
 * @param <SELF> The concrete builder type for method chaining
 * @param <TARGET> The target MetaField type being built
 */
public class MetaFieldBuilder<SELF extends MetaFieldBuilder<SELF, TARGET>, TARGET extends MetaField> 
        extends MetaDataBuilder<SELF, TARGET> {
    
    /**
     * Constructor taking the target MetaField to build
     * @param target The MetaField to configure
     */
    public MetaFieldBuilder(TARGET target) {
        super(target);
    }
    
    /**
     * Add a MetaAttribute to the MetaField (fluent version)
     * @param attr The attribute to add
     * @return This builder for method chaining
     */
    @Override
    public SELF addMetaAttr(MetaAttribute attr) {
        target.addMetaAttrSafe(attr);
        return self();
    }
    
    /**
     * Add a child MetaData object to the MetaField (fluent version)
     * @param child The child MetaData to add
     * @return This builder for method chaining
     */
    @Override
    public SELF addChild(MetaData child) throws InvalidMetaDataException {
        target.addChildSafe(child);
        return self();
    }
    
    /**
     * Create an overloaded copy and return a new builder for it
     * @return A new builder wrapping the overloaded MetaField
     */
    @SuppressWarnings("unchecked")
    public SELF overload() {
        TARGET overloaded = (TARGET) target.overload();
        try {
            return (SELF) this.getClass()
                    .getConstructor(target.getClass())
                    .newInstance(overloaded);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create builder for overloaded field", e);
        }
    }
}