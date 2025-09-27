package com.metaobjects.generator;

import com.metaobjects.generator.direct.metadata.ai.MetaDataAIDocumentationWriter;
import com.metaobjects.generator.direct.metadata.file.json.MetaDataFileSchemaWriter;
import com.metaobjects.generator.direct.metadata.file.xsd.MetaDataFileXSDWriter;
import com.metaobjects.registry.MetaDataRegistry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Working test to generate schema files based on successful AI documentation test
 */
public class WorkingSchemaGeneratorTest {

    private static final Logger log = LoggerFactory.getLogger(WorkingSchemaGeneratorTest.class);

    private MetaDataRegistry registry;

    @Before
    public void setUp() {
        registry = MetaDataRegistry.getInstance();

        // Ensure core types are loaded by forcing class initialization
        try {
            Class.forName("com.metaobjects.field.StringField");
            Class.forName("com.metaobjects.field.IntegerField");
            Class.forName("com.metaobjects.object.MetaObject");
        } catch (ClassNotFoundException e) {
            log.warn("Some core types not available for testing: {}", e.getMessage());
        }

        log.info("Test setup completed with {} registered types", registry.getRegisteredTypes().size());
        log.info("Available types: {}", registry.getRegisteredTypeNames());
        log.info("Total constraints: {}", registry.getAllValidationConstraints().size());
    }

    @Test
    public void generateWorkingSchemas() throws Exception {
        log.info("=== GENERATING WORKING SCHEMAS ===");

        // 1. Generate AI Documentation (using working approach)
        log.info("Generating AI documentation...");
        try (ByteArrayOutputStream aiStream = new ByteArrayOutputStream()) {
            MetaDataAIDocumentationWriter aiWriter = new MetaDataAIDocumentationWriter(null, aiStream);
            aiWriter.withInheritance(true)
                   .withExtensionGuidance(true)
                   .withCrossLanguageInfo(true);
            aiWriter.writeJson();
            aiWriter.close();

            String aiJson = aiStream.toString();
            if (!aiJson.trim().isEmpty()) {
                // Save to file
                try (FileWriter writer = new FileWriter("target/working-ai-documentation.json")) {
                    writer.write(aiJson);
                }
                log.info("✅ Generated AI documentation: {} bytes -> target/working-ai-documentation.json", aiJson.length());

                // Parse and log summary
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonObject doc = gson.fromJson(aiJson, JsonObject.class);
                JsonObject docInfo = doc.getAsJsonObject("documentationInfo");
                log.info("AI Doc Summary: {} types, {} inheritance relationships",
                        docInfo.get("totalTypes").getAsInt(),
                        doc.has("inheritanceHierarchy") ? "yes" : "none");
            } else {
                log.warn("❌ AI documentation is empty");
            }
        }

        // 2. Generate JSON Schema
        log.info("Generating JSON schema...");
        try (ByteArrayOutputStream jsonStream = new ByteArrayOutputStream()) {
            MetaDataFileSchemaWriter jsonWriter = new MetaDataFileSchemaWriter(null, jsonStream);
            jsonWriter.writeJson();
            jsonWriter.close();

            String jsonSchema = jsonStream.toString();
            if (!jsonSchema.trim().isEmpty()) {
                try (FileWriter writer = new FileWriter("target/working-metadata-schema.json")) {
                    writer.write(jsonSchema);
                }
                log.info("✅ Generated JSON schema: {} bytes -> target/working-metadata-schema.json", jsonSchema.length());
            } else {
                log.warn("❌ JSON schema is empty");
            }
        }

        // 3. Generate XSD Schema
        log.info("Generating XSD schema...");
        try (ByteArrayOutputStream xsdStream = new ByteArrayOutputStream()) {
            MetaDataFileXSDWriter xsdWriter = new MetaDataFileXSDWriter(null, xsdStream);
            xsdWriter.writeXML();
            xsdWriter.close();

            String xsdSchema = xsdStream.toString();
            if (!xsdSchema.trim().isEmpty()) {
                try (FileWriter writer = new FileWriter("target/working-metadata-schema.xsd")) {
                    writer.write(xsdSchema);
                }
                log.info("✅ Generated XSD schema: {} bytes -> target/working-metadata-schema.xsd", xsdSchema.length());
            } else {
                log.warn("❌ XSD schema is empty");
            }
        }

        log.info("=== SCHEMA GENERATION COMPLETED ===");
    }
}