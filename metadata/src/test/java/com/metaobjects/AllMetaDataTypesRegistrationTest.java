package com.metaobjects;

import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.registry.SharedRegistryTestBase;
import com.metaobjects.field.*;
import com.metaobjects.object.pojo.PojoMetaObject;
import com.metaobjects.object.mapped.MappedMetaObject;
import com.metaobjects.object.proxy.ProxyMetaObject;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.attr.IntAttribute;
import com.metaobjects.attr.BooleanAttribute;
import com.metaobjects.validator.RequiredValidator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive test to verify all MetaData type registrations are working.
 * Uses SharedRegistryTestBase to avoid registry conflicts on different platforms.
 */
public class AllMetaDataTypesRegistrationTest extends SharedRegistryTestBase {

    @Before
    public void setUp() {
        // Use the shared registry from base class
        
        // Trigger static registrations for ALL field types
        try {
            // Original field types
            new StringField("testString");
            new IntegerField("testInt");
            
            // Previously added field types 
            new LongField("testLong");
            new DoubleField("testDouble");
            new BooleanField("testBoolean");
            new DateField("testDate");
            new TimestampField("testTimestamp");
            
            // Newly added field types
            new FloatField("testFloat");
            new DecimalField("testDecimal");  // High-precision decimal field
            new ObjectField("testObject");
            new StringArrayField("testStringArray");
            new ObjectArrayField("testObjectArray");
            new ClassField("testClass");
            
            // Object types
            new PojoMetaObject("testPojo");
            new MappedMetaObject("testMapped");
            new ProxyMetaObject("testProxy");
            
            // Attribute types
            new StringAttribute("testStringAttr");
            new IntAttribute("testIntAttr");
            new BooleanAttribute("testBoolAttr");
            new com.metaobjects.attr.LongAttribute("testLongAttr");
            new com.metaobjects.attr.DoubleAttribute("testDoubleAttr");
            
            // Validator types
            new RequiredValidator("testRequired");
            
            // Trigger view registrations
            Class.forName("com.metaobjects.view.BasicMetaView");
            
        } catch (Exception e) {
            // Ignore - just triggering static registrations
        }
    }

    @Test
    public void testRegistryHasAllTypes() {
        MetaDataRegistry.RegistryStats stats = sharedRegistry.getStats();
        assertNotNull("Registry stats should be available", stats);
        System.out.println("Registry has " + stats.totalTypes() + " types registered");

        // Should have at least field types + object types + attribute types
        assertTrue("Should have at least 16 types registered", stats.totalTypes() >= 16);
    }

    @Test
    public void testAllFieldTypesRegistered() {
        // Verify ALL field types are registered
        assertTrue("StringField should be registered",
                  sharedRegistry.isRegistered("field", "string"));
        assertTrue("IntegerField should be registered",
                  sharedRegistry.isRegistered("field", "int"));
        assertTrue("LongField should be registered",
                  sharedRegistry.isRegistered("field", "long"));
        assertTrue("DoubleField should be registered",
                  sharedRegistry.isRegistered("field", "double"));
        assertTrue("BooleanField should be registered",
                  sharedRegistry.isRegistered("field", "boolean"));
        assertTrue("DateField should be registered",
                  sharedRegistry.isRegistered("field", "date"));
        assertTrue("FloatField should be registered",
                  sharedRegistry.isRegistered("field", "float"));
        assertTrue("DecimalField should be registered",
                  sharedRegistry.isRegistered("field", "decimal"));
        assertTrue("ObjectField should be registered",
                  sharedRegistry.isRegistered("field", "object"));
        assertTrue("StringArrayField should be registered",
                  sharedRegistry.isRegistered("field", "stringArray"));
        assertTrue("ObjectArrayField should be registered",
                  sharedRegistry.isRegistered("field", "objectArray"));
        assertTrue("ClassField should be registered",
                  sharedRegistry.isRegistered("field", "class"));
    }

    @Test
    public void testObjectTypesRegistered() {
        // Verify object types are registered
        assertTrue("PojoMetaObject should be registered",
                  sharedRegistry.isRegistered("object", "pojo"));
        assertTrue("MappedMetaObject should be registered",
                  sharedRegistry.isRegistered("object", "map"));
    }

    @Test
    public void testAttributeTypesRegistered() {
        // Verify attribute types are registered
        assertTrue("StringAttribute should be registered",
                  sharedRegistry.isRegistered("attr", "string"));
        assertTrue("IntAttribute should be registered",
                  sharedRegistry.isRegistered("attr", "int"));
        assertTrue("BooleanAttribute should be registered",
                  sharedRegistry.isRegistered("attr", "boolean"));
    }

    @Test
    public void testFieldConstraintsWorking() {
        // Test that field constraints are working
        assertTrue("StringField should accept pattern attribute",
                  sharedRegistry.acceptsChild("field", "string", "attr", "string", "pattern"));
        assertTrue("DoubleField should accept range validation attributes",
                  sharedRegistry.acceptsChild("field", "double", "attr", "double", "minValue"));
        assertTrue("DecimalField should accept precision attribute",
                  sharedRegistry.acceptsChild("field", "decimal", "attr", "int", "precision"));
        assertTrue("ObjectField should accept objectRef attribute",
                  sharedRegistry.acceptsChild("field", "object", "attr", "string", "objectRef"));
    }

    @Test
    public void testAllRegisteredTypesDisplay() {
        System.out.println("ALL registered types:");
        sharedRegistry.getRegisteredTypes().forEach(typeId -> {
            System.out.println("  " + typeId.type() + "." + typeId.subType());
        });

        // Count by type
        long fieldCount = sharedRegistry.getRegisteredTypes().stream()
            .filter(t -> "field".equals(t.type())).count();
        long objectCount = sharedRegistry.getRegisteredTypes().stream()
            .filter(t -> "object".equals(t.type())).count();
        long attrCount = sharedRegistry.getRegisteredTypes().stream()
            .filter(t -> "attr".equals(t.type())).count();

        System.out.println("Field types: " + fieldCount);
        System.out.println("Object types: " + objectCount);
        System.out.println("Attribute types: " + attrCount);

        assertTrue("Should have at least 12 field types", fieldCount >= 12);
        assertTrue("Should have at least 2 object types", objectCount >= 2);
        assertTrue("Should have at least 3 attribute types", attrCount >= 3);
    }
}