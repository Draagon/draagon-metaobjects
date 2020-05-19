package com.draagon.meta.loader.simple;

import com.draagon.meta.loader.types.TypesConfig;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class SimpleLoaderTest {

    @Test
    public void testLoadSimpleTypes() throws URISyntaxException {

        TypesConfig tc = new TypesConfig();
        String s = tc.toString();

        SimpleLoader loader = new SimpleLoader("test1" );
        loader.setSourceURI( new URI( "model:resource:com/draagon/meta/loader/simple/fruitbasket-metadata.xml" ));
        loader.init();
    }
}
