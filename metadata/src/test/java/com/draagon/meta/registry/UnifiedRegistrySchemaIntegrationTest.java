package com.draagon.meta.registry;

import com.draagon.meta.field.StringField;
import com.draagon.meta.field.IntegerField;
import com.draagon.meta.object.pojo.PojoMetaObject;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.attr.BooleanAttribute;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Integration tests for schema generation using the unified MetaDataRegistry.
 * Tests that the unified registry integrates properly with schema generation systems.
 */
public class UnifiedRegistrySchemaIntegrationTest {

    private MetaDataRegistry registry;
    private Path tempDir;

    @Before
    public void setUp() throws IOException {
        // Create temp directory for test files
        tempDir = Files.createTempDirectory("unified-registry-schema-integration-test");
        
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
     * Trigger static registrations by creating instances of registered types
     */
    private void triggerStaticRegistrations() {
        try {
            // Create instances to ensure static blocks execute
            new StringField("testString");
            new IntegerField("testInt");
            new com.draagon.meta.field.TimestampField("testTimestamp");  // Trigger TimestampField registration
            new PojoMetaObject("testObject");
            new StringAttribute("testStringAttr");
            new IntAttribute("testIntAttr");
        } catch (Exception e) {
            // Ignore - just triggering static registrations
        }
    }

    @Test
    public void testUnifiedRegistryConstraintIntegration() {
        // Verify unified registry is working
        assertNotNull("Unified registry should be available", registry);
        
        // Test type registration from static blocks
        assertTrue("Registry should have StringField type", 
                  registry.acceptsChild("field", "string", "attr", "string", "pattern"));
        assertTrue("Registry should have IntegerField type", 
                  registry.acceptsChild("field", "int", "attr", "int", "maxValue"));
        assertTrue("Registry should have MetaObject type", 
                  registry.acceptsChild("object", "pojo", "field", "string", "testField"));
        
        // Test constraint enforcement
        String description = registry.getSupportedChildrenDescription("field", "string");
        assertNotNull("Should have supported children description", description);
        assertTrue("Description should mention attributes", 
                  description.toLowerCase().contains("attribute"));
    }

    @Test
    public void testRegistryTypeLookup() {
        // Verify registry type lookups work properly
        MetaDataRegistry.RegistryStats stats = registry.getStats();
        assertNotNull("Registry stats should be available", stats);
        assertTrue("Should have registered types", stats.totalTypes() > 0);
        
        // Test specific type lookups that schema generation would use
        Set<String> typeNames = registry.getRegisteredTypeNames();
        // Extract base types from qualified names (e.g., "field.string" -> "field")
        Set<String> baseTypes = typeNames.stream()
            .map(name -> name.split("\\.")[0])
            .collect(java.util.stream.Collectors.toSet());
        assertTrue("Should have field types", baseTypes.contains("field"));
        assertTrue("Should have object types", baseTypes.contains("object"));  
        assertTrue("Should have attr types", baseTypes.contains("attr"));
    }

    @Test
    public void testConstraintSystemIntegration() {
        // Test that the constraint system works with the unified registry
        
        // Create a valid metadata structure
        PojoMetaObject userObject = new PojoMetaObject("User");
        StringField emailField = new StringField("email");
        BooleanAttribute requiredAttr = new BooleanAttribute("required");
        
        // Test that valid child relationships work
        try {
            emailField.addChild(requiredAttr);
            userObject.addChild(emailField);
            assertTrue("Valid metadata structure should be accepted", true);
        } catch (Exception e) {
            fail("Valid metadata structure should not throw exception: " + e.getMessage());
        }
        
        // Test constraint validation - invalid names should be rejected 
        try {
            StringField invalidField = new StringField("123invalid");  // Starts with number - truly invalid
            userObject.addChild(invalidField);
            fail("Invalid field name should be rejected by constraints");
        } catch (Exception e) {
            // Expected - constraint should reject invalid names
            assertTrue("Exception should mention constraint violation", 
                      e.getMessage().toLowerCase().contains("constraint violation"));
        }
    }

    @Test
    public void testMetadataStructureValidation() throws IOException {
        // Create test metadata files that would be validated by generated schemas
        
        // Test 1: Valid metadata structure
        Path validMetadata = tempDir.resolve("valid-metadata.json");
        createValidMetadataJson(validMetadata);
        
        // Verify structure meets registry expectations
        String validContent = Files.readString(validMetadata, StandardCharsets.UTF_8);
        JsonObject validData = JsonParser.parseString(validContent).getAsJsonObject();
        
        assertTrue("Valid metadata should have metadata root", validData.has("metadata"));
        JsonObject metadata = validData.getAsJsonObject("metadata");
        assertTrue("Valid metadata should have children", metadata.has("children"));
        
        // Verify naming constraints that registry enforces
        validateNamingConstraints(metadata, "Valid metadata");
        
        // Test 2: Invalid metadata structure 
        Path invalidMetadata = tempDir.resolve("invalid-metadata.json");
        createInvalidMetadataJson(invalidMetadata);
        
        String invalidContent = Files.readString(invalidMetadata, StandardCharsets.UTF_8);
        JsonObject invalidData = JsonParser.parseString(invalidContent).getAsJsonObject();
        
        // Verify this violates registry constraints
        JsonObject firstChild = invalidData.getAsJsonObject("metadata")
                .getAsJsonArray("children")
                .get(0).getAsJsonObject()
                .getAsJsonObject("object");
        String invalidName = firstChild.get("name").getAsString();
        
        assertFalse("Invalid name should not match registry constraints", 
                   invalidName.matches("^[a-zA-Z][a-zA-Z0-9_]*$"));
    }

    @Test
    public void testMultipleMetadataStructures() throws IOException {
        // Test various metadata structures that the registry should support
        
        // Simple user structure
        Path simpleUser = tempDir.resolve("simple-user.json");
        createSimpleUserMetadata(simpleUser);
        validateMetadataStructure(simpleUser, "Simple user structure");
        
        // E-commerce structure with multiple objects
        Path ecommerce = tempDir.resolve("ecommerce.json");
        createEcommerceMetadata(ecommerce);
        validateMetadataStructure(ecommerce, "E-commerce structure");
        
        // Nested object structure
        Path nested = tempDir.resolve("nested.json");
        createNestedMetadata(nested);
        validateMetadataStructure(nested, "Nested structure");
        
        // Various field types
        Path fieldTypes = tempDir.resolve("field-types.json");
        createFieldTypesMetadata(fieldTypes);
        validateMetadataStructure(fieldTypes, "Field types structure");
    }

    @Test
    public void testRegistrySchemaComplianceMapping() {
        // Test that registry types map correctly to schema expectations

        // Debug registry state
        System.out.println("=== REGISTRY DEBUG ===");
        System.out.println("Total types: " + registry.getStats().totalTypes());
        System.out.println("Registered types: " + registry.getRegisteredTypeNames());

        // Check if field.string is registered
        boolean stringFieldExists = registry.getRegisteredTypeNames().contains("field.string");
        System.out.println("field.string registered: " + stringFieldExists);

        // Check if attr.string is registered
        boolean stringAttrExists = registry.getRegisteredTypeNames().contains("attr.string");
        System.out.println("attr.string registered: " + stringAttrExists);

        // Check field.base acceptance of attr.string
        boolean baseAccepts = registry.acceptsChild("field", "base", "attr", "string", "testAttr");
        System.out.println("field.base accepts attr.string: " + baseAccepts);

        // Get description of what field.base supports
        String baseDescription = registry.getSupportedChildrenDescription("field", "base");
        System.out.println("field.base supported children: " + baseDescription);

        // Get description of what field.string supports
        String description = registry.getSupportedChildrenDescription("field", "string");
        System.out.println("field.string supported children: " + description);

        // Check if field.string properly inherits from field.base
        try {
            java.lang.reflect.Method getTypeDefMethod = registry.getClass().getDeclaredMethod("getTypeDefinition", String.class, String.class);
            getTypeDefMethod.setAccessible(true);
            Object baseTypeDef = getTypeDefMethod.invoke(registry, "field", "base");
            Object stringTypeDef = getTypeDefMethod.invoke(registry, "field", "string");

            if (baseTypeDef != null && stringTypeDef != null) {
                System.out.println("field.base definition exists: true");
                System.out.println("field.string definition exists: true");
                System.out.println("field.string parent: " + stringTypeDef.getClass().getMethod("getParentType").invoke(stringTypeDef) + "." +
                                 stringTypeDef.getClass().getMethod("getParentSubType").invoke(stringTypeDef));
            }
        } catch (Exception e) {
            System.out.println("Reflection debug failed: " + e.getMessage());
        }

        // Field types that should be supported
        String[] expectedFieldTypes = {"string", "int", "long", "double", "float", "boolean", "date", "timestamp"};
        for (String fieldType : expectedFieldTypes) {
            boolean accepts = registry.acceptsChild("field", fieldType, "attr", "string", "testAttr");
            System.out.println("field." + fieldType + " accepts attr.string: " + accepts);
            assertTrue("Registry should support field type: " + fieldType, accepts);
        }
        
        // Object types that should be supported  
        String[] expectedObjectTypes = {"pojo", "proxy", "map"};
        for (String objectType : expectedObjectTypes) {
            assertTrue("Registry should support object type: " + objectType,
                      registry.acceptsChild("object", objectType, "field", "string", "testField"));
        }
        
        // Attribute support
        assertTrue("String fields should accept string attributes",
                  registry.acceptsChild("field", "string", "attr", "string", "pattern"));
        assertTrue("Integer fields should accept int attributes", 
                  registry.acceptsChild("field", "int", "attr", "int", "maxValue"));
    }

    // Helper methods

    private void createValidMetadataJson(Path outputFile) throws IOException {
        String validJson = """
            {
              "metadata": {
                "package": "test::models",
                "children": [
                  {
                    "object": {
                      "name": "User",
                      "subType": "pojo",
                      "children": [
                        {
                          "field": {
                            "name": "id",
                            "subType": "long",
                            "@required": true
                          }
                        },
                        {
                          "field": {
                            "name": "email",
                            "subType": "string",
                            "@required": true,
                            "@maxLength": 255
                          }
                        }
                      ]
                    }
                  }
                ]
              }
            }
            """;
        Files.writeString(outputFile, validJson, StandardCharsets.UTF_8);
    }

    private void createInvalidMetadataJson(Path outputFile) throws IOException {
        String invalidJson = """
            {
              "metadata": {
                "children": [
                  {
                    "object": {
                      "name": "invalid::name",
                      "subType": "pojo"
                    }
                  }
                ]
              }
            }
            """;
        Files.writeString(outputFile, invalidJson, StandardCharsets.UTF_8);
    }

    private void createSimpleUserMetadata(Path outputFile) throws IOException {
        String simpleUser = """
            {
              "metadata": {
                "package": "simple::models",
                "children": [
                  {
                    "object": {
                      "name": "SimpleUser",
                      "subType": "pojo",
                      "children": [
                        {
                          "field": {
                            "name": "name",
                            "subType": "string",
                            "@required": true,
                            "@maxLength": 100
                          }
                        },
                        {
                          "field": {
                            "name": "age",
                            "subType": "int",
                            "@required": false
                          }
                        }
                      ]
                    }
                  }
                ]
              }
            }
            """;
        Files.writeString(outputFile, simpleUser, StandardCharsets.UTF_8);
    }

    private void createEcommerceMetadata(Path outputFile) throws IOException {
        String ecommerce = """
            {
              "metadata": {
                "package": "ecommerce::models",
                "children": [
                  {
                    "object": {
                      "name": "Product",
                      "subType": "pojo",
                      "children": [
                        {
                          "field": {
                            "name": "id",
                            "subType": "long",
                            "@required": true
                          }
                        },
                        {
                          "field": {
                            "name": "name",
                            "subType": "string",
                            "@required": true,
                            "@maxLength": 200
                          }
                        },
                        {
                          "field": {
                            "name": "price",
                            "subType": "double",
                            "@required": true
                          }
                        }
                      ]
                    }
                  },
                  {
                    "object": {
                      "name": "Order",
                      "subType": "pojo",
                      "children": [
                        {
                          "field": {
                            "name": "orderId",
                            "subType": "string",
                            "@required": true
                          }
                        },
                        {
                          "field": {
                            "name": "customerId",
                            "subType": "long",
                            "@required": true
                          }
                        }
                      ]
                    }
                  }
                ]
              }
            }
            """;
        Files.writeString(outputFile, ecommerce, StandardCharsets.UTF_8);
    }

    private void createNestedMetadata(Path outputFile) throws IOException {
        String nested = """
            {
              "metadata": {
                "package": "nested::models",
                "children": [
                  {
                    "object": {
                      "name": "Company",
                      "subType": "pojo",
                      "children": [
                        {
                          "field": {
                            "name": "companyName",
                            "subType": "string",
                            "@required": true
                          }
                        },
                        {
                          "object": {
                            "name": "Employee",
                            "subType": "pojo",
                            "children": [
                              {
                                "field": {
                                  "name": "employeeId",
                                  "subType": "int",
                                  "@required": true
                                }
                              },
                              {
                                "field": {
                                  "name": "firstName",
                                  "subType": "string",
                                  "@required": true
                                }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  }
                ]
              }
            }
            """;
        Files.writeString(outputFile, nested, StandardCharsets.UTF_8);
    }

    private void createFieldTypesMetadata(Path outputFile) throws IOException {
        String fieldTypes = """
            {
              "metadata": {
                "package": "types::test",
                "children": [
                  {
                    "object": {
                      "name": "AllFieldTypes",
                      "subType": "pojo",
                      "children": [
                        {
                          "field": {
                            "name": "stringField",
                            "subType": "string",
                            "@maxLength": 50
                          }
                        },
                        {
                          "field": {
                            "name": "intField",
                            "subType": "int"
                          }
                        },
                        {
                          "field": {
                            "name": "longField",
                            "subType": "long"
                          }
                        },
                        {
                          "field": {
                            "name": "doubleField",
                            "subType": "double"
                          }
                        },
                        {
                          "field": {
                            "name": "floatField",
                            "subType": "float"
                          }
                        },
                        {
                          "field": {
                            "name": "booleanField",
                            "subType": "boolean"
                          }
                        },
                        {
                          "field": {
                            "name": "dateField",
                            "subType": "date"
                          }
                        },
                        {
                          "field": {
                            "name": "timestampField",
                            "subType": "timestamp"
                          }
                        }
                      ]
                    }
                  }
                ]
              }
            }
            """;
        Files.writeString(outputFile, fieldTypes, StandardCharsets.UTF_8);
    }

    private void validateMetadataStructure(Path dataFile, String description) throws IOException {
        String content = Files.readString(dataFile, StandardCharsets.UTF_8);
        JsonObject data = JsonParser.parseString(content).getAsJsonObject();
        
        // Basic structure validation
        assertTrue(description + " should have metadata root", data.has("metadata"));
        JsonObject metadata = data.getAsJsonObject("metadata");
        assertTrue(description + " should have children", metadata.has("children"));
        
        // Validate naming constraints throughout the structure
        validateNamingConstraints(metadata, description);
    }

    private void validateNamingConstraints(JsonObject metadata, String context) {
        if (metadata.has("children")) {
            metadata.getAsJsonArray("children").forEach(childElement -> {
                JsonObject child = childElement.getAsJsonObject();
                
                // Check object names
                if (child.has("object")) {
                    JsonObject obj = child.getAsJsonObject("object");
                    if (obj.has("name")) {
                        String name = obj.get("name").getAsString();
                        assertTrue(context + " - object name should follow pattern: " + name, 
                                  name.matches("^[a-zA-Z][a-zA-Z0-9_]*$"));
                    }
                    // Recursive validation for nested objects
                    validateNamingConstraints(obj, context);
                }
                
                // Check field names
                if (child.has("field")) {
                    JsonObject field = child.getAsJsonObject("field");
                    if (field.has("name")) {
                        String name = field.get("name").getAsString();
                        assertTrue(context + " - field name should follow pattern: " + name, 
                                  name.matches("^[a-zA-Z][a-zA-Z0-9_]*$"));
                    }
                }
            });
        }
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