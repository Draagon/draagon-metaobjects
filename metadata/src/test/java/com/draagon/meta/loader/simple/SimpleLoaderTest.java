package com.draagon.meta.loader.simple;

import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.config.TypesConfig;
import com.draagon.meta.loader.config.TypesConfigBuilder;
import org.junit.Test;

public class SimpleLoaderTest {

    @Test
    public void testLoadSimpleTypes() {

        SimpleLoader loader = new SimpleLoader("test1" );
        loader.init();
    }
}
