package com.draagon.meta.io.value.json;

import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.value.ValueObjectIOTestBase;
import com.draagon.meta.object.value.ValueObject;
import org.junit.Assert;

import java.io.*;

public class JsonValueObjectIOTest extends ValueObjectIOTestBase {

    private final static String PRE = "json-valueobject-io-test-";

    protected void runTest(ValueObject o, String name) throws IOException, MetaDataIOException {

        String filename = PRE+name+".json";

        writeJson(filename, o);
        ValueObject o2 = readJson(filename);

        Assert.assertEquals(name+"-json", o, o2);
    }
}
