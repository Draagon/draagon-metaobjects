package com.draagon.meta.key;

import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.draagon.meta.key.MetaKey.SUBTYPE_BASE;

@MetaDataType(type = "key", subType = "secondary", description = "Secondary key for alternative record identification")
public class SecondaryKey extends MetaKey {

    private static final Logger log = LoggerFactory.getLogger(SecondaryKey.class);

    public final static String SUBTYPE = "secondary";

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.registerType(SecondaryKey.class, def -> def
                .type(TYPE_KEY).subType(SUBTYPE)
                .description("Secondary key for alternative record identification")

                // INHERIT FROM BASE KEY
                .inheritsFrom(TYPE_KEY, SUBTYPE_BASE)

                // SECONDARY KEY SPECIFIC ATTRIBUTES (base attributes inherited)
                // Note: keys and description are inherited from key.base
            );
            
            log.debug("Registered SecondaryKey type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register SecondaryKey type with unified registry", e);
        }
    }

    public SecondaryKey(String name) {
        super(SUBTYPE, name);
    }

    private SecondaryKey(String subType, String name) {
        super(subType, name);
    }

    @Override
    public ObjectKey getObjectKey(Object o) {
        return getObjectKeyForKeyFields( getDeclaringObject(), KeyTypes.SECONDARY, getKeyFields(), o );
    }
}
