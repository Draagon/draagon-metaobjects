package com.draagon.meta.loader.object.value;

import com.draagon.meta.object.value.ValueObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class ValueObjectTests {

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
}
