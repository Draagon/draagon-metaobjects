package com.draagon.meta.registry;

import com.draagon.meta.MetaDataTypeId;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.field.StringField;
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
        System.out.println("=== INHERITANCE DEBUG TEST ===");

        // Check base MetaField
        MetaDataTypeId baseFieldId = new MetaDataTypeId("field", "base");
        TypeDefinition baseFieldDef = registry.getTypeDefinition(baseFieldId);

        System.out.println("\n--- BASE FIELD (field.base) ---");
        if (baseFieldDef != null) {
            System.out.println("Registered: YES");
            System.out.println("Direct requirements: " + baseFieldDef.getDirectChildRequirements().size());
            for (ChildRequirement req : baseFieldDef.getDirectChildRequirements()) {
                System.out.println("  - " + req.getDescription());
            }
        } else {
            System.out.println("Registered: NO");
        }

        // Check StringField
        MetaDataTypeId stringFieldId = new MetaDataTypeId("field", "string");
        TypeDefinition stringFieldDef = registry.getTypeDefinition(stringFieldId);

        System.out.println("\n--- STRING FIELD (field.string) ---");
        if (stringFieldDef != null) {
            System.out.println("Registered: YES");
            System.out.println("Has parent: " + stringFieldDef.hasParent());
            System.out.println("Parent: " + stringFieldDef.getParentQualifiedName());
            System.out.println("Direct requirements: " + stringFieldDef.getDirectChildRequirements().size());
            System.out.println("Inherited requirements: " + stringFieldDef.getInheritedChildRequirements().size());
            System.out.println("Total requirements: " + stringFieldDef.getChildRequirements().size());

            System.out.println("\nDirect requirements:");
            for (ChildRequirement req : stringFieldDef.getDirectChildRequirements()) {
                System.out.println("  - " + req.getDescription());
            }

            System.out.println("\nInherited requirements:");
            for (ChildRequirement req : stringFieldDef.getInheritedChildRequirements().values()) {
                System.out.println("  - " + req.getDescription());
            }

            // Test specific child acceptance
            System.out.println("\n--- CHILD ACCEPTANCE TESTS ---");
            System.out.println("Accepts required (attr,boolean): " +
                             stringFieldDef.acceptsChild("attr", "boolean", "required"));
            System.out.println("Accepts validator (validator,required): " +
                             stringFieldDef.acceptsChild("validator", "required", "myValidator"));
            System.out.println("Accepts any validator (validator,*): " +
                             stringFieldDef.acceptsChild("validator", "any", "anyValidator"));
        } else {
            System.out.println("Registered: NO");
        }
    }
}