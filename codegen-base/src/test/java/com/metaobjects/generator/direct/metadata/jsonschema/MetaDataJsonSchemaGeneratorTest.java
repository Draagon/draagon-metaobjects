package com.metaobjects.generator.direct.metadata.jsonschema;

import com.metaobjects.generator.GeneratorIOException;
import com.metaobjects.generator.direct.metadata.file.json.MetaDataFileJsonSchemaGenerator;
import com.metaobjects.generator.direct.metadata.file.json.MetaDataFileSchemaWriter;
import com.metaobjects.loader.MetaDataLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for MetaDataFileJsonSchemaGenerator and MetaDataFileSchemaWriter.
 */
public class MetaDataJsonSchemaGeneratorTest {

    private MetaDataLoader testLoader;

    @Before
    public void setUp() {
        // Create a simple test loader with basic configuration
        testLoader = MetaDataLoader.createManual(false, "json-schema-test")
                .init()
                .register()
                .getLoader();
    }

    @After
    public void tearDown() {
        if (testLoader != null) {
            testLoader.destroy();
            testLoader = null;
        }
    }

    @Test
    public void testJsonSchemaGeneration() throws GeneratorIOException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        MetaDataFileSchemaWriter writer = new MetaDataFileSchemaWriter(testLoader, output)
                .withSchemaVersion("https://json-schema.org/draft/2020-12/schema")
                .withSchemaId("https://example.com/metadata-schema.json")
                .withTitle("Test MetaData Schema")
                .withDescription("Test schema for metadata validation");

        writer.writeJson();
        writer.close();

        String jsonOutput = output.toString(StandardCharsets.UTF_8);
        assertNotNull("JSON output should not be null", jsonOutput);
        assertFalse("JSON output should not be empty", jsonOutput.trim().isEmpty());

        // Parse and validate the JSON structure
        JsonObject schema = JsonParser.parseString(jsonOutput).getAsJsonObject();
        
        // Verify schema metadata
        assertTrue("Schema should have $schema property", schema.has("$schema"));
        assertEquals("https://json-schema.org/draft/2020-12/schema", 
                    schema.get("$schema").getAsString());
        
        assertTrue("Schema should have $id property", schema.has("$id"));
        assertEquals("https://example.com/metadata-schema.json", 
                    schema.get("$id").getAsString());
        
        assertTrue("Schema should have title property", schema.has("title"));
        assertEquals("Test MetaData Schema", schema.get("title").getAsString());
        
        assertTrue("Schema should have description property", schema.has("description"));
        assertEquals("Test schema for metadata validation", 
                    schema.get("description").getAsString());
        
        // Verify structure
        assertTrue("Schema should have type property", schema.has("type"));
        assertEquals("object", schema.get("type").getAsString());
        
        assertTrue("Schema should have $defs property", schema.has("$defs"));
        assertTrue("Schema should have properties", schema.has("properties"));
    }

    @Test
    public void testGeneratorConfiguration() {
        MetaDataFileJsonSchemaGenerator generator = new MetaDataFileJsonSchemaGenerator();
        
        // Set test arguments using setArgs with Map
        Map<String, String> args = new HashMap<>();
        args.put(MetaDataFileJsonSchemaGenerator.ARG_SCHEMA_VERSION, 
                 "https://json-schema.org/draft/2019-09/schema");
        args.put(MetaDataFileJsonSchemaGenerator.ARG_SCHEMA_ID, 
                 "https://test.com/schema.json");
        args.put(MetaDataFileJsonSchemaGenerator.ARG_TITLE, "Custom Title");
        args.put(MetaDataFileJsonSchemaGenerator.ARG_DESCRIPTION, "Custom Description");
        
        generator.setArgs(args);
        
        // Test toString includes all arguments
        String toString = generator.toString();
        assertTrue("toString should include class name", 
                  toString.contains("MetaDataFileJsonSchemaGenerator"));
    }

    @Test
    public void testWriterConfiguration() throws GeneratorIOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        MetaDataFileSchemaWriter writer = new MetaDataFileSchemaWriter(testLoader, output)
                .withSchemaVersion("custom-version")
                .withSchemaId("custom-id")
                .withTitle("custom-title")
                .withDescription("custom-description");

        // Test toString includes configuration - note: the new writer doesn't expose getters
        String toString = writer.toString();
        assertTrue("toString should include schema version", toString.contains("custom-version"));
        assertTrue("toString should include schema id", toString.contains("custom-id"));
    }

    @Test
    public void testMinimalConfiguration() throws GeneratorIOException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        // Test with minimal configuration (only required parameters)
        MetaDataFileSchemaWriter writer = new MetaDataFileSchemaWriter(testLoader, output);
        writer.writeJson();
        writer.close();

        String jsonOutput = output.toString(StandardCharsets.UTF_8);
        JsonObject schema = JsonParser.parseString(jsonOutput).getAsJsonObject();
        
        // Should have default values
        assertTrue("Should have default schema version", schema.has("$schema"));
        assertTrue("Should have default title", schema.has("title"));
        assertTrue("Should have default description", schema.has("description"));
        
        // Should not have $id if not specified
        assertFalse("Should not have $id when not specified", 
                   schema.has("$id") && !schema.get("$id").isJsonNull());
    }
}