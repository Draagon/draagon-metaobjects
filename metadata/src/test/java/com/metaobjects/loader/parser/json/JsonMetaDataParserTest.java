package com.metaobjects.loader.parser.json;

import com.metaobjects.MetaData;
import com.metaobjects.attr.*;
import com.metaobjects.field.*;
import com.metaobjects.identity.PrimaryIdentity;
import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.loader.simple.SimpleLoader;
import com.metaobjects.object.MetaObject;
import com.metaobjects.registry.SharedRegistryTestBase;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for JsonMetaDataParser's MetaAttribute-first approach.
 * Tests all value type scenarios and JSON-specific features.
 */
public class JsonMetaDataParserTest extends SharedRegistryTestBase {

    @Before
    public void setUp() {
        // Trigger static registrations for all field types needed by tests
        try {
            new StringField("testString");
            new IntegerField("testInt");
            new LongField("testLong");
            new DoubleField("testDouble");
            new BooleanField("testBoolean");

            // Identity types
            new PrimaryIdentity("testPrimary");

            // Attribute types
            new StringAttribute("testStringAttr");
            new IntAttribute("testIntAttr");
            new BooleanAttribute("testBoolAttr");
            new DoubleAttribute("testDoubleAttr");
            new LongAttribute("testLongAttr");
        } catch (Exception e) {
            // Ignore registration errors
        }
    }

