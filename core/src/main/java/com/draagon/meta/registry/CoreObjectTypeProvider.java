package com.draagon.meta.registry;

import com.draagon.meta.MetaDataTypeId;
import com.draagon.meta.object.value.ValueMetaObject;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.attr.BooleanAttribute;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.attr.StringArrayAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core object type provider that registers object types specific to the core module.
 * 
 * <p>This provider registers object types that depend on core module classes
 * and cannot be registered in the base metadata module due to dependency hierarchy.</p>
 * 
 * @since 6.0.0
 */
public class CoreObjectTypeProvider implements MetaDataTypeProvider {
    
    private static final Logger log = LoggerFactory.getLogger(CoreObjectTypeProvider.class);

    @Override
    public void registerTypes(MetaDataTypeRegistry registry) {
        log.debug("Registering core object types and essential attr types");
        
        // Register ValueMetaObject for "value" subtype
        registry.registerHandler(new MetaDataTypeId("object", "value"), 
            ValueMetaObject.class);
            
        // Register essential attr types for XML metadata parsing
        // These are needed for <attr> elements to be processed correctly
        registry.registerHandler(new MetaDataTypeId("attr", "string"), 
            StringAttribute.class);
        registry.registerHandler(new MetaDataTypeId("attr", "boolean"), 
            BooleanAttribute.class);
        registry.registerHandler(new MetaDataTypeId("attr", "int"), 
            IntAttribute.class);
        registry.registerHandler(new MetaDataTypeId("attr", "stringarray"), 
            StringArrayAttribute.class);
            
        log.info("Registered core object types and {} attr types", 4);
    }

    @Override
    public void registerDefaults(MetaDataTypeRegistry registry) {
        // Register default subtype for attr (string is most common)
        registry.registerDefaultSubType("attr", "string");
        log.debug("Registered default subtype for attr -> string");
    }

    @Override
    public void enhanceValidation(MetaDataTypeRegistry registry) {
        // No validation enhancements needed for core object types
        log.debug("No validation enhancements required for core object types");
    }

    @Override
    public String getDescription() {
        return "Core Object Type Provider - registers ValueMetaObject, essential attr types, and other core-specific types";
    }
}