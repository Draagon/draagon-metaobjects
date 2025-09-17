package com.draagon.meta.generator;

import com.draagon.meta.generator.GeneratorTestBase;
import com.draagon.meta.loader.simple.SimpleLoader;
import org.junit.Test;
import org.junit.Assert;

import java.util.Collections;

/**
 * Basic test to verify that the test infrastructure is working
 */
public class BasicInfrastructureTest extends GeneratorTestBase {

    @Test
    public void testInfrastructureWorks() {
        // Test that we can create a simple loader
        SimpleLoader loader = initLoader(Collections.emptyList());
        Assert.assertNotNull("Loader should not be null", loader);
        Assert.assertNotNull("Loader name should not be null", loader.getName());
    }
}