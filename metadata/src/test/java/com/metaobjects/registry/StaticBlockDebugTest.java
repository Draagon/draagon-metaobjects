package com.metaobjects.registry;

import com.metaobjects.field.StringField;
import com.metaobjects.field.IntegerField;
import com.metaobjects.attr.StringAttribute;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Debug test to understand why static blocks aren't executing.
 *
 * ⚠️ ISOLATED TEST: This test manipulates the shared MetaDataRegistry directly
 * by clearing and restoring it. It must run in isolation from other tests
 * to prevent registry conflicts.
 *
 * @since 6.0.0
 */
@IsolatedTest("Clears and restores shared MetaDataRegistry state")
public class StaticBlockDebugTest extends SharedRegistryTestBase {

    private static final Logger log = LoggerFactory.getLogger(StaticBlockDebugTest.class);

    private Map<String, TypeDefinition> backupRegistry;

    @Before
    public void setUp() {
        // Use the shared registry but back up its state for isolation
        MetaDataRegistry registry = getSharedRegistry();

        // Disable strict duplicate detection for isolated testing
        registry.disableStrictDuplicateDetection();

        // Backup existing registrations before this test runs
        backupRegistry = new HashMap<>();
        for (String typeName : registry.getRegisteredTypeNames()) {
            String[] parts = typeName.split("\\.");
            if (parts.length == 2) {
                TypeDefinition def = registry.getTypeDefinition(parts[0], parts[1]);
                if (def != null) {
                    backupRegistry.put(typeName, def);
                }
            }
        }
        log.info("Set up isolated registry test with backup of {} types", backupRegistry.size());
    }

    @After
    public void tearDown() {
        MetaDataRegistry registry = getSharedRegistry();
        if (registry != null) {
            registry.clear();
            // Restore original registrations
            restoreRegistryFromBackup();
            // Re-enable strict duplicate detection
            registry.enableStrictDuplicateDetection();
        }
        log.info("Tore down isolated registry test with registry restored");
    }
    
    /**
     * Test direct static block execution
     */
    @Test
    public void testDirectStaticBlockExecution() {
        log.info("Testing direct static block execution...");

        // Clear registry
        MetaDataRegistry registry = getSharedRegistry();
        registry.clear();
        
        // Force class loading and static block execution
        try {
            log.info("Force loading StringField class...");
            Class<?> stringFieldClass = Class.forName("com.metaobjects.field.StringField", true, 
                                                     Thread.currentThread().getContextClassLoader());
            log.info("StringField class loaded: {}", stringFieldClass.getName());
            
            // Check if registration happened
            TypeDefinition def = registry.getTypeDefinition("field", "string");
            if (def != null) {
                log.info("SUCCESS: Static registration worked! {}", def);
            } else {
                log.warn("Static registration did not work - no type definition found");
            }
            
        } catch (Exception e) {
            log.error("Exception during class loading", e);
            fail("Should not have exception during class loading: " + e.getMessage());
        }
    }
    
    /**
     * Test manual execution of what the static block should do
     */
    @Test
    public void testManualStaticBlockEquivalent() {
        log.info("Testing manual execution of static block equivalent...");

        MetaDataRegistry registry = getSharedRegistry();
        registry.clear();
        
        try {
            // Manually execute what the StringField static block should do
            log.info("Manually registering StringField...");
            registry.registerType(StringField.class, def -> def
                .type("field").subType("string")
                .description("String field with pattern validation")
                .optionalAttribute("pattern", "string")
                .optionalAttribute("maxLength", "int")
                .optionalAttribute("minLength", "int")
            );
            
            // Verify it worked
            TypeDefinition def = registry.getTypeDefinition("field", "string");
            assertNotNull("Manual registration should work", def);
            assertEquals("Implementation class should match", StringField.class, def.getImplementationClass());
            
            log.info("Manual registration successful: {}", def);
            
        } catch (Exception e) {
            log.error("Exception during manual registration", e);
            fail("Manual registration should not fail: " + e.getMessage());
        }
    }
    
