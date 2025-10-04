package com.metaobjects.identity;

import com.metaobjects.MetaData;
import com.metaobjects.attr.MetaAttribute;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.metaobjects.object.MetaObject.ATTR_DESCRIPTION;
import static com.metaobjects.attr.StringArrayAttribute.SUBTYPE_STRING_ARRAY;

/**
 * Primary identity for object identification. Each object should have exactly one primary identity
 * that serves as the main identifier and is typically used for relationships and persistence.
 *
 * Primary identities support auto-generation strategies like increment, uuid, etc.
 */
public class PrimaryIdentity extends MetaIdentity {

    private static final Logger log = LoggerFactory.getLogger(PrimaryIdentity.class);

    /**
     * Create a primary identity with the specified name.
     * The subType is automatically set to "primary".
     */
    public PrimaryIdentity(String name) {
        super(SUBTYPE_PRIMARY, name);
    }

    /**
     * Register PrimaryIdentity type with the MetaDataRegistry.
     * Called by IdentityTypesMetaDataProvider during service discovery.
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(PrimaryIdentity.class, def -> def
            .type(TYPE_IDENTITY).subType(SUBTYPE_PRIMARY)
            .description("Primary identity for object identification")
            .inheritsFrom(MetaData.TYPE_METADATA, MetaData.SUBTYPE_BASE)
            .optionalAttribute(ATTR_FIELDS, SUBTYPE_STRING_ARRAY)
            .optionalAttribute(ATTR_GENERATION, StringAttribute.SUBTYPE_STRING)
            .optionalAttribute(ATTR_DESCRIPTION, StringAttribute.SUBTYPE_STRING)

            // ACCEPTS ANY ATTRIBUTES (for extensibility from service providers)
            .optionalChild(MetaAttribute.TYPE_ATTR, "*", "*")
        );
    }

    /**
     * Returns true if this primary identity has auto-generation enabled.
     * This is a convenience method for checking common generation strategies.
     */
    public boolean hasAutoGeneration() {
        String generation = getGeneration();
        return GENERATION_INCREMENT.equals(generation) || GENERATION_UUID.equals(generation);
    }

    /**
     * Returns true if this primary identity uses database auto-increment.
     */
    public boolean usesIncrement() {
        return GENERATION_INCREMENT.equals(getGeneration());
    }

    /**
     * Returns true if this primary identity uses UUID generation.
     */
    public boolean usesUuid() {
        return GENERATION_UUID.equals(getGeneration());
    }

    /**
     * Returns true if this primary identity expects application-assigned values.
     */
    public boolean usesAssignedValues() {
        return GENERATION_ASSIGNED.equals(getGeneration()) || getGeneration() == null;
    }

    @Override
    public String toString() {
        return String.format("%s[%s:%s]{%s -> %s}%s",
            getClass().getSimpleName(),
            getType(),
            getSubType(),
            getName(),
            getFields(),
            hasAutoGeneration() ? " [AUTO:" + getGeneration() + "]" : "");
    }
}