package com.draagon.meta;

import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.field.*;
import com.draagon.meta.object.pojo.PojoMetaObject;
import com.draagon.meta.object.mapped.MappedMetaObject;
import com.draagon.meta.object.proxy.ProxyMetaObject;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.attr.BooleanAttribute;
import com.draagon.meta.validator.RequiredValidator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive test to verify all MetaData type registrations are working.
 */
public class AllMetaDataTypesRegistrationTest {

    private MetaDataRegistry registry;

    @Before
    public void setUp() {
        registry = MetaDataRegistry.getInstance();
        
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
            new ShortField("testShort");
            new ByteField("testByte");
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
            new com.draagon.meta.attr.LongAttribute("testLongAttr");
            new com.draagon.meta.attr.DoubleAttribute("testDoubleAttr");
            
            // Validator types
            new RequiredValidator("testRequired");
            
            // Trigger view registrations
            Class.forName("com.draagon.meta.view.BasicMetaView");
            
        } catch (Exception e) {
            // Ignore - just triggering static registrations
        }
    }

    @Test
    public void testRegistryHasAllTypes() {
        MetaDataRegistry.RegistryStats stats = registry.getStats();
        assertNotNull("Registry stats should be available", stats);
        System.out.println("Registry has " + stats.totalTypes() + " types registered");
        
        // Should have at least field types + object types + attribute types
        assertTrue("Should have at least 16 types registered", stats.totalTypes() >= 16);
    }

    @Test
    public void testAllFieldTypesRegistered() {
        // Verify ALL field types are registered
        assertTrue("StringField should be registered", 
                  registry.isRegistered("field", "string"));
        assertTrue("IntegerField should be registered", 
                  registry.isRegistered("field", "int"));
        assertTrue("LongField should be registered", 
                  registry.isRegistered("field", "long"));
        assertTrue("DoubleField should be registered", 
                  registry.isRegistered("field", "double"));
        assertTrue("BooleanField should be registered", 
                  registry.isRegistered("field", "boolean"));
        assertTrue("DateField should be registered", 
                  registry.isRegistered("field", "date"));
        assertTrue("FloatField should be registered", 
                  registry.isRegistered("field", "float"));
        assertTrue("ShortField should be registered", 
                  registry.isRegistered("field", "short"));
        assertTrue("ByteField should be registered", 
                  registry.isRegistered("field", "byte"));
        assertTrue("ObjectField should be registered", 
                  registry.isRegistered("field", "object"));
        assertTrue("StringArrayField should be registered", 
                  registry.isRegistered("field", "stringArray"));
        assertTrue("ObjectArrayField should be registered", 
                  registry.isRegistered("field", "objectArray"));
        assertTrue("ClassField should be registered", 
                  registry.isRegistered("field", "class"));
    }

    @Test
    public void testObjectTypesRegistered() {
        // Verify object types are registered
        assertTrue("PojoMetaObject should be registered", 
                  registry.isRegistered("object", "pojo"));
        assertTrue("MappedMetaObject should be registered", 
                  registry.isRegistered("object", "map"));
    }

    @Test
    public void testAttributeTypesRegistered() {
        // Verify attribute types are registered
        assertTrue("StringAttribute should be registered", 
                  registry.isRegistered("attr", "string"));
        assertTrue("IntAttribute should be registered", 
                  registry.isRegistered("attr", "int"));
        assertTrue("BooleanAttribute should be registered", 
                  registry.isRegistered("attr", "boolean"));
    }

    @Test
    public void testFieldConstraintsWorking() {
        // Test that field constraints are working
        assertTrue("StringField should accept pattern attribute",
                  registry.acceptsChild("field", "string", "attr", "string", "pattern"));
        assertTrue("DoubleField should accept precision attribute",
                  registry.acceptsChild("field", "double", "attr", "int", "precision"));
        assertTrue("ObjectField should accept objectRef attribute",
                  registry.acceptsChild("field", "object", "attr", "string", "objectRef"));
    }

    @Test
    public void testAllRegisteredTypesDisplay() {
        System.out.println("ALL registered types:");
        registry.getRegisteredTypes().forEach(typeId -> {
            System.out.println("  " + typeId.type() + "." + typeId.subType());
        });
        
        // Count by type
        long fieldCount = registry.getRegisteredTypes().stream()
            .filter(t -> "field".equals(t.type())).count();
        long objectCount = registry.getRegisteredTypes().stream()
            .filter(t -> "object".equals(t.type())).count();
        long attrCount = registry.getRegisteredTypes().stream()
            .filter(t -> "attr".equals(t.type())).count();
            
        System.out.println("Field types: " + fieldCount);
        System.out.println("Object types: " + objectCount);
        System.out.println("Attribute types: " + attrCount);
        
        assertTrue("Should have at least 13 field types", fieldCount >= 13);
        assertTrue("Should have at least 2 object types", objectCount >= 2);
        assertTrue("Should have at least 3 attribute types", attrCount >= 3);
    }
}