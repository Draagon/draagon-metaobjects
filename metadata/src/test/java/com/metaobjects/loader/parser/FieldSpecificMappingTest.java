package com.metaobjects.loader.parser;

import com.metaobjects.MetaData;
import com.metaobjects.attr.BooleanAttribute;
import com.metaobjects.attr.DoubleAttribute;
import com.metaobjects.attr.IntAttribute;
import com.metaobjects.attr.MetaAttribute;
import com.metaobjects.field.DoubleField;
import com.metaobjects.field.IntegerField;
import com.metaobjects.field.StringField;
import com.metaobjects.loader.simple.SimpleLoader;
import com.metaobjects.loader.parser.json.JsonMetaDataParser;
import com.metaobjects.object.MetaObject;
import com.metaobjects.registry.SharedRegistryTestBase;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Test field-specific attribute type mapping
 */
public class FieldSpecificMappingTest extends SharedRegistryTestBase {

    @Test
    public void testContextAwareAttributeCreation() throws Exception {
        // Test field-specific attribute type mapping with the same pattern as ParserComparisonTest
        String json = """
        {
          "metadata": {
            "children": [
              {
                "field": {
                  "name": "intField",
                  "subType": "int",
                  "@maxValue": 100,
                  "@required": true
                }
              },
              {
                "field": {
                  "name": "doubleField",
                  "subType": "double",
                  "@maxValue": 100.5,
                  "@priority": 3.14,
                  "@required": true
                }
              },
              {
                "field": {
                  "name": "stringField",
                  "subType": "string",
                  "@maxLength": 50,
                  "@required": true
                }
              }
            ]
          }
        }
        """;

        // Parse the JSON using the same pattern as ParserComparisonTest
        SimpleLoader loader = createTestLoader("FieldSpecificMappingTest", Collections.emptyList());
        JsonMetaDataParser parser = new JsonMetaDataParser(loader, "test.json");
        InputStream is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        parser.loadFromStream(is);

        // Test IntegerField - maxValue should be IntAttribute
        IntegerField intField = (IntegerField) loader.getChildOfType("field", "intField");
        assertNotNull("intField should exist", intField);

        MetaAttribute intMaxValue = intField.getMetaAttr("maxValue");
        assertNotNull("maxValue should exist on intField", intMaxValue);
        assertTrue("maxValue on IntegerField should be IntAttribute", intMaxValue instanceof IntAttribute);
        assertEquals("maxValue should be 100", "100", intMaxValue.getValueAsString());

        MetaAttribute intRequired = intField.getMetaAttr("required");
        assertNotNull("required should exist on intField", intRequired);
        assertTrue("required on IntegerField should be BooleanAttribute", intRequired instanceof BooleanAttribute);

        // Test DoubleField - maxValue and priority should be DoubleAttribute
        DoubleField doubleField = (DoubleField) loader.getChildOfType("field", "doubleField");
        assertNotNull("doubleField should exist", doubleField);

        MetaAttribute doubleMaxValue = doubleField.getMetaAttr("maxValue");
        assertNotNull("maxValue should exist on doubleField", doubleMaxValue);
        assertTrue("maxValue on DoubleField should be DoubleAttribute", doubleMaxValue instanceof DoubleAttribute);
        assertEquals("maxValue should be 100.5", "100.5", doubleMaxValue.getValueAsString());

        MetaAttribute doublePriority = doubleField.getMetaAttr("priority");
        assertNotNull("priority should exist on doubleField", doublePriority);
        assertTrue("priority on DoubleField should be DoubleAttribute", doublePriority instanceof DoubleAttribute);
        assertEquals("priority should be 3.14", "3.14", doublePriority.getValueAsString());

        MetaAttribute doubleRequired = doubleField.getMetaAttr("required");
        assertNotNull("required should exist on doubleField", doubleRequired);
        assertTrue("required on DoubleField should be BooleanAttribute", doubleRequired instanceof BooleanAttribute);

        // Test StringField - maxLength should be IntAttribute
        StringField stringField = (StringField) loader.getChildOfType("field", "stringField");
        assertNotNull("stringField should exist", stringField);

        MetaAttribute stringMaxLength = stringField.getMetaAttr("maxLength");
        assertNotNull("maxLength should exist on stringField", stringMaxLength);
        assertTrue("maxLength on StringField should be IntAttribute", stringMaxLength instanceof IntAttribute);
        assertEquals("maxLength should be 50", "50", stringMaxLength.getValueAsString());

        MetaAttribute stringRequired = stringField.getMetaAttr("required");
        assertNotNull("required should exist on stringField", stringRequired);
        assertTrue("required on StringField should be BooleanAttribute", stringRequired instanceof BooleanAttribute);
    }
}