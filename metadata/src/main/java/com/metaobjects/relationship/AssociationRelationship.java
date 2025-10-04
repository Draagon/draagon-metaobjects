package com.metaobjects.relationship;

import com.metaobjects.registry.MetaDataRegistry;

/**
 * Association relationship metadata.
 * Parent references independent child - independent lifecycle.
 * Use for: Order → Customer, Employee → Manager, Student → Courses
 */
public class AssociationRelationship extends MetaRelationship {

    /** Association subtype constant */
    public static final String SUBTYPE_ASSOCIATION = "association";

    /**
     * Register association relationship type with registry
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(AssociationRelationship.class, def -> def
            .type(TYPE_RELATIONSHIP).subType(SUBTYPE_ASSOCIATION)
            .description("Association relationship - parent references independent child (independent lifecycle)")
            .inheritsFrom(TYPE_RELATIONSHIP, SUBTYPE_BASE)
        );
    }

    public AssociationRelationship(String name) {
        super(SUBTYPE_ASSOCIATION, name);
    }

    @Override
    public String getLifecycle() {
        return LIFECYCLE_INDEPENDENT;
    }

    @Override
    public boolean isOwning() {
        return false;
    }

    @Override
    public boolean isReferencing() {
        return true;
    }
}