package com.draagon.meta.field;

import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.object.pojo.PojoMetaObject;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.attr.BooleanAttribute;
import com.draagon.meta.validator.RequiredValidator;
import com.draagon.meta.field.StringField;
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.loader.MetaDataLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Comprehensive test for unified field type registry and metadata loading.
 * Tests that all field types properly self-register and can be loaded from metadata files.
 */
public class UnifiedFieldRegistryTest {

    private MetaDataRegistry registry;
    private Path tempDir;

    @Before
    public void setUp() throws IOException {
        // Create temp directory for test files
        tempDir = Files.createTempDirectory("unified-field-registry-test");
        
        // Get the unified registry instance
        registry = MetaDataRegistry.getInstance();
        
        // Ensure static registrations are loaded by creating instances
        triggerStaticRegistrations();
    }

    @After
    public void tearDown() throws IOException {
        // Clean up temp directory
        if (tempDir != null) {
            deleteDirectory(tempDir.toFile());
        }
    }

    /**
     * Trigger static registrations by creating instances of registered field types
     */
    private void triggerStaticRegistrations() {
        try {
            // Create instances to ensure static blocks execute
            new StringField("testString");           // Already registered
            new IntegerField("testInt");             // Already registered
            new LongField("testLong");               // New registration
            new DoubleField("testDouble");           // New registration
            new BooleanField("testBoolean");         // New registration
            new DateField("testDate");               // New registration
            new PojoMetaObject("testObject");        // Object type
            new StringAttribute("testStringAttr");   // Attribute type
            new IntAttribute("testIntAttr");         // Attribute type
            new BooleanAttribute("testBoolAttr");    // Attribute type
            new com.draagon.meta.attr.LongAttribute("testLongAttr");     // Long attribute type
            new com.draagon.meta.attr.DoubleAttribute("testDoubleAttr"); // Double attribute type
        } catch (Exception e) {
            // Ignore - just triggering static registrations
        }
    }

    @Test
    public void testFieldTypeRegistrations() {
        // Verify all expected field types are registered
        Set<String> registeredTypes = registry.getRegisteredTypeNames();
        // Extract base types from qualified names (e.g., "field.string" -> "field")
        Set<String> baseTypes = registeredTypes.stream()
            .map(name -> name.split("\\.")[0])
            .collect(java.util.stream.Collectors.toSet());
        
        assertTrue("Should have field types registered", baseTypes.contains("field"));
        assertTrue("Should have object types registered", baseTypes.contains("object"));
        assertTrue("Should have attr types registered", baseTypes.contains("attr"));
        
        MetaDataRegistry.RegistryStats stats = registry.getStats();
        assertNotNull("Registry stats should be available", stats);
        assertTrue("Should have multiple registered types", stats.totalTypes() >= 8);
    }

    @Test
    public void testStringFieldRegistration() {
        // Test StringField registration and child requirements
        assertTrue("StringField should accept pattern attribute",
                  registry.acceptsChild("field", "string", "attr", "string", "pattern"));
        assertTrue("StringField should accept maxLength attribute", 
                  registry.acceptsChild("field", "string", "attr", "int", "maxLength"));
        assertTrue("StringField should accept minLength attribute",
                  registry.acceptsChild("field", "string", "attr", "int", "minLength"));
        
        String description = registry.getSupportedChildrenDescription("field", "string");
        assertNotNull("Should have supported children description", description);
        assertTrue("Description should mention pattern", description.toLowerCase().contains("pattern"));
        assertTrue("Description should mention maxLength", description.toLowerCase().contains("maxlength"));
    }

