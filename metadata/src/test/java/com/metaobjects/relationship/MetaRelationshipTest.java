package com.metaobjects.relationship;

import com.metaobjects.MetaDataException;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.registry.TypeDefinition;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive unit tests for MetaRelationship class hierarchy.
 * Tests the AI-optimized 3-type relationship system for model-driven metadata.
 */
public class MetaRelationshipTest {

    private MetaDataRegistry registry;

    @Before
    public void setUp() {
        // Get the shared registry and ensure relationship types are registered
        registry = MetaDataRegistry.getInstance();

        // Verify that relationship types are registered
        TypeDefinition baseDef = registry.getTypeDefinition(MetaRelationship.TYPE_RELATIONSHIP, MetaRelationship.SUBTYPE_BASE);
        assertNotNull("MetaRelationship base type should be registered", baseDef);

        TypeDefinition compDef = registry.getTypeDefinition(MetaRelationship.TYPE_RELATIONSHIP, CompositionRelationship.SUBTYPE_COMPOSITION);
        assertNotNull("CompositionRelationship type should be registered", compDef);

        TypeDefinition aggrDef = registry.getTypeDefinition(MetaRelationship.TYPE_RELATIONSHIP, AggregationRelationship.SUBTYPE_AGGREGATION);
        assertNotNull("AggregationRelationship type should be registered", aggrDef);

        TypeDefinition assocDef = registry.getTypeDefinition(MetaRelationship.TYPE_RELATIONSHIP, AssociationRelationship.SUBTYPE_ASSOCIATION);
        assertNotNull("AssociationRelationship type should be registered", assocDef);
    }

    @Test
    public void testCompositionRelationshipCreation() {
        // Test composition relationship creation
        CompositionRelationship relationship = new CompositionRelationship("userProfile");

        assertNotNull("Relationship should be created", relationship);
        assertEquals("Type should be relationship", MetaRelationship.TYPE_RELATIONSHIP, relationship.getType());
        assertEquals("SubType should be composition", CompositionRelationship.SUBTYPE_COMPOSITION, relationship.getSubType());
        assertEquals("Name should be set", "userProfile", relationship.getName());
        assertEquals("SubType should be composition", "composition", relationship.getSubType());
        assertEquals("Lifecycle should be dependent", "dependent", relationship.getLifecycle());
    }

    @Test
    public void testAggregationRelationshipCreation() {
        // Test aggregation relationship creation
        AggregationRelationship relationship = new AggregationRelationship("employees");

        assertNotNull("Relationship should be created", relationship);
        assertEquals("Type should be relationship", MetaRelationship.TYPE_RELATIONSHIP, relationship.getType());
        assertEquals("SubType should be aggregation", AggregationRelationship.SUBTYPE_AGGREGATION, relationship.getSubType());
        assertEquals("Name should be set", "employees", relationship.getName());
        assertEquals("SubType should be aggregation", "aggregation", relationship.getSubType());
        assertEquals("Lifecycle should be shared", "shared", relationship.getLifecycle());
    }

    @Test
    public void testAssociationRelationshipCreation() {
        // Test association relationship creation
        AssociationRelationship relationship = new AssociationRelationship("customer");

        assertNotNull("Relationship should be created", relationship);
        assertEquals("Type should be relationship", MetaRelationship.TYPE_RELATIONSHIP, relationship.getType());
        assertEquals("SubType should be association", AssociationRelationship.SUBTYPE_ASSOCIATION, relationship.getSubType());
        assertEquals("Name should be set", "customer", relationship.getName());
        assertEquals("SubType should be association", "association", relationship.getSubType());
        assertEquals("Lifecycle should be independent", "independent", relationship.getLifecycle());
    }

