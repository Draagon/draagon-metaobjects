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
        assertFalse( filterMatch( "acme::store::PrimaryStore", "acme::store::??????Store" ));
        assertTrue( filterMatch( "acme::store::PrimaryStore", "acme::*::*Store" ));
        // TODO:  Determine if this should be true or false
        assertTrue( filterMatch( "acme::store::bad::PrimaryStore", "acme::*::*Store" ));
        assertFalse( filterMatch( "acme::store::PrimaryStore", "acme::*::*StoreZ" ));
        assertFalse( filterMatch( "acme::store::PrimaryStore", "bobs::*" ));
    }
}
