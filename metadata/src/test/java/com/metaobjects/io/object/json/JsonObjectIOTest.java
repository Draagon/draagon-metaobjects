package com.metaobjects.io.object.json;

import com.metaobjects.io.MetaDataIOException;
import com.metaobjects.io.object.ObjectIOTestBase;
import com.metaobjects.object.mapped.MappedObject;
import org.junit.Assert;

import java.io.*;

public class JsonObjectIOTest extends ObjectIOTestBase {

    private final static String PRE = "json-valueobject-io-test-";

    @Override
    protected void runTest(MappedObject o, String name) throws IOException, MetaDataIOException {

        String filename = PRE+name+".json";

        writeJson(filename, o);
        Object o2 = readJson(filename,o.getMetaData());

        Assert.assertEquals(name+"-json", o, o2);
    }
}
