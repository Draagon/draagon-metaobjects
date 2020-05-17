package com.draagon.meta.io.object.json;

import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.object.ObjectIOTestBase;
import com.draagon.meta.object.value.ValueObject;
import org.junit.Assert;

import java.io.*;

public class JsonObjectIOTest extends ObjectIOTestBase {

    private final static String PRE = "json-valueobject-io-test-";

    protected void runTest(ValueObject o, String name) throws IOException, MetaDataIOException {

        String filename = PRE+name+".json";

        writeJson(filename, o);
        ValueObject o2 = readJson(filename);

        Assert.assertEquals(name+"-json", o, o2);
    }
}
