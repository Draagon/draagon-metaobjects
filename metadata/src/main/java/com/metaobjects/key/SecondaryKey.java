package com.metaobjects.key;

import com.metaobjects.registry.MetaDataRegistry;

import static com.metaobjects.key.MetaKey.TYPE_KEY;
import static com.metaobjects.key.MetaKey.SUBTYPE_BASE;

/**
 * Secondary key for alternative record identification.
 */
public class SecondaryKey extends MetaKey {

    public final static String SUBTYPE = "secondary";

    /**
     * Register this type with the MetaDataRegistry (called by provider)
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(SecondaryKey.class, def -> def
            .type(TYPE_KEY).subType(SUBTYPE)
            .description("Secondary key for alternative record identification")
            .inheritsFrom(TYPE_KEY, SUBTYPE_BASE)
        );
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
