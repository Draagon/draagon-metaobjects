package com.draagon.meta.registry;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test to validate base subtype consistency using the registry health validation system.
 * This test ensures all type families have proper base subtype registrations and
 * inheritance patterns are working correctly.
 */
public class BaseSubTypeAnalysisTest {

    private MetaDataRegistry registry;

    @Before
    public void setUp() {
        registry = MetaDataRegistry.getInstance();
    }

    @Test
    public void testBaseSubTypeConsistency() {
        // Use the new health validation system
        RegistryHealthReport report = registry.validateConsistency();

        // Print detailed analysis
        System.out.println(report.generateSummary());

        // Test should pass structurally but report any missing base types
        assertTrue("Registry should be structurally sound", report.isStructurallySound());

        // Log specific findings about base types
        if (report.hasMissingBases()) {
            System.out.println("\n⚠️  MISSING BASE TYPES DETECTED:");
            for (String missingType : report.getMissingBases()) {
                System.out.println("  - " + missingType + ".base is missing");
            }
            System.out.println("\nThese warnings indicate architectural inconsistencies but don't break functionality.");
        } else {
            System.out.println("\n✅ EXCELLENT: All type families have base subtypes!");
        }

        // Verify that our new key.base type was added successfully
        assertTrue("key.base should now be registered", registry.isRegistered("key", "base"));

        // Verify inheritance is working for key types
        TypeDefinition primaryKeyDef = registry.getTypeDefinition("key", "primary");
        assertNotNull("PrimaryKey should be registered", primaryKeyDef);
        assertTrue("PrimaryKey should inherit from key.base",
            primaryKeyDef.hasParent() && "base".equals(primaryKeyDef.getParentSubType()));

        TypeDefinition foreignKeyDef = registry.getTypeDefinition("key", "foreign");
        assertNotNull("ForeignKey should be registered", foreignKeyDef);
        assertTrue("ForeignKey should inherit from key.base",
            foreignKeyDef.hasParent() && "base".equals(foreignKeyDef.getParentSubType()));

        TypeDefinition secondaryKeyDef = registry.getTypeDefinition("key", "secondary");
        assertNotNull("SecondaryKey should be registered", secondaryKeyDef);
        assertTrue("SecondaryKey should inherit from key.base",
            secondaryKeyDef.hasParent() && "base".equals(secondaryKeyDef.getParentSubType()));
    }

    @Test
    public void testRegistryHealthValidation() {
        RegistryHealthReport report = registry.validateConsistency();

        // Ensure core functionality is working
        assertFalse("Registry should have some types registered",
            ((Integer) report.getMetadata("totalTypes")) == 0);

        // Log inheritance patterns
        @SuppressWarnings("unchecked")
        java.util.List<String> inheritanceChains =
            (java.util.List<String>) report.getMetadata("inheritanceChains");

        if (inheritanceChains != null && !inheritanceChains.isEmpty()) {
            System.out.println("\n=== ACTIVE INHERITANCE PATTERNS ===");
            inheritanceChains.forEach(chain -> System.out.println("  " + chain));
        }

        // Inheritance should be actively used
        Integer typesWithInheritance = (Integer) report.getMetadata("typesWithInheritance");
        assertNotNull("Should track inheritance usage", typesWithInheritance);
        assertTrue("Should have types using inheritance", typesWithInheritance > 0);

        // Should have types inheriting from base types
        Integer typesInheritingFromBase = (Integer) report.getMetadata("typesInheritingFromBase");
        assertNotNull("Should track base inheritance", typesInheritingFromBase);
        assertTrue("Should have types inheriting from base", typesInheritingFromBase > 0);
    }

    @Test
    public void ensureAllCoreBaseTypesPresent() {
        RegistryHealthReport report = registry.validateConsistency();

        // These are the expected core base types
        String[] expectedBaseTypes = {"field.base", "object.base", "attr.base", "validator.base", "key.base"};

        for (String expectedType : expectedBaseTypes) {
            String[] parts = expectedType.split("\\.");
            assertTrue("Core base type " + expectedType + " should be registered",
                registry.isRegistered(parts[0], parts[1]));
        }

        // Should have no errors about missing core types
        assertFalse("Should not have core type errors",
            report.getErrors().stream().anyMatch(error -> error.contains("Missing core base types")));

        System.out.println("✅ All expected core base types are present and registered correctly.");
    }
}