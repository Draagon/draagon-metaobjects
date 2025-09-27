package com.metaobjects.mojo;

import com.metaobjects.loader.LoaderConfigurable;
import com.metaobjects.loader.LoaderConfigurationConstants;
import com.metaobjects.loader.MetaDataLoader;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for MavenLoaderConfiguration
 */
public class MavenLoaderConfigurationTest {

    private TestLoaderConfigurable testLoader;
    private ClassLoader testClassLoader;
    private List<String> testSources;
    private Map<String, String> testGlobals;

    @Before
    public void setUp() {
        testLoader = new TestLoaderConfigurable();
        testClassLoader = getClass().getClassLoader();
        testSources = Arrays.asList("source1.json", "source2.json");
        testGlobals = new HashMap<>();
        testGlobals.put(LoaderConfigurationConstants.ARG_REGISTER, "true");
        testGlobals.put(LoaderConfigurationConstants.ARG_VERBOSE, "false");
        testGlobals.put("customArg", "customValue");
    }

    @Test
    public void testConfigureWithAllParameters() {
        String sourceDir = "/test/source";

        MavenLoaderConfiguration.configure(testLoader, sourceDir, testClassLoader, testSources, testGlobals);

        LoaderConfigurable.LoaderConfiguration config = testLoader.getReceivedConfiguration();
        assertNotNull("Configuration should have been received", config);

        assertEquals(sourceDir, config.getSourceDir());
        assertEquals(testClassLoader, config.getClassLoader());
        assertEquals(testSources, config.getSources());
        assertEquals(testGlobals, config.getArguments());
    }

    @Test
    public void testConfigureWithNulls() {
        MavenLoaderConfiguration.configure(testLoader, null, null, null, null);

        LoaderConfigurable.LoaderConfiguration config = testLoader.getReceivedConfiguration();
        assertNotNull("Configuration should have been received", config);

        assertNull(config.getSourceDir());
        assertNull(config.getClassLoader());
        assertTrue(config.getSources().isEmpty());
        assertTrue(config.getArguments().isEmpty());
    }

    @Test
    public void testConfigureWithEmptyCollections() {
        List<String> emptySources = Arrays.asList();
        Map<String, String> emptyGlobals = new HashMap<>();

        MavenLoaderConfiguration.configure(testLoader, "/test", testClassLoader, emptySources, emptyGlobals);

        LoaderConfigurable.LoaderConfiguration config = testLoader.getReceivedConfiguration();
        assertNotNull("Configuration should have been received", config);

        assertEquals("/test", config.getSourceDir());
        assertEquals(testClassLoader, config.getClassLoader());
        assertTrue(config.getSources().isEmpty());
        assertTrue(config.getArguments().isEmpty());
    }

    /**
     * Test implementation of LoaderConfigurable for testing purposes
     */
    private static class TestLoaderConfigurable implements LoaderConfigurable {
        private LoaderConfiguration receivedConfiguration;

        @Override
        public void configure(LoaderConfiguration config) {
            this.receivedConfiguration = config;
        }

        @Override
        public MetaDataLoader getLoader() {
            return null; // Not needed for this test
        }

        public LoaderConfiguration getReceivedConfiguration() {
            return receivedConfiguration;
        }
    }
}