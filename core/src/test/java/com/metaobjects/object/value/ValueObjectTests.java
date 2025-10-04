package com.metaobjects.object.value;

import com.metaobjects.attr.BooleanAttribute;
import com.metaobjects.field.MetaField;
import com.metaobjects.field.StringField;
import com.metaobjects.object.MetaObject;
import com.metaobjects.object.pojo.PojoMetaObject;
import com.metaobjects.registry.SharedRegistryTestBase;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ValueObjectTests extends SharedRegistryTestBase {

    @Test
    public void testLooseValueObject() {

        ValueObject v1 = new ValueObject( "one");
        ValueObject v2 = new ValueObject( "two");

        assertNotSame( "v1.name != v2.name", v1, v2 );

        v2 = new ValueObject( "one");
        assertEquals( "v1.name == v2.name", v1, v2 );

        v1.setString( "hello", "world");
        assertNotSame( "v1.fields != v2.fields", v1, v2 );

        v2.setString( "hello", "world");
        assertEquals( "v1 == v2 (hello)", v1, v2 );

        v1.setString( "length", "10");
        v2.setInt( "length", 10);
        assertNotSame( "v1.fields != v2.fields", v1, v2 );

        v2.setString( "length", "10");
        assertNotSame( "v1.fields != v2.fields", v1, v2 );

        assertTrue( "v2.length is int", v2.getObjectAttribute("length") instanceof Integer);
        assertTrue( "v1.length is String", v1.getObjectAttribute("length") instanceof String);

        v2.remove( "length" );
        v2.setString( "length", "10");
        assertEquals( "v1 == v2 (remove/add length)", v1, v2 );
    }

    @Test
    public void testArrayFieldsWithMetaData() {
        // Create a MetaObject with array fields
        PojoMetaObject metaObject = new PojoMetaObject("TestObject");

        // Create regular string field
        StringField nameField = new StringField("name");
        metaObject.addChild(nameField);

        // Create array string field
        StringField tagsField = new StringField("tags");
        tagsField.addMetaAttr(BooleanAttribute.create("isArray", true)); // Mark as array
        metaObject.addChild(tagsField);

        // Create ValueObject with MetaData
        ValueObject obj = new ValueObject(metaObject);

        // Test array field with List input
        List<String> tagsList = Arrays.asList("important", "urgent", "review");
        obj.setStringArray("tags", tagsList);

        // Test array field retrieval
        List<String> retrievedTags = obj.getStringArray("tags");
        assertEquals("Array field should return list", tagsList, retrievedTags);

        // Test smart fallback: getString on array field should return comma-delimited
        String tagsAsString = obj.getString("tags");
        assertEquals("Array field as string should be comma-delimited", "important,urgent,review", tagsAsString);

        // Test setting single value on array field (should convert to array)
        obj.setString("tags", "single");
        List<String> singleTagList = obj.getStringArray("tags");
        assertEquals("Single value should convert to array", Arrays.asList("single"), singleTagList);

        // Test setting comma-delimited string on array field
        obj.setString("tags", "one,two,three");
        List<String> parsedTags = obj.getStringArray("tags");
        assertEquals("Comma-delimited string should parse to array", Arrays.asList("one", "two", "three"), parsedTags);

        // Test regular (non-array) field
        obj.setString("name", "TestName");
        assertEquals("Regular field should work normally", "TestName", obj.getString("name"));
    }

    @Test
    public void testArrayFieldsWithoutMetaData() {
        // Test array behavior when no MetaData is attached (best-effort mode)
        ValueObject obj = new ValueObject("TestObjectLoose");

        // Set a list value
        List<String> tagsList = Arrays.asList("tag1", "tag2", "tag3");
        obj.setStringArray("tags", tagsList);

        // Best-effort detection should recognize this as array
        List<String> retrievedTags = obj.getStringArray("tags");
        assertEquals("Array should be stored and retrieved", tagsList, retrievedTags);

        // Smart fallback should still work
        String tagsAsString = obj.getString("tags");
        assertEquals("Array as string should be comma-delimited", "tag1,tag2,tag3", tagsAsString);

        // Test other primitive array types
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4);
        obj.setIntArray("numbers", numbers);

        List<Integer> retrievedNumbers = obj.getIntArray("numbers");
        assertEquals("Integer array should work", numbers, retrievedNumbers);

        // Test smart fallback for integer array
        Integer firstNumber = obj.getInt("numbers");
        assertEquals("First element should be returned for primitive getter", Integer.valueOf(1), firstNumber);
    }

    @Test
    public void testMixedArrayAndRegularFields() {
        ValueObject obj = new ValueObject("MixedTest");

        // Set regular fields
        obj.setString("name", "John");
        obj.setInt("age", 30);

        // Set array fields
        obj.setStringArray("hobbies", Arrays.asList("reading", "coding"));
        obj.setIntArray("scores", Arrays.asList(85, 90, 78));

        // Verify regular fields work normally
        assertEquals("Regular string field", "John", obj.getString("name"));
        assertEquals("Regular int field", Integer.valueOf(30), obj.getInt("age"));

        // Verify array fields work correctly
        assertEquals("String array field", Arrays.asList("reading", "coding"), obj.getStringArray("hobbies"));
        assertEquals("Int array field", Arrays.asList(85, 90, 78), obj.getIntArray("scores"));

        // Verify smart fallbacks
        assertEquals("Array as string", "reading,coding", obj.getString("hobbies"));
        assertEquals("Array first element", Integer.valueOf(85), obj.getInt("scores"));
    }
}
