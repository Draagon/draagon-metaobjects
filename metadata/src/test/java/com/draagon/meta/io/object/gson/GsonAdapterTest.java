package com.draagon.meta.io.object.gson;

import com.draagon.meta.io.object.json.JsonObjectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.registry.SharedTestRegistry;
import com.draagon.meta.test.proxy.fruitbasket.Apple;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;

/**
 * GSON adapter test using SharedTestRegistry to prevent test interference.
 */
public class GsonAdapterTest {

    private static final Logger log = LoggerFactory.getLogger(GsonAdapterTest.class);

    protected final String BASKET_TO_FRUIT = "simple::fruitbasket::BasketToFruit";
    protected MetaDataLoader loader = null;
    protected GsonBuilder builder = null;

    @Before
    public void initLoader() throws ClassNotFoundException {
        // Use SharedTestRegistry to ensure proper provider discovery
        SharedTestRegistry.getInstance();
        log.debug("GsonAdapterTest setup with shared registry: {}", SharedTestRegistry.getStatus());

        // Create loader using the same pattern as SimpleLoaderTestBase
        SimpleLoader simpleLoader = new SimpleLoader("gsontest")
                .setSourceURIs(Arrays.asList(
                        URI.create("model:resource:com/draagon/meta/loader/simple/fruitbasket-proxy-metadata.json")
                ))
                .init();

        loader = simpleLoader;
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
