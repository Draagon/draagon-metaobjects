package com.draagon.meta.key;

import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.draagon.meta.key.MetaKey.SUBTYPE_BASE;

@MetaDataType(type = "key", subType = "primary", description = "Primary key for unique record identification")
public class PrimaryKey extends MetaKey {

    private static final Logger log = LoggerFactory.getLogger(PrimaryKey.class);

    public final static String SUBTYPE = "primary";
    public final static String NAME = "primary";

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.getInstance().registerType(PrimaryKey.class, def -> def
                .type(TYPE_KEY).subType(SUBTYPE)
                .description("Primary key for unique record identification")

                // INHERIT FROM BASE KEY
                .inheritsFrom(TYPE_KEY, SUBTYPE_BASE)

                // PRIMARY KEY SPECIFIC ATTRIBUTES (base attributes inherited)
                // Note: keys and description are inherited from key.base
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