    /**
     * Test if static block executes when creating instances
     */
    @Test
    public void testStaticBlockOnInstanceCreation() {
        log.info("Testing static block execution on instance creation...");

        MetaDataRegistry registry = getSharedRegistry();
        registry.clear();
        
        log.info("Registry cleared. Current types: {}", registry.getRegisteredTypeNames().size());
        
        // Create instance - this should definitely trigger static block
        log.info("Creating StringField instance...");
        StringField field = new StringField("testField");
        log.info("StringField instance created: {}", field.getName());
        
        // Check registry
        TypeDefinition def = registry.getTypeDefinition("field", "string");
        if (def != null) {
            log.info("SUCCESS: Instance creation triggered static registration: {}", def);
        } else {
            log.warn("Instance creation did NOT trigger static registration");
            log.info("Current registered types: {}", registry.getRegisteredTypeNames());
        }
    }
    
    /**
     * Test if there are any initialization order issues
     */
    @Test
    public void testInitializationOrder() {
        log.info("Testing initialization order issues...");

        try {
            // Create registry instance first
            MetaDataRegistry registry = getSharedRegistry();
            registry.clear();
            log.info("Registry created and cleared");
            
            // Now create field instance
            StringField field = new StringField("testField");
            log.info("Field created: {}", field);
            
            // Check if registration happened
            TypeDefinition def = registry.getTypeDefinition("field", "string");
            if (def != null) {
                log.info("Registration worked with explicit initialization order");
            } else {
                log.warn("Registration failed even with explicit initialization order");
                
                // Try to understand what's in the registry
                log.info("Registry stats: {}", registry.getStats());
                log.info("All registered types: {}", registry.getRegisteredTypeNames());
            }
            
        } catch (Exception e) {
            log.error("Exception during initialization order test", e);
            throw e;
        }
    }
    
    /**
     * Test if multiple classes can register successfully
     */
    @Test
    public void testMultipleClassRegistration() {
        log.info("Testing multiple class registration...");

        MetaDataRegistry registry = getSharedRegistry();
        registry.clear();
        
        // Manually register multiple types to ensure the registry itself works
        try {
            log.info("Manually registering multiple types...");
            
            // Register StringField
            registry.registerType(StringField.class, def -> def
                .type("field").subType("string")
                .description("String field")
                .optionalAttribute("pattern", "string")
            );

            // Register IntegerField
            registry.registerType(IntegerField.class, def -> def
                .type("field").subType("int")
                .description("Integer field")
                .optionalAttribute("minValue", "int")
            );

            // Register StringAttribute
            registry.registerType(StringAttribute.class, def -> def
                .type("attr").subType("string")
                .description("String attribute")
            );
            
            // Verify all registrations
            assertNotNull("StringField should be registered", 
                         registry.getTypeDefinition("field", "string"));
            assertNotNull("IntegerField should be registered", 
                         registry.getTypeDefinition("field", "int"));
            assertNotNull("StringAttribute should be registered", 
                         registry.getTypeDefinition("attr", "string"));
            
            log.info("All manual registrations successful. Registry contains {} types", 
                    registry.getRegisteredTypeNames().size());
            
            // Test child acceptance
            boolean accepts = registry.acceptsChild("field", "string", "attr", "string", "pattern");
            assertTrue("StringField should accept pattern attribute", accepts);
            
            log.info("Child acceptance test passed");
            
        } catch (Exception e) {
            log.error("Exception during multiple class registration", e);
            fail("Multiple class registration should work: " + e.getMessage());
        }
    }
    
    /**
     * Restore registry from backup taken during setUp
     */
    private void restoreRegistryFromBackup() {
        try {
            MetaDataRegistry registry = getSharedRegistry();
            for (Map.Entry<String, TypeDefinition> entry : backupRegistry.entrySet()) {
                String typeName = entry.getKey();
                TypeDefinition def = entry.getValue();
                String[] parts = typeName.split("\\.");
                if (parts.length == 2) {
                    // Re-register the backed up definition
                    registry.register(def);
                }
            }
            log.info("Restored {} type definitions from backup", backupRegistry.size());
        } catch (Exception e) {
            log.error("Failed to restore registry from backup", e);
            // Fallback to triggering static registrations
            triggerStaticRegistrationsAsFallback();
        }
    }
    
    /**
     * Fallback method to trigger static registrations if backup restore fails
     */
    private void triggerStaticRegistrationsAsFallback() {
        try {
            new StringField("testString");
            new IntegerField("testInt");
            new StringAttribute("testStringAttr");
        } catch (Exception e) {
            log.error("Failed to restore registry via static registrations", e);
        }
    }
}