package com.draagon.meta.constraint;

import com.draagon.meta.attr.StringArrayAttribute;
import com.draagon.meta.field.StringField;
import com.draagon.meta.registry.ServiceRegistry;
import com.draagon.meta.registry.ServiceRegistryFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Test the unified constraint system using ServiceRegistry pattern.
 * 
 * This test verifies that:
 * 1. ConstraintProvider services are discovered and loaded
 * 2. Constraints are properly registered via ServiceRegistry
 * 3. StringArrayAttribute issue is resolved (classes are loaded via ServiceRegistry)
 * 4. The system works in both OSGi and non-OSGi environments
 */
public class UnifiedConstraintSystemTest {
    
    private static final Logger log = LoggerFactory.getLogger(UnifiedConstraintSystemTest.class);
    
    private ConstraintRegistry constraintRegistry;
    private ServiceRegistry serviceRegistry;
    
    @Before
    public void setUp() {
        // Use the factory to get appropriate ServiceRegistry for environment
        serviceRegistry = ServiceRegistryFactory.getDefault();
        constraintRegistry = new ConstraintRegistry(serviceRegistry);
        
        log.info("Test setup with ServiceRegistry: {}", serviceRegistry.getDescription());
    }
    
    @After
    public void tearDown() {
        // Don't close ServiceRegistry between tests as it's shared via factory
        // serviceRegistry.close();
    }
    
    @Test
    public void testBaseClassConstraintsRegistered() {
        // Verify that base class constraints are registered (v6.1.0+ pattern)
        var providers = serviceRegistry.getServices(ConstraintProvider.class);
        
        assertNotNull("ConstraintProvider services should exist", providers);
        assertTrue("Should have no constraint providers (base class pattern)", providers.isEmpty());
        
        log.info("Using base class constraint pattern - no providers needed");
        
        // Verify specific base class constraints are registered
        var allConstraints = constraintRegistry.getAllConstraints();
        
        // Check for MetaField constraints
        boolean foundFieldNaming = allConstraints.stream()
            .anyMatch(c -> "field.naming.pattern".equals(getConstraintId(c)));
        boolean foundFieldRequired = allConstraints.stream()
            .anyMatch(c -> "field.required.placement".equals(getConstraintId(c)));
            
        // Check for MetaAttribute constraints
        boolean foundAttrPlacement = allConstraints.stream()
            .anyMatch(c -> "attribute.universal.placement".equals(getConstraintId(c)));
        boolean foundAttrNaming = allConstraints.stream()
            .anyMatch(c -> "attribute.naming.pattern".equals(getConstraintId(c)));
            
        // Check for MetaObject constraints
        boolean foundObjNaming = allConstraints.stream()
            .anyMatch(c -> "object.naming.pattern".equals(getConstraintId(c)));
        boolean foundFieldsPlacement = allConstraints.stream()
            .anyMatch(c -> "object.fields.placement".equals(getConstraintId(c)));
        
        assertTrue("Should find field naming constraint", foundFieldNaming);
        assertTrue("Should find field required placement constraint", foundFieldRequired);
        assertTrue("Should find attribute placement constraint", foundAttrPlacement);
        assertTrue("Should find attribute naming constraint", foundAttrNaming);
        assertTrue("Should find object naming constraint", foundObjNaming);
        assertTrue("Should find object fields placement constraint", foundFieldsPlacement);
    }
    
    @Test
    public void testConstraintsLoaded() {
        // Verify that constraints are loaded from base classes (v6.1.0+ pattern)
        var stats = constraintRegistry.getStats();
        
        assertTrue("Should have constraints loaded", stats.totalConstraints() > 0);
        assertEquals("Should have zero constraint providers (base class pattern)", 0, stats.providerCount());
        assertTrue("Should be initialized", stats.initialized());
        
        log.info("Constraint registry stats: {}", stats);
        
        // Check for specific constraint types
        var placementConstraints = constraintRegistry.getPlacementConstraints();
        var validationConstraints = constraintRegistry.getValidationConstraints();
        
        assertFalse("Should have placement constraints", placementConstraints.isEmpty());
        assertFalse("Should have validation constraints", validationConstraints.isEmpty());
        
        log.info("Loaded {} placement constraints and {} validation constraints", 
                 placementConstraints.size(), validationConstraints.size());
    }
    