    @Test
    public void testIntegerFieldRegistration() {
        // Test IntegerField registration and child requirements
        assertTrue("IntegerField should accept minValue attribute",
                  registry.acceptsChild("field", "int", "attr", "int", "minValue"));
        assertTrue("IntegerField should accept maxValue attribute",
                  registry.acceptsChild("field", "int", "attr", "int", "maxValue"));
        
        String description = registry.getSupportedChildrenDescription("field", "int");
        assertNotNull("Should have supported children description", description);
        assertTrue("Description should mention minValue", description.toLowerCase().contains("minvalue"));
        assertTrue("Description should mention maxValue", description.toLowerCase().contains("maxvalue"));
    }

    @Test
    public void testLongFieldRegistration() {
        // Test LongField registration and child requirements
        assertTrue("LongField should accept minValue attribute",
                  registry.acceptsChild("field", "long", "attr", "long", "minValue"));
        assertTrue("LongField should accept maxValue attribute",
                  registry.acceptsChild("field", "long", "attr", "long", "maxValue"));
        
        String description = registry.getSupportedChildrenDescription("field", "long");
        assertNotNull("Should have supported children description", description);
        assertTrue("Description should mention numeric validation", description.toLowerCase().contains("numeric"));
    }

    @Test 
    public void testDoubleFieldRegistration() {
        // Test DoubleField registration and child requirements
        assertTrue("DoubleField should accept minValue attribute",
                  registry.acceptsChild("field", "double", "attr", "double", "minValue"));
        assertTrue("DoubleField should accept maxValue attribute",
                  registry.acceptsChild("field", "double", "attr", "double", "maxValue"));
        assertTrue("DoubleField should accept precision attribute",
                  registry.acceptsChild("field", "double", "attr", "int", "precision"));
        
        String description = registry.getSupportedChildrenDescription("field", "double");
        assertNotNull("Should have supported children description", description);
        assertTrue("Description should mention precision", description.toLowerCase().contains("precision"));
    }

    @Test
    public void testBooleanFieldRegistration() {
        // Test BooleanField registration
        assertTrue("BooleanField should be registered",
                  registry.isRegistered("field", "boolean"));
        
        String description = registry.getSupportedChildrenDescription("field", "boolean");
        assertNotNull("Should have supported children description", description);
        assertTrue("Description should mention true/false", description.toLowerCase().contains("true"));
    }

    @Test
    public void testDateFieldRegistration() {
        // Test DateField registration and child requirements
        assertTrue("DateField should accept dateFormat attribute",
                  registry.acceptsChild("field", "date", "attr", "string", "dateFormat"));
        assertTrue("DateField should accept minDate attribute",
                  registry.acceptsChild("field", "date", "attr", "string", "minDate"));
        assertTrue("DateField should accept maxDate attribute",
                  registry.acceptsChild("field", "date", "attr", "string", "maxDate"));
        
        String description = registry.getSupportedChildrenDescription("field", "date");
        assertNotNull("Should have supported children description", description);
        assertTrue("Description should mention format", description.toLowerCase().contains("format"));
    }

