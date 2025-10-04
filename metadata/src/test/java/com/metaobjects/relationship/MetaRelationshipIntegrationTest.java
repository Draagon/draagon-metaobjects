package com.metaobjects.relationship;

import com.metaobjects.MetaDataException;
import com.metaobjects.MetaDataNotFoundException;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.field.StringField;
import com.metaobjects.loader.simple.SimpleLoader;
import com.metaobjects.object.pojo.PojoMetaObject;
import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.registry.TypeDefinition;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Integration tests for MetaRelationship with the existing MetaObjects system.
 * Tests relationships working with MetaObject, JSON parsing, and constraint system.
 */
public class MetaRelationshipIntegrationTest {

    private MetaDataRegistry registry;
    private PojoMetaObject testObject;

    @Before
    public void setUp() {
        // Get shared registry and verify relationship support
        registry = MetaDataRegistry.getInstance();

        // Create a test MetaObject for relationship testing
        testObject = new PojoMetaObject("TestUser");

        // Add some test fields
        StringField nameField = new StringField("name");
        StringField emailField = new StringField("email");
        testObject.addMetaField(nameField);
        testObject.addMetaField(emailField);
    }

    @Test
    public void testMetaObjectRelationshipAccessors() throws Exception {
        // Test that MetaObject relationship accessor methods work correctly

        // Create a composition relationship (User owns Profile)
        MetaRelationship profileRel = new TestableMetaRelationship("base", "profile");
        addRelationshipAttributes(profileRel, "Profile", "one", "profileId");

        // Create an association relationship (User references Orders)
        MetaRelationship ordersRel = new TestableMetaRelationship("base", "orders");
        addRelationshipAttributes(ordersRel, "Order", "many", "orderIds");

        // Add relationships to the object
        testObject.addRelationship(profileRel);
        testObject.addRelationship(ordersRel);

        // Test basic relationship access
        assertTrue("Should have profile relationship", testObject.hasRelationship("profile"));
        assertTrue("Should have orders relationship", testObject.hasRelationship("orders"));
        assertFalse("Should not have unknown relationship", testObject.hasRelationship("unknown"));

        // Test getting all relationships
        Collection<MetaRelationship> allRels = testObject.getRelationships();
        assertEquals("Should have 2 relationships", 2, allRels.size());

        // Test getting specific relationships
        MetaRelationship foundProfile = testObject.getRelationship("profile");
        assertNotNull("Should find profile relationship", foundProfile);
        assertEquals("Profile relationship target should be Profile", "Profile", foundProfile.getTargetObject());

        // Test Optional-based access
        Optional<MetaRelationship> optProfile = testObject.findRelationship("profile");
        assertTrue("Should find profile relationship", optProfile.isPresent());
        assertEquals("Should be same relationship", foundProfile, optProfile.get());

        Optional<MetaRelationship> optUnknown = testObject.findRelationship("unknown");
        assertFalse("Should not find unknown relationship", optUnknown.isPresent());
    }

    @Test
    public void testRelationshipFiltering() throws Exception {
        // Test MetaObject relationship filtering methods

        // Create various relationship types using concrete classes
        CompositionRelationship compositionOne = new CompositionRelationship("profile");
        addRelationshipAttributes(compositionOne, "Profile", "one", "profileId");

        CompositionRelationship compositionMany = new CompositionRelationship("addresses");
        addRelationshipAttributes(compositionMany, "Address", "many", "addressIds");

        AssociationRelationship associationOne = new AssociationRelationship("manager");
        addRelationshipAttributes(associationOne, "User", "one", "managerId");

        AssociationRelationship associationMany = new AssociationRelationship("orders");
        addRelationshipAttributes(associationMany, "Order", "many", "orderIds");

        // Add all relationships
        testObject.addRelationship(compositionOne);
        testObject.addRelationship(compositionMany);
        testObject.addRelationship(associationOne);
        testObject.addRelationship(associationMany);

        // Test cardinality filtering
        Collection<MetaRelationship> oneRels = testObject.getOneToOneRelationships();
        assertEquals("Should have 2 one-to-one relationships", 2, oneRels.size());

        Collection<MetaRelationship> manyRels = testObject.getOneToManyRelationships();
        assertEquals("Should have 2 one-to-many relationships", 2, manyRels.size());

        // Test ownership filtering
        Collection<MetaRelationship> compositions = testObject.getCompositionRelationships();
        assertEquals("Should have 2 composition relationships", 2, compositions.size());

        Collection<MetaRelationship> associations = testObject.getAssociationRelationships();
        assertEquals("Should have 2 association relationships", 2, associations.size());

        // Test target filtering
        Collection<MetaRelationship> userRels = testObject.getRelationshipsByTarget("User");
        assertEquals("Should have 1 relationship targeting User", 1, userRels.size());
        assertEquals("Should be the manager relationship", "manager", userRels.iterator().next().getName());

        Collection<MetaRelationship> orderRels = testObject.getRelationshipsByTarget("Order");
        assertEquals("Should have 1 relationship targeting Order", 1, orderRels.size());
        assertEquals("Should be the orders relationship", "orders", orderRels.iterator().next().getName());

        // Test combined filtering using streams
        long compositionOneCount = testObject.getRelationshipsStream()
            .filter(r -> "composition".equals(r.getSubType()) && r.isOneToOne())
            .count();
        assertEquals("Should have 1 composition one-to-one relationship", 1, compositionOneCount);
    }

