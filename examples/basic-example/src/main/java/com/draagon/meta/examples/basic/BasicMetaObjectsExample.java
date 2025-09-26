package com.draagon.meta.examples.basic;

import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.value.ValueObject;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.generator.mustache.MustacheTemplateGenerator;
import com.draagon.meta.MetaData;
import com.draagon.meta.util.MetaDataUtil;

import java.net.URI;
import java.util.Arrays;

/**
 * Basic example demonstrating SIMPLE PATTERN MetaObjects usage without framework integration.
 *
 * This example demonstrates the simple pattern for single-loader scenarios:
 * 1. Direct SimpleLoader usage (no registry complexity)
 * 2. MetaDataUtil.findMetaObject*(loader, ...) methods
 * 3. Single-loader metadata operations
 * 4. Basic object creation and validation
 * 5. Code generation capabilities
 *
 * Use this pattern when you have one MetaDataLoader and don't need multi-loader registry features.
 */
public class BasicMetaObjectsExample {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== MetaObjects Basic Example ===");
            
            // 1. Load metadata from shared resources
            System.out.println("\n1. Loading metadata...");
            SimpleLoader loader = new SimpleLoader("examples");
            
            // Load from classpath - create a temporary file for SimpleLoader
            java.net.URL resourceUrl = BasicMetaObjectsExample.class.getResource("/metadata/examples-metadata.json");
            if (resourceUrl == null) {
                throw new RuntimeException("Could not find metadata resource: /metadata/examples-metadata.json");
            }
            
            // Create temporary file and copy resource content
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("examples-metadata", ".json");
            try (java.io.InputStream is = resourceUrl.openStream()) {
                java.nio.file.Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            
            URI metadataUri = tempFile.toUri();
            System.out.println("   Temporary file URI: " + metadataUri);
            
            loader.setSourceURIs(Arrays.asList(metadataUri));
            loader.init();
            
            System.out.println("   Loaded " + loader.getChildren().size() + " metadata items");
            
            // 2. Generate POJO classes using Mustache templates (commented out for simplicity)
            System.out.println("\n2. Code generation capability available...");
            // MustacheTemplateGenerator generator = new MustacheTemplateGenerator();
            // String outputDir = "./target/generated-sources/java/";
            // generator.setOutputDirectory(outputDir);
            // generator.setTemplatePath("/templates/basic-pojo.mustache");
            // generator.generate(loader);
            System.out.println("   (Code generation available via MustacheTemplateGenerator)");
            
            // 3. Work with metadata directly
            System.out.println("\n3. Working with metadata...");
            
            // Debug: List all loaded children
            System.out.println("   Debug: All loaded children:");
            for (MetaData child : loader.getChildren()) {
                System.out.println("     - " + child.getClass().getSimpleName() + ": " + child.getName() + " (type: " + child.getType() + ")");
            }
            
            // Debug: List all MetaObjects specifically
            System.out.println("   Debug: All MetaObjects:");
            for (MetaObject obj : loader.getChildren(MetaObject.class)) {
                System.out.println("     - " + obj.getName() + " (package: " + obj.getPackage() + ")");
            }
            
            // SIMPLE PATTERN: Direct loader MetaObject lookup using utility methods
            MetaObject userMeta = null;
            try {
                // Use MetaDataUtil simple pattern method (recommended)
                userMeta = MetaDataUtil.findMetaObjectByName(loader, "com_example_model::User");
                System.out.println("   Found User MetaObject via MetaDataUtil: " + userMeta.getName());
                System.out.println("   Fields: " + userMeta.getMetaFields().size());
            } catch (Exception e) {
                System.out.println("   Error finding User via MetaDataUtil: " + e.getMessage());
                // Fallback to direct loader access
                try {
                    userMeta = loader.getMetaObjectByName("com_example_model::User");
                    System.out.println("   Found User via direct loader: " + userMeta.getName());
                    System.out.println("   Fields: " + userMeta.getMetaFields().size());
                } catch (Exception e2) {
                    System.out.println("   Error with direct loader access: " + e2.getMessage());
                }
            }
            
            if (userMeta != null) {
                for (MetaField field : userMeta.getMetaFields()) {
                    System.out.println("     - " + field.getName() + " (" + field.getSubType() + ")");
                }
            }
            
            // 4. Create and manipulate objects using metadata
            System.out.println("\n4. Creating and validating objects...");
            
            if (userMeta != null) {
                // Create a user object associated with the metadata
                ValueObject user = new ValueObject(userMeta);
                user.put("id", 1L);
                user.put("username", "john_doe");
                user.put("email", "john@example.com");
                user.put("createdDate", new java.util.Date());
                
                System.out.println("   Created user: " + user);
                
                // Validate using metadata (basic validation)
                boolean isValid = true;
                for (MetaField field : userMeta.getMetaFields()) {
                    if (field.hasMetaAttr("required")) {
                        boolean required = Boolean.parseBoolean(
                            field.getMetaAttr("required").getValueAsString());
                        if (required && !user.containsKey(field.getName())) {
                            System.out.println("   Validation error: Required field '" + 
                                field.getName() + "' is missing");
                            isValid = false;
                        }
                    }
                }
                System.out.println("   Validation result: " + (isValid ? "VALID" : "INVALID"));
                
                // 5. Direct metadata manipulation examples
                System.out.println("\n5. Direct metadata access...");
                
                MetaField emailField = userMeta.getMetaField("email");
                Object emailValue = user.get("email");
                System.out.println("   Email field value: " + emailValue);
                
                // Check field attributes
                if (emailField.hasMetaAttr("required")) {
                    boolean required = Boolean.parseBoolean(
                        emailField.getMetaAttr("required").getValueAsString());
                    System.out.println("   Email field is required: " + required);
                }
                
                if (emailField.hasMetaAttr("maxLength")) {
                    int maxLength = Integer.parseInt(
                        emailField.getMetaAttr("maxLength").getValueAsString());
                    System.out.println("   Email field max length: " + maxLength);
                }
            } else {
                System.out.println("   Cannot create user object - User MetaObject not found");
            }
            
            // 6. SIMPLE PATTERN: Demonstrate utility methods for single-loader scenarios
            System.out.println("\n6. Simple pattern utility methods...");
            System.out.println("   Loader name: " + loader.getName());

            // Use MetaDataUtil simple pattern method to get all MetaObjects
            java.util.List<MetaObject> allObjects = MetaDataUtil.getAllMetaObjects(loader);
            System.out.println("   Total MetaObjects via utility: " + allObjects.size());

            for (MetaObject obj : allObjects) {
                System.out.println("     - " + obj.getName() + " (package: " + obj.getPackage() + ")");
            }

            // 7. Simple vs Complex pattern comparison
            System.out.println("\n7. When to use simple vs complex patterns...");
            System.out.println("   SIMPLE PATTERN (this example):");
            System.out.println("     - Single MetaDataLoader");
            System.out.println("     - Direct loader.getMetaObjectByName() or MetaDataUtil.findMetaObjectByName(loader, ...)");
            System.out.println("     - No registry complexity needed");
            System.out.println("   COMPLEX PATTERN (see spring-example, osgi-example):");
            System.out.println("     - Multiple MetaDataLoaders in registry");
            System.out.println("     - MetaDataLoaderRegistry or MetaDataService");
            System.out.println("     - Multi-tenant, plugin, or framework scenarios");
            
            System.out.println("\n=== Example completed successfully ===");
            
        } catch (Exception e) {
            System.err.println("Error running example: " + e.getMessage());
            e.printStackTrace();
        }
    }
}