    @Test
    public void testMetadataFileLoadingWithAllFieldTypes() throws Exception {
        // Create metadata file with all the field types we've registered
        Path metadataFile = tempDir.resolve("all-field-types-metadata.json");
        createAllFieldTypesMetadata(metadataFile);
        
        // Load the metadata using SimpleLoader
        MetaDataLoader loader = MetaDataLoader.createManual(false, "all-field-types-test")
                .init()
                .register()
                .getLoader();
        
        try {
            SimpleLoader simpleLoader = new SimpleLoader("all-field-types");
            simpleLoader.setSourceURIs(java.util.Arrays.asList(metadataFile.toUri()));
            simpleLoader.init();
            
            // Debug: Print what children are actually loaded
            System.out.println("SimpleLoader children: " + simpleLoader.getChildren().size());
            for (com.draagon.meta.MetaData child : simpleLoader.getChildren()) {
                System.out.println("  Child: " + child.getName() + " (" + child.getClass().getSimpleName() + ")");
            }
            
            // Verify the metadata loaded successfully  
            // Try both simple name and fully qualified name
            com.draagon.meta.object.MetaObject testObject = null;
            try {
                testObject = simpleLoader.getMetaObjectByName("AllFieldTypesTest");
            } catch (Exception e) {
                // Try fully qualified name
                testObject = simpleLoader.getMetaObjectByName("test::alltypes::AllFieldTypesTest");
            }
            assertNotNull("Test object should be loaded", testObject);
            
            // Verify each field type loaded correctly
            MetaField stringField = testObject.getMetaField("testString");
            assertNotNull("String field should be loaded", stringField);
            assertTrue("String field should be StringField", stringField instanceof StringField);
            
            MetaField intField = testObject.getMetaField("testInt");
            assertNotNull("Int field should be loaded", intField);
            assertTrue("Int field should be IntegerField", intField instanceof IntegerField);
            
            MetaField longField = testObject.getMetaField("testLong");
            assertNotNull("Long field should be loaded", longField);
            assertTrue("Long field should be LongField", longField instanceof LongField);
            
            MetaField doubleField = testObject.getMetaField("testDouble");
            assertNotNull("Double field should be loaded", doubleField);
            assertTrue("Double field should be DoubleField", doubleField instanceof DoubleField);
            
            MetaField booleanField = testObject.getMetaField("testBoolean");
            assertNotNull("Boolean field should be loaded", booleanField);
            assertTrue("Boolean field should be BooleanField", booleanField instanceof BooleanField);
            
            MetaField dateField = testObject.getMetaField("testDate");
            assertNotNull("Date field should be loaded", dateField);
            assertTrue("Date field should be DateField", dateField instanceof DateField);
            
        } finally {
            loader.destroy();
        }
    }

    @Test
    public void testFieldWithAttributesLoading() throws Exception {
        // Create metadata file with fields that have attributes
        Path metadataFile = tempDir.resolve("fields-with-attributes-metadata.json");
        createFieldsWithAttributesMetadata(metadataFile);
        
        MetaDataLoader loader = MetaDataLoader.createManual(false, "fields-with-attributes-test")
                .init()
                .register()
                .getLoader();
        
        try {
            SimpleLoader simpleLoader = new SimpleLoader("fields-with-attributes");
            simpleLoader.setSourceURIs(java.util.Arrays.asList(metadataFile.toUri()));
            simpleLoader.init();
            
            // Try both simple name and fully qualified name
            com.draagon.meta.object.MetaObject testObject = null;
            try {
                testObject = simpleLoader.getMetaObjectByName("FieldsWithAttributesTest");
            } catch (Exception e) {
                // Try fully qualified name (package is test::withattributes)
                testObject = simpleLoader.getMetaObjectByName("test::withattributes::FieldsWithAttributesTest");
            }
            assertNotNull("Test object should be loaded", testObject);
            
            // Test string field with attributes
            StringField emailField = (StringField) testObject.getMetaField("email");
            assertNotNull("Email field should be loaded", emailField);
            assertTrue("Email field should have maxLength attribute", 
                      emailField.hasMetaAttr("maxLength"));
            assertTrue("Email field should have pattern attribute", 
                      emailField.hasMetaAttr("pattern"));
            
            // Test double field with attributes  
            DoubleField priceField = (DoubleField) testObject.getMetaField("price");
            assertNotNull("Price field should be loaded", priceField);
            assertTrue("Price field should have precision attribute",
                      priceField.hasMetaAttr("precision"));
            
        } finally {
            loader.destroy();
        }
    }

