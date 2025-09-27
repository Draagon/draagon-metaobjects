package com.metaobjects.key;

import com.metaobjects.registry.MetaDataRegistry;

import static com.metaobjects.key.MetaKey.TYPE_KEY;
import static com.metaobjects.key.MetaKey.SUBTYPE_BASE;

/**
 * Primary key for unique record identification.
 */
public class PrimaryKey extends MetaKey {

    public final static String SUBTYPE = "primary";
    public final static String NAME = "primary";

    /**
     * Register this type with the MetaDataRegistry (called by provider)
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(PrimaryKey.class, def -> def
            .type(TYPE_KEY).subType(SUBTYPE)
            .description("Primary key for unique record identification")
            .inheritsFrom(TYPE_KEY, SUBTYPE_BASE)
        );
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
