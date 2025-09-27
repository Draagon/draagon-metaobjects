package com.metaobjects.registry;

import com.metaobjects.MetaDataTypeId;
import com.metaobjects.field.MetaField;
import com.metaobjects.field.StringField;
import static com.metaobjects.MetaData.ATTR_IS_ABSTRACT;
import static com.metaobjects.field.MetaField.ATTR_REQUIRED;
import static com.metaobjects.field.MetaField.ATTR_DEFAULT_VALUE;
import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.*;

/**
 * Test inheritance functionality in TypeDefinition system.
 * Demonstrates that StringField inherits attributes from base MetaField.
 */
public class TypeDefinitionInheritanceTest {

    private MetaDataRegistry registry;

    @Before
    public void setUp() {
        // Use the singleton registry that has the static registrations
        registry = MetaDataRegistry.getInstance();

        // Trigger class loading to ensure static blocks execute
        // This forces the static registration blocks to run
        try {
            Class.forName(MetaField.class.getName());
            Class.forName(StringField.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load required classes", e);
        }
    }

    @Test
    public void testStringFieldInheritsFromBaseField() {
        // Get the StringField type definition
        MetaDataTypeId stringFieldId = new MetaDataTypeId("field", "string");
        TypeDefinition stringFieldDef = registry.getTypeDefinition(stringFieldId);

        assertNotNull("StringField type definition should be registered", stringFieldDef);
        assertTrue("StringField should have a parent type", stringFieldDef.hasParent());
        assertEquals("StringField parent type should be 'field'", "field", stringFieldDef.getParentType());
        assertEquals("StringField parent subType should be 'base'", "base", stringFieldDef.getParentSubType());

        // Test that StringField accepts inherited attributes from MetaField base
        assertTrue("StringField should accept 'required' attribute (inherited from MetaField)",
                  stringFieldDef.acceptsChild("attr", "boolean", ATTR_REQUIRED));

        assertTrue("StringField should accept 'isAbstract' attribute (inherited from MetaField)",
                  stringFieldDef.acceptsChild("attr", "boolean", ATTR_IS_ABSTRACT));

        assertTrue("StringField should accept 'defaultValue' attribute (inherited from MetaField)",
                  stringFieldDef.acceptsChild("attr", "string", ATTR_DEFAULT_VALUE));

        // Test that StringField accepts its own specific attributes
        assertTrue("StringField should accept 'maxLength' attribute (StringField-specific)",
                  stringFieldDef.acceptsChild("attr", "int", StringField.ATTR_MAX_LENGTH));

        assertTrue("StringField should accept 'pattern' attribute (StringField-specific)",
                  stringFieldDef.acceptsChild("attr", "string", StringField.ATTR_PATTERN));

        // Test that StringField accepts inherited child types from MetaField base
        assertTrue("StringField should accept validator children (inherited from MetaField)",
                  stringFieldDef.acceptsChild("validator", "required", "myValidator"));

        assertTrue("StringField should accept view children (inherited from MetaField)",
                  stringFieldDef.acceptsChild("view", "form", "myView"));
    }

    @Test
    public void testBaseFieldHasNoParent() {
        // Get the base MetaField type definition
        MetaDataTypeId baseFieldId = new MetaDataTypeId("field", "base");
        TypeDefinition baseFieldDef = registry.getTypeDefinition(baseFieldId);

        assertNotNull("Base MetaField type definition should be registered", baseFieldDef);
        assertFalse("Base MetaField should have no parent", baseFieldDef.hasParent());
        assertNull("Base MetaField parent type should be null", baseFieldDef.getParentType());
        assertNull("Base MetaField parent subType should be null", baseFieldDef.getParentSubType());
    }

    @Test
    public void testInheritanceChainLogging() {
        // This test verifies that inheritance resolution logging works correctly
        // The StringField registration should show inheritance from field.base

        MetaDataTypeId stringFieldId = new MetaDataTypeId("field", "string");
        TypeDefinition stringFieldDef = registry.getTypeDefinition(stringFieldId);

        assertNotNull("StringField should be registered with inheritance", stringFieldDef);

        // Verify the inheritance chain is set up correctly
        assertEquals("Inheritance chain should be complete", "field.base", stringFieldDef.getParentQualifiedName());

        // Count total requirements (direct + inherited)
        int totalRequirements = stringFieldDef.getChildRequirements().size();
        int directRequirements = stringFieldDef.getDirectChildRequirements().size();
        int inheritedRequirements = stringFieldDef.getInheritedChildRequirements().size();

        assertEquals("Total requirements should equal direct + inherited",
                    totalRequirements, directRequirements + inheritedRequirements);

        assertTrue("Should have some inherited requirements from MetaField base",
                  inheritedRequirements > 0);

        System.out.println("StringField inheritance test results:");
        System.out.println("  - Total requirements: " + totalRequirements);
        System.out.println("  - Direct requirements: " + directRequirements);
        System.out.println("  - Inherited requirements: " + inheritedRequirements);
        System.out.println("  - Parent: " + stringFieldDef.getParentQualifiedName());
    }
}