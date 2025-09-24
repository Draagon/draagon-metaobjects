package com.draagon.meta.field;

import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.SharedTestRegistry;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Simple test to verify field type registrations are working.
 */
public class SimpleFieldRegistrationTest {

    private static final Logger log = LoggerFactory.getLogger(SimpleFieldRegistrationTest.class);
    private MetaDataRegistry registry;

    @Before
    public void setUp() {
        // Use SharedTestRegistry to ensure proper provider discovery timing
        SharedTestRegistry.getInstance();
        log.debug("SimpleFieldRegistrationTest setup with shared registry: {}", SharedTestRegistry.getStatus());

        registry = MetaDataRegistry.getInstance();
        
        // Trigger static registrations
        try {
            new StringField("testString");
            new IntegerField("testInt");
            new LongField("testLong");
            new DoubleField("testDouble");
            new BooleanField("testBoolean");
            new DateField("testDate");
            // New field types added
            new FloatField("testFloat");
            new ShortField("testShort");
            new ByteField("testByte");
            new ObjectField("testObject");
            new StringArrayField("testStringArray");
            new ObjectArrayField("testObjectArray");
            new ClassField("testClass");
        } catch (Exception e) {
            // Ignore
        }
    }

    @Test
    public void testRegistryIsAvailable() {
        assertNotNull("Registry should be available", registry);
        MetaDataRegistry.RegistryStats stats = registry.getStats();
        assertNotNull("Stats should be available", stats);
        System.out.println("Registry has " + stats.totalTypes() + " types registered");
    }

    @Test
    public void testStringFieldRegistration() {
        assertTrue("StringField should be registered",
                  registry.isRegistered("field", "string"));
        
        assertTrue("StringField should accept string attributes",
                  registry.acceptsChild("field", "string", "attr", "string", "pattern"));
        
        String description = registry.getSupportedChildrenDescription("field", "string");
        System.out.println("StringField description: " + description);
        assertNotNull("Should have description", description);
    }

    @Test 
    public void testLongFieldRegistration() {
        assertTrue("LongField should be registered",
                  registry.isRegistered("field", "long"));
        
        assertTrue("LongField should accept long attributes",
                  registry.acceptsChild("field", "long", "attr", "long", "minValue"));
        
        String description = registry.getSupportedChildrenDescription("field", "long");
        System.out.println("LongField description: " + description);
        assertNotNull("Should have description", description);
    }

    @Test
    public void testDoubleFieldRegistration() {
        assertTrue("DoubleField should be registered",
                  registry.isRegistered("field", "double"));
        
        assertTrue("DoubleField should accept precision attribute",
                  registry.acceptsChild("field", "double", "attr", "int", "precision"));
        
        String description = registry.getSupportedChildrenDescription("field", "double");
        System.out.println("DoubleField description: " + description);
        assertNotNull("Should have description", description);
    }

    @Test
    public void testBooleanFieldRegistration() {
        assertTrue("BooleanField should be registered",
                  registry.isRegistered("field", "boolean"));
        
        String description = registry.getSupportedChildrenDescription("field", "boolean");
        System.out.println("BooleanField description: " + description);
        assertNotNull("Should have description", description);
    }

    @Test
    public void testDateFieldRegistration() {
        assertTrue("DateField should be registered",
                  registry.isRegistered("field", "date"));
        
        assertTrue("DateField should accept format attribute",
                  registry.acceptsChild("field", "date", "attr", "string", "dateFormat"));
        
        String description = registry.getSupportedChildrenDescription("field", "date");
        System.out.println("DateField description: " + description);
        assertNotNull("Should have description", description);
    }

    @Test
    public void testAllRegisteredTypes() {
        System.out.println("All registered types:");
        registry.getRegisteredTypes().forEach(typeId -> {
            System.out.println("  " + typeId.type() + "." + typeId.subType());
        });
    }
}