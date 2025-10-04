package com.metaobjects.identity;

import com.metaobjects.MetaData;
import com.metaobjects.attr.MetaAttribute;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.metaobjects.object.MetaObject.ATTR_DESCRIPTION;
import static com.metaobjects.attr.StringAttribute.SUBTYPE_STRING;

/**
 * Secondary identity for business keys and alternate identifiers. Objects can have multiple
 * secondary identities for different business scenarios like email addresses, SKU codes, etc.
 *
 * Secondary identities typically use application-assigned values rather than auto-generation.
 */
public class SecondaryIdentity extends MetaIdentity {

    private static final Logger log = LoggerFactory.getLogger(SecondaryIdentity.class);

    /**
     * Create a secondary identity with the specified name.
     * The subType is automatically set to "secondary".
     */
    public SecondaryIdentity(String name) {
        super(SUBTYPE_SECONDARY, name);
    }

    /**
     * Register SecondaryIdentity type with the MetaDataRegistry.
     * Called by IdentityTypesMetaDataProvider during service discovery.
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(SecondaryIdentity.class, def -> {
            // ✅ FLUENT ARRAY CONSTRAINTS WITH CONSTANTS
            def.type(TYPE_IDENTITY).subType(SUBTYPE_SECONDARY)
               .description("Secondary identity for business keys and alternate identifiers")
               .inheritsFrom(MetaData.TYPE_METADATA, MetaData.SUBTYPE_BASE);

            // Configure each attribute separately to avoid method chaining conflicts
            def.optionalAttributeWithConstraints(ATTR_FIELDS).ofType(StringAttribute.SUBTYPE_STRING).asArray();
            def.optionalAttributeWithConstraints(ATTR_GENERATION).ofType(StringAttribute.SUBTYPE_STRING).withEnum(GENERATION_INCREMENT, GENERATION_UUID, GENERATION_ASSIGNED);
            def.optionalAttributeWithConstraints(ATTR_DESCRIPTION).ofType(StringAttribute.SUBTYPE_STRING).asSingle();

            // ACCEPTS ANY ATTRIBUTES (for extensibility from service providers)
            def.optionalChild(MetaAttribute.TYPE_ATTR, "*", "*");
        });
    }

    /**
     * Returns true if this secondary identity is unique across the object.
     * Most secondary identities should be unique (like email, SKU, etc.)
     */
    public boolean isUniqueKey() {
        // Secondary identities are typically unique business keys
        return true;
    }

    /**
     * Returns true if this secondary identity represents a business key.
     * Business keys are stable, meaningful identifiers used by business users.
     */
    public boolean isBusinessKey() {
        // Secondary identities are by definition business keys
        return true;
    }

    /**
     * Returns true if this secondary identity uses natural (meaningful) values.
     * This is typically true for secondary keys like email, SKU, username, etc.
     */
    public boolean usesNaturalValues() {
        // Most secondary identities use natural values rather than generated ones
        String generation = getGeneration();
        return generation == null || GENERATION_ASSIGNED.equals(generation);
    }

    /**
     * Returns true if this secondary identity supports lookups and queries.
     * Secondary identities are often used for finding objects by business criteria.
     */
    public boolean supportsLookup() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s[%s:%s]{%s -> %s}%s",
            getClass().getSimpleName(),
            getType(),
            getSubType(),
            getName(),
            getFields(),
            isCompound() ? " [COMPOUND]" : " [SIMPLE]");
    }
}