package com.draagon.meta.registry;

import com.draagon.meta.MetaData;
import com.draagon.meta.attr.StringAttribute;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Simple test to verify that attr types are registered correctly
 */
public class AttrTypeTest {
    
    @Test
    public void testAttrTypeRegistration() {
        // Create a simple registry to test
        MetaDataTypeRegistry registry = new MetaDataTypeRegistry();
        
        // Test if "attr" type is recognized
        boolean hasAttrType = registry.hasType("attr");
        System.out.println("Registry hasType('attr'): " + hasAttrType);
        
        // Try to create a StringAttribute
        if (hasAttrType) {
            try {
                MetaData attr = registry.createInstance("attr", "string", "testAttr");
                System.out.println("Created attr instance: " + attr.getClass().getSimpleName());
                assertTrue("Should be StringAttribute", attr instanceof StringAttribute);
            } catch (Exception e) {
                System.out.println("Failed to create attr instance: " + e.getMessage());
                fail("Should be able to create attr instance: " + e.getMessage());
            }
        } else {
            fail("Registry should recognize 'attr' type");
        }
    }
}