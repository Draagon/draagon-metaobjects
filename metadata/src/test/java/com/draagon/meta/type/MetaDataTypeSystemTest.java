package com.draagon.meta.type;

import com.draagon.meta.MetaData;
import com.draagon.meta.ValidationResult;
import com.draagon.meta.attr.MetaAttribute;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Test the new type system functionality
 */
public class MetaDataTypeSystemTest {

    private MetaDataTypeRegistry registry;
    
    @Before
    public void setUp() {
        // Create a fresh registry for testing
        registry = new MetaDataTypeRegistry();
        
        // Initialize with migration utils
        MetaDataMigrationUtils.initializeLegacyTypes();
    }
    
    @Test
    public void testTypeDefinitionCreation() {
        // Create a type definition
        MetaDataTypeDefinition definition = MetaDataTypeDefinition.builder("test", MetaData.class)
            .description("Test type")
            .allowedSubTypes("sub1", "sub2")
            .allowsChildren(true)
            .build();
            
        assertEquals("test", definition.typeName());
        assertEquals("Test type", definition.description());
        assertEquals(MetaData.class, definition.implementationClass());
        assertTrue(definition.isSubTypeAllowed("sub1"));
        assertTrue(definition.isSubTypeAllowed("sub2"));
        assertFalse(definition.isSubTypeAllowed("sub3"));
        assertTrue(definition.allowsChildren());
    }
    
    @Test
    public void testTypeRegistryBasics() {
        // Register a type
        MetaDataTypeDefinition definition = MetaDataTypeDefinition.builder("test", MetaData.class)
            .description("Test type")
            .build();
            
        registry.registerType(definition);
        
        // Verify registration
        assertTrue(registry.hasType("test"));
        Optional<MetaDataTypeDefinition> retrieved = registry.getType("test");
        assertTrue(retrieved.isPresent());
        assertEquals("test", retrieved.get().typeName());
        
        // Test requireType
        MetaDataTypeDefinition required = registry.requireType("test");
        assertNotNull(required);
        assertEquals("test", required.typeName());
    }
    
    @Test
    public void testMetaDataEnhancedAPIs() {
        // Create a MetaData instance
        MetaData md = new MetaData("attr", "string", "test");
        
        // Test enhanced validation
        ValidationResult result = md.validateEnhanced();
        assertNotNull(result);
        
        // Test Stream APIs
        assertNotNull(md.getChildrenStream());
        
        // Test Optional-based child finding
        Optional<MetaData> child = md.findChild("nonexistent");
        assertFalse(child.isPresent());
        
        // Test modern caching
        md.setModernCacheValue("key1", "value1");
        Optional<String> cachedValue = md.getCacheValue("key1", String.class);
        assertTrue(cachedValue.isPresent());
        assertEquals("value1", cachedValue.get());
    }
    
    @Test
    public void testCoreMetaDataTypes() {
        // Test that core types can be identified
        assertTrue(CoreMetaDataTypes.isCoreType("attr"));
        assertTrue(CoreMetaDataTypes.isCoreType("field"));
        assertTrue(CoreMetaDataTypes.isCoreType("object"));
        assertFalse(CoreMetaDataTypes.isCoreType("custom"));
        
        // Test enum lookup
        CoreMetaDataTypes attrType = CoreMetaDataTypes.fromTypeName("attr");
        assertEquals("attr", attrType.getTypeName());
        assertEquals("Metadata attribute", attrType.getDescription());
    }
    
    @Test
    public void testValidationFramework() {
        // Test ValidationResult builder
        ValidationResult result = ValidationResult.builder()
            .addError("Error 1")
            .addError("Error 2")
            .addChildError("child1", "Child error")
            .build();
            
        assertFalse(result.isValid());
        assertEquals(2, result.getErrors().size());
        assertTrue(result.hasChildErrors());
        assertEquals(3, result.getAllErrors().size());
    }
    
    @Test
    public void testMigrationUtils() {
        // Test legacy type detection
        assertTrue(MetaDataMigrationUtils.isLegacyType("attr"));
        assertTrue(MetaDataMigrationUtils.isLegacyType("field"));
        assertFalse(MetaDataMigrationUtils.isLegacyType("custom"));
        
        // Test legacy mappings
        String attrClass = MetaDataMigrationUtils.getModernImplementationClass("attr");
        assertEquals("com.draagon.meta.attr.MetaAttribute", attrClass);
    }
}