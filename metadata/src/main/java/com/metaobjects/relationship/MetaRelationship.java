package com.metaobjects.relationship;

import com.metaobjects.MetaData;
import com.metaobjects.attr.BooleanAttribute;
import com.metaobjects.attr.MetaAttribute;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.metaobjects.object.MetaObject.ATTR_DESCRIPTION;

/**
 * Abstract base relationship metadata for expressing object relationships.
 * Provides simplified model-driven relationship patterns optimized for AI generation.
 *
 * This is an ABSTRACT base type - use concrete subtypes:
 * - CompositionRelationship: Parent exclusively owns child (dependent lifecycle)
 * - AggregationRelationship: Parent has shared ownership (shared lifecycle)
 * - AssociationRelationship: Parent references independent child (independent lifecycle)
 */
public abstract class MetaRelationship extends MetaData {

    private static final Logger log = LoggerFactory.getLogger(MetaRelationship.class);

    // === TYPE AND SUBTYPE CONSTANTS ===
    /** Relationship type constant - MetaRelationship owns this concept */
    public final static String TYPE_RELATIONSHIP = "relationship";

    /** Abstract base relationship subtype - NEVER instantiate directly */
    public final static String SUBTYPE_BASE = "base";

    // === ESSENTIAL ATTRIBUTES (AI specifies these 3) ===
    /** Target object that this relationship points to */
    public final static String ATTR_TARGET_OBJECT = "targetObject";

    /** Cardinality of relationship: "one" or "many" */
    public final static String ATTR_CARDINALITY = "cardinality";

    /** Field name that implements the relationship */
    public final static String ATTR_REFERENCED_BY = "referencedBy";

    // === CARDINALITY CONSTANTS ===
    public static final String CARDINALITY_ONE = "one";
    public static final String CARDINALITY_MANY = "many";

    // === LIFECYCLES ===
    public static final String LIFECYCLE_DEPENDENT = "dependent";
    public static final String LIFECYCLE_INDEPENDENT = "independent";
    public static final String LIFECYCLE_SHARED = "shared";

    /**
     * Register this type with the MetaDataRegistry (called by provider)
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(MetaRelationship.class, def -> {
            def.type(TYPE_RELATIONSHIP).subType(SUBTYPE_BASE)
               .description("Abstract base relationship metadata with model-driven patterns")
               .inheritsFrom(MetaData.TYPE_METADATA, MetaData.SUBTYPE_BASE)
               // ACCEPTS ANY ATTRIBUTES (all relationship types inherit these)
               .optionalChild(MetaAttribute.TYPE_ATTR, "*", "*");

            // RELATIONSHIP-SPECIFIC ATTRIBUTES WITH FLUENT CONSTRAINTS
            def.optionalAttributeWithConstraints(ATTR_IS_ABSTRACT)
               .ofType(BooleanAttribute.SUBTYPE_BOOLEAN)
               .asSingle();

            def.optionalAttributeWithConstraints(ATTR_TARGET_OBJECT)
               .ofType(StringAttribute.SUBTYPE_STRING)
               .asSingle();

            def.optionalAttributeWithConstraints(ATTR_CARDINALITY)
               .ofType(StringAttribute.SUBTYPE_STRING)
               .asSingle();

            def.optionalAttributeWithConstraints(ATTR_REFERENCED_BY)
               .ofType(StringAttribute.SUBTYPE_STRING)
               .asSingle();

            def.optionalAttributeWithConstraints(ATTR_DESCRIPTION)
               .ofType(StringAttribute.SUBTYPE_STRING)
               .asSingle();
        });
    }

    protected MetaRelationship(String subType, String name) {
        super(TYPE_RELATIONSHIP, subType, name);
    }

    // === ESSENTIAL ATTRIBUTE ACCESSORS ===

    public String getTargetObject() {
        return hasMetaAttr(ATTR_TARGET_OBJECT) ?
               getMetaAttr(ATTR_TARGET_OBJECT).getValueAsString() : null;
    }

    public String getCardinality() {
        return hasMetaAttr(ATTR_CARDINALITY) ?
               getMetaAttr(ATTR_CARDINALITY).getValueAsString() : CARDINALITY_ONE;
    }

    public String getReferencedBy() {
        return hasMetaAttr(ATTR_REFERENCED_BY) ?
               getMetaAttr(ATTR_REFERENCED_BY).getValueAsString() : null;
    }

    /**
     * Returns lifecycle dependency based on subtype.
     */
    public abstract String getLifecycle();


    public boolean isOneToOne() {
        return CARDINALITY_ONE.equals(getCardinality());
    }

    public boolean isOneToMany() {
        return CARDINALITY_MANY.equals(getCardinality());
    }

    /**
     * Convenience method: Check if this relationship represents ownership (composition/aggregation)
     */
    public abstract boolean isOwning();

    /**
     * Convenience method: Check if this relationship represents a reference (association)
     */
    public abstract boolean isReferencing();

    @Override
    public String toString() {
        return String.format("%s[%s:%s]{%s -> %s (%s)}",
            getClass().getSimpleName(),
            getType(),
            getSubType(),
            getName(),
            getTargetObject(),
            getCardinality());
    }
}