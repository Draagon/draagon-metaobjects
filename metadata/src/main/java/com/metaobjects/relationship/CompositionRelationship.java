package com.metaobjects.relationship;

import com.metaobjects.registry.MetaDataRegistry;

/**
 * Composition relationship metadata.
 * Parent exclusively owns child - when parent is deleted, child is also deleted.
 * Use for: User → Profile, Order → OrderItems, Document → Sections
 */
public class CompositionRelationship extends MetaRelationship {

    /** Composition subtype constant */
    public static final String SUBTYPE_COMPOSITION = "composition";

    /**
     * Register composition relationship type with registry
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(CompositionRelationship.class, def -> def
            .type(TYPE_RELATIONSHIP).subType(SUBTYPE_COMPOSITION)
            .description("Composition relationship - parent exclusively owns child (dependent lifecycle)")
            .inheritsFrom(TYPE_RELATIONSHIP, SUBTYPE_BASE)
        );
    }

    public CompositionRelationship(String name) {
        super(SUBTYPE_COMPOSITION, name);
    }

    @Override
    public String getLifecycle() {
        return LIFECYCLE_DEPENDENT;
    }

    @Override
    public boolean isOwning() {
        return true;
    }

    @Override
    public boolean isReferencing() {
        return false; 
    }
}