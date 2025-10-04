package com.metaobjects.field;

import com.metaobjects.registry.MetaDataRegistry;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Simple test to verify field type registrations are working.
 */
public class SimpleFieldRegistrationTest {

    private MetaDataRegistry registry;

    @Before
    public void setUp() {
        registry = MetaDataRegistry.getInstance();
        
        // Trigger static registrations
        try {
            new StringField("testString");
            new IntegerField("testInt");
            new LongField("testLong");
            new DoubleField("testDouble");
            new BooleanField("testBoolean");
            new DateField("testDate");
            new FloatField("testFloat");
            new DecimalField("testDecimal");  // New high-precision decimal field
            new ObjectField("testObject");
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
        // Registry stats available for assertions - no verbose output needed
    }

    @Test
    public void testStringFieldRegistration() {
        assertTrue("StringField should be registered",
                  registry.isRegistered("field", "string"));
        
        assertTrue("StringField should accept string attributes",
                  registry.acceptsChild("field", "string", "attr", "string", "pattern"));
        
        String description = registry.getSupportedChildrenDescription("field", "string");
        // Field description available for assertions - no verbose output needed;
        assertNotNull("Should have description", description);
    }

    @Test 
    public void testLongFieldRegistration() {
        assertTrue("LongField should be registered",
                  registry.isRegistered("field", "long"));
        
        assertTrue("LongField should accept long attributes",
                  registry.acceptsChild("field", "long", "attr", "long", "minValue"));
        
        String description = registry.getSupportedChildrenDescription("field", "long");
        // Field description available for assertions - no verbose output needed;
        assertNotNull("Should have description", description);
    }

    @Test
    public void testDoubleFieldRegistration() {
        assertTrue("DoubleField should be registered",
                  registry.isRegistered("field", "double"));

        assertTrue("DoubleField should accept range validation attributes",
                  registry.acceptsChild("field", "double", "attr", "double", "minValue"));

        String description = registry.getSupportedChildrenDescription("field", "double");
        // Field description available for assertions - no verbose output needed;
        assertNotNull("Should have description", description);
    }

    @Test
    public void testBooleanFieldRegistration() {
        assertTrue("BooleanField should be registered",
                  registry.isRegistered("field", "boolean"));
        
        String description = registry.getSupportedChildrenDescription("field", "boolean");
        // Field description available for assertions - no verbose output needed;
        assertNotNull("Should have description", description);
    }

    @Test
    public void testDateFieldRegistration() {
        assertTrue("DateField should be registered",
                  registry.isRegistered("field", "date"));

        assertTrue("DateField should accept format attribute",
                  registry.acceptsChild("field", "date", "attr", "string", "dateFormat"));

        String description = registry.getSupportedChildrenDescription("field", "date");
        // Field description available for assertions - no verbose output needed;
        assertNotNull("Should have description", description);
    }

    @Test
    public void testTimeFieldRegistration() {
        assertTrue("TimeField should be registered",
                  registry.isRegistered("field", "time"));

        assertTrue("TimeField should accept format attribute",
                  registry.acceptsChild("field", "time", "attr", "string", "format"));

        assertTrue("TimeField should accept minTime attribute",
                  registry.acceptsChild("field", "time", "attr", "string", "minTime"));

        assertTrue("TimeField should accept maxTime attribute",
                  registry.acceptsChild("field", "time", "attr", "string", "maxTime"));

        String description = registry.getSupportedChildrenDescription("field", "time");
        // Field description available for assertions - no verbose output needed;
        assertNotNull("Should have description", description);
    }

    @Test
    public void testDecimalFieldRegistration() {
        assertTrue("DecimalField should be registered",
                  registry.isRegistered("field", "decimal"));

        assertTrue("DecimalField should accept precision attribute",
                  registry.acceptsChild("field", "decimal", "attr", "int", "precision"));

        assertTrue("DecimalField should accept scale attribute",
                  registry.acceptsChild("field", "decimal", "attr", "int", "scale"));

        assertTrue("DecimalField should accept minValue attribute",
                  registry.acceptsChild("field", "decimal", "attr", "string", "minValue"));

        String description = registry.getSupportedChildrenDescription("field", "decimal");
        // Field description available for assertions - no verbose output needed;
        assertNotNull("Should have description", description);
    }

    @Test
    public void testFloatFieldRegistration() {
        assertTrue("FloatField should be registered",
                  registry.isRegistered("field", "float"));

        assertTrue("FloatField should accept range validation attributes",
                  registry.acceptsChild("field", "float", "attr", "double", "minValue"));

        String description = registry.getSupportedChildrenDescription("field", "float");
        // Field description available for assertions - no verbose output needed;
        assertNotNull("Should have description", description);
    }

    @Test
    public void testUniversalArraySupport() {
        // Test that all field types support @isArray attribute
        assertTrue("StringField should accept isArray attribute",
                  registry.acceptsChild("field", "string", "attr", "boolean", "isArray"));

        assertTrue("IntegerField should accept isArray attribute",
                  registry.acceptsChild("field", "int", "attr", "boolean", "isArray"));

        assertTrue("LongField should accept isArray attribute",
                  registry.acceptsChild("field", "long", "attr", "boolean", "isArray"));

        assertTrue("DoubleField should accept isArray attribute",
                  registry.acceptsChild("field", "double", "attr", "boolean", "isArray"));

        assertTrue("DecimalField should accept isArray attribute",
                  registry.acceptsChild("field", "decimal", "attr", "boolean", "isArray"));

        assertTrue("BooleanField should accept isArray attribute",
                  registry.acceptsChild("field", "boolean", "attr", "boolean", "isArray"));

        assertTrue("DateField should accept isArray attribute",
                  registry.acceptsChild("field", "date", "attr", "boolean", "isArray"));

        assertTrue("TimeField should accept isArray attribute",
                  registry.acceptsChild("field", "time", "attr", "boolean", "isArray"));

        // Test validation successful - no verbose output needed;
    }

    // @Test - DISABLED: isArrayType() method not implemented yet (array architecture on hold)
    // public void testIsArrayTypeMethod() {
    //     // Test the new isArrayType() method
    //     StringField regularField = new StringField("testRegular");
    //     assertFalse("Regular field should not be array type", regularField.isArrayType());
    //
    //     // Create a field with @isArray=true
    //     StringField arrayField = new StringField("testArray");
    //     arrayField.addMetaAttr(com.metaobjects.attr.BooleanAttribute.create("isArray", true));
    //     assertTrue("Array field should be detected as array type", arrayField.isArrayType());
    //
    //     // Test validation successful - no verbose output needed;
    // }

    @Test
    public void testTimeFieldCreationAndValidation() {
        // Test basic TimeField creation
        TimeField timeField = new TimeField("businessHours");
        assertNotNull("TimeField should be created", timeField);
        assertEquals("Field subtype should be 'time'", "time", timeField.getSubType());

        // Test format attribute
        timeField.addMetaAttr(com.metaobjects.attr.StringAttribute.create("format", "HH:mm"));
        assertEquals("Format should be HH:mm", "HH:mm", timeField.getFormat());

        // Test time constraints
        timeField.addMetaAttr(com.metaobjects.attr.StringAttribute.create("minTime", "08:00"));
        timeField.addMetaAttr(com.metaobjects.attr.StringAttribute.create("maxTime", "18:00"));
        assertEquals("MinTime should be 08:00", "08:00", timeField.getMinTime());
        assertEquals("MaxTime should be 18:00", "18:00", timeField.getMaxTime());

        // Test time validation
        assertTrue("Valid time should pass", timeField.isValidTime("09:30"));
        assertTrue("Valid time should pass", timeField.isValidTime("12:15"));
        assertTrue("Valid time should pass", timeField.isValidTime("17:45"));

        assertFalse("Early time should fail", timeField.isValidTime("07:30"));
        assertFalse("Late time should fail", timeField.isValidTime("19:30"));
        assertFalse("Invalid format should fail", timeField.isValidTime("9:30"));

        // Test factory method with constraints
        TimeField storeHours = TimeField.create("storeHours", "HH:mm", "09:00", "21:00");
        assertTrue("Store hours should accept 10:00", storeHours.isValidTime("10:00"));
        assertFalse("Store hours should reject 08:00", storeHours.isValidTime("08:00"));

        // Test validation successful - no verbose output needed;
    }

    @Test
    public void testRemovedFieldTypes() {
        // Verify that ByteField and ShortField are no longer registered
        assertFalse("ByteField should not be registered",
                   registry.isRegistered("field", "byte"));

        assertFalse("ShortField should not be registered",
                   registry.isRegistered("field", "short"));

        // Test validation successful - no verbose output needed;
    }

    @Test
    public void testAllRegisteredTypes() {
        // Registry types available for assertions - no verbose output needed
        MetaDataRegistry.RegistryStats stats = registry.getStats();
    }
}