    @Test
    public void testConstraintSystemIntegration() {
        // Test that relationships work with the constraint system

        // Verify that relationship type is registered and accepts proper children
        TypeDefinition relDef = registry.getTypeDefinition(MetaRelationship.TYPE_RELATIONSHIP, MetaRelationship.SUBTYPE_BASE);
        assertNotNull("Relationship type should be registered", relDef);

        // Verify that objects can contain relationships (placement constraint)
        TypeDefinition objDef = registry.getTypeDefinition("object", "pojo");
        assertNotNull("Object type should be registered", objDef);

        // Create a relationship and verify it can be added to an object
        MetaRelationship testRel = new TestableMetaRelationship("base", "testConstraint");

        try {
            testObject.addRelationship(testRel);
            assertTrue("Relationship should be successfully added", testObject.hasRelationship("testConstraint"));
        } catch (Exception e) {
            fail("Adding relationship should not violate constraints: " + e.getMessage());
        }

        // Test that relationship accepts string attributes for essential properties
        try {
            StringAttribute targetAttr = new StringAttribute(MetaRelationship.ATTR_TARGET_OBJECT);
            targetAttr.setValueAsString("TestTarget");
            testRel.addChild(targetAttr);

            assertEquals("Target attribute should be set", "TestTarget", testRel.getTargetObject());
        } catch (Exception e) {
            fail("Adding target attribute should not violate constraints: " + e.getMessage());
        }
    }

    @Test
    public void testTypeRegistrationAndDiscovery() {
        // Test that relationships are properly registered and discoverable

        // Verify relationship base type is registered (but abstract)
        TypeDefinition baseType = registry.getTypeDefinition(MetaRelationship.TYPE_RELATIONSHIP, MetaRelationship.SUBTYPE_BASE);
        assertNotNull("Relationship base type should be registered", baseType);
        assertEquals("Should have correct type", MetaRelationship.TYPE_RELATIONSHIP, baseType.getType());
        assertEquals("Should have correct subtype", MetaRelationship.SUBTYPE_BASE, baseType.getSubType());

        // Test creating concrete relationship instances via registry
        CompositionRelationship compCreated = (CompositionRelationship) registry.createInstance(
            MetaRelationship.TYPE_RELATIONSHIP,
            CompositionRelationship.SUBTYPE_COMPOSITION,
            "discoveryCompTest"
        );
        assertNotNull("Should create CompositionRelationship via registry", compCreated);
        assertEquals("Should have correct name", "discoveryCompTest", compCreated.getName());
        assertEquals("Should have correct type", MetaRelationship.TYPE_RELATIONSHIP, compCreated.getType());
        assertEquals("Should have correct subtype", CompositionRelationship.SUBTYPE_COMPOSITION, compCreated.getSubType());

        // Test that abstract base cannot be instantiated
        try {
            registry.createInstance(MetaRelationship.TYPE_RELATIONSHIP, MetaRelationship.SUBTYPE_BASE, "shouldFail");
            fail("Should not be able to create instance of abstract base type");
        } catch (Exception e) {
            // Expected - abstract type cannot be instantiated
        }

        // Test that all essential attributes have proper type support
        String[] essentialAttrs = {
            MetaRelationship.ATTR_TARGET_OBJECT,
            MetaRelationship.ATTR_CARDINALITY,
            MetaRelationship.ATTR_REFERENCED_BY
        };

        for (String attrName : essentialAttrs) {
            StringAttribute attr = new StringAttribute(attrName);
            attr.setValueAsString("testValue");

            try {
                compCreated.addChild(attr);
                assertTrue("Should accept " + attrName + " attribute", compCreated.hasMetaAttr(attrName));
            } catch (Exception e) {
                fail("Should accept essential attribute " + attrName + ": " + e.getMessage());
            }
        }
    }

