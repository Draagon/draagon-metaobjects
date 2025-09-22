package com.draagon.meta.generator.direct.metadata.ai;

import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.TypeDefinition;
import com.draagon.meta.util.MetaDataConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import static org.junit.Assert.*;

/**
 * Test for the AI documentation generator to verify inheritance visualization,
 * attribute classification, and extension guidance generation.
 */
public class MetaDataAIDocumentationTest {

    private static final Logger log = LoggerFactory.getLogger(MetaDataAIDocumentationTest.class);

    private MetaDataRegistry registry;
    private ByteArrayOutputStream outputStream;

    @Before
    public void setUp() {
        registry = MetaDataRegistry.getInstance();
        outputStream = new ByteArrayOutputStream();

        // Ensure core types are loaded by forcing class initialization
        try {
            Class.forName("com.draagon.meta.field.StringField");
            Class.forName("com.draagon.meta.field.IntegerField");
            Class.forName("com.draagon.meta.object.MetaObject");
        } catch (ClassNotFoundException e) {
            log.warn("Some core types not available for testing: {}", e.getMessage());
        }

        log.info("Test setup completed with {} registered types", registry.getRegisteredTypes().size());
    }

    @Test
    public void testAIDocumentationGeneration() throws Exception {
        // Create the AI documentation generator
        MetaDataAIDocumentationGenerator generator = new MetaDataAIDocumentationGenerator();
        generator.setIncludeInheritance(true);
        generator.setIncludeExtensionGuidance(true);
        generator.setIncludeCrossLanguageInfo(true);

        log.info("Testing AI documentation generation with configuration: {}",
                generator.getConfigurationSummary());

        // Create a writer with test loader (null is acceptable for registry-based generation)
        MetaDataAIDocumentationWriter writer = new MetaDataAIDocumentationWriter(null, outputStream);
        writer.withInheritance(true)
              .withExtensionGuidance(true)
              .withCrossLanguageInfo(true);

        // Generate the documentation
        writer.writeJson();

        // Close the writer to flush JSON to the output stream
        writer.close();

        // Parse and validate the output
        String jsonOutput = outputStream.toString();
        assertNotNull("Documentation should be generated", jsonOutput);
        assertFalse("Documentation should not be empty", jsonOutput.trim().isEmpty());

        log.info("Generated AI documentation ({} bytes)", jsonOutput.length());

        // Parse JSON to validate structure
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject doc = gson.fromJson(jsonOutput, JsonObject.class);

        // Validate main sections
        assertTrue("Should have documentation info", doc.has("documentationInfo"));
        assertTrue("Should have inheritance hierarchy", doc.has("inheritanceHierarchy"));
        assertTrue("Should have type catalog", doc.has("typeCatalog"));
        assertTrue("Should have extension guidance", doc.has("extensionGuidance"));
        assertTrue("Should have implementation mapping", doc.has("implementationMapping"));
        assertTrue("Should have cross-language support", doc.has("crossLanguageSupport"));

        // Validate documentation info
        JsonObject docInfo = doc.getAsJsonObject("documentationInfo");
        assertTrue("Should be marked as AI optimized", docInfo.get("aiOptimized").getAsBoolean());
        assertTrue("Should have inheritance support", docInfo.get("inheritanceSupport").getAsBoolean());
        assertTrue("Should have positive type count", docInfo.get("totalTypes").getAsInt() > 0);

        // Validate type catalog has entries
        JsonObject typeCatalog = doc.getAsJsonObject("typeCatalog");
        assertTrue("Type catalog should have entries", typeCatalog.size() > 0);

        // Log sample of the generated documentation for inspection
        log.info("Sample AI documentation structure:\n{}",
                gson.toJson(doc.getAsJsonObject("documentationInfo")));

        // Validate extension guidance
        JsonObject extensionGuidance = doc.getAsJsonObject("extensionGuidance");
        assertTrue("Should have extension patterns", extensionGuidance.has("extensionPatterns"));
        assertTrue("Should have extension points", extensionGuidance.has("extensionPoints"));
        assertTrue("Should have registration example", extensionGuidance.has("registrationExample"));

        log.info("AI documentation generation test completed successfully");
    }

    @Test
    public void testRegistryTypeDiscovery() {
        // Verify we have types registered for testing
        int typeCount = registry.getRegisteredTypes().size();
        log.info("Registry contains {} types: {}", typeCount, registry.getRegisteredTypeNames());

        assertTrue("Should have at least some core types registered", typeCount > 0);

        // Check for specific type patterns
        boolean hasFieldTypes = registry.getRegisteredTypeNames().stream()
                .anyMatch(name -> name.startsWith(MetaDataConstants.TYPE_FIELD + "."));
        boolean hasObjectTypes = registry.getRegisteredTypeNames().stream()
                .anyMatch(name -> name.startsWith(MetaDataConstants.TYPE_OBJECT + "."));

        log.info("Has field types: {}, Has object types: {}", hasFieldTypes, hasObjectTypes);

        // At minimum we should have some registered types
        assertTrue("Registry should contain registered types for documentation generation", typeCount > 0);
    }

    @Test
    public void testInheritanceAnalysis() {
        // Count types with inheritance relationships
        long inheritedTypes = registry.getAllTypeDefinitions().stream()
                .filter(TypeDefinition::hasParent)
                .count();

        log.info("Found {} types with inheritance relationships", inheritedTypes);

        // Log inheritance examples if available
        registry.getAllTypeDefinitions().stream()
                .filter(TypeDefinition::hasParent)
                .limit(3)
                .forEach(def -> log.info("Inheritance example: {} inherits from {}",
                        def.getQualifiedName(), def.getParentQualifiedName()));
    }

    @Test
    public void testGeneratorConfiguration() {
        MetaDataAIDocumentationGenerator generator = new MetaDataAIDocumentationGenerator();

        // Test configuration methods
        generator.setVersion("test-version");
        generator.setIncludeInheritance(false);
        generator.setIncludeImplementationDetails(false);
        generator.setIncludeExtensionGuidance(false);
        generator.setIncludeCrossLanguageInfo(true);

        String summary = generator.getConfigurationSummary();
        assertNotNull("Configuration summary should not be null", summary);
        assertTrue("Should include version", summary.contains("test-version"));
        assertTrue("Should reflect disabled inheritance", summary.contains("Inheritance Analysis: Disabled"));
        assertTrue("Should reflect enabled cross-language", summary.contains("Cross-Language Info: Enabled"));

        log.info("Generator configuration test: {}", summary);
    }
}