    @Test
    public void testEssentialAttributes() throws Exception {
        // Create composition relationship with all essential attributes
        CompositionRelationship relationship = new CompositionRelationship("userProfile");

        // Add target object attribute
        StringAttribute targetAttr = new StringAttribute(MetaRelationship.ATTR_TARGET_OBJECT);
        targetAttr.setValueAsString("Profile");
        relationship.addChild(targetAttr);

        // Add cardinality attribute
        StringAttribute cardAttr = new StringAttribute(MetaRelationship.ATTR_CARDINALITY);
        cardAttr.setValueAsString(MetaRelationship.CARDINALITY_ONE);
        relationship.addChild(cardAttr);

        // Add referenced by attribute
        StringAttribute refAttr = new StringAttribute(MetaRelationship.ATTR_REFERENCED_BY);
        refAttr.setValueAsString("profileId");
        relationship.addChild(refAttr);

        // Test accessors
        assertEquals("Target object should be Profile", "Profile", relationship.getTargetObject());
        assertEquals("Cardinality should be one", MetaRelationship.CARDINALITY_ONE, relationship.getCardinality());
        assertEquals("Referenced by should be profileId", "profileId", relationship.getReferencedBy());
    }

    @Test
    public void testSmartDefaults() {
        // Test that smart defaults work when attributes are not specified
        AssociationRelationship relationship = new AssociationRelationship("defaultTest");

        // Test default cardinality (should be "one")
        assertEquals("Default cardinality should be one", MetaRelationship.CARDINALITY_ONE, relationship.getCardinality());

        // Test null values for non-defaulted attributes
        assertNull("Target object should be null by default", relationship.getTargetObject());
        assertNull("Referenced by should be null by default", relationship.getReferencedBy());
    }

    @Test
    public void testDerivedProperties() throws Exception {
        // Test composition relationship properties
        CompositionRelationship compRelationship = new CompositionRelationship("orderItems");
        assertEquals("SubType should be composition", "composition", compRelationship.getSubType());
        assertEquals("Lifecycle should be dependent", "dependent", compRelationship.getLifecycle());

        // Test aggregation relationship properties
        AggregationRelationship aggrRelationship = new AggregationRelationship("employees");
        assertEquals("SubType should be aggregation", "aggregation", aggrRelationship.getSubType());
        assertEquals("Lifecycle should be shared", "shared", aggrRelationship.getLifecycle());

        // Test association relationship properties
        AssociationRelationship assocRelationship = new AssociationRelationship("customer");
        assertEquals("SubType should be association", "association", assocRelationship.getSubType());
        assertEquals("Lifecycle should be independent", "independent", assocRelationship.getLifecycle());
    }

    @Test
    public void testConvenienceMethods() throws Exception {
        CompositionRelationship compRelationship = new CompositionRelationship("convenienceTest");

        // Test cardinality convenience methods
        StringAttribute cardAttr = new StringAttribute(MetaRelationship.ATTR_CARDINALITY);
        cardAttr.setValueAsString(MetaRelationship.CARDINALITY_ONE);
        compRelationship.addChild(cardAttr);

        assertTrue("Should be one-to-one", compRelationship.isOneToOne());
        assertFalse("Should not be one-to-many", compRelationship.isOneToMany());

        // Change to many
        cardAttr.setValueAsString(MetaRelationship.CARDINALITY_MANY);

        assertFalse("Should not be one-to-one", compRelationship.isOneToOne());
        assertTrue("Should be one-to-many", compRelationship.isOneToMany());

        // Test semantic type convenience methods for composition
        assertTrue("Should be owning", compRelationship.isOwning());
        assertFalse("Should not be referencing", compRelationship.isReferencing());
        assertTrue("Should be composition", "composition".equals(compRelationship.getSubType()));
        assertFalse("Should not be association", "association".equals(compRelationship.getSubType()));

        // Test association relationship convenience methods
        AssociationRelationship assocRelationship = new AssociationRelationship("assocTest");

        assertFalse("Should not be owning", assocRelationship.isOwning());
        assertTrue("Should be referencing", assocRelationship.isReferencing());
        assertFalse("Should not be composition", "composition".equals(assocRelationship.getSubType()));
        assertTrue("Should be association", "association".equals(assocRelationship.getSubType()));
    }

