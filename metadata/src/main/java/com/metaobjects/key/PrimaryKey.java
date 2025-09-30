package com.metaobjects.key;

import com.metaobjects.registry.MetaDataRegistry;

import static com.metaobjects.key.MetaKey.TYPE_KEY;
import static com.metaobjects.key.MetaKey.SUBTYPE_BASE;

/**
 * Primary key for unique record identification.
 * Supports auto-increment configuration for database ID generation.
 */
public class PrimaryKey extends MetaKey {

    public final static String SUBTYPE = "primary";
    public final static String NAME = "primary";

    // === DATABASE AUTO-INCREMENT ATTRIBUTES ===
    /** Database auto-increment attribute for ID generation strategy */
    public final static String ATTR_DB_AUTO_INCREMENT = "dbAutoIncrement";

    // Auto-increment strategies
    public final static String AUTO_INCREMENT_SEQUENTIAL = "sequential";
    public final static String AUTO_INCREMENT_UUID = "uuid";
    public final static String AUTO_INCREMENT_NONE = "none";

    /**
     * Register this type with the MetaDataRegistry (called by provider)
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(PrimaryKey.class, def -> def
            .type(TYPE_KEY).subType(SUBTYPE)
            .description("Primary key for unique record identification with auto-increment support")
            .inheritsFrom(TYPE_KEY, SUBTYPE_BASE)

            // Database auto-increment configuration
            .optionalAttribute(ATTR_DB_AUTO_INCREMENT, "string")
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

    /**
     * Returns the auto-increment strategy configured for this primary key.
     * @return AUTO_INCREMENT_SEQUENTIAL, AUTO_INCREMENT_UUID, AUTO_INCREMENT_NONE, or null if not configured
     */
    public String getAutoIncrementStrategy() {
        if (hasMetaAttr(ATTR_DB_AUTO_INCREMENT)) {
            String strategy = getMetaAttr(ATTR_DB_AUTO_INCREMENT).getValueAsString();
            return strategy != null ? strategy : AUTO_INCREMENT_NONE;
        }
        return null;
    }

    /**
     * Returns true if this primary key is configured for sequential auto-increment.
     */
    public boolean isSequentialAutoIncrement() {
        return AUTO_INCREMENT_SEQUENTIAL.equals(getAutoIncrementStrategy());
    }

    /**
     * Returns true if this primary key is configured for UUID auto-increment.
     */
    public boolean isUuidAutoIncrement() {
        return AUTO_INCREMENT_UUID.equals(getAutoIncrementStrategy());
    }

    /**
     * Returns true if this primary key has any auto-increment configuration.
     */
    public boolean hasAutoIncrement() {
        String strategy = getAutoIncrementStrategy();
        return strategy != null && !AUTO_INCREMENT_NONE.equals(strategy);
    }
}