    @Test
    public void testStringFieldConstraints() {
        // Test that StringField constraints are properly loaded
        var placementConstraints = constraintRegistry.getPlacementConstraints();
        
        // Look for StringField-specific constraints
        boolean foundMaxLengthPlacement = placementConstraints.stream()
            .anyMatch(c -> getConstraintId(c).contains("stringfield.maxlength"));
        boolean foundPatternPlacement = placementConstraints.stream()
            .anyMatch(c -> getConstraintId(c).contains("stringfield.pattern"));
        
        assertTrue("Should find StringField maxLength placement constraint", foundMaxLengthPlacement);
        assertTrue("Should find StringField pattern placement constraint", foundPatternPlacement);
        
        var validationConstraints = constraintRegistry.getValidationConstraints();
        boolean foundFieldNamingValidation = validationConstraints.stream()
            .anyMatch(c -> getConstraintId(c).contains("field.naming.pattern"));
        
        assertTrue("Should find field naming validation constraint", foundFieldNamingValidation);
    }
    
    @Test
    public void testStringArrayAttributeLoadable() {
        // This is the critical test - StringArrayAttribute should be loadable
        // because the ServiceRegistry approach forces class loading
        
        try {
            // Test that StringArrayAttribute can be created
            StringArrayAttribute attr = StringArrayAttribute.create("testArray", "value1,value2");
            assertNotNull("StringArrayAttribute should be creatable", attr);
            assertEquals("Should have correct name", "testArray", attr.getName());
            
            // Test that it has proper subtype registered
            assertEquals("Should have stringarray subtype", "stringarray", attr.getSubTypeName());
            
            log.info("StringArrayAttribute created successfully: {} with values: {}", 
                     attr.getName(), attr.getValue());
            
        } catch (Exception e) {
            fail("StringArrayAttribute should be loadable via ServiceRegistry: " + e.getMessage());
        }
    }
    
    @Test
    public void testServiceRegistryEnvironmentDetection() {
        // Test that ServiceRegistry correctly detects environment
        String description = serviceRegistry.getDescription();
        assertNotNull("ServiceRegistry should have description", description);
        
        boolean isOSGI = serviceRegistry.isOSGIEnvironment();
        log.info("ServiceRegistry environment - OSGi: {}, Description: {}", isOSGI, description);
        
        // In test environment, should typically be non-OSGi
        if (!isOSGI) {
            assertTrue("Non-OSGi registry should mention ServiceLoader", 
                      description.toLowerCase().contains("serviceloader") || 
                      description.toLowerCase().contains("java"));
        }
    }
    
    @Test
    public void testConstraintReload() {
        // Test that constraint registry can be reloaded (useful for testing)
        int initialCount = constraintRegistry.getConstraintCount();
        assertTrue("Should have initial constraints", initialCount > 0);
        
        // Reload constraints
        constraintRegistry.reload();
        
        int reloadedCount = constraintRegistry.getConstraintCount();
        assertEquals("Constraint count should be same after reload", initialCount, reloadedCount);
        assertTrue("Should still be initialized after reload", constraintRegistry.isInitialized());
    }
    
    @Test
    public void testConstraintProviderPriorities() {
        // Test that providers are loaded in priority order
        var providers = serviceRegistry.getServices(ConstraintProvider.class);
        
        // Core providers should have lower priority numbers (higher priority)
        var coreFieldProvider = providers.stream()
            .filter(p -> p.getClass().getSimpleName().equals("CoreFieldConstraintProvider"))
            .findFirst();
        
        if (coreFieldProvider.isPresent()) {
            int priority = coreFieldProvider.get().getPriority();
            assertTrue("Core field provider should have high priority (low number)", priority < 1000);
            log.info("CoreFieldConstraintProvider priority: {}", priority);
        }
    }
    
    /**
     * Helper method to safely get constraint ID from either ValidationConstraint or PlacementConstraint
     */
    private String getConstraintId(Constraint constraint) {
        if (constraint instanceof ValidationConstraint) {
            return ((ValidationConstraint) constraint).getId();
        } else if (constraint instanceof PlacementConstraint) {
            return ((PlacementConstraint) constraint).getId();
        } else {
            // Fallback - use type as identifier for unknown constraint types
            return constraint.getType();
        }
    }
}