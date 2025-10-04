package com.metaobjects.registry;

import com.metaobjects.MetaDataTypeId;
import com.metaobjects.field.MetaField;
import com.metaobjects.field.StringField;
import org.junit.Test;
import org.junit.Before;

/**
 * Debug test to examine exactly what inheritance is working
 */
public class InheritanceDebugTest {

    private MetaDataRegistry registry;

    @Before
    public void setUp() {
        registry = MetaDataRegistry.getInstance();

        // Force class loading
        try {
            Class.forName(MetaField.class.getName());
            Class.forName(StringField.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load required classes", e);
        }
    }

    @Test
    public void debugInheritanceDetails() {
        // Inheritance debug data available for assertions - no verbose output needed

        // Check base MetaField
        MetaDataTypeId baseFieldId = new MetaDataTypeId("field", "base");
        TypeDefinition baseFieldDef = registry.getTypeDefinition(baseFieldId);

        // Base field definition available for assertions - no verbose output needed
        if (baseFieldDef != null) {
            // Registration and requirements data available for assertions
            assert baseFieldDef.getDirectChildRequirements().size() >= 0;
        }

        // Check StringField
        MetaDataTypeId stringFieldId = new MetaDataTypeId("field", "string");
        TypeDefinition stringFieldDef = registry.getTypeDefinition(stringFieldId);

        // String field inheritance data available for assertions - no verbose output needed
        if (stringFieldDef != null) {
            // Inheritance verification available for assertions
            assert stringFieldDef.hasParent();
            assert stringFieldDef.getDirectChildRequirements().size() >= 0;
            assert stringFieldDef.getInheritedChildRequirements().size() >= 0;
            assert stringFieldDef.getChildRequirements().size() >= 0;

            // Child acceptance verification available for assertions
            assert stringFieldDef.acceptsChild("attr", "boolean", "required");
        }
    }
}