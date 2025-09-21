package com.draagon.meta.key;

import com.draagon.meta.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrimaryKey extends MetaKey {

    private static final Logger log = LoggerFactory.getLogger(PrimaryKey.class);

    public final static String SUBTYPE = "primary";
    public final static String NAME = "primary";

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.registerType(PrimaryKey.class, def -> def
                .type(TYPE_KEY).subType(SUBTYPE)
                .description("Primary key for unique record identification")
                
                // PRIMARY KEY ATTRIBUTES
                .optionalAttribute("keys", "stringArray")
                .optionalAttribute("description", "string")
            );
            
            log.debug("Registered PrimaryKey type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register PrimaryKey type with unified registry", e);
        }
    }

    public PrimaryKey() {
        super(SUBTYPE, NAME);
    }
    
    public PrimaryKey(String name) {
        super(SUBTYPE, name);
    }

    @Override
    public ObjectKey getObjectKey(Object o) {
        return getObjectKeyForKeyFields( getDeclaringObject(), KeyTypes.PRIMARY, getKeyFields(), o );
    }
}
