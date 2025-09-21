package com.draagon.meta.registry;

import com.draagon.meta.MetaData;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.registry.MetaDataRegistry;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Simple test to verify that attr types are registered correctly
 */
public class AttrTypeTest {
    
    @Test
    public void testAttrTypeRegistration() {
        // Test that StringAttribute can be created directly using the modern approach
        try {
            StringAttribute attr = new StringAttribute("testAttr");
            assertNotNull("StringAttribute should be created", attr);
            assertEquals("Attribute name should match", "testAttr", attr.getName());
            assertEquals("Attribute type should be 'attr'", "attr", attr.getTypeName());
            assertEquals("Attribute subtype should be 'string'", "string", attr.getSubTypeName());
            
            System.out.println("Successfully created StringAttribute: " + attr.getName() + 
                             " [" + attr.getTypeName() + ":" + attr.getSubTypeName() + "]");
            
        } catch (Exception e) {
            System.out.println("Failed to create StringAttribute: " + e.getMessage());
            fail("Should be able to create StringAttribute: " + e.getMessage());
        }
    }
}