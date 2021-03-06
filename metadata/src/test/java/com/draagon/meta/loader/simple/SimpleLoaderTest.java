package com.draagon.meta.loader.simple;

import com.draagon.meta.loader.types.TypesConfig;
import com.draagon.meta.loader.types.TypesConfigLoader;
import com.draagon.meta.loader.uri.URIHelper;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public class SimpleLoaderTest extends SimpleLoaderTestBase {

    @Test
    public void testLoadSimpleTypes() throws URISyntaxException {

        initLoader(Arrays.asList(
                URIHelper.toURI( "model:resource:com/draagon/meta/loader/simple/fruitbasket-metadata.xml" )
        ));
    }
}
