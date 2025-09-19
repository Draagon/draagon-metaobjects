package com.draagon.meta.loader.model;

import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.Validatable;

import java.util.List;

public interface MetaModel extends MetaObjectAware, Validatable {

    public final static String OBJECT_NAME      = "metadata";
    public final static String FIELD_PACKAGE    = "package";
    public final static String FIELD_TYPE       = "type";
    public final static String FIELD_SUBTYPE    = "subType";
    public final static String FIELD_NAME       = "name";
    public final static String FIELD_SUPER      = "super";
    public final static String FIELD_VALUE      = "value";
    public final static String FIELD_CHILDREN   = "children";
    public final static String OBJREF_CHILDREF  = "childRef";
    
    // Inline attribute support constants
    public final static String ATTR_CHILDREN_ARRAY = "childrenArray";  // Controls if children serialize as array [] vs "children": []
    public final static String INLINE_ATTR_PREFIX = "@";               // Prefix for inline attributes in JSON
    public final static String SYNTHETIC_ATTR_TYPE = "attr";           // Type for synthetic attribute children

    public String getPackage();
    public void setPackage(String pkg);

    public String getType();
    public void setType(String type);

    public String getSubType();
    public void setSubType(String subType);

    public String getName();
    public void setName(String name);

    public String getSuper();
    public void setSuper(String superStr);

    public String getValue();
    public void setValue(String value);

    public List<MetaModel> getChildren();
    public void setChildren(List<MetaModel> children);
    
    // Inline attribute helper methods (default implementations for backward compatibility)
    default void addInlineAttribute(String name, Object value) {
        List<MetaModel> children = getChildren();
        if (children == null) {
            children = new java.util.ArrayList<>();
            setChildren(children);
        }
        
        // Create synthetic attribute child
        MetaModel attrChild = createInlineAttributeChild(name, value);
        children.add(attrChild);
    }
    
    default MetaModel createInlineAttributeChild(String name, Object value) {
        // This would need to be implemented by concrete classes
        // For now, throw UnsupportedOperationException to force implementation
        throw new UnsupportedOperationException("createInlineAttributeChild must be implemented by concrete MetaModel classes");
    }
    
    default boolean hasInlineAttributes() {
        List<MetaModel> children = getChildren();
        if (children == null) return false;
        
        return children.stream()
            .anyMatch(child -> SYNTHETIC_ATTR_TYPE.equals(child.getType()));
    }
}