    @Test
    public void testStringRepresentation() throws Exception {
        CompositionRelationship relationship = new CompositionRelationship("stringTest");

        // Add all essential attributes
        StringAttribute targetAttr = new StringAttribute(MetaRelationship.ATTR_TARGET_OBJECT);
        targetAttr.setValueAsString("Order");
        relationship.addChild(targetAttr);

        StringAttribute cardAttr = new StringAttribute(MetaRelationship.ATTR_CARDINALITY);
        cardAttr.setValueAsString(MetaRelationship.CARDINALITY_MANY);
        relationship.addChild(cardAttr);

        StringAttribute refAttr = new StringAttribute(MetaRelationship.ATTR_REFERENCED_BY);
        refAttr.setValueAsString("orderId");
        relationship.addChild(refAttr);

        String toString = relationship.toString();

        // Verify the toString format includes key information
        assertTrue("Should contain class name", toString.contains("Relationship"));
        assertTrue("Should contain type:subtype", toString.contains("relationship:composition"));
        assertTrue("Should contain relationship name", toString.contains("stringTest"));
        assertTrue("Should contain target object", toString.contains("Order"));
        assertTrue("Should contain cardinality", toString.contains("many"));
    }

    @Test
    public void testTypeConstants() {
        // Verify type constants are properly defined
        assertEquals("Type constant should be 'relationship'", "relationship", MetaRelationship.TYPE_RELATIONSHIP);
        assertEquals("Base subtype constant should be 'base'", "base", MetaRelationship.SUBTYPE_BASE);

        // Verify concrete subtype constants
        assertEquals("Composition subtype constant should be 'composition'", "composition", CompositionRelationship.SUBTYPE_COMPOSITION);
        assertEquals("Aggregation subtype constant should be 'aggregation'", "aggregation", AggregationRelationship.SUBTYPE_AGGREGATION);
        assertEquals("Association subtype constant should be 'association'", "association", AssociationRelationship.SUBTYPE_ASSOCIATION);

        // Verify attribute constants (3 essential attributes)
        assertEquals("Target object attribute", "targetObject", MetaRelationship.ATTR_TARGET_OBJECT);
        assertEquals("Cardinality attribute", "cardinality", MetaRelationship.ATTR_CARDINALITY);
        assertEquals("Referenced by attribute", "referencedBy", MetaRelationship.ATTR_REFERENCED_BY);

        // Verify cardinality constants
        assertEquals("One cardinality", "one", MetaRelationship.CARDINALITY_ONE);
        assertEquals("Many cardinality", "many", MetaRelationship.CARDINALITY_MANY);
    }

    @Test
    public void testRegistryIntegration() {
        // Test that concrete relationship types are properly registered and can be created via registry

        // Test CompositionRelationship creation
        CompositionRelationship compCreated = (CompositionRelationship) registry.createInstance(
            MetaRelationship.TYPE_RELATIONSHIP,
            CompositionRelationship.SUBTYPE_COMPOSITION,
            "registryCompTest"
        );
        assertNotNull("Should create CompositionRelationship via registry", compCreated);
        assertEquals("Should have correct type", MetaRelationship.TYPE_RELATIONSHIP, compCreated.getType());
        assertEquals("Should have correct subtype", CompositionRelationship.SUBTYPE_COMPOSITION, compCreated.getSubType());
        assertEquals("Should have correct name", "registryCompTest", compCreated.getName());

        // Test AggregationRelationship creation
        AggregationRelationship aggrCreated = (AggregationRelationship) registry.createInstance(
            MetaRelationship.TYPE_RELATIONSHIP,
            AggregationRelationship.SUBTYPE_AGGREGATION,
            "registryAggrTest"
        );
        assertNotNull("Should create AggregationRelationship via registry", aggrCreated);
        assertEquals("Should have correct subtype", AggregationRelationship.SUBTYPE_AGGREGATION, aggrCreated.getSubType());

        // Test AssociationRelationship creation
        AssociationRelationship assocCreated = (AssociationRelationship) registry.createInstance(
            MetaRelationship.TYPE_RELATIONSHIP,
            AssociationRelationship.SUBTYPE_ASSOCIATION,
            "registryAssocTest"
        );
        assertNotNull("Should create AssociationRelationship via registry", assocCreated);
        assertEquals("Should have correct subtype", AssociationRelationship.SUBTYPE_ASSOCIATION, assocCreated.getSubType());
    }

}