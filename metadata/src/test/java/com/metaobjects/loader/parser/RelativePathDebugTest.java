package com.metaobjects.loader.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import com.metaobjects.MetaData;
import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.loader.simple.SimpleLoader;
import com.metaobjects.util.MetaDataUtil;

/**
 * Debug test to verify relative path resolution is working correctly
 * for cross-file references like "..::common::id"
 */
public class RelativePathDebugTest {

    @Test
    public void testRelativePathExpansion() {
        // Test the utility method directly
        String basePackage = "acme::vehicle";
        String relativeRef = "..::common::id";

        String expanded = MetaDataUtil.expandPackageForMetaDataRef(basePackage, relativeRef);
        System.out.println("DEBUG: Base package: " + basePackage);
        System.out.println("DEBUG: Relative ref: " + relativeRef);
        System.out.println("DEBUG: Expanded result: " + expanded);

        assertEquals("acme::common::id", expanded);
    }

    @Test
    public void testSimpleLoaderCrossFileReferences() throws Exception {
        SimpleLoader loader = new SimpleLoader("debug-loader");

        // Load the metadata files in a list
        java.util.List<java.net.URI> uris = java.util.Arrays.asList(
            getClass().getClassLoader().getResource("com/draagon/meta/loader/simple/acme-common-metadata.json").toURI(),
            getClass().getClassLoader().getResource("com/draagon/meta/loader/simple/acme-vehicle-metadata.json").toURI()
        );
        loader.setSourceURIs(uris);

        try {
            loader.init();

            // Try to find the Vehicle object
            MetaData vehicleObject = loader.getChildOfType("object", "acme::vehicle::Vehicle");
            assertNotNull("Vehicle object should be found", vehicleObject);

            // Debug: What children does the Vehicle object actually have?
            System.out.println("VEHICLE DEBUG: Vehicle object has " + vehicleObject.getChildren().size() + " children:");
            for (MetaData child : vehicleObject.getChildren()) {
                System.out.println("  - " + child.getClass().getSimpleName() + " named '" + child.getName() + "' with package '" + child.getPackage() + "'");
            }

            // Try to find a field that uses super reference
            MetaData idField = vehicleObject.getChild("id", MetaData.class);
            assertNotNull("ID field should be found", idField);

            System.out.println("DEBUG: Successfully loaded cross-file references");

        } catch (Exception e) {
            System.err.println("ERROR: Cross-file reference loading failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}