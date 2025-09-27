package com.metaobjects.generator;

import com.metaobjects.generator.direct.metadata.ai.MetaDataAIDocumentationWriter;
import com.metaobjects.generator.direct.metadata.file.json.MetaDataFileSchemaWriter;
import com.metaobjects.generator.direct.metadata.file.xsd.MetaDataFileXSDWriter;
import com.metaobjects.registry.MetaDataRegistry;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;

/**
 * Test to generate all schema files for comprehensive review
 */
public class SchemaReviewTest {

    private static final Logger log = LoggerFactory.getLogger(SchemaReviewTest.class);

    @Before
    public void setUp() {
        // Ensure core types are loaded by forcing class initialization
        try {
            Class.forName("com.metaobjects.field.StringField");
            Class.forName("com.metaobjects.field.IntegerField");
            Class.forName("com.metaobjects.field.LongField");
            Class.forName("com.metaobjects.field.DoubleField");
            Class.forName("com.metaobjects.field.DateField");
            Class.forName("com.metaobjects.field.TimestampField");
            Class.forName("com.metaobjects.object.MetaObject");
            Class.forName("com.metaobjects.object.pojo.PojoMetaObject");
            Class.forName("com.metaobjects.object.proxy.ProxyMetaObject");
            Class.forName("com.metaobjects.attr.StringAttribute");
            Class.forName("com.metaobjects.attr.IntAttribute");
            Class.forName("com.metaobjects.attr.BooleanAttribute");
        } catch (ClassNotFoundException e) {
            log.warn("Some core types not available for testing: {}", e.getMessage());
        }

        MetaDataRegistry registry = MetaDataRegistry.getInstance();
        log.info("Registry initialized with {} registered types", registry.getRegisteredTypes().size());
        log.info("Constraint count: {}", registry.getAllValidationConstraints().size());
        log.info("Registered types: {}", registry.getRegisteredTypeNames());
    }

    @Test
    public void generateAllSchemasForReview() throws Exception {
        log.info("=== GENERATING ALL SCHEMAS FOR COMPREHENSIVE REVIEW ===");

        // Generate AI Documentation
        log.info("Generating AI documentation...");
        try (FileOutputStream aiOut = new FileOutputStream("target/ai-documentation.json")) {
            MetaDataAIDocumentationWriter aiWriter = new MetaDataAIDocumentationWriter(null, aiOut);
            aiWriter.withInheritance(true)
                   .withExtensionGuidance(true)
                   .withCrossLanguageInfo(true);
            aiWriter.writeJson();
            aiWriter.close();  // Important: Must call close() to actually write the JSON
            log.info("✅ Generated AI documentation: target/ai-documentation.json");
        }

        // Generate working AI Documentation (for other tests)
        try (FileOutputStream aiOut = new FileOutputStream("target/working-ai-documentation.json")) {
            MetaDataAIDocumentationWriter aiWriter = new MetaDataAIDocumentationWriter(null, aiOut);
            aiWriter.withInheritance(true)
                   .withExtensionGuidance(true)
                   .withCrossLanguageInfo(true);
            aiWriter.writeJson();
            aiWriter.close();
            log.info("✅ Generated working AI documentation: target/working-ai-documentation.json");
        }

        // Generate JSON Schema
        log.info("Generating JSON schema...");
        try (FileOutputStream jsonOut = new FileOutputStream("target/metadata-schema.json")) {
            MetaDataFileSchemaWriter jsonWriter = new MetaDataFileSchemaWriter(null, jsonOut);
            jsonWriter.writeJson();
            jsonWriter.close();  // Important: Must call close() to actually write the JSON
            log.info("✅ Generated JSON schema: target/metadata-schema.json");
        }

        // Generate working JSON Schema (for other tests)
        try (FileOutputStream jsonOut = new FileOutputStream("target/working-metadata-schema.json")) {
            MetaDataFileSchemaWriter jsonWriter = new MetaDataFileSchemaWriter(null, jsonOut);
            jsonWriter.writeJson();
            jsonWriter.close();
            log.info("✅ Generated working JSON schema: target/working-metadata-schema.json");
        }

        // Generate XSD Schema
        log.info("Generating XSD schema...");
        try (FileOutputStream xsdOut = new FileOutputStream("target/metadata-schema.xsd")) {
            MetaDataFileXSDWriter xsdWriter = new MetaDataFileXSDWriter(null, xsdOut);
            xsdWriter.writeXML();
            xsdWriter.close();  // Important: Must call close() to actually write the XML
            log.info("✅ Generated XSD schema: target/metadata-schema.xsd");
        }

        // Generate working XSD Schema (for other tests)
        try (FileOutputStream xsdOut = new FileOutputStream("target/working-metadata-schema.xsd")) {
            MetaDataFileXSDWriter xsdWriter = new MetaDataFileXSDWriter(null, xsdOut);
            xsdWriter.writeXML();
            xsdWriter.close();
            log.info("✅ Generated working XSD schema: target/working-metadata-schema.xsd");
        }

        log.info("=== ALL SCHEMAS GENERATED SUCCESSFULLY ===");
        log.info("Review files in codegen-base/target/:");
        log.info("- ai-documentation.json (AI-optimized metadata documentation)");
        log.info("- metadata-schema.json (JSON Schema for metadata validation)");
        log.info("- metadata-schema.xsd (XSD Schema for XML metadata validation)");
    }
}