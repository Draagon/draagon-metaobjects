package com.metaobjects.loader;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for LoaderConfigurationBuilder
 */
public class LoaderConfigurationBuilderTest {

    @Test
    public void testBuilderWithAllProperties() {
        ClassLoader testClassLoader = getClass().getClassLoader();
        Map<String, String> args = new HashMap<>();
        args.put("customArg", "customValue");

        LoaderConfigurable.LoaderConfiguration config = new LoaderConfigurationBuilder()
                .sourceDir("/test/source")
                .classLoader(testClassLoader)
                .source("source1.json")
                .source("source2.json")
                .sources(Arrays.asList("source3.json", "source4.json"))
                .argument("testArg", "testValue")
                .arguments(args)
                .register(true)
                .verbose(false)
                .strict(true)
                .build();

        assertEquals("/test/source", config.getSourceDir());
        assertEquals(testClassLoader, config.getClassLoader());
        assertEquals(4, config.getSources().size());
        assertTrue(config.getSources().contains("source1.json"));
        assertTrue(config.getSources().contains("source2.json"));
        assertTrue(config.getSources().contains("source3.json"));
        assertTrue(config.getSources().contains("source4.json"));

        Map<String, String> configArgs = config.getArguments();
        assertEquals("testValue", configArgs.get("testArg"));
        assertEquals("customValue", configArgs.get("customArg"));
        assertEquals("true", configArgs.get(LoaderConfigurationConstants.ARG_REGISTER));
        assertEquals("false", configArgs.get(LoaderConfigurationConstants.ARG_VERBOSE));
        assertEquals("true", configArgs.get(LoaderConfigurationConstants.ARG_STRICT));
    }

    @Test
    public void testBuilderWithMinimalProperties() {
        LoaderConfigurable.LoaderConfiguration config = new LoaderConfigurationBuilder()
                .build();

        assertNull(config.getSourceDir());
        assertNull(config.getClassLoader());
        assertEquals(0, config.getSources().size());
        assertEquals(0, config.getArguments().size());
    }

    @Test
    public void testBuilderWithNullCollections() {
        LoaderConfigurable.LoaderConfiguration config = new LoaderConfigurationBuilder()
                .sources(null)
                .arguments(null)
                .build();

        assertNull(config.getSourceDir());
        assertNull(config.getClassLoader());
        assertEquals(0, config.getSources().size());
        assertEquals(0, config.getArguments().size());
    }

    @Test
    public void testConfigurationConstants() {
        assertEquals("register", LoaderConfigurationConstants.ARG_REGISTER);
        assertEquals("verbose", LoaderConfigurationConstants.ARG_VERBOSE);
        assertEquals("strict", LoaderConfigurationConstants.ARG_STRICT);
    }
}