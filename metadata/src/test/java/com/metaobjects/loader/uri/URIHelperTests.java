package com.metaobjects.loader.uri;

import static org.junit.Assert.*;
import static com.metaobjects.loader.uri.URIHelper.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class URIHelperTests {

    final static String TEST_TYPES_RESOURCE = "com/draagon/meta/loader/simple/acme-common-metadata.json";
    final static String TEST_TYPES_BASEDIR = "./src/test/resources";
    final static String TEST_TYPES_FILE = TEST_TYPES_BASEDIR + "/" + TEST_TYPES_RESOURCE;

    @Test
    public void uriValidTypesTest() {
        for (String type : VALID_URI_TYPES) {
            validateUriType(type);
            assertTrue(isValidUriType(type));
            assertFalse(isValidUriType(type + "bad"));
        }
        assertFalse(isValidUriType("bad"));
        assertFalse(isValidUriType(null));
    }

    @Test
    public void uriValidSourceTypesTest() {
        for (String type : VALID_URI_SOURCE_TYPES) {
            validateUriSourceType(type);
            assertTrue(isValidUriSourceType(type));
            assertFalse(isValidUriSourceType(type + "bad"));
        }
        assertFalse(isValidUriSourceType("bad"));
        assertFalse(isValidUriSourceType(null));
    }

    @Test
    public void uriValidResourceSourceTest() {

        final String TYPES_RESOURCE = "com/draagon/meta/loader/simple/acme-common-metadata.json";

        validateUriSource(URI_SOURCE_RESOURCE, TYPES_RESOURCE);

        URL url = getClass().getClassLoader().getResource(TYPES_RESOURCE);
        validateUriSource(URI_SOURCE_RESOURCE, url.toString());

        // url = getClass().getClassLoader().getResource( "/java/io/Reader.class" );
        // validateUriSource( URI_SOURCE_RESOURCE, url.toString() );
    }

    @Test
    public void uriValidFileSourceTest() throws URISyntaxException {

        // Windows
        if ( File.pathSeparatorChar=='\\') {
            assertFalse(isValidUriSource(URI_SOURCE_FILE, "\\/\\/\\/\\/"));
        }
        // Unix/Mac
        else if ( File.pathSeparatorChar=='/') {
            assertTrue(isValidUriSource(URI_SOURCE_FILE, "\\/\\/\\/\\/"));
        }

        validateUriSource(URI_SOURCE_FILE, TEST_TYPES_FILE);

        URL url = getClass().getClassLoader().getResource(TEST_TYPES_RESOURCE);

        validateUriSource(URI_SOURCE_FILE, url.toURI().getPath());

        File f = new File(url.toURI());
        validateUriSource(URI_SOURCE_FILE, f.toString());
        validateUriSource(URI_SOURCE_FILE, f.getName());

        URI uri = toURI(URI_TYPE_MODEL, f);
        assertTrue(uri.toString().startsWith("model:file:"));

        // url = getClass().getClassLoader().getResource( "/java/io/Reader.class" );
        // validateUriSource( URI_SOURCE_RESOURCE, url.toString() );
    }

    @Test
    public void uriModelTests() throws URISyntaxException {

        URL url = getClass().getClassLoader().getResource(TEST_TYPES_RESOURCE);
        String in = url.getPath();
        URIModel uriModel = toURIModel(URIConstants.URI_TYPE_TYPES + ":" + URIConstants.URI_SOURCE_FILE + ":" + in);

        assertEquals(URI_TYPE_TYPES, uriModel.getUriType());
        assertEquals(URI_SOURCE_FILE, uriModel.getUriSourceType());
        assertEquals(in, uriModel.getUriSource());
    }

    @Test
    public void uriModelRoundTrips() throws URISyntaxException {

        assertTrue(roundTrip(true, "types:https://www.acme.com/aarc/qwrqwr/qwr/qwr/qw/r"));

        // Pass tests
        assertTrue(roundTrip(true, "types", "file", "C:/test"));
        assertTrue(roundTrip(true, "types", "file", "./aarc/qwrqwr/qwr/qwr/qw/r"));
        assertTrue(roundTrip(false, "model", "url", "./aarc/qwrqwr/qwr/qwr/qw/r"));
        assertTrue(roundTrip(false, "url:./aarc/qwrqwr/qwr/qwr/qw/r"));
        assertTrue(roundTrip(true, "url:http://www.acme.com/aarc/qwrqwr/qwr/qwr/qw/r"));
        assertTrue(roundTrip(false, "url:htp://www.acme.com/aarc/qwrqwr/qwr/qwr/qw/r"));
        assertTrue(roundTrip(true, "https://www.acme.com/aarc/qwrqwr/qwr/qwr/qw/r"));

        // Fail tests
        assertTrue(roundTrip(false, "sdf", "file", "C:/test"));
    }

    protected boolean roundTrip(boolean shouldPass, String t, String st, String s) {
        return roundTrip(shouldPass, t + ":" + st + ":" + s);
    }

    protected boolean roundTrip(boolean shouldPass, String in) {
        try {
            URIModel uriModel = toURIModel(in);
            URI uri1 = toURI(in);

            assertEquals("uris match", uriModel.toURI(), uri1);
        } catch (IllegalArgumentException e) {
            if (shouldPass) throw e;
            return true;
        }
        if (!shouldPass) return false;
        return true;
    }

    @Test
    public void uriFileInputStreamTests() throws IOException {
        File f = new File(TEST_TYPES_FILE);
        InputStream is = getInputStream(toURI(URI_TYPE_MODEL, f));
        is.close();

        is = getInputStream(toURI("types:resource:" + TEST_TYPES_RESOURCE));
        is.close();

        is = getInputStream(toURI("model:url:https://postman-echo.com/get?foo1=bar1&foo2=bar2"));
        is.close();
    }

    @Test
    public void uriURIArgTest() throws URISyntaxException {

        String uriString = "types:resource:"+TEST_TYPES_FILE+";sourceDir="+TEST_TYPES_BASEDIR;
        URIModel m = toURIModel(uriString);
        assertEquals( TEST_TYPES_BASEDIR, m.getUriArg("sourceDir"));
        assertEquals( TEST_TYPES_FILE, m.getUriSource());
        assertEquals( uriString, m.toURI().toString());
    }
}