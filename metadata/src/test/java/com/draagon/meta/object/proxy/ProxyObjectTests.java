package com.draagon.meta.object.proxy;

import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.loader.uri.URIHelper;
import com.draagon.meta.test.proxy.fruitbasket.Apple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Arrays;

public class ProxyObjectTests {

    protected MetaDataLoader loader = null;

    @Before
    public void initLoader() throws ClassNotFoundException {
        loader = SimpleLoader.createManual("proxytest", Arrays.asList(
                //"com/draagon/meta/loader/simple/fruitbasket-metadata.xml",
                "com/draagon/meta/loader/simple/fruitbasket-proxy-metadata.xml"
        ));
    }

    @Test
    public void appleTest() throws ClassNotFoundException {
        Apple apple = loader.newObjectInstance(Apple.class);
        apple.setId(5l);
        Long id = apple.getId();
        Long five = 5l;
        Assert.assertEquals(five,id);
    }
}
