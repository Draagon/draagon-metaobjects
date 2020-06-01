package com.draagon.meta.io.object.gson;

import com.draagon.meta.io.object.json.JsonObjectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.test.proxy.fruitbasket.Apple;
import com.draagon.meta.test.proxy.fruitbasket.BasketToFruit;
import com.draagon.meta.test.proxy.fruitbasket.Fruit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GsonAdapterTest {

    protected final String BASKET_TO_FRUIT = "simple::fruitbasket::BasketToFruit";
    protected MetaDataLoader loader = null;
    protected GsonBuilder builder = null;

    @Before
    public void initLoader() throws ClassNotFoundException {
        loader = SimpleLoader.createManual("proxytest", Arrays.asList(
                "com/draagon/meta/loader/simple/fruitbasket-proxy-metadata.xml"
        ));

        builder = MetaObjectGsonInitializer.getBuilderWithAdapters(loader);
    }

    @Test
    public void testSerializer() throws ClassNotFoundException {

        Gson gson = builder.create();

        Apple f = loader.newObjectInstance( Apple.class );
        f.setId(1L);
        f.setName("apple");

        String s = gson.toJson( f );

        Assert.assertTrue( s.contains("@type"));

        gson = builder.create();

        Apple a = (Apple) gson.fromJson(s, Apple.class);

        Assert.assertEquals( Long.valueOf(1L), a.getId());
    }

    @Test
    public void testSerializerWriter() throws ClassNotFoundException, IOException {

        StringWriter sw = new StringWriter();

        Apple f = loader.newObjectInstance( Apple.class );
        f.setId(1L);
        f.setName("apple");

        JsonObjectWriter writer = new JsonObjectWriter(loader,sw);
        writer.write(f);
        writer.close();

        String s = sw.toString();

        Assert.assertTrue( s.contains("@type"));
    }
}
