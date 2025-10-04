package com.metaobjects.relationship;

import com.metaobjects.registry.MetaDataRegistry;

/**
 * Aggregation relationship metadata.
 * Parent has shared ownership - child may survive if referenced elsewhere.
 * Use for: Department → Employees, Team → Members, Course → Students
 */
public class AggregationRelationship extends MetaRelationship {

    /** Aggregation subtype constant */
    public static final String SUBTYPE_AGGREGATION = "aggregation";

    /**
     * Register aggregation relationship type with registry
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(AggregationRelationship.class, def -> def
            .type(TYPE_RELATIONSHIP).subType(SUBTYPE_AGGREGATION)
            .description("Aggregation relationship - parent has shared ownership (shared lifecycle)")
            .inheritsFrom(TYPE_RELATIONSHIP, SUBTYPE_BASE)
        );
    }

    public AggregationRelationship(String name) {
        super(SUBTYPE_AGGREGATION, name);
    }

    @Override
    public String getLifecycle() {
        return LIFECYCLE_SHARED;
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