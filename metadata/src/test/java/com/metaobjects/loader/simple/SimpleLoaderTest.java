package com.metaobjects.loader.simple;

import com.metaobjects.loader.uri.URIHelper;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.Arrays;

public class SimpleLoaderTest extends SimpleLoaderTestBase {

    @Test
    public void testLoadSimpleTypes() throws URISyntaxException {

        initLoader(Arrays.asList(
                URIHelper.toURI( "model:resource:com/metaobjects/loader/simple/fruitbasket-metadata.json" )
        ));
    }

}
