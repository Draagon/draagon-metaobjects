package com.metaobjects.loader.parser;

import com.metaobjects.MetaData;
import com.metaobjects.attr.*;
import com.metaobjects.field.*;
import com.metaobjects.identity.PrimaryIdentity;
import com.metaobjects.loader.parser.json.JsonMetaDataParser;
import com.metaobjects.loader.parser.xml.XMLMetaDataParser;
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
 * Comprehensive test suite for comparing JSON and XML parser behavior
 * and testing error handling scenarios.
 */
public class ParserComparisonTest extends SharedRegistryTestBase {

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
     * Test that JSON and XML parsers produce equivalent results for the same logical content
     */
    @Test
    public void testJsonXmlEquivalence() throws Exception {
        // Define equivalent content in both formats
        String json = """
        {
          "metadata": {
            "children": [
              {
                "field": {
                  "name": "testField",
                  "subType": "string",
                  "@required": true,
                  "@maxLength": 100,
                  "@priority": 3.14,
                  "@description": "Test field for comparison"
                }
              }
            ]
          }
        }
        """;

        String xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <metadata>
          <field name="testField" subType="string" required="true" maxLength="100" priority="3.14" description="Test field for comparison" />
        </metadata>
        """;

        // Parse both formats - but expect fields, not objects for standalone field definitions
        MetaField jsonField = parseJsonAndGetFirstField(json);
        MetaField xmlField = parseXmlAndGetFirstField(xml);

        assertNotNull("JSON field should exist", jsonField);
        assertNotNull("XML field should exist", xmlField);
        assertEquals("Both fields should have the same name", "testField", jsonField.getName());
        assertEquals("Both fields should have the same name", "testField", xmlField.getName());

        // Verify attribute types are identical
        verifyAttributeTypesMatch(jsonField, xmlField, "required", BooleanAttribute.class);
        verifyAttributeTypesMatch(jsonField, xmlField, "maxLength", IntAttribute.class);
        verifyAttributeTypesMatch(jsonField, xmlField, "priority", DoubleAttribute.class);
        verifyAttributeTypesMatch(jsonField, xmlField, "description", StringAttribute.class);

        // Verify attribute values are identical
        verifyAttributeValuesMatch(jsonField, xmlField, "required", "true");
        verifyAttributeValuesMatch(jsonField, xmlField, "maxLength", "100");
        verifyAttributeValuesMatch(jsonField, xmlField, "priority", "3.14");
        verifyAttributeValuesMatch(jsonField, xmlField, "description", "Test field for comparison");
    }

    /**
     * Test JSON array parsing (XML doesn't have direct array syntax)
     */
    @Test
    public void testJsonArrayHandling() throws Exception {
        String json = """
        {
          "metadata": {
            "children": [
              {
                "identity": {
                  "name": "primary",
                  "subType": "primary",
                  "@fields": ["id", "name"],
                  "@singleField": ["code"]
                }
              }
            ]
          }
        }
        """;

        PrimaryIdentity identity = parseJsonAndGetFirstIdentity(json);
        assertNotNull("Identity should exist", identity);
        assertEquals("Identity should have correct name", "primary", identity.getName());

        // Multi-value array
        MetaAttribute fieldsAttr = identity.getMetaAttr("fields");
        assertNotNull("Fields attribute should exist", fieldsAttr);
        assertTrue("Fields should be StringAttribute", fieldsAttr instanceof StringAttribute);
        assertEquals("Fields value should be comma-delimited", "id,name", fieldsAttr.getValueAsString());

        // Check native isArray property instead of MetaAttribute child
        assertTrue("Fields should have native isArray=true", fieldsAttr.isArray());

        // Single-value array
        MetaAttribute singleFieldAttr = identity.getMetaAttr("singleField");
        assertNotNull("SingleField attribute should exist", singleFieldAttr);
        assertTrue("SingleField should be StringAttribute", singleFieldAttr instanceof StringAttribute);
        assertEquals("SingleField value should be single item", "code", singleFieldAttr.getValueAsString());

        // Check native isArray property instead of MetaAttribute child
        assertTrue("SingleField should have native isArray=true", singleFieldAttr.isArray());
    }

    /**
     * Test numeric edge cases and boundary values
     */
    @Test
    public void testNumericEdgeCases() throws Exception {
        String json = """
        {
          "metadata": {
            "children": [
              {
                "field": {
                  "name": "numericField",
                  "subType": "string",
                  "@maxInt": 2147483647,
                  "@minInt": -2147483648,
                  "@maxLong": 9223372036854775807,
                  "@minLong": -9223372036854775808,
                  "@maxDouble": 1.7976931348623157E308,
                  "@minDouble": 4.9E-324,
                  "@negativeDouble": -123.456
                }
              }
            ]
          }
        }
        """;

        MetaField field = parseJsonAndGetFirstField(json);
        assertNotNull("Field should exist", field);
        assertEquals("Field should have correct name", "numericField", field.getName());

        // Integer boundaries
        MetaAttribute maxIntAttr = field.getMetaAttr("maxInt");
        assertTrue("MaxInt should be IntAttribute", maxIntAttr instanceof IntAttribute);
        assertEquals("MaxInt value", "2147483647", maxIntAttr.getValueAsString());

        MetaAttribute minIntAttr = field.getMetaAttr("minInt");
        assertTrue("MinInt should be IntAttribute", minIntAttr instanceof IntAttribute);
        assertEquals("MinInt value", "-2147483648", minIntAttr.getValueAsString());

        // Long boundaries
        MetaAttribute maxLongAttr = field.getMetaAttr("maxLong");
        assertTrue("MaxLong should be LongAttribute", maxLongAttr instanceof LongAttribute);
        assertEquals("MaxLong value", "9223372036854775807", maxLongAttr.getValueAsString());

        MetaAttribute minLongAttr = field.getMetaAttr("minLong");
        assertTrue("MinLong should be LongAttribute", minLongAttr instanceof LongAttribute);
        assertEquals("MinLong value", "-9223372036854775808", minLongAttr.getValueAsString());

        // Double boundaries and negative values
        MetaAttribute maxDoubleAttr = field.getMetaAttr("maxDouble");
        assertTrue("MaxDouble should be DoubleAttribute", maxDoubleAttr instanceof DoubleAttribute);

        MetaAttribute minDoubleAttr = field.getMetaAttr("minDouble");
        assertTrue("MinDouble should be DoubleAttribute", minDoubleAttr instanceof DoubleAttribute);

        MetaAttribute negativeDoubleAttr = field.getMetaAttr("negativeDouble");
        assertTrue("NegativeDouble should be DoubleAttribute", negativeDoubleAttr instanceof DoubleAttribute);
        assertEquals("NegativeDouble value", "-123.456", negativeDoubleAttr.getValueAsString());
    }

    /**
     * Test string edge cases and special characters
     */
    @Test
    public void testStringEdgeCases() throws Exception {
        String json = """
        {
          "metadata": {
            "children": [
              {
                "field": {
                  "name": "stringField",
                  "subType": "string",
                  "@empty": "",
                  "@whitespace": "   ",
                  "@special": "!@#$%^&*()_+-={}[]|\\\\:;'\\\"<>?,./",
                  "@unicode": "éñ中文🚀",
                  "@newlines": "line1\\nline2\\ttab"
                }
              }
            ]
          }
        }
        """;

        MetaField field = parseJsonAndGetFirstField(json);
        assertNotNull("Field should exist", field);
        assertEquals("Field should have correct name", "stringField", field.getName());

        // Empty string
        MetaAttribute emptyAttr = field.getMetaAttr("empty");
        assertTrue("Empty should be StringAttribute", emptyAttr instanceof StringAttribute);
        assertEquals("Empty value", "", emptyAttr.getValueAsString());

        // Whitespace
        MetaAttribute whitespaceAttr = field.getMetaAttr("whitespace");
        assertTrue("Whitespace should be StringAttribute", whitespaceAttr instanceof StringAttribute);
        assertEquals("Whitespace value", "   ", whitespaceAttr.getValueAsString());

        // Special characters
        MetaAttribute specialAttr = field.getMetaAttr("special");
        assertTrue("Special should be StringAttribute", specialAttr instanceof StringAttribute);
        assertEquals("Special value", "!@#$%^&*()_+-={}[]|\\:;'\"<>?,./", specialAttr.getValueAsString());

        // Unicode characters
        MetaAttribute unicodeAttr = field.getMetaAttr("unicode");
        assertTrue("Unicode should be StringAttribute", unicodeAttr instanceof StringAttribute);
        assertEquals("Unicode value", "éñ中文🚀", unicodeAttr.getValueAsString());
    }

    /**
     * Test boolean edge cases
     */
    @Test
    public void testBooleanEdgeCases() throws Exception {
        String json = """
        {
          "metadata": {
            "children": [
              {
                "field": {
                  "name": "boolField",
                  "subType": "string",
                  "@trueValue": true,
                  "@falseValue": false,
                  "@trueString": "true",
                  "@falseString": "false",
                  "@invalidBool": "maybe"
                }
              }
            ]
          }
        }
        """;

        MetaField field = parseJsonAndGetFirstField(json);
        assertNotNull("Field should exist", field);
        assertEquals("Field should have correct name", "boolField", field.getName());

        // JSON boolean values
        MetaAttribute trueAttr = field.getMetaAttr("trueValue");
        assertTrue("TrueValue should be BooleanAttribute", trueAttr instanceof BooleanAttribute);
        assertEquals("TrueValue should be true", "true", trueAttr.getValueAsString());

        MetaAttribute falseAttr = field.getMetaAttr("falseValue");
        assertTrue("FalseValue should be BooleanAttribute", falseAttr instanceof BooleanAttribute);
        assertEquals("FalseValue should be false", "false", falseAttr.getValueAsString());

        // String boolean values
        MetaAttribute trueStringAttr = field.getMetaAttr("trueString");
        assertTrue("TrueString should be BooleanAttribute", trueStringAttr instanceof BooleanAttribute);
        assertEquals("TrueString should be true", "true", trueStringAttr.getValueAsString());

        MetaAttribute falseStringAttr = field.getMetaAttr("falseString");
        assertTrue("FalseString should be BooleanAttribute", falseStringAttr instanceof BooleanAttribute);
        assertEquals("FalseString should be false", "false", falseStringAttr.getValueAsString());

        // Invalid boolean - should be treated as string
        MetaAttribute invalidAttr = field.getMetaAttr("invalidBool");
        assertTrue("InvalidBool should be StringAttribute", invalidAttr instanceof StringAttribute);
        assertEquals("InvalidBool should be maybe", "maybe", invalidAttr.getValueAsString());
    }

    /**
     * Test the MetaAttribute-first approach is working correctly
     */
    @Test
    public void testMetaAttributeFirstApproach() throws Exception {
        String json = """
        {
          "metadata": {
            "children": [
              {
                "field": {
                  "name": "testField",
                  "subType": "string",
                  "@authRequired": true,
                  "@instanceCount": 5,
                  "@configValue": "production"
                }
              }
            ]
          }
        }
        """;

        MetaField field = parseJsonAndGetFirstField(json);
        assertNotNull("Field should exist", field);
        assertEquals("Field should have correct name", "testField", field.getName());

        // This is the original problem scenario - these should now create the correct attribute types
        MetaAttribute authAttr = field.getMetaAttr("authRequired");
        assertNotNull("AuthRequired should exist", authAttr);
        assertTrue("AuthRequired should be BooleanAttribute (not StringAttribute)", authAttr instanceof BooleanAttribute);
        assertEquals("AuthRequired value", "true", authAttr.getValueAsString());

        MetaAttribute instanceAttr = field.getMetaAttr("instanceCount");
        assertNotNull("InstanceCount should exist", instanceAttr);
        assertTrue("InstanceCount should be IntAttribute (not StringAttribute)", instanceAttr instanceof IntAttribute);
        assertEquals("InstanceCount value", "5", instanceAttr.getValueAsString());

        MetaAttribute configAttr = field.getMetaAttr("configValue");
        assertNotNull("ConfigValue should exist", configAttr);
        assertTrue("ConfigValue should be StringAttribute", configAttr instanceof StringAttribute);
        assertEquals("ConfigValue value", "production", configAttr.getValueAsString());
    }

    /**
     * Helper method to verify attribute types match between JSON and XML parsing
     */
    private void verifyAttributeTypesMatch(MetaField jsonField, MetaField xmlField,
                                          String attrName, Class<? extends MetaAttribute> expectedType) {
        MetaAttribute jsonAttr = jsonField.getMetaAttr(attrName);
        MetaAttribute xmlAttr = xmlField.getMetaAttr(attrName);

        assertNotNull("JSON " + attrName + " should exist", jsonAttr);
        assertNotNull("XML " + attrName + " should exist", xmlAttr);

        assertTrue("JSON " + attrName + " should be " + expectedType.getSimpleName(),
                  expectedType.isInstance(jsonAttr));
        assertTrue("XML " + attrName + " should be " + expectedType.getSimpleName(),
                  expectedType.isInstance(xmlAttr));
    }

    /**
     * Helper method to verify attribute values match between JSON and XML parsing
     */
    private void verifyAttributeValuesMatch(MetaField jsonField, MetaField xmlField,
                                           String attrName, String expectedValue) {
        MetaAttribute jsonAttr = jsonField.getMetaAttr(attrName);
        MetaAttribute xmlAttr = xmlField.getMetaAttr(attrName);

        assertEquals("JSON " + attrName + " value should match", expectedValue, jsonAttr.getValueAsString());
        assertEquals("XML " + attrName + " value should match", expectedValue, xmlAttr.getValueAsString());
    }

    /**
     * Helper method to parse JSON and return the first MetaField
     */
    private MetaField parseJsonAndGetFirstField(String json) throws Exception {
        SimpleLoader loader = createTestLoader("JsonComparisonTest", Collections.emptyList());
        JsonMetaDataParser parser = new JsonMetaDataParser(loader, "test.json");
        InputStream is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        parser.loadFromStream(is);

        Collection<MetaField> fields = loader.getChildren(MetaField.class);
        assertFalse("Should have at least one field", fields.isEmpty());
        return fields.iterator().next();
    }

    /**
     * Helper method to parse JSON and return the first PrimaryIdentity
     */
    private PrimaryIdentity parseJsonAndGetFirstIdentity(String json) throws Exception {
        SimpleLoader loader = createTestLoader("JsonComparisonTest", Collections.emptyList());
        JsonMetaDataParser parser = new JsonMetaDataParser(loader, "test.json");
        InputStream is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        parser.loadFromStream(is);

        Collection<PrimaryIdentity> identities = loader.getChildren(PrimaryIdentity.class);
        assertFalse("Should have at least one identity", identities.isEmpty());
        return identities.iterator().next();
    }

    /**
     * Helper method to parse JSON and return the first MetaObject (deprecated - use parseJsonAndGetFirstField for field tests)
     */
    private MetaObject parseJsonAndGetFirstObject(String json) throws Exception {
        SimpleLoader loader = createTestLoader("JsonComparisonTest", Collections.emptyList());
        JsonMetaDataParser parser = new JsonMetaDataParser(loader, "test.json");
        InputStream is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        parser.loadFromStream(is);

        // DEBUG: Check what children were actually created
        Collection<MetaData> allChildren = loader.getChildren();
        System.out.println("DEBUG JSON: All children found: " + allChildren.size());
        for (MetaData child : allChildren) {
            System.out.println("  - " + child.getClass().getSimpleName() + ": " + child.getName() + " (" + child.getType() + ":" + child.getSubType() + ")");
        }

        Collection<MetaObject> objects = loader.getChildren(MetaObject.class);
        if (objects.isEmpty()) {
            System.out.println("DEBUG JSON: No objects found, allowing test to continue to check XML");
            return null; // Return null instead of failing to allow further testing
        }
        return objects.iterator().next();
    }

    /**
     * Helper method to parse XML and return the first MetaField
     */
    private MetaField parseXmlAndGetFirstField(String xml) throws Exception {
        SimpleLoader loader = createTestLoader("XmlComparisonTest", Collections.emptyList());
        XMLMetaDataParser parser = new XMLMetaDataParser(loader, "test.xml");
        InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        parser.loadFromStream(is);

        Collection<MetaField> fields = loader.getChildren(MetaField.class);
        assertFalse("Should have at least one field", fields.isEmpty());
        return fields.iterator().next();
    }

    /**
     * Helper method to parse XML and return the first MetaObject (deprecated - use parseXmlAndGetFirstField for field tests)
     */
    private MetaObject parseXmlAndGetFirstObject(String xml) throws Exception {
        SimpleLoader loader = createTestLoader("XmlComparisonTest", Collections.emptyList());
        XMLMetaDataParser parser = new XMLMetaDataParser(loader, "test.xml");
        InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        parser.loadFromStream(is);

        // DEBUG: Check what children were actually created
        Collection<MetaData> allChildren = loader.getChildren();
        System.out.println("DEBUG XML: All children found: " + allChildren.size());
        for (MetaData child : allChildren) {
            System.out.println("  - " + child.getClass().getSimpleName() + ": " + child.getName() + " (" + child.getType() + ":" + child.getSubType() + ")");
        }

        Collection<MetaObject> objects = loader.getChildren(MetaObject.class);
        if (objects.isEmpty()) {
            System.out.println("DEBUG XML: No objects found either");
            return null; // Return null instead of failing to allow further testing
        }
        return objects.iterator().next();
    }
}