    /**
     * Debug test - simple field without attributes first
     */
    @Test
    public void testSimpleFieldParsing() throws Exception {
        String json = """
        {
          "metadata": {
            "children": [
              {
                "object": {
                  "name": "TestObject",
                  "subType": "pojo",
                  "children": [
                    {
                      "field": {
                        "name": "testField",
                        "subType": "string"
                      }
                    }
                  ]
                }
              }
            ]
          }
        }
        """;

        try {
            MetaObject result = parseJsonAndGetFirstObject(json);
            assertNotNull("Should have created object", result);
            assertEquals("Object name should be TestObject", "TestObject", result.getName());
            MetaField field = result.getMetaField("testField");
            assertNotNull("Field should exist", field);
            assertEquals("Field subType should be string", "string", field.getSubType());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Test boolean value parsing - should create BooleanAttribute
     */
    @Test
    public void testBooleanAttributeParsing() throws Exception {
        String json = """
        {
          "metadata": {
            "children": [
              {
                "object": {
                  "name": "User",
                  "subType": "pojo",
                  "children": [
                    {
                      "field": {
                        "name": "isActive",
                        "subType": "string",
                        "@required": true,
                        "@defaultValue": false
                      }
                    }
                  ]
                }
              }
            ]
          }
        }
        """;

        MetaObject result = parseJsonAndGetFirstObject(json);
        MetaField field = result.getMetaField("isActive");
        assertNotNull("Field should exist", field);

        // Check required attribute (boolean)
        MetaAttribute requiredAttr = field.getMetaAttr("required");
        assertNotNull("Required attribute should exist", requiredAttr);
        assertTrue("Required should be BooleanAttribute", requiredAttr instanceof BooleanAttribute);
        assertEquals("Required value should be true", "true", requiredAttr.getValueAsString());

        // Check defaultValue attribute (boolean)
        MetaAttribute defaultAttr = field.getMetaAttr("defaultValue");
        assertNotNull("DefaultValue attribute should exist", defaultAttr);
        assertTrue("DefaultValue should be BooleanAttribute", defaultAttr instanceof BooleanAttribute);
        assertEquals("DefaultValue should be false", "false", defaultAttr.getValueAsString());
    }

    /**
     * Test integer value parsing - should create IntAttribute
     */
    @Test
    public void testIntegerAttributeParsing() throws Exception {
        String json = """
        {
          "metadata": {
            "children": [
              {
                "object": {
                  "name": "Product",
                  "subType": "pojo",
                  "children": [
                    {
                      "field": {
                        "name": "quantity",
                        "subType": "int",
                        "@minValue": 1,
                        "@maxValue": 100,
                        "@defaultValue": 10
                      }
                    }
                  ]
                }
              }
            ]
          }
        }
        """;

        MetaObject result = parseJsonAndGetFirstObject(json);
        MetaField field = result.getMetaField("quantity");
        assertNotNull("Field should exist", field);

        // Check minValue attribute (integer)
        MetaAttribute minAttr = field.getMetaAttr("minValue");
        assertNotNull("MinValue attribute should exist", minAttr);
        assertTrue("MinValue should be IntAttribute", minAttr instanceof IntAttribute);
        assertEquals("MinValue should be 1", "1", minAttr.getValueAsString());

        // Check maxValue attribute (integer)
        MetaAttribute maxAttr = field.getMetaAttr("maxValue");
        assertNotNull("MaxValue attribute should exist", maxAttr);
        assertTrue("MaxValue should be IntAttribute", maxAttr instanceof IntAttribute);
        assertEquals("MaxValue should be 100", "100", maxAttr.getValueAsString());

        // Check defaultValue attribute (integer)
        MetaAttribute defaultAttr = field.getMetaAttr("defaultValue");
        assertNotNull("DefaultValue attribute should exist", defaultAttr);
        assertTrue("DefaultValue should be IntAttribute", defaultAttr instanceof IntAttribute);
        assertEquals("DefaultValue should be 10", "10", defaultAttr.getValueAsString());
    }

    /**
     * Test long value parsing - should create LongAttribute
     */
    @Test
    public void testLongAttributeParsing() throws Exception {
        String json = """
        {
          "metadata": {
            "children": [
              {
                "object": {
                  "name": "LargeData",
                  "subType": "pojo",
                  "children": [
                    {
                      "field": {
                        "name": "bigNumber",
                        "subType": "long",
                        "@minValue": 2147483648,
                        "@maxValue": 9223372036854775807
                      }
                    }
                  ]
                }
              }
            ]
          }
        }
        """;

        MetaObject result = parseJsonAndGetFirstObject(json);
        MetaField field = result.getMetaField("bigNumber");
        assertNotNull("Field should exist", field);

        // Check minValue attribute (long)
        MetaAttribute minAttr = field.getMetaAttr("minValue");
        assertNotNull("MinValue attribute should exist", minAttr);
        assertTrue("MinValue should be LongAttribute", minAttr instanceof LongAttribute);
        assertEquals("MinValue should be 2147483648", "2147483648", minAttr.getValueAsString());

        // Check maxValue attribute (long)
        MetaAttribute maxAttr = field.getMetaAttr("maxValue");
        assertNotNull("MaxValue attribute should exist", maxAttr);
        assertTrue("MaxValue should be LongAttribute", maxAttr instanceof LongAttribute);
        assertEquals("MaxValue should be 9223372036854775807", "9223372036854775807", maxAttr.getValueAsString());
    }

    /**
     * Test double value parsing - should create DoubleAttribute
     */
    @Test
    public void testDoubleAttributeParsing() throws Exception {
        String json = """
        {
          "metadata": {
            "children": [
              {
                "object": {
                  "name": "PriceData",
                  "subType": "pojo",
                  "children": [
                    {
                      "field": {
                        "name": "price",
                        "subType": "double",
                        "@minValue": 0.01,
                        "@maxValue": 999.99,
                        "@defaultValue": 1.0
                      }
                    }
                  ]
                }
              }
            ]
          }
        }
        """;

        MetaObject result = parseJsonAndGetFirstObject(json);
        MetaField field = result.getMetaField("price");
        assertNotNull("Field should exist", field);

        // Check minValue attribute (double)
        MetaAttribute minAttr = field.getMetaAttr("minValue");
        assertNotNull("MinValue attribute should exist", minAttr);
        assertTrue("MinValue should be DoubleAttribute", minAttr instanceof DoubleAttribute);
        assertEquals("MinValue should be 0.01", "0.01", minAttr.getValueAsString());

        // Check maxValue attribute (double)
        MetaAttribute maxAttr = field.getMetaAttr("maxValue");
        assertNotNull("MaxValue attribute should exist", maxAttr);
        assertTrue("MaxValue should be DoubleAttribute", maxAttr instanceof DoubleAttribute);
        assertEquals("MaxValue should be 999.99", "999.99", maxAttr.getValueAsString());

        // Check defaultValue attribute (double)
        MetaAttribute defaultAttr = field.getMetaAttr("defaultValue");
        assertNotNull("DefaultValue attribute should exist", defaultAttr);
        assertTrue("DefaultValue should be DoubleAttribute", defaultAttr instanceof DoubleAttribute);
        assertEquals("DefaultValue should be 1.0", "1.0", defaultAttr.getValueAsString());
    }

    /**
     * Test string value parsing - should create StringAttribute
     */
    @Test
    public void testStringAttributeParsing() throws Exception {
        String json = """
        {
          "metadata": {
            "children": [
              {
                "object": {
                  "name": "UserData",
                  "subType": "pojo",
                  "children": [
                    {
                      "field": {
                        "name": "username",
                        "subType": "string",
                        "@pattern": "^[a-zA-Z0-9_]+$",
                        "@description": "User login name",
                        "@defaultValue": "guest"
                      }
                    }
                  ]
                }
              }
            ]
          }
        }
        """;

        MetaObject result = parseJsonAndGetFirstObject(json);
        MetaField field = result.getMetaField("username");
        assertNotNull("Field should exist", field);

        // Check pattern attribute (string)
        MetaAttribute patternAttr = field.getMetaAttr("pattern");
        assertNotNull("Pattern attribute should exist", patternAttr);
        assertTrue("Pattern should be StringAttribute", patternAttr instanceof StringAttribute);
        assertEquals("Pattern should match", "^[a-zA-Z0-9_]+$", patternAttr.getValueAsString());

        // Check description attribute (string)
        MetaAttribute descAttr = field.getMetaAttr("description");
        assertNotNull("Description attribute should exist", descAttr);
        assertTrue("Description should be StringAttribute", descAttr instanceof StringAttribute);
        assertEquals("Description should match", "User login name", descAttr.getValueAsString());

        // Check defaultValue attribute (string)
        MetaAttribute defaultAttr = field.getMetaAttr("defaultValue");
        assertNotNull("DefaultValue attribute should exist", defaultAttr);
        assertTrue("DefaultValue should be StringAttribute", defaultAttr instanceof StringAttribute);
        assertEquals("DefaultValue should be guest", "guest", defaultAttr.getValueAsString());
    }


    /**
     * Test mixed value types in same object
     */
    @Test
    public void testMixedValueTypes() throws Exception {
        String json = """
        {
          "metadata": {
            "children": [
              {
                "object": {
                  "name": "MixedData",
                  "subType": "pojo",
                  "children": [
                    {
                      "field": {
                        "name": "mixedField",
                        "subType": "string",
                        "@required": true,
                        "@maxLength": 50,
                        "@priority": 1.5,
                        "@description": "A field with mixed attribute types"
                      }
                    }
                  ]
                }
              }
            ]
          }
        }
        """;

        MetaObject result = parseJsonAndGetFirstObject(json);
        MetaField field = result.getMetaField("mixedField");
        assertNotNull("Field should exist", field);

        // Boolean attribute
        MetaAttribute requiredAttr = field.getMetaAttr("required");
        assertTrue("Required should be BooleanAttribute", requiredAttr instanceof BooleanAttribute);

        // Integer attribute
        MetaAttribute maxLengthAttr = field.getMetaAttr("maxLength");
        assertTrue("MaxLength should be IntAttribute", maxLengthAttr instanceof IntAttribute);

        // Double attribute
        MetaAttribute priorityAttr = field.getMetaAttr("priority");
        assertTrue("Priority should be DoubleAttribute", priorityAttr instanceof DoubleAttribute);

        // String attribute
        MetaAttribute descAttr = field.getMetaAttr("description");
        assertTrue("Description should be StringAttribute", descAttr instanceof StringAttribute);
    }

    /**
     * Test edge cases: null, empty string, zero values
     */
    @Test
    public void testEdgeCases() throws Exception {
        String json = """
        {
          "metadata": {
            "children": [
              {
                "object": {
                  "name": "EdgeData",
                  "subType": "pojo",
                  "children": [
                    {
                      "field": {
                        "name": "edgeField",
                        "subType": "string",
                        "@emptyString": "",
                        "@zeroInt": 0,
                        "@zeroDouble": 0.0,
                        "@falseValue": false
                      }
                    }
                  ]
                }
              }
            ]
          }
        }
        """;

        MetaObject result = parseJsonAndGetFirstObject(json);
        MetaField field = result.getMetaField("edgeField");
        assertNotNull("Field should exist", field);

        // Empty string
        MetaAttribute emptyAttr = field.getMetaAttr("emptyString");
        assertNotNull("EmptyString attribute should exist", emptyAttr);
        assertTrue("EmptyString should be StringAttribute", emptyAttr instanceof StringAttribute);
        assertEquals("EmptyString should be empty", "", emptyAttr.getValueAsString());

        // Zero integer
        MetaAttribute zeroIntAttr = field.getMetaAttr("zeroInt");
        assertNotNull("ZeroInt attribute should exist", zeroIntAttr);
        assertTrue("ZeroInt should be IntAttribute", zeroIntAttr instanceof IntAttribute);
        assertEquals("ZeroInt should be 0", "0", zeroIntAttr.getValueAsString());

        // Zero double
        MetaAttribute zeroDoubleAttr = field.getMetaAttr("zeroDouble");
        assertNotNull("ZeroDouble attribute should exist", zeroDoubleAttr);
        assertTrue("ZeroDouble should be DoubleAttribute", zeroDoubleAttr instanceof DoubleAttribute);
        assertEquals("ZeroDouble should be 0.0", "0.0", zeroDoubleAttr.getValueAsString());

        // False boolean
        MetaAttribute falseAttr = field.getMetaAttr("falseValue");
        assertNotNull("FalseValue attribute should exist", falseAttr);
        assertTrue("FalseValue should be BooleanAttribute", falseAttr instanceof BooleanAttribute);
        assertEquals("FalseValue should be false", "false", falseAttr.getValueAsString());
    }

    /**
     * Helper method to parse JSON and return the first MetaObject
     */
    private MetaObject parseJsonAndGetFirstObject(String json) throws Exception {
        // Create test loader
        SimpleLoader loader = createTestLoader("JsonParserTest", Collections.emptyList());

        // Parse JSON using JsonMetaDataParser
        JsonMetaDataParser parser = new JsonMetaDataParser(loader, "test.json");
        InputStream is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        parser.loadFromStream(is);

        // Return first object
        Collection<MetaObject> objects = loader.getChildren(MetaObject.class);
        assertFalse("Should have at least one object", objects.isEmpty());
        return objects.iterator().next();
    }
}