    @Test
    public void testConstraintEnforcementInLoading() throws Exception {
        // Test that constraint enforcement works during metadata loading
        Path metadataFile = tempDir.resolve("constraint-test-metadata.json");
        createConstraintTestMetadata(metadataFile);
        
        MetaDataLoader loader = MetaDataLoader.createManual(false, "constraint-test")
                .init()
                .register()
                .getLoader();
        
        try {
            SimpleLoader simpleLoader = new SimpleLoader("constraint-test");
            simpleLoader.setSourceURIs(java.util.Arrays.asList(metadataFile.toUri()));
            simpleLoader.init();
            
            // Verify constraint enforcement is working
            // Try both simple name and fully qualified name
            com.draagon.meta.object.MetaObject testObject = null;
            try {
                testObject = simpleLoader.getMetaObjectByName("ConstraintTest");
            } catch (Exception e) {
                // Try fully qualified name (package is test::constraints)
                testObject = simpleLoader.getMetaObjectByName("test::constraints::ConstraintTest");
            }
            assertNotNull("Test object should be loaded", testObject);
            
            // Try to add an invalid child - should be rejected
            // Get a field from the object and try to add another field to it (invalid)
            MetaField validField = testObject.getMetaField("validField");
            assertNotNull("Should have validField", validField);

            try {
                StringField invalidChild = new StringField("invalidNestedField");
                validField.addChild(invalidChild);
                fail("Should reject field as child of field");
            } catch (Exception e) {
                assertTrue("Should reject same type addition (actual: " + e.getMessage() + ")",
                          e.getMessage().toLowerCase().contains("cannot add the same metadata type"));
            }
            
        } finally {
            loader.destroy();
        }
    }

    // Helper methods

    private void createAllFieldTypesMetadata(Path outputFile) throws IOException {
        String metadata = """
            {
              "metadata": {
                "package": "test::alltypes",
                "children": [
                  {
                    "object": {
                      "name": "AllFieldTypesTest",
                      "type": "pojo",
                      "children": [
                        {
                          "field": {
                            "name": "testString",
                            "type": "string"
                          }
                        },
                        {
                          "field": {
                            "name": "testInt",
                            "type": "int"
                          }
                        },
                        {
                          "field": {
                            "name": "testLong",
                            "type": "long"
                          }
                        },
                        {
                          "field": {
                            "name": "testDouble",
                            "type": "double"
                          }
                        },
                        {
                          "field": {
                            "name": "testBoolean",
                            "type": "boolean"
                          }
                        },
                        {
                          "field": {
                            "name": "testDate",
                            "type": "date"
                          }
                        }
                      ]
                    }
                  }
                ]
              }
            }
            """;
        Files.writeString(outputFile, metadata, StandardCharsets.UTF_8);
    }

    private void createFieldsWithAttributesMetadata(Path outputFile) throws IOException {
        String metadata = """
            {
              "metadata": {
                "package": "test::withattributes",
                "children": [
                  {
                    "object": {
                      "name": "FieldsWithAttributesTest",
                      "type": "pojo",
                      "children": [
                        {
                          "field": {
                            "name": "email",
                            "type": "string",
                            "@maxLength": 255,
                            "@pattern": "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}$"
                          }
                        },
                        {
                          "field": {
                            "name": "price",
                            "type": "double",
                            "@precision": 2,
                            "@minValue": 0.0
                          }
                        },
                        {
                          "field": {
                            "name": "quantity",
                            "type": "long",
                            "@minValue": 1,
                            "@maxValue": 1000
                          }
                        }
                      ]
                    }
                  }
                ]
              }
            }
            """;
        Files.writeString(outputFile, metadata, StandardCharsets.UTF_8);
    }

    private void createConstraintTestMetadata(Path outputFile) throws IOException {
        String metadata = """
            {
              "metadata": {
                "package": "test::constraints",
                "children": [
                  {
                    "object": {
                      "name": "ConstraintTest",
                      "type": "pojo",
                      "children": [
                        {
                          "field": {
                            "name": "validField",
                            "type": "string"
                          }
                        }
                      ]
                    }
                  }
                ]
              }
            }
            """;
        Files.writeString(outputFile, metadata, StandardCharsets.UTF_8);
    }

    private void deleteDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            dir.delete();
        }
    }
}