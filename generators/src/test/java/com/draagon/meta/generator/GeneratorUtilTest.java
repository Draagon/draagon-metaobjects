package com.draagon.meta.generator;

import static org.junit.Assert.*;
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
}