    @Test
    public void testInheritanceSupport() throws Exception {
        // Test that relationships work with MetaObject inheritance

        // Create a base object with a relationship
        PojoMetaObject baseUser = new PojoMetaObject("BaseUser");
        CompositionRelationship baseRel = new CompositionRelationship("baseProfile");
        addRelationshipAttributes(baseRel, "Profile", "one", "profileId");
        baseUser.addRelationship(baseRel);

        // Create a derived object that inherits from base
        PojoMetaObject adminUser = new PojoMetaObject("AdminUser");
        adminUser.setSuperObject(baseUser);

        // Add a specific relationship to derived object
        CompositionRelationship adminRel = new CompositionRelationship("permissions");
        addRelationshipAttributes(adminRel, "Permission", "many", "permissionIds");
        adminUser.addRelationship(adminRel);

        // Test that derived object sees both relationships
        assertTrue("Should have base relationship", adminUser.hasRelationship("baseProfile"));
        assertTrue("Should have derived relationship", adminUser.hasRelationship("permissions"));

        Collection<MetaRelationship> allRels = adminUser.getRelationships(true);
        assertEquals("Should have 2 relationships including inherited", 2, allRels.size());

        // Test that inherited relationship has correct properties
        MetaRelationship inheritedRel = adminUser.getRelationship("baseProfile");
        assertEquals("Inherited relationship should have correct target", "Profile", inheritedRel.getTargetObject());
        assertTrue("Inherited relationship should be composition", "composition".equals(inheritedRel.getSubType()));
    }

    @Test
    public void testErrorHandling() {
        // Test proper error handling for relationship operations

        // Test getting non-existent relationship
        try {
            testObject.getRelationship("nonExistent");
            fail("Should throw exception for non-existent relationship");
        } catch (MetaDataNotFoundException e) {
            // Expected behavior
            assertTrue("Error message should mention relationship name", e.getMessage().contains("nonExistent"));
        }

        // Test invalid relationship creation - constraint validation occurs during metadata construction
        try {
            CompositionRelationship nullNameRel = new CompositionRelationship(null);
            testObject.addRelationship(nullNameRel);
            fail("Should not allow null name when adding to MetaObject");
        } catch (Exception e) {
            // Expected behavior - constraint system should enforce naming requirements
            assertTrue("Should indicate naming constraint violation",
                      e.getMessage().contains("name") || e.getMessage().contains("null") ||
                      e.getMessage().contains("constraint") || e.getMessage().contains("identifier"));
        }
    }

    /**
     * Helper method to add essential relationship attributes (3-attribute approach)
     */
    private void addRelationshipAttributes(MetaRelationship relationship, String target, String cardinality, String referencedBy) throws Exception {
        if (target != null) {
            StringAttribute targetAttr = new StringAttribute(MetaRelationship.ATTR_TARGET_OBJECT);
            targetAttr.setValueAsString(target);
            relationship.addChild(targetAttr);
        }

        if (cardinality != null) {
            StringAttribute cardAttr = new StringAttribute(MetaRelationship.ATTR_CARDINALITY);
            cardAttr.setValueAsString(cardinality);
            relationship.addChild(cardAttr);
        }

        if (referencedBy != null) {
            StringAttribute refAttr = new StringAttribute(MetaRelationship.ATTR_REFERENCED_BY);
            refAttr.setValueAsString(referencedBy);
            relationship.addChild(refAttr);
        }
    }

    /**
     * Testable concrete implementation of MetaRelationship for integration testing
     */
    private static class TestableMetaRelationship extends MetaRelationship {
        public TestableMetaRelationship(String subType, String name) {
            super(subType, name);
        }

        @Override
        public String getLifecycle() {
            // Default lifecycle for test relationship
            return "test";
        }

        @Override
        public boolean isOwning() {
            // Test implementation - return false by default
            return false;
        }

        @Override
        public boolean isReferencing() {
            // Test implementation - return true by default
            return true;
        }
    }
}