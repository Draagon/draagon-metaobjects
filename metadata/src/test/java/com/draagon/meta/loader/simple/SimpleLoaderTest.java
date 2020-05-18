package com.draagon.meta.loader.simple;

import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.config.TypesConfig;
import com.draagon.meta.loader.config.TypesConfigBuilder;
import org.junit.Test;

public class SimpleLoaderTest {

    @Test
    public void testLoadSimpleTypes() {

        TypesConfig tc = new TypesConfig();
        String s = tc.toString();

        SimpleLoader loader = new SimpleLoader("test1" );
        loader.setSourceData( "com/draagon/meta/loader/simple/fruitbasket-metadata.xml" );
        loader.init();
    }
}
