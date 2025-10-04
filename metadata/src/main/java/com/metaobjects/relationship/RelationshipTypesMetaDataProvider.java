package com.metaobjects.relationship;

import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.registry.MetaDataTypeProvider;

/**
 * Relationship Types MetaData provider.
 * Registers abstract base type + 3 concrete relationship types.
 * Depends on core-types for metadata.base inheritance.
 */
public class RelationshipTypesMetaDataProvider implements MetaDataTypeProvider {

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // Register abstract base type first
        MetaRelationship.registerTypes(registry);

        // Register concrete relationship types
        CompositionRelationship.registerTypes(registry);
        AggregationRelationship.registerTypes(registry);
        AssociationRelationship.registerTypes(registry);
    }

    @Override
    public String getProviderId() {
        return "relationship-types";
    }

    @Override
    public String[] getDependencies() {
        return new String[]{"core-types"}; // Need metadata.base for inheritance
    }

    @Override
    public String getDescription() {
        return "Relationship Types (composition, aggregation, association)";
    }
}