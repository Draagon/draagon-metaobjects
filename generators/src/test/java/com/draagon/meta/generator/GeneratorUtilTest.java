package com.draagon.meta.generator;

import static org.junit.Assert.*;

import com.draagon.meta.generator.util.GeneratorUtil;
import org.junit.Test;

import static com.draagon.meta.generator.util.GeneratorUtil.*;

public class GeneratorUtilTest {

    @Test
    public void testFilterMatching() {
        assertTrue( filterMatch( "acme::store::PrimaryStore", "acme::*" ));
        assertTrue( filterMatch( "acme::store::PrimaryStore", "acme::store::PrimaryStore" ));
        assertTrue( filterMatch( "acme::store::PrimaryStore", "acme::store::???????Store" ));
        assertTrue( filterMatch( "acme::store::PrimaryStore", "acme::store::@Store" ));
        assertFalse( filterMatch( "acme::store::PrimaryStore", "acme::store::??????Store" ));
        assertTrue( filterMatch( "acme::store::PrimaryStore", "acme::*::*Store" ));
        assertTrue( filterMatch( "acme::store::good::PrimaryStore", "acme::*Store" ));
        assertTrue( filterMatch( "acme::store::good::PrimaryStore", "acme::*::*Store" ));
        assertTrue( filterMatch( "acme::store::PrimaryStore", "acme::@::@Store" ));
        assertFalse( filterMatch( "acme::store::bad::PrimaryStore", "acme::@::@Store" ));
        assertFalse( filterMatch( "acme::store::bad::PrimaryStore", "acme::store::@Store" ));
        assertTrue( filterMatch( "acme::store::PrimaryStore", "acme::store::Pri@Store" ));
        assertTrue( filterMatch( "acme::store::PrimaryStore", "acme::store::Pri*Store" ));
        assertFalse( filterMatch( "acme::store::PrimaryStore", "acme::*::*StoreZ" ));
        assertTrue( filterMatch( "acme::store::PrimaryStore", "acme::*::*Store*" ));
        assertFalse( filterMatch( "acme::store::PrimaryStore", "acme::*::*Store@" ));
        assertFalse( filterMatch( "acme::store::PrimaryStore", "acme::*::*Store?" ));
        assertFalse( filterMatch( "acme::store::PrimaryStore", "bobs::*" ));
    }

    @Test
    public void testToRelativePackage() {
        assertEquals( "::ext::", toRelativePackage( "produce::v1::container", "produce::v1::container::ext" ));
        assertEquals( "global::v1::common::", toRelativePackage( "produce::v1::container", "global::v1::common" ));
        assertEquals( "..::fruit::", toRelativePackage( "produce::v1::container", "produce::v1::fruit" ));
        assertEquals( "", toRelativePackage( "", "" ));
        assertEquals( "acme::store::", toRelativePackage( "", "acme::store" ));
        assertEquals( "", toRelativePackage( "acme::store", "" ));
        assertEquals( "::", toRelativePackage( "acme::store", "acme::store" ));
        assertEquals( "..::store::", toRelativePackage( "acme::floor", "acme::store" ));
        assertEquals( "...::store::", toRelativePackage( "acme::floor::first", "acme::store" ));
        assertEquals( "...::store::", toRelativePackage( "acme::floor::first::second", "acme::store" ));
        assertEquals( "..::floor::first::second::", toRelativePackage( "acme::store", "acme::floor::first::second" ));
    }

    @Test
    public void testToCamelCase() {
        assertEquals( "FruitBasket", toCamelCase( "fruit-basket", true ));
        assertEquals( "fruitBasket", toCamelCase( "fruit-basket", false ));
        assertEquals( "fruitBasket", toCamelCase( "fruit--basket", false ));
        assertEquals( "FruitBasket", toCamelCase( "-fruit-basket", false ));

        assertEquals( "OneTwoThreeFourFive", toCamelCase( "one-two-three-four-five", true ));
        assertEquals( "OneTwoThreeFourFive", toCamelCase( "oneTwoThree-four-five", true ));
